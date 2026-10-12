//! Benchmarks shared with the kirun-js and kirun-java harnesses (see bench/README.md).
//!
//! Usage: cargo run --release --example bench [-- <workload>...]
//! Prints one JSON line per workload: { lang, workload, runs, median_ms, min_ms }.

use std::path::{Path, PathBuf};
use std::sync::Arc;
use std::time::Instant;

use kirun::dsl::DSLCompiler;
use kirun::function::Function;
use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::model::FunctionDefinition;
use kirun::repository::{KIRunFunctionRepository, KIRunSchemaRepository, Repository};
use kirun::runtime::expression::tokenextractor::TokenValueExtractor;
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::tokenextractor::ArgumentsTokenValueExtractor;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::{Map, Value};
use serde_json::json;

#[cfg(feature = "mimalloc")]
#[global_allocator]
static GLOBAL: mimalloc::MiMalloc = mimalloc::MiMalloc;

fn lang() -> &'static str {
    if cfg!(feature = "mimalloc") {
        "rust+mimalloc"
    } else {
        "rust"
    }
}

fn bench_dir() -> PathBuf {
    Path::new(env!("CARGO_MANIFEST_DIR")).join("bench")
}

fn read_json(path: &Path) -> Value {
    serde_json::from_str(&std::fs::read_to_string(path).unwrap_or_else(|e| panic!("{}: {e}", path.display()))).unwrap()
}

fn to_map(v: &Value) -> Map<Value> {
    v.as_object().map(|o| o.iter().map(|(k, v)| (k.clone(), v.clone())).collect()).unwrap_or_default()
}

/// Runs `f` `warmup` times untimed, then `runs` times timed; prints the result line.
async fn measure<F, Fut>(workload: &str, warmup: usize, runs: usize, mut f: F)
where
    F: FnMut() -> Fut,
    Fut: std::future::Future<Output = ()>,
{
    // KIRUN_BENCH_RUNS overrides the run count (handy for keeping a profiler attached).
    let runs = std::env::var("KIRUN_BENCH_RUNS").ok().and_then(|r| r.parse().ok()).unwrap_or(runs);
    for _ in 0..warmup {
        f().await;
    }
    let mut times = Vec::with_capacity(runs);
    for _ in 0..runs {
        let start = Instant::now();
        f().await;
        times.push(start.elapsed().as_secs_f64() * 1000.0);
    }
    times.sort_by(|a, b| a.partial_cmp(b).unwrap());
    println!(
        "{}",
        json!({ "lang": lang(), "workload": workload, "runs": runs, "median_ms": times[times.len() / 2], "min_ms": times[0] })
    );
}

#[tokio::main(flavor = "current_thread")]
async fn main() {
    let only: Vec<String> = std::env::args().skip(1).collect();
    let wants = |w: &str| only.is_empty() || only.iter().any(|o| o == w);
    let cfg = read_json(&bench_dir().join("config.json"));
    let f_repo: Arc<dyn Repository<dyn Function>> = Arc::new(KIRunFunctionRepository::new());
    let s_repo: Arc<dyn Repository<Schema>> = Arc::new(KIRunSchemaRepository::new());

    for (workload, def_file) in [("fibonacci", "fibonacci.json"), ("loopsum", "loopsum.json")] {
        if !wants(workload) {
            continue;
        }
        let c = &cfg[workload];
        let fd = FunctionDefinition::from_value(&read_json(&bench_dir().join("defs").join(def_file))).unwrap();
        let args = Map::from([("n".to_string(), c["n"].clone())]);
        measure(workload, c["warmup"].as_u64().unwrap() as usize, c["runs"].as_u64().unwrap() as usize, || {
            let (fd, args, f_repo, s_repo) = (fd.clone(), args.clone(), f_repo.clone(), s_repo.clone());
            async move {
                let runtime = KIRuntime::new(fd, false).unwrap();
                let mut fep = FunctionExecutionParameters::new(f_repo, s_repo).set_arguments(args);
                let mut out = runtime.execute(&mut fep).await.unwrap();
                assert!(out.next().is_some());
            }
        })
        .await;
    }

    if wants("expression") {
        let c = &cfg["expression"];
        let expression = c["expression"].as_str().unwrap().to_string();
        let extractor: Arc<dyn TokenValueExtractor> = Arc::new(ArgumentsTokenValueExtractor::new(to_map(&c["args"])));
        let values: Map<Arc<dyn TokenValueExtractor>> = Map::from([(extractor.get_prefix().to_string(), extractor)]);
        let n = c["evaluations"].as_u64().unwrap();
        // Sanity: the expression must evaluate to true in every runtime.
        assert_eq!(ExpressionEvaluator::new(&expression).evaluate(&values).unwrap(), Value::Bool(true));
        measure("expression", c["warmup"].as_u64().unwrap() as usize, c["runs"].as_u64().unwrap() as usize, || {
            let (expression, values) = (&expression, &values);
            async move {
                for _ in 0..n {
                    std::hint::black_box(ExpressionEvaluator::new(expression).evaluate(values).unwrap());
                }
            }
        })
        .await;
    }

    if wants("schema") {
        let c = &cfg["schema"];
        let schema = Schema::from_value(&read_json(&bench_dir().join("defs/record_schema.json"))).unwrap().unwrap();
        let data = read_json(&bench_dir().join("data/records.json"));
        measure("schema", c["warmup"].as_u64().unwrap() as usize, c["runs"].as_u64().unwrap() as usize, || {
            let (schema, data, s_repo) = (&schema, data.clone(), s_repo.clone());
            async move {
                let out = SchemaValidator::validate(None, Some(schema), Some(s_repo.as_ref()), data).await.unwrap();
                assert_eq!(out.as_array().map(Vec::len), Some(5000));
            }
        })
        .await;
    }

    if wants("dsl") {
        let c = &cfg["dsl"];
        let corpus = load_corpus();
        if corpus.is_empty() {
            eprintln!("dsl: no functions in test-data/functions, skipped");
        } else {
            measure("dsl", c["warmup"].as_u64().unwrap() as usize, c["runs"].as_u64().unwrap() as usize, || {
                let corpus = &corpus;
                async move {
                    for def in corpus {
                        if let Ok(text) = DSLCompiler::decompile(def) {
                            std::hint::black_box(DSLCompiler::compile(&text).ok());
                        }
                    }
                }
            })
            .await;
        }
    }
}

/// Every stored function definition under ../test-data/functions (gitignored, local only).
fn load_corpus() -> Vec<Value> {
    let root = Path::new(env!("CARGO_MANIFEST_DIR")).join("../test-data/functions");
    let mut defs = Vec::new();
    for sub in ["ui-functions", "core-functions", "page-event-functions"] {
        let Ok(entries) = std::fs::read_dir(root.join(sub)) else { continue };
        let mut files: Vec<PathBuf> = entries.filter_map(|e| e.ok().map(|e| e.path())).collect();
        files.sort();
        for f in files.into_iter().filter(|f| f.extension().is_some_and(|e| e == "json")) {
            let raw = read_json(&f);
            defs.push(raw.get("definition").cloned().unwrap_or(raw));
        }
    }
    defs
}
