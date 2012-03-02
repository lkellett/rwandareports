package org.openmrs.module.rwandareports.util;

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Form;
import org.openmrs.Program;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;


public class EncounterQuerys {
	
	private static GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public static SqlEncounterQuery getFormsBetweenStartEndDates(String name, Form form)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		formsCompleted.setQuery("select encounter_id from encounter where voided = 0 and form_id = " + form.getFormId() + " and encounter_datetime >= :startDate and encounter_datetime <= :endDate");
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
	public static SqlEncounterQuery getFormsBetweenStartEndDatesForAProgramEnrollment(String name, Form form, Program program)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		formsCompleted.setQuery("select encounter_id from encounter where encounter_id in (select e.encounter_id from encounter e, patient_program pp where e.voided = 0 and e.form_id = " + form.getFormId() + " and e.encounter_datetime >= :startDate and e.encounter_datetime <= :endDate and e.patient_id = pp.patient_id and pp.program_id = " + program.getProgramId() + " and pp.date_enrolled <= e.encounter_datetime and if(pp.date_completed is null, :endDate, pp.date_completed) >= e.encounter_datetime)");
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
	public static SqlEncounterQuery getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient(String name, Form form, Program program)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		formsCompleted.setQuery("select encounter_id from encounter where encounter_id in (select e.encounter_id from encounter e, patient_program pp where e.voided = 0 and e.form_id = " + form.getFormId() + " and e.encounter_datetime >= :startDate and e.encounter_datetime <= :endDate and e.patient_id = pp.patient_id and pp.program_id = " + program.getProgramId() + " and pp.date_enrolled <= e.encounter_datetime and if(pp.date_completed is null, :endDate, pp.date_completed) >= e.encounter_datetime group by e.patient_id)");
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
	public static SqlEncounterQuery getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation(String name, Form form, Concept concept, Program program)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		formsCompleted.setQuery("select encounter_id from encounter where encounter_id in (select e.encounter_id from encounter e, patient_program pp, obs o where e.voided = 0 and e.form_id = " + form.getFormId() + " and e.encounter_datetime >= :startDate and e.encounter_datetime <= :endDate and e.patient_id = pp.patient_id and pp.program_id = " + program.getProgramId() + " and pp.date_enrolled <= e.encounter_datetime and if(pp.date_completed is null, :endDate, pp.date_completed) >= e.encounter_datetime and o.voided = 0 and o.encounter_id = e.encounter_id and o.concept_id = " + concept.getId() + " and (o.value_boolean is not null or o.value_coded is not null or o.value_datetime is not null or o.value_numeric is not null))");
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
	public static SqlEncounterQuery getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation(String name, Form form, List<Concept> concepts, Program program)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		
		StringBuilder query = new StringBuilder();
		query.append("select encounter_id from encounter where encounter_id in (select e.encounter_id from encounter e, patient_program pp, obs o where e.voided = 0 and e.form_id = " + form.getFormId() + " and e.encounter_datetime >= :startDate and e.encounter_datetime <= :endDate and e.patient_id = pp.patient_id and pp.program_id = " + program.getProgramId() + " and pp.date_enrolled <= e.encounter_datetime and if(pp.date_completed is null, :endDate, pp.date_completed) >= e.encounter_datetime and o.voided = 0 and o.encounter_id = e.encounter_id and o.concept_id in(");
		
		int i = 0;
		for(Concept c: concepts)
		{
			if(i > 0)
			{
				query.append(",");
			}
			query.append(c.getId());
			i++;
		}
		query.append(" and (o.value_boolean is not null or o.value_coded is not null or o.value_datetime is not null or o.value_numeric is not null))");
		
		formsCompleted.setQuery(query.toString());
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
	public static SqlEncounterQuery getFormsBetweenStartEndDatesForAProgramEnrollmentContainingCodedObservationValue(String name, Form form, Concept concept, Concept answer, Program program)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		formsCompleted.setQuery("select encounter_id from encounter where encounter_id in (select e.encounter_id from encounter e, patient_program pp, obs o where e.voided = 0 and e.form_id = " + form.getFormId() + " and e.encounter_datetime >= :startDate and e.encounter_datetime <= :endDate and e.patient_id = pp.patient_id and pp.program_id = " + program.getProgramId() + " and pp.date_enrolled <= e.encounter_datetime and if(pp.date_completed is null, :endDate, pp.date_completed) >= e.encounter_datetime and o.voided = 0 and o.encounter_id = e.encounter_id and o.concept_id = " + concept.getId() + " and o.value_coded = " + answer.getId() + ")");
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
	public static SqlEncounterQuery getFormsBetweenStartEndDatesForAProgramEnrollmentContainingCodedObservationValue(String name, Form form, Concept concept, List<Concept> answer, Program program)
	{
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		
		StringBuilder query = new StringBuilder();
		query.append("select encounter_id from encounter where encounter_id in (select e.encounter_id from encounter e, patient_program pp, obs o where e.voided = 0 and e.form_id = " + form.getFormId() + " and e.encounter_datetime >= :startDate and e.encounter_datetime <= :endDate and e.patient_id = pp.patient_id and pp.program_id = " + program.getProgramId() + " and pp.date_enrolled <= e.encounter_datetime and if(pp.date_completed is null, :endDate, pp.date_completed) >= e.encounter_datetime and o.voided = 0 and o.encounter_id = e.encounter_id and o.concept_id = " + concept.getId() + " and o.value_coded in (");
		
		int i = 0;
		for(Concept c: answer)
		{
			if(i > 0)
			{
				query.append(",");
			}
			query.append(c.getId());
			i++;
		}
		query.append("))");
		
		formsCompleted.setQuery(query.toString());
		formsCompleted.setName(name);
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		return formsCompleted;
	}
	
}
