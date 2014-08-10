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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Util {

	/**
	 * Count number of times specified character appears in string.
	 * 
	 * @param string
	 *            string in question
	 * @param character
	 *            character to count
	 * @return count of occurrences of character in string
	 */
	public static int countChars(String string, int character) {
		int count = 0;
		int currIndex = 0;
		for (;;) {
			currIndex = string.indexOf(character, currIndex);
			if (currIndex == -1)
				break;
			++currIndex; // Start next search from next character
			++count;
		}
		return count;
	}

	/**
	 * Recursively get a list of all files in the specified directory, having the given suffix. The
	 * resulting files are added to the files list.
	 * 
	 * @param directory
	 *            starting directory
	 * @param suffix
	 *            suffix (e.g. ".java") to match on; pass "" if want to match all files
	 * @param files
	 *            list to add files to, which caller should create
	 * @throws FileNotFoundException
	 */
	static public void getFilesRecursive(File directory, String suffix, List<File> files) throws FileNotFoundException {
		File[] filesList = directory.listFiles();
		if (filesList == null)
			throw new FileNotFoundException(directory.getPath());

		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				getFilesRecursive(file, suffix, files);
			else {
				if (file.getName().endsWith(suffix))
					files.add(file);
			}
		}
	}

	public static String readFile(String filePath) {
		File file = new File(filePath);

		FileReader fileReader = null;
		try {
			StringBuilder stringBuilder = new StringBuilder();

			fileReader = new FileReader(file);

			char[] contentsBuffer = new char[1024];
			int charsRead = 0;
			while ((charsRead = fileReader.read(contentsBuffer)) != -1)
				stringBuilder.append(contentsBuffer, 0, charsRead);

			fileReader.close();

			return stringBuilder.toString();
		} catch (FileNotFoundException e) {
			throw new JUniversalException(e);
		} catch (IOException ioe) {
			throw new JUniversalException(ioe);
		}
	}
}
