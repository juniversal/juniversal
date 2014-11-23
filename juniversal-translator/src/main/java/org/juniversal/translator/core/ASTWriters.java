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

package org.juniversal.translator.core;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.HashMap;

public abstract class ASTWriters {
    private Context context;
    private HashMap<Class<? extends ASTNode>, ASTWriter> m_visitors = new HashMap<>();

    protected ASTWriters(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    protected void addWriter(Class<? extends ASTNode> clazz, ASTWriter visitor) {
        if (m_visitors.get(clazz) != null)
            throw new JUniversalException("Writer for class " + clazz + " already added to ASTWriters");
        m_visitors.put(clazz, visitor);
    }

    public abstract Translator getTranslator();

    public ASTWriter getVisitor(Class clazz) {
        ASTWriter visitor = m_visitors.get(clazz);
        if (visitor == null)
            throw new JUniversalException("No visitor found for class " + clazz.getName());
        return visitor;
    }

    private static final boolean VALIDATE_CONTEXT_POSITION = true;
    public void writeNode(ASTNode node) {
        int nodeStartPosition;
        if (VALIDATE_CONTEXT_POSITION) {
            nodeStartPosition = node.getStartPosition();

            // If the node starts with Javadoc (true for method declarations with Javadoc before
            // them), then skip past it; the caller is always expected to handle any comments,
            // Javadoc or not, coming before the start of code proper for the node. The exception to
            // that rule is the CompilationUnit; as outermost node it handles any comments at the
            // beginning itself.
            SourceCopier sourceCopier = context.getSourceCopier();
            if (sourceCopier.getSourceCharAt(nodeStartPosition) == '/'
                && sourceCopier.getSourceCharAt(nodeStartPosition + 1) == '*'
                && sourceCopier.getSourceCharAt(nodeStartPosition + 2) == '*'
                && !(node instanceof CompilationUnit))
                context.assertPositionIs(sourceCopier.skipSpaceAndComments(nodeStartPosition, false));
            else context.assertPositionIs(nodeStartPosition);
        }

        getVisitor(node.getClass()).write(node);

        if (VALIDATE_CONTEXT_POSITION) {
            if (context.getKnowinglyProcessedTrailingSpaceAndComments())
                context.assertPositionIsAtLeast(nodeStartPosition + node.getLength());
            else context.assertPositionIs(nodeStartPosition + node.getLength());
        }

        context.setKnowinglyProcessedTrailingSpaceAndComments(false);
    }

    public void writeRootNode(ASTNode node) {
        try {
            writeNode(node);
        }
        catch (UserViewableException e) {
            throw e;
        }
        catch (RuntimeException e) {
            if (e instanceof ContextPositionMismatchException)
                throw e;
            else
                throw new JUniversalException(e.getMessage() + "\nError occurred with context at position\n"
                                              + context.getPositionDescription(context.getPosition()), e);
        }
    }

    /**
     * Write a node that's not at the current context position. The context position is unchanged by this method--the
     * position is restored to the original position when done. No comments before/after the node are written. This
     * method can be used to write a node multiple times.
     *
     * @param node    node to write
     * @param context the context
     */
    public void writeNodeAtDifferentPosition(ASTNode node, Context context) {
        int originalPosition = context.getPosition();
        context.setPosition(node.getStartPosition());
        writeNode(node);
        context.setPosition(originalPosition);
    }
}
