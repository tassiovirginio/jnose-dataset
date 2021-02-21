package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.TextUtil;
import org.jsoup.integration.ParseTest;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 Tests for Document.

 @author Jonathan Hedley, jonathan@hedley.net */
public class DocumentTest {

    @Test public void DocumentsWithSameContentAreEqual() throws Exception {
        Document docA = Jsoup.parse("<div/>One");
        Document docB = Jsoup.parse("<div/>One");
        Document docC = Jsoup.parse("<div/>Two");

        assertFalse(docA.equals(docB));
        assertTrue(docA.equals(docA));
        assertEquals(docA.hashCode(), docA.hashCode());
        assertFalse(docA.hashCode() == docC.hashCode());
    }

    @Test public void DocumentsWithSameContentAreVerifialbe() throws Exception {
        Document docA = Jsoup.parse("<div/>One");
        Document docB = Jsoup.parse("<div/>One");
        Document docC = Jsoup.parse("<div/>Two");

        assertTrue(docA.hasSameValue(docB));
        assertFalse(docA.hasSameValue(docC));
    }
    
}
