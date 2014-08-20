package org.imirp.imirp.mutation.rule;

import junit.framework.Assert;

import org.imirp.imirp.data.Nucleotide;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.region.MutationRegionBuilder;
import org.imirp.imirp.mutation.rule.IdenticalNeighboringChangesPerSiteRule;
import org.junit.Test;

public class IdenticalNeighboringChangesPerSiteRuleTest {
	
	MutationRegion testRegion = null;	
	
	public void initData(int... siteIndexes){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		for(int i = 0; i < siteIndexes.length; i += 2){
			builder.addSites(new MutationSite(siteIndexes[i], siteIndexes[i+1]));
		}
		testRegion = builder.build();
	}
	
	@Test
	public void testSimpleOverlappingGG(){
		final String WILD_SEQUENCE = "AAAAAAAAAAAAAAAA";
		initData(new int[]{0,3,2,5});
		IdenticalNeighboringChangesPerSiteRule rule = new IdenticalNeighboringChangesPerSiteRule(Nucleotide.GUANOSINE, 2);
		Assert.assertTrue(rule.checkRegion(testRegion,   WILD_SEQUENCE, "GGGGAAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "GGAGAAAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "GGGAAAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,    WILD_SEQUENCE, "GGAAGGAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AGGGAAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AGGGGAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,    WILD_SEQUENCE, "AAGGAAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AAGGGAAAAAAAAAAA"));
		Assert.assertTrue(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AAGGGGAAAAAAAAAA"));
		Assert.assertFalse(rule.checkRegion(testRegion,   WILD_SEQUENCE, "AAGAGGAAAAAAAAAA"));
	}
	
}
