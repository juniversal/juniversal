package juniversal.cplusplus;

import juniversal.JUniversalException;

import org.eclipse.jdt.core.dom.CompilationUnit;


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
		if (! source.startsWith(match, startPosition))
			throw new JUniversalException("Expected source to contain '" + match + "' at position " + startPosition
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
	 * @param justUntilEOL
	 *            if true, then the copying stops just short of the end of line (not including \r or
	 *            \n characters); if there's a multiline comment, it's copied in its entirety and
	 *            the copying stops at the first EOL or code character after that
	 * @return ending position--position of character following space and comments
	 */
	public int copySpaceAndComments(int startPosition, boolean justUntilEOL) {
		int position = startPosition;
		while (true) {
			int currChar = getSourceCharAt(position);

			if (currChar == -1)
				return position;
			else if (currChar == ' ' || currChar == '\t')
				position = copySpacesAndTabs(position);
			else if (currChar == '\r' || currChar == '\n') {
				if (justUntilEOL)
					return position;

				cppWriter.write((char) currChar);
				++position;
			} else if (currChar == '/' && getSourceCharAt(position + 1) == '/')
				position = copySingleLineComment(position);
			else if (currChar == '/' && getSourceCharAt(position + 1) == '*')
				position = copyMultilineComment(position);
			else
				return position;
		}
	}

	private int copySingleLineComment(int position) {
		int commentStartSourceColumn = getSourceLogicalColumn(position);
		int commentStartOutputColumn = cppWriter.getCurrColumn();

		while (true) {
			cppWriter.write("//");
			position += 2;

			while (true) {
				int currChar = getSourceCharAt(position);

				if (currChar == -1 || currChar == '\r' || currChar == '\n')
					break;
				else if (currChar == ' ' || currChar == '\t')
					position = copySpacesAndTabs(position);
				else {
					cppWriter.write((char) currChar);
					++position;
				}
			}

			// See if there is a / / comment on the next line indented exactly the same amount as
			// the first line. If so assume that it's a continuation of the previous comment & copy
			// it as well, taking care that it's indented in the output so all lines of the comment
			// line up together
			boolean commentContinues = false;
			int peekPosition = position;
			int peekChar = getSourceCharAt(peekPosition);
			if (peekChar == '\r') {
				++peekPosition;
				peekChar = getSourceCharAt(peekPosition);
			}
			if (peekChar == '\n') {
				++peekPosition;

				peekPosition = skipSpacesAndTabs(peekPosition);
				peekChar = getSourceCharAt(peekPosition);

				if (peekChar == '/' && getSourceCharAt(peekPosition + 1) == '/' && getSourceLogicalColumn(peekPosition) == commentStartSourceColumn) {
					position = peekPosition;
					commentContinues = true;
				}
			}

			if (! commentContinues)
				return position;

			cppWriter.write("\n");

			int currSourceColumn = getSourceLogicalColumn(position);

			// Ensure that subsequent lines of the comment are indented by the same amount, relative
			// to the first of the comment, as they are in the source
			cppWriter.writeSpacesUntilColumn(commentStartOutputColumn + (currSourceColumn - commentStartSourceColumn));
		}
	}

	private int copyMultilineComment(int position) {
		int currChar;
		int commentStartSourceColumn = getSourceLogicalColumn(position);
		int commentStartOutputColumn = cppWriter.getCurrColumn();

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
			else if (currChar == '\n') {
				cppWriter.write((char) currChar);
				++position;

				position = skipSpacesAndTabs(position);
				int currSourceColumn = getSourceLogicalColumn(position);

				// Ensure that subsequent lines of the multiline comment are indented by the
				// same amount, relative to the first of the comment, as they are in the
				// source
				cppWriter.writeSpacesUntilColumn(commentStartOutputColumn
						+ (currSourceColumn - commentStartSourceColumn));
			} else {
				cppWriter.write((char) currChar);
				++position;
			}
		}
		return position;
	}

	/**
	 * Skips all whitespace, newlines, and comments from the source starting at startPosition and
	 * continuing until the end of the whitespace/newlines/comments.
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
	 * Skips contiguous spaces, tabs, and newlines from the source starting at startPosition and continuing
	 * until the end of the spaces/tabs.
	 * 
	 * @param startPosition
	 *            starting position in source
	 */
	private int getFirstNonspacePositionOnNextLine(int startPosition) {
		int position;
		for (position = startPosition; true; ++position) {
			int currChar = getSourceCharAt(position);
			if (!(currChar == ' ' || currChar == '\t' || currChar == '\r' || currChar == '\n'))
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
	public int getSourceCharAt(int position) {
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

	/**
	 * Returns a description of the specified position, including line number, column number, and
	 * the contents of the entire line with the position marked. Normally this description is put in
	 * error messages. The description should normally be output starting on a new line; the
	 * description doesn't include a line break at the beginning though it does include a line break
	 * in the middle. If more text will be output following the description, the caller should
	 * normally add a line break before appending additional text.
	 * 
	 * @param position position in source file
	 * @return description string for position, with one line break in the middle
	 */
	public String getPositionDescription(int position) {
		int lineNumber = compilationUnit.getLineNumber(position);
		int columnNumber = getSourceLogicalColumn(position);

		int startPosition = compilationUnit.getPosition(lineNumber, 0);
		int endPosition = compilationUnit.getPosition(lineNumber + 1, 0) - 1;

		// Chop off newlines / carriage returns on the end of the line 
		while (source.charAt(endPosition) == '\n' || source.charAt(endPosition) == '\r')
			--endPosition;
		if (endPosition < position)
			endPosition = position;

		String linePrefix = "    " + lineNumber + ":" + columnNumber + ":  ";

		String line = source.substring(startPosition, endPosition + 1);

		StringBuilder description = new StringBuilder();

		description.append(linePrefix);
		description.append(line + "\n");

		// Append spaces or tabs to position the caret at the right spot on the line
		String textBeforePositionMarker = linePrefix + source.substring(startPosition, position);
		int length = textBeforePositionMarker.length();
		for (int i = 0; i < length; ++i)
			if (textBeforePositionMarker.charAt(i) == '\t')
				description.append('\t');
			else description.append(' ');
		description.append("^");

		return description.toString();
	}
}
