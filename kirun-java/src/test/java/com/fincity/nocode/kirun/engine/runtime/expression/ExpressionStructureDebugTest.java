package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.LinkedList;

import org.junit.jupiter.api.Test;

class ExpressionStructureDebugTest {

    private void printExpressionStructure(Expression expr, String indent) {
        System.out.println(indent + "Expression: " + expr.getExpression());
        System.out.println(indent + "toString(): " + expr.toString());
        
        LinkedList<ExpressionToken> tokens = expr.getTokens();
        LinkedList<Operation> ops = expr.getOperations();
        
        System.out.println(indent + "Operations: " + ops.size());
        for (int i = 0; i < ops.size(); i++) {
            System.out.println(indent + "  Op[" + i + "]: " + ops.get(i).getOperator());
        }
        
        System.out.println(indent + "Tokens: " + tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            ExpressionToken token = tokens.get(i);
            System.out.println(indent + "  Token[" + i + "]: " + token + 
                " (class: " + token.getClass().getSimpleName() + ")");
            
            if (token instanceof Expression) {
                Expression nested = (Expression) token;
                System.out.println(indent + "    -> Nested Expression:");
                printExpressionStructure(nested, indent + "      ");
            } else if (token instanceof ExpressionTokenValue) {
                ExpressionTokenValue etv = (ExpressionTokenValue) token;
                System.out.println(indent + "    -> ExpressionTokenValue:");
                System.out.println(indent + "      expression: " + etv.getExpression());
                System.out.println(indent + "      value: " + etv.getTokenValue());
            }
        }
        System.out.println();
    }

    @Test
    void testFailingExpressionStructure() {
        System.out.println("=== Testing: Arguments.b+\"'kir\" + ' an' ===\n");
        Expression expr = new Expression("Arguments.b+\"'kir\" + ' an'");
        printExpressionStructure(expr, "");
    }
    
    @Test
    void testSimpleExpressionStructure() {
        System.out.println("=== Testing: 'ki/run'+'ab' ===\n");
        Expression expr = new Expression("'ki/run'+'ab'");
        printExpressionStructure(expr, "");
    }
    
    @Test
    void testLengthSubtractionStructure() {
        System.out.println("=== Testing: Page.items.length - 1 > 0 ===\n");
        Expression expr = new Expression("Page.items.length - 1 > 0");
        printExpressionStructure(expr, "");
    }
    
    @Test
    void testNestedStringExpression() {
        System.out.println("=== Testing: \"'kir\" + ' an' ===\n");
        Expression expr = new Expression("\"'kir\" + ' an'");
        printExpressionStructure(expr, "");
    }
    
    @Test
    void testMultipleLengthOperationsStructure() {
        System.out.println("=== Testing: Page.arr1.length-1 + Page.arr2.length-1 ===\n");
        Expression expr = new Expression("Page.arr1.length-1 + Page.arr2.length-1");
        printExpressionStructure(expr, "");
    }
}
