package org.openmrs.module.rwandareports.definition.evaluator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition.CohortIndicatorAndDimensionColumn;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicatorResult;
import org.openmrs.module.reporting.indicator.aggregation.CountAggregator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.indicator.dimension.CohortDimensionResult;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.module.reporting.indicator.dimension.service.DimensionService;
import org.openmrs.module.reporting.indicator.service.IndicatorService;
import org.openmrs.module.rwandareports.dataset.RollingDailyCohortIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;


@Handler(supports={RollingDailyCohortIndicatorDataSetDefinition.class},order=0)
public class RollingDailyCohortIndicatorDataSetEvaluator implements DataSetEvaluator {

	protected Log log = LogFactory.getLog(this.getClass());
	
	private static final SimpleDateFormat sdfIndicatorVar = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public RollingDailyCohortIndicatorDataSetEvaluator() { }
	
	public MapDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		RollingDailyCohortIndicatorDataSetDefinition dsd = (RollingDailyCohortIndicatorDataSetDefinition) dataSetDefinition;
		
		// get registration encounter types:
		int registrationEncTypeId=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("primarycarereport.registration.encountertypeid"));
		List<EncounterType> registrationEncounterType=new ArrayList<EncounterType>();
		EncounterType registration=Context.getEncounterService().getEncounterType(registrationEncTypeId);
		if (registration == null)
			throw new RuntimeException("Are you sure the global property primarycarereport.registration.encountertypeid is set correctly?");
		registrationEncounterType.add(registration);
		
		//get startDate and endDate parameters
		Date startDate = (Date) context.getParameterValue("startDate");
		Date endDate = (Date) context.getParameterValue("endDate");
		if (startDate.getTime() >= endDate.getTime())
			throw new IllegalArgumentException("Start date must be before End date.");
		Location location = (Location) context.getParameterValue("location");
		
		
		
		
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
				
        		//build the cohort definition
        		SqlCohortDefinition cohortQuery=new SqlCohortDefinition();
        		cohortQuery.setName("query" + sdfIndicatorVar.format(weeklyCal.getTime()));
        		//cohortQuery.setQuery("select distinct patient_id from encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0 and encounter_datetime >= :calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + " and encounter_datetime< :calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()));
        		String query = "select distinct patient_id from encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0 and encounter_datetime >= '" + databaseFormat.format(weeklyCal.getTime()) + "' and encounter_datetime<  '"+databaseFormat.format(endDateCal.getTime())+"'  ";
        		if (location != null)
        			query += " and e.location_id = " + location.getLocationId().toString();
        		cohortQuery.setQuery(query);
        		
        		
        		//cohortQuery.addParameter(new Parameter("calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), String.class));
        		//cohortQuery.addParameter(new Parameter("calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), String.class));
        		
				CohortIndicator dailyCohortIndicator = new CohortIndicator();
				dailyCohortIndicator.setName("cal_" + sdfIndicatorVar.format(weeklyCal.getTime()));
				dailyCohortIndicator.setDescription("cal_" + sdfIndicatorVar.format(weeklyCal.getTime()));
				//dailyCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
				//dailyCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
				//dailyCohortIndicator.addParameter(new Parameter("calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), String.class));
				//dailyCohortIndicator.addParameter(new Parameter("calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), String.class));
				dailyCohortIndicator.setAggregator(CountAggregator.class);
				
				//dailyCohortIndicator.setCohortDefinition(new Mapped<CohortDefinition>(cohortQuery,ParameterizableUtil.createParameterMappings("calStartDate"+sdfIndicatorVar.format(weeklyCal.getTime())+"='" + databaseFormat.format(weeklyCal.getTime()) + "',calEndDate"+sdfIndicatorVar.format(weeklyCal.getTime())+"='"+ databaseFormat.format(endDateCal.getTime()) +"'")));
				dailyCohortIndicator.setCohortDefinition(new Mapped<CohortDefinition>(cohortQuery,new HashMap<String, Object>()));
				//Mapped<CohortIndicator> m = new Mapped<CohortIndicator>(dailyCohortIndicator, IndicatorUtil.getDefaultParameterMappings());
				//Mapped<CohortIndicator> m = new Mapped<CohortIndicator>(dailyCohortIndicator, ParameterizableUtil.createParameterMappings("calStartDate"+sdfIndicatorVar.format(weeklyCal.getTime())+"='" + databaseFormat.format(weeklyCal.getTime()) + "',calEndDate"+sdfIndicatorVar.format(weeklyCal.getTime())+"='"+ databaseFormat.format(endDateCal.getTime()) +"'"));
				Mapped<CohortIndicator> m = new Mapped<CohortIndicator>(dailyCohortIndicator, new HashMap<String, Object>() );
				dsd.addColumn("cal_" + sdfIndicatorVar.format(weeklyCal.getTime()), "Number of registrations on " + Context.getDateFormat().format(weeklyCal.getTime()), m, new HashMap<String,String>());
				//context.addParameterValue("calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "'" + databaseFormat.format(weeklyCal.getTime()) + "'");
        		//context.addParameterValue("calEndDate" + sdfIndicatorVar.format(weeklyCal.getTime()), "'" + databaseFormat.format(endDateCal.getTime()) + "'" );
        		
				weeklyCal.add(Calendar.DATE, 1);
			
            }
			
			calendarStart.add(Calendar.DATE, 7);
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
		Map<String, Map<String, Cohort>> dimensionCalculationCache = new HashMap<String, Map<String, Cohort>>();
		for (Map.Entry<String, Mapped<CohortDefinitionDimension>> e : dsd.getDimensions().entrySet()) {
			String dimensionKey = e.getKey();
			try {
				CohortDimensionResult dim = (CohortDimensionResult)ds.evaluate(e.getValue(), context);
				dimensionCalculationCache.put(dimensionKey, dim.getOptionCohorts());
			} catch (Exception ex) {
				throw new EvaluationException("dimension " + dimensionKey, ex);
			}
		}
		
		// evaluate unique indicators
		Map<Mapped<? extends CohortIndicator>, CohortIndicatorResult> indicatorCalculationCache = new HashMap<Mapped<? extends CohortIndicator>, CohortIndicatorResult>();
		for (DataSetColumn c : dsd.getColumns()) {
			CohortIndicatorAndDimensionColumn col = (CohortIndicatorAndDimensionColumn) c;
			if (!indicatorCalculationCache.containsKey(col.getIndicator())) {
				try {
					
					CohortIndicatorResult result = (CohortIndicatorResult) is.evaluate(col.getIndicator(), context);
					log.debug("Caching indicator: " + col.getIndicator());
					indicatorCalculationCache.put(col.getIndicator(), result);
				} catch (Exception ex) {
					throw new EvaluationException("indicator for column " + col.getLabel() + " (" + col.getName() + ")", ex);
				}
			}
		}
		
		// Populate Data Set columns with Indicator and Dimension Results as defined
		for (DataSetColumn c : dsd.getColumns()) {
			CohortIndicatorAndDimensionColumn col = (CohortIndicatorAndDimensionColumn) c;
			// get this indicator result from the cache
			CohortIndicatorResult result = indicatorCalculationCache.get(col.getIndicator());
			// get its value taking dimensions into account
			CohortIndicatorAndDimensionResult resultWithDimensions = new CohortIndicatorAndDimensionResult(result, context);

			if (col.getDimensionOptions() != null) {
				for (Map.Entry<String, String> e : col.getDimensionOptions().entrySet()) {
					log.debug("looking up dimension: " + e.getKey() + " = " + e.getValue());
					CohortDefinitionDimension dimension = dsd.getDimension(e.getKey()).getParameterizable();
					Cohort dimensionCohort = dimensionCalculationCache.get(e.getKey()).get(e.getValue());
					resultWithDimensions.addDimensionResult(dimension, dimensionCohort);
				}
			}
			ret.addData(col, resultWithDimensions);
		}
		
		return ret;
	}
	
}
