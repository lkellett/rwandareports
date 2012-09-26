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
package org.openmrs.module.rwandareports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.rwandareports.util.CleanReportingTablesAndRegisterAllReports;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class RwandaReportsModuleActivator extends BaseModuleActivator {
	
	private static Log log = LogFactory.getLog(RwandaReportsModuleActivator.class);
	
	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void started() {
		log.info("Starting Rwanda Report Module Config");
		
		try {
			String version = ModuleFactory.getModuleById("rwandareports").getVersion();
			String oldversion = Context.getAdministrationService().getGlobalProperty("reports.moduleVersion");
			if(!version.equals(oldversion)){
				CleanReportingTablesAndRegisterAllReports.cleanTables();
				CleanReportingTablesAndRegisterAllReports.registerReports();
				Context.getAdministrationService().saveGlobalProperty(new GlobalProperty("reports.moduleVersion", version));
			}
		}
		catch (Exception ex) {
			log.error("One of reports has an error which blocks it and other reports to be registered");
			ex.printStackTrace();
		}
	}
	
	/**
	 * @see org.openmrs.module.Activator#shutdown()
	 */
	public void stopped() {
		log.info("Stopped Rwanda Report Module");
	}
	
}
