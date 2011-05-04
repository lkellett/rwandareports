package org.openmrs.module.rwandareports.objectgroup;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.module.reporting.ReportingException;

/**
 * This is the basic unit of the ObjectGroupIndicator class.  And object group is a group of objects, and is designed to resemble an OpenMRS Cohort, just with ObjectGroups.
 * The basic unit within an ObjectGroup is an Integer[object_id, patient_id].  
 * 
 * Any query meant to return an ObjectGroup MUST return exactly two columns, first object_id, then patient_id
 * 
 * The second mandatory value is necessary so that and ObjectGroup can be intersected with a Cohort without instantiating any hibernate-managed OpenMRS objects, with minimal overhead.
 * 
 * @author dthomas
 *
 */
public class ObjectGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Log log = LogFactory.getLog(this.getClass());
	
	private String name;
	
	private String description;
	
	/**
	 * Expects a group of [objectId,patientId].
	 */
	private Set<Integer[]> memberIds;
	
	
	public ObjectGroup() {
		memberIds = new HashSet<Integer[]>();
	}
	
	/**
	 * 
	 * Sets up the ObjectGroup with objectGroup members.  Each member is an Integer[], first position is object_id, second position is patient_id 
	 * 
	 * @param rows
	 */
	public ObjectGroup(List<Object[]> rows){
		if (memberIds == null)
			memberIds = new HashSet<Integer[]>();
		for (int i = 0; i < rows.size(); i++){
			Object[] oSet = rows.get(i);
			//TODO:  make length variable so that you can return obs values, etc...   ??
			if (oSet.length != 2)
				throw new ReportingException("ObjectGroup Query must return exactly 2 rows:  id, patient_id");
			Integer[] member = {(Integer) oSet[0], (Integer) oSet[1]};
			memberIds.add(member);
		}
	}
	
	public ObjectGroup(Set<Integer[]> members){
		memberIds = members;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public Set<Integer[]> getMemberIds() {
		return memberIds;
	}


	public void setMemberIds(Set<Integer[]> memberIds) {
		this.memberIds = memberIds;
	}
	
	public void addMemberId(Integer[] id){
		if (this.memberIds == null)
			memberIds = new HashSet<Integer[]>();
		memberIds.add(id);
	}
	
	public int size() {
		return getMemberIds() == null ? 0 : getMemberIds().size();
	}
	
	public int getSize(){
		return size();
	}
	
	public static ObjectGroup intersect(ObjectGroup a, ObjectGroup b) {
		ObjectGroup ret = new ObjectGroup();
		ret.setName("(" + (a == null ? "NULL" : a.getName()) + " * " + (b == null ? "NULL" : b.getName()) + ")");
		if (a != null && b != null) {
			ret.getMemberIds().addAll(a.getMemberIds());
			ret.getMemberIds().retainAll(b.getMemberIds());
		}
		return ret;
	}


	/**
	 * 
	 * @param a ObjectGroup
	 * @param b Cohort
	 * @return and ObjectGroup containing only ObjectGroups for patients in the Cohort
	 */
    public static ObjectGroup intersect(ObjectGroup a, Cohort b){
    	ObjectGroup ret = new ObjectGroup();
    	for (Integer[] memberId : a.getMemberIds()){
    		if (b.getMemberIds().contains(memberId[1]))
    			ret.addMemberId(memberId);
    	}
    	return ret;
	}
    
    /**
     * 
     * @return a Cohort of all patients who have an object in this ObjectGroup
     */
    public Cohort getCohort(){
    	Cohort cohort = new Cohort();
    	for (Integer[] i : this.memberIds){
    		cohort.addMember(i[1]);
    	}
    	return cohort;
    }
	
}
