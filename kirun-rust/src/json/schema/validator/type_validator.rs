//! Port of kirun-js `json/schema/validator/TypeValidator.ts`.

use crate::json::schema::convertor::{
    BooleanConvertor, ConversionMode, NullConvertor, NumberConvertor, StringConvertor,
};
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::validator::schema_validator::SchemaParents;
use crate::json::schema::validator::{
    ArrayValidator, BooleanValidator, NullValidator, NumberValidator, ObjectValidator, StringValidator,
};
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::{Result, Value};

pub struct TypeValidator;

impl TypeValidator {
    /// JS `TypeValidator.validate(parents, type, schema, repository, element)`.
    pub async fn validate(
        parents: &[Schema],
        r#type: SchemaType,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
    ) -> Result<Value> {
        Self::validate_with(parents, r#type, schema, repository, element, false, None).await
    }

    /// JS `TypeValidator.validate(..., convert, mode)`.
    pub async fn validate_with(
        parents: &[Schema],
        r#type: SchemaType,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut p = SchemaParents::from_slice(parents);
        Self::validate_in(&mut p, r#type, schema, repository, element, convert, mode).await
    }

    pub(crate) async fn validate_in(
        parents: &mut SchemaParents,
        r#type: SchemaType,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        match r#type {
            SchemaType::Object => {
                ObjectValidator::validate_in(parents, schema, repository, element, convert, mode).await
            }
            SchemaType::Array => {
                ArrayValidator::validate_in(parents, schema, repository, element, convert, mode).await
            }
            _ => Self::handle_type_validation_and_conversion(parents, r#type, schema, element, convert, mode),
        }
    }

    fn handle_type_validation_and_conversion(
        parents: &SchemaParents,
        r#type: SchemaType,
        schema: &Schema,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mode = mode.unwrap_or(ConversionMode::Strict);
        let c_element =
            if convert { Self::convert_element(parents, r#type, schema, &element, mode)? } else { element };
        Self::validate_element(parents, r#type, schema, c_element)
    }

    /// Object and array never reach here (JS falls to `handleUnConvertibleValueWithDefault`
    /// for them, which `validate_in` makes unreachable).
    fn convert_element(
        parents: &SchemaParents,
        r#type: SchemaType,
        schema: &Schema,
        element: &Value,
        mode: ConversionMode,
    ) -> Result<Value> {
        match r#type {
            SchemaType::String => StringConvertor::convert_in(parents, schema, mode, element),
            SchemaType::Integer | SchemaType::Long | SchemaType::Double | SchemaType::Float => {
                NumberConvertor::convert_in(parents, r#type, schema, mode, element)
            }
            SchemaType::Boolean => BooleanConvertor::convert_in(parents, schema, mode, element),
            SchemaType::Null => NullConvertor::convert_in(parents, schema, mode, element),
            SchemaType::Object | SchemaType::Array => crate::util::json::convertor_util::ConvertorUtil::
                handle_un_convertible_value_with_default(
                    parents,
                    Some(mode),
                    element,
                    schema.get_default_value().cloned().unwrap_or(Value::Null),
                    &format!("{} is not a valid type for conversion.", r#type.as_str()),
                ),
        }
    }

    fn validate_element(parents: &SchemaParents, r#type: SchemaType, schema: &Schema, element: Value) -> Result<Value> {
        match r#type {
            SchemaType::String => StringValidator::validate_in(parents, schema, element),
            SchemaType::Integer | SchemaType::Long | SchemaType::Double | SchemaType::Float => {
                NumberValidator::validate_in(r#type, parents, schema, element)
            }
            SchemaType::Boolean => BooleanValidator::validate_in(parents, schema, element),
            SchemaType::Null => NullValidator::validate_in(parents, schema, element),
            SchemaType::Object | SchemaType::Array => crate::util::json::validator_util::ValidatorUtil::
                handle_validation_error(
                    parents,
                    Some(ConversionMode::Strict),
                    &element,
                    schema.get_default_value().cloned().unwrap_or(Value::Null),
                    &format!("{} is not a valid type.", r#type.as_str()),
                ),
        }
    }
}
