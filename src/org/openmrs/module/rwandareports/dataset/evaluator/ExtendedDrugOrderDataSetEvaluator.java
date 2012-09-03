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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.ExtendedDrugOrder;
import org.openmrs.module.orderextension.api.OrderExtensionService;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rwandareports.dataset.ExtendedDrugOrderDataSetDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

/**
 * The logic that evaluates a {@link EncounterDataSetDefinition} and produces an {@link DataSet}
 */
@Handler(supports=ExtendedDrugOrderDataSetDefinition.class)
public class ExtendedDrugOrderDataSetEvaluator implements DataSetEvaluator {

	protected Log log = LogFactory.getLog(this.getClass());
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

	/**
	 * Public constructor
	 */
	public ExtendedDrugOrderDataSetEvaluator() { }
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 */
	@SuppressWarnings("unchecked")
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		Concept bsa = gp.getConcept(GlobalPropertiesManagement.BSA_CONCEPT);
		
		ExtendedDrugOrderDataSetDefinition dsd = (ExtendedDrugOrderDataSetDefinition) dataSetDefinition;
		context = ObjectUtil.nvl(context, new EvaluationContext());
		
		SimpleDataSet dataSet = new SimpleDataSet(dsd, context);
		
		List<ExtendedDrugOrder> orders = new ArrayList<ExtendedDrugOrder>();
		
		if(dsd.getDrugRegimen() != null)
		{
			DrugRegimen regimen =  Context.getService(OrderExtensionService.class).getDrugRegimen(Integer.parseInt(dsd.getDrugRegimen()));
			if(dsd.getIndication() != null)
			{
				for(ExtendedDrugOrder order: regimen.getMembers())
				{
					if(order.getIndication() != null && order.getIndication().equals(dsd.getIndication()))
					{
						orders.add(order);
					}
				}
			}
			else
			{
				orders.addAll(regimen.getMembers());
			}
			
			DataSetColumn start = new DataSetColumn("startDate", "startDate", Date.class); 
			dataSet.getMetaData().addColumn(start);
			
			DataSetColumn drug = new DataSetColumn("drug", "drug", String.class); 
			dataSet.getMetaData().addColumn(drug);
			
			DataSetColumn dose = new DataSetColumn("dose", "dose", String.class); 
			dataSet.getMetaData().addColumn(dose);
			
			DataSetColumn actualDose = new DataSetColumn("actualDose", "actualDose", String.class); 
			dataSet.getMetaData().addColumn(actualDose);
			
			DataSetColumn route = new DataSetColumn("route", "route", String.class); 
			dataSet.getMetaData().addColumn(route);
			
			DataSetColumn infInst = new DataSetColumn("infusionInstructions", "infusionInstructions", String.class); 
			dataSet.getMetaData().addColumn(infInst);
			
			DataSetColumn freq = new DataSetColumn("frequency", "frequency", String.class); 
			dataSet.getMetaData().addColumn(freq);
			
			DataSetColumn instructions = new DataSetColumn("instructions", "instructions", String.class); 
			dataSet.getMetaData().addColumn(instructions);
			
			DataSetColumn indication = new DataSetColumn("indication", "indication", String.class); 
			dataSet.getMetaData().addColumn(indication);
			
			Collections.sort(orders, new Comparator<ExtendedDrugOrder>() {
               
                public int compare(ExtendedDrugOrder left, ExtendedDrugOrder right) {
                    return left.getStartDate().compareTo(right.getStartDate());
                }
            });
			for (ExtendedDrugOrder edo : orders) {
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
				dataSet.addColumnValue(edo.getId(), start, dateFormat.format(edo.getStartDate()));
				
				String drugDisplay = "";
				if(edo.getDrug() != null)
				{
					drugDisplay = edo.getDrug().getName();
				}
				else if(edo.getConcept() != null)
				{
					drugDisplay = edo.getConcept().getDisplayString();
				}
				dataSet.addColumnValue(edo.getId(), drug, drugDisplay);
				
				String doseDisplay = "";
				String actualDoseDisplay = "";
				DecimalFormat f = new DecimalFormat("0.#");
				if(edo.getDose() != null && edo.getUnits() != null)
				{
					if(edo.getUnits().equals("mg/m2"))
					{
						doseDisplay = f.format(edo.getDose());
					}
					else
					{
						doseDisplay = f.format(edo.getDose()) + " (" + edo.getUnits() + ")";
					}	
					
					if(edo.getUnits().equals("mg/m2"))
					{
						if(regimen.getCycleNumber() > 1)
						{
							Patient patient = edo.getPatient();
							List<Obs> bsaValues = Context.getObsService().getObservationsByPersonAndConcept(patient, bsa);
							
							if(bsaValues != null && bsaValues.size() > 0)
							{
								Obs recent = null;
								for(Obs o: bsaValues)
								{
									if(recent == null || recent.getObsDatetime().before(o.getObsDatetime()))
									{
										recent = o;
									}
								}
								
								double calcDose = edo.getDose()*recent.getValueNumeric();
								if(edo.getDrug() != null && edo.getDrug().getMaximumDailyDose() != null && calcDose > edo.getDrug().getMaximumDailyDose())
								{
									calcDose = edo.getDrug().getMaximumDailyDose();
								}
								actualDoseDisplay = f.format(calcDose);
							}
						}
					}
				}
				dataSet.addColumnValue(edo.getId(), dose, doseDisplay);
				dataSet.addColumnValue(edo.getId(), actualDose, actualDoseDisplay);
				
				String routeDisplay = "";
				if(edo.getRoute() != null)
				{
					routeDisplay = edo.getRoute().getDisplayString();
				}
				dataSet.addColumnValue(edo.getId(), route, routeDisplay);
				
				String infInstDisplay = "";
				if(edo.getAdministrationInstructions() != null)
				{
					infInstDisplay = edo.getAdministrationInstructions();
				}
				dataSet.addColumnValue(edo.getId(), infInst, infInstDisplay);
				
				String freqDisplay = "";
				if(edo.getFrequency() != null)
				{
					freqDisplay = edo.getFrequency();
					
					int length = 0;
					if(edo.getDiscontinuedDate() != null) {
						length = calculateDaysDifference(edo.getDiscontinuedDate(), edo.getStartDate());
						
					}
					if(edo.getAutoExpireDate() != null) {
						length =  calculateDaysDifference(edo.getAutoExpireDate(), edo.getStartDate());
						
					}
					
					if(length > 1)
					{
						freqDisplay = edo.getFrequency() + " for " + Integer.toString(length) + " days";
					}
				}
				dataSet.addColumnValue(edo.getId(), freq, freqDisplay);
				
				String instructionsDisplay = "";
				if(edo.getInstructions() != null)
				{
					instructionsDisplay = edo.getInstructions();
				}
				dataSet.addColumnValue(edo.getId(), instructions, instructionsDisplay);	
			}
		}
		return dataSet;
	}
	
	private int calculateDaysDifference(Date observation, Date startingDate)
	{
		long milis1 = observation.getTime();
		long milis2 = startingDate.getTime();
		
		long diff = milis1 - milis2;
		
		long diffDays = diff / (24 * 60 * 60 * 1000);
		
	
		return (int)diffDays + 1;
	}
}
