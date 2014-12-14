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

import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cingram on 12/10/2014.
 */
public class JavadocCommentWriter extends CSharpASTNodeWriter<Javadoc> {
    protected JavadocCommentWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Javadoc javadoc) {
        boolean bWroteSummary = false;
        int tagCount = 0;
        for (Object tagObj: javadoc.tags()) {
            TagElement tag = (TagElement)tagObj;
            String tagName = tag.getTagName();
            String tagString = stripLeadingTrailingWhitespaceAndLeadingAsterisk(tag.toString());
            String prefix = tagCount++ == 0 ? "/// " : "\n/// ";
            if (tagName == null) {
                if (!bWroteSummary) {
                    write(prefix + "<summary>" + tagString + "</summary>");
                    bWroteSummary = true;
                }
                else
                    write(prefix + "<remarks>" + tagString + "</remarks>");
            } else {
                String tagAfterName = tagString.split("\\s+", 2)[1];
                switch (tagName) {
                    case "@param":
                        String split[] = tagAfterName.split("\\s+", 2);
                        String name = split.length > 0 ? split[0] : "";
                        String desc = split.length > 1 ? split[1] : "";
                        write(prefix + "<param name=\"" + name + "\">" + desc + "</param>");
                        break;
                    case "@returns":
                        write(prefix + "<returns>" + tagAfterName + "</returns>");
                        break;
                    case "@since":
                        write(prefix + "<remarks> Since: " + tagAfterName + "</remarks>");
                        break;
                    case "@author":
                        write(prefix + "<remarks> Author: " + tagAfterName + "</remarks>");
                        break;
                    default:
                        write(prefix + "<remarks>" + tagName + ": " + tagAfterName + "</remarks>");
                        break;
                }
            }
        }
        setPositionToEndOfNode(javadoc);
    }

    private static Pattern REGEX_LEADING_WHITESPACE_AND_ASTERISK = Pattern.compile("\\A\\s*\\*?\\s*");
    private static Pattern REGEX_TRAILING_WHITESPACE = Pattern.compile("\\s*\\Z");

    public static String stripLeadingTrailingWhitespaceAndLeadingAsterisk(String s) {
        String ret = s;
        Matcher m = REGEX_LEADING_WHITESPACE_AND_ASTERISK.matcher(ret);
        ret = m.find() ? m.replaceFirst("") : ret;
        m = REGEX_TRAILING_WHITESPACE.matcher(ret);
        ret = m.matches() ? m.replaceFirst("") : ret;
        return ret;
    }
}
