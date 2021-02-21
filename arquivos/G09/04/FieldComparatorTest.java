package org.jabref.logic.bibtex.comparator;

import org.jabref.model.entry.BibEntry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldComparatorTest {
    @Test
    public void nameOfComparisonField() throws Exception {
        FieldComparator comparator = new FieldComparator("title");
        assertEquals("title", comparator.getFieldName());
    }

    @Test
    public void nameOfComparisonFieldAlias() throws Exception {
        FieldComparator comparator = new FieldComparator("author/editor");
        assertEquals("author/editor", comparator.getFieldName());
    }
}
