package org.openmrs.module.rwandareports.dataset.evaluator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlwidgets.util.ReflectionUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.query.encounter.service.EncounterQueryService;
import org.openmrs.module.rwandareports.dataset.DataEntryDelayDataSetDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.widget.AllLocation;

@Handler(supports = { DataEntryDelayDataSetDefinition.class })
public class DataEntryDelayDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	private static String LOCATION = "LOCATION";
	
	private static String HIERARCHY = "HIERARCHY";
	
	private static String ALL_SITES = "ALL_SITES";
	
	private GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public DataEntryDelayDataSetDefinitionEvaluator() {
	}
	
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
		
		DataEntryDelayDataSetDefinition lhdsd = (DataEntryDelayDataSetDefinition) dataSetDefinition;
		
		SimpleDataSet ret = new SimpleDataSet(dataSetDefinition, context);
		
		AllLocation location = lhdsd.getLocation();
		if (location != null) {
			if (!location.isAllSites() && location.getHierarchy().equals(AllLocation.LOCATION)) {
				addIteration(ret, getEncounters(location.getValue(), LOCATION, location.getValue()), location.getValue(),
				    context, lhdsd);
			} else if (!location.isAllSites()) {
				List<Location> allLocations = Context.getLocationService().getAllLocations(false);
				
				addIteration(ret, getEncounters(location.getValue(), HIERARCHY, location.getHierarchy()),
				    location.getValue() + " " + location.getDisplayHierarchy(), context, lhdsd);
				
				for (Location l : allLocations) {
					String hierarchyValue = (String) ReflectionUtil.getPropertyValue(l, location.getHierarchy());
					if (hierarchyValue != null) {
						hierarchyValue = hierarchyValue.trim();
						hierarchyValue = hierarchyValue.toUpperCase();
					}
					
					if (location.getValue() != null && location.getValue().toUpperCase().equals(hierarchyValue)) {
						addIteration(ret, getEncounters(l.getName(), LOCATION, l.getName()), l.getName(), context,
							lhdsd);
					}
				}
			} else {
				addIteration(ret, getEncounters("All Sites", ALL_SITES, "All Sites"), "All Sites", context,
					lhdsd);
				
				List<Location> allLocations = Context.getLocationService().getAllLocations(false);
				
				String[] hierarchyToCheck = null;
				
				if (location.getHierarchy() != null) {
					hierarchyToCheck = location.getHierarchy().split(",");
				}
				
				for (String h : hierarchyToCheck) {
					String[] config = h.split(":");
					String hVal = config[0];
					String hDisplay = config[0];
					if (config.length > 0) {
						hDisplay = config[1];
					}
					
					TreeSet<String> allLoc = new TreeSet<String>();
					for (Location l : allLocations) {
						String hierarchyValue = (String) ReflectionUtil.getPropertyValue(l, hVal);
						if (hierarchyValue != null) {
							hierarchyValue = hierarchyValue.trim();
							hierarchyValue = hierarchyValue.toUpperCase();
							allLoc.add(hierarchyValue);
						}
					}
					
					for (String hLoc : allLoc) {
						addIteration(ret, getEncounters(hLoc, HIERARCHY, hVal), hLoc + " " + hDisplay, context,
							lhdsd);
					}
				}
				
				for (Location l : allLocations) {
					addIteration(ret, getEncounters(l.getName(), LOCATION, l.getName()), l.getName(), context,
						lhdsd);
				}
			}
		}
		
		return ret;
	}
	
	private SqlEncounterQuery getEncounters(String location, String hierarchy, String hierarchyValue) {
		
		if (hierarchy.equals(LOCATION)) {
			Location loc = Context.getLocationService().getLocation(location);
			
			SqlEncounterQuery locationCohort = new SqlEncounterQuery();
			locationCohort
			        .setQuery("select encounter_id from encounter where form_id is not null and date_created >= :startDate and date_created <= :endDate and voided=0 and location_id ="
			                + loc.getLocationId());
			locationCohort.addParameter(new Parameter("startDate", "startDate", Date.class));
			locationCohort.addParameter(new Parameter("endDate", "endDate", Date.class));
			
			return locationCohort;
		} else if (hierarchy.equals(HIERARCHY)) {
			
			String hVal = resolveDatabaseColumnName(hierarchyValue);
			
			SqlEncounterQuery locationCohort = new SqlEncounterQuery();
			locationCohort
			        .setQuery("select encounter_id from encounter where form_id is not null and date_created >= :startDate and date_created <= :endDate and voided=0 and location_id in (select location_id from location where retired = 0 and "
			                + hVal + " = '" + location + "')");
			locationCohort.addParameter(new Parameter("startDate", "startDate", Date.class));
			locationCohort.addParameter(new Parameter("endDate", "endDate", Date.class));
			
			return locationCohort;
		} else {
			
			String hVal = resolveDatabaseColumnName(hierarchyValue);
			
			SqlEncounterQuery locationCohort = new SqlEncounterQuery();
			locationCohort
			        .setQuery("select encounter_id from encounter where form_id is not null and date_created >= :startDate and date_created <= :endDate and voided=0");
			locationCohort.addParameter(new Parameter("startDate", "startDate", Date.class));
			locationCohort.addParameter(new Parameter("endDate", "endDate", Date.class));
			
			return locationCohort;
		}
	}
	
	private void addIteration(SimpleDataSet resultsSet, SqlEncounterQuery cohort, String locationDisplay,
	                          EvaluationContext context, DataEntryDelayDataSetDefinition dataSetDefinition) throws EvaluationException {
		
		if (cohort != null) {
			try {
				EncounterQueryService eqs = Context.getService(EncounterQueryService.class);
				
				EncounterQueryResult eqr = eqs.evaluate(cohort, context);
				
				Set<Integer> encounters = eqr.getMemberIds();
				List<Encounter> encs = getEncounters(encounters);
				
				SimpleDataSet summary = getSummaryDataSetForLocation(encs, dataSetDefinition, context);
				
				DataSetRow row = new DataSetRow();
				row.addColumnValue(new DataSetColumn("locationDisplay", "locationDisplay", String.class), locationDisplay);
				
				addResults(row, summary, "summary");
				
				List<EncounterType> encTypes = dataSetDefinition.getEncounterTypes();
				
				for (EncounterType et : encTypes) {
					SimpleDataSet encType = getEncounterSummaryDataSetForLocation(et, encs, dataSetDefinition, context);
					addResults(row, encType, et.getName());
				}
				
				SimpleDataSet bigDelay = getExtraLongDelayDataSet(encs, dataSetDefinition, context);
				addResults(row, bigDelay, "delayCases");
				
				resultsSet.addRow(row);
			}
			catch (Exception ex) {
				
				throw new EvaluationException("baseCohort", ex);
			}
		}
	}
	
	private void addResults(DataSetRow row, SimpleDataSet dataSet, String name) {
		
		row.addColumnValue(new DataSetColumn(name, name, SimpleDataSet.class), dataSet);
	}
	
	private String resolveDatabaseColumnName(String value) {
		if (value.equals("cityVillage")) {
			return "city_village";
		} else if (value.equals("stateProvince")) {
			return "state_province";
		} else if (value.equals("postalCode")) {
			return "postal_code";
		} else if (value.equals("countyDistrict")) {
			return "county_district";
		} else if (value.equals("neighbourhoodCell")) {
			return "address3";
		}

		else if (value.equals("townshipDivision")) {
			return "township_division";
		}
		
		return value;
	}
	
	private SimpleDataSet getSummaryDataSetForLocation(List<Encounter> encounters, DataSetDefinition dataSetDefinition,
	                                                   EvaluationContext context) {
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		
		DataSetColumn dataOfficer = new DataSetColumn("dataOfficer", "dataOfficer", String.class);
		dataSet.getMetaData().addColumn(dataOfficer);
		
		DataSetColumn totalEncounters = new DataSetColumn("totalEncounters", "totalEncounters", Integer.class);
		dataSet.getMetaData().addColumn(totalEncounters);
		
		DataSetColumn numberAcceptable = new DataSetColumn("numberAcceptable", "numberAcceptable", Integer.class);
		dataSet.getMetaData().addColumn(numberAcceptable);
		
		DataSetColumn shortestDelay = new DataSetColumn("shortestDelay", "shortestDelay", Integer.class);
		dataSet.getMetaData().addColumn(shortestDelay);
		
		DataSetColumn longestDelay = new DataSetColumn("longestDelay", "longestDelay", Integer.class);
		dataSet.getMetaData().addColumn(longestDelay);
		
		DataSetColumn meanDelay = new DataSetColumn("meanDelay", "meanDelay", String.class);
		dataSet.getMetaData().addColumn(meanDelay);
		
		DataSetColumn modeDelay = new DataSetColumn("modeDelay", "modeDelay", Integer.class);
		dataSet.getMetaData().addColumn(modeDelay);
		
		DataSetColumn proportion = new DataSetColumn("proportion", "proportion", String.class);
		dataSet.getMetaData().addColumn(proportion);
		
		Map<User, List<Encounter>> encounterMap = new HashMap<User, List<Encounter>>();
		for (Encounter enc : encounters) {
			User user = enc.getCreator();
			
			if (encounterMap.containsKey(user)) {
				encounterMap.get(user).add(enc);
			} else {
				List<Encounter> userList = new ArrayList<Encounter>();
				userList.add(enc);
				encounterMap.put(user, userList);
			}
		}
		
		for (User user : encounterMap.keySet()) {
			
			List<Encounter> encs = encounterMap.get(user);
			addEncounterValues(encs, user.getGivenName() + " " + user.getFamilyName(), dataSet, dataOfficer, totalEncounters, numberAcceptable, proportion,  longestDelay, shortestDelay, meanDelay, modeDelay);
		}
		
		addEncounterValues(encounters, "Total", dataSet, dataOfficer, totalEncounters, numberAcceptable, proportion,  longestDelay, shortestDelay, meanDelay, modeDelay);
		return dataSet;
	}
	
	private void addEncounterValues(List<Encounter> encounters, String userName, SimpleDataSet dataSet, DataSetColumn dataOfficer, DataSetColumn totalEncounters, DataSetColumn numberAcceptable, DataSetColumn proportion, DataSetColumn longestDelay, DataSetColumn shortestDelay, DataSetColumn meanDelay, DataSetColumn modeDelay)
	{
		DataSetRow row = new DataSetRow();
		
		row.addColumnValue(dataOfficer, userName);
		
		row.addColumnValue(totalEncounters, encounters.size());
		
		Integer longest = null;
		Integer shortest = null;
		double mean = 0;
		int[] mode = new int[encounters.size()];
		int i = 0;
		Integer acceptable = 0;
		
		Integer acceptance = gp.getGlobalPropertyAsInt(GlobalPropertiesManagement.DATA_ENTRY_DELAY_ACCEPTABLE);
		
		for (Encounter e : encounters) {
			Date created = e.getDateCreated();
			Date encDate = e.getEncounterDatetime();
			
			long diff = created.getTime() - encDate.getTime();
			diff = (diff / (1000 * 60 * 60 * 24));
			
			Integer delay = (int) diff;
			
			if(delay <= acceptance)
			{
				acceptable++;
			}
			
			if (longest == null || delay > longest) {
				longest = delay;
			}
			if (shortest == null || delay < shortest) {
				shortest = delay;
			}
			mean = mean + delay;
			mode[i] = delay;
			
			i++;
		}
		row.addColumnValue(longestDelay, longest);
		row.addColumnValue(shortestDelay, shortest);
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		
		if(encounters.size() > 0)
		{
			double numerator = acceptable;
			double denominator = encounters.size();
			
			double prop = numerator/denominator;
			row.addColumnValue(proportion, df.format(prop));
		}
		else
		{
			row.addColumnValue(proportion, "");
		}
		
		mean = mean / encounters.size();
		
		row.addColumnValue(meanDelay, df.format(mean));
		
		row.addColumnValue(modeDelay, getModeDelay(mode));
		
		row.addColumnValue(numberAcceptable, acceptable);
		
		dataSet.addRow(row);
	}
	
	private SimpleDataSet getEncounterSummaryDataSetForLocation(EncounterType encounterType, List<Encounter> encounters,
	                                                            DataSetDefinition dataSetDefinition,
	                                                            EvaluationContext context) {
		
		Map<User, List<Encounter>> encounterMap = new HashMap<User, List<Encounter>>();
		List<Encounter> encountersOfType = new ArrayList<Encounter>();
		for (Encounter enc : encounters) {
			if (enc.getEncounterType().equals(encounterType)) {
				
				encountersOfType.add(enc);
				
				User user = enc.getCreator();
				
				if (encounterMap.containsKey(user)) {
					encounterMap.get(user).add(enc);
				} else {
					List<Encounter> userList = new ArrayList<Encounter>();
					userList.add(enc);
					encounterMap.put(user, userList);
				}
			}
		}
		
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		
		DataSetColumn dataOfficer = new DataSetColumn("dataOfficer", "dataOfficer", String.class);
		dataSet.getMetaData().addColumn(dataOfficer);
		
		DataSetColumn totalEncounters = new DataSetColumn("totalEncounters", "totalEncounters", Integer.class);
		dataSet.getMetaData().addColumn(totalEncounters);
		
		DataSetColumn shortestDelay = new DataSetColumn("shortestDelay", "shortestDelay", Integer.class);
		dataSet.getMetaData().addColumn(shortestDelay);
		
		DataSetColumn longestDelay = new DataSetColumn("longestDelay", "longestDelay", Integer.class);
		dataSet.getMetaData().addColumn(longestDelay);
		
		DataSetColumn meanDelay = new DataSetColumn("meanDelay", "meanDelay", String.class);
		dataSet.getMetaData().addColumn(meanDelay);
		
		DataSetColumn modeDelay = new DataSetColumn("modeDelay", "modeDelay", Integer.class);
		dataSet.getMetaData().addColumn(modeDelay);
		
		DataSetColumn numberAcceptable = new DataSetColumn("numberAcceptable", "numberAcceptable", Integer.class);
		dataSet.getMetaData().addColumn(numberAcceptable);
		
		DataSetColumn proportion = new DataSetColumn("proportion", "proportion", String.class);
		dataSet.getMetaData().addColumn(proportion);
		
		if (encounterMap.size() > 0) {
			for (User user : encounterMap.keySet()) {
								
				List<Encounter> encs = encounterMap.get(user);
				addEncounterValues(encs, user.getGivenName() + " " + user.getFamilyName(), dataSet, dataOfficer, totalEncounters, numberAcceptable, proportion, longestDelay, shortestDelay, meanDelay, modeDelay);
			}
			addEncounterValues(encountersOfType, "Total", dataSet, dataOfficer, totalEncounters, numberAcceptable, proportion, longestDelay, shortestDelay, meanDelay, modeDelay);
		}
		else
		{
			DataSetRow row = new DataSetRow();
			
			row.addColumnValue(dataOfficer, "");
			row.addColumnValue(totalEncounters, "");
			row.addColumnValue(shortestDelay, "");
			row.addColumnValue(longestDelay, "");
			row.addColumnValue(meanDelay, "");
			row.addColumnValue(modeDelay, "");
			row.addColumnValue(numberAcceptable, "");
			row.addColumnValue(proportion, "");
			dataSet.addRow(row);
		}
		return dataSet;
	}
	
	private SimpleDataSet getExtraLongDelayDataSet(List<Encounter> encounters,
	                                                            DataSetDefinition dataSetDefinition,
	                                                            EvaluationContext context) {
		
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		
		DataSetColumn encounterId = new DataSetColumn("encounterId", "encounterId", String.class);
		dataSet.getMetaData().addColumn(encounterId);
		
		DataSetColumn dataOfficer = new DataSetColumn("dataOfficer", "dataOfficer", String.class);
		dataSet.getMetaData().addColumn(dataOfficer);
		
		DataSetColumn delay = new DataSetColumn("delay", "delay", Integer.class);
		dataSet.getMetaData().addColumn(delay);
		
		DataSetColumn encounterType = new DataSetColumn("encounterType", "encounterType", String.class);
		dataSet.getMetaData().addColumn(encounterType);
		
		boolean includeBlank = true;
		for(Encounter e: encounters)
		{
			Date created = e.getDateCreated();
			Date encDate = e.getEncounterDatetime();
			
			long diff = created.getTime() - encDate.getTime();
			diff = (diff / (1000 * 60 * 60 * 24));
			
			int delayThreshold = gp.getGlobalPropertyAsInt(GlobalPropertiesManagement.DATA_ENTRY_DELAY);
			if(diff > delayThreshold)
			{
				includeBlank = false;
				DataSetRow row = new DataSetRow();
				
				User user = e.getCreator();
				row.addColumnValue(dataOfficer, user.getGivenName() + " " + user.getFamilyName());
				
				row.addColumnValue(encounterId, e.getId());
				row.addColumnValue(delay, diff);
				row.addColumnValue(encounterType, e.getEncounterType().getName());
				
				dataSet.addRow(row);
			}
		}
		
		if(includeBlank)
		{
			DataSetRow row = new DataSetRow();
			
			row.addColumnValue(dataOfficer, "");
			
			row.addColumnValue(encounterId, "");
			row.addColumnValue(delay, "");
			row.addColumnValue(encounterType, "");
			
			dataSet.addRow(row);
		}
		return dataSet;
	}
	
	private Integer getModeDelay(int a[]) {
		int maxValue = 0;
		int maxCount = 0;
		
		for (int i = 0; i < a.length; ++i) {
			int count = 0;
			for (int j = 0; j < a.length; ++j) {
				if (a[j] == a[i])
					++count;
			}
			if (count > maxCount) {
				maxCount = count;
				maxValue = a[i];
			}
		}
		
		return maxValue;
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
