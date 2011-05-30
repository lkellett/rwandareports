package org.openmrs.module.rwandareports.filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class DateFormatFilter implements ResultFilter {
	
	private String finalDateFormat = null;
	
	public String filter(String result) {
		if(result != null && result.indexOf("00:") > -1)
		{
			result = result.substring(0, result.indexOf("00:")).trim();
		}
		
		if(finalDateFormat != null)
		{
			
			Date resultDate = null;
			try {
				SimpleDateFormat sdf = new SimpleDateFormat();
				resultDate = sdf.parse(result);
			} catch (ParseException e) {
				
				try{
					SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yyyy");
					resultDate = sdf1.parse(result);
				} catch(ParseException e1)
				{
					try{
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMMM yyyy");
						resultDate = sdf2.parse(result);
					} catch(ParseException e2)
					{
						e2.printStackTrace();
					}
				}
			}
			
			if(resultDate != null)
			{
				result = new SimpleDateFormat(finalDateFormat).format(resultDate);
			}
			
		}
		
		return result;
	}


	public String getFinalDateFormat() {
		return finalDateFormat;
	}

	public void setFinalDateFormat(String finalDateFormat) {
		this.finalDateFormat = finalDateFormat;
	}
	
	
}
