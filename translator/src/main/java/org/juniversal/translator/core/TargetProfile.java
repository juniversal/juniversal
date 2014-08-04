package org.juniversal.translator.core;

/**
 * @author Bret Johnson
 * @since 7/20/2014 6:45 PM
 */
public class TargetProfile {
    private int tabStop = -1;
    private boolean classBraceOnSameLine = true;

    /**
     * Returns how many spaces a tab should correspond to in the C++ output (e.g. 4 or 2). If tabs
     * shouldn't shouldn't be used in the output, returns -1.
     *
     * @return number of spaces to use for tabs in output or -1 if shouldn't use tabs
     */
    public int getTabStop() {
        return tabStop;
    }

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
     * @param value true if brace should be on same line, false for next line
     */
    public void setClassBraceOnSameLine(boolean value) {
        classBraceOnSameLine = value;
    }
}
