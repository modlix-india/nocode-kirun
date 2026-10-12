//! Port of kirun-js `__tests__/engine/repository/RepositoryFilterTest.ts`.

use kirun::repository::{KIRunFunctionRepository, KIRunSchemaRepository, Repository};

#[tokio::test]
async fn repository_filter_test() {
    let func_repo = KIRunFunctionRepository::new();
    let schema_repo = KIRunSchemaRepository::new();

    assert_eq!(
        func_repo.filter("Rep").await,
        vec![
            "System.String.Repeat",
            "System.String.Replace",
            "System.String.ReplaceFirst",
            "System.String.PrePad",
            "System.String.ReplaceAtGivenPosition",
        ]
    );

    assert_eq!(func_repo.filter("root").await, vec!["System.Math.CubeRoot", "System.Math.SquareRoot"]);

    assert_eq!(schema_repo.filter("root").await, Vec::<String>::new());

    assert_eq!(schema_repo.filter("rin").await, vec!["System.string"]);

    assert_eq!(schema_repo.filter("ny").await, vec!["System.any"]);

    let mut all = schema_repo.filter("").await;
    all.sort();
    assert_eq!(
        all,
        vec![
            "System.Date.Duration",
            "System.Date.TimeObject",
            "System.Date.Timestamp",
            "System.Date.Timeunit",
            "System.Null",
            "System.ParameterExpression",
            "System.Schema",
            "System.any",
            "System.boolean",
            "System.double",
            "System.float",
            "System.integer",
            "System.long",
            "System.number",
            "System.string",
        ]
    );
}
