package org.jabref.model.strings;

import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {
    @Test
    public void testGetPart() {
        // Should be added
    }

    @Test
    public void testFindEncodingsForString() {
        // Unused in JabRef, but should be added in case it finds some use
    }

    @Test
    public void testIntValueOfLongString() {
        assertEquals(1234567890, StringUtil.intValueOf("1234567890"));
    }

    @Test
    public void testIntValueOfStartWithZeros() {
        assertEquals(1234, StringUtil.intValueOf("001234"));
    }

}
