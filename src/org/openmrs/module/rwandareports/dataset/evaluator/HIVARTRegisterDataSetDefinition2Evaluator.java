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
package org.openmrs.module.rwandareports.dataset.evaluator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.service.PatientDataService;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition2;
import org.openmrs.module.rwandareports.dataset.HIVRegisterDataSetRowComparator;

/**
 * The logic that evaluates a {@link PatientDataSetDefinition} and produces an {@link DataSet}
 * @see PatientDataSetDefinition
 */
@Handler(supports={HIVARTRegisterDataSetDefinition2.class})
public class HIVARTRegisterDataSetDefinition2Evaluator implements DataSetEvaluator {

	protected Log log = LogFactory.getLog(this.getClass());
	
	private static int START_DATE_WORKFLOW_ART = 0;
	private static int START_ART_REGIMEN = 1;
	private static int IMB_ID = 2;
	private static int TRACNET_ID = 3;
	private static int GIVEN_NAME = 4;
	private static int FAMILY_NAME = 5;
	private static int GENDER = 6;
	private static int BIRTHDATE = 7;
	private static int AGE_AT_START = 8;
	private static int WEIGHT_AT_START = 9;
	private static int INITIAL_STAGE = 10;
	private static int INITIAL_CD4_COUNT = 11;
	private static int INITIAL_CD4_PERCENT = 12;
	private static int CTX_TREATMENT = 13;
	private static int TB_TREATMENT = 14;
	private static int PREGNANCY_DELIVERY_DATES = 15;
	private static int INITIAL_REGIMEN = 16;
	private static int FIRST_LINE_CHANGES = 17;
	private static int SECOND_LINE_CHANGES = 18;
	private static int ART_DRUGS = 19;
	private static int CD4_OBS = 20;
	private static int STAGE_OBS = 21;
	private static int TB_OBS = 22;

