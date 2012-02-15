package org.openmrs.module.rwandareports.dataset.evaluator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlwidgets.util.ReflectionUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.cohort.definition.util.CohortFilter;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.widget.AllLocation;

@Handler(supports={LocationHierachyIndicatorDataSetDefinition.class})
public class LocationHierachyIndicatorDataSetEvaluator implements DataSetEvaluator {
	
	private static String LOCATION = "LOCATION";
	private static String HIERARCHY = "HIERARCHY";
	private static String ALL_SITES = "ALL_SITES";
	private List<String> keysToCopy = Arrays.asList(new String[] {"locationDisplay" });
	
	public LocationHierachyIndicatorDataSetEvaluator() { }
	
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
		
		LocationHierachyIndicatorDataSetDefinition lhdsd = (LocationHierachyIndicatorDataSetDefinition)dataSetDefinition;
		
		SimpleDataSet ret = new SimpleDataSet(dataSetDefinition, context);
	
		
		AllLocation location = lhdsd.getLocation();
		if(location != null)
		{
			if(!location.isAllSites() && location.getHierarchy().equals(AllLocation.LOCATION))
			{
				addIteration(ret, getBaseCohort(location.getValue(), LOCATION, location.getValue()), location.getValue(), context, lhdsd.getBaseDefinition());
			}
			else if(!location.isAllSites())
			{
				List<Location> allLocations = Context.getLocationService().getAllLocations(false);
				
				addIteration(ret, getBaseCohort(location.getValue(), HIERARCHY, location.getHierarchy()), location.getValue() + " " + location.getHierarchy(), context, lhdsd.getBaseDefinition());
				
				for(Location l: allLocations)
				{
					String hierarchyValue = (String)ReflectionUtil.getPropertyValue(l, location.getHierarchy());
					if(hierarchyValue != null)
					{
						hierarchyValue = hierarchyValue.trim();
						hierarchyValue = hierarchyValue.toUpperCase();
					}	
					
					if(location.getValue() != null && location.getValue().equals(hierarchyValue))
					{
						addIteration(ret, getBaseCohort(l.getName(), LOCATION, l.getName()), l.getName(), context, lhdsd.getBaseDefinition());
					}
				}
			}	
			else
			{
				addIteration(ret, getBaseCohort("All Sites", ALL_SITES, "All Sites"), "All Sites", context, lhdsd.getBaseDefinition());
				
				List<Location> allLocations = Context.getLocationService().getAllLocations(false);
				
				String[] hierarchyToCheck =  null;
				
				if(location.getHierarchy()!=null)
				{
					hierarchyToCheck = location.getHierarchy().split(",");
				}
				
				for(String h: hierarchyToCheck)
				{
					String[] config = h.split(":");
					String hVal = config[0];
					String hDisplay = config[0];
					if(config.length > 0)
					{
						hDisplay = config[1];
					}
					
					TreeSet<String> allLoc = new TreeSet<String>();
					for(Location l: allLocations)
					{
						String hierarchyValue = (String)ReflectionUtil.getPropertyValue(l, hVal);
						if(hierarchyValue != null)
						{
							hierarchyValue = hierarchyValue.trim();
							hierarchyValue = hierarchyValue.toUpperCase();
							allLoc.add(hierarchyValue);	
						}
					}
					
					for(String hLoc: allLoc)
					{
						addIteration(ret, getBaseCohort(hLoc, HIERARCHY, hVal), hLoc + " " + hDisplay, context, lhdsd.getBaseDefinition());
					}
				}
				
				for(Location l: allLocations)
				{
					addIteration(ret, getBaseCohort(l.getName(), LOCATION, l.getName()), l.getName(), context, lhdsd.getBaseDefinition());
				}
			}
		}
		
		return ret;
	}
	
	private SqlCohortDefinition getBaseCohort(String location, String hierarchy, String hierarchyValue)
	{
		
	
		if(hierarchy.equals(LOCATION))
		{
			Location loc = Context.getLocationService().getLocation(location);
		
			SqlCohortDefinition locationCohort = new SqlCohortDefinition();
			locationCohort
			        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = '" +  loc.getLocationId() + "'");
			
			return locationCohort;
		}
		else if(hierarchy.equals(HIERARCHY))
		{
			
			String hVal = resolveDatabaseColumnName(hierarchyValue);
			
			SqlCohortDefinition locationCohort = new SqlCohortDefinition();
			locationCohort
			        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value in (select location_id from location where retired = 0 and " + hVal + " = '" + location + "')");
			
			return locationCohort;
		}
		
		
		return null;
	}
	
	private void addIteration(SimpleDataSet sds, SqlCohortDefinition cohort, String locationDisplay, EvaluationContext context, List<DataSetDefinition> baseDefinition) throws EvaluationException
	{
		for(DataSetDefinition bd: baseDefinition)
		{
		
			EvaluationContext ec = new EvaluationContext(context);
			//EvaluationContext ec = EvaluationContext.cloneForChild(context, new Mapped<DataSetDefinition>(bd, new HashMap<String, Object>()));
			//EvaluationContext ec = EvaluationContext.clone(context);
		
		if(cohort != null)
		{
			try {
				Cohort baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cohort, ec);
				ec.setBaseCohort(baseCohort);
			} catch (Exception ex) {
				
				throw new EvaluationException("baseCohort", ex);
			}
		}
		DataSetRow row = new DataSetRow();
		
		
			
			DataSet ds;
			try {
				ds = (DataSet) Context.getService(DataSetDefinitionService.class).evaluate(bd, ec);
			} catch (Exception ex) {
				throw new EvaluationException("baseDefinition", ex);
			}
		    
		    row.addColumnValue(new DataSetColumn("locationDisplay", "locationDisplay", String.class), locationDisplay);
		    if(bd instanceof CohortIndicatorDataSetDefinition)
		    {
		    	MapDataSet mds = (MapDataSet)ds;
		    	CohortIndicatorDataSetDefinition cidsd = (CohortIndicatorDataSetDefinition)bd;
		    	for (DataSetColumn column : cidsd.getColumns()) {
		    		row.addColumnValue(column, mds.getData(column));
		    	}
		    }
		    else if(bd instanceof RowPerPatientDataSetDefinition)
		    {
		    	row.addColumnValue(new DataSetColumn(bd.getName() + "PatientDataSet", bd.getName() + "PatientDataSet", SimpleDataSet.class), ds);
		    }
		
    	sds.addRow(row);
		}
	}
	
	private String resolveDatabaseColumnName(String value)
	{
		if(value.equals("cityVillage"))
		{
			return "city_village";
		}
		else if(value.equals("stateProvince"))
		{
			return "state_province";
		}
		else if(value.equals("postalCode"))
		{
			return "postal_code";
		}
		else if(value.equals("countyDistrict"))
		{
			return "county_district";
		}
		else if(value.equals("neighbourhoodCell"))
		{
			return "neighbourhood_cell";
		}
		
		else if(value.equals("townshipDivision"))
		{
			return "township_division";
		}
		
		return value;
	}
}
