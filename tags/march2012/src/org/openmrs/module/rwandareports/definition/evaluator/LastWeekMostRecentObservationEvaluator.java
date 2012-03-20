package org.openmrs.module.rwandareports.definition.evaluator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rwandareports.definition.LastWeekMostRecentObservation;

@Handler(supports={LastWeekMostRecentObservation.class})
public class LastWeekMostRecentObservationEvaluator implements RowPerPatientDataEvaluator{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {
	    
		ObservationResult par = new ObservationResult(patientData, context);
		LastWeekMostRecentObservation pd = (LastWeekMostRecentObservation)patientData;
	
		Concept c = pd.getConcept();
		
		List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(pd.getPatient(), c);
		
		Date endDate=(Date)context.getParameterValue("endDate");
		//go back one week
		Calendar c1 = Calendar.getInstance();
		c1.setTime(endDate);
		c1.add(Calendar.DATE, -7);
		
		List<Obs> obsToRemove = new ArrayList<Obs>();
		
		for(Obs o:obs)
		{ 
			if(o.getValueDatetime().compareTo(endDate) > 0 || c1.getTime().compareTo(o.getValueDatetime()) > 0)
				obsToRemove.add(o);
		}
		for(Obs o:obsToRemove)
		{ 
			obs.remove(o);
		}
		
		
		Obs ob = null;
        if(obs != null)
        {
			//find the most recent value
			for(Obs o:obs)
			{ 
				if (ob == null
	                     || o.getObsDatetime().compareTo(ob.getObsDatetime()) > 0)
				 {
					 	if(pd.isIncludeNull())
					 	{
					 		ob = o;
					 	}
					 	else
					 	{
					 		String value = o.getValueAsString(Context.getLocale());
					 		if(value != null && value.trim().length() > 0)
					 		{
					 			ob = o;
					 		}
					 	}
				 }
			}
        }

        if(ob != null)
        {
        	String resultValue = ob.getValueAsString(Context.getLocale());
        	if(pd.getFilter() != null)
        	{
        		resultValue = (String)pd.getFilter().filter(resultValue);
        	}
			par.setValue(resultValue);
			par.setDateOfObservation(ob.getObsDatetime());
		}		
		
		return par;
    }
}
