package org.imirp.imirp.mutation.rule;

import junit.framework.Assert;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.region.MutationRegionBuilder;
import org.imirp.imirp.mutation.rule.SequentialChangesOnlyPerSiteRule;
import org.junit.Test;

public class SequentialChangesOnlyPerSiteRuleTest {
	
	MutationRegion testRegion = null;	
	
	public void initData(int... siteIndexes){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		for(int i = 0; i < siteIndexes.length; i += 2){
			builder.addSites(new MutationSite(siteIndexes[i], siteIndexes[i+1]));
		}
		testRegion = builder.build();
	}
	
	@Test
	public void testSingleSite(){
		initData(new int[]{0,7});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		Assert.assertTrue(rule.checkRegion(testRegion, "AAAAAAAA",  "GGAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion, "AAAAAAAA", "GGGAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion, "AAAAAAAA", "GAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion, "AAAAAAAA", "GAAAAAGG"));
	}
	
	@Test
	public void testTTAT(){
		initData(new int[]{0,3});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		Assert.assertFalse(rule.checkRegion(testRegion, "AAAA",  "TTAT"));
	}
	
	@Test
	public void testTTATAT(){
		initData(new int[]{0,5});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		Assert.assertFalse(rule.checkRegion(testRegion, "AAAAAA",  "TTATAT"));
	}
	
	@Test
	public void testSingleSiteGreatorOrEqual(){
		initData(new int[]{0,7});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, true);
		Assert.assertTrue(rule.checkRegion(testRegion, "AAAAAAAA", "GGGAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion, "AAAAAAAA", "GGGGGGGG"));
		Assert.assertTrue(rule.checkRegion(testRegion, "AAAAAAAA", "GGAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion, "AAAAAAAA", "GAAAAAAA"));
	}
	
	@Test
	public void testDoubleSiteNoOverlap(){
		initData(new int[]{1,8,9,16});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "ACCAAAAAAAAAGGAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "ACCAAAAAAAAAAAAGG"));
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "AAAAAAACCGGAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAGG"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AATTAAAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AATTTTAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AATTTTAAAAAAAAAGG"));
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "AAAATTAAAAAAAAAGG"));
		
	}
	
	@Test
	public void testGG(){
		final String WILD_SEQUENCE = "AAAAAAAAAAAAAAAA";
		initData(new int[]{0,3,2,5});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "GGGGAAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,    WILD_SEQUENCE, "GGAAGGAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AGGGAAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AGGGGAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,    WILD_SEQUENCE, "AAGGAAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AAGGGAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AAGGGGAAAAAAAAAA"));
	}
	
	@Test
	public void testDoubleSiteWithOverlap(){
		initData(new int[]{1,8,7,14});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "ACCAAAAAAAAAGGAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "ACCAAAAAAAAAAGGAA"));
		// Ensure that when we have changes within an overlap nothing funny happens
		Assert.assertFalse(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "AAAAAAACCGGAAAAAA")); // tests overlap
		Assert.assertTrue(rule.checkRegion(testRegion,   "AAAAAAAAAAAAAAAAA", "AAAAAAATTAAAAAAAA")); // tests overlap
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAGG"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AATTAAAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AATTTTAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,  "AAAAAAAAAAAAAAAAA", "AATTTTAAAAAAAAAGG"));
		
	}
	
	@Test
	public void testMultipleSitesOverlaps(){
		initData(new int[]{
				1,8,7,14, // overlaps at 7,8 
				16,23, 
				24,31
			});
		SequentialChangesOnlyPerSiteRule rule = new SequentialChangesOnlyPerSiteRule(2, false);
		String origSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		
		// Some easy conditions
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, origSequence));
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, origSequence + "AAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, "AAAA" + origSequence + "AAAAA"));
		
		Assert.assertTrue(rule.checkRegion(testRegion, origSequence, genMutation(origSequence, new int[]{7,8,16,17,24,25})));
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, genMutation(origSequence, new int[]{5,7,8,16,17,24,25})));
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, genMutation(origSequence, new int[]{6,7,8,16,17,24,25})));
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, genMutation(origSequence, new int[]{7,8,16,17,24,26})));
		Assert.assertFalse(rule.checkRegion(testRegion, origSequence, genMutation(origSequence, new int[]{5,6,9,10,16,17,24,26})));
	}
	
	private String genMutation(String seq, int... mutIndexes){
		StringBuilder sb = new StringBuilder(seq);
		for(int seqIndex = 0; seqIndex < mutIndexes.length; seqIndex++){
			char nucToMut = seq.charAt(mutIndexes[seqIndex]);
			Nucleotide nuc = Nucleotide.getNucleotide(nucToMut);
			sb.setCharAt(mutIndexes[seqIndex], nuc.next().toChar());
		}		
		return sb.toString();
	}
}
