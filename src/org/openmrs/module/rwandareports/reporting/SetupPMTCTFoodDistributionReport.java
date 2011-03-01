package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;

public class SetupPMTCTFoodDistributionReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupPMTCTFoodDistributionReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
//		delete();
//		
//		setUpGlobalProperties();
//		
//		createCohortDefinitions();
//		ReportDefinition rd = createReportDefinition();
//		h.createRowPerPatientXlsOverview(rd, "PMTCTFoodDistribution.xls", "PMTCTFoodDistribution.xls_", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTFoodDistribution.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Food Package Distribution");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "Food Package Distribution Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "pmtct: Combined Clinic In Program");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Food Package Distribution");
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("pmtct: Combined Clinic In Program"), new HashMap<String,Object>());
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		
		PersonAttributeCohortDefinition location = new PersonAttributeCohortDefinition();
		PersonAttributeType healthCenterType = Context.getPersonService().getPersonAttributeTypeByName("Health Center");
		location.setAttributeType(healthCenterType);
		
		List<Location> locations = new ArrayList<Location>();
		Location currentLocationObj = Context.getLocationService().getLocation(properties.get("CURRENT_LOCATION"));
		locations.add(currentLocationObj);
		location.setValueLocations(locations);
		dataSetDefinition.addFilter(location);
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName);
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName);
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		dataSetDefinition.addColumn(imbId);
		
		//dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		//mappings.put("location", "${location}");
		
		reportDefinition.addDataSetDefinition("Register", dataSetDefinition, mappings);
		
		//h.replaceDataSetDefinition(dataSetDefinition);
	}
	
	
	
	private void createCohortDefinitions() {
		
		InProgramCohortDefinition inPMTCTProgram = new InProgramCohortDefinition();
		inPMTCTProgram.setName("pmtct: Combined Clinic In Program");
		List<Program> programs = new ArrayList<Program>();
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_COMBINED_CLINIC_PROGRAM"));
		if(pmtct != null)
		{
			programs.add(pmtct);
		}
		inPMTCTProgram.setPrograms(programs);
		h.replaceCohortDefinition(inPMTCTProgram);
		
	}
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctcombinedprogramname");
		properties.put("PMTCT_COMBINED_CLINIC_PROGRAM", pmtctProgram);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
	}
	
}
