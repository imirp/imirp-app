package org.imirp.imirp.tsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.Assert;

import org.imirp.imirp.data.SiteType;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.tsp.Mirbase;
import org.imirp.imirp.tsp.MirbaseScanner;
import org.imirp.imirp.tsp.Species;
import org.imirp.imirp.tsp.MirbaseScannerResultsUtil.LoadedTargetResults;
import org.junit.Test;

public class MirbaseScannerTest {
	@Test
	public void testSimpleNoMatches(){
		Mirbase mirbase = new Mirbase();
		mirbase.addMirset(">aaa aaaaa aaaaaaaaaaaa aaaaaaaaaaa aaaaa", "AAAAAAAAAAAAAAAAAAAAAAAAA");
		mirbase.addMirset(">aaa aaaaa aaaaaaaaaaaa aaaaaaaaaaa aaaaa", "AAAAAAAAAAAAAAAAAAAAAAAAA");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("aaa"), "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", true).predictions;
		Assert.assertEquals("It should be impossible to get any matches.", 0, predictedTargets.size());
	}
	
	/* ****************************************************************************
	 * 6MER
	 ***************************************************************************** */
	
	@Test
	public void testSimple_SixMerNoGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "ATACCTCC", false).predictions;
		Assert.assertEquals("We should only get one 6MER match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 6MER match.", SiteType.SIX_MER, pt.type.siteType);
		Assert.assertFalse(pt.type.guWobble);
	}
	
	@Test
	public void testSimple_SixMerWithGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "ATACCTCC", true).predictions;
		Assert.assertEquals("We should only get one 6MER match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 6MER match.", SiteType.SIX_MER, pt.type.siteType);
		Assert.assertFalse(pt.type.guWobble);
	}
	
	/* ****************************************************************************
	 * 7MER-M8
	 ***************************************************************************** */
	
	@Test
	public void testSimple_SevenMerM8NoGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "CTACCTCC", false).predictions;
		Assert.assertEquals("We should only get one 7MER-M8 match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 7MER-M8 match.", SiteType.SEVEN_MER_M8, pt.type.siteType);
		Assert.assertFalse(pt.type.guWobble);
	}
	
	@Test
	public void testSimple_SevenMerM8WithGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "CTACCTCC", true).predictions;
		Assert.assertEquals("We should only get one 7MER-M8 match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 7MER-M8 match.", SiteType.SEVEN_MER_M8, pt.type.siteType);
		Assert.assertFalse(pt.type.guWobble);
	}
	
	/* ****************************************************************************
	 * 7MER-M8
	 ***************************************************************************** */
	
	@Test
	public void testSimple_SevenMerA1NoGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "ATACCTCA", false).predictions;
		Assert.assertEquals("We should only get one 7MER-A1 match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 7MER-A1 match.", SiteType.SEVEN_MER_A1, pt.type.siteType);
		Assert.assertFalse("Should not get GU Wobble", pt.type.guWobble);
	}
	
	@Test
	public void testSimple_SevenMerA1WithGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "ATACCTCA", true).predictions;
		Assert.assertEquals("We should only get one 7MER-A1 match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 7MER-A1 match.", SiteType.SEVEN_MER_A1, pt.type.siteType);
		Assert.assertFalse("Should not get GU Wobble", pt.type.guWobble);
	}
	
	/* ****************************************************************************
	 * 8MER
	 ***************************************************************************** */
	
	@Test
	public void testSimple_EightMerNoGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "CTACCTCA", false).predictions;
		Assert.assertEquals("We should only get one 8MER match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 8MER match.", SiteType.EIGHT_MER, pt.type.siteType);
		Assert.assertFalse("Should not get GU Wobble", pt.type.guWobble);
	}
	
	@Test
	public void testSimple_EightMerWithGU(){
		Mirbase mirbase = new Mirbase();
		String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
		mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
		MirbaseScanner scanner = new MirbaseScanner();
		List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "CTACCTCA", true).predictions;
		Assert.assertEquals("We should only get one 8MER match.", 1, predictedTargets.size());
		TargetPredictionResult pt = predictedTargets.get(0);
		Assert.assertEquals("We should only get one 8MER match.", SiteType.EIGHT_MER, pt.type.siteType);
		Assert.assertFalse("Should not get GU Wobble", pt.type.guWobble);
	}
	
	/* ****************************************************************************
     * OS-6MER
     ***************************************************************************** */
	@Test
    public void testSimple_OSSixMer(){
        Mirbase mirbase = new Mirbase();
        String mirname = ">cel-let-7-5p MIMAT00000001 Caenorhabditis elegans let-7-5p";
        mirbase.addMirset(mirname, "UGAGGUAGUAGGUUGUAUAGUU");
        MirbaseScanner scanner = new MirbaseScanner();
        List<TargetPredictionResult> predictedTargets = scanner.findPredictedTargets(mirbase, new Species("cel"), "CTACCTAA", false).predictions;
        Assert.assertEquals("We should only get one OS-6MER match.", 1, predictedTargets.size());
        TargetPredictionResult pt = predictedTargets.get(0);
        Assert.assertEquals("We should only get one OS-6MER match.", SiteType.OFFSET_SIX_MER, pt.type.siteType);
        Assert.assertFalse("Should not get GU Wobble", pt.type.guWobble);
    }
	
	private static final String PAX6_WILDTYPE_SEQUENCE_TARGETS_FILENAME = "pax6_wildtype_sequence_targets";
	private static final String MIRNA_DATABASE_FILENAME = "/com/torbinsky/imirp/testmature.fa";
	
	@Test
	public void testFullTargetResults() throws NumberFormatException, IOException{
	    LoadedTargetResults loadedResults = MirbaseScannerResultsUtil.loadResults(getClass().getResourceAsStream(PAX6_WILDTYPE_SEQUENCE_TARGETS_FILENAME));
	    List<TargetPredictionResult> loadedTargets = loadedResults.scanResult.predictions;
		// Predict targets using the same params
		Mirbase mirbase = new Mirbase();
		mirbase.load(getClass().getResourceAsStream(MIRNA_DATABASE_FILENAME));
		
		List<TargetPredictionResult> predictedTargets = new MirbaseScanner().findPredictedTargets(mirbase, loadedResults.species, loadedResults.sequence, loadedResults.allowGUWobble).predictions;
		// First let's make sure we have the same numbers of predicted target sites
		Assert.assertEquals(predictedTargets.size(), loadedTargets.size());
		// Scan each predicted target and make sure it exists in the list of loaded targets
		for(TargetPredictionResult tpr : predictedTargets){
			Assert.assertTrue("Could not find predicted target site[" + tpr + "]", MirbaseScannerResultsUtil.checkTargetSiteExists(tpr, loadedTargets));
		}		
	}
	
	public static void transformFormats() throws IOException{
		String fileToTransform = null; // Set file path here
		Mirbase mirbase = new Mirbase();
		mirbase.load(MirbaseScannerTest.class.getResourceAsStream(MIRNA_DATABASE_FILENAME));		
		try(BufferedReader br = new BufferedReader(
				new InputStreamReader(
						MirbaseScannerTest.class.getResourceAsStream(fileToTransform)
				)
			)
		){
			String description, mirna;
			while ((description = br.readLine()) != null && (mirna = br.readLine()) != null) {
				System.out.print(description);
				System.out.print(" ");
				Species s = mirbase.getSpeciesById(description.substring(1, 4));
				System.out.print(s.getGenus());
				System.out.print(" ");
				System.out.println(s.getSpecies());
				System.out.println(mirna);
			}
		}
	}
	
}
