package org.openmrs.module.rwandareports.util;

import java.util.Date;
import java.util.Map;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.aggregation.Aggregator;
import org.openmrs.module.reportingobjectgroup.objectgroup.definition.ObjectGroupDefinition;
import org.openmrs.module.reportingobjectgroup.objectgroup.definition.SqlObjectGroupDefinition;
import org.openmrs.module.reportingobjectgroup.objectgroup.indicator.ObjectGroupIndicator;

public class Indicators {

	public static CohortIndicator newCountIndicator(String name, CohortDefinition cohort, Map<String, Object> map) {
		CohortIndicator i = CohortIndicator.newCountIndicator(name, new Mapped<CohortDefinition>(cohort, map), null);
		i.addParameter(new Parameter("startDate", "Start date", Date.class));
		i.addParameter(new Parameter("endDate", "End date", Date.class));
		
		return i;
	}
	
	public static CohortIndicator newFractionIndicator(String name, CohortDefinition numerator, Map<String, Object> numeratorMap,
	                                            CohortDefinition denominator, Map<String, Object> denominatorMap) {
		CohortIndicator i = CohortIndicator.newFractionIndicator(name,
		    new Mapped<CohortDefinition>(numerator, numeratorMap),
		    new Mapped<CohortDefinition>(denominator, denominatorMap), null);
		i.addParameter(new Parameter("startDate", "Start date", Date.class));
		i.addParameter(new Parameter("endDate", "End date", Date.class));
		
		return i;
	}
	
	public static CohortIndicator newLogicIndicator(String name, CohortDefinition logic, Map<String, Object> map,
	                                         Class<? extends Aggregator> aggregator, String logicName) {
		CohortIndicator i = CohortIndicator.newLogicIndicator(name, new Mapped<CohortDefinition>(logic, map), null,
		    aggregator, logicName);
		i.addParameter(new Parameter("startDate", "Start date", Date.class));
		i.addParameter(new Parameter("endDate", "End date", Date.class));
		
		return i;
	}
	
	public static CohortIndicator newCohortIndicator(String name, CohortDefinition cohort, Map<String, Object> map) {
		CohortIndicator i = new CohortIndicator();
		i.setName(name);
		i.setCohortDefinition(new Mapped<CohortDefinition>(cohort, map));
		i.addParameter(new Parameter("startDate", "Start date", Date.class));
		i.addParameter(new Parameter("endDate", "End date", Date.class));
		
		return i;
	}
	
	public static ObjectGroupIndicator newFractionIndicatorObjectGroupIndicator(String name, ObjectGroupDefinition numerator,
	                                                                     Map<String, Object> numeratorMap,
	                                                                     ObjectGroupDefinition denominator,
	                                                                     Map<String, Object> denominatorMap) {
		ObjectGroupIndicator i = ObjectGroupIndicator.newFractionIndicator(name, new Mapped<ObjectGroupDefinition>(
		        numerator, numeratorMap), new Mapped<ObjectGroupDefinition>(denominator, denominatorMap), null);
		i.addParameter(new Parameter("startDate", "Start date", Date.class));
		i.addParameter(new Parameter("endDate", "End date", Date.class));
		
		return i;
	}
	
	public static ObjectGroupIndicator newDailyDivisionIndicatorPerWeekDays(String name, ObjectGroupDefinition definition,
	                                                                 Map<String, Object> mappings, Integer value) {
		ObjectGroupIndicator peakHoursAndPeakDaysIndicator = ObjectGroupIndicator.newDailyDivisionIndicator(
		    "peakHoursIndicator", new Mapped<ObjectGroupDefinition>(definition, mappings), value,
		    ObjectGroupIndicator.IndicatorType.PER_WEEKDAYS, null);
		peakHoursAndPeakDaysIndicator.setName("peakHoursAndPeakDaysIndicator");
		peakHoursAndPeakDaysIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		peakHoursAndPeakDaysIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		peakHoursAndPeakDaysIndicator.setPerHourDenominator(2);
		return peakHoursAndPeakDaysIndicator;
	}
	
	public static ObjectGroupIndicator newCountIndicatorObjectGroupIndicator(String name, SqlObjectGroupDefinition definition,
	                                                                  Map<String, Object> mappings) {
		ObjectGroupIndicator countIndicator = ObjectGroupIndicator.newCountIndicator(name,
		    new Mapped<SqlObjectGroupDefinition>(definition, mappings), null);
		countIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		countIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		
		return countIndicator;
	}
	
	public static ObjectGroupIndicator objectGroupIndicator(String name, SqlObjectGroupDefinition definition,
	                                                 Map<String, Object> mappings) {
		ObjectGroupIndicator objectIndicator = new ObjectGroupIndicator();
		objectIndicator.setName("femalePatientsrequestPrimCareInRegistrationIndicator");
		objectIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		objectIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		objectIndicator.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(definition, mappings));
		return objectIndicator;
	}
}
