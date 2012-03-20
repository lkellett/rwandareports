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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SimplePatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.module.reporting.report.ReportData;
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
	
	@SuppressWarnings("unused")
	@RequestMapping("/module/rwandareports/renderDataQualityDataSet")
	public String showReport(@RequestParam(required=false, value="savedDataSetKey") String savedDataSetKey,
			/*@RequestParam(required=false, value="savedColumnKey") String savedColumnKey,   */                
            @RequestParam(required=false, value="applyDataSetId") String applyDataSetId,
            @RequestParam(required=false, value="limit") Integer limit,
            Model model, HttpSession session) {
		String renderArg = (String) session.getAttribute(ReportingConstants.OPENMRS_REPORT_ARGUMENT);
		List<Model> modelList=new ArrayList<Model>();		
		
		ReportData data = null;
		try {
			data = (ReportData) session.getAttribute(ReportingConstants.OPENMRS_REPORT_DATA);
			//start
			
            savedDataSetKey="defaultDataSet";
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
            List<DQReportModel> dQRList=new ArrayList<DQReportModel>();
            
            for (String savedColumnKey : savedColumnKeys) {
				
			
            	DQReportModel  dQRObject=new DQReportModel();
            
            
            	
            	
            for (Map.Entry<String, DataSet> e : data.getDataSets().entrySet()) {
                    if (e.getKey().equals(savedDataSetKey)) { 
                            
                            MapDataSet mapDataSet = (MapDataSet) e.getValue();
                                                        
                                                     
                            DataSetColumn dataSetColumn = mapDataSet.getMetaData().getColumn(savedColumnKey);
                            //model.addAttribute("selectedColumn"+savedColumnKey, dataSetColumn);
                            
                            dQRObject.setSelectedColumn(dataSetColumn);
                            
                            Object result = mapDataSet.getData(dataSetColumn);
                            Cohort selectedCohort = null;
                            if (result instanceof CohortIndicatorAndDimensionResult) {
                                    CohortIndicatorAndDimensionResult cidr = (CohortIndicatorAndDimensionResult) mapDataSet.getData(dataSetColumn);
                                    selectedCohort = cidr.getCohortIndicatorAndDimensionCohort();
                            }
                            else if (result instanceof Cohort) {
                                    selectedCohort = (Cohort) result;
                            } 
                            
                            System.out.println("Result is of type " + result.getClass().getName());
                           // model.addAttribute("selectedCohort"+savedColumnKey, selectedCohort);
                            
                            dQRObject.setSelectedCohort(selectedCohort);
                            
                            //model.addAttribute("patients"+savedColumnKey, Context.getPatientSetService().getPatients(selectedCohort.getMemberIds()));        
                            
                            dQRObject.setPatients(Context.getPatientSetService().getPatients(selectedCohort.getMemberIds()));
                            
                            
                            // Evaluate the default patient dataset definition
                            DataSetDefinition dsd = null;
                            if (applyDataSetId != null) {
                                    try {
                                            dsd = Context.getService(DataSetDefinitionService.class).getDefinition(applyDataSetId, null);
                                    } catch (Exception ex) { 
                                                            
                                    }
                            }
                            
                            if (dsd == null) {
                                    SimplePatientDataSetDefinition d = new SimplePatientDataSetDefinition();
                                    d.addPatientProperty("patientId");
                                    List<PatientIdentifierType> types = ReportingConstants.GLOBAL_PROPERTY_PREFERRED_IDENTIFIER_TYPES();
                                    if (!types.isEmpty()) {
                                            d.setIdentifierTypes(types);
                                    }
                                    
                                    List<ProgramWorkflow> programWorkFlows=new ArrayList<ProgramWorkflow>();
                                    for (Program program : Context.getProgramWorkflowService().getAllPrograms(true)) {
                                    	/*if(program.getWorkflowByName("TREATMENT GROUP")!=null)
                                    	programWorkFlows.add(program.getWorkflowByName("TREATMENT GROUP"));
                                    	*/
                                    	for (ProgramWorkflow programWorkflow : program.getAllWorkflows()) {
                                    		if(programWorkflow!=null){
                                    		programWorkFlows.add(programWorkflow);
                                    		}
                                    		log.info("WorkFlow Name: "+programWorkflow.getName());
										}
									}
                                    
                                    if(programWorkFlows!=null && programWorkFlows.size()>0){
                                     //d.setProgramWorkflows(programWorkFlows);
                                    }
                                    d.addPatientProperty("givenName");
                                    d.addPatientProperty("familyName");
                                    d.addPatientProperty("age");
                                    d.addPatientProperty("gender");
                                    dsd = d;
                            }
                            
                            EvaluationContext evalContext = new EvaluationContext();
                            if (limit != null && limit > 0) 
                                    evalContext.setLimit(limit);
                            evalContext.setBaseCohort(selectedCohort);
                            
                            DataSet patientDataSet;
                            try {
                                    patientDataSet = Context.getService(DataSetDefinitionService.class).evaluate(dsd, evalContext);
                                    //model.addAttribute("dataSet"+savedColumnKey, patientDataSet);
                                //model.addAttribute("dataSetDefinition"+savedColumnKey, dsd);
                                
                                dQRObject.setDataSet(patientDataSet);   
                                dQRObject.setDataSetDefinition(dsd);
                                
                            } catch (EvaluationException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                            }
                    
                            
                    }
            }
        // Add all dataset definition to the request (allow user to choose)
        //model.addAttribute("dataSetDefinitions"+savedColumnKey, Context.getService(DataSetDefinitionService.class).getAllDefinitions(false));
        dQRObject.setDataSetDefinitions(Context.getService(DataSetDefinitionService.class).getAllDefinitions(false));
        //modelList.add(model);		
        dQRList.add(dQRObject);
            }
                model.addAttribute("dQRList",dQRList);
            
			//end
		}
		catch (ClassCastException ex) {
			// pass
		}
		if (data == null)
			return "redirect:../reporting/dashboard/index.form";
		
		MapDataSet dataSet = (MapDataSet) data.getDataSets().get(renderArg);
		model.addAttribute("columns", dataSet.getMetaData());
		
		
		/* List<Regimen> regimens = RwandaReportsUtil.createRegimenList(Context.getService(QuarterReportingService.class).getAllRegimens());  
		model.addAttribute("regimens", regimens);
		*/return null;
	}
	
}