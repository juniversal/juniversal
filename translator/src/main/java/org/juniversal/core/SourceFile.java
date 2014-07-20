package org.juniversal.core;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class SourceFile {
	private final CompilationUnit compilationUnit;
	private final String sourceFilePath;    // Null if there is no file
	private final String source;

	public SourceFile(CompilationUnit compilationUnit, String sourceFilePath) {
		this.compilationUnit = compilationUnit;
		this.sourceFilePath = sourceFilePath;
		this.source = Util.readFile(sourceFilePath);
	}

	public SourceFile(CompilationUnit compilationUnit, String sourceFilePath, String source) {
		this.compilationUnit = compilationUnit;
		this.sourceFilePath = sourceFilePath;
		this.source = source;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public boolean isDiskFile() {
		return sourceFilePath != null;
	}

	public String getSource() {
		return source;
	}
}
