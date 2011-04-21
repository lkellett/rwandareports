package org.openmrs.module.rwandareports.dataset.evaluator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.indicator.aggregation.CountAggregator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.indicator.dimension.CohortDimensionResult;
import org.openmrs.module.reporting.indicator.dimension.Dimension;
import org.openmrs.module.reporting.indicator.dimension.service.DimensionService;
import org.openmrs.module.reporting.indicator.service.IndicatorService;
import org.openmrs.module.rwandareports.dataset.RollingDailyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.RollingDailyIndicatorDataSetDefinition.RwandaReportsIndicatorAndDimensionColumn;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.definition.SqlEncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.encounter.indicator.IndicatorAndDimensionResult;
import org.openmrs.module.rwandareports.report.definition.RollingDailyPeriodIndicatorReportDefinition;
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;



/**
 * This evaluator class supports the DataSetDefinition and ReportDesign types necessary for building a dynamic indicator calendar.
 * The calendar is rendered according to the enum RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType:
 * if this is set to NONE, then no calendar is built.  Otherwise , the SQL is as follows:
 * 
 * for ENCOUNTER
 * select distinct e.encounter_id, e.patient_id from encounter e, patient p, person per where  e.voided=0 and e.encounter_datetime >= :calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + " and e.encounter_datetime< :calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) +  " and e.location_id = :location and e.patient_id = p.patient_id and p.voided = 0 and e.patient_id = per.person_id and per.voided = 0 " + dsd.getBaseRollingQueryExtension());
 * 
 * for ENCOUNTER_AND_OBS
 * select distinct e.encounter_id, e.patient_id from encounter e, patient p, person per , obs o where o.encounter_id = e.encounter_id and o.voided = 0 and e.voided=0 and e.encounter_datetime >= :calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + " and e.encounter_datetime< :calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) +  " and e.location_id = :location and e.patient_id = p.patient_id and p.voided = 0 and e.patient_id = per.person_id and per.voided = 0 " + dsd.getBaseRollingQueryExtension());
 * 
 * for COHORT: not yet implemented
 * 
 * dsd.getBaseRollingQueryExtension() is retrieved from the RollingDailyPeriodIndicatorReportDefinition itself.  getBaseRollingQueryExtension retrieves the last piece of the query that is used to define a dynamic indicator for each calendar day in the calendar renderer.
 * For example, if the calendar type is ENCOUNTER, you might set baseRollingQueryExtension to be ' and encounter_type = 8' to limit the encounter query used to build the calendar day EncounterGroups.
 * 
 * However, regular cohort or encounter indicators are run normally.
 * 
 * @author dthomas
 *
 */
@Handler(supports={RollingDailyIndicatorDataSetDefinition.class},order=50)
public class RollingDailyIndicatorDataSetEvaluator implements DataSetEvaluator {

	protected Log log = LogFactory.getLog(this.getClass());
	
	private static final SimpleDateFormat sdfIndicatorVar = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	
	public RollingDailyIndicatorDataSetEvaluator() { }
	
