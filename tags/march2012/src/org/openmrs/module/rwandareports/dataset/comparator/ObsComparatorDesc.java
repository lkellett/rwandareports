package org.openmrs.module.rwandareports.dataset.comparator;

import java.util.Comparator;

import org.openmrs.Obs;


public class ObsComparatorDesc implements Comparator<Obs>{

	
	public int compare(Obs ob1, Obs ob2) {
		
		if(ob1 != null && ob2 != null)
		{
			if(ob1.getObsDatetime().after(ob2.getObsDatetime()))
			{
				return -2;
			}
			else if(ob2.getObsDatetime().after(ob1.getObsDatetime()))
			{
				return 2;
			}
			else
			{
				return 0;
			}
		}
		
		if(ob1 != null && ob2 == null)
		{
			return 1;
		}
		else if(ob1 == null && ob2 != null)
		{
			return -1;
		}
		return 0;
    }

}