package org.openmrs.module.rwandareports.widget;


public class LocationHierarchy {

	String hierarchy;
	String value;
	
	public LocationHierarchy(String hierarchy)
	{
		this.hierarchy = hierarchy;
	}

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
}
