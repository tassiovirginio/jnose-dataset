/*
 * Copyright 2011 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitblit.tests;

import java.io.File;

import org.junit.Test;

import com.gitblit.utils.FileUtils;

public class FileUtilsTest extends GitblitUnitTest {

	@Test
	public void testReadContent() throws Exception {
		File dir = new File(System.getProperty("user.dir"));
		String rawContent = FileUtils.readContent(new File(dir, "LICENSE"), "\n");
		assertTrue(rawContent.trim().startsWith("Apache License"));
	}

	@Test
	public void testWriteContent() throws Exception {
		String contentA = "this is a test";
		File tmp = File.createTempFile("gitblit-", ".test");
		FileUtils.writeContent(tmp, contentA);
		String contentB = FileUtils.readContent(tmp, "\n").trim();
		assertEquals(contentA, contentB);
	}

}