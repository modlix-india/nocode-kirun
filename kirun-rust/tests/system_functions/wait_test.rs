//! Port of kirun-js `__tests__/engine/function/system/WaitTest.ts`.

use std::time::Instant;

use kirun::function::system::wait::Wait;
use kirun::function::Function;

use crate::common::*;

#[tokio::test]
async fn wait_test() {
    let wait_function = Wait::new();
    let wait_time: u128 = 1000;
    let start_time = Instant::now();
    let mut fep = fep().set_arguments(args! { "millis" => 1000 });
    wait_function.execute(&mut fep).await.unwrap();

    assert!(start_time.elapsed().as_millis() >= wait_time);
}

#[tokio::test]
async fn wait_test_3500ms() {
    let wait_function = Wait::new();
    let wait_time: u128 = 3500;
    let start_time = Instant::now();
    let mut fep = fep().set_arguments(args! { "millis" => 3500 });
    wait_function.execute(&mut fep).await.unwrap();

    assert!(start_time.elapsed().as_millis() >= wait_time);
}

#[tokio::test]
async fn wait_test_immediately() {
    let wait_function = Wait::new();
    let start_time = Instant::now();
    let mut fep = fep().set_arguments(args! {});
    wait_function.execute(&mut fep).await.unwrap();

    assert!(start_time.elapsed().as_millis() < 50);
}

#[tokio::test]
async fn wait_test_error_with_negative_number() {
    let wait_function = Wait::new();
    let mut fep = fep().set_arguments(args! { "millis" => -3500 });

    // JS `rejects.toThrow()` with no message: any error.
    assert!(wait_function.execute(&mut fep).await.is_err());
}
