package cj.writecpp;

import java.io.File;

public abstract class CPPWriterFactory {
	abstract CPPWriter createCPPWriter(File file);
}
