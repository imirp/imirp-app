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

/* App Module */

var imirpApp = angular.module('imirpApp', ['ngSanitize', 'ui.bootstrap']);

imirpApp.controller('ToolsCtrl', [function() {
	
}]).directive('imirpProjectsList', function() {
	return {
        restrict: 'E',
        templateUrl: '/assets/html/partials/projects.html',
        transclude: true,
        scope: {
        	pages: '=',
        	pageNum: '=',
        	selectProject: '&onProjectSelect',
        	changePage: '&onPageChange'
        },
        link: function(scope, elem, attrs){
        	scope.prevPage = function(){
        		scope.goPage(Math.max(0, scope.pageNum - 1));
        	};
        	scope.nextPage = function(){
        		scope.goPage(Math.min(scope.pages - 1, scope.pageNum + 1));
        	};
        	scope.goPage = function(num){
        		scope.changePage({
        			page: num
    			});
        	};
        	scope.getPages = function(){
        		return new Array(scope.pages);
        	};
        },
        controller: function($scope) {
        	this.selectProject = function(projectId){
        		$scope.selectProject({
        			"projectId": projectId
        		});
        	};
        }
      };
}).directive('imirpProjectItem', function() {
	return {
        restrict: 'E',
        require: '^imirpProjectsList',
        templateUrl: '/assets/html/partials/project-item.html',
        scope: {
        	project: '='
        },
        link: function (scope, elem, attrs, projectsCtrl) {
        	scope.dateCreated = Date.parse(scope.project.dateCreated);
        	
        	scope.selectProject = function(projectId){
        		projectsCtrl.selectProject(projectId);
        	};
        }
	};
}).directive('imirpResultPnl', function() {
    return {
        restrict: 'E',
        template: '<div ng-transclude></div>',
        transclude: true,
        scope: {
        	sequence: '=',
        	regionSelections: '=',
        },
        controller: function($scope) {
        	this.sequence = $scope.sequence;
        	this.selectedMutants = $scope.regionSelections;
        	this.hiddenRegions = {};
        	this.toggleRegion = function(regionId){
        		this.hiddenRegions[regionId] = this.hiddenRegions[regionId] === true ? false: true;
        	};
        	this.isRegionSelected = function(regionId){
        		return typeof(this.selectedMutants[regionId]) !== 'undefined';
        	};
        }
      };
}).directive('imirpRegionSelect', function() {
    return {
        restrict: 'E',
        require: '^imirpResultPnl',
        templateUrl: '/assets/html/partials/result-detail.html',
        scope: {
        	selected: '=',
        	region: '=',
        	mutants: '='
        },
        link: function (scope, elem, attrs, resultsCtrl) {
        	scope.selectedMutants = resultsCtrl.selectedMutants;
        	scope.isHidden = function(){
        		return resultsCtrl.hiddenRegions[scope.region.regionId];
        	}
        	scope.sequence = resultsCtrl.sequence;
        	
        	scope.getRegionTitle = function(){
        		return ((scope.region.regionStart + 1) + " to " + (scope.region.regionEnd + 1));
        	};
        	
        	scope.toggleMutants = function(){
        		resultsCtrl.toggleRegion(scope.region.regionId);
        	}
        	
        	// Basically gets a 'snippet' of the sequence that is defined by the bounds of a region with delimitters indicating where the sites begin/end
        	scope.getHighlightedSequenceSnippetHtml = function(sequence, openDelim, closeDelim){
        		openDelim = typeof openDelim !== 'undefined' ? openDelim : "[";
        		closeDelim = typeof closeDelim !== 'undefined' ? closeDelim : "]";
        		var sequenceSnippetHtml = "";
        		// To build our sequence we will first separate the site positions into two lists of the open and close boundaries
        		var openBrackets = [];
        		var closeBrackets = [];
        		var sites = scope.region.sites;
        		for(var i = 0; i < sites.length; i++){
        			var site = sites[i];
        			openBrackets.push(site[0]);
        			closeBrackets.push(site[1]);
        		}
        		openBrackets.sort(function(a, b){return a-b});
        		closeBrackets.sort(function(a, b){return a-b});
        		
        		// Now build the sequence HTML display string
        		for(var i = openBrackets[0]; i <= closeBrackets[closeBrackets.length - 1];){
        			// If we have any brackets to place, we need to find the position of the next closest one
        			if(openBrackets.length > 0 || closeBrackets.length > 0){
        				var sequenceChunkEnd = 0;
        				var bracket;
        				// If we have both open and close brackets left, then find which one is closer
        				if(openBrackets.length > 0 && closeBrackets.length > 0){
        					// Find which is closer
        					if(openBrackets[0] < closeBrackets[0]){
        						sequenceChunkEnd = openBrackets.shift();
        						bracket = openDelim;
        					}else{
        						sequenceChunkEnd = closeBrackets.shift() + 1; // the closing ends are inclusive positions
        						bracket = closeDelim;
        					}
        				}else{
        					// Assume only closed left (we should never only have an open bracket left!)
        					sequenceChunkEnd = closeBrackets.shift() + 1; // the closing ends are inclusive positions
        					bracket = closeDelim;
        				}
        				// Now append the sequence chunk remaining up to this bracket
        				sequenceSnippetHtml += sequence.substring(i, sequenceChunkEnd);
        				i = sequenceChunkEnd;
        				// And append the bracket
        				sequenceSnippetHtml += bracket;
        			}
        		}
        		
        		return sequenceSnippetHtml;
        	};
        }
      };
}).directive('imirpSequenceDisplay', function(){	
	return {
        restrict: 'E',
        templateUrl: '/assets/html/partials/sequence-display.html',
        scope: {
        	sequence: '@',
        	highlights: '=',
        	focus: '='
        },
        link: function (scope, elem, attrs) {
        	// Generates HTML that splits a sequence into chunks of 50 length pieces
        	scope.generateSeqFiftyHTML = function(){
				var sequenceFiftyChunks = [];
				var chunk;
				for(var i = 0; i < scope.sequence.length; i += 50){
					// Simply use substring to create a chunk of maximum length 50
					chunk = scope.sequence.substring(i, Math.min(scope.sequence.length, i+50));
					sequenceFiftyChunks.push(scope.highlightChunk(chunk, i)); // Add highlighting to the chunk
				}
				return sequenceFiftyChunks;
			};
			
			scope.getChunkEndPosition = function(chunkNum){
				return Math.min(chunkNum * 50 + 50, scope.sequence.length);
			};
			
			// Basically this function "highlights" a portion (aka "chunk") of the sequence.
			// The main trick to understand here is that the highlight positions are NOT relative to the chunk being highlighted
			// The highlight positions are the index within the full sequence string that the highlight should begin at
			scope.highlightChunk = function(chunk, chunkStartIdx){
				// Don't bother doing anything if we have no highlights, just return the original (unhighlighted) chunk
				if((!scope.highlights || scope.highlights.length == 0) && !scope.focus){
					return chunk;
				}
				
				// Step 1: Pre-processing - build a list of tags and the indexes which they should be inserted into the chunk
				// A list of objects representing the highlight tags as sort've a pre-processing step before we modify the actual chunk string with the tags
				var highlightTags = [];
				var highlight;
				var chunkEndIdx = chunk.length + chunkStartIdx; // Calculate where this chunk ends in the original, full sequence
				var isTagInChunk = function(tag){
					return (tag[0] >= chunkStartIdx && tag[0] < chunkEndIdx) || (tag[1] > chunkStartIdx && tag[1] <= chunkEndIdx);
				};
				var getOpenIdx = function(tagPos){
					return Math.max(chunkStartIdx - 1, tagPos - 1);
				};
				var getCloseIdx = function(tagPos){
					return Math.min(chunkEndIdx, tagPos);
				};
				// Iterate through ALL highlights for the entire sequence and build the list of tags that apply to this chunk
				for(var i = 0; i < scope.highlights.length; i++){
					highlight = scope.highlights[i];
					// Check if this highlight at least partially covers some of the chunk, otherwise we ignore this highlight
					if(isTagInChunk(highlight)){
						// Push the opening highlight tag
						var openIdx = getOpenIdx(highlight[0]);
						highlightTags.push([openIdx, '<span class="highlight">']);
						// Push the closing highlight tag
						var closeIdx = getCloseIdx(highlight[1]);
						highlightTags.push([closeIdx, '</span>']);
					}
				}
				// Add the "focus" highlight tag
				if(scope.focus && isTagInChunk(scope.focus)){
					highlightTags.push([getOpenIdx(scope.focus[0]), '<span class="focus">']);
					highlightTags.push([getCloseIdx(scope.focus[1]), '</span>']);
				}
				
				// Bail out early if we have no highlight tags for this chunk
				if(highlightTags.length == 0){
					return chunk;
				}
				
				// Sort in ascending order so we can insert into string from start to end. This just makes our string manipulation process a little easier.
				highlightTags.sort(function(a,b){
					// Sort ASCENDING according to the tag position (array position '0') so that our tags appear in the order that we build the string
	        		return a[0] - b[0];
        		});
        		
				// Step 2: Iterate over the list of highlight tag 
        		// Build a highlighted chunk string by appending substrings of the chunk and our tags at appropriate indexes 
        		var highlightedChunk = "";
        		var chunkPos = 0; // The position in the chunk that we have processed up to
        		var insertPos; // The position that we are inserting the tag at
        		var tag; // The current tag
        		// Splice together the pieces of the chunk and the highlight tags at the appropriate positions
        		while(highlightTags.length > 0){
        			tag = highlightTags.shift(); // get next tag
        			insertPos = tag[0] - chunkStartIdx + 1; // figure out the relative index of our tag WITHIN the chunk
        			// Check if there is a piece of the chunk that we need to append before adding in the next highlight tag
        			if(insertPos != chunkPos){
        				// The insert position is not the same as the chunk position which means that we need to append more of the
        				// chunk string before we add in this tag
        				highlightedChunk = highlightedChunk.concat(chunk.substring(chunkPos, insertPos));
        				chunkPos = insertPos;
        			}
        			// Append the highlight tag to our "highlighted" chunk string
        			highlightedChunk = highlightedChunk.concat(tag[1]);
        		}
        		// Append any remaining chunk string that may have been missed if we didn't have a tag at the very end
        		highlightedChunk = highlightedChunk.concat(chunk.substring(chunkPos, chunkEndIdx));
				
				return highlightedChunk; // le' highlighted!
			};
        }
	};	
}).directive('imirpSiteSelector', function(){
	return {
        restrict: 'E',
        templateUrl: '/assets/html/partials/siteselector.html',
        scope: {
        	siteLength: '@',
        	sequenceLength: '@',
        	sitesUpdated: '&onSiteUpdate',
        	siteFocused: '&onSiteFocus'
        },
        link: function (scope, elem, attrs) {
        	scope.sites = [];
        	scope.inputSite = "";
        	
        	scope.validateSites = function(){
        		var site;
        		
        		// Check if any sites have become invalidated and remove them if they have
        		for(var i = 0; i < scope.sites.length; i++){
        			site = scope.sites[i];
        			if(site[1] >= scope.sequenceLength){
        				scope.sites.splice(scope.sites.indexOf(site),1);
        			}
        		}
        	};
        	
        	scope.$watch('sequenceLength', function(){
        		scope.validateSites();
        	});
        	
        	scope.$watchCollection('sites', function(newValue){
        		scope.sitesUpdated({
        			"sites": newValue
        		});
        	});
        	
        	scope.hoverSite = function(site){
        		scope.siteFocused({
        			"site": site
        		});
        	};
        	
        	// Add the current "input site" to the list of sites
        	scope.addSite = function(){
        		if(scope.inputSite.length == 0){
        			return;
        		}
        		var inputSiteNum = parseInt(scope.inputSite);
        		var siteStart = inputSiteNum - 1;
        		var siteEnd = siteStart + parseInt(scope.siteLength) - 1;
        		scope.sites.push([siteStart, siteEnd]);
        		scope.inputSite = "";
        	};
        	
        	// Remove the specified site from the list of sites
        	scope.removeSite = function(site){
        		var index = scope.sites.indexOf(site);
        		if(index > -1){
        			scope.sites.splice(index, 1);
        		}
        	};
        	
        	scope.handleKeypress = function($event){
        		var keyCode = $event.keyCode;
        		
        		// Handle 'enter' key
        		if(keyCode == 13){
        			scope.addSite();
        			return;
        		}
        		
        		// Validate that the input site produced from the next key character being entered would result in a valid site index
        		
        		// Filter out any non-numeric input
        		if (keyCode > 31 && (keyCode < 48 || keyCode > 57)){
        			$event.preventDefault();
        		}
        		
        		// Ensure that the number is within the valid bounds
        		var num = parseInt(scope.inputSite + '' + String.fromCharCode(keyCode));
        		if(num == 0 || num > (scope.sequenceLength - scope.siteLength + 1)){
        			$event.preventDefault();
				}
        	};
        }
	};
}).directive('imirpSpeciesSelect', function(){
	return {
		restrict: 'E',
		templateUrl: '/assets/html/partials/speciesselect.html',
		scope: {
			species: '=',
			selectSpecies: '&onSpeciesSelect'
		},
		link: function($scope, elem, attrs){
			$scope.selectedSpecies = undefined;
			$scope.$watch('selectedSpecies', function(){
				$scope.selectSpecies({					
					species: $scope.selectedSpecies
				});
			});
		}
	};
}).directive('formatSequence', function() {
   return {
     require: 'ngModel',
     link: function(scope, element, attrs, modelCtrl) {
        var formatSequence = function(inputValue) {
			if(!inputValue){
				return;
			}
			var newValue = inputValue.replace(/\s+/g, '').toUpperCase();
			if(newValue !== inputValue) {
				modelCtrl.$setViewValue(newValue);
				modelCtrl.$render();
			}
			return newValue;
		};
		modelCtrl.$parsers.push(formatSequence);
		formatSequence(scope[attrs.ngModel]);
     }
   };
}).directive('imirpMutationInstructions', function(){
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: '/assets/html/partials/mutationinstructions.html',
		scope: {
			caption: '='
		},
		link: function(scope, element, attrs){
			scope.showInstructions = false;
			
			scope.toggle = function(){
				scope.showInstructions = !scope.showInstructions;
			};
		}
	};
}).directive('imirpBreadcrumbs', function(){
	return {
		restrict: 'E',
		templateUrl: '/assets/html/partials/breadcrumbs.html',
		scope: true,
		link: function(scope, element, attrs){
			if(!scope.breadcrumbs){
				scope.breadcrumbs = [];
			}
			scope.breadcrumbs.unshift({name: 'Projects', href: appRoutes.controllers.ViewController.viewProjects().url});
		}
	};
}).directive('imirpProjectParametersDisplay', function(){
	return {
		restrict: 'E',
		templateUrl: '/assets/html/partials/projectparameters.html',
		scope: {
			project: '=',
			noSequence: '@'
		},
		link: function(scope, element, attrs){
			
		}
	};
}).directive('imirpMutationRequest', function(){
	return {
		restrict: 'E',
		templateUrl: '/assets/html/partials/mutationrequest.html',
		link: function(scope, element, attrs){
			scope.displaySiteType = function(st){
				var displayTxt = st.siteType;
				if(st.guWobble){
					displayTxt += ' + GU';
				}
				return displayTxt;
			};
		}
	};
});
