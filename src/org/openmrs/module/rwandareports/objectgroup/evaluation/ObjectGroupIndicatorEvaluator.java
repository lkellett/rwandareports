package org.openmrs.module.rwandareports.objectgroup.evaluation;

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
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.indicator.ObjectGroupIndicator;
import org.openmrs.module.rwandareports.objectgroup.service.ObjectGroupDefinitionService;
import org.openmrs.module.rwandareports.objectgroup.service.ObjectGroupIndicatorResult;
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;


@Handler(supports={ObjectGroupIndicator.class})
public class ObjectGroupIndicatorEvaluator implements IndicatorEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	
	private int calculateDateDiff(ObjectGroupIndicator.IndicatorType type, Date startDate, Date endDate){
		if (type == null || startDate == null || endDate == null)
			throw new RuntimeException("To do this type of report, you must provide a startdate, enddate, and objectGroupIndicator type.");
		if (type == ObjectGroupIndicator.IndicatorType.PER_DAY){
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
		    ObjectGroupIndicator cid = (ObjectGroupIndicator) indicator;
	    	
		    ObjectGroupIndicatorResult result = new ObjectGroupIndicatorResult();
		    result.setPerDay(cid.getPerHourDenominator());
	    	result.setContext(context);
	    	result.setIndicator(cid);

	    	
	    	
	    	Date startDate = (Date) context.getParameterValue("startDate");
	    	Date endDate = (Date) context.getParameterValue("endDate");
	    	
	    	if (cid.getType() == ObjectGroupIndicator.IndicatorType.PER_DAY || cid.getType() == ObjectGroupIndicator.IndicatorType.PER_WEEKDAYS){
	    		result.setNumDays(calculateDateDiff(cid.getType(), startDate, endDate));
	    	} 
	    	


			
			ObjectGroupDefinitionService cds = Context.getService(ObjectGroupDefinitionService.class);
			
			
//			//resets the base cohort to be all people in the objectGroup
			Cohort baseCohort = context.getBaseCohort();
			if (cid.getLocationFilter() != null) {
				try {
					ObjectGroup locationObjectGroup = cds.evaluate(cid.getLocationFilter(), context);
					if (baseCohort == null) {
						baseCohort = locationObjectGroup.getCohort();
					}
					else {
						baseCohort = Cohort.intersect(baseCohort, locationObjectGroup.getCohort());
					}
				} catch (Exception ex) {
					throw new EvaluationException("locationFilter", ex);
				}
			}
//			
//			// Set Definition Denominator and further restrict base cohort
			if (cid.getDenominator() != null) {
				try {
					ObjectGroup denominatorObjectGroup = cds.evaluate(cid.getDenominator(), context);
					if (baseCohort != null) {
						denominatorObjectGroup = ObjectGroup.intersect(denominatorObjectGroup, baseCohort);
					}
					baseCohort = denominatorObjectGroup.getCohort();
					result.setDenominatorObjectGroup(denominatorObjectGroup);
				} catch (Exception ex) {
					throw new EvaluationException("denominator", ex);
				}
			}
//			
//			// Definition Cohort / Numerator
			ObjectGroup objectGroup;
			try {
				objectGroup = cds.evaluate(cid.getObjectGroupDefinition(), context);
				if (baseCohort != null) {
					 objectGroup = ObjectGroup.intersect(objectGroup, baseCohort);
				}
				result.setObjectGroup(objectGroup);
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
