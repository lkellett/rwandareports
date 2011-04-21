package org.openmrs.module.rwandareports.report.definition;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.indicator.util.IndicatorUtil;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.rwandareports.dataset.RollingDailyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.encounter.indicator.EncounterIndicator;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 * Basically the same as PeriodIndicatorReportDefinition, except that you can store CohortIndicators for later use.
 *
 */
public class RollingDailyPeriodIndicatorReportDefinition  extends ReportDefinition {

	
	    public static final String DEFAULT_DATASET_KEY = "defaultDataSet";
		 
		 
	    public enum RollingBaseReportQueryType {
	    	//results in the following base query being run for all days:
	    	ENCOUNTER,  //select distinct e.encounter_id, e.patient_id from encounter e, patient p, person per where  e.voided=0 and e.encounter_datetime >= :calStartDate" + sdfIndicatorVar.format(weeklyCal.getTime()) + " and e.encounter_datetime< :calEndDate"+ sdfIndicatorVar.format(weeklyCal.getTime()) +  " and e.location_id = :location and e.patient_id = p.patient and p.voided = 0 and e.patient_id = per.person_id and per.voided = 0 
	    	ENCOUNTER_AND_OBS, //TODO: not implemented -- will be more processor intensive, will include join to obs table on encounter_id
		 	COHORT, //TODO: not implemented
		 	NONE //don't do anything
		}
		 
		private String baseRollingQueryExtension = "";
		private RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType rollingBaseReportQueryType = RollingBaseReportQueryType.NONE;





		public RollingDailyPeriodIndicatorReportDefinition(){
			super();
			// add parameters for startDate, endDate, and location
			addParameter(ReportingConstants.START_DATE_PARAMETER);
			addParameter(ReportingConstants.END_DATE_PARAMETER);
			addParameter(ReportingConstants.LOCATION_PARAMETER);
		}
		
		public RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType getRollingBaseReportQueryType() {
			return rollingBaseReportQueryType;
		}


		public void setRollingBaseReportQueryType(
				RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType rollingBaseReportQueryType) {
			this.rollingBaseReportQueryType = rollingBaseReportQueryType;
		}


		
		
		public String getBaseRollingQueryExtension() {
			return baseRollingQueryExtension;
		}

		public void setBaseRollingQueryExtension(String baseRollingQueryExtension) {
			this.baseRollingQueryExtension = baseRollingQueryExtension;
		}

		/**
		 * Ensure this report has a data set definition
		 */
		public void setupDataSetDefinition() {
			if (this.getIndicatorDataSetDefinition() == null) {
				
				// Create new dataset definition 
				RollingDailyIndicatorDataSetDefinition dataSetDefinition = new RollingDailyIndicatorDataSetDefinition();
				dataSetDefinition.setBaseRollingQueryExtension(baseRollingQueryExtension);
				dataSetDefinition.setRollingBaseReportQueryType(this.getRollingBaseReportQueryType());
//				dataSetDefinition.setName(getName() + " Data Set");
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
		public void addIndicator(String key, String displayName, CohortIndicator indicator, Map<String,String> dimensionOptions) { 
			Mapped<CohortIndicator> m = new Mapped<CohortIndicator>(indicator, IndicatorUtil.getDefaultParameterMappings());
			getIndicatorDataSetDefinition().addColumn(key, displayName, m, dimensionOptions);		
		} 
		
		public void addIndicator(String key, String displayName, EncounterIndicator indicator, Map<String,String> dimensionOptions) { 
			Mapped<EncounterIndicator> m = new Mapped<EncounterIndicator>(indicator, IndicatorUtil.getDefaultParameterMappings());
			getIndicatorDataSetDefinition().addColumn(key, displayName, m, dimensionOptions);		
		} 
		
		/**
		 * @return the indicator dataset definition from the report.  There's only one of these
		 * dataset definitions, so we store it in the dataset definition map with a default
		 * key.
		 */
		@SuppressWarnings("unchecked")
		public RollingDailyIndicatorDataSetDefinition getIndicatorDataSetDefinition() {
			Mapped<RollingDailyIndicatorDataSetDefinition> mappedDataSetDefinition = 
				(Mapped<RollingDailyIndicatorDataSetDefinition>) getDataSetDefinitions().get(DEFAULT_DATASET_KEY);
			if (mappedDataSetDefinition != null) {
				return mappedDataSetDefinition.getParameterizable();
			}
			return null;
		}

		/**
		 * Add a period cohort indicator to the report definition with no dimension categories.
		 * @param periodCohortIndicator
		 */
		public void addIndicator(CohortIndicator indicator) {				
			addIndicator(indicator.getUuid(), indicator.getName(), indicator, new HashMap<String,String>());
		}
		
		/**
		 * Add a period cohort indicator to the report definition with no dimension categories.
		 * @param CohortIndicator
		 */
		public void addIndicator(String uniqueName, String displayName, CohortIndicator indicator) {				
			addIndicator(uniqueName, displayName, indicator, new HashMap<String,String>());
		}
		
		
		public void addIndicator(String uniqueName, String displayName, EncounterIndicator indicator) {				
			addIndicator(uniqueName, displayName, indicator, new HashMap<String,String>());
		}
		 

		/**
		 * Add a period cohort indicator to the report definition with dimension categories.
		 * @param CohortIndicator
		 */
		public void addIndicator(CohortIndicator indicator, String dimensionCategories) { 		
			addIndicator(indicator.getUuid(), indicator.getName(), indicator, dimensionCategories);
		}	
		
		/**
		 * Add a period cohort indicator to the report definition with dimension categories.
		 * @param CohortIndicator
		 */
		public void addIndicator(String uniqueName, String displayName, CohortIndicator indicator, String dimensionCategories) { 		
			addIndicator(uniqueName, displayName, indicator, OpenmrsUtil.parseParameterList(dimensionCategories));
		}	
		
		/**
		 * Add a period cohort indicator to the report definition with dimension cateogies.
		 * @param CohortIndicator
		 */
		public void addIndicator(CohortIndicator indicator, Map<String,String> dimensionCategories) { 		
			addIndicator(indicator.getName(), indicator.getName(), indicator, dimensionCategories);
		}
		
		
		/**
		 * Add dimensions to a period indicator report definition.  This also adds the default 
		 * parameters to the dimension.
		 * 
		 * @param dimensionKey
		 * @param dimension
		 */
		public void addDimension(String dimensionKey, CohortDefinitionDimension dimension) { 	
			dimension.addParameters(IndicatorUtil.getDefaultParameters());
			addDimension(dimensionKey, dimension, IndicatorUtil.getDefaultParameterMappings());
		}
		
		/**
		 * Add dimension to a period indicator report definition where the cohort definition dimension 
		 * needs to be mapped to report parameters.  
		 * 
		 * @param dimensionKey
		 * @param dimension
		 * @param parameterMappings
		 */
		public void addDimension(String dimensionKey, CohortDefinitionDimension dimension, Map<String,Object> parameterMappings) { 		
			getIndicatorDataSetDefinition().addDimension(dimensionKey, dimension, parameterMappings);
		}
		
		
}
