//! Port of kirun-js `__tests__/engine/function/system/array/FillTest.ts`.

use kirun::function::system::array::fill::Fill;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn fill_test() {
    let fill = Fill::new();

    let array = json!([0, 1]);

    let fep = crate::common::fep().set_arguments(args! { "source" => array, "element" => 3 });
    let array = first_value(&fill, fep, "result").await.unwrap();
    assert_json_eq!(array.clone(), json!([3, 3]));

    let fep = crate::common::fep()
        .set_arguments(args! { "source" => array, "element" => 5, "srcFrom" => 2, "length" => 5 })
        .set_context(kirun::Map::new())
        .set_steps(kirun::Map::new());

    let fin_array = json!([3, 3, 5, 5, 5, 5, 5]);
    let array = first_value(&fill, fep, "result").await.unwrap();
    assert_json_eq!(array.clone(), fin_array);

    let fep = crate::common::fep().set_arguments(args! { "source" => array.clone(), "element" => 25, "srcFrom" => 5 });
    let fin_array = json!([3, 3, 5, 5, 5, 25, 25]);
    assert_json_eq!(first_value(&fill, fep, "result").await.unwrap(), fin_array);

    let fep = crate::common::fep().set_arguments(args! { "source" => array, "element" => 20, "srcFrom" => -1 });
    assert!(first_value(&fill, fep, "result").await.is_err());
}
