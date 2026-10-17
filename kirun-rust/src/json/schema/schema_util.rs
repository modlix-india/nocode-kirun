//! Port of kirun-js `json/schema/SchemaUtil.ts`.

use std::sync::Arc;

use futures::future::BoxFuture;

use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::util::primitive::js_truthy;
use crate::util::string::string_util::StringUtil;
use crate::{KIRunError, Result, Value};

const UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH: &str = "Unable to retrive schema from referenced path";

const CYCLIC_REFERENCE_LIMIT_COUNTER: u32 = 20;

pub struct SchemaUtil;

impl SchemaUtil {
    /// JS `getDefaultValue`: a truthy constant, else a non-null default, else the default of
    /// the referenced schema.
    pub async fn get_default_value(
        s: Option<&Schema>,
        s_repository: Option<&dyn Repository<Schema>>,
    ) -> Result<Value> {
        let Some(s) = s else {
            return Ok(Value::Null);
        };
        if let Some(v) = Self::own_default(s) {
            return Ok(v);
        }
        let mut next = Self::get_schema_from_ref_arc(Arc::new(s.clone()), s_repository, s.get_ref(), 0).await?;
        // The JS recursion, unrolled: each step follows the next schema's ref.
        while let Some(s) = next {
            if let Some(v) = Self::own_default(&s) {
                return Ok(v);
            }
            let r = s.get_ref().map(str::to_string);
            next = Self::get_schema_from_ref_arc(s, s_repository, r.as_deref(), 0).await?;
        }
        Ok(Value::Null)
    }

    fn own_default(s: &Schema) -> Option<Value> {
        if let Some(c) = s.get_constant().filter(|c| js_truthy(c)) {
            return Some(c.clone());
        }
        s.get_default_value().filter(|d| !d.is_null()).cloned()
    }

    /// JS `hasDefaultValueOrNullSchemaType`.
    pub async fn has_default_value_or_null_schema_type(
        s: Option<&Schema>,
        s_repository: Option<&dyn Repository<Schema>>,
    ) -> Result<bool> {
        let Some(s) = s else {
            return Ok(false);
        };
        let mut current = Arc::new(s.clone());
        loop {
            // kirun-java: any constant or default value counts; a schema with neither, no
            // `$ref` and no `type` fails (`getType().getAllowedSchemaTypes()` on null).
            if current.get_constant().is_some() || current.get_default_value().is_some() {
                return Ok(true);
            }
            if current.get_ref().is_none() {
                return match current.get_type() {
                    Some(t) => Ok(t.contains(SchemaType::Null)),
                    None => Err(KIRunError::uncaught(
                        "Cannot invoke \"com.fincity.nocode.kirun.engine.json.schema.type.Type.getAllowedSchemaTypes()\" because the return value of \"com.fincity.nocode.kirun.engine.json.schema.Schema.getType()\" is null",
                    )),
                };
            }
            let r = current.get_ref().map(str::to_string);
            match Self::get_schema_from_ref_arc(current, s_repository, r.as_deref(), 0).await? {
                Some(next) => current = next,
                None => return Ok(false),
            }
        }
    }

