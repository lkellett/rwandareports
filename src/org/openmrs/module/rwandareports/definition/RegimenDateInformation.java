package org.openmrs.module.rwandareports.definition;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.BasePatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;

public class RegimenDateInformation extends BasePatientData implements RowPerPatientData {

	@ConfigurationProperty(required=false)
	private Integer regimen = null;
	
	@ConfigurationProperty(required=false)
	private Concept indication = null;
	
	@ConfigurationProperty(required=false)
	private String dateFormat = "dd/MM/yyyy";
	
	@ConfigurationProperty(required=false)
	private Date asOfDate = null;
	
	@ConfigurationProperty(required=false)
	private Date untilDate = null;

	
    public Integer getRegimen() {
    	return regimen;
    }
	
    public void setRegimen(Integer regimen) {
    	this.regimen = regimen;
    }
	
    public Concept getIndication() {
    	return indication;
    }

    public void setIndication(Concept indication) {
    	this.indication = indication;
    }
	
    public String getDateFormat() {
    	return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
    	this.dateFormat = dateFormat;
    }

	
    public Date getAsOfDate() {
    	return asOfDate;
    }

	
    public void setAsOfDate(Date asOfDate) {
    	this.asOfDate = asOfDate;
    }

	
    public Date getUntilDate() {
    	return untilDate;
    }

    public void setUntilDate(Date untilDate) {
    	this.untilDate = untilDate;
    }
}