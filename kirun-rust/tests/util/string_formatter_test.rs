//! Port of kirun-js `__tests__/engine/util/string/StringFormatterTest.ts`.

use kirun::util::string::string_formatter::StringFormatter;

#[test]
fn string_formatter_test() {
    assert_eq!(StringFormatter::format("Hello $", &["Kiran"]), "Hello Kiran");
    assert_eq!(StringFormatter::format("\\$Hello $", &["Kiran"]), "$Hello Kiran");
    assert_eq!(StringFormatter::format("Hi Hello How are you $?", &[]), "Hi Hello How are you $?");
    assert_eq!(
        StringFormatter::format("Hi Hello How are you $$$$", &["1", "2", "3"]),
        "Hi Hello How are you 123$"
    );
    assert_eq!(
        StringFormatter::format("Hi Hello How are you \\$$$$", &["1", "2", "3"]),
        "Hi Hello How are you $123"
    );
    assert_eq!(
        StringFormatter::format("Hi Hello How are you $$$\\$", &["1", "2"]),
        "Hi Hello How are you 12$$"
    );
    assert_eq!(StringFormatter::format("Extra closing $ found", &["}"]), "Extra closing } found");
}
