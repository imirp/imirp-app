package org.imirp.imirp.mutation.region;

import junit.framework.Assert;

import org.imirp.imirp.mutation.MutationSite;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.mutation.region.MutationRegionBuilder;
import org.imirp.imirp.mutation.region.OverlappingRegion;
import org.junit.Test;

public class MutationRegionTest {
	
	@Test
	public void testSingleEmptyRegion(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addEmptyRegion(8);
		MutationRegion mr = builder.build();
		
		Assert.assertEquals(new Integer(0), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(7), mr.getRegionEndIndex());
		Assert.assertEquals(8, mr.getRegionLength());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
		Assert.assertEquals("", mr.createCombinedSiteSubSequenceForMutation("AAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		Assert.assertEquals("", mr.reassembleSequence("AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "GGGGGGGG"));
		Assert.assertNull(mr.getNextRegion());
	}
	
	@Test
	public void testEmptyRegionLengthExtendsProperly(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(new MutationSite(0,7)).addEmptyRegion(8).addSites(new MutationSite(16,23));
		MutationRegion mr = builder.build();
		
		// Non-empty region
		Assert.assertNotNull(mr.getNextRegion());
		Assert.assertEquals(new Integer(0), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(7), mr.getRegionEndIndex());
		Assert.assertEquals(8, mr.getRegionLength());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
		Assert.assertEquals("AAAAAAAA", mr.createCombinedSiteSubSequenceForMutation("AAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		Assert.assertEquals("GGGGGGGGAAAAAAAAAAAAAAAAAAAA", mr.reassembleSequence("AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "GGGGGGGG"));
		
		// Empty region
		mr = mr.getNextRegion();
		Assert.assertNotNull(mr.getNextRegion());
		Assert.assertEquals(new Integer(8), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(15), mr.getRegionEndIndex());
		Assert.assertEquals(8, mr.getRegionLength());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
		Assert.assertEquals("", mr.createCombinedSiteSubSequenceForMutation("AAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		Assert.assertEquals("", mr.reassembleSequence("AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "GGGGGGGG"));
		
		// Non-empty region
		mr = mr.getNextRegion();
		Assert.assertNull(mr.getNextRegion());
		Assert.assertEquals(new Integer(16), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(23), mr.getRegionEndIndex());
		Assert.assertEquals(8, mr.getRegionLength());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
		Assert.assertEquals("AAAAAAAA", mr.createCombinedSiteSubSequenceForMutation("AAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		Assert.assertEquals("AAAAAAAAAAAAAAAAGGGGGGGGAAAA", mr.reassembleSequence("AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "GGGGGGGG"));
	}
	
	@Test
	public void testSingleSiteSingleRegion(){
		MutationSite site = new MutationSite(0,8);
		MutationRegionBuilder builder = new MutationRegionBuilder();
		MutationRegion mr = builder.addSites(site).build();
		Assert.assertEquals(new Integer(0), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(8), mr.getRegionEndIndex());
		Assert.assertEquals(9, mr.getRegionLength());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
		Assert.assertNull(mr.getNextRegion());
	}
	
	@Test
	public void testTwoSiteIndependentRegions(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		MutationSite ms = new MutationSite(8 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH + 1,16 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH + 1);		
		MutationRegion mr = builder.addSites(new MutationSite(0,8), ms).build();
		
		Assert.assertEquals(new Integer(0), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(8), mr.getRegionEndIndex());
		Assert.assertEquals(9, mr.getRegionLength());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
		Assert.assertNotNull(mr.getNextRegion());
		
		Assert.assertEquals(new Integer(8 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH + 1), mr.getNextRegion().getRegionStartIndex());
		Assert.assertEquals(new Integer(16 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH + 1), mr.getNextRegion().getRegionEndIndex());
		Assert.assertEquals(0, mr.getNextRegion().getOverlapRegions().size());
		Assert.assertNull(mr.getNextRegion().getNextRegion());
	}
	
	@Test
	public void testTwoSiteDependentRegionsNoOverlap(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(new MutationSite(0,8));
		builder.addSites(
			new MutationSite(
				8 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH,
				16 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH
			)
		);
		
		MutationRegion mr = builder.build();
		Assert.assertNull(mr.getNextRegion());
		Assert.assertEquals(new Integer(0), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(16 + MutationRegionBuilder.INDEPENDENT_REGION_SEPARATION_LENGTH), mr.getRegionEndIndex());
		Assert.assertEquals(0, mr.getOverlapRegions().size());
	}
	
