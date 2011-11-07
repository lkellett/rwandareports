package org.openmrs.module.rwandareports.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PatientStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.rwandareports.definition.DrugsActiveCohortDefinition;

public class Cohorts {
	
	public static SqlCohortDefinition createParameterizedLocationCohort() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location.setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("AdultHIVLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		return location;
	}
	
	public static SqlCohortDefinition createPatientsNotVoided() {
		SqlCohortDefinition patientsNotVoided = new SqlCohortDefinition(
		        "select distinct p.patient_id from patient p where p.voided=0");
		return patientsNotVoided;
	}
	
	public static SqlCohortDefinition createPatientsWithAccompagnateur(String name, String parameterName) {
		SqlCohortDefinition allPatientsWithAccompagnateur = new SqlCohortDefinition(
		        "SELECT DISTINCT person_b FROM relationship WHERE relationship='1' and date_created<= :endDate and voided=0");
		allPatientsWithAccompagnateur.setName(name);
		if (parameterName != null) {
			allPatientsWithAccompagnateur.addParameter(new Parameter(parameterName, parameterName, Date.class));
		}
		return allPatientsWithAccompagnateur;
	}
	
	public static AgeCohortDefinition createOver15AgeCohort(String name) {
		AgeCohortDefinition over15Cohort = new AgeCohortDefinition();
		over15Cohort.setName(name);
		over15Cohort.setMinAge(new Integer(15));
		over15Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
		return over15Cohort;
	}
	
	public static InProgramCohortDefinition createInProgram(String name, Program program) {
		InProgramCohortDefinition inProgram = new InProgramCohortDefinition();
		inProgram.setName(name);
		
		List<Program> programs = new ArrayList<Program>();
		programs.add(program);
		
		inProgram.setPrograms(programs);
		
		return inProgram;
	}
	
	public static InProgramCohortDefinition createInProgram(String name, List<Program> programs) {
		InProgramCohortDefinition inProgram = new InProgramCohortDefinition();
		inProgram.setName(name);
		
		inProgram.setPrograms(programs);
		
		return inProgram;
	}
	
	public static InProgramCohortDefinition createInProgramParameterizableByDate(String name, Program program) {
		InProgramCohortDefinition inProgram = createInProgram(name, program);
		inProgram.addParameter(new Parameter("onDate", "On Date", Date.class));
		return inProgram;
	}
	
	public static InProgramCohortDefinition createInProgramParameterizableByDate(String name, List<Program> programs,
	                                                                             String parameterName) {
		InProgramCohortDefinition inProgram = createInProgram(name, programs);
		inProgram.addParameter(new Parameter(parameterName, parameterName, Date.class));
		return inProgram;
	}
	
	public static InProgramCohortDefinition createInProgramParameterizableByDate(String name, List<Program> programs,
	                                                                             List<String> parameterName) {
		InProgramCohortDefinition inProgram = createInProgram(name, programs);
		
		for (String p : parameterName) {
			inProgram.addParameter(new Parameter(p, p, Date.class));
		}
		return inProgram;
	}
	
	public static InProgramCohortDefinition createInProgramParameterizableByStartEndDate(String name, Program program) {
		InProgramCohortDefinition inProgram = createInProgram(name, program);
		inProgram.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		inProgram.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		return inProgram;
	}
	
	public static ProgramEnrollmentCohortDefinition createProgramEnrollment(String name, Program program) {
		
		ProgramEnrollmentCohortDefinition programEnrollmentCohortDefinition = new ProgramEnrollmentCohortDefinition();
		programEnrollmentCohortDefinition.setName(name);
		
		List<Program> programs = new ArrayList<Program>();
		programs.add(program);
		
		programEnrollmentCohortDefinition.setPrograms(programs);
		return programEnrollmentCohortDefinition;
	}
	
	public static ProgramEnrollmentCohortDefinition createProgramEnrollmentParameterizedByStartEndDate(String name,
	                                                                                                   Program program) {
		
		ProgramEnrollmentCohortDefinition programEnrollmentCohortDefinition = createProgramEnrollment(name, program);
		programEnrollmentCohortDefinition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		programEnrollmentCohortDefinition
		        .addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		return programEnrollmentCohortDefinition;
	}
	
