package org.juniversal.translator.cplusplus;

import org.juniversal.translator.core.TargetWriter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;



public class MemoryCPPWriterFactory extends CPPWriterFactory {
	private CPPProfile cppProfile;
	private HashMap<File, String> cppData = new HashMap<File, String>();

	public MemoryCPPWriterFactory(CPPProfile cppProfile) {
		this.cppProfile = cppProfile;
	}

	@Override TargetWriter createCPPWriter(final File file) {

		Writer writer = new StringWriter() {
			@Override public void close() throws IOException {
				super.close();
				cppData.put(file, getBuffer().toString());
			}
		};

		return new TargetWriter(writer, cppProfile);
	}
}
