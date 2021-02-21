package org.jabref.model.search.matchers;

import org.jabref.model.entry.BibEntry;
import org.jabref.model.search.rules.MockSearchMatcher;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MatcherSetsTest {

    @Test
    public void testBuildAnd() {
        MatcherSet matcherSet = MatcherSets.build(MatcherSets.MatcherType.AND);
        assertTrue(matcherSet.isMatch(new BibEntry()));

        matcherSet.addRule(new MockSearchMatcher(true));
        assertTrue(matcherSet.isMatch(new BibEntry()));

        matcherSet.addRule(new MockSearchMatcher(false));
        assertFalse(matcherSet.isMatch(new BibEntry()));
    }
	
    @Test
    public void testBuildNotWithFalse() {
        NotMatcher matcher = new NotMatcher(new MockSearchMatcher(false));
        assertTrue(matcher.isMatch(new BibEntry()));
    }

}