	public static InStateCohortDefinition createInProgramStateParameterizableByDate(String name, ProgramWorkflowState state) {
		InStateCohortDefinition stateCohort = new InStateCohortDefinition();
		
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		states.add(state);
		
		stateCohort.setStates(states);
		stateCohort.setName(name);
		stateCohort.addParameter(new Parameter("onDate", "On Date", Date.class));
		
		return stateCohort;
	}
	
	public static PatientStateCohortDefinition createPatientStateCohortDefinition(String name,
	                                                                              ProgramWorkflowState programWorkflowState) {
		PatientStateCohortDefinition patientState = new PatientStateCohortDefinition();
		patientState.setName(name);
		
		List<ProgramWorkflowState> programWorkFlowStateList = new ArrayList<ProgramWorkflowState>();
		programWorkFlowStateList.add(programWorkflowState);
		patientState.setStates(programWorkFlowStateList);
		
		return patientState;
	}
	
	public static InStateCohortDefinition createInCurrentStateParameterized(String name, String parameterName) {
		InStateCohortDefinition state = new InStateCohortDefinition();
		state.addParameter(new Parameter(parameterName, parameterName, ProgramWorkflowState.class));
		state.addParameter(new Parameter("onDate", "On Date", Date.class));
		state.setName(name);
		
		return state;
	}
	
	public static InStateCohortDefinition createInCurrentState(String name, List<ProgramWorkflowState> states) {
		InStateCohortDefinition state = new InStateCohortDefinition();
		state.setName(name);
		state.setStates(states);
		state.addParameter(new Parameter("onDate", "On Date", Date.class));
		
		return state;
	}
	
	public static InStateCohortDefinition createInCurrentState(String name, List<ProgramWorkflowState> states,
	                                                           String parameterName) {
		InStateCohortDefinition state = new InStateCohortDefinition();
		state.setName(name);
		state.setStates(states);
		state.addParameter(new Parameter(parameterName, parameterName, Date.class));
		
		return state;
	}
	
	public static InStateCohortDefinition createInCurrentState(String name, List<ProgramWorkflowState> states,
	                                                           List<String> parameterName) {
		InStateCohortDefinition state = createInCurrentState(name, states);
		
		for (String p : parameterName) {
			state.addParameter(new Parameter(p, p, Date.class));
		}
		
		return state;
	}
	