    /// Resolves `ref` (`#/...` local or `Namespace.Name/...` external) against `schema`.
    pub async fn get_schema_from_ref(
        schema: &Schema,
        s_repository: Option<&dyn Repository<Schema>>,
        r#ref: Option<&str>,
    ) -> Result<Option<Schema>> {
        Ok(Self::get_schema_from_ref_arc(Arc::new(schema.clone()), s_repository, r#ref, 0)
            .await?
            .map(|s| Arc::try_unwrap(s).unwrap_or_else(|a| (*a).clone())))
    }

    /// JS `getSchemaFromRef(schema, repository, ref, iteration)` on shared schemas.
    pub(crate) fn get_schema_from_ref_arc<'a>(
        schema: Arc<Schema>,
        s_repository: Option<&'a dyn Repository<Schema>>,
        r#ref: Option<&'a str>,
        iteration: u32,
    ) -> BoxFuture<'a, Result<Option<Arc<Schema>>>> {
        Box::pin(async move {
            let iteration = iteration + 1;
            if iteration == CYCLIC_REFERENCE_LIMIT_COUNTER {
                return Err(KIRunError::schema_validation(
                    r#ref.unwrap_or(""),
                    "Schema has a cyclic reference",
                ));
            }

            let Some(r) = r#ref.filter(|r| !StringUtil::is_null_or_blank(Some(r))) else {
                return Ok(None);
            };

            let mut schema = schema;
            let mut r = r.to_string();

            if !r.starts_with('#') {
                match Self::resolve_external_schema(s_repository, &r).await? {
                    Some((s, nr)) => {
                        schema = s;
                        r = nr;
                    }
                    // JS keeps the referring schema here and returns it as the resolution, so a
                    // schema whose ref cannot be found "resolves" to its root (and validating
                    // it loops forever when that root is the ref itself). Not found is None.
                    None => return Ok(None),
                }
            }

            let parts: Vec<&str> = r.split('/').collect();
            let i = 1;

            if i == parts.len() {
                return Ok(Some(schema));
            }

            Self::resolve_internal_schema(schema, s_repository, &r, iteration, &parts, i).await
        })
    }

    async fn resolve_internal_schema(
        in_schema: Arc<Schema>,
        s_repository: Option<&dyn Repository<Schema>>,
        r#ref: &str,
        iteration: u32,
        parts: &[&str],
        mut i: usize,
    ) -> Result<Option<Arc<Schema>>> {
        let mut schema: Arc<Schema> = in_schema;
        if i == parts.len() {
            return Ok(None);
        }
        let not_found = || KIRunError::schema_reference(r#ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH);
        while i < parts.len() {
            let next: Option<Schema> = if parts[i] == "$defs" {
                i += 1;
                if i >= parts.len() || schema.get_defs().is_none() {
                    return Err(not_found());
                }
                schema.get_defs().and_then(|d| d.get(parts[i])).cloned()
            } else {
                if !schema.get_type().is_some_and(|t| t.contains(SchemaType::Object)) || schema.get_properties().is_none()
                {
                    return Err(KIRunError::schema_reference(
                        r#ref,
                        "Cannot retrievie schema from non Object type schemas",
                    ));
                }
                schema.get_properties().and_then(|p| p.get(parts[i])).cloned()
            };

            i += 1;

            let Some(next) = next else {
                return Err(not_found());
            };
            schema = Arc::new(next);

            if !StringUtil::is_null_or_blank(schema.get_ref()) {
                let r = schema.get_ref().map(str::to_string);
                schema = Self::get_schema_from_ref_arc(schema.clone(), s_repository, r.as_deref(), iteration)
                    .await?
                    .ok_or_else(not_found)?;
            }
        }
        Ok(Some(schema))
    }

    async fn resolve_external_schema(
        s_repository: Option<&dyn Repository<Schema>>,
        r#ref: &str,
    ) -> Result<Option<(Arc<Schema>, String)>> {
        let Some(repo) = s_repository else {
            return Ok(None);
        };

        let (first, rest) = match r#ref.split_once('/') {
            Some((a, b)) => (a, Some(b)),
            None => (r#ref, None),
        };
        if first.is_empty() {
            return Ok(None);
        }

        let Some((namespace, name)) = first.rsplit_once('.') else {
            return Ok(None);
        };
        if namespace.is_empty() || name.is_empty() {
            return Ok(None);
        }

        let Some(schema) = repo.find(namespace, name).await else {
            return Ok(None);
        };
        match rest {
            None | Some("") => Ok(Some((schema, r#ref.to_string()))),
            Some(rest) => Ok(Some((schema, format!("#/{rest}")))),
        }
    }
}