	/**
	 * Public constructor
	 */
	public HIVARTRegisterDataSetDefinition2Evaluator() { }
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 * @should evaluate a PatientDataSetDefinition
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException{
		
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		HIVARTRegisterDataSetDefinition2 definition = (HIVARTRegisterDataSetDefinition2) dataSetDefinition;
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		Cohort cohort = context.getBaseCohort();

		// By default, get all patients
		if (cohort == null) {
			cohort = Context.getPatientSetService().getAllPatients();
		}
		
		for(CohortDefinition cd: definition.getFilters())
		{
			Cohort filter;
			try {
				filter = Context.getService(CohortDefinitionService.class).evaluate(cd, context);
				cohort = cohort.intersect(cohort, filter);
			} catch (EvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Get a list of patients based on the cohort members
		List<Patient> patients = Context.getPatientSetService().getPatients(cohort.getMemberIds());
	
//		int i = 0;
		for (Patient p : patients) {			
			DataSetRow row = new DataSetRow();
			
//			i++;
//			
//			if(i > 20)
//			{
//				break;
//			}
				
			for(RowPerPatientData pd: definition.getColumns())
			{
				pd.setPatient(p);
				pd.setPatientId(p.getPatientId());
				long startTime = System.currentTimeMillis();
				PatientDataResult patientDataResult = Context.getService(PatientDataService.class).evaluate(pd, context);
				long timeTake = System.currentTimeMillis() - startTime;
				log.info(pd.getName() + ": " + timeTake);
				
				DataSetColumn c = new DataSetColumn(patientDataResult.getName(), patientDataResult.getDescription(), patientDataResult.getColumnClass());
				row.addColumnValue(c, patientDataResult);
			}
			dataSet.addRow(row);
		}
		
		dataSet = transformDataSet(dataSet, dataSetDefinition, context);
		return dataSet;
	}
	
	private SimpleDataSet transformDataSet(DataSet dataset, DataSetDefinition dataSetDefinition, EvaluationContext context)
	{
		//sort into a list
		List<DataSetRow> rows = new ArrayList<DataSetRow>();
		
		for(DataSetRow row: dataset)
		{
			rows.add(row);
		}
		
		Collections.sort(rows, new HIVRegisterDataSetRowComparator(dataset));
		
		SimpleDataSet resultSet = new SimpleDataSet(dataSetDefinition, context);
		
		int rowNumber = 0;
		for(DataSetRow row: rows)
	    {
			
			DataSetRow rr = new DataSetRow();
			
			rowNumber++;
			int sheetNumber = 1;
			int startingMonth = 0;
			
			List<DataSetColumn> columnList = dataset.getMetaData().getColumns();
			
			Date startingDate =  null;
			DrugOrder startingRegimen = (DrugOrder)((PatientDataResult)row.getColumnValue(columnList.get(START_ART_REGIMEN))).getValue();
			
			startingDate = (Date)((PatientDataResult)row.getColumnValue(columnList.get(START_DATE_WORKFLOW_ART))).getValue();
			
			List<DrugOrder> drugsValue = (List<DrugOrder>)((PatientDataResult)row.getColumnValue(columnList.get(ART_DRUGS))).getValue();
	        List<Obs> cd4Value = (List<Obs>)((PatientDataResult)row.getColumnValue(columnList.get(CD4_OBS))).getValue();
	        List<Obs> stageValue = (List<Obs>)((PatientDataResult)row.getColumnValue(columnList.get(STAGE_OBS))).getValue();
	        List<Obs> tbValue = (List<Obs>)((PatientDataResult)row.getColumnValue(columnList.get(TB_OBS))).getValue();
	        
	        List<DrugOrder> firstLineChange = (List<DrugOrder>)((PatientDataResult)row.getColumnValue(columnList.get(FIRST_LINE_CHANGES))).getValue();
	        List<DrugOrder> secondLineChange = (List<DrugOrder>)((PatientDataResult)row.getColumnValue(columnList.get(SECOND_LINE_CHANGES))).getValue();
	        
	        drugsValue = cleanseDrugsList(drugsValue, startingDate);
	        cd4Value = cleanseObsList(cd4Value, startingDate);
	        stageValue = cleanseObsList(stageValue, startingDate);
	        tbValue = cleanseObsList(tbValue, startingDate);
	        
	        firstLineChange = cleanseDrugsList(firstLineChange, startingDate);
	        secondLineChange = cleanseDrugsList(secondLineChange, startingDate);
	        
	        String firstLineChangeStr = getDiscontinuedReasons(firstLineChange);
	        String secondLineChangeStr = getDiscontinuedReasons(secondLineChange);
	        
	        String colName = "No";
			DataSetColumn one = new DataSetColumn(colName, colName, Integer.class);
			rr.addColumnValue(one, rowNumber);
			
			DataSetColumn two = new DataSetColumn("Date of Debut of ARV/ART", "Date of Debut of ARV/ART", Date.class);
			rr.addColumnValue(two, startingDate);
			
			String id = (String)((PatientDataResult)row.getColumnValue(columnList.get(TRACNET_ID))).getValue();
			if(id == null)
			{
				id = " / " + (String)((PatientDataResult)row.getColumnValue(columnList.get(IMB_ID))).getValue();
			}
			else
			{
				id = id + " / " + (String)((PatientDataResult)row.getColumnValue(columnList.get(IMB_ID))).getValue();
			}
			DataSetColumn idcol = new DataSetColumn("id", "id", String.class);
			rr.addColumnValue(idcol, id);
			
			String name = (String)((PatientDataResult)row.getColumnValue(columnList.get(GIVEN_NAME))).getValue();
			if(name == null)
			{
				name = (String)((PatientDataResult)row.getColumnValue(columnList.get(FAMILY_NAME))).getValue();
			}
			else
			{
				name = name + " " + (String)((PatientDataResult)row.getColumnValue(columnList.get(FAMILY_NAME))).getValue();
			}
			DataSetColumn namecol = new DataSetColumn("name", "name", String.class);
			rr.addColumnValue(namecol, name);
			
			for (int j = 6; j < 9; j++)
			{
				Object cellValue = ((PatientDataResult)row.getColumnValue(columnList.get(j))).getValue();
		    
				if(cellValue instanceof ArrayList)
				{
					cellValue = cellValue.toString();
				}
				if(cellValue instanceof DrugOrder)
				{
					String drugName = "Drug Missing";
					try{
						drugName = ((DrugOrder)cellValue).getDrug().getName();
					}catch(Exception e)
					{
						System.err.println(e.getMessage());
					}
					cellValue = drugName;
				}
				DataSetColumn col = new DataSetColumn(columnList.get(j).getLabel(), columnList.get(j).getLabel(), columnList.get(j).getClass());
				rr.addColumnValue(col, cellValue);
			}
			
			ObservationResult weightAtStart = (ObservationResult)row.getColumnValue(columnList.get(WEIGHT_AT_START));
			String colLabel = columnList.get(WEIGHT_AT_START).getLabel();
			DataSetColumn weightCol = new DataSetColumn(colLabel, colLabel, String.class);
			rr.addColumnValue(weightCol, weightAtStart.getValue());
			
			ObservationResult stageAtStart = (ObservationResult)row.getColumnValue(columnList.get(INITIAL_STAGE));
			String stageResult = (String)stageAtStart.getValue();
			//need to remove the "WHO STAGE" from the result
			if(stageResult != null)
			{
				stageResult = stageResult.replaceFirst("WHO STAGE", "");
			}
			DataSetColumn initStageCol = new DataSetColumn(columnList.get(INITIAL_STAGE).getLabel(), columnList.get(INITIAL_STAGE).getLabel(), String.class);
			rr.addColumnValue(initStageCol, stageResult);
			
			DataSetColumn col = new DataSetColumn("initialCD4", "initialCD4", String.class);
			String cd4Result = null;
			for(int c = 11; c < 13; c++)
			{
				Object obsResult = row.getColumnValue(columnList.get(c));
				if(obsResult instanceof ObservationResult)
				{
					ObservationResult obs = (ObservationResult)row.getColumnValue(columnList.get(c));
					
					if(obs.getValue() != null)
					{
						if(cd4Result == null)
						{
							cd4Result = obs.getValue();
						}
						else
						{
							cd4Result = cd4Result + " " + obs.getValue();
						}
						if(obs.getDateOfObservation() != null)
						{
							cd4Result = cd4Result + " " + new SimpleDateFormat("yyyy-MM-dd").format(obs.getDateOfObservation());
						}
					}
				}
			}
			rr.addColumnValue(col, cd4Result);
			
			for(int k = 13; k < 15; k++)
			{
				List<DrugOrder> values = (List<DrugOrder>)((PatientDataResult)row.getColumnValue(columnList.get(k))).getValue();
			
				String cellValue = "";
				Date startDate = null;
				Date endDate = null;
				for(DrugOrder drO: values)
				{
					startDate = drO.getStartDate();
					endDate = drO.getDiscontinuedDate();
				}
				
				String dates = null;
				if(startDate != null)
				{
					dates = "Start: " + new SimpleDateFormat("yyyy-MM-dd").format(startDate);
					if(endDate != null)
					{
						dates = dates + "/ End: " + new SimpleDateFormat("yyyy-MM-dd").format(endDate);
					}
				}
				
				DataSetColumn startCol = new DataSetColumn(columnList.get(k).getLabel(), columnList.get(k).getLabel(), String.class);
				rr.addColumnValue(startCol, dates);
			}

			List<Obs> pregnancy = (List<Obs>)((PatientDataResult)row.getColumnValue(columnList.get(PREGNANCY_DELIVERY_DATES))).getValue();
			for(int m = 0; m < 4; m++)
			{
				String columnName = "Pregnancy " + m;
				DataSetColumn pregcol = new DataSetColumn(columnName, columnName, String.class);
				
				if(pregnancy != null && pregnancy.size() > m)
				{
					Obs pregOb = pregnancy.get(m);
					rr.addColumnValue(pregcol,pregOb.getValueAsString(Context.getLocale()));
				}
				else
				{
					rr.addColumnValue(pregcol,null);
				}
			}

			DrugOrder initial = (DrugOrder)((PatientDataResult)row.getColumnValue(columnList.get(INITIAL_REGIMEN))).getValue();
			String drugName = "Drug Missing";
			try{
				drugName = ((DrugOrder)initial).getDrug().getName();
			}catch(Exception e)
			{
				System.err.println(e.getMessage());
			}
			DataSetColumn initReg = new DataSetColumn("Initial Regimen", "Initial Regimen", String.class);
			rr.addColumnValue(initReg, drugName);
		
			DataSetColumn firstLine = new DataSetColumn("First Line Changes", "First Line Changes", String.class);
			rr.addColumnValue(firstLine, getDiscontinuedReasons(firstLineChange));
		
			DataSetColumn secondLine = new DataSetColumn("Second Line Changes", "Second Line Changes", String.class);
			rr.addColumnValue(secondLine, getDiscontinuedReasons(secondLineChange));

			String drugCellValue = retrieveCorrectMonthsOb(0, drugsValue, startingDate);
			DataSetColumn monthZero = new DataSetColumn("Month 0", "Month 0", String.class);
			rr.addColumnValue(monthZero, drugCellValue);
			
	        addPatientRow(startingDate, rowNumber, startingMonth, sheetNumber, row, rr, dataset, firstLineChangeStr, secondLineChangeStr, drugsValue, cd4Value, stageValue, tbValue);
	        resultSet.addRow(rr);
	    }
		
		return resultSet;
	}
	
	private void addPatientRow(Date startingDate, int rowNumber, int startingMonth, int sheetNumber, DataSetRow row, DataSetRow resultRow, DataSet dataset, String firstLineChange, String secondLineChange, List<DrugOrder> drugsValue, List<Obs> cd4Value, List<Obs> stageValue, List<Obs> tbValue)
	{
		int month = startingMonth;
	    
		for(int f = 0; f < 6; f++)
		{
			for(int n = 0; n < 5; n++)
			{       
				month++;
				String cellValue = retrieveCorrectMonthsOb(month, drugsValue, startingDate);
				
				String columnName = "Month " + month;
				DataSetColumn monthCol = new DataSetColumn(columnName, columnName, String.class);
				resultRow.addColumnValue(monthCol, cellValue);
			}
			month++;
			String columnName = "CD4 " + month;
			DataSetColumn monthCol = new DataSetColumn(columnName, columnName, String.class);
			
			if(cd4Value != null && cd4Value.size() > 0)
			{
				List<Obs> valueToBeUsed = retrieveCorrect6MonthsOb(month, cd4Value, startingDate);
				String cellValue = "";
				Date date = null;
				if(valueToBeUsed.size() > 0)
				{
					for(Obs ob: valueToBeUsed)
					{
						cellValue = ob.getValueAsString(Context.getLocale()); 
						date = ob.getObsDatetime();
						if(date != null)
						{
							cellValue = cellValue + " " + new SimpleDateFormat("yyyy-MM-dd").format(date);
						}
					}
	        	
					cd4Value.removeAll(valueToBeUsed);
				}
				resultRow.addColumnValue(monthCol, cellValue);
	    	}
			else
			{
				resultRow.addColumnValue(monthCol, null);
			}
	    
			String stageColName = "Stage " + month;
			DataSetColumn stageCol = new DataSetColumn(stageColName, stageColName, String.class);
			if(stageValue != null && stageValue.size() > 0)
			{
				List<Obs> valueToBeUsed = retrieveCorrect6MonthsOb(month, stageValue, startingDate);
				String cellValue = "";
				if(valueToBeUsed.size() > 0)
				{
					for(Obs ob: valueToBeUsed)
					{
						cellValue = ob.getValueAsString(Context.getLocale()); 
						cellValue = cellValue.replaceFirst("WHO STAGE", "");
						
					}
	        	
					stageValue.removeAll(valueToBeUsed);
				}
				resultRow.addColumnValue(stageCol, cellValue);
			}
			else
			{
				resultRow.addColumnValue(stageCol, null);
			}
	        
			String tbColName = "TB " + month;
			DataSetColumn tbCol = new DataSetColumn(tbColName, tbColName, String.class);
			if(tbValue != null && tbValue.size() > 0)
			{
				List<Obs> valueToBeUsed = retrieveCorrect6MonthsOb(month, tbValue, startingDate);
				String cellValue = "";
				if(valueToBeUsed.size() > 0)
				{
					for(Obs ob: valueToBeUsed)
					{
						cellValue = ob.getValueAsString(Context.getLocale()); 
					}
	       
					tbValue.removeAll(valueToBeUsed);
				}
				resultRow.addColumnValue(tbCol, cellValue);
			}
			else
			{
				resultRow.addColumnValue(tbCol, null);
			}
		}
		//if we still have cd4, stage, or tb obs left we need to move onto sheet 2
		//or if the drug orders are still current
		String checkForDrugOrders = retrieveCorrectMonthsOb(month + 1, drugsValue, startingDate);
		if((cd4Value != null && cd4Value.size() > 0) || (stageValue != null && stageValue.size() > 0) || (tbValue != null && tbValue.size() > 0) || checkForDrugOrders.length() > 0)
		{
			addPatientRow(startingDate, rowNumber, month, sheetNumber + 1, row, resultRow, dataset, firstLineChange, secondLineChange, drugsValue, cd4Value, stageValue, tbValue);
		}
	}

	//to avoid infinite loops we are going to remove all obs that are before the starting date + 3 months
	private List<Obs> cleanseObsList(List<Obs> obs, Date startingDate)
	{
		List<Obs> obsToReturn = new ArrayList<Obs>();
		//if the starting date is null, we are not going to be able to do any month
		//calculations so we are just going to set the list to null and exit
		if(startingDate != null)
		{
	
			for(Obs o: obs)
			{
				int diff = calculateMonthsDifference(o.getObsDatetime(), startingDate);
		
				if(diff > 3)
				{
					obsToReturn.add(o);
				}
			}	
		}
		return obsToReturn;
	}

	private List<DrugOrder> cleanseDrugsList(List<DrugOrder> drugOrders, Date startingDate)
	{
		List<DrugOrder> ordersToReturn = new ArrayList<DrugOrder>();
		//if the starting date is null, we are not going to be able to do any month
		//calculations so we are just going to set the list to null and exit
		if(startingDate != null)
		{
			Calendar obsResultCal = Calendar.getInstance();
			obsResultCal.setTime(startingDate);
	
			for(DrugOrder o: drugOrders)
			{
				Calendar oCal = Calendar.getInstance();
				oCal.setTime(o.getStartDate());
		
				if((oCal.get(Calendar.YEAR) == obsResultCal.get(Calendar.YEAR) && oCal.get(Calendar.DAY_OF_YEAR) == obsResultCal.get(Calendar.DAY_OF_YEAR)) ||  o.getStartDate().after(startingDate))
				{
					ordersToReturn.add(o);
				}
			}	
		}
		return ordersToReturn;
	}

	private String retrieveCorrectMonthsOb(int month, List<DrugOrder>orders, Date startingDate)
	{
		String drugOrders = "";

		if(startingDate != null)
		{
			Calendar monthDate = Calendar.getInstance();
			monthDate.setTime(startingDate);
			monthDate.add(Calendar.MONTH, month);
			
			Calendar currentDate = Calendar.getInstance();
			
			if(monthDate.before(currentDate))
			{
	
				for(DrugOrder current: orders)
				{	
					if(current.isCurrent(monthDate.getTime()))
					{
						String drugName = "Drug Missing";
						try{
							drugName = current.getDrug().getName();
						}catch(Exception e)
						{
							System.err.println(e.getMessage());
						}
				
						if(drugOrders.length() > 0)
						{
							drugOrders = drugOrders + "," + drugName;
						}
						else
						{
							drugOrders = drugName;
						}
					}
				}
			}
			
			if(drugOrders.length() > 0)
			{
				drugOrders = drugOrders + " " + new SimpleDateFormat("MMM yyyy").format(monthDate.getTime());
			}
		}
		return drugOrders;
	}

	private List<Obs> retrieveCorrect6MonthsOb(int month, List<Obs>obs, Date startingDate)
	{
		List<Obs> obList = new ArrayList<Obs>();
		for(Obs current: obs)	
		{
			int monthsFromStart = calculateMonthsDifference(current.getObsDatetime(), startingDate);
	
			if(monthsFromStart >= month - 3 && monthsFromStart <= month + 3)
			{
				obList.add(current);
			}
		}

		return obList;
	}

	private int calculateMonthsDifference(Date observation, Date startingDate)
	{
		int diff = 0;

		Calendar obsDate = Calendar.getInstance();	
		obsDate.setTime(observation);

		Calendar startDate = Calendar.getInstance();
		startDate.setTime(startingDate);

		//find out if there is any difference in years first
		diff = obsDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
		diff = diff * 12;

		int monthDiff = obsDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);
		diff = diff + monthDiff;

		return diff;
	}

	private String getDiscontinuedReasons(List<DrugOrder> drugOrderList)
	{
		String discontinuedReasons = "";

		for(DrugOrder o: drugOrderList)
		{
			if(o.isDiscontinued(null))
			{
				if(discontinuedReasons.length() > 0)
				{
					discontinuedReasons = discontinuedReasons + " , ";
				}
		
				if(o.getDiscontinuedReason() != null && o.getDrug() != null)
				{
					discontinuedReasons = discontinuedReasons + o.getDrug().getName() + " - " + o.getDiscontinuedReason().getDisplayString(); 
				}
		
				if(o.getDiscontinuedDate() != null)
				{
					discontinuedReasons =  discontinuedReasons + ":" + new SimpleDateFormat("yyyy-MM-dd").format(o.getDiscontinuedDate());
				}
			}
		}

		return discontinuedReasons;
	}
}
