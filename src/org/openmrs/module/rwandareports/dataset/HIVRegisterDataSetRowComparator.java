package org.openmrs.module.rwandareports.dataset;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;


public class HIVRegisterDataSetRowComparator implements Comparator<DataSetRow>{

	List<DataSetColumn> columnList;
	
	public HIVRegisterDataSetRowComparator(DataSet dataset)
	{
		columnList = dataset.getMetaData().getColumns();
	}
	
	public int compare(DataSetRow row1, DataSetRow row2) {
	    
		Date startingDate1 = (Date) row1.getColumnValue(columnList.get(0));
		Date startingDate2 = (Date) row2.getColumnValue(columnList.get(0));
		
		return startingDate1.compareTo(startingDate2);
    }

}
