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

import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * @author Chris Ingram
 * @since 12/10/2014
 */
public class JavadocCommentWriter extends CSharpASTNodeWriter<Javadoc> {
    protected JavadocCommentWriter(CSharpTranslator cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Javadoc javadoc) {
        int previousAdditionalIndentation = getTargetWriter().setAdditionalIndentation(getTargetWriter().getCurrColumn());

        boolean wroteSummary = false;
        for (Object tagObj : javadoc.tags()) {
            TagElement tag = (TagElement) tagObj;
            String tagName = tag.getTagName();
            int position = tag.getStartPosition();
            int lineNumber = getContext().getSourceLineNumber(position);
            //String prefix = tagCount++ == 0 ? "/// " : "\n/// ";

            if (tagName == null) {
                if (!wroteSummary) {
                    writeSummaryTag(lineNumber, tag);
                    wroteSummary = true;
                } else
                    writeRemarksTag(lineNumber, tag);
            } else {
                switch (tagName) {
                    case "@param":
                        writeParamTag(lineNumber, tag);
                        break;
                    case "@returns":
                        writeReturnsTag(lineNumber, tag);
                        break;
                    case "@since":
                        writeSinceTag(lineNumber, tag);
                        break;
                    case "@author":
                        writeAuthorTag(lineNumber, tag);
                        break;
                    default:
                        writeRemarksTag(lineNumber, tag);
                        break;
                }
            }
        }

        getTargetWriter().setAdditionalIndentation(previousAdditionalIndentation);
        setPositionToEndOfNode(javadoc);
    }

    private void writeLineBreaks(int howMany) {
        for (int i = 0; i < howMany; i++)
            write("\n/// ");
    }

    private int writeFragments(int previousLineNumber, List<ASTNode> fragments) {
        int lineNumber = previousLineNumber;
        for (ASTNode fragment : fragments) {
            lineNumber = writeFragment(previousLineNumber, fragment);
            previousLineNumber = lineNumber;
        }
        return lineNumber;
    }

    private int writeFragment(int previousLineNumber, ASTNode fragment) {
        int lineNumber = getContext().getSourceLineNumber(fragment.getStartPosition());
        int lineNumberDiff = lineNumber - previousLineNumber;
        writeLineBreaks(lineNumberDiff);
        if (fragment instanceof TagElement) {
            TagElement tagElement = (TagElement) fragment;
            switch (tagElement.getTagName()) {
                case "@code":
                    lineNumber = writeInlineCodeTag(lineNumber, tagElement);
                    break;
                default:
                    write("{" + tagElement.getTagName() + " ");
                    stripLeadingSingleSpaceFromFirstFragment(tagElement.fragments());
                    lineNumber = writeFragments(lineNumber, tagElement.fragments());
                    write("}");
                    break;
            }
        } else if (fragment instanceof TextElement) {
            TextElement textElement = (TextElement) fragment;
            String text = textElement.getText();
            write(text);
        } else if (fragment instanceof Name) {
            Name name = (Name) fragment;
            String text = name.getFullyQualifiedName();
            write(text);
        } else if (fragment instanceof MethodRef) {
            MethodRef methodRef = (MethodRef) fragment;
            // TODO: Probably need to do something different here.
            String methodRefStr = methodRef.toString();
            write(methodRefStr);
        } else if (fragment instanceof MemberRef) {
            MemberRef memberRef = (MemberRef) fragment;
            // TODO: Probably need to do something different here.
            String memberRefStr = memberRef.toString();
            write(memberRefStr);
        }
        return lineNumber;
    }

    private void writeSummaryTag(int lineNumber, TagElement tag) {
        write("/// <summary>\n");
        write("/// ");
        writeFragments(lineNumber, tag.fragments());
        write("\n/// </summary>\n");
    }

    private void writeRemarksTag(int lineNumber, TagElement tag) {
        write("/// <remarks>");
        writeFragments(lineNumber, tag.fragments());
        write("</remarks>\n");
    }

    private void writeParamTag(int lineNumber, TagElement tag) {
        List<ASTNode> tagFrags = tag.fragments();
        String name = tagFrags.size() >= 1 ? tagFrags.get(0).toString() : "";
        tagFrags = tagFrags.subList(1, tagFrags.size());
        write("/// <param name=\"" + name + "\">");
        stripLeadingSingleSpaceFromFirstFragment(tagFrags);
        writeFragments(lineNumber, tagFrags);
        write("</param>\n");
    }

    private void writeReturnsTag(int lineNumber, TagElement tag) {
        write("/// <returns>");
        writeFragments(lineNumber, tag.fragments());
        write("</returns>\n");
    }

    private void writeSinceTag(int lineNumber, TagElement tag) {
        TextElement firstFragment = (TextElement) tag.fragments().get(0);
        firstFragment.setText("Since:" + firstFragment.getText());
        writeRemarksTag(lineNumber, tag);
    }

    private void writeAuthorTag(int lineNumber, TagElement tag) {
        TextElement firstFragment = (TextElement) tag.fragments().get(0);
        firstFragment.setText("Author:" + firstFragment.getText());
        writeRemarksTag(lineNumber, tag);
    }

    private int writeInlineCodeTag(int lineNumber, TagElement tag) {
        write("<c>");
        stripLeadingSingleSpaceFromFirstFragment(tag.fragments());
        lineNumber = writeFragments(lineNumber, tag.fragments());
        write("</c>");
        return lineNumber;
    }

    private void stripLeadingSingleSpaceFromFirstFragment(List<ASTNode> fragments) {
        if (fragments.size() > 0) {
            ASTNode firstFragment = fragments.get(0);
            if (firstFragment instanceof TextElement) {
                TextElement textElement = (TextElement) firstFragment;
                String text = textElement.getText();
                text = text.startsWith(" ") ? text.substring(1) : text;
                textElement.setText(text);
            }
        }
    }
}
