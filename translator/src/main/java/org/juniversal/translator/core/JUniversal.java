/*
 * Copyright (c) 2011-2014, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.translator.core;

import org.juniversal.translator.cplusplus.Translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JUniversal {
	private List<File> javaProjectDirectories;
	private File outputDirectory;
	private int preferredIndent = 4;

	public static void main(String[] args) {
		JUniversal jUniversal = new JUniversal(args);

		new Translator(jUniversal).translate();

		System.out.println("Translation complete; output is in " + jUniversal.outputDirectory);
	}

	public JUniversal(String[] args) {
		this.javaProjectDirectories = new ArrayList<File>();

		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];

			if (arg.startsWith("-")) {
				if (arg.equals("-o")) {
					++i;
					if (i >= args.length)
						usageError();
					arg = args[i];
					
					this.outputDirectory = validateAndNormalizeDirectoryArugment(arg);
				} else
					usageError();
			} else
				this.javaProjectDirectories.add(validateAndNormalizeDirectoryArugment(arg));
		}

		// Ensure that there's at least one input directory & the output directory is specified
		if (this.javaProjectDirectories.size() == 0 || this.outputDirectory == null)
			usageError();
	}

	private File validateAndNormalizeDirectoryArugment(String path) {
		File file = new File(path);
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			throw new UserViewableException("IOException when turning directory argument into canonical path: " + file.getPath());
		}

		if (! file.exists())
			throw new UserViewableException("Directory doesn't exist: " + file.getPath());

		return file;
	}

	private void usageError() {
		System.err.println("Usage: juniversal <java-project-directories>... -o <output-directory>");
		System.exit(1);
	}

	public List<File> getJavaProjectDirectories() {
		return javaProjectDirectories; 
	}

	public File getOutputDirectory() {
		return outputDirectory; 
	}

	public int getPreferredIndent() { 
		return preferredIndent;
	}

	/**
	 * Get all the source files in the specified Java project directories.
	 * 
	 * @return list of all files in the project directories, in project directory order specified on
	 *         command line
	 */
	public String[] getJavaFiles() {
		ArrayList<File> files = new ArrayList<File>();

		for (File directory : javaProjectDirectories) {
			System.out.println(directory);
			try {
				Util.getFilesRecursive(directory, ".java", files);
			} catch (FileNotFoundException e) {
				throw new UserViewableException("Java project directory " + directory + " not found or not an accessible directory");
			}
		}

		int length = files.size();
		String[] filePathsArray = new String[length];
		for (int i = 0; i < length; ++i)
			filePathsArray [i] = files.get(i).getPath(); 

		return filePathsArray;
	}
}
