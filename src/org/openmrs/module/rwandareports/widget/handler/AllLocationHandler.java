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
import org.openmrs.module.rwandareports.widget.AllLocation;

/**
 * FieldGenHandler for Enumerated Types
 */
@Handler(supports={AllLocation.class}, order=1)
public class AllLocationHandler extends CodedHandler {
	
	
private String[] hierarchy = null;
/** 
 * @see CodedHandler#populateOptions(WidgetConfig, CodedWidget)
 */
@Override
public void populateOptions(WidgetConfig config, CodedWidget widget) {
		
		List<String> usedLocations = new ArrayList<String>();
		
		widget.addOption(new Option("All Sites", Context.getMessageSourceService().getMessage("rwandareports.allsites"), Context.getMessageSourceService().getMessage("rwandareports.allsites"), "All Sites"), config);
		
		List<Location> locations = Context.getLocationService().getAllLocations(false);
		
		String hierarchyConfig = config.getAttributeValue("hierarchyFields");
		
		if(hierarchyConfig != null && hierarchyConfig.trim().length() > 0)
		{
			hierarchy = hierarchyConfig.split(",");
		}
		
		if(hierarchy != null)
		{	
			for(String h: hierarchy)
			{
				String[] hConfig = h.split(":");
				String hVal = hConfig[0];
				String hDisplay;
				if(hConfig.length > 1)
				{
					hDisplay = hConfig[1];
				}
				else
				{
					hDisplay = hVal;
				}
				
				for(Location l: locations)
				{
					String hierarchyValue = (String)ReflectionUtil.getPropertyValue(l, hVal);
					if(hierarchyValue != null && hierarchyValue.trim().length() > 0 && !usedLocations.contains(hVal + "&&&" + hierarchyValue))
					{
						widget.addOption(new Option(hDisplay + "***" + hVal + "&&&" + hierarchyValue, hDisplay + " - " + hierarchyValue, hDisplay + " - " + hierarchyValue, hDisplay + " - " + hierarchyValue), config);
						usedLocations.add(hVal + "&&&" + hierarchyValue);
					}
				}	
			}
		}
		
		for(Location l: locations)
		{
			widget.addOption(new Option(l.getName(), l.getName(), l.getName(), l.getName()), config);
		}
	}
	
	/** 
	 * @see WidgetHandler#parse(String, Class<?>)
	 */
	@Override
	public Object parse(String input, Class<?> type) {
		if (StringUtils.isNotBlank(input)) {
			
			AllLocation location = new AllLocation();
			if(input.equals("All Sites"))
			{
				location.setAllSites(true);
				
				StringBuilder h = new StringBuilder();
				if(hierarchy != null)
				{
					int index = 0;
					for(String hVal: hierarchy)
					{
						if(index > 0)
						{
							h.append(",");
						}
						h.append(hVal);
						index++;
					}
				}
				location.setHierarchy(h.toString());
			}
			else if(input.indexOf("&&&") > -1)
			{
				location.setHierarchy(input.substring(input.indexOf("***") + 3,input.indexOf("&&&")));
				location.setDisplayHierarchy(input.substring(0,input.indexOf("***")));
				String value = input.substring(input.indexOf("&&&") + 3);
				location.setValue(value);
			}
			else
			{
				location.setHierarchy(AllLocation.LOCATION);
				location.setValue(input);
			}
			
			return location;
		}
		return null;
	}
	
}
