/*
 * Copyright (c) 2011-2014, Microsoft Mobile
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

import java.util.List;


public abstract class ASTWriter<T extends ASTNode> {
    private ASTWriters astWrtiers;

    protected ASTWriter(ASTWriters astWrtiers) {
        this.astWrtiers = astWrtiers;
    }

    abstract public void write(Context context, T node);

    public boolean canProcessTrailingWhitespaceOrComments() {
        return false;
    }
    
    protected abstract ASTWriters getASTWriters();

    protected void writeNode(Context context, ASTNode node) {
        getASTWriters().writeNode(context, node);
    }

    protected void writeNodeFromOtherPosition(Context context, ASTNode node) {
        int savedPosition = context.getPosition();

        context.setPositionToStartOfNode(node);
        getASTWriters().writeNode(context, node);

        context.setPosition(savedPosition);
    }

    protected void writeNodeAtDifferentPosition(ASTNode node, Context context) {
        getASTWriters().writeNodeAtDifferentPosition(node, context);
    }

    public <TElmt> void writeCommaDelimitedNodes(Context context, List list, ASTUtil.IProcessListElmt<TElmt> processList) {
        boolean first = true;
        for (Object elmtObject : list) {
            if (!first) {
                context.copySpaceAndComments();
                context.matchAndWrite(",");
            }

            TElmt elmt = (TElmt) elmtObject;
            processList.process(elmt);

            first = false;
        }
    }

    public void writeCommaDelimitedNodes(Context context, List list) {
        boolean first = true;
        for (Object astNodeObject : list) {
            if (!first) {
                context.copySpaceAndComments();
                context.matchAndWrite(",");
            }

            ASTNode astNode = (ASTNode) astNodeObject;

            context.copySpaceAndComments();
            writeNode(context, astNode);

            first = false;
        }
    }

    public void writeNodes(Context context, List list) {
        for (Object astNodeObject : list) {
            ASTNode astNode = (ASTNode) astNodeObject;

            context.copySpaceAndComments();
            writeNode(context, astNode);
        }
    }
}