	public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, String parameterName,
	                                                                           List<EncounterType> encounters) {
		EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterName);
		encounter.setEncounterTypeList(encounters);
		return encounter;
	}
	
	public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, List<String> parameterNames,
	                                                                           List<EncounterType> encounters) {
		EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterNames);
		encounter.setEncounterTypeList(encounters);
		return encounter;
	}
	
	public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, List<String> parameterNames,
	                                                                           EncounterType encounterType) {
		List<EncounterType> encounters = new ArrayList<EncounterType>();
		encounters.add(encounterType);
		
		EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterNames);
		encounter.setEncounterTypeList(encounters);
		return encounter;
	}
	
	public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, String parameterName) {
		EncounterCohortDefinition encounter = new EncounterCohortDefinition();
		encounter.setName(name);
		encounter.addParameter(new Parameter(parameterName, parameterName, Date.class));
		return encounter;
	}
	
	public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, List<String> parameterNames) {
		EncounterCohortDefinition encounter = new EncounterCohortDefinition();
		encounter.setName(name);
		if (parameterNames != null) {
			for (String p : parameterNames) {
				encounter.addParameter(new Parameter(p, p, Date.class));
			}
		}
		return encounter;
	}
	
	public static EncounterCohortDefinition createEncounterBasedOnForms(String name, String parameterName, List<Form> forms) {
		EncounterCohortDefinition encounter = createEncounterParameterizedByDate(parameterName, name);
		encounter.setFormList(forms);
		return encounter;
	}
	
	public static NumericObsCohortDefinition createNumericObsCohortDefinition(String name, Concept question, double value,
	                                                                          RangeComparator setComparator,
	                                                                          TimeModifier timeModifier) {
		
		NumericObsCohortDefinition obsCohortDefinition = new NumericObsCohortDefinition();
		
		obsCohortDefinition.setName(name);
		
		if (question != null)
			obsCohortDefinition.setQuestion(question);
		
		if (setComparator != null)
			obsCohortDefinition.setOperator1(setComparator);
		
		if (timeModifier != null)
			obsCohortDefinition.setTimeModifier(timeModifier);
		
		if (value != 0) {
			obsCohortDefinition.setValue1(value);
		}
		
		return obsCohortDefinition;
	}
	
	public static NumericObsCohortDefinition createNumericObsCohortDefinition(String name, String parameterName,
	                                                                          Concept question, double value,
	                                                                          RangeComparator setComparator,
	                                                                          TimeModifier timeModifier) {
		
		NumericObsCohortDefinition obsCohortDefinition = createNumericObsCohortDefinition(parameterName, question, value,
		    setComparator, timeModifier);
		
		if (parameterName != null) {
			obsCohortDefinition.addParameter(new Parameter(parameterName, parameterName, Date.class));
		}
		
		return obsCohortDefinition;
	}
	
	public static NumericObsCohortDefinition createNumericObsCohortDefinition(String name, List<String> parameterNames,
	                                                                          Concept question, double value,
	                                                                          RangeComparator setComparator,
	                                                                          TimeModifier timeModifier) {
		
		NumericObsCohortDefinition obsCohortDefinition = createNumericObsCohortDefinition(name, question, value,
		    setComparator, timeModifier);
		
		if (parameterNames != null) {
			for (String p : parameterNames) {
				obsCohortDefinition.addParameter(new Parameter(p, p, Date.class));
			}
		}
		
		return obsCohortDefinition;
	}
	
	public static CodedObsCohortDefinition createCodedObsCohortDefinition(Concept question, Concept value,
	                                                                      SetComparator setComparator,
	                                                                      TimeModifier timeModifier) {
		CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();
		
		if (question != null) {
			obsCohortDefinition.setQuestion(question);
		}
		if (setComparator != null) {
			obsCohortDefinition.setOperator(setComparator);
		}
		if (timeModifier != null) {
			obsCohortDefinition.setTimeModifier(timeModifier);
		}
		
		List<Concept> valueList = new ArrayList<Concept>();
		if (value != null) {
			valueList.add(value);
			obsCohortDefinition.setValueList(valueList);
		}
		return obsCohortDefinition;
	}
	
	public static CodedObsCohortDefinition createCodedObsCohortDefinition(String name, Concept question, Concept value,
	                                                                      SetComparator setComparator,
	                                                                      TimeModifier timeModifier) {
		CodedObsCohortDefinition obsCohortDefinition = createCodedObsCohortDefinition(question, value, setComparator,
		    timeModifier);
		obsCohortDefinition.setName(name);
		return obsCohortDefinition;
	}
	
	public static CodedObsCohortDefinition createCodedObsCohortDefinition(String name, String parameterName,
	                                                                      Concept question, Concept value,
	                                                                      SetComparator setComparator,
	                                                                      TimeModifier timeModifier) {
		CodedObsCohortDefinition obsCohortDefinition = createCodedObsCohortDefinition(name, question, value, setComparator,
		    timeModifier);
		if (parameterName != null) {
			obsCohortDefinition.addParameter(new Parameter(parameterName, parameterName, Date.class));
		}
		return obsCohortDefinition;
	}
	
	public static CodedObsCohortDefinition createCodedObsCohortDefinition(String name, List<String> parameterNames,
	                                                                      Concept question, Concept value,
	                                                                      SetComparator setComparator,
	                                                                      TimeModifier timeModifier) {
		CodedObsCohortDefinition obsCohortDefinition = createCodedObsCohortDefinition(name, question, value, setComparator,
		    timeModifier);
		if (parameterNames != null) {
			for (String p : parameterNames) {
				obsCohortDefinition.addParameter(new Parameter(p, p, Date.class));
			}
		}
		return obsCohortDefinition;
	}
	
	public static GenderCohortDefinition createFemaleCohortDefinition(String name) {
		GenderCohortDefinition femaleDefinition = new GenderCohortDefinition();
		femaleDefinition.setName(name);
		femaleDefinition.setFemaleIncluded(true);
		return femaleDefinition;
	}
	
	public static GenderCohortDefinition createMaleCohortDefinition(String name) {
		GenderCohortDefinition maleDefinition = new GenderCohortDefinition();
		maleDefinition.setName(name);
		maleDefinition.setMaleIncluded(true);
		return maleDefinition;
	}
	
	public static DrugsActiveCohortDefinition createDrugsActiveCohort(String name, String parameterName, List<Drug> drugs) {
		DrugsActiveCohortDefinition drugsActive = new DrugsActiveCohortDefinition();
		drugsActive.setName(name);
		drugsActive.setDrugs(drugs);
		drugsActive.addParameter(new Parameter(parameterName, parameterName, Date.class));
		return drugsActive;
	}
}
