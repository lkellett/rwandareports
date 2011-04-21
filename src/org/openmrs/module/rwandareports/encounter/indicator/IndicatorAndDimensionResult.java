package org.openmrs.module.rwandareports.encounter.indicator;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicatorResult;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.indicator.dimension.CohortDimension;
import org.openmrs.module.rwandareports.encounter.service.EncounterIndicatorResult;

/**
 * Result class that can hold ChortIndicatorResults or EncounterIndicatorResults, the getValue() method will return the right value based on Results type.
 * 
 *  TODO:  this should extend CohortIndicatorAndDimensionResult from reporting module -- its basically a copy.
 *  
 * @author dthomas
 *
 */
public class IndicatorAndDimensionResult implements IndicatorResult {
//***** PROPERTIES *****
	
	
	private EvaluationContext context;
	private CohortIndicatorResult cohortIndicatorResult;
	private EncounterIndicatorResult encounterIndicatorResult;
	private Map<CohortDimension, String> dimensions = new HashMap<CohortDimension, String>();
	private Map<CohortDimension, Cohort> dimensionResults = new HashMap<CohortDimension, Cohort>();
	
	//***** CONSTRUCTORS *****
	
	/**
	 * Default constructor
	 */
	public IndicatorAndDimensionResult(IndicatorResult indicatorResult, EvaluationContext context) {
		if (indicatorResult instanceof CohortIndicatorResult)
			this.cohortIndicatorResult = (CohortIndicatorResult) indicatorResult;
		 else if (indicatorResult instanceof EncounterIndicatorResult)
			this.encounterIndicatorResult = (EncounterIndicatorResult) indicatorResult;
		 else
			 throw new RuntimeException("Found unsupported IndicatorResult type.");
		this.context = context;
    }
	
	//***** INSTANCE METHODS *****
	
	/**
	 * Applies a DimensionResult to this
	 */
	public void applyDimensionResult(CohortDimension dimension, String option, Cohort result) {
		getDimensions().put(dimension, option);
		getDimensionResults().put(dimension, result);
	}
	
	/**
	 * Retrieves the combined filter Cohort for all configured Dimensions
	 */
	public Cohort calculateDimensionCohort() {
		Cohort ret = null;
		for (Cohort c : getDimensionResults().values()) {
			if (ret == null) {
				ret = new Cohort(c.getMemberIds());
			}
			else {
				ret = Cohort.intersect(ret, c);
			}
		}
		return ret;
	}
	
	/**
	 * @return the Cohort which results from intersecting the CohortIndicator cohort with the dimension Cohorts
	 */
	public Cohort getCohortIndicatorAndDimensionCohort() {
		Cohort ret = cohortIndicatorResult.getCohort();
		if (ret != null && !getDimensionResults().isEmpty()) {
			ret = Cohort.intersect(ret, calculateDimensionCohort());
		}
		return ret;
	}
	
	/**
	 * @return the Denominator which results from intersecting the CohortIndicator cohort with the dimension Cohorts
	 */
	public Cohort getCohortIndicatorAndDimensionDenominator() {
		Cohort ret = cohortIndicatorResult.getDenominatorCohort();
		if (ret != null && !getDimensionResults().isEmpty()) {
			ret = Cohort.intersect(ret, calculateDimensionCohort());
		}
		return ret;
	}
	
	/**
	 * @return the LogicResults which results from intersecting the CohortIndicator cohort with the dimension Cohorts
	 */
	public Map<Integer, Number> getCohortIndicatorAndDimensionLogicResults() {
		Map<Integer, Number> ret = cohortIndicatorResult.getLogicResults();
		if (ret != null && !getDimensionResults().isEmpty()) {
			ret = new HashMap<Integer, Number>(cohortIndicatorResult.getLogicResults());
			ret.keySet().retainAll(calculateDimensionCohort().getMemberIds());
		}
		return ret;
	}

	/**
	 * @see IndicatorResult#getValue()
	 */
	public Number getValue() {
		if (this.cohortIndicatorResult != null)
			return CohortIndicatorResult.getResultValue(cohortIndicatorResult, calculateDimensionCohort());
		else
			return EncounterIndicatorResult.getResultValue(encounterIndicatorResult);
		//TODO:  add args above if you ever want to do encounter dimensions
    }
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return ObjectUtil.nvlStr(getValue(), "");
	}
	
	//***** PROPERTY ACCESS *****

    /**
     * @see Evaluated#getContext()
     */
	public EvaluationContext getContext() {
	    return context;
    }

    /**
     * @see Evaluated#getDefinition()
     */
	public CohortIndicator getDefinition() {
	    return cohortIndicatorResult.getDefinition();
    }

	/**
	 * @return the dimensions
	 */
	public Map<CohortDimension, String> getDimensions() {
		if (dimensions == null) {
			dimensions = new HashMap<CohortDimension, String>();
		}
		return dimensions;
	}

	/**
	 * @param dimensions the dimensions to set
	 */
	public void setDimensions(Map<CohortDimension, String> dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * @return the cohortIndicatorResult
	 */
	public CohortIndicatorResult getCohortIndicatorResult() {
		return cohortIndicatorResult;
	}

	/**
	 * @param cohortIndicatorResult the cohortIndicatorResult to set
	 */
	public void setCohortIndicatorResult(CohortIndicatorResult cohortIndicatorResult) {
		this.cohortIndicatorResult = cohortIndicatorResult;
	}

	/**
	 * @return the dimensionResults
	 */
	public Map<CohortDimension, Cohort> getDimensionResults() {
		if (dimensionResults == null) {
			dimensionResults = new HashMap<CohortDimension, Cohort>();
		}
		return dimensionResults;
	}

	/**
	 * @param dimensionResults the dimensionResults to set
	 */
	public void setDimensionResults(Map<CohortDimension, Cohort> dimensionResults) {
		this.dimensionResults = dimensionResults;
	}
	
	/**
	 * Add a Dimension Result
	 */
	public void addDimensionResult(CohortDimension dimension, Cohort cohort) {
	    getDimensionResults().put(dimension, cohort);
    }

	public void setContext(EvaluationContext context) {
		this.context = context;
	}

	public EncounterIndicatorResult getEncounterIndicatorResult() {
		return encounterIndicatorResult;
	}

	public void setEncounterIndicatorResult(
			EncounterIndicatorResult encounterIndicatorResult) {
		this.encounterIndicatorResult = encounterIndicatorResult;
	}
	
	
}
