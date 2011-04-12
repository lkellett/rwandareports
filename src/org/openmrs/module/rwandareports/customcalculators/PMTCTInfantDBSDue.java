package org.openmrs.module.rwandareports.customcalculators;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.EncounterResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class PMTCTInfantDBSDue implements CustomCalculation{
	
	private static Concept testsOrdered = Context.getConceptService().getConcept(new Integer(Context.getAdministrationService().getGlobalProperty("reports.testOrderedConcept")));
	
	private static Concept labTestDate = Context.getConceptService().getConcept(new Integer(Context.getAdministrationService().getGlobalProperty("reports.labTestDateConcept")));

	public PatientDataResult calculateResult(List<PatientDataResult> results) {
		
		EncounterResult encounter = null;
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("LastPMTCTEncounter"))
			{
				encounter = (EncounterResult)result;
			}
		}
		
		ObservationResult result = new ObservationResult(null, null);
		if(encounter.getValue() != null)
		{
			Encounter enc = encounter.getValue();
			Date encounterDate = enc.getEncounterDatetime();
			List<Obs> testOrdered = new ArrayList<Obs>();
			Obs testDate = null;
			
			Set<Obs> obs = enc.getAllObs(false);
			for(Obs o: obs)
			{
				if(o.getConcept().getConceptId().equals(testsOrdered.getConceptId()))
				{
					testOrdered.add(o);
				}
				if(o.getConcept().getConceptId().equals(labTestDate.getConceptId()))
				{
					testDate = o;
				}
			}
			
			if(testDate != null && testOrdered.size() > 0)
			{
				if(encounterDate.before(testDate.getValueDatetime()))
				{
					StringBuilder rs = new StringBuilder();
					int i = 0;
					for(Obs ro: testOrdered)
					{
						if(i > 0)
						{
							rs.append(", ");
						}
						rs.append(ro.getValueCoded().getName());
						i++;
					}
					result.setValue(rs.toString().trim());
					result.setDateOfObservation(testDate.getValueDatetime());
				}
			}
		}
		
		
		
		return result;
	}
}
