package cj.writecpp;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;

import cj.ASTUtil;
import cj.CJException;
import cj.CPPProfile;
import cj.SourceNotSupportedException;

public class WriteCPPContext {
	private CompilationUnit compilationUnit;
	private CPPProfile cppProfile;
	private CPPWriter cppWriter;
	private SourceCopier sourceCopier;
	private int position;
	private OutputType outputType; 


	public WriteCPPContext(CompilationUnit compilationUnit, String source, int sourceTabStop, CPPProfile cppProfile,
			CPPWriter cppWriter, OutputType outputType) {
		this.compilationUnit = compilationUnit;
		this.cppProfile = cppProfile;
		this.cppWriter = cppWriter;
		this.outputType = outputType;

		sourceCopier = new SourceCopier(compilationUnit, source, sourceTabStop, cppWriter);
		position = compilationUnit.getStartPosition();
	}

	/**
	 * Get the current position for the context.
	 * 
	 * @return current position for context
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Change the context position to the specified new position. Only needed when position changes
	 * explicitly, other than as a result of calling methods of this class to match and skip.
	 * 
	 * @param position
	 *            new position
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Get the CPPProfile object, describing the target C++ compiler and how the C++ should be
	 * generated.
	 * 
	 * @return CPPProfile object
	 */
	public CPPProfile getCPPProfile() {
		return cppProfile;
	}

	/**
	 * Assert that the current position for the context is expectedPosition. Throw an exception if
	 * it isn't.
	 * 
	 * @param expectedPosition
	 *            position where expect that context is at
	 */
	public void assertPositionIs(int expectedPosition) {
		if (position != expectedPosition)
			throw new CJException("Context is positioned at " + position + " when expected it to be positioned at "
					+ expectedPosition);
	}

	/**
	 * Copies to output the whitespace, newlines, and comments that appear right after the specified
	 * position in the source.
	 * 
	 * @param node
	 *            node in question
	 */
	public void copySpaceAndComments() {
		position = sourceCopier.copySpaceAndComments(position);
	}

	public void skipSpaceAndComments() {
		position = sourceCopier.skipSpaceAndComments(position);
	}

	/**
	 * Ensure that the Java source contains the specified match string at it's current position &
	 * advance past it. Also write "write" to output.
	 * 
	 * @param match
	 *            string to ensure occurs in source
	 * @param write
	 *            string to write to C++ output
	 */
	public void matchAndWrite(String match, String write) {
		position = sourceCopier.match(position, match);
		write(write);
	}

	/**
	 * Match the specified string in the Java source and write it to the output. Equivalent to
	 * matchAndWrite(matchAndWrite, matchAndWrite)
	 * 
	 * @param matchAndWrite
	 *            string to both match and write to output
	 */
	public void matchAndWrite(String matchAndWrite) {
		position = sourceCopier.match(position, matchAndWrite);
		write(matchAndWrite);
	}

	/**
	 * Write the specified string to the C++ output.
	 * 
	 * @param string
	 *            string to write to C++ output.
	 */
	public void write(String string) {
		cppWriter.write(string);
	}
	
	/**
	 * Write the specified string to the C++ output, followed by a newline.
	 * 
	 * @param string
	 *            string to write to C++ output
	 */
	public void writeln(String string) {
		cppWriter.write(string);
		cppWriter.write("\n");
	}

	/**
	 * Write a newline to the C++ output.
	 */
	public void writeln() {
		cppWriter.write("\n");
	}

	/**
	 * Write the specified character to the C++ output. Equivalent to
	 * write(Character.toString(character))
	 * 
	 * @param character
	 *            character to write to output
	 */
	public void write(char character) {
		cppWriter.write(character);
	}

	/**
	 * Set the context's current position to just after the end of the modifiers. If there are no
	 * modifiers in the list, the position remains unchanged.
	 * 
	 * @param extendedModifiers
	 *            modifiers
	 */
	public void skipModifiers(List<?> extendedModifiers) {
		int size = extendedModifiers.size();
		if (size == 0)
			return;
		IExtendedModifier lastExtendedModifier = ((IExtendedModifier) extendedModifiers.get(size - 1));
		setPosition(ASTUtil.getEndPosition((ASTNode) lastExtendedModifier));
	}

	public void throwSourceNotSupported(String baseMessage) {
		throw new SourceNotSupportedException(compilationUnit, position, baseMessage);
	}
	
	public OutputType getOutputType() {
		return outputType;
	}

	public CPPWriter getCPPWriter() {
		return cppWriter;
	}
}
