package org.jabref.logic.importer.fileformat;

import java.nio.file.Paths;
import java.util.Arrays;

import org.jabref.logic.importer.Importer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomImporterTest {

    private CustomImporter importer;

    @Before
    public void setUp() throws Exception {
        importer = asCustomImporter(new CopacImporter());
    }

    @Test
    public void testGetAsStringList() {
        assertEquals(Arrays.asList("src/main/java/org/jabref/logic/importer/fileformat/CopacImporter.java",
                "org.jabref.logic.importer.fileformat.CopacImporter"), importer.getAsStringList());
    }

    @Test
    public void equalsWithSameReference() {
        assertEquals(importer, importer);
    }
}
