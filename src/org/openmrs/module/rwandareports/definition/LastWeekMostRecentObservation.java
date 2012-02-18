package org.openmrs.module.rwandareports.definition;

import org.openmrs.Concept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.BasePatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;



public class LastWeekMostRecentObservation extends BasePatientData implements RowPerPatientData {

	private Concept concept;
	
	private ResultFilter filter = null;
	
	private boolean includeNull = true;
	
    /**
     * @return the concept
     */
    public Concept getConcept() {
    	return concept;
    }

	
    /**
     * @param concept the concept to set
     */
    public void setConcept(Concept concept) {
    	this.concept = concept;
    	if(concept != null)
    	{
    		setName(concept.getName().getName());
    		setDescription(concept.getDisplayString());
    	}
    }


	public ResultFilter getFilter() {
		return filter;
	}


	public void setFilter(ResultFilter filter) {
		this.filter = filter;
	}


	public boolean isIncludeNull() {
		return includeNull;
	}


	public void setIncludeNull(boolean includeNull) {
		this.includeNull = includeNull;
	}
	
	
}
