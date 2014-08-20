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

package org.imirp.imirp;

import org.imirp.imirp.akka._AkkaModule;
import org.imirp.imirp.db._DbModule;
import org.imirp.imirp.services._ServicesModule;
import org.imirp.imirp.tsp._MirbaseModule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class _ImirpCoreModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new _AkkaModule());
		install(new _DbModule());
		install(new _ServicesModule());
		install(new _MirbaseModule());
	}
	
	@Provides
	Config provideConfig(){
		return ConfigFactory.load();
	}

}
