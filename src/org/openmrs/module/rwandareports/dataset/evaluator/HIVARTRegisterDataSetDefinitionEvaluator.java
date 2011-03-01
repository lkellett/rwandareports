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
import java.util.Collection;
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
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientData;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.service.PatientDataService;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.HIVRegisterDataSetRowComparator;

/**
 * The logic that evaluates a {@link PatientDataSetDefinition} and produces an {@link DataSet}
 * @see PatientDataSetDefinition
 */
@Handler(supports={HIVARTRegisterDataSetDefinition.class})
public class HIVARTRegisterDataSetDefinitionEvaluator implements DataSetEvaluator {

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * Public constructor
	 */
	public HIVARTRegisterDataSetDefinitionEvaluator() { }
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 * @should evaluate a PatientDataSetDefinition
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) {
		
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		HIVARTRegisterDataSetDefinition definition = (HIVARTRegisterDataSetDefinition) dataSetDefinition;
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		Cohort cohort = context.getBaseCohort();

		// By default, get all patients
		if (cohort == null) {
			cohort = Context.getPatientSetService().getAllPatients();
		}
		
		for(CohortDefinition cd: definition.getFilters())
		{
			Cohort filter = Context.getService(CohortDefinitionService.class).evaluate(cd, context);
			cohort = cohort.intersect(cohort, filter);
		}

		// Get a list of patients based on the cohort members
		List<Patient> patients = Context.getPatientSetService().getPatients(cohort.getMemberIds());
		
		int i = 0;
		for (Patient p : patients) {			
			DataSetRow row = new DataSetRow();
			
			i++;
			
			if(i > 15)
			{
				break;
			}
				
			for(PatientData pd: definition.getColumns())
			{
				pd.setPatient(p);
				pd.setPatientId(p.getPatientId());
				long startTime = System.currentTimeMillis();
				PatientDataResult patientDataResult = Context.getService(PatientDataService.class).evaluate(pd, context);
				long timeTake = System.currentTimeMillis() - startTime;
				System.out.println(pd.getName() + ": " + timeTake);
				
				DataSetColumn c = new DataSetColumn(patientDataResult.getName(), patientDataResult.getDescription(), patientDataResult.getColumnClass());
				row.addColumnValue(c, patientDataResult.getValue());
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
			
			Date startingDate = (Date) row.getColumnValue(columnList.get(0));
			
			List<DrugOrder> drugsValue = (List<DrugOrder>)row.getColumnValue(columnList.get(19));
	        List<Obs> cd4Value = (List<Obs>)row.getColumnValue(columnList.get(20));
	        List<Obs> stageValue = (List<Obs>)row.getColumnValue(columnList.get(21));
	        List<Obs> tbValue = (List<Obs>)row.getColumnValue(columnList.get(22));
	        
	        List<DrugOrder> firstLineChange = (List<DrugOrder>)row.getColumnValue(columnList.get(17));
	        List<DrugOrder> secondLineChange = (List<DrugOrder>)row.getColumnValue(columnList.get(18));
	        
	        drugsValue = cleanseDrugsList(drugsValue, startingDate);
	        cd4Value = cleanseObsList(cd4Value, startingDate);
	        stageValue = cleanseObsList(stageValue, startingDate);
	        tbValue = cleanseObsList(tbValue, startingDate);
	        
	        firstLineChange = cleanseDrugsList(firstLineChange, startingDate);
	        secondLineChange = cleanseDrugsList(secondLineChange, startingDate);
	        
	        String firstLineChangeStr = getDiscontinuedReasons(firstLineChange);
	        String secondLineChangeStr = getDiscontinuedReasons(secondLineChange);
	        
	        addPatientRow(rowNumber, startingMonth, sheetNumber, row, rr, dataset, firstLineChangeStr, secondLineChangeStr, drugsValue, cd4Value, stageValue, tbValue);
	        resultSet.addRow(rr);
	    }
		
		return resultSet;
	}
	
	private void addPatientRow(int rowNumber, int startingMonth, int sheetNumber, DataSetRow row, DataSetRow resultRow, DataSet dataset, String firstLineChange, String secondLineChange, List<DrugOrder> drugsValue, List<Obs> cd4Value, List<Obs> stageValue, List<Obs> tbValue)
	{
		String colName = "No" + sheetNumber;
		DataSetColumn one = new DataSetColumn(colName, colName, Integer.class);
		resultRow.addColumnValue(one, rowNumber);
		
		Date startingDate = null;
		
		List<DataSetColumn> columnList = dataset.getMetaData().getColumns();
		startingDate = (Date) row.getColumnValue(columnList.get(0));
		
		DataSetColumn two = new DataSetColumn("Date of Debut of ARV/ART" + sheetNumber, "Date of Debut of ARV/ART", Date.class);
		resultRow.addColumnValue(two, startingDate);

		DrugOrder startingRegimen = (DrugOrder)row.getColumnValue(columnList.get(1));
		DataSetColumn three = new DataSetColumn("Date of Starting Regimen" + sheetNumber, "Date of Starting Regimen", Date.class);
		if(startingRegimen != null)
		{
			Date startingRegimenDate = startingRegimen.getStartDate();
			
			resultRow.addColumnValue(three, startingRegimenDate);
		}
		else
		{
			resultRow.addColumnValue(three, null);
		}

		for (int j = 2; j < 13; j++)
		{
			Object cellValue = row.getColumnValue(columnList.get(j));
	    
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
			DataSetColumn col = new DataSetColumn(columnList.get(j).getLabel()+ sheetNumber, columnList.get(j).getLabel(), columnList.get(j).getClass());
			resultRow.addColumnValue(col, cellValue);
		}

		for(int k = 13; k < 15; k++)
		{
			List<DrugOrder> values = (List<DrugOrder>)row.getColumnValue(columnList.get(k));
		
			String cellValue = "";
			for(DrugOrder drO: values)
			{
				Date startDate = drO.getStartDate();
				Date endDate = drO.getDiscontinuedDate();
				if(startDate != null)
				{
					if(cellValue.length() > 0)
					{
						cellValue = cellValue + " , ";
					}
				
					cellValue = cellValue + " Start: " +  new SimpleDateFormat("dd/MM/yyyy").format(startDate); 
				}
			
				if(endDate != null)
				{
					cellValue = cellValue + " End: " +  new SimpleDateFormat("dd/MM/yyyy").format(endDate); 
				}
			}
			DataSetColumn col = new DataSetColumn(columnList.get(k).getLabel() + sheetNumber, columnList.get(k).getLabel(), String.class);
			resultRow.addColumnValue(col, cellValue);
		}

		List<Obs> pregnancy = (List<Obs>)row.getColumnValue(columnList.get(15));
		for(int m = 0; m < 4; m++)
		{
			String columnName = "Pregnancy " + m + sheetNumber;
			DataSetColumn col = new DataSetColumn(columnName, columnName, String.class);
			
			if(pregnancy != null && pregnancy.size() > m)
			{
				Obs pregOb = pregnancy.get(m);
				resultRow.addColumnValue(col,pregOb.getValueAsString(Context.getLocale()));
			}
			else
			{
				resultRow.addColumnValue(col,null);
			}
		}

		DrugOrder initial = (DrugOrder)row.getColumnValue(columnList.get(16));
		String drugName = "Drug Missing";
		try{
			drugName = ((DrugOrder)initial).getDrug().getName();
		}catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
		DataSetColumn initReg = new DataSetColumn("Initial Regimen" + sheetNumber, "Initial Regimen", String.class);
		resultRow.addColumnValue(initReg, drugName);
	
		DataSetColumn firstLine = new DataSetColumn("First Line Changes" + sheetNumber, "First Line Changes", String.class);
		resultRow.addColumnValue(firstLine, firstLineChange);
	
		DataSetColumn secondLine = new DataSetColumn("Second Line Changes"+ sheetNumber, "Second Line Changes", String.class);
		resultRow.addColumnValue(secondLine, secondLineChange);
	    
		int month = startingMonth;

		String drugCellValue = retrieveCorrectMonthsOb(month, drugsValue, startingDate);
		DataSetColumn monthZero = new DataSetColumn("Month 0" + sheetNumber, "Month 0", String.class);
		resultRow.addColumnValue(monthZero, drugCellValue);
	    
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
				if(valueToBeUsed.size() > 0)
				{
					for(Obs ob: valueToBeUsed)
					{
						cellValue = cellValue + " " + ob.getValueAsString(Context.getLocale()) + " " + new SimpleDateFormat("dd/MM/yyyy").format(ob.getObsDatetime()); 
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
						cellValue = cellValue + " " + ob.getValueAsString(Context.getLocale()) + " " + new SimpleDateFormat("MMMM").format(ob.getObsDatetime()); 
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
						cellValue = cellValue + " " + ob.getValueAsString(Context.getLocale()) + " " + new SimpleDateFormat("MMMM").format(ob.getObsDatetime()); 
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
		if((cd4Value != null && cd4Value.size() > 0) || (stageValue != null && stageValue.size() > 0) || (tbValue != null && tbValue.size() > 0))
		{
			
			addPatientRow(rowNumber, month, sheetNumber + 1, row, resultRow, dataset, firstLineChange, secondLineChange, drugsValue, cd4Value, stageValue, tbValue);	
		}
	}

	//to avoid infinite loops we are going to remove all obs that are before the starting date
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
		
				if(diff > 0)
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
		return drugOrders;
	}

	private List<Obs> retrieveCorrect6MonthsOb(int month, List<Obs>obs, Date startingDate)
	{
		List<Obs> obList = new ArrayList<Obs>();
		for(Obs current: obs)	
		{
			int monthsFromStart = calculateMonthsDifference(current.getObsDatetime(), startingDate);
	
			if(monthsFromStart > month - 6 && monthsFromStart <= month)
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
		
				if(o.getDiscontinuedReason() != null)
				{
					discontinuedReasons = discontinuedReasons + o.getDiscontinuedReason().getDisplayString(); 
				}
				else
				{
					discontinuedReasons = discontinuedReasons + "unknown reason"; 
				}
		
				if(o.getDiscontinuedDate() != null)
				{
					discontinuedReasons =  discontinuedReasons + ":" + new SimpleDateFormat("dd/MM/yyyy").format(o.getDiscontinuedDate());
				}
				else
				{
					discontinuedReasons = discontinuedReasons + "unknown date"; 
				}
			}
		}

		return discontinuedReasons;
	}
}
