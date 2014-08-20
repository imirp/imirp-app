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

'use strict';

/* Services */

imirpApp.factory('SequenceSvc', [function() {

	var factory = {};

	factory.combineSequence = function(sequence, mutationInfo) {
		var combinedSequence = "";
		var seqPos = 0;
		mutationInfo.sort(function(a,b){
			if(a.regionStart < b.regionStart){
				return -1;
			}
			if(a.regionStart > b.regionStart){
				return 1;
			}
			return 0;
		});
		for(var i = 0; i < mutationInfo.length; i++){
			var mi = mutationInfo[i];
			combinedSequence += sequence.substring(seqPos, mi.regionStart);
			combinedSequence += mi.sequence.substring(mi.regionStart, mi.regionEnd + 1);
			seqPos = mi.regionEnd + 1;
		}
		combinedSequence += sequence.substring(seqPos, sequence.length);
		return combinedSequence;
	};

	return factory;
}]);
