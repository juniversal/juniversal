/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

import org.eclipse.jdt.core.dom.NumberLiteral;
import org.juniversal.translator.core.ASTUtil;

import java.math.BigInteger;


// TODO: Finish this
public class NumberLiteralWriter extends CSharpASTNodeWriter<NumberLiteral> {
    public NumberLiteralWriter(CSharpFileTranslator cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(NumberLiteral numberLiteral) {
        String rawToken = numberLiteral.getToken();

        // Strip out any _ separators in the number, as those aren't supported in C# (at least not until the
        // new C# 6 comes out)
        String token = rawToken.replace("_", "");

        boolean isHex = token.startsWith("0x") || token.startsWith("0X");
        char lastChar = token.charAt(token.length() - 1);

        boolean isFloatingPoint = !isHex && (token.contains(".") || token.contains("e") || token.contains("E") ||
                                             lastChar == 'f' || lastChar == 'F' || lastChar == 'd' || lastChar == 'D');

        // First see if it's a floating point (with a decimal point or in scientific notation) or an integer
        if (isFloatingPoint)
            write(token);
        else {
            // TODO: Support binary (and octal) literals by converting to hex
            if (token.length() >= 2 && token.startsWith("0")) {
                char secondChar = token.charAt(1);
                if (secondChar >= '0' && secondChar <= '7') {
                    throw sourceNotSupported("Octal literals aren't currently supported; change the source to use hex instead");
                } else if ((secondChar == 'b') || (secondChar == 'B')) {
                    throw sourceNotSupported("Binary literals aren't currently supported; change the source to use hex instead");
                }
            }

            if (token.length() < 8)
                write(token);
            else {
                // If the literal exceeds the max size of an int/long, then C# will automatically treat its type as an
                // unsigned int/long instead of signed.   In that case, add an explicit cast, with the unchecked modifier,
                // to convert the type back to signed

                BigInteger tokenValue = ASTUtil.getIntegerLiteralValue(numberLiteral);

                // TODO: ADD TEST CASES HERE
                if (lastChar == 'L' || lastChar == 'l') {
                    if (tokenValue.longValue() < 0)
                        write("unchecked((long) " + token + ")");
                    else write(token);
                } else {
                    if (tokenValue.intValue() < 0)
                        write("unchecked((int) " + token + ")");
                    else write(token);
                }
            }
        }

        match(rawToken);
    }
}
