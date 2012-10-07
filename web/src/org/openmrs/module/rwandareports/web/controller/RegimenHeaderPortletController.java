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
package org.openmrs.module.rwandareports.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.ExtendedDrugOrder;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.web.controller.PortletController;
import org.springframework.stereotype.Controller;

/**
 * The main controller.
 */
@Controller
public class RegimenHeaderPortletController extends PortletController {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	/**
	 * @see PortletController#populateModel(HttpServletRequest, Map)
	 */
	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {

		Concept iv = gp.getConcept(GlobalPropertiesManagement.IV_CONCEPT);
		Patient patient = Context.getPatientService().getPatient((Integer) model.get("patientId"));
    	
		List<DrugOrder> allDrugOrders = Context.getOrderService().getDrugOrdersByPatient(patient);
		List<DrugRegimen> regimens = new ArrayList<DrugRegimen>();
		
		Calendar compareDate = Calendar.getInstance();
		compareDate.add(Calendar.DAY_OF_YEAR, -7);
		
		for(DrugOrder drugOrder : allDrugOrders)
		{
			if (drugOrder instanceof ExtendedDrugOrder) {
				ExtendedDrugOrder edo = (ExtendedDrugOrder)drugOrder;
				if(edo.getGroup() != null && edo.getGroup() instanceof DrugRegimen) {
					DrugRegimen regimen = (DrugRegimen)edo.getGroup();
					if (!regimens.contains(regimen))
					{
						List<ExtendedDrugOrder> members = regimen.getMembers();
						for(ExtendedDrugOrder order: members)
						{
							if(order.getStartDate().after(compareDate.getTime()) && order.getRoute() != null && order.getRoute().equals(iv))
							{
								regimens.add(regimen);
								break;
							}
						}
					}
				}
			}
		}
		
		if(regimens.size() > 0)
		{
			model.put("regimens", regimens);
		}
    	model.put("patient", patient);
	}
}
