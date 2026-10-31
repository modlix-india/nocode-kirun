#[path = "../common/mod.rs"]
mod common;

mod catch_errors;
mod generate_event_literal;
mod graph_debug_test;
mod param_type_array_round_trip;
mod readable_layout;
// kirun-py-only DSL cases (tests/dsl/test_transformer_shapes.py and the parts of
// test_param_type_array_round_trip.py the JS spec does not cover).
mod py_param_type_array_round_trip;
mod py_transformer_shapes;