	@Test
	public void testTwoSiteDependentRegionsWithOverlap(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(
				new MutationSite(0,8),
				new MutationSite(6,13)
			);
		MutationRegion mr = builder.build();
		
		Assert.assertEquals(new Integer(0), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(13), mr.getRegionEndIndex());
		Assert.assertEquals(1, mr.getOverlapRegions().size());
		OverlappingRegion overlap = mr.getOverlapRegions().get(0);
		Assert.assertEquals(6, overlap.overlapStart);
		Assert.assertEquals(8, overlap.overlapEnd);
		Assert.assertNull(mr.getNextRegion());
	}
	
	@Test
	public void testThreeSiteDependentRegionsWithDoubleOverlap(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(
				new MutationSite(10,17),
				new MutationSite(15,22),
				new MutationSite(20,27)
			);
		
		MutationRegion mr = builder.build();
		Assert.assertEquals(new Integer(10), mr.getRegionStartIndex());
		Assert.assertEquals(new Integer(27), mr.getRegionEndIndex());
		Assert.assertNull(mr.getNextRegion());
		
		Assert.assertEquals(2, mr.getOverlapRegions().size());
		OverlappingRegion overlap = mr.getOverlapRegions().get(0);
		Assert.assertEquals(15, overlap.overlapStart);
		Assert.assertEquals(17, overlap.overlapEnd);
		overlap = mr.getOverlapRegions().get(1);
		Assert.assertEquals(20, overlap.overlapStart);
		Assert.assertEquals(22, overlap.overlapEnd);
		
	}
	
	@Test
	public void testThreeSiteWithDoubleOverlapRegionSubSequenceConstructionAndReassembly(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(
				new MutationSite(10,17),
				new MutationSite(15,22),
				new MutationSite(20,27)
			);
		
		MutationRegion mr = builder.build();
		String wildSequence = "ACGTACGTACGTACGTACGTACGTACGT";
		String subSequence = mr.createCombinedSiteSubSequenceForMutation(wildSequence);
		Assert.assertEquals("GTACGTACGTACGTACGT", subSequence);
		Assert.assertEquals(wildSequence, mr.reassembleSequence(wildSequence, subSequence));
	}
	
	@Test
	public void testSingleSiteRegionSubSequenceConstructionAndReassembly(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(new MutationSite(10,17));
		
		MutationRegion mr = builder.build();
		String wildSequence = "ACGTACGTACGTACGTACGTACGTACGT";
		String subSequence = mr.createCombinedSiteSubSequenceForMutation(wildSequence);
		Assert.assertEquals("GTACGTAC", subSequence);
		Assert.assertEquals(wildSequence, mr.reassembleSequence(wildSequence, subSequence));
	}
	
	@Test
	public void testTwoSiteIndependentRegionSubSequenceConstructionAndReassembly(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(
				new MutationSite(10,17), 
				new MutationSite(36,43)
			);
		
		MutationRegion mr = builder.build();
		String wildSequence = "ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGT";
		String subSequence = mr.createCombinedSiteSubSequenceForMutation(wildSequence);
		Assert.assertEquals("GTACGTAC", subSequence);
		Assert.assertEquals(wildSequence, mr.reassembleSequence(wildSequence, subSequence));
		subSequence = mr.getNextRegion().createCombinedSiteSubSequenceForMutation(wildSequence);
		Assert.assertEquals("ACGTACGT", subSequence);
		Assert.assertEquals(wildSequence, mr.getNextRegion().reassembleSequence(wildSequence, subSequence));
	}
	
	@Test
	public void testTwoSiteDependentRegionSubSequenceConstructionAndReassembly(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(
				new MutationSite(10,17), 
				new MutationSite(20,27)
		);
		
		MutationRegion mr = builder.build();
		String wildSequence = "ACGTACGTACGTACGTACGTTCGTACGTACGTACGTACGTACGT";
		String subSequence = mr.createCombinedSiteSubSequenceForMutation(wildSequence);
		Assert.assertEquals("GTACGTACTCGTACGT", subSequence);
		Assert.assertEquals(wildSequence, mr.reassembleSequence(wildSequence, subSequence));
	}
	
	@Test
	public void testMutatedSubSequenceReassembly(){
		MutationRegionBuilder builder = new MutationRegionBuilder();
		builder.addSites(new MutationSite(10,17));
		
		MutationRegion mr = builder.build();
		String wildSequence = "ACGTACGTACGTACGTACGTACGTACGT";
		String mutatedSubSequence = "GTAAAAAC";
		String mutatedWildSequence = "ACGTACGTACGTAAAAACGTACGTACGT";
		Assert.assertEquals(mutatedWildSequence, mr.reassembleSequence(wildSequence, mutatedSubSequence));
	}
	
}
