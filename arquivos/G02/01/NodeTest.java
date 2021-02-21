package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.TextUtil;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
/**
 Tests Nodes

 @author Jonathan Hedley, jonathan@hedley.net */
public class NodeTest {

    @Test public void handlesAbsOnImage() {
        Document doc = Jsoup.parse("<p><img src=\"/rez/osi_logo.png\" /></p>", "https://jsoup.org/");
        Element img = doc.select("img").first();
        assertEquals("https://jsoup.org/rez/osi_logo.png", img.attr("abs:src"));
        assertEquals(img.absUrl("src"), img.attr("abs:src"));
    }

    @Test public void handlesAbsPrefixOnHasAttr() {
        // 1: no abs url; 2: has abs url
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='https://jsoup.org/'>Two</a>");
        Element one = doc.select("#1").first();
        Element two = doc.select("#2").first();

        assertFalse(one.hasAttr("abs:href"));
        assertTrue(one.hasAttr("href"));
        assertEquals("", one.absUrl("href"));

        assertTrue(two.hasAttr("abs:href"));
        assertTrue(two.hasAttr("href"));
        assertEquals("https://jsoup.org/", two.absUrl("href"));
    } 
}
