package juniversal;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class CPPProfile {
	private String int8Type = "char";
	private String int16Type = "short";
	private String int32Type = "int";
	private String int64Type = null;   // Not supported by default
	private String float32Type = "float";
	private String float64Type = "double";
	private String unsignedInt32UnicodeCharType = "unsigned short";
	private int tabStop = -1;
	private int preferredIndent = 4;
	private boolean classBraceOnSameLine = true;


	public String getInt8Type() { return int8Type; }
	public String getInt16Type() { return int16Type; }
	public String getInt32Type() { return int32Type; }
	public String getInt64Type() { return int64Type; }

	public String getFloat32Type() { return float32Type; }
	public String getFloat64Type() { return float64Type; }

	public String getUnsignedInt32UnicodeCharType() { return unsignedInt32UnicodeCharType; }

	/**
	 * Returns how many spaces a tab should correspond to in the C++ output (e.g. 4 or 2). If tabs
	 * shouldn't shouldn't be used in the output, returns -1.
	 *
	 * @return number of spaces to use for tabs in output or -1 if shouldn't use tabs
	 */
	public int getTabStop() { return tabStop; }

	/**
	 * Sets how many spaces a tab should correspond to in the C++ output (e.g. 4 or 2).  If tabs
	 * shouldn't be used in the output, set it to -1.
	 *
	 * @param tabStop number of spaces to use for tabs in output or -1 if shouldn't use tabs
	 */
	public void setTabStop(int tabStop) {
		this.tabStop = tabStop;
	}

	/**
	 * Gets the preferred indent, used for output that's generated as opposed to just copying the
	 * source indent.
	 * 
	 * @return preferred indent
	 */
	public int getPreferredIndent() {
		return preferredIndent;
	}

	/**
	 * Sets the preferred indent, used for output that's generated as opposed to just copying the
	 * source indent.
	 * 
	 * @param preferredIndent
	 */
	public void setPreferredIndent(int preferredIndent) {
		this.preferredIndent = preferredIndent;
	}

	/**
	 * Returns whether for a class declaration the left brace, {, should be on the same line as the class keyword or
	 * on the next line.
	 * 
	 * @return true if brace should be on same line
	 */
	public boolean getClassBraceOnSameLine() {
		return classBraceOnSameLine;
	}

	/**
	 * Sets whether for a class declaration the left brace, {, should be on the same line as the
	 * class keyword or on the next line.
	 * 
	 * @param value
	 *            true if brace should be on same line, false for next line
	 */
	public void setClassBraceOnSameLine(boolean value) {
		classBraceOnSameLine = value;
	}
}