	public MapDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		RollingDailyIndicatorDataSetDefinition dsd = (RollingDailyIndicatorDataSetDefinition) dataSetDefinition;
		
		
		if (dsd.getRollingBaseReportQueryType() != RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType.NONE){
				//get startDate and endDate parameters
				Date startDate = (Date) context.getParameterValue("startDate");
				Date endDate = (Date) context.getParameterValue("endDate");
				if (startDate.getTime() >= endDate.getTime())
					throw new IllegalArgumentException("Start date must be before End date.");
		
				
				Calendar calendarStart = RwandaReportsUtil.findSundayBeforeOrEqualToStartDate(startDate);
				//for all of the weeks that we're rendering in the calendar:
				while (calendarStart.getTime().getTime() <= endDate.getTime()){
					
					Calendar weeklyCal = new GregorianCalendar();
		        	weeklyCal.setTime(calendarStart.getTime());
		        	// for the days in the week:
		        	for (int i = 0; i < 7; i++) {
		
		        		Calendar endDateCal = new GregorianCalendar();
						endDateCal.setTime(weeklyCal.getTime());
						endDateCal.add(Calendar.DATE, 1);
						
						
						if (dsd.getRollingBaseReportQueryType() == RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType.ENCOUNTER || dsd.getRollingBaseReportQueryType() == RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType.ENCOUNTER_AND_OBS){
			        		//build the  definition
			        		SqlEncounterGroupDefinition encQuery=new SqlEncounterGroupDefinition();
			        		encQuery.setName("query" + sdfIndicatorVar.format(weeklyCal.getTime()));
			        		if (dsd.getRollingBaseReportQueryType() == RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType.ENCOUNTER)
			        			encQuery.setQuery("select distinct e.encounter_id, e.patient_id from encounter e, patient p, person per where  e.voided=0 and e.encounter_datetime >= :calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + " and e.encounter_datetime< :calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) +  " and e.location_id = :location and e.patient_id = p.patient_id and p.voided = 0 and e.patient_id = per.person_id and per.voided = 0 " + dsd.getBaseRollingQueryExtension());
			        		else  //ENCOUNTER_AND_OBS
			        			encQuery.setQuery("select distinct e.encounter_id, e.patient_id from encounter e, patient p, person per , obs o where o.encounter_id = e.encounter_id and o.voided = 0 and e.voided=0 and e.encounter_datetime >= :calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + " and e.encounter_datetime< :calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) +  " and e.location_id = :location and e.patient_id = p.patient_id and p.voided = 0 and e.patient_id = per.person_id and per.voided = 0 " + dsd.getBaseRollingQueryExtension());
			        		
			        		encQuery.addParameter(new Parameter("location", "location", Location.class));
			        		encQuery.addParameter(new Parameter("calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), Date.class));
			        		encQuery.addParameter(new Parameter("calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()), "calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()), Date.class));
			        		
			        		
			        		EncounterIndicator dailyEncIndicator = EncounterIndicator.newCountIndicator("cal_" + sdfIndicatorVar.format(weeklyCal.getTime()), 
									new Mapped<EncounterGroupDefinition>(encQuery, ParameterizableUtil.createParameterMappings("location=${location},calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + "="+databaseFormat.format(weeklyCal.getTime())+",calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) + "="+databaseFormat.format(endDateCal.getTime()))), 
									null);
			        		dailyEncIndicator.setDescription("cal_" + sdfIndicatorVar.format(weeklyCal.getTime()));
			        		dailyEncIndicator.addParameter(new Parameter("calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), Date.class));
			        		dailyEncIndicator.addParameter(new Parameter("calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), Date.class));
			        		dailyEncIndicator.setAggregator(CountAggregator.class);
							
							Mapped<EncounterIndicator> m = new Mapped<EncounterIndicator>(dailyEncIndicator, ParameterizableUtil.createParameterMappings("location=${location},calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + "="+databaseFormat.format(weeklyCal.getTime())+",calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) + "="+databaseFormat.format(endDateCal.getTime())) );
							dsd.addColumn("cal_" + sdfIndicatorVar.format(weeklyCal.getTime()), "Number of registrations on " + Context.getDateFormat().format(weeklyCal.getTime()), m, new HashMap<String,String>());
							
						} else if (dsd.getRollingBaseReportQueryType() == RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType.COHORT){
							throw new RuntimeException ("Rolling Cohort Base Query not yet implemented.");
						}
						
						
						
						weeklyCal.add(Calendar.DATE, 1);
					
		            }
					
					calendarStart.add(Calendar.DATE, 7);
				}
		}
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		if (context.getBaseCohort() == null) {
			context.setBaseCohort(Context.getPatientSetService().getAllPatients());
		}
		
		IndicatorService is = Context.getService(IndicatorService.class);
		DimensionService ds = Context.getService(DimensionService.class);
		
		MapDataSet ret = new MapDataSet(dataSetDefinition, context);
		for (DataSetColumn dsc : dsd.getColumns()) {
			ret.getMetaData().addColumn(dsc);
		}
		
		// evaluate all dimension options
		//TODO:  encounter dimensions?
		Map<String, Map<String, Cohort>> dimensionCalculationCache = new HashMap<String, Map<String, Cohort>>();
		for (Map.Entry<String, Mapped<? extends Dimension>> e : dsd.getDimensions().entrySet()) {
				String dimensionKey = e.getKey();
				Mapped<? extends Dimension> m = e.getValue();
				if (m.getParameterizable() instanceof CohortDefinitionDimension){
					try {
						CohortDimensionResult dim = (CohortDimensionResult)ds.evaluate(m, context);
						dimensionCalculationCache.put(dimensionKey, dim.getOptionCohorts());
					} catch (Exception ex) {
						throw new EvaluationException("dimension " + dimensionKey, ex);
					}
				} else {
					throw new EvaluationException("Other dimension evaluations besides cohort evaluations are not supported.");
				}
		}
		
		// evaluate unique indicators
		Map<Mapped<? extends Indicator>, IndicatorResult> indicatorCalculationCache = new HashMap<Mapped<? extends Indicator>, IndicatorResult>();
		for (DataSetColumn c : dsd.getColumns()) {
			RwandaReportsIndicatorAndDimensionColumn col = (RwandaReportsIndicatorAndDimensionColumn) c;
			if (!indicatorCalculationCache.containsKey(col.getIndicator())) {
				try {
					
					IndicatorResult result = (IndicatorResult) is.evaluate(col.getIndicator(), context);
					log.debug("Caching indicator: " + col.getIndicator());
					indicatorCalculationCache.put(col.getIndicator(), result);
				} catch (Exception ex) {
					throw new EvaluationException("indicator for column " + col.getLabel() + " (" + col.getName() + ")", ex);
				}
			}
		}
		
		// Populate Data Set columns with Indicator and Dimension Results as defined
		for (DataSetColumn c : dsd.getColumns()) {
			RwandaReportsIndicatorAndDimensionColumn col = (RwandaReportsIndicatorAndDimensionColumn) c;
			// get this indicator result from the cache
			IndicatorResult result = indicatorCalculationCache.get(col.getIndicator());
			// get its value taking dimensions into account
			IndicatorAndDimensionResult resultWithDimensions = new IndicatorAndDimensionResult(result, context);

			if (col.getDimensionOptions() != null) {
				for (Map.Entry<String, String> e : col.getDimensionOptions().entrySet()) {
					log.debug("looking up dimension: " + e.getKey() + " = " + e.getValue());
					CohortDefinitionDimension dimension = (CohortDefinitionDimension) dsd.getDimension(e.getKey()).getParameterizable();
					Cohort dimensionCohort = dimensionCalculationCache.get(e.getKey()).get(e.getValue());
					resultWithDimensions.addDimensionResult(dimension, dimensionCohort);
				}
			}
			ret.addData(col, resultWithDimensions);
		}
		
		return ret;
	}
	
}
