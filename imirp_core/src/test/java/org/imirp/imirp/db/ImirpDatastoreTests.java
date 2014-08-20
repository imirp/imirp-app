package org.imirp.imirp.db;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ImirpDatastoreTests {
	static Injector INJECTOR = Guice.createInjector(new _TestDbModule());
	
	@Test
	public void testDatastore(){
		
	}
}
