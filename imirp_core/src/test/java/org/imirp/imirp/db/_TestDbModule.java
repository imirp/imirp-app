package org.imirp.imirp.db;

import org.imirp.imirp.db._DbModule;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class _TestDbModule extends _DbModule {

	public static final String TEST_DB_NAME = "imirp_test_only";
	
	@Override
	protected void configure() {
		Config config = ConfigFactory.parseString("imirp {\ndb {\nport=27017\nhost=\"127.0.0.1\"\nname=" + TEST_DB_NAME + "\n}\n}");
		bind(Config.class).toInstance(config);
		super.configure();
	}
	
}
