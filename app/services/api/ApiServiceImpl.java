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

package services.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.imirp.imirp.data.TargetPredictionResult;
import org.imirp.imirp.db.ImirpDataStore;
import org.imirp.imirp.db.model.ImirpProjectMutationRequest;
import org.imirp.imirp.db.model.MutationResultSet;
import org.imirp.imirp.db.model.Project;
import org.imirp.imirp.mutation.region.MultiSiteMutation;
import org.imirp.imirp.mutation.region.MutationRegion;
import org.imirp.imirp.tsp.Mirbase;
import org.imirp.imirp.tsp.MirbaseScanner;
import org.imirp.imirp.tsp.Species;
import org.imirp.imirp.tsp.MirbaseScanner.ScanResult;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import dto.ProjectDto;
import dto.ProjectSummaryDto;

public class ApiServiceImpl implements ApiService {
	private ImirpDataStore datastore;
	private Mirbase mirbase;
	private MirbaseScanner mirScanner;

	@Inject
	public ApiServiceImpl(ImirpDataStore datstore, Mirbase mirbase, MirbaseScanner mirScanner) {
		this.datastore = datstore;
		this.mirbase = mirbase;
		this.mirScanner = mirScanner;
	}

	@Override
	public ImirpResultsDataDto getProjectResults(ObjectId projectId,
			Set<String> regionIds, int limitPerRegion) {
		ImirpResultsDataDto data = new ImirpResultsDataDto();
		data.project = getProject(projectId);
		data.activeRequests = getRequestsForProject(projectId, false);
		data.completedRequests = getRequestsForProject(projectId, true);
		
		for (MutationRegion region : getProjectMutationRegions(projectId)) {
			RegionDetailsDto regionDetails = new RegionDetailsDto();
			regionDetails.sites = region.getMutationSites();
			regionDetails.regionId = region.getRegionId();
			regionDetails.regionStart = region.getRegionStartIndex();
			regionDetails.regionEnd = region.getRegionEndIndex();
			data.addRegion(regionDetails);
			data.addRegionMutants(
					region.getRegionId(),
					getMutantsForRegion(projectId, region.getRegionId(),
							limitPerRegion));
		}

		return data;
	}

	@Override
	public RegionMutantsDto getMutantsForRegion(ObjectId projectId,
			String regionId, int limit) {
		RegionMutantsDto data = new RegionMutantsDto();
		Iterable<MutationResultSet> storeResults = datastore.getResultSets(
				projectId, regionId, limit);
		for (MutationResultSet mrs : storeResults) {
			data.add(new MutantDto(mrs.getSequence(), mrs.getId().toString()));
		}

		return data;
	}

	@Override
	public ProjectDto saveNewProject(ProjectDto newProject) {
		final Project newProjectModel = newProject.toModel();
		final Project savedProject = datastore.saveNewProject(newProjectModel);
		return ProjectDto.fromModel(savedProject);
	}

	@Override
	public ProjectsPageDto getProjectsByEmail(String email) {
		Iterable<Project> projects = datastore.getProjectsByEmail(email);
		ArrayList<ProjectDto> projectDtos = new ArrayList<>();
		for (Project project : projects) {
			projectDtos.add(ProjectDto.fromModel(project));
		}
		
		ProjectsPageDto dto = new ProjectsPageDto(projectDtos);
		return dto;
	}

	@Override
	public Set<Species> getAvailableSpecies() {
		return mirbase.getSpecies();
	}

	@Override
	public ProjectDto getProject(ObjectId projectId) {
		Project proj = datastore.getProject(projectId);
		return ProjectDto.fromModel(proj);
	}

	@Override
	public long getProjectCount() {
		return datastore.countProjects();
	}

	@Override
	public Iterable<MutationRegion> getProjectMutationRegions(ObjectId projectId) {
		ProjectDto project = getProject(projectId);
		MultiSiteMutation msm = new MultiSiteMutation(project.sequence,
				project.mutationSites, null);
		return msm.getRegions();
	}

	@Override
	public Set<String> getProjectRegionIds(ObjectId projectId) {
		Set<String> regionIds = new HashSet<>();
		for (MutationRegion region : getProjectMutationRegions(projectId)) {
			regionIds.add(region.getRegionId());
		}
		return regionIds;
	}

