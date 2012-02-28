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
package org.openmrs.module.rwandareports.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

/**
 * This provides a default abstract implementation
 */
public abstract class BaseReportSetup {
	protected final static Log log = LogFactory.getLog(BaseReportSetup.class);
	protected Helper h = new Helper();

	protected boolean isSaveIndicatorsIndependentlyOfReportDefinition() {
    	Boolean saveIndependently = Boolean.valueOf(getGPAsString("rwandareports.saveIndicatorsIndependentlyOfReportDefinition"));
    	return saveIndependently;
    }
	
	public abstract void setup() throws Exception;
	
	public void delete() {
		if ( isSaveIndicatorsIndependentlyOfReportDefinition()) {
			ReportDefinition rd = createReportDefinition();
			delete(rd);
		} else {
			delete(null);
		}		
	}
    protected abstract void delete(ReportDefinition rd);
    //protected abstract void delete();
    
    protected abstract ReportDefinition createReportDefinition();

	/**
	 * Read a global property by key as Integer
	 * 
	 * @param key
	 * @return
	 */
	public int getGPAsInteger(String key) {
    	String globalProperty = Context.getAdministrationService().getGlobalProperty(key);
    	if (globalProperty == null || globalProperty.length()<1) {
    		log.warn("Global property ["+ key +"] isn't set");
    	}
		Integer returnValue = Integer.valueOf(globalProperty);
		return returnValue;
    }
	
	/**
	 * Read a global property by key as String
	 * 
	 * @param key
	 * @return
	 */
	public String getGPAsString(String key) {
    	String globalProperty = Context.getAdministrationService().getGlobalProperty(key);
    	if (globalProperty == null || globalProperty.length()<1) {
    		log.warn("Global property ["+ key +"] isn't set");
    	}
		return globalProperty;
    }
	
}
