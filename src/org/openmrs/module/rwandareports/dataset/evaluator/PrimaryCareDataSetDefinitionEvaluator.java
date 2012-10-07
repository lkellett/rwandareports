package org.openmrs.module.rwandareports.dataset.evaluator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.query.encounter.service.EncounterQueryService;
import org.openmrs.module.rwandareports.dataset.PrimaryCareDataSetDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

@Handler(supports = { PrimaryCareDataSetDefinition.class })
public class PrimaryCareDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	private GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	/**
	 * @throws EvaluationException
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 * @should evaluate a MultiPeriodIndicatorDataSetDefinition
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		if (context == null) {
			context = new EvaluationContext();
		}
		
		SimpleDataSet ret = new SimpleDataSet(dataSetDefinition, context);
		
		DataSetColumn pcId = new DataSetColumn("pcId", "pcId", String.class);
		ret.getMetaData().addColumn(pcId);
		
		DataSetColumn imbId = new DataSetColumn("imbId", "pcId", String.class);
		ret.getMetaData().addColumn(imbId);
		
		DataSetColumn visitDate = new DataSetColumn("visitDate", "visitDate", Date.class);
		ret.getMetaData().addColumn(visitDate);
		
		DataSetColumn service = new DataSetColumn("service", "service", String.class);
		ret.getMetaData().addColumn(service);
		
		DataSetColumn dob = new DataSetColumn("dob", "dob", String.class);
		ret.getMetaData().addColumn(dob);
		
		DataSetColumn gender = new DataSetColumn("gender", "gender", String.class);
		ret.getMetaData().addColumn(gender);
		
		DataSetColumn calcAge = new DataSetColumn("calcAge", "calcAge", String.class);
		ret.getMetaData().addColumn(calcAge);
		
		DataSetColumn insurance = new DataSetColumn("insurance", "insurance", String.class);
		ret.getMetaData().addColumn(insurance);
		
		DataSetColumn province = new DataSetColumn("province", "province", String.class);
		ret.getMetaData().addColumn(province);
		
		DataSetColumn district = new DataSetColumn("district", "district", String.class);
		ret.getMetaData().addColumn(district);
		
		DataSetColumn sector = new DataSetColumn("sector", "sector", String.class);
		ret.getMetaData().addColumn(sector);
		
		DataSetColumn cell = new DataSetColumn("cell", "cell", String.class);
		ret.getMetaData().addColumn(cell);
		
		DataSetColumn umudugudu = new DataSetColumn("umudugudu", "umudugudu", String.class);
		ret.getMetaData().addColumn(umudugudu);
		
		List<Encounter> encounters = getEncounters(context);
		
		for(Encounter e: encounters)
		{
			addEncounterValues(e, ret, pcId, imbId, visitDate, service, dob, gender, calcAge, insurance, province, district, sector, cell, umudugudu);
		}
		
		return ret;
	}
	
	private List<Encounter> getEncounters(EvaluationContext context) throws EvaluationException {
		
		
		EncounterType registrationEncounter = gp.getEncounterType(GlobalPropertiesManagement.PRIMARY_CARE_REGISTRATION);
		
		SqlEncounterQuery encCohort = new SqlEncounterQuery();
		String sql = "select encounter_id from encounter where voided=0 and encounter_type ="
		        + registrationEncounter.getEncounterTypeId();
		encCohort.setQuery(sql);
		
		EncounterQueryService eqs = Context.getService(EncounterQueryService.class);
		
		EncounterQueryResult eqr = eqs.evaluate(encCohort, context);
		
		Set<Integer> encounters = eqr.getMemberIds();
		List<Encounter> encs = getEncounters(encounters);
		
		return encs;
	}
	
	
	private void addEncounterValues(Encounter encounter, SimpleDataSet dataSet, DataSetColumn pcId, DataSetColumn imbId, DataSetColumn visitDate, DataSetColumn service, DataSetColumn dob, 
	                                DataSetColumn gender, DataSetColumn calcAge, DataSetColumn insurance, DataSetColumn province, DataSetColumn district, DataSetColumn sector, DataSetColumn cell, DataSetColumn umudugudu) 
	{
		DataSetRow row = new DataSetRow();
		
		Patient patient = encounter.getPatient();
		
		PatientIdentifierType pcType = gp.getPatientIdentifier(GlobalPropertiesManagement.PC_IDENTIFIER);
		PatientIdentifier pc = patient.getPatientIdentifier(pcType);
		
		if(pc != null)
		{
			row.addColumnValue(pcId, pc.getIdentifier());
		}
		
		PatientIdentifierType imbType = gp.getPatientIdentifier(GlobalPropertiesManagement.IMB_IDENTIFIER);
		PatientIdentifier imb = patient.getPatientIdentifier(imbType);
		
		if(imb != null)
		{
			row.addColumnValue(imbId, imb.getIdentifier());
		}
		else
		{
			row.addColumnValue(imbId, "");
		}
		
		row.addColumnValue(visitDate, encounter.getEncounterDatetime());
		
		Concept serviceConcept = gp.getConcept(GlobalPropertiesManagement.PRIMARY_CARE_SERVICE_REQUESTED);
		Concept insuranceConcept = gp.getConcept(GlobalPropertiesManagement.RWANDA_INSURANCE_TYPE);
		
		Set<Obs> allObs = encounter.getAllObs();
		
		boolean serviceFound = false;
		boolean insuranceFound = false;
		for(Obs o: allObs)
		{
			if(!o.isVoided() && o.getConcept().equals(serviceConcept))
			{
				row.addColumnValue(service, o.getValueCoded().getDisplayString());
				serviceFound = true;
			}
			
			if(!o.isVoided() && o.getConcept().equals(insuranceConcept))
			{
				row.addColumnValue(insurance, o.getValueCoded().getDisplayString());
				insuranceFound = true;
			}
		}
		
		if(!serviceFound)
		{
			row.addColumnValue(service, "");
		}
		if(!insuranceFound)
		{
			row.addColumnValue(insurance, "");
		}
		
		row.addColumnValue(dob, patient.getBirthdate());
		row.addColumnValue(gender, patient.getGender());
		row.addColumnValue(calcAge, patient.getAge(encounter.getEncounterDatetime()));
		
		Set<PersonAddress> addresses = patient.getAddresses();
		
		PersonAddress preferred = null;
		for(PersonAddress a: addresses)
		{
			if(a.isPreferred())
			{
				preferred = a;
				break;
			}
		}
		
		if(preferred == null)
		{
			for(PersonAddress a: addresses)
			{
				if(a.isActive())
				{
					preferred = a;
					break;
				}
			}
		}
		
		if(preferred != null)
		{
			row.addColumnValue(province, preferred.getStateProvince());
			row.addColumnValue(district, preferred.getCountyDistrict());
			row.addColumnValue(sector, preferred.getCityVillage());
			row.addColumnValue(cell, preferred.getAddress3());
			row.addColumnValue(umudugudu, preferred.getAddress1());
		}
		else
		{
			row.addColumnValue(province, "");
			row.addColumnValue(district, "");
			row.addColumnValue(sector, "");
			row.addColumnValue(cell, "");
			row.addColumnValue(umudugudu, "");
		}
		
		dataSet.addRow(row);
	}
	
	private List<Encounter> getEncounters(Set<Integer> encounterIds) {
		List<Encounter> encs = new ArrayList<Encounter>();
		
		for (Integer eId : encounterIds) {
			Encounter e = Context.getEncounterService().getEncounter(eId);
			
			if (e != null) {
				encs.add(e);
			}
		}
		
		return encs;
	}
}
