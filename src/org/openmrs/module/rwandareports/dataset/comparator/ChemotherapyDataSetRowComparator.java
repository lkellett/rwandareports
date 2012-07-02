package org.openmrs.module.rwandareports.dataset.comparator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.openmrs.module.reporting.dataset.DataSetRow;


public class ChemotherapyDataSetRowComparator implements Comparator<DataSetRow>{

	
	public int compare(DataSetRow row1, DataSetRow row2) {
		
		String scheduledStart1 = (String)row1.getColumnValue("regimen");
		String scheduledStart2 = (String)row2.getColumnValue("regimen");
		
		if(scheduledStart1 != null && scheduledStart2 != null)
		{
			scheduledStart1 = scheduledStart1.substring(scheduledStart1.indexOf("Administration start date:"));
			scheduledStart1 = scheduledStart1.substring(scheduledStart1.indexOf(":") +2);
			
			scheduledStart2 = scheduledStart2.substring(scheduledStart2.indexOf("Administration start date:"));
			scheduledStart2 = scheduledStart2.substring(scheduledStart2.indexOf(":") +2);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date startingDate1;
			try {
				startingDate1 = sdf.parse(scheduledStart1);
				Date startingDate2 = sdf.parse(scheduledStart2);
				
				return startingDate1.compareTo(startingDate2);
			} catch (ParseException e) {
				
				e.printStackTrace();
			}
		}
		
		if(scheduledStart1 != null && scheduledStart2 == null)
		{
			return 1;
		}
		else if(scheduledStart1 == null && scheduledStart2 != null)
		{
			return -1;
		}
		return 0;
    }

}