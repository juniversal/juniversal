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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

@SuppressWarnings("serial")
public class SourceNotSupportedException extends UserViewableException {

	public SourceNotSupportedException(CompilationUnit compilationUnit, int position,
			String baseMessage) {
		super(generateMessage(compilationUnit, position, baseMessage));
		m_compilationUnit = compilationUnit;
		m_position = position;
		m_baseMessage = baseMessage;
	}
	
	public CompilationUnit getCompilationUnit() {
		return m_compilationUnit;
	}

	public int getLineNumber() {
		return m_position;
	}

	public String getBaseMessage() {
		return m_baseMessage;
	}
	
	private static String generateMessage(CompilationUnit compilationUnit, int position,
			String baseMessage) {
		String filePath = "<unknown-file>";
		IJavaElement javaElement = compilationUnit.getJavaElement();
		if (javaElement != null) {
			IResource resource;
			try {
				resource = javaElement.getCorrespondingResource();
			}
			catch(JavaModelException e) {
				resource = null;
			}

			if (resource != null && resource instanceof IFile) {
				IFile file = (IFile) resource;
				IPath path = file.getFullPath();
				filePath = path.toOSString();
			}
		}
		
		String lineNumberString;
		int lineNumber = compilationUnit.getLineNumber(position);
		if (lineNumber == -1)
			lineNumberString = "<invalid-line-number>";
		else if (lineNumber == -2)
			lineNumberString = "<unknown>";
		else lineNumberString = Integer.toString(lineNumber);
		
		String columnNumberString;
		int columnNumber = compilationUnit.getColumnNumber(position);
		if (columnNumber == -1)
			columnNumberString = "<invalid-column-number>";
		else if (lineNumber == -2)
			columnNumberString = "<unknown>";
		else columnNumberString = Integer.toString(columnNumber  + 1);

		return filePath + " (line " + lineNumberString + ", col " + columnNumberString + "): "
				+ baseMessage;
	}

	// Data
	final private CompilationUnit m_compilationUnit;
	final private int m_position;
	final private String m_baseMessage;
}
