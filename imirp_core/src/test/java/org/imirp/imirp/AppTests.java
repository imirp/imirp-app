package org.imirp.imirp;

import org.imirp.imirp.App;
import org.imirp.imirp.data.SiteType;
import org.imirp.imirp.data.TargetSiteType;
import org.junit.Assert;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AppTests {    
    private static final String TEST_CONFIG = App.INVALID_TARGET_PREDICTIONS_PROPERTY + " = [\"8mer:false\",\"8mer:true\",\"7mer-A1:false\",\"7mer-A1:true\",\"6mer:false\"]";
    Config config = ConfigFactory.parseString(TEST_CONFIG);
    
    @Test
    public void testInvalidSitesLoadedCorrectly() {
        Assert.assertArrayEquals(new TargetSiteType[] { 
                new TargetSiteType(SiteType.EIGHT_MER, false),
                new TargetSiteType(SiteType.EIGHT_MER, true),
                new TargetSiteType(SiteType.SEVEN_MER_A1, false),
                new TargetSiteType(SiteType.SEVEN_MER_A1, true),
                new TargetSiteType(SiteType.SIX_MER, false),
            },
            App.loadInvalidSiteTypes(config));
    }
}
