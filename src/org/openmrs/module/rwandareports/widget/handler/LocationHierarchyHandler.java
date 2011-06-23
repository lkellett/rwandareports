/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.widget.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlwidgets.util.ReflectionUtil;
import org.openmrs.module.htmlwidgets.web.WidgetConfig;
import org.openmrs.module.htmlwidgets.web.handler.CodedHandler;
import org.openmrs.module.htmlwidgets.web.html.CodedWidget;
import org.openmrs.module.htmlwidgets.web.html.Option;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

/**
 * FieldGenHandler for Enumerated Types
 */
@Handler(supports={LocationHierarchy.class}, order=1)
public class LocationHierarchyHandler extends CodedHandler {
	
	private String hierarchy = null;
	/** 
	 * @see CodedHandler#populateOptions(WidgetConfig, CodedWidget)
	 */
	@Override
public void populateOptions(WidgetConfig config, CodedWidget widget) {
		
		List<String> usedLocations = new ArrayList<String>();
		
		hierarchy = config.getAttributeValue("hierarchyField");
		
		if(hierarchy != null && hierarchy.trim().length() > 0)
		{
			List<Location> locations = Context.getLocationService().getAllLocations(false);
			
			for(Location l: locations)
			{
				String hierarchyValue = (String)ReflectionUtil.getPropertyValue(l, hierarchy);
				if(hierarchyValue != null && hierarchyValue.trim().length() > 0 && !usedLocations.contains(hierarchyValue))
				{
					widget.addOption(new Option(hierarchyValue, hierarchyValue, null, hierarchyValue), config);
					usedLocations.add(hierarchyValue);
				}
			}	
		}
	}
	
	/** 
	 * @see WidgetHandler#parse(String, Class<?>)
	 */
	@Override
	public Object parse(String input, Class<?> type) {
		if (StringUtils.isNotBlank(input)) {
			LocationHierarchy location = new LocationHierarchy(hierarchy);
			location.setValue(input);
			return location;
		}
		return null;
	}
	
}
