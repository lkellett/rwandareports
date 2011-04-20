package org.openmrs.module.rwandareports.encounter.evaluation;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.indicator.evaluator.IndicatorEvaluator;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.encounter.service.EncounterGroupDefinitionService;
import org.openmrs.module.rwandareports.encounter.service.EncounterIndicatorResult;
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;


@Handler(supports={EncounterIndicator.class})
public class EncounterIndicatorEvaluator implements IndicatorEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	
	private int calculateDateDiff(EncounterIndicator.IndicatorType type, Date startDate, Date endDate){
		if (type == null || startDate == null || endDate == null)
			throw new RuntimeException("To do this type of report, you must provide a startdate, enddate, and encounterIndicator type.");
		if (type == EncounterIndicator.IndicatorType.PER_DAY){
			return (int) (((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000)));		
		} else {
			 Calendar cal = new GregorianCalendar();
			 cal.setTime(startDate);
			 int count = 0;
			 while (cal.getTime().getTime() <= endDate.getTime()){
				 if (RwandaReportsUtil.isWeekday(cal.getTime()))
					 count ++;
				 
				 cal.add(Calendar.DATE, 1);
			 }
			 
			 return count;
		}

	}
	
	 public IndicatorResult evaluate(Indicator indicator, EvaluationContext context) throws EvaluationException {
		    EncounterIndicator cid = (EncounterIndicator) indicator;
	    	
		    EncounterIndicatorResult result = new EncounterIndicatorResult();
		    result.setPerDay(cid.getPerHourDenominator());
	    	result.setContext(context);
	    	result.setIndicator(cid);

	    	
	    	
	    	Date startDate = (Date) context.getParameterValue("startDate");
	    	Date endDate = (Date) context.getParameterValue("endDate");
	    	
	    	if (cid.getType() == EncounterIndicator.IndicatorType.PER_DAY || cid.getType() == EncounterIndicator.IndicatorType.PER_WEEKDAYS){
	    		result.setNumDays(calculateDateDiff(cid.getType(), startDate, endDate));
	    	} 
	    	


			
			EncounterGroupDefinitionService cds = Context.getService(EncounterGroupDefinitionService.class);
			
			// Determine Base Cohort from LocationFilter and EvaluationContext base cohort
			
			
//			//resets the base cohort to be all people in the encounterGroup
			Cohort baseCohort = context.getBaseCohort();
			if (cid.getLocationFilter() != null) {
				try {
					EncounterGroup locationCohort = cds.evaluate(cid.getLocationFilter(), context);
					if (baseCohort == null) {
						baseCohort = locationCohort.getCohort();
					}
					else {
						baseCohort = Cohort.intersect(baseCohort, locationCohort.getCohort());
					}
				} catch (Exception ex) {
					throw new EvaluationException("locationFilter", ex);
				}
			}
//			
//			// Set Definition Denominator and further restrict base cohort
			if (cid.getDenominator() != null) {
				try {
					EncounterGroup denominatorEncounterGroup = cds.evaluate(cid.getDenominator(), context);
					if (baseCohort != null) {
						denominatorEncounterGroup = EncounterGroup.intersect(denominatorEncounterGroup, baseCohort);
					}
					baseCohort = denominatorEncounterGroup.getCohort();
					result.setDenominatorEncounterGroup(denominatorEncounterGroup);
				} catch (Exception ex) {
					throw new EvaluationException("denominator", ex);
				}
			}
//			
//			// Definition Cohort / Numerator
			EncounterGroup encounterGroup;
			try {
				encounterGroup = cds.evaluate(cid.getEncounterGroupDefinition(), context);
				if (baseCohort != null) {
					 encounterGroup = EncounterGroup.intersect(encounterGroup, baseCohort);
				}
				result.setEncounterGroup(encounterGroup);
			} catch (Exception ex) {
				throw new EvaluationException("numerator/cohort", ex);
			}
//			
//			// Evaluate Logic Criteria
//	    	if (cid.getLogicExpression() != null) {
//	    		try {
//	    			LogicCriteria criteria = Context.getLogicService().parseString(cid.getLogicExpression());
//	    			maybeSetIndexDate(criteria, context);
//	    			Map<Integer, Result> logicResults = Context.getLogicService().eval(cohort, criteria);
//	    			for (Integer memberId : logicResults.keySet()) {
//	    				result.addLogicResult(memberId, logicResults.get(memberId).toNumber());
//	    			}
//	    		}
//	    		catch(LogicException e) {
//	    			throw new EvaluationException("logic expression: " + cid.getLogicExpression(), e);
//	    		}
//	    	}
			
		
			
			return result;
	    }

//		/**
//	     * If context has a parameter called (in order) any of [indexDate, date, endDate, startDate] then
//	     * the logic criteria's index date will be set to the value of that parameter.
//	     * 
//	     * Note that criteria should be a LogicCriteria. I'm using reflection so this code works on both 1.5
//	     * (where LogicCriteria is a class) and 1.6+ (where it's an interface)
//	     * 
//	     * @param criteria
//	     * @param context
//	     */
//	    private static String[] possibilities = new String[] { "indexDate", "date", "endDate", "startDate" };
//	    private void maybeSetIndexDate(Object criteria, EvaluationContext context) {
//	    	for (String p : possibilities) {
//	    		if (context.containsParameter(p)) {
//	    			Date date = (Date) context.getParameterValue(p);
//	    			try {
//	    				criteria.getClass().getMethod("asOf", Date.class).invoke(criteria, date);
//	    			} catch (Exception ex) {
//	    				throw new RuntimeException(ex);
//	    			}
//	    			return;
//	    		}
//	    	}
//	    }
	 
}
