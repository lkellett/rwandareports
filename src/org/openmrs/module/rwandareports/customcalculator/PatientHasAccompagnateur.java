package org.openmrs.module.rwandareports.customcalculator;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

public class PatientHasAccompagnateur implements CustomCalculation {
	static GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

	public PatientDataResult calculateResult(List<PatientDataResult> results,
			EvaluationContext context) {
		StringResult sr = new StringResult(null, null);
		RelationshipType rt = gp.getRelationshipType(GlobalPropertiesManagement.ACCOMPAGNATUER_RELATIONSHIP);
		for (PatientDataResult result : results) {
			if (result.getName().equals("age")) {
				Patient p = result.getPatientData().getPatient();
				List<Relationship> relOfPerson = Context.getPersonService().getRelationshipsByPerson(p);
				if (relOfPerson.size() > 0) {
					for (Relationship relationship : relOfPerson) {
						if(relationship.getRelationshipType().getRelationshipTypeId() == rt.getRelationshipTypeId() && relationship.getPersonA() != null){
							sr.setValue("Yes");
							return sr;
						}
					}
					sr.setValue("No");
				}else{
					sr.setValue("No");
				}

			}
		}

		return sr;
	}

}
