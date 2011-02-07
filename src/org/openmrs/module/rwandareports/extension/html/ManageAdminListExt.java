package org.openmrs.module.rwandareports.extension.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class ManageAdminListExt extends AdministrationSectionExt {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "reporting.manage.title";
	}
	
	public String getRequiredPrivilege() {
		return "Manage Rwanda Report Definitions";
	}
	
	public Map<String, String> getLinks() {
		// Using linked hash map to keep order of links
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("module/rwandareports/rwandareports.form", "rwandareports.rwandareports.title");
		return map;
	}
	
}
