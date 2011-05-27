package cj.writecpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import cj.CJException;
import cj.CPPProfile;
import cj.UserViewableException;

public class CPPWriter {
	private int currLine;
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
			throw new CJException(e);
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
		spacesAtBeginningOfLine = 0;
	}

	public CPPProfile getCPPProfile() { return cppProfile; }

	public void write(String string) {
		try {
			int length = string.length();
			for (int i = 0; i < length; ++i)
				writeCharInternal(string.charAt(i));
		} catch (IOException e) {
			throw new CJException(e);
		}
	}

	public void write(char character) {
		try {
			writeCharInternal(character);
		} catch (IOException e) {
			throw new CJException(e);
		}
	}

	private void writeCharInternal(char character) throws IOException {
		if (character == '\r')
			;
		else if (character == '\n') {
			// If only whitespace on a line, don't write out indentation

			writer.write("\r\n");
			++currLine;
			spacesAtBeginningOfLine = 0;
		}
		else if (character == ' ') {
			if (spacesAtBeginningOfLine != -1)
				++spacesAtBeginningOfLine;
			else writer.write(' ');
		}
		else if (character == '\t')
			throw new CJException("Can't directly write tabs to a CPPWriter");
		else {

			// Write any accumulated indentation plus any defined additional indentation
			if (spacesAtBeginningOfLine != -1) {

				int spaces = additionalIndentation + spacesAtBeginningOfLine;

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
				spacesAtBeginningOfLine = -1;
			}

			writer.write(character);
		}
	}

	/**
	 * Increment the additional indentation by the specified amount, so that subsequent lines are indented more.
	 * 
	 * @param amount amount to increment indent
	 */
	public void incrementAdditionalIndentation(int amount) {
		additionalIndentation += amount;
	}

	/**
	 * Decrement the additional indentation, normally to undo a previous increment.
	 * 
	 * @param amount amount to decrement indent
	 */
	public void decrementAdditionalIndentation(int amount) {
		additionalIndentation -= amount;
	}

	/**
	 * Increment the additional indentation by whatever the preferred indent is.
	 */
	public void incrementByPreferredIndent() {
		additionalIndentation += cppProfile.getPreferredIndent();
	}
	
	/**
	 * Decrement the additional indentation by whatever the preferred indent is.
	 */
	public void decrementByPreferredIndent() {
		additionalIndentation -= cppProfile.getPreferredIndent();
	}
}
