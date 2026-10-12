//! Port of kirun-js `__tests__/engine/repository/KIRunFunctionRepositoryTest.ts`.

use kirun::namespaces::Namespaces;
use kirun::repository::{KIRunFunctionRepository, Repository};

#[tokio::test]
async fn ki_run_function_repository_test() {
    let repo = KIRunFunctionRepository::new();

    let fun = repo.find(Namespaces::STRING, "ToString").await;
    assert!(fun.is_some());
    assert_eq!(fun.unwrap().get_signature().get_name(), "ToString");

    let fun = repo.find(Namespaces::STRING, "IndexOfWithStartPoint").await;
    assert!(fun.is_some());
    assert_eq!(fun.unwrap().get_signature().get_name(), "IndexOfWithStartPoint");

    let fun = repo.find(Namespaces::SYSTEM_ARRAY, "Compare").await;
    assert!(fun.is_some());
    assert_eq!(fun.unwrap().get_signature().get_name(), "Compare");

    let fun = repo.find(Namespaces::MATH, "RandomInt").await;
    assert!(fun.is_some());
    assert_eq!(fun.unwrap().get_signature().get_name(), "RandomInt");

    let fun = repo.find(Namespaces::MATH, "Exponential").await;
    assert!(fun.is_some());
    assert_eq!(fun.unwrap().get_signature().get_name(), "Exponential");

    let fun = repo.find(Namespaces::SYSTEM, "If").await;
    assert!(fun.is_some());
    assert_eq!(fun.unwrap().get_signature().get_name(), "If");
}
