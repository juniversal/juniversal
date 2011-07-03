package juniversal.cplusplus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import juniversal.JUniversalException;
import juniversal.UserViewableException;


public class CPPWriter {
	private int currLine;                             // Current line (1 based)
	private int currColumn;                           // Current column on line (1 based)
	private boolean accumulatingSpacesAtBeginningOfLine;
	private int spacesAtBeginningOfLine;
	private Writer writer;
	// Additional amount to indent or (if negative) outdent
	private int additionalIndentation = 0;
	private CPPProfile cppProfile;
	private int destTabStop = -1;


	public CPPWriter(File file, CPPProfile cppProfile) {
		try {
			init(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "Cp1252")),
					cppProfile);
		} catch (UnsupportedEncodingException e) {
			throw new JUniversalException(e);
		} catch (FileNotFoundException e) {
			throw new UserViewableException("Ouptut file " + file.getAbsolutePath()
					+ " could not be created");
		}
	}

	public CPPWriter(Writer writer, CPPProfile cppProfile) {
		init(writer, cppProfile);
	}

	private void init(Writer writer, CPPProfile cppProfile) {
		this.writer = writer;
		this.cppProfile = cppProfile;
		destTabStop = cppProfile.getTabStop();

		currLine = 1;
		currColumn = 1;
		accumulatingSpacesAtBeginningOfLine = true;
		spacesAtBeginningOfLine = 0;
	}

	public CPPProfile getCPPProfile() { return cppProfile; }

	public int getCurrLine() { return currLine; }

	/**
	 * Return the current column, where the next output character will go.
	 * 
	 * @return current column
	 */
	public int getCurrColumn() {
		return currColumn;
	}

	public void write(String string) {
		try {
			int length = string.length();
			for (int i = 0; i < length; ++i)
				writeCharInternal(string.charAt(i));
		} catch (IOException e) {
			throw new JUniversalException(e);
		}
	}

	public void write(char character) {
		try {
			writeCharInternal(character);
		} catch (IOException e) {
			throw new JUniversalException(e);
		}
	}

	/**
	 * Space over so that the next character written will be at the specified column. If the current
	 * column is already past the specified column, nothing is output.
	 * 
	 * @param column
	 *            desired column
	 */
	public void writeSpacesUntilColumn(int column) {
		while (getCurrColumn() < column)
			write(" ");
	}

	private void writeCharInternal(char character) throws IOException {
		if (character == '\r')
			;
		else if (character == '\n') {
			// If only whitespace on a line, don't write out indentation

			writer.write("\r\n");
			++currLine;

			accumulatingSpacesAtBeginningOfLine = true;
			spacesAtBeginningOfLine = additionalIndentation;
			currColumn = additionalIndentation;
		}
		else if (character == ' ') {
			if (spacesAtBeginningOfLine != -1)
				++spacesAtBeginningOfLine;
			else writer.write(' ');

			++currColumn;
		}
		else if (character == '\t')
			throw new JUniversalException("Can't directly write tabs to a CPPWriter");
		else {
			// Write any accumulated indentation plus any defined additional indentation
			if (accumulatingSpacesAtBeginningOfLine) {
				writeAccumulatedSpacesAtBeginningOfLine();
				accumulatingSpacesAtBeginningOfLine = false;
			}

			writer.write(character);
			++currColumn;
		}
	}

	private void writeAccumulatedSpacesAtBeginningOfLine() throws IOException {
		int spaces = spacesAtBeginningOfLine;
		if (spaces <= 0)
			return;

		// If tabs are enabled for the output, tabify what we can at the beginning of a line
		if (destTabStop != -1) {
			int tabs = spaces / destTabStop;
			for (int i = 0; i < tabs; ++i)
				writer.write('\t');

			// What can't be tabified should be a space
			spaces = spaces % destTabStop;
		}

		for (int i = 0; i < spaces; ++i)
			writer.write(' ');
	}

	public int setAdditionalIndentation(int value) {
		int oldValue = this.additionalIndentation;
		this.additionalIndentation = value;

		// If we're at the beginning of the line, then make the additional indentation take effect immediately
		if (currColumn == oldValue + 1) {
			int delta = value - oldValue;
			currColumn += delta;
			spacesAtBeginningOfLine += delta; 
		}

		return oldValue;
	}

	/**
	 * Increment the additional indentation by whatever the preferred indent is.
	 */
	public void incrementByPreferredIndent() {
		setAdditionalIndentation(additionalIndentation + cppProfile.getPreferredIndent());
	}

	/**
	 * Decrement the additional indentation by whatever the preferred indent is.
	 */
	public void decrementByPreferredIndent() {
		setAdditionalIndentation(additionalIndentation - cppProfile.getPreferredIndent());
	}
}
