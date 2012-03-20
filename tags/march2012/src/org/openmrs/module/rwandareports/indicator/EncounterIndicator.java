package org.openmrs.module.rwandareports.indicator;

import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.BaseIndicator;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;


public class EncounterIndicator extends BaseIndicator {
	
	@ConfigurationProperty
    private Mapped<? extends EncounterQuery> encounterQuery;

	
    public Mapped<? extends EncounterQuery> getEncounterQuery() {
    	return encounterQuery;
    }

	
    public void setEncounterQuery(Mapped<? extends EncounterQuery> encounterQuery) {
    	this.encounterQuery = encounterQuery;
    }
}
