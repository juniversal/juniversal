/*
 * Copyright (c) 2012-2014, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.Var;

import static org.juniversal.translator.core.ASTUtil.forEach;


public class SwitchStatementWriter extends CSharpASTNodeWriter<SwitchStatement> {
    public SwitchStatementWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(SwitchStatement switchStatement) {
        // TODO: Handle string switch statements
        // TODO: Check for fall through of cases (disallowed in C#)

        matchAndWrite("switch");

        copySpaceAndComments();
        matchAndWrite("(");

        writeNode(switchStatement.getExpression());

        copySpaceAndComments();
        matchAndWrite(")");

        copySpaceAndComments();
        matchAndWrite("{");

        Var<Statement> previousStatement = new Var<>();
        Var<Integer> previousStatementIndent = new Var<>();

        forEach(switchStatement.statements(), (Statement statement, boolean first) -> {
            if (statement instanceof SwitchCase) {
                SwitchCase switchCase = (SwitchCase) statement;

                if (previousStatement.isSet() && !isSwitchExitStatement(previousStatement.value())) {
                    copySpaceAndCommentsUntilEOL();
                    writeln();
                    indentToColumn(previousStatementIndent.value());

                    write("goto case ");
                    writeCaseLabelAtOtherPosition(switchCase.getExpression());
                    write(";");
                }

                copySpaceAndComments();
                writeSwitchCase(switchCase);

                previousStatement.clear();
            } else {
                copySpaceAndComments();

                previousStatement.set(statement);
                previousStatementIndent.set(getTargetColumn());

                writeNode(statement);
            }
        });

        // In C#, unlike Java, the final case statement / default in the switch must end in an explicit break (assuming
        // it doesn't exit the scope in some other way)
        if (previousStatement.isSet() && !isSwitchExitStatement(previousStatement.value())) {
            copySpaceAndCommentsUntilEOL();
            writeln();
            indentToColumn(previousStatementIndent.value());

            write("break;");
        }

        copySpaceAndComments();
        matchAndWrite("}");
    }

    private boolean isSwitchExitStatement(Statement previousStatement) {
        return previousStatement instanceof BreakStatement ||
               previousStatement instanceof ContinueStatement ||
               previousStatement instanceof ReturnStatement ||
               previousStatement instanceof ThrowStatement;
    }

    private void writeSwitchCase(SwitchCase switchCase) {
        if (switchCase.isDefault()) {
            matchAndWrite("default");

            copySpaceAndComments();
            matchAndWrite(":");
        } else {
            matchAndWrite("case");

            copySpaceAndComments();
            writeCaseLabel(switchCase.getExpression());

            copySpaceAndComments();
            matchAndWrite(":");
        }
    }

    private void writeCaseLabelAtOtherPosition(Expression expression) {
        int savedPosition = getPosition();
        setPositionToStartOfNode(expression);
        writeCaseLabel(expression);
        setPosition(savedPosition);
    }

    /**
     * Write the case label if it's an enum constant.  Enum constant switch labels in Java don't need to use a qualified
     * name, including the enum type.   That is FOO is valid for a case label; you don't have to say EnumType.FOO.
     * However, that's not the case in C#.   So map accordingly, writing the fully qualified name for enum constants.
     *
     * @param expression case label expression
     */
    private void writeCaseLabel(Expression expression) {
        if (expression instanceof SimpleName) {
            SimpleName simpleName = (SimpleName) expression;

            @Nullable ITypeBinding typeBinding = simpleName.resolveTypeBinding();
            if (typeBinding != null && typeBinding.isEnum()) {
                // TODO: Add using statement if needed (test to verify this case exists)
                write(typeBinding.getName() + ".");
                writeNode(expression);

                return;
            }
        }

        writeNode(expression);
    }
}
