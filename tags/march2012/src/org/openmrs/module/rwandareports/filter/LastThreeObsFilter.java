package org.openmrs.module.rwandareports.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmrs.Obs;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;
import org.openmrs.module.rwandareports.dataset.comparator.ObsComparator;
import org.openmrs.module.rwandareports.dataset.comparator.ObsComparatorDesc;

public class LastThreeObsFilter implements ResultFilter {
	
	
	public Object filter(Object value) {
		
		List<Obs> allObs = (List<Obs>)value;
		
		if(allObs != null)
		{
			Collections.sort(allObs, new ObsComparatorDesc());
		}
		
		if(allObs.size() > 3)
		{
			List<Obs> lastObs = new ArrayList<Obs>();
			
			for(int i = 0; i < 3; i++)
			{
				lastObs.add(allObs.get(i));
				Collections.sort(lastObs, new ObsComparator());
			}
			return lastObs;
		}
		Collections.sort(allObs, new ObsComparator());
		return allObs;
	}

	public Object filterWhenNull() {
	    // TODO Auto-generated method stub
	    return null;
    }
}
