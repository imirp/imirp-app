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

package org.imirp.imirp.tsp;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;

public class _MirbaseModule extends AbstractModule {

	@Override
	protected void configure() {
		
	}
	
	@Provides @Singleton
	public Mirbase provideMirbase(Config config) throws IOException {
	    Mirbase mirbase = null;
		if(config.hasPath("mirbasePath")){
		    mirbase = new Mirbase();
		    mirbase.load(config.getString("mirbasePath"));
		}else{
		    mirbase = loadDefaultMirbase();
		}
		return mirbase;
	}

    public static Mirbase loadDefaultMirbase() throws IOException {
        Mirbase mirbase = new Mirbase();
        try(InputStream is = _MirbaseModule.class.getResourceAsStream("mature.fa")){
        	mirbase.load(is);
        }
        return mirbase;
    }

}
