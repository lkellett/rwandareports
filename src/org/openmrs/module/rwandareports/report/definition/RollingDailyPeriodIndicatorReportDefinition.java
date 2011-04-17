package org.openmrs.module.rwandareports.report.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.util.IndicatorUtil;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.rwandareports.dataset.RollingDailyCohortIndicatorDataSetDefinition;

/**
 * 
 * Basically the same as PeriodIndicatorReportDefinition, except that you can store CohortIndicators for later use.
 *
 */
public class RollingDailyPeriodIndicatorReportDefinition  extends PeriodIndicatorReportDefinition {

		public RollingDailyPeriodIndicatorReportDefinition(){
			super();
		}
	
		private List<SqlCohortDefinition> dailyRollingIndicators =  new ArrayList<SqlCohortDefinition>();

		public List<SqlCohortDefinition> getDailyRollingIndicators() {
			return dailyRollingIndicators;
		}

		public void setDailyRollingIndicators(
				List<SqlCohortDefinition> dailyRollingIndicators) {
			this.dailyRollingIndicators = dailyRollingIndicators;
		}
		
		public void addSqlCohortDefinition(SqlCohortDefinition scd){
			this.dailyRollingIndicators.add(scd);
		}
		
		
		/**
		 * Ensure this report has a data set definition
		 */
		@Override
		public void setupDataSetDefinition() {
			if (this.getIndicatorDataSetDefinition() == null) {
				
				// Create new dataset definition 
				RollingDailyCohortIndicatorDataSetDefinition dataSetDefinition = new RollingDailyCohortIndicatorDataSetDefinition();
				dataSetDefinition.setName(getName() + " Data Set");
				dataSetDefinition.addParameter(ReportingConstants.START_DATE_PARAMETER);
				dataSetDefinition.addParameter(ReportingConstants.END_DATE_PARAMETER);
				dataSetDefinition.addParameter(ReportingConstants.LOCATION_PARAMETER);
				
				// Add dataset definition to report definition
				addDataSetDefinition(DEFAULT_DATASET_KEY, dataSetDefinition, IndicatorUtil.getDefaultParameterMappings());
			}
	    }
		
		
		/**
		 * Add a period cohort indicator to the report definition with no dimension options
		 * @param CohortIndicator
		 */
		@Override 
		public void addIndicator(String key, String displayName, CohortIndicator indicator, Map<String,String> dimensionOptions) { 
			Mapped<CohortIndicator> m = new Mapped<CohortIndicator>(indicator, IndicatorUtil.getDefaultParameterMappings());
			getIndicatorDataSetDefinition().addColumn(key, displayName, m, dimensionOptions);		
		} 
		
		/**
		 * @return the indicator dataset definition from the report.  There's only one of these
		 * dataset definitions, so we store it in the dataset definition map with a default
		 * key.
		 */
		@Override 
		@SuppressWarnings("unchecked")
		public RollingDailyCohortIndicatorDataSetDefinition getIndicatorDataSetDefinition() {
			Mapped<RollingDailyCohortIndicatorDataSetDefinition> mappedDataSetDefinition = 
				(Mapped<RollingDailyCohortIndicatorDataSetDefinition>) getDataSetDefinitions().get(DEFAULT_DATASET_KEY);
			if (mappedDataSetDefinition != null) {
				return mappedDataSetDefinition.getParameterizable();
			}
			return null;
		}
		
}
