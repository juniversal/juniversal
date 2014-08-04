package org.juniversal.translator.cplusplus;

import org.juniversal.translator.core.TargetProfile;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class CPPProfile extends TargetProfile {
    private String int8Type = "char";
    private String int16Type = "short";
    private String int32Type = "int";
    private String int64Type = "long64";
    private String float32Type = "float";
    private String float64Type = "double";
    private String unsignedInt32UnicodeCharType = "unsigned short";


    public String getInt8Type() {
        return int8Type;
    }

    public String getInt16Type() {
        return int16Type;
    }

    public String getInt32Type() {
        return int32Type;
    }

    public String getInt64Type() {
        return int64Type;
    }

    public String getFloat32Type() {
        return float32Type;
    }

    public String getFloat64Type() {
        return float64Type;
    }

    public String getUnsignedInt32UnicodeCharType() {
        return unsignedInt32UnicodeCharType;
    }
}
