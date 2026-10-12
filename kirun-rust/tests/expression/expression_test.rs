//! Port of `ExpressionTest.ts`.

use kirun::runtime::expression::Expression;

fn s(expr: &str) -> String {
    Expression::new(expr).unwrap().to_string()
}

#[test]
fn expression_test() {
    assert_eq!(s("2+3"), "(2+3)");
    // New parser produces semantically equivalent format without extra leaf parens
    assert_eq!(s("2.234 + 3 * 1.22243"), "(2.234+(3*1.22243))");
    assert_eq!(s("10*11+12*13*14/7"), "((10*11)+(12*(13*(14/7))))");
    assert_eq!(s("34 << 2 = 8 "), "((34<<2)=8)");

    let ex = Expression::new("Context.a[Steps.loop.iteration.index - 1]+ Context.a[Steps.loop.iteration.index - 2]").unwrap();
    assert_eq!(
        ex.to_string(),
        "((Context.(a[((Steps.(loop.(iteration.index)))-1)]))+(Context.(a[((Steps.(loop.(iteration.index)))-2)])))"
    );

    let ex = Expression::new("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2").unwrap();
    assert_eq!(
        ex.to_string(),
        "((Steps.(step1.(output.(obj.(array[((Steps.(step1.(output.(obj.num))))+1)])))))+2)"
    );

    let arrays = Expression::new("Context.a[Steps.loop.iteration.index][Steps.loop.iteration.index + 1]").unwrap();
    let deep_object = Expression::new("Context.a.b.c").unwrap();
    let deep_object_with_array = Expression::new("Context.a.b[2].c").unwrap();

    assert_eq!(
        arrays.to_string(),
        "(Context.(a[(Steps.(loop.(iteration.index)))][((Steps.(loop.(iteration.index)))+1)]))"
    );
    assert_eq!(deep_object.to_string(), "(Context.(a.(b.c)))");
    assert_eq!(deep_object_with_array.to_string(), "(Context.(a.(b[2].c)))");

    let op_in_the_name = Expression::new("Store.a.b.c or Store.c.d.x").unwrap();
    assert_eq!(op_in_the_name.to_string(), "((Store.(a.(b.c)))or(Store.(c.(d.x))))");

    let op_in_the_name = Expression::new("Store.a.b.corStore.c.d.x").unwrap();
    assert_eq!(op_in_the_name.to_string(), "(Store.(a.(b.(corStore.(c.(d.x))))))");
}

#[test]
fn expression_with_bracket_notation_and_quoted_keys() {
    assert_eq!(s("Context.obj[\"mail.props.port\"]"), "(Context.(obj[\"mail.props.port\"]))");

    assert_eq!(s("Context.obj['api.key.secret']"), "(Context.(obj['api.key.secret']))");

    assert_eq!(s("Context.obj[\"mail.props.port\"] = 587"), "((Context.(obj[\"mail.props.port\"]))=587)");

    assert_eq!(s("Context.obj[\"mail.props.port\"] != 500"), "((Context.(obj[\"mail.props.port\"]))!=500)");

    assert_eq!(s("Context.obj[\"mail.props.port\"] > 500"), "((Context.(obj[\"mail.props.port\"]))>500)");

    assert_eq!(
        s("Context.obj[\"mail.props.port\"] > 500 ? \"high\" : \"low\""),
        "(((Context.(obj[\"mail.props.port\"]))>500)?\"high\":\"low\")"
    );

    assert_eq!(s("Context.obj[\"mail.props.port\"].value"), "(Context.(obj[\"mail.props.port\"].value))");
}
