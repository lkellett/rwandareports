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
package org.openmrs.module.rwandareports.web.controller;

import java.util.*;

import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SimplePatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.rwandareports.indicator.EncounterIndicatorResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller prepares result page for Data Quality Report
 */
@Controller
public class RenderDataQualityReportController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@SuppressWarnings({ "unused", "deprecation" })
	@RequestMapping("/module/rwandareports/renderDataQualityDataSet")
	public String showReport(@RequestParam(required=false, value="savedDataSetKey") String savedDataSetKey,
			@RequestParam(required=false, value="savedDataSetEncKey") String savedDataSetEncKey,                   
            @RequestParam(required=false, value="applyDataSetId") String applyDataSetId,
            @RequestParam(required=false, value="limit") Integer limit,
            Model model, HttpSession session) throws Exception {
		String renderArg = (String) session.getAttribute(ReportingConstants.OPENMRS_REPORT_ARGUMENT);
		List<Model> modelList=new ArrayList<Model>();
		 
		ReportData data = null;
		try {
			data = (ReportData) session.getAttribute(ReportingConstants.OPENMRS_REPORT_DATA);
			//start
			
            savedDataSetKey="defaultDataSet";
            savedDataSetEncKey="encFuture";
          
 //evaluate indicator report
            
            if(savedDataSetKey.equals("defaultDataSet")){
            	  List<String> savedColumnKeys=new ArrayList<String>();
                  savedColumnKeys.add("1");
                  savedColumnKeys.add("2");
                  savedColumnKeys.add("3");
                  savedColumnKeys.add("4");
                  savedColumnKeys.add("5");
                  savedColumnKeys.add("6");
                  savedColumnKeys.add("7");
                  savedColumnKeys.add("8");
                  savedColumnKeys.add("9");
                  savedColumnKeys.add("10");
                  savedColumnKeys.add("11");
                  savedColumnKeys.add("12");
                  savedColumnKeys.add("13");
                  savedColumnKeys.add("14");
                  savedColumnKeys.add("15");
                  savedColumnKeys.add("16");
                  savedColumnKeys.add("17");
                  savedColumnKeys.add("18");
                  savedColumnKeys.add("19");
                  savedColumnKeys.add("20");
                  savedColumnKeys.add("21");
                    
             List<DQReportModel> dQRList=new ArrayList<DQReportModel>();
             for (String savedColumnKey : savedColumnKeys) {
      		 DQReportModel dQRObject=new DQReportModel();
                  
             for (Map.Entry<String, DataSet> e : data.getDataSets().entrySet()) {
                if (e.getKey().equals(savedDataSetKey)) { 
                 MapDataSet mapDataSet = (MapDataSet) e.getValue();
                 DataSetColumn dataSetColumn = mapDataSet.getMetaData().getColumn(savedColumnKey);
                 dQRObject.setSelectedColumn(dataSetColumn);
                                  
                 Object result = mapDataSet.getData(dataSetColumn);
                 Cohort selectedCohort = null;
                 if (result instanceof CohortIndicatorAndDimensionResult) {
                 CohortIndicatorAndDimensionResult cidr = (CohortIndicatorAndDimensionResult) mapDataSet.getData(dataSetColumn);
                 selectedCohort = cidr.getCohortIndicatorAndDimensionCohort();
                                        
                                  }
                 // Evaluate the default patient dataset definition
                 DataSetDefinition dsd = null;
                 if (dsd == null) {
                    SimplePatientDataSetDefinition d = new SimplePatientDataSetDefinition();
                    d.addPatientProperty("patientId");
                    List<PatientIdentifierType> types = ReportingConstants.GLOBAL_PROPERTY_PREFERRED_IDENTIFIER_TYPES();
                    if (!types.isEmpty()) {
                       d.setIdentifierTypes(types);
                    }
                                         
                    List<ProgramWorkflow> programWorkFlows=new ArrayList<ProgramWorkflow>(); 
                  
                    ProgramWorkflow hivWorkflow=Context.getProgramWorkflowService().getProgram(3).getWorkflowByName("TREATMENT GROUP");
                    ProgramWorkflow tbWorkflow=Context.getProgramWorkflowService().getProgram(4).getWorkflowByName("TUBERCULOSIS TREATMENT GROUP");
                   
                    programWorkFlows.add(hivWorkflow);
                    programWorkFlows.add(tbWorkflow);
                    log.info("==========workflow result========="+programWorkFlows);
                    
                    if((programWorkFlows!=null && programWorkFlows.size()>0)) {
                       
                    d.setProgramWorkflows(programWorkFlows);
                     
                    d.addPatientProperty("givenName");
                    d.addPatientProperty("familyName");
                    d.addPatientProperty("age");
                    d.addPatientProperty("gender");
                      
                    dsd = d;
                       }
                    
                     }
                    EvaluationContext evalContext = new EvaluationContext();
                    evalContext.setBaseCohort(selectedCohort);
                                  
                    DataSet patientDataSet;
                    try {
                      patientDataSet = Context.getService(DataSetDefinitionService.class).evaluate(dsd, evalContext);
                      dQRObject.setDataSet(patientDataSet);   
                      dQRObject.setDataSetDefinition(dsd);
                        } catch (EvaluationException e1) {
                   // TODO Auto-generated catch block
                         e1.printStackTrace();
                              }
                       }
                  }
              // Add all dataset definition to the request (allow user to choose)
              dQRObject.setDataSetDefinitions(Context.getService(DataSetDefinitionService.class).getAllDefinitions(false));		
              dQRList.add(dQRObject);
                  }
                  
              model.addAttribute("dQRList",dQRList);
            }
	 
          
         // evaluate encounter dataset
            if(savedDataSetEncKey.equals("encFuture")){
            List<String> savedColumnKeys=new ArrayList<String>();
            savedColumnKeys.add("Observations in the future (except return visit date)");
           List<DQReportModel> dQRListenc=new ArrayList<DQReportModel>();
           for (String savedColumnKey : savedColumnKeys) {
			for (Map.Entry<String, DataSet> e : data.getDataSets().entrySet()) {
                    if (e.getKey().equals(savedDataSetEncKey)) {
                    	
                 List<DataSetRow> mapDataSet=((SimpleDataSet) e.getValue()).getRows();
                 log.info("==========mapDataSet========="+mapDataSet);
                            DataSetColumn dataSetColumn = e.getValue().getMetaData().getColumn(savedColumnKey);
                            log.info("==========datasetColumn========="+dataSetColumn);
                            List<DataSetColumn> dsdlist=new ArrayList<DataSetColumn>();
                            dsdlist.add(dataSetColumn);
                           
                            Object colValue = new Object();
                            Object result=new Object();
                            Set<Integer> selectedEncounter = new TreeSet<Integer>();
                            for (DataSetRow row : mapDataSet) {
                			  for (DataSetColumn column : dsdlist) {
                			colValue = row.getColumnValue(column.getName());
                			 result =colValue;
                			 log.info("==========result========="+result);
                			 if (result instanceof EncounterIndicatorResult) {
                            	EncounterIndicatorResult cidr = (EncounterIndicatorResult)result;
                                selectedEncounter =cidr.getMemberIds();
                              }
                           
                			 }
                            }
                            
                	       List<Encounter> encounters=new ArrayList<Encounter>();
                	       List<Patient> patients=new ArrayList<Patient>();
                           DQReportModel dQRObjectenc=new DQReportModel();

                           for (Integer encId : selectedEncounter) {
                        	   //here i will add the encounter_Datetime sorting.
                        	   encounters.add(Context.getEncounterService().getEncounter(encId)); 
                        	   
                        	 List<Date>encounterDatetime=new ArrayList<Date> ();
                        	 encounterDatetime.add(Context.getEncounterService().getEncounter(encId).getEncounterDatetime());
                        	  
                        	   log.info("=====the encounters====="+encounterDatetime);
                        	                          	
                               patients.add(Context.getEncounterService().getEncounter(encId).getPatient());
                        	  log.info("=====this is it====="+patients);
                              }
                			  
                           dQRObjectenc.setEncounters(encounters);
                           dQRObjectenc.setPatients(patients);
                           dQRObjectenc.setSelectedEncounter(savedColumnKey);
                           dQRListenc.add(dQRObjectenc);
                       }
                  }
        }
            model.addAttribute("dQRListenc",dQRListenc);
            
		} //end of if 
           
       }
		catch (ClassCastException ex) {
			// pass
		}
		if (data == null)
			return "redirect:../reporting/dashboard/index.form";
		
		SimpleDataSet dataSet = (SimpleDataSet) data.getDataSets().get(renderArg);
		//model.addAttribute("columns", dataSet.getMetaData());
		return null;
	}
	
}