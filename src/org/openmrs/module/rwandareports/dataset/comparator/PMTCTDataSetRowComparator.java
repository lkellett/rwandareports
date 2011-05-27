package org.openmrs.module.rwandareports.dataset.comparator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.openmrs.module.reporting.dataset.DataSetRow;


public class PMTCTDataSetRowComparator implements Comparator<DataSetRow>{

	
	public int compare(DataSetRow row1, DataSetRow row2) {
		
		String nextVisit1 = (String)row1.getColumnValue("nextVisit");
		String nextVisit2 = (String)row2.getColumnValue("nextVisit");
		
		if(nextVisit1 != null && nextVisit2 != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			
			Date startingDate1;
			try {
				startingDate1 = sdf.parse(nextVisit1);
				Date startingDate2 = sdf.parse(nextVisit2);
				
				return startingDate1.compareTo(startingDate2);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(nextVisit1 != null && nextVisit2 == null)
		{
			return 1;
		}
		else if(nextVisit1 == null && nextVisit2 != null)
		{
			return -1;
		}
		return 0;
    }

}