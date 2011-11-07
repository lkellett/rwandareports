package org.openmrs.module.rwandareports.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AgeAtDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiffInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfObsAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedAfterDateRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstRecordedObservationWithCodedConceptAnswer;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueBeforeDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAgeInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;

public class RowPerPatientColumns {
	
	static GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public static PatientProperty getFirstNameColumn(String name) {
		PatientProperty givenName = new PatientProperty("givenName");
		givenName.setName(name);
		return givenName;
	}
	
	public static PatientProperty getFamilyNameColumn(String name) {
		PatientProperty familyName = new PatientProperty("familyName");
		familyName.setName(name);
		return familyName;
	}
	
	public static PatientProperty getAge(String name) {
		PatientProperty age = new PatientProperty("age");
		age.setName(name);
		return age;
	}
	
	public static PatientAgeInMonths getAgeInMonths(String name) {
		PatientAgeInMonths ageInMonths = new PatientAgeInMonths();
		ageInMonths.setName(name);
		return ageInMonths;
	}
	
	public static AgeAtDateOfOtherDefinition getAgeAtDateOfOtherDefinition(String name, DateOfPatientData definition) {
		AgeAtDateOfOtherDefinition age = new AgeAtDateOfOtherDefinition();
		age.setDateOfPatientData(definition, new HashMap<String, Object>());
		return age;
	}
	
	public static PatientProperty getGender(String name) {
		PatientProperty gender = new PatientProperty("gender");
		gender.setName(name);
		return gender;
	}
	
	public static DateOfBirthShowingEstimation getDateOfBirth(String name, String dateFormat, String estimatedDateFormat) {
		DateOfBirthShowingEstimation birthdate = new DateOfBirthShowingEstimation();
		birthdate.setName(name);
		
		if (dateFormat != null) {
			birthdate.setDateFormat(dateFormat);
		}
		if (estimatedDateFormat != null) {
			birthdate.setEstimatedDateFormat(estimatedDateFormat);
		}
		return birthdate;
	}
	
	public static MultiplePatientDataDefinitions getIMBId(String name) {
		PatientIdentifierType imbType = gp.getPatientIdentifier(GlobalPropertiesManagement.IMB_IDENTIFIER);
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		
		PatientIdentifierType pcType = gp.getPatientIdentifier(GlobalPropertiesManagement.PC_IDENTIFIER);
		PatientIdentifier pcId = new PatientIdentifier(pcType);
		
		MultiplePatientDataDefinitions id = new MultiplePatientDataDefinitions();
		id.setName(name);
		id.addPatientDataDefinition(imbId, new HashMap<String, Object>());
		id.addPatientDataDefinition(pcId, new HashMap<String, Object>());
		
		return id;
	}
	
	public static PatientIdentifier getTracnetId(String name) {
		PatientIdentifierType tracNetId = gp.getPatientIdentifier(GlobalPropertiesManagement.TRACNET_IDENTIFIER);
		PatientIdentifier id = new PatientIdentifier(tracNetId);
		
		return id;
	}
	
	public static RetrievePersonByRelationship getMother() {
		RetrievePersonByRelationship mother = new RetrievePersonByRelationship();
		mother.setRelationshipTypeId(gp.getRelationshipType(GlobalPropertiesManagement.MOTHER_RELATIONSHIP)
		        .getRelationshipTypeId());
		mother.setRetrievePersonAorB("A");
		return mother;
	}
	
	public static StateOfPatient getStateOfPatient(String name, Program program, ProgramWorkflow programWorkflow,
	                                               ResultFilter filter) {
		StateOfPatient state = new StateOfPatient();
		state.setPatientProgram(program);
		state.setPatienProgramWorkflow(programWorkflow);
		state.setName(name);
		
		if (filter != null) {
			state.setFilter(filter);
		}
		
		return state;
	}
	
	public static RecentEncounterType getRecentEncounterType(String name, List<EncounterType> encounterTypes,
	                                                         ResultFilter filter) {
		RecentEncounterType lastEncounter = new RecentEncounterType();
		lastEncounter.setName(name);
		lastEncounter.setEncounterTypes(encounterTypes);
		lastEncounter.setFilter(filter);
		return lastEncounter;
	}
	
	public static RecentEncounterType getRecentEncounterType(String name, List<EncounterType> encounterTypes,
	                                                         String dateFormat, ResultFilter filter) {
		RecentEncounterType lastEncounter = getRecentEncounterType(name, encounterTypes, filter);
		if (dateFormat != null) {
			lastEncounter.setDateFormat(dateFormat);
		}
		return lastEncounter;
	}
	
	public static DateDiffInMonths getDifferenceInMonthsSinceLastEncounter(String name, List<EncounterType> encounterTypes) {
		DateDiffInMonths lastVisit = new DateDiffInMonths();
		lastVisit.setName(name);
		lastVisit.setEncounterTypes(encounterTypes);
		return lastVisit;
	}
	
	public static DateDiffInMonths getDifferenceInMonthsSinceLastObservation(String name, Concept concept) {
		DateDiffInMonths lastObs = new DateDiffInMonths();
		lastObs.setName(name);
		lastObs.setConcept(concept);
		return lastObs;
	}
	
	public static MultiplePatientDataDefinitions getMultiplePatientDataDefinitions(String name, List<PatientData> definitions) {
		MultiplePatientDataDefinitions mult = new MultiplePatientDataDefinitions();
		mult.setName(name);
		
		for (PatientData pd : definitions) {
			mult.addPatientDataDefinition(pd, new HashMap<String, Object>());
		}
		return mult;
	}
	
	public static MostRecentObservation getMostRecentTbTest(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.TB_TEST_CONCEPT), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentWeight(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentWeight(String name, String dateFormat, ResultFilter resultFilter) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT), dateFormat, resultFilter);
	}
	
	public static MostRecentObservation getMostRecentHeight(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentHeight(String name, String dateFormat, ResultFilter resultFilter) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT), dateFormat, resultFilter);
	}
	
	public static MostRecentObservation getMostRecentCD4(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.CD4_TEST), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentCD4(String name, String dateFormat, ResultFilter resultFilter) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.CD4_TEST), dateFormat, resultFilter);
	}
	
	public static MostRecentObservation getMostRecentCD4Percentage(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.CD4_PERCENTAGE_TEST), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentViralLoad(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentReturnVisitDate(String name, String dateFormat) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE), dateFormat);
	}
	
	public static MostRecentObservation getMostRecentReturnVisitDate(String name, String dateFormat,
	                                                                 ResultFilter resultFilter) {
		return getMostRecent(name, gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE), dateFormat, resultFilter);
	}
	
	public static AllObservationValues getAllWeightValues(String name, String dateFormat, ResultFilter resultFilter,
	                                                      ResultFilter outputFilter) {
		return getAllObservationValues(name, gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT), dateFormat,
		    resultFilter, outputFilter);
	}
	
	public static AllObservationValues getAllCD4Values(String name, String dateFormat, ResultFilter resultFilter,
	                                                   ResultFilter outputFilter) {
		return getAllObservationValues(name, gp.getConcept(GlobalPropertiesManagement.CD4_TEST), dateFormat, resultFilter,
		    outputFilter);
	}
	
	public static ObservationInMostRecentEncounterOfType getIOInMostRecentEncounterOfType(String name,
	                                                                                      EncounterType encounterType) {
		return getObservationInMostRecentEncounterOfType(name, gp.getConcept(GlobalPropertiesManagement.IO_CONCEPT),
		    encounterType);
	}
	
	public static ObservationInMostRecentEncounterOfType getSideEffectInMostRecentEncounterOfType(String name,
	                                                                                              EncounterType encounterType) {
		return getObservationInMostRecentEncounterOfType(name,
		    gp.getConcept(GlobalPropertiesManagement.SIDE_EFFECT_CONCEPT), encounterType);
	}
	
	public static PatientRelationship getAccompRelationship(String name) {
		return getPatientRelationship(name, gp.getRelationshipType(GlobalPropertiesManagement.ACCOMPAGNATUER_RELATIONSHIP)
		        .getRelationshipTypeId(), "A");
	}
	
	public static PatientRelationship getMotherRelationship(String name) {
		return getPatientRelationship(name, gp.getRelationshipType(GlobalPropertiesManagement.MOTHER_RELATIONSHIP)
		        .getRelationshipTypeId(), "A");
	}
	
	public static CurrentOrdersRestrictedByConceptSet getCurrentARTOrders(String name, String dateFormat,
	                                                                      ResultFilter drugFilter) {
		return getCurrentOrdersRestrictedByConceptSet(name, gp.getConcept(GlobalPropertiesManagement.ART_DRUGS_SET),
		    dateFormat, drugFilter);
	}
	
	public static CurrentOrdersRestrictedByConceptSet getCurrentTBOrders(String name, String dateFormat,
	                                                                     ResultFilter drugFilter) {
		return getCurrentOrdersRestrictedByConceptSet(name, gp.getConcept(GlobalPropertiesManagement.TB_TREATMENT_DRUGS),
		    dateFormat, drugFilter);
	}
	
	public static MostRecentObservation getMostRecent(String name, Concept concept, String dateFormat) {
		MostRecentObservation mostRecent = new MostRecentObservation();
		mostRecent.setConcept(concept);
		mostRecent.setName(name);
		if (dateFormat != null) {
			mostRecent.setDateFormat(dateFormat);
		}
		return mostRecent;
	}
	
	public static MostRecentObservation getMostRecent(String name, Concept concept, String dateFormat,
	                                                  ResultFilter resultFilter) {
		MostRecentObservation mostRecent = getMostRecent(name, concept, dateFormat);
		if (resultFilter != null) {
			mostRecent.setFilter(resultFilter);
		}
		return mostRecent;
	}
	
	public static AllObservationValues getAllObservationValues(String name, Concept concept, String dateFormat,
	                                                           ResultFilter resultFilter, ResultFilter outputFilter) {
		AllObservationValues allObs = new AllObservationValues();
		allObs.setConcept(concept);
		allObs.setName(name);
		if (resultFilter != null) {
			allObs.setFilter(resultFilter);
		}
		if (dateFormat != null) {
			allObs.setDateFormat(dateFormat);
		}
		if (outputFilter != null) {
			allObs.setOutputFilter(outputFilter);
		}
		return allObs;
	}
	
	public static ObservationInMostRecentEncounterOfType getObservationInMostRecentEncounterOfType(String name,
	                                                                                               Concept concept,
	                                                                                               EncounterType encounterType) {
		ObservationInMostRecentEncounterOfType oe = new ObservationInMostRecentEncounterOfType();
		oe.setName(name);
		oe.setObservationConcept(concept);
		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		encounterTypes.add(encounterType);
		oe.setEncounterTypes(encounterTypes);
		
		return oe;
	}
	
	public static PatientRelationship getPatientRelationship(String name, int relationshipTypeId, String side) {
		PatientRelationship rel = new PatientRelationship();
		rel.setName(name);
		rel.setRelationshipTypeId(relationshipTypeId);
		rel.setRetrievePersonAorB(side);
		return rel;
	}
	
	public static CurrentOrdersRestrictedByConceptSet getCurrentOrdersRestrictedByConceptSet(String name,
	                                                                                         Concept drugConcept,
	                                                                                         String dateFormat,
	                                                                                         ResultFilter drugFilter) {
		CurrentOrdersRestrictedByConceptSet co = new CurrentOrdersRestrictedByConceptSet();
		co.setDrugConceptSetConcept(drugConcept);
		if (dateFormat != null) {
			co.setDateFormat(dateFormat);
		}
		if (drugFilter != null) {
			co.setDrugFilter(drugFilter);
		}
		co.setName(name);
		return co;
	}
	
	public static PatientAddress getPatientAddress(String name, boolean district, boolean sector, boolean cell,
	                                               boolean umudugudu) {
		PatientAddress address = new PatientAddress();
		address.setName("Address");
		address.setDescription("Address");
		address.setIncludeCountry(false);
		address.setIncludeProvince(false);
		address.setIncludeDistrict(district);
		address.setIncludeSector(sector);
		address.setIncludeCell(cell);
		address.setIncludeUmudugudu(umudugudu);
		return address;
	}
	
	public static EvaluateDefinitionForOtherPersonData getDefinitionForOtherPerson(String name, PersonData person,
	                                                                               PatientData definition) {
		EvaluateDefinitionForOtherPersonData otherDef = new EvaluateDefinitionForOtherPersonData();
		otherDef.setPersonData(person, new HashMap<String, Object>());
		otherDef.setDefinition(definition, new HashMap<String, Object>());
		otherDef.setName(name);
		return otherDef;
	}
	
	public static ObsValueAfterDateOfOtherDefinition getObsValueAfterDateOfOtherDefinition(String name, Concept concept,
	                                                                                       DateOfPatientData patientData,
	                                                                                       String dateFormat) {
		ObsValueAfterDateOfOtherDefinition ovadood = new ObsValueAfterDateOfOtherDefinition();
		ovadood.setConcept(concept);
		ovadood.setName(name);
		ovadood.setDateOfPatientData(patientData, new HashMap<String, Object>());
		
		if (dateFormat != null) {
			ovadood.setDateFormat(dateFormat);
		}
		
		return ovadood;
	}
	
	public static ObsValueBeforeDateOfOtherDefinition getObsValueBeforeDateOfOtherDefinition(String name, Concept concept,
	                                                                                         DateOfPatientData patientData,
	                                                                                         String dateFormat) {
		ObsValueBeforeDateOfOtherDefinition ovbdood = new ObsValueBeforeDateOfOtherDefinition();
		ovbdood.setConcept(concept);
		ovbdood.setName(name);
		ovbdood.setDateOfPatientData(patientData, new HashMap<String, Object>());
		
		if (dateFormat != null) {
			ovbdood.setDateFormat(dateFormat);
		}
		
		return ovbdood;
	}
	
	public static DateOfObsAfterDateOfOtherDefinition getDateOfObsAfterDateOfOtherDefinition(String name, Concept concept,
	                                                                                         DateOfPatientData patientData) {
		DateOfObsAfterDateOfOtherDefinition dooadood = new DateOfObsAfterDateOfOtherDefinition();
		dooadood.setConcept(concept);
		dooadood.setName(name);
		dooadood.setDateOfPatientData(patientData, new HashMap<String, Object>());
		return dooadood;
	}
	
	public static DateOfWorkflowStateChange getDateOfWorkflowStateChange(String name, Concept workflowConcept) {
		DateOfWorkflowStateChange startDate = new DateOfWorkflowStateChange();
		startDate.setConcept(workflowConcept);
		startDate.setName(name);
		return startDate;
	}
	
	public static FirstDrugOrderStartedRestrictedByConceptSet getFirstDrugOrderStartedRestrictedByConceptSet(String name,
	                                                                                                         Concept conceptSet) {
		FirstDrugOrderStartedRestrictedByConceptSet startDateDrugs = new FirstDrugOrderStartedRestrictedByConceptSet();
		startDateDrugs.setName(name);
		startDateDrugs.setDrugConceptSetConcept(conceptSet);
		return startDateDrugs;
	}
	
	public static FirstDrugOrderStartedRestrictedByConceptSet getFirstDrugOrderStartedRestrictedByConceptSet(String name,
	                                                                                                         Concept conceptSet,
	                                                                                                         String dateFormat) {
		FirstDrugOrderStartedRestrictedByConceptSet startDateDrugs = getFirstDrugOrderStartedRestrictedByConceptSet(name,
		    conceptSet);
		if (dateFormat != null) {
			startDateDrugs.setDateFormat(dateFormat);
		}
		return startDateDrugs;
	}
	
	public static FirstDrugOrderStartedAfterDateRestrictedByConceptSet getFirstDrugOrderStartedAfterDateRestrictedByConceptSet(String name,
	                                                                                                                           Concept conceptSet,
	                                                                                                                           DateOfPatientData patientData) {
		FirstDrugOrderStartedAfterDateRestrictedByConceptSet initial = new FirstDrugOrderStartedAfterDateRestrictedByConceptSet();
		initial.setName(name);
		initial.setDrugConceptSetConcept(conceptSet);
		initial.setDateOfPatientData(patientData, new HashMap<String, Object>());
		return initial;
	}
	
	public static FirstDrugOrderStartedRestrictedByConceptSet getDrugOrderForStartOfART(String name) {
		return getFirstDrugOrderStartedRestrictedByConceptSet(name, gp.getConcept(GlobalPropertiesManagement.ART_DRUGS_SET));
	}
	
	public static FirstDrugOrderStartedRestrictedByConceptSet getDrugOrderForStartOfART(String name, String dateFormat) {
		return getFirstDrugOrderStartedRestrictedByConceptSet(name, gp.getConcept(GlobalPropertiesManagement.ART_DRUGS_SET),
		    dateFormat);
	}
	
	public static FirstDrugOrderStartedAfterDateRestrictedByConceptSet getDrugOrderForStartOfARTAfterDate(String name,
	                                                                                                      DateOfPatientData patientData) {
		return getFirstDrugOrderStartedAfterDateRestrictedByConceptSet(name,
		    gp.getConcept(GlobalPropertiesManagement.ART_DRUGS_SET), patientData);
	}
	
	public static AllDrugOrdersRestrictedByConcept getAllDrugOrdersRestrictedByConcept(String name, Concept concept) {
		AllDrugOrdersRestrictedByConcept all = new AllDrugOrdersRestrictedByConcept();
		all.setName(name);
		all.setConcept(concept);
		return all;
	}
	
	public static AllDrugOrdersRestrictedByConceptSet getAllDrugOrdersRestrictedByConceptSet(String name, Concept concept) {
		AllDrugOrdersRestrictedByConceptSet all = new AllDrugOrdersRestrictedByConceptSet();
		all.setName(name);
		all.setDrugConceptSetConcept(concept);
		return all;
	}
	
	public static FirstRecordedObservationWithCodedConceptAnswer getFirstRecordedObservationWithCodedConceptAnswer(String name,
	                                                                                                               Concept question,
	                                                                                                               Concept answer,
	                                                                                                               String dateFormat) {
		FirstRecordedObservationWithCodedConceptAnswer firstRecorded = new FirstRecordedObservationWithCodedConceptAnswer();
		firstRecorded.setName(name);
		firstRecorded.setAnswerRequired(answer);
		firstRecorded.setQuestion(question);
		
		if (dateFormat != null) {
			firstRecorded.setDateFormat("dd-MMM-yyyy");
		}
		return firstRecorded;
	}
}
