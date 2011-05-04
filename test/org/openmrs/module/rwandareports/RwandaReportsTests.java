package org.openmrs.module.rwandareports;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.query.service.ObjectGroupQueryService;
import org.openmrs.module.rwandareports.objectgroup.service.ObjectGroupDefinitionService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.util.Assert;


public class RwandaReportsTests extends BaseModuleContextSensitiveTest  {
	
	@Override
    public Boolean useInMemoryDatabase(){
        return true;
    }
	

	@Test
    public void should_loadServices() {
		
		ObjectGroupQueryService eqs = Context.getService(ObjectGroupQueryService.class);
		ObjectGroupDefinitionService eds = Context.getService(ObjectGroupDefinitionService.class);

		Assert.notNull(eqs);
		Assert.notNull(eds);
       
    }
	
	/**
	 * runs a simple query returning encounter_id, patient_id, checks for instantiation of ObjectGroup of length > 0
	 */
	@Test
    public void should_testObjectGroupDefinitionService() {
		
		ObjectGroupQueryService eqs = Context.getService(ObjectGroupQueryService.class);
		String query = "select encounter_id, patient_id from encounter where location_id = :location";
		Map<String, Object> params = new HashMap<String, Object>();
		Location loc = Context.getLocationService().getLocation(1);
		Assert.notNull(loc);
		params.put("location", (Object) loc);
		ObjectGroup eg = eqs.executeSqlQuery(query, params);
		System.out.println(eg.size());
		Assert.notNull(eg);
		Assert.isTrue(eg.size() > 1);
	 
    }
	
	
}
