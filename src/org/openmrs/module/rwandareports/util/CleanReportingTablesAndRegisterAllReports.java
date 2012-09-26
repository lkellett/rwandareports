/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.reporting.SetupAdultHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAdultLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaQuarterlyAndMonthReport;
import org.openmrs.module.rwandareports.reporting.SetupChemotherapyExpectedPatientList;
import org.openmrs.module.rwandareports.reporting.SetupCombinedHFCSPConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupDataEntryDelayReport;
import org.openmrs.module.rwandareports.reporting.SetupDataQualityIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupDiabetesConsultAndLTFU;
import org.openmrs.module.rwandareports.reporting.SetupDiabetesQuarterlyAndMonthReport;
import org.openmrs.module.rwandareports.reporting.SetupEpilepsyConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupEpilepsyLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupExposedClinicInfantMonthly;
import org.openmrs.module.rwandareports.reporting.SetupHIVResearchDataQualitySheet;
import org.openmrs.module.rwandareports.reporting.SetupHIVResearchExtractionSheet;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailurereport;
import org.openmrs.module.rwandareports.reporting.SetupHypertensionConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupHypertensionLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupHypertensionQuarterlyAndMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupIDProgramQuarterlyIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupMissingCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupMonthlyCD4DeclineReport;
import org.openmrs.module.rwandareports.reporting.SetupNCDConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupNCDLateVisitandLTFUReport;
import org.openmrs.module.rwandareports.reporting.SetupOncologyTreatmentAdministrationPlan;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTCombinedClinicMotherMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFoodDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFormCompletionSheet;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFormulaDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupPediHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupPediatricLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorByDistrictReport;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyViralLoadReport;
import org.openmrs.module.rwandareports.reporting.SetupRwandaPrimaryCareReport;
import org.openmrs.module.rwandareports.reporting.SetupTBConsultationSheet;


/**
 *
 */
public class CleanReportingTablesAndRegisterAllReports {
	
	private static Log log = LogFactory.getLog(CleanReportingTablesAndRegisterAllReports.class);
	
	//public enum ReportCategories{ HIV,NCD,CENTRAL,SITE,ONCOLOGY,CHW}
	
	public static String classification= Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.REPORT_CLASSIFICATION);
	
	public static void cleanTables() throws Exception {		
		
		ReportService rs=Context.getService(ReportService.class);
		List<ReportDesign> rDes=rs.getAllReportDesigns(true);		
		for (ReportDesign reportDesign : rDes) {
	        rs.purgeReportDesign(reportDesign);
        }
		
		ReportDefinitionService rds=Context.getService(ReportDefinitionService.class);
		List<ReportDefinition> rDefs=rds.getAllDefinitions(true);		
		for (ReportDefinition reportDefinition : rDefs) {		
			rds.purgeDefinition(reportDefinition);
        }
		
		for (ReportRequest request : rs.getReportRequests(null, null, null, Status.COMPLETED,Status.FAILED)) {
			try {
				rs.purgeReportRequest(request);
			}
			catch (Exception e) {
				log.warn("Unable to delete old report request: " + request, e);
			}
		}
		
			
    }
	
	public static void registerReports() throws Exception{
		
		String[] classifications=classification.split(",");
		
		for (String category : classifications) {
	       if(category.equalsIgnoreCase("HIV")) 
	    	   registerHIVReports();
	       else if(category.equalsIgnoreCase("NCD"))
	    	   registerNCDReports();
	       else if(category.equalsIgnoreCase("CENTRAL"))
	    	   registerCentralReports();
	       else if(category.equalsIgnoreCase("SITE"))
	    	   registerSiteReports();
	       else if(category.equalsIgnoreCase("ONCOLOGY"))
	    	   registerOncologyReports();
	       else if(category.equalsIgnoreCase("CHW"))
	    	   registerCHWReports();	       
        }
	}
	public static void registerHIVReports() throws Exception {
			////new SetupHivArtRegisterReport(false).setup();
			////new SetupHivArtRegisterReport(true).setup();
			
			new SetupCombinedHFCSPConsultationReport().setup();
			new SetupPMTCTFoodDistributionReport().setup();
			new SetupPMTCTFormulaDistributionReport().setup();
			new SetupPMTCTPregnancyConsultationReport().setup(); 
			
			new SetupPediHIVConsultationSheet().setup();
			new SetupAdultHIVConsultationSheet().setup();
			new SetupTBConsultationSheet().setup();
			new SetupAdultLateVisitAndCD4Report().setup();
			new SetupPediatricLateVisitAndCD4Report().setup();
			
			
			////new SetupTracNetRwandaReportBySite().setup();
			new SetupPMTCTCombinedClinicMotherMonthlyReport().setup();
			new SetupPMTCTPregnancyMonthlyReport().setup();
			new SetupExposedClinicInfantMonthly().setup();
    }
	public static void registerNCDReports() throws Exception {
			new SetupAsthmaConsultationSheet().setup();  
			//new SetupHeartFailurereport().setup();
			new SetupDiabetesConsultAndLTFU().setup();
			new SetupDiabetesQuarterlyAndMonthReport().setup();
			new SetupAsthmaQuarterlyAndMonthReport().setup();
			new SetupAsthmaLateVisit().setup();  
			
			new SetupEpilepsyConsultationSheet().setup();
			new SetupEpilepsyLateVisit().setup();
			new SetupHypertensionConsultationSheet().setup();
			new SetupHypertensionLateVisit().setup();
			new SetupHypertensionQuarterlyAndMonthlyReport().setup();
    }
	public static void registerCentralReports() throws Exception {
			new SetupHIVResearchDataQualitySheet().setup();
			new SetupHIVResearchExtractionSheet().setup();
			new SetupIDProgramQuarterlyIndicatorReport().setup(); 
			new SetupMonthlyCD4DeclineReport().setup();
			new SetupMissingCD4Report().setup();
			new SetupQuarterlyCrossSiteIndicatorByDistrictReport().setup();	
			new SetupQuarterlyViralLoadReport().setup();
			new SetupPMTCTFormCompletionSheet().setup();
			new SetupRwandaPrimaryCareReport().setup();
    }
	public static void registerSiteReports() throws Exception {
			new SetupDataQualityIndicatorReport().setup();
			new SetupDataEntryDelayReport().setup();
    }
	public static void registerOncologyReports() throws Exception {
			new SetupOncologyTreatmentAdministrationPlan().setup();
			new SetupChemotherapyExpectedPatientList().setup();
    }
	public static void registerCHWReports() {
	    
    }

}
