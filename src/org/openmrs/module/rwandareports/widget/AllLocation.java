package org.openmrs.module.rwandareports.widget;


public class AllLocation {
	
	public static String LOCATION = "location";

	String hierarchy;
	String value;
	
	boolean allSites = false;
	

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public boolean isAllSites() {
		return allSites;
	}

	public void setAllSites(boolean allSites) {
		this.allSites = allSites;
	}
}
