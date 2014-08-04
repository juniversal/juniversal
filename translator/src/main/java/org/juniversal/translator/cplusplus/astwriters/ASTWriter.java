package org.juniversal.translator.cplusplus.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.cplusplus.astwriters.ASTWriters;


public abstract class ASTWriter {
    private ASTWriters astWriters;


    public ASTWriter() {
    }

    public ASTWriter(ASTWriters writeCPP) {
        this.astWriters = writeCPP;
    }

    public ASTWriters getASTWriters() {
        return this.astWriters;
    }

    abstract public void write(ASTNode node, Context context);
}
