/**
*   Copyright 2014 Torben Werner, Bridget Ryan
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package org.imirp.imirp.db;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.jongo.Jongo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.typesafe.config.Config;

public class _DbModule extends AbstractModule {
	private static final Logger logger = Logger.getLogger(_DbModule.class);
	
	@Override
	protected void configure() {
		bind(ImirpDataStore.class).to(MongoDataStoreImpl.class);
	}

	@Provides @Singleton
	MongoClientURI provideMongoURI(Config config) throws UnknownHostException{	
        MongoClientURI uri = new MongoClientURI(config.getString("imirp.db.uri"));
		return uri;
	}
	
	@Provides @Singleton
	MongoClient provideMongo(MongoClientURI uri) throws UnknownHostException {	
        MongoClient mongo = new MongoClient(uri);
		return mongo;
	}
	
	@Provides @Singleton
	Jongo provideJongo(MongoClient mongo, MongoClientURI uri){
		String dbName = uri.getDatabase();
		logger.debug("Using database[" + dbName + "]");
		return new Jongo(mongo.getDB(dbName));
	}
}