package juniversal.cplusplus;

import java.util.List;

import juniversal.ASTUtil;
import juniversal.ContextPositionMismatchException;
import juniversal.JUniversalException;
import juniversal.UserViewableException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.SimpleName;

public class Context {
	private CompilationUnit compilationUnit;
	private CPPProfile cppProfile;
	private CPPWriter cppWriter;
	private SourceCopier sourceCopier;
	private int position;
	private OutputType outputType;
	private int preferredIndent = 4;    // TODO: Set

	private boolean writingVariableDeclarationNeedingStar;
	private boolean writingMethodImplementation;
	private SimpleName typeDeclarationName;

	public Context(CompilationUnit compilationUnit, String source, int sourceTabStop, CPPProfile cppProfile,
			CPPWriter cppWriter, OutputType outputType) {
		this.compilationUnit = compilationUnit;
		this.cppProfile = cppProfile;
		this.cppWriter = cppWriter;
		this.outputType = outputType;

		sourceCopier = new SourceCopier(compilationUnit, source, sourceTabStop, cppWriter);
		position = compilationUnit.getStartPosition();
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
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

	public int getPreferredIndent() {
		return preferredIndent;
	}

	public void setWritingMethodImplementation(boolean value) {
		this.writingMethodImplementation = value;
	}

	public boolean isWritingMethodImplementation() {
		return this.writingMethodImplementation;
	}

	public boolean isWritingVariableDeclarationNeedingStar() {
		return writingVariableDeclarationNeedingStar;
	}

	public void setWritingVariableDeclarationNeedingStar(boolean value) {
		this.writingVariableDeclarationNeedingStar = value;
	}

	public void setTypeDeclarationName(SimpleName typeName) {
		this.typeDeclarationName = typeName;
	}

	public SimpleName getTypeDeclarationName() {
		return this.typeDeclarationName;
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
			throw new ContextPositionMismatchException("Context is positioned at:\n" + getPositionDescription(position)
					+ "\n  when expected it to be positioned at:\n" + getPositionDescription(expectedPosition));
	}

	public SourceCopier getSourceCopier() {
		return sourceCopier;
	}

	public String getPositionDescription(int position) {
		return sourceCopier.getPositionDescription(position);
	}

	public int getSourceLogicalColumn(int position) {
		return sourceCopier.getSourceLogicalColumn(position);
	}

	public int getSourceLogicalColumn() {
		return sourceCopier.getSourceLogicalColumn(getPosition());
	}

	public void copySpaceAndComments() {
		position = sourceCopier.copySpaceAndComments(position, false);
	}

	public void copySpaceAndCommentsUntilEOL() {
		position = sourceCopier.copySpaceAndComments(position, true);
	}

	public void skipSpaceAndComments() {
		position = sourceCopier.skipSpaceAndComments(position, false);
	}

	public void skipSpaceAndCommentsUntilEOL() {
		position = sourceCopier.skipSpaceAndComments(position, true);
	}

	public void skipNewline() {
		position = sourceCopier.skipNewline(position);
	}

	public void backwardSkipSpaceAndComments() {
		position = sourceCopier.skipSpaceAndCommentsBackward(position);
	}

	public void skipSpacesAndTabsBackward() {
		position = sourceCopier.skipSpacesAndTabsBackward(position);
	}

	/**
	 * Ensure that the Java source contains the specified match string at it's current position &
	 * advance past
	 * 
	 * @param match
	 *            string to ensure occurs in source
	 */
	public void match(String match) {
		position = sourceCopier.match(position, match);
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
	 * Write the specified number of spaces to the output.
	 * 
	 * @param string
	 *            string to write to C++ output.
	 */
	public void writeSpaces(int count) {
		for (int i = 0; i < count; ++i)
			cppWriter.write(" ");
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
		IExtendedModifier lastExtendedModifier = (IExtendedModifier) extendedModifiers.get(size - 1);
		setPosition(ASTUtil.getEndPosition((ASTNode) lastExtendedModifier));
	}

	public void throwSourceNotSupported(String baseMessage) {
		throw new UserViewableException(baseMessage + "\n" + getPositionDescription(position));
	}

	public void throwInvalidAST(String baseMessage) {
		throw new JUniversalException(baseMessage + "\n" + getPositionDescription(position));
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public CPPWriter getCPPWriter() {
		return cppWriter;
	}
}
