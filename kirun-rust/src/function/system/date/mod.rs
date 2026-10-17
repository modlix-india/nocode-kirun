//! `System.Date` functions: the kirun-js set and signatures, behaving as kirun-java's (see
//! `java_time` and `java_format`).

pub mod abstract_date_function;
pub mod add_subtract_time;
pub mod common;
pub mod date_function_repository;
pub mod epoch_to_timestamp;
pub mod from_date_string;
pub mod from_now;
pub mod get_current;
pub mod get_names;
pub mod is_between;
pub mod is_valid_iso_date;
pub mod java_format;
pub mod java_time;
pub mod last_first_of;
pub mod set_time_zone;
pub mod start_end_of;
pub mod time_as;
pub mod timestamp_to_epoch;
pub mod to_date_string;
