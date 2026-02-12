import React, { useState } from 'react';
import { EditorTheme } from './KIRunTextEditor';

interface DSLHelpWindowProps {
    onClose: () => void;
    isVisible: boolean;
    theme?: EditorTheme;
}

type HelpSection =
    | 'overview'
    | 'keywords'
    | 'structure'
    | 'datatypes'
    | 'expressions'
    | 'dependencies'
    | 'blocks'
    | 'examples';

interface HelpContent {
    title: string;
    content: React.ReactNode;
}

export function DSLHelpWindow({ onClose, isVisible, theme = 'light' }: DSLHelpWindowProps) {
    const [activeSection, setActiveSection] = useState<HelpSection>('overview');

    if (!isVisible) return null;

    const sections: Record<HelpSection, HelpContent> = {
        overview: {
            title: 'KIRun Language Overview',
            content: (
                <div className="_help-content">
                    <h3>What is KIRun Language?</h3>
                    <p>
                        KIRun Language is a text-based language for defining functions in the KIRun
                        runtime. It provides a more readable and maintainable way to write function
                        logic compared to visual editors.
                    </p>

                    <h3>Key Features</h3>
                    <ul>
                        <li>Type-safe function parameters and events</li>
                        <li>Expression-based programming with KIRun expression language</li>
                        <li>Dependency management with AFTER clauses</li>
                        <li>Conditional execution with IF clauses</li>
                        <li>
                            Nested blocks for control flow (true, false, iteration, output, error)
                        </li>
                        <li>Comments using /* */ syntax (placed after statements)</li>
                    </ul>

                    <h3>Basic Structure</h3>
                    <pre>{`FUNCTION functionName
NAMESPACE optional.namespace
PARAMETERS
    param1 AS INTEGER
    param2 AS STRING
EVENTS
    output
        result AS BOOLEAN
LOGIC
    statementName: Function.Call(param = value) /* Your comment here */`}</pre>
                </div>
            ),
        },

        keywords: {
            title: 'Keywords Reference',
            content: (
                <div className="_help-content">
                    <h3>Function Structure Keywords</h3>
                    <table className="_keyword-table">
                        <thead>
                            <tr>
                                <th>Keyword</th>
                                <th>Description</th>
                                <th>Usage</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <code>FUNCTION</code>
                                </td>
                                <td>Declares a function definition</td>
                                <td>
                                    <code>FUNCTION myFunction</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>NAMESPACE</code>
                                </td>
                                <td>Optional namespace for the function</td>
                                <td>
                                    <code>NAMESPACE MyApp.Utils</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>PARAMETERS</code>
                                </td>
                                <td>Declares input parameters</td>
                                <td>
                                    <code>PARAMETERS</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>EVENTS</code>
                                </td>
                                <td>Declares output events</td>
                                <td>
                                    <code>EVENTS</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>LOGIC</code>
                                </td>
                                <td>Begins the function logic block</td>
                                <td>
                                    <code>LOGIC</code>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <h3>Type Declaration Keywords</h3>
                    <table className="_keyword-table">
                        <thead>
                            <tr>
                                <th>Keyword</th>
                                <th>Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <code>AS</code>
                                </td>
                                <td>Specifies type of parameter or event field</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>OF</code>
                                </td>
                                <td>Used in ARRAY OF syntax</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>WITH DEFAULT VALUE</code>
                                </td>
                                <td>Specifies default value for schema literal</td>
                            </tr>
                        </tbody>
                    </table>

                    <h3>Control Flow Keywords</h3>
                    <table className="_keyword-table">
                        <thead>
                            <tr>
                                <th>Keyword</th>
                                <th>Description</th>
                                <th>Example</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <code>AFTER</code>
                                </td>
                                <td>Specifies statement dependencies</td>
                                <td>
                                    <code>AFTER Steps.step1.output</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>IF</code>
                                </td>
                                <td>Conditional execution</td>
                                <td>
                                    <code>IF Steps.check.true</code>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            ),
        },

        structure: {
            title: 'Function Structure',
            content: (
                <div className="_help-content">
                    <h3>Complete Function Template</h3>
                    <pre>{`FUNCTION functionName
NAMESPACE optional.namespace.path

PARAMETERS
    paramName1 AS TYPE
    paramName2 AS ARRAY OF TYPE
    paramName3 AS OBJECT

EVENTS
    eventName1
        field1 AS TYPE
        field2 AS TYPE
    eventName2
        result AS TYPE

LOGIC
    statementName: Namespace.FunctionName(
        param1 = value,
        param2 = expression,
        param3 = Steps.previousStep.output.result
    ) AFTER Steps.dependency.output IF Steps.condition.true /* comment */

    iterationStep: System.Loop.RangeLoop(to = 10)
        iteration
            innerStep: SomeFunction(param = Steps.iterationStep.iteration.index)

    conditionalStep: System.If(condition = \`x > 5\`)
        true
            trueStep: DoSomething()
        false
            falseStep: DoSomethingElse()`}</pre>

                    <h3>Statement Syntax</h3>
                    <p>Each statement follows this pattern:</p>
                    <pre>{`statementName: Namespace.FunctionName(param = value) AFTER dep IF cond /* comment */
    nestedBlock
        nestedStatement: Function()`}</pre>

                    <h3>Comments</h3>
                    <p>Comments are placed AFTER the statement using /* */ syntax:</p>
                    <pre>{`step1: System.Log(message = "Hello") /* This logs a message */
step2: Math.Add(a = 5, b = 3) /* Calculate sum */`}</pre>

                    <h3>Anonymous Statements</h3>
                    <p>Statements without names can be declared with just a colon:</p>
                    <pre>{`: System.Log(message = "Hello")`}</pre>
                </div>
            ),
        },

        datatypes: {
            title: 'Data Types',
            content: (
                <div className="_help-content">
                    <h3>Primitive Types</h3>
                    <table className="_keyword-table">
                        <thead>
                            <tr>
                                <th>Type</th>
                                <th>Description</th>
                                <th>Example Values</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <code>INTEGER</code>
                                </td>
                                <td>32-bit integer</td>
                                <td>42, -10, 0</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>LONG</code>
                                </td>
                                <td>64-bit integer</td>
                                <td>9999999999</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>FLOAT</code>
                                </td>
                                <td>32-bit floating point</td>
                                <td>3.14, -0.5</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>DOUBLE</code>
                                </td>
                                <td>64-bit floating point</td>
                                <td>3.14159265359</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>STRING</code>
                                </td>
                                <td>Text string</td>
                                <td>"hello", "world"</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>BOOLEAN</code>
                                </td>
                                <td>True or false</td>
                                <td>true, false</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>NULL</code>
                                </td>
                                <td>Null type</td>
                                <td>null</td>
                            </tr>
                            <tr>
                                <td>
                                    <code>ANY</code>
                                </td>
                                <td>Any type</td>
                                <td>-</td>
                            </tr>
                        </tbody>
                    </table>

                    <h3>Complex Types</h3>
                    <table className="_keyword-table">
                        <thead>
                            <tr>
                                <th>Type</th>
                                <th>Syntax</th>
                                <th>Example</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <code>ARRAY</code>
                                </td>
                                <td>
                                    <code>ARRAY OF TYPE</code>
                                </td>
                                <td>
                                    <code>items AS ARRAY OF INTEGER</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>OBJECT</code>
                                </td>
                                <td>
                                    <code>OBJECT</code> or JSON schema
                                </td>
                                <td>
                                    <code>data AS OBJECT</code>
                                </td>
                            </tr>
                            <tr>
                                <td>JSON Schema</td>
                                <td>Inline JSON object</td>
                                <td>
                                    <code>config AS {`{type: "STRING"}`}</code>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <h3>Schema Literals</h3>
                    <p>Create typed values with default values:</p>
                    <pre>{`(INTEGER) WITH DEFAULT VALUE 42
(ARRAY OF STRING) WITH DEFAULT VALUE ["a", "b"]
(OBJECT) WITH DEFAULT VALUE {name: "test"}`}</pre>
                </div>
            ),
        },

        expressions: {
            title: 'Expressions & Values',
            content: (
                <div className="_help-content">
                    <h3>Value Types</h3>
                    <p>There are three types of parameter values:</p>

                    <h4>1. Literal VALUES (double quotes)</h4>
                    <p>Use double-quoted strings for literal string values:</p>
                    <pre>{`name: SetValue(value = "hello world")
count: SetValue(value = 42)
flag: SetValue(value = true)
empty: SetValue(value = null)`}</pre>

                    <h4>2. EXPRESSIONS (backticks or single quotes)</h4>
                    <p>
                        Use backticks or single quotes for expressions that will be evaluated:
                    </p>
                    <pre>{`calc: Math.Add(a = \`x + 5\`, b = \`y * 2\`) /* Backtick expressions */

check: If(condition = 'Arguments.age >= 18') /* Single-quote expressions */

combine: Concat(a = Steps.step1.output.result) /* Reference step outputs */

compute: Calculate(value = \`Arguments.x * 2 + Steps.prev.output.y\`) /* Complex expressions */`}</pre>

                    <h4>3. Complex VALUES (JSON objects/arrays)</h4>
                    <p>Use JSON syntax for objects and arrays:</p>
                    <pre>{`config: SetObject(value = {
    name: "test",
    count: 10,
    enabled: true
}) /* JSON object */

items: SetArray(value = [1, 2, 3, 4, 5]) /* JSON array */

data: SetData(value = {
    user: {name: "John", age: 30},
    tags: ["admin", "user"]
}) /* Nested structures */`}</pre>

                    <h3>Expression References</h3>
                    <table className="_keyword-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Description</th>
                                <th>Example</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <code>Arguments.name</code>
                                </td>
                                <td>Function input parameters</td>
                                <td>
                                    <code>Arguments.userId</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>Steps.name.output</code>
                                </td>
                                <td>Statement output</td>
                                <td>
                                    <code>Steps.calc.output.result</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>Steps.name.eventName</code>
                                </td>
                                <td>Statement event output</td>
                                <td>
                                    <code>Steps.fetch.success.data</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>Steps.name.iteration</code>
                                </td>
                                <td>Loop iteration data</td>
                                <td>
                                    <code>Steps.loop.iteration.index</code>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <code>Context.name</code>
                                </td>
                                <td>Context variable</td>
                                <td>
                                    <code>Context.tempData</code>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <h3>Expression Operators</h3>
                    <pre>{`\`x + y\`, \`x - y\`, \`x * y\`, \`x / y\`, \`x % y\` /* Arithmetic */

\`x == y\`, \`x != y\`, \`x < y\`, \`x > y\`, \`x <= y\`, \`x >= y\` /* Comparison */

\`x && y\`, \`x || y\`, \`!x\`, \`x ?? y\` /* Logical */

\`object.property\`, \`array[index]\`, \`object?.property\` /* Property access */`}</pre>
                </div>
            ),
        },

        dependencies: {
            title: 'Dependencies & Control Flow',
            content: (
                <div className="_help-content">
                    <h3>AFTER Clause (Dependencies)</h3>
                    <p>Control execution order by specifying dependencies:</p>
                    <pre>{`step2: Function() AFTER Steps.step1.output /* Single dependency */

step3: Function() AFTER Steps.step1.output, Steps.step2.output /* Multiple dependencies */

step4: Function() AFTER Steps.async.success, Steps.backup.output /* Event-specific dependencies */`}</pre>

                    <h3>IF Clause (Conditional Execution)</h3>
                    <p>Execute a statement only if a condition is met:</p>
                    <pre>{`cleanup: DeleteTemp() IF Steps.check.true /* Execute only if condition is true */

rollback: Undo() IF Steps.process.error /* Execute after error */

finalize: Complete() IF Steps.check1.true, Steps.check2.true /* Multiple conditions */`}</pre>

                    <h3>Combining AFTER and IF</h3>
                    <pre>{`conditional: Process() AFTER Steps.step1.output IF Steps.check.true /* Execute after step1, but only if condition is true */`}</pre>

                    <h3>Execution Flow</h3>
                    <ul>
                        <li>
                            <strong>Parallel execution:</strong> Steps without dependencies run in
                            parallel
                        </li>
                        <li>
                            <strong>Sequential execution:</strong> Use AFTER to ensure order
                        </li>
                        <li>
                            <strong>Conditional execution:</strong> Use IF to skip steps based on
                            conditions
                        </li>
                        <li>
                            <strong>Event-based flow:</strong> Branch logic based on event outputs
                            (true/false/error)
                        </li>
                    </ul>
                </div>
            ),
        },

        blocks: {
            title: 'Nested Blocks',
            content: (
                <div className="_help-content">
                    <h3>Block Types</h3>
                    <p>
                        Statements can contain nested blocks for different execution contexts:
                    </p>

                    <h4>1. Conditional Blocks (true/false)</h4>
                    <pre>{`check: System.If(condition = \`x > 5\`)
    true
        success: Log(message = "Condition is true")
        process: DoSomething()
    false
        failure: Log(message = "Condition is false")
        alternate: DoSomethingElse()`}</pre>

                    <h4>2. Iteration Block</h4>
                    <pre>{`loop: System.Loop.RangeLoop(from = 0, to = 10)
    iteration
        process: HandleItem(
            index = Steps.loop.iteration.index,
            value = Steps.loop.iteration.value
        ) /* Access loop variables */`}</pre>

                    <h4>3. Event Blocks</h4>
                    <pre>{`fetch: HTTP.Get(url = "https://api.example.com")
    success
        handle: ProcessData(data = Steps.fetch.success.body)
    error
        log: LogError(error = Steps.fetch.error.message)
        retry: RetryFetch()`}</pre>

                    <h4>4. Output Block</h4>
                    <pre>{`main: ComplexOperation()
    output
        transform: TransformResult(data = Steps.main.output.result)`}</pre>

                    <h3>Block Indentation</h3>
                    <p>
                        Blocks must be indented relative to their parent statement. Use consistent
                        indentation (spaces or tabs).
                    </p>

                    <h3>Multiple Nested Blocks</h3>
                    <pre>{`outer: System.Loop.RangeLoop(to = 5)
    iteration
        check: System.If(condition = \`Steps.outer.iteration.index % 2 == 0\`)
            true
                even: ProcessEven(val = Steps.outer.iteration.index)
            false
                odd: ProcessOdd(val = Steps.outer.iteration.index)`}</pre>
                </div>
            ),
        },

        examples: {
            title: 'Complete Examples',
            content: (
                <div className="_help-content">
                    <h3>Example 1: Simple Calculator</h3>
                    <pre>{`FUNCTION Calculator
NAMESPACE MyApp

PARAMETERS
    a AS DOUBLE
    b AS DOUBLE
    operation AS STRING

EVENTS
    output
        result AS DOUBLE
        message AS STRING

LOGIC
    calculate: System.Conditional(
        condition = \`Arguments.operation\`,
        cases = {
            add: Steps.add.output,
            subtract: Steps.subtract.output,
            multiply: Steps.multiply.output,
            divide: Steps.divide.output
        }
    ) /* Perform calculation based on operation */

    add: System.Math.Add(a = Arguments.a, b = Arguments.b)
    subtract: System.Math.Subtract(a = Arguments.a, b = Arguments.b)
    multiply: System.Math.Multiply(a = Arguments.a, b = Arguments.b)
    divide: System.Math.Divide(a = Arguments.a, b = Arguments.b)`}</pre>

                    <h3>Example 2: User Validation</h3>
                    <pre>{`FUNCTION ValidateUser
NAMESPACE Auth

PARAMETERS
    username AS STRING
    email AS STRING
    age AS INTEGER

EVENTS
    valid
    invalid
        errors AS ARRAY OF STRING

LOGIC
    checkUsername: System.String.Length(value = Arguments.username) /* Check username length */
    usernameValid: System.Compare(
        a = Steps.checkUsername.output.result,
        operator = ">=",
        b = 3
    )

    emailValid: System.String.Matches(
        value = Arguments.email,
        pattern = \`^[^@]+@[^@]+\\.[^@]+$\`
    ) /* Check email format */

    ageValid: System.Compare(
        a = Arguments.age,
        operator = ">=",
        b = 18
    ) /* Check age */

    allValid: System.Logic.And(
        conditions = [
            Steps.usernameValid.true,
            Steps.emailValid.true,
            Steps.ageValid.true
        ]
    ) /* Combine validations */
        true
            log: System.Log(message = "User is valid")
        false
            collectErrors: System.Array.Create(
                items = [
                    \`Steps.usernameValid.false ? "Username too short" : null\`,
                    \`Steps.emailValid.false ? "Invalid email" : null\`,
                    \`Steps.ageValid.false ? "Must be 18+" : null\`
                ]
            )`}</pre>

                    <h3>Example 3: Data Processing Loop</h3>
                    <pre>{`FUNCTION ProcessItems
NAMESPACE Data

PARAMETERS
    items AS ARRAY OF OBJECT

EVENTS
    output
        processed AS ARRAY OF OBJECT
        count AS INTEGER

LOGIC
    init: System.Context.Create(
        name = "results",
        schema = (ARRAY OF OBJECT) WITH DEFAULT VALUE []
    ) /* Initialize result array */

    loop: System.Loop.ForEach(array = Arguments.items) /* Loop through items */
        iteration
            process: TransformItem(
                item = Steps.loop.iteration.value,
                index = Steps.loop.iteration.index
            ) /* Process each item */

            append: System.Array.InsertLast(
                source = Context.results,
                element = Steps.process.output.result
            ) /* Add to results */

            update: System.Context.Set(
                name = "results",
                value = Steps.append.output.result
            ) /* Update context */

    count: System.Array.Length(array = Context.results) /* Get final count */

    output: System.Log(
        message = \`Processed \${Steps.count.output.result} items\`
    ) AFTER Steps.loop.output /* Output results */`}</pre>
                </div>
            ),
        },
    };

    const sectionButtons: Array<{ key: HelpSection; label: string }> = [
        { key: 'overview', label: 'Overview' },
        { key: 'keywords', label: 'Keywords' },
        { key: 'structure', label: 'Structure' },
        { key: 'datatypes', label: 'Data Types' },
        { key: 'expressions', label: 'Expressions' },
        { key: 'dependencies', label: 'Dependencies' },
        { key: 'blocks', label: 'Nested Blocks' },
        { key: 'examples', label: 'Examples' },
    ];

    return (
        <div className={`_dsl-help-container _theme-${theme}`} data-theme={theme}>
            <div className="_help-header">
                <h2>KIRun Language</h2>
            </div>

            <div className="_help-body">
                <div className="_help-sidebar">
                    <nav className="_help-nav">
                        {sectionButtons.map(({ key, label }) => (
                            <a
                                key={key}
                                className={`_nav-item ${activeSection === key ? '_active' : ''}`}
                                onClick={() => setActiveSection(key)}
                            >
                                {label}
                            </a>
                        ))}
                    </nav>
                </div>

                <div className="_help-main">
                    <h2>{sections[activeSection].title}</h2>
                    {sections[activeSection].content}
                </div>
            </div>

            <div className="_help-footer">
                <p>
                    <strong>Quick Tips:</strong> Use backticks ` for expressions, double quotes "
                    for literal strings. Comments go after the statement with /* */. Press{' '}
                    <kbd>Ctrl+Space</kbd> for autocomplete.
                </p>
            </div>
        </div>
    );
}