	@Override
	public ProjectSummaryDto getProjectSummary(ObjectId projectOId) {
		ProjectSummaryDto summary = new ProjectSummaryDto();
		summary.project = getProject(projectOId);
		Iterable<ImirpProjectMutationRequest> requests = datastore
				.getMutationRequestsForProject(projectOId);
		summary.requests = new ArrayList<>();
		for (ImirpProjectMutationRequest request : requests) {
			summary.requests.add(request);
		}
		return summary;
	}
	
	@Override
	public List<ImirpProjectMutationRequest> getRequestsForProject(ObjectId projectId, @Nullable Boolean completed) {
		return datastore.getMutationRequestsForProject(projectId, completed);
	}
	
	@Override
	public List<ImirpProjectMutationRequest> getActiveRequestsForProject(ObjectId projectId) {
		return getRequestsForProject(projectId, false);
	}

	@Override
	public File createMutantAnalysisZip(ObjectId projectOid, String mutantSequence) throws IOException {				
		Project project = datastore.getProject(projectOid);
		
		File zipFile = File.createTempFile(project.name + "imirp_analysis", ".zip");
		try(ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile), Charset.forName("UTF-8"))){
			// Generate the analysis files
			generateAnalysisFiles(project, mutantSequence, zipStream);
		}		
		
		return zipFile;
	}
	
	@Override
	public File createTargetScanCSV(String sequence, Species species) throws IOException {
		File csvFile = File.createTempFile("results", ".csv");
		ScanResult scanResults = mirScanner.findPredictedTargets(mirbase, species, sequence, true);
		try(PrintWriter writer = new PrintWriter(new FileWriter(csvFile))){
			writeResultsToCsv(scanResults, writer);
		}
		return csvFile;
	}

	private void generateAnalysisFiles(Project project, String mutantSequence, ZipOutputStream zipStream) throws IOException {
		// Project details file
		try(PrintWriter zipWriter = new PrintWriter(zipStream)){
			zipStream.putNextEntry(new ZipEntry(project.name + "_project_info.txt"));
			writeProjectDetails(project, mutantSequence, zipWriter);
			zipWriter.flush();
			zipStream.closeEntry();
			
			// Wild results file
			ScanResult wildResults = mirScanner.findPredictedTargets(mirbase, project.species, project.wildSequence, true);
			zipStream.putNextEntry(new ZipEntry(project.name + "_wildtype_results.csv"));
			writeResultsToCsv(wildResults, zipWriter);
			zipWriter.flush();
			zipStream.closeEntry();
			
			// Mutant results file
			ScanResult mutantResults = mirScanner.findPredictedTargets(mirbase, project.species, mutantSequence, true);
			zipStream.putNextEntry(new ZipEntry(project.name + "_mutant_results.csv"));
			writeResultsToCsv(mutantResults, zipWriter);
			zipWriter.flush();
			zipStream.closeEntry();
			
			// NEW results file (those not present in the wild results)
			ScanResult newResults = MirbaseScanner.removeResults(mutantResults, wildResults);
			zipStream.putNextEntry(new ZipEntry(project.name + "_new_results.csv"));
			writeResultsToCsv(newResults, zipWriter);
			zipWriter.flush();
			zipStream.closeEntry();
		}
	}

	/**
	 * Writes the project details to a .txt file
	 */
	private static void writeProjectDetails(Project project, String mutantSequence, PrintWriter zipWriter) throws IOException {
		zipWriter.println("Mutant Analysis Generated on: " + ISODateTimeFormat.dateTime().print(new DateTime()));
		zipWriter.println("Project ID: " + project._id.toString());
		zipWriter.println("Project Name: " + project.getName());
		zipWriter.println("Project Description: " + project.getDescription());
		zipWriter.println("Species: " + project.getSpecies().getDisplayName());			
		zipWriter.println("Wildtype Sequence: " + project.getWildSequence());
		zipWriter.println("Mutant Sequence: " + mutantSequence);
	}

	/**
	 * Writes a ScanResult object to a .csv file
	 */
	public static void writeResultsToCsv(ScanResult results, PrintWriter writer) throws IOException {
		writer.println("position,mirna,type,gu");
		for(TargetPredictionResult tpr : results.predictions){
			writer.print(tpr.position + 1); // Humans expect indexing from position 1 :P
			writer.print(",");
			writer.print(tpr.microRNA);
			writer.print(",");
			writer.print(tpr.type.siteType.name);
			writer.print(",");
			writer.print(tpr.type.guWobble ? "yes" : "no");
			writer.println();
		}
	}
}
