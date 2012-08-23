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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.rwandareports.reporting.SetupAdultHIVConsultationSheet;
import org.openmrs.module.rwandareports.util.CleanReportingTablesAndRegisterAllReports;

/**
 * This class contains the logic that is run every time this module
 * is either started or shutdown
 */
public class RwandaReportsModuleActivator implements Activator {

	private static Log log = LogFactory.getLog(RwandaReportsModuleActivator.class);

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.info("Starting Rwanda Report Module");
		try{
		CleanReportingTablesAndRegisterAllReports.cleanTables();	
		CleanReportingTablesAndRegisterAllReports.registerReports();
	} catch (Exception ex){
        log.error("One of reports has an error which blocks it and other reports to be registered");
        throw new ModuleException(ex.getMessage());
    }		
		
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.info("Stopping Rwanda Report Module");
	}
	
}
