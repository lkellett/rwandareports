package org.openmrs.module.rwandareports.customcalculator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

public class OnInsulin implements CustomCalculation {

	protected Log log = LogFactory.getLog(OnInsulin.class);
	private List<EncounterType> diabetesEncouters;

	public OnInsulin(List<EncounterType> diabetesEncouters) {
		this.diabetesEncouters =diabetesEncouters;
	}

	public PatientDataResult calculateResult(List<PatientDataResult> results,EvaluationContext context) {
			
		StringResult sr = new StringResult(null, null);
		
		for (PatientDataResult result : results) {
			if (result.getName().equals("age")) {

				Patient p = result.getPatientData().getPatient();
				List<Encounter> patientEncounters = Context.getEncounterService().getEncounters(p, null, null, null, null, diabetesEncouters, null, false);

				if (patientEncounters.size() >= 1) {
					Encounter recentEncounter = patientEncounters.get(0);  //the first encounter in the List should be the most recent one.
					for (Order order : recentEncounter.getOrders()) {
						if (order instanceof DrugOrder && isInsulineOrder((DrugOrder) order)) {

							sr.setValue("Y");
							break;
						} else {
							sr.setValue("N");
						}
					}

				} else {
					sr.setValue("N");
				}

			}
		}

		return sr;
	}

	//should return true if the order-concept's conceptId is equal to one of the Insulins-concept conceptId
	public boolean isInsulineOrder(DrugOrder order) {
		String insulineConcepts = Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.INSULIN);
		String[] items = insulineConcepts.split(",");
		
		for (String string : items) {
			try {
				int i = Integer.parseInt(string);
				if (i == order.getConcept().getConceptId())
					return true;
			} catch (NumberFormatException e) {
				log.error("Invalid Global property: "+GlobalPropertiesManagement.INSULIN+". Value should be a comma separated list of Integers");
			}
			
		}
		return false;

	}
}
