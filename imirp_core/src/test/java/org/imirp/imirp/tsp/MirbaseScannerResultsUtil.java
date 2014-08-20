package org.imirp.imirp.tsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.imirp.imirp.data.SiteType;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.data.TargetSiteType;
import org.imirp.imirp.tsp.Species;
import org.imirp.imirp.tsp.MirbaseScanner.ScanResult;

public class MirbaseScannerResultsUtil {

    public static LoadedTargetResults loadResults(InputStream resultStream) throws IOException {
        // Load sequence, scan paramaters and valid targets from file
        String sequence = null;
        String[] scanParams = null;
        ScanResult scanResult = new ScanResult();
        String line = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resultStream))) {
            while ((line = br.readLine()) != null) {
                if (sequence == null) {
                    // The sequence that was used for the loaded target predictions
                    sequence = line;
                } else if (scanParams == null) {
                    // The parameters that were used to predict the targets
                    scanParams = line.split(",");
                } else {
                    // Targets
                    String[] lineParts = line.split("\t");
                    int position = Integer.parseInt(lineParts[0]);
                    String microRNA = lineParts[1];
                    SiteType siteType = SiteType.fromName(lineParts[2]);
                    boolean guWobbleFound = lineParts[3].equals("yes") ? true : false;
                    scanResult.addResult(new TargetPredictionResult(microRNA, position, new TargetSiteType(siteType, guWobbleFound)));
                }
            }
        }
        Species species = new Species(scanParams[0]);
        boolean allowGUWobble = Boolean.parseBoolean(scanParams[1]);
        return new LoadedTargetResults(species, sequence, allowGUWobble, scanResult);
    }

    public static boolean checkTargetSiteExists(TargetPredictionResult tpr, List<TargetPredictionResult> results) {
        for (TargetPredictionResult result : results) {
            if (result.equals(tpr)) {
                return true;
            }
        }
        return false;
    }

    public static class LoadedTargetResults {
        public final Species species;
        public final String sequence;
        public final boolean allowGUWobble;
        public final ScanResult scanResult;

        public LoadedTargetResults(Species species, String sequence, boolean allowGUWobble, ScanResult scanResult) {
            super();
            this.species = species;
            this.sequence = sequence;
            this.allowGUWobble = allowGUWobble;
            this.scanResult = scanResult;
        }

        public Species getSpecies() {
            return species;
        }

        public String getSequence() {
            return sequence;
        }

        public boolean isAllowGUWobble() {
            return allowGUWobble;
        }

        public ScanResult getScanResult() {
            return scanResult;
        }

    }
}
