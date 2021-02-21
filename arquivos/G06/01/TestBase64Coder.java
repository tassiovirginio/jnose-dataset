// Tests for the Base64Coder class.

package net.bull.javamelody.internal.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

/**
 * Test for Base64Coder.
 * <br/>
 * Project home page: <a href="http://www.source-code.biz/base64coder/java/">www.source-code.biz/base64coder/java</a><br>
 * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br>
 * Multi-licensed: EPL / LGPL / GPL / AL / BSD / MIT.
 * Changed on format and java syntax by Emeric Vernat
 */
public class TestBase64Coder {


	/**
	 * Test Base64Coder against sun.misc.BASE64Encoder/Decoder with random data.
	 * Line length below 76.
	 * @throws IOException e
	 */
	@Test
	public void test2() throws IOException {
		final int maxLineLen = 76 - 1; // the Sun encoder adds a CR/LF when a line is longer
		final int maxDataBlockLen = maxLineLen * 3 / 4;
		final sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
		final sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
		final Random rnd = new Random(0x538afb92);
		for (int i = 0; i < 100; i++) {
			final int len = rnd.nextInt(maxDataBlockLen + 1);
			final byte[] b0 = new byte[len];
			rnd.nextBytes(b0);
			final String e1 = new String(Base64Coder.encode(b0));
			final String e2 = sunEncoder.encode(b0);
			assertEquals("test2", e2, e1);
			final byte[] b1 = Base64Coder.decode(e1);
			final byte[] b2 = sunDecoder.decodeBuffer(e2);
			assertArrayEquals(b0, b1);
			assertArrayEquals(b0, b2);
		}
	}

} // end class TestBase64Coder
