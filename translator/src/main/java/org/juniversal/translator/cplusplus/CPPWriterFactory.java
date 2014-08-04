package org.juniversal.translator.cplusplus;

import org.juniversal.translator.core.TargetWriter;

import java.io.File;

public abstract class CPPWriterFactory {
	abstract TargetWriter createCPPWriter(File file);
}
