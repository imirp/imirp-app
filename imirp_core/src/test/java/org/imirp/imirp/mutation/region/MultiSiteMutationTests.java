package org.imirp.imirp.mutation.region;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.imirp.imirp.mutation.MutationCallback;
import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.mutation.region.MultiSiteMutationGenerator;
import org.imirp.imirp.mutation.rule.MutationRule;
import org.imirp.imirp.mutation.rule.NaiveMutationRule;
import org.junit.Before;
import org.junit.Test;

public class MultiSiteMutationTests {
	static final String WILD_TEST_SEQ= "AGGGTACAGTATGATTCACGTACGATAGCTAGTCGCAGCATACTCGTGATTCAGGTGTGCAGCTGGCCAGGCTTAGAGCTAGTACAGTGTACGATAGCTAGTTAGTGTATGATGATTCACGTACAGTATGATTCACGTACGATAGCTAGTGTACAGTATGATTCACAACGCAGCAGCATTATTCAGGACGCAGCAGCATTATGTGCAGCATTATGTGCATTTACTCGTGGCCAGGCTTAGAGCTAGTACAGTATGATTCAGGACCAATGCTAGTAGCATGT";
	static ArrayList<MutationSite> mutationSites;
	static ArrayList<MutationRule> mutationRules;
	
	@Before
	public void reset(){
		mutationSites = new ArrayList<MutationSite>();
		mutationRules = new ArrayList<MutationRule>();
	}
	
	@Test(timeout=10000)
	public void testSingleSiteNoRules(){
		mutationSites.add(new MutationSite(0, 7));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the one site
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {			
			@Override
			protected void actOnSequence(String mutatedSequence, String regionId) {
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals(mutationCount.get(), (int)(Math.pow(4, 8)));
	}
	
	@Test(timeout=10000)
	public void testMultipleIndependantSitesNoRules(){
		mutationSites.add(new MutationSite(0, 7));
		mutationSites.add(new MutationSite(20, 27));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the one site
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 8) * 2), mutationCount.get());
	}
	
	@Test(timeout=10000)
	public void testMultipleDependentSitesNoOverlapNoRules(){
		mutationSites.add(new MutationSite(10, 12));
		mutationSites.add(new MutationSite(13, 15));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 6)), mutationCount.get());
	}
	
	@Test(timeout=10000)
	public void testMultipleDependentSitesMaxSeparationNoOverlapNoRules(){
		mutationSites.add(new MutationSite(10, 12));
		mutationSites.add(new MutationSite(18, 20));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 6)), mutationCount.get());
	}
	
	@Test(timeout=10000)
	public void testMultipleIndependentSitesMinSeparationNoOverlapNoRules(){
		mutationSites.add(new MutationSite(10, 12));
		mutationSites.add(new MutationSite(20, 22));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 3) * 2), mutationCount.get());
	}
	
	@Test(timeout=10000)
	public void testMultipleDependentSitesOneOverlapNoRules(){
		mutationSites.add(new MutationSite(10, 12));
		mutationSites.add(new MutationSite(12, 14));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 5)), mutationCount.get());
	}
	
	@Test(timeout=10000)
	public void testSingleSiteRequireTwoDifferencesRule(){
		mutationSites.add(new MutationSite(10, 12));
		mutationRules.add(new NaiveMutationRule(2));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		
		// Generate all mutations possible for the one site
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 3) - 10), mutationCount.get());
	}
	
	@Test(timeout=10000)
	public void testMultipleIndependantSitesTwoDifferencesRule(){
		mutationSites.add(new MutationSite(0, 7));
		mutationSites.add(new MutationSite(20, 27));
		mutationRules.add(new NaiveMutationRule(2));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the one site
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 8) * 2 - (2 + 3 * 16)), mutationCount.get());
	}
	
	@Test(timeout=100000)
	public void testMultipleAdjacentDependentSitesNoOverlapTwoDifferencesRule(){
		mutationSites.add(new MutationSite(11, 14));
		mutationSites.add(new MutationSite(15, 18));
		mutationRules.add(new NaiveMutationRule(2));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 8) - (1 + 3*8)), mutationCount.get());
	}
	
	@Test(timeout=100000)
	public void testMultipleDependentSitesNoOverlapTwoDifferencesRule(){
		mutationSites.add(new MutationSite(11, 14));
		mutationSites.add(new MutationSite(19, 22));
		mutationRules.add(new NaiveMutationRule(2));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 8) - (1 + 3*8)), mutationCount.get());
	}
	
	@Test(timeout=1000000)
	public void testMultipleDependentSitesOneOverlapTwoDifferencesRule(){
		mutationSites.add(new MutationSite(10, 12));
		mutationSites.add(new MutationSite(12, 14));
		mutationRules.add(new NaiveMutationRule(2));
		
		MultiSiteMutation msm = new MultiSiteMutation(WILD_TEST_SEQ, mutationSites, mutationRules);
		final AtomicInteger mutationCount = new AtomicInteger(0);
		// Generate all mutations possible for the sites
		MultiSiteMutationGenerator.mutateAndOperate(msm, new MutationCallback() {
			
			@Override
			public void actOnSequence(String mutatedSequence, String regionId) {				
				mutationCount.incrementAndGet();
			}
		});
		
		// Verify that we received all permutations
		Assert.assertEquals((int)(Math.pow(4, 5) - (1 + 5 * 3)), mutationCount.get());
	}
}
