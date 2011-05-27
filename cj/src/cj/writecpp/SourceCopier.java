package cj.writecpp;

import org.eclipse.jdt.core.dom.CompilationUnit;

import cj.CJException;

public class SourceCopier {
	private CompilationUnit compilationUnit;
	private String source;
	private int sourceTabStop;
	private CPPWriter cppWriter;

	public SourceCopier(CompilationUnit compilationUnit, String source, int sourceTabStop, CPPWriter cppWriter) {
		this.compilationUnit = compilationUnit;
		this.source = source;
		this.sourceTabStop = sourceTabStop;
		this.cppWriter = cppWriter;
	}

	public int match(int startPosition, String match) {
		if (!source.startsWith(match, startPosition))
			throw new CJException("Expected source to contain '" + match + "' at position " + startPosition
					+ ", but it doesn't");
		return startPosition + match.length();
	}

	/**
	 * Copies to output all whitespace, newlines, and comments from the source starting at
	 * startPosition and continuing until the end of the whitespace/newlines/comments. Any tabs in
	 * the source are expanded; tabs are reintroduced as appropriate at output time by the CPPWriter
	 * class.
	 * 
	 * @param startPosition
	 *            starting position in source
	 * @return ending position--position of character following space and comments
	 */
	public int copySpaceAndComments(int startPosition) {

		int position = startPosition;
		while (true) {
			int currChar = getSourceCharAt(position);

			if (currChar == -1)
				return position;
			else if (currChar == ' ' || currChar == '\t')
				position = copySpacesAndTabs(position);
			else if (currChar == '\r' || currChar == '\n') {
				cppWriter.write((char) currChar);
				++position;
			} else if (currChar == '/' && getSourceCharAt(position + 1) == '/') {
				cppWriter.write("//");
				position += 2;

				while (true) {
					currChar = getSourceCharAt(position);

					if (currChar == -1 || currChar == '\r' || currChar == '\n')
						break;
					else if (currChar == ' ' || currChar == '\t')
						position = copySpacesAndTabs(position);
					else {
						cppWriter.write((char) currChar);
						++position;
					}
				}
			} else if (currChar == '/' && getSourceCharAt(position + 1) == '*') {
				cppWriter.write("/*");
				position += 2;

				while (true) {
					currChar = getSourceCharAt(position);

					if (currChar == -1)
						break;
					else if (currChar == '*' && getSourceCharAt(position + 1) == '/') {
						cppWriter.write("*/");
						position += 2;
						break;
					} else if (currChar == ' ' || currChar == '\t')
						position = copySpacesAndTabs(position);
					else {
						cppWriter.write((char) currChar);
						++position;
					}
				}
			} else
				return position;
		}
	}

	/**
	 * Copies to output all whitespace, newlines, and comments from the source starting at
	 * startPosition and continuing until the end of the whitespace/newlines/comments. Any tabs in
	 * the source are expanded; tabs are reintroduced as appropriate at output time by the CPPWriter
	 * class.
	 * 
	 * @param startPosition
	 *            starting position in source
	 * @return ending position--position of character following space and comments
	 */
	public int skipSpaceAndComments(int startPosition) {

		int position = startPosition;
		while (true) {
			int currChar = getSourceCharAt(position);

			if (currChar == -1)
				return position;
			else if (currChar == ' ' || currChar == '\t')
				position = skipSpacesAndTabs(position);
			else if (currChar == '\r' || currChar == '\n')
				++position;
			else if (currChar == '/' && getSourceCharAt(position + 1) == '/') {
				position += 2;

				while (true) {
					currChar = getSourceCharAt(position);

					if (currChar == -1 || currChar == '\r' || currChar == '\n')
						break;
					else if (currChar == ' ' || currChar == '\t')
						position = skipSpacesAndTabs(position);
					else
						++position;
				}
			} else if (currChar == '/' && getSourceCharAt(position + 1) == '*') {
				position += 2;

				while (true) {
					currChar = getSourceCharAt(position);

					if (currChar == -1)
						break;
					else if (currChar == '*' && getSourceCharAt(position + 1) == '/') {
						position += 2;
						break;
					} else if (currChar == ' ' || currChar == '\t')
						position = skipSpacesAndTabs(position);
					else
						++position;
				}
			} else
				return position;
		}
	}

	/**
	 * Copies to output contiguous spaces and tabs from the source starting at startPosition and
	 * continuing until the end of the spaces/tabs. Any tabs in the source are expanded; tabs are
	 * reintroduced as appropriate at output time by the CPPWriter class.
	 * 
	 * @param startPosition
	 *            starting position in source
	 */
	public int copySpacesAndTabs(int startPosition) {

		int logicalColumn = -1; // -1 means don't know logical (untabified) column--yet
		int logicalColumnOffset = 0; // Logical (untabified) column offset

		int position;
		for (position = startPosition; true; ++position) {
			int currChar = getSourceCharAt(position);

			if (currChar == ' ') {
				++logicalColumnOffset;
				if (logicalColumn != -1)
					++logicalColumn;
			} else if (currChar == '\t') {
				// If we haven't yet computed the logical column, compute it now
				if (logicalColumn == -1)
					logicalColumn = getSourceLogicalColumn(position);

				int spacesForTab = sourceTabStop - (logicalColumn % sourceTabStop);

				logicalColumn += spacesForTab;
				logicalColumnOffset += spacesForTab;
			} else if (currChar == '\r')
				;
			// If the space is at the end of the line (trailing space), don't copy it
			else if (currChar == '\n')
				return position;
			else
				break;
		}

		for (int i = 0; i < logicalColumnOffset; ++i)
			cppWriter.write(' ');
		return position;
	}

	/**
	 * Skips contiguous spaces and tabs from the source starting at startPosition and continuing
	 * until the end of the spaces/tabs.
	 * 
	 * @param startPosition
	 *            starting position in source
	 */
	public int skipSpacesAndTabs(int startPosition) {
		int position;
		for (position = startPosition; true; ++position) {
			int currChar = getSourceCharAt(position);
			if (!(currChar == ' ' || currChar == '\t' || currChar == '\r'))
				break;
		}

		return position;
	}

	/**
	 * Get the character from the source at the specified position or -1 if past the end of the
	 * source.
	 * 
	 * @param position
	 *            source position in question
	 * @return character at that position or -1 if past end
	 */
	private int getSourceCharAt(int position) {
		if (position >= source.length())
			return -1;
		return source.charAt(position);
	}

	/**
	 * Gets the logical column in the source corresponding to the specified position. "Logical"
	 * means with tabs expanded according to the specified source tab stop.
	 * 
	 * @param position
	 *            source position in question
	 * @return logical column
	 */
	public int getSourceLogicalColumn(int position) {
		int physicalColumn = compilationUnit.getColumnNumber(position);

		int logicalColumn = 0;
		for (int i = position - physicalColumn; i < position; ++i) {
			char currChar = source.charAt(i);
			if (currChar == '\t')
				logicalColumn += sourceTabStop - (logicalColumn % sourceTabStop);
			else
				++logicalColumn;
		}

		return logicalColumn;
	}
}
