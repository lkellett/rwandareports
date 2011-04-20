package org.openmrs.module.rwandareports.encounter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.module.reporting.ReportingException;


public class EncounterGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Log log = LogFactory.getLog(this.getClass());
	
	private String name;
	
	private String description;
	
	/**
	 * Expects a group of [encounterId,patientId].
	 */
	private Set<Integer[]> memberIds;
	
	
	public EncounterGroup() {
		memberIds = new HashSet<Integer[]>();
	}
	
	/**
	 * 
	 * Sets up the EncounterGroup with encounterGroup members.  Each member is an Integer[], first position is encounter_id, second position is patient_id 
	 * 
	 * @param rows
	 */
	public EncounterGroup(List<Object[]> rows){
		if (memberIds == null)
			memberIds = new HashSet<Integer[]>();
		for (int i = 0; i < rows.size(); i++){
			Object[] oSet = rows.get(i);
			//TODO:  make length variable so that you can return obs values, etc...   ??
			if (oSet.length != 2)
				throw new ReportingException("Encounter Query must return exactly 2 rows:  encounter_id, patient_id");
			Integer[] member = {(Integer) oSet[0], (Integer) oSet[1]};
			memberIds.add(member);
		}
	}
	
	public EncounterGroup(Set<Integer[]> members){
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
	
	public static EncounterGroup intersect(EncounterGroup a, EncounterGroup b) {
		EncounterGroup ret = new EncounterGroup();
		ret.setName("(" + (a == null ? "NULL" : a.getName()) + " * " + (b == null ? "NULL" : b.getName()) + ")");
		if (a != null && b != null) {
			ret.getMemberIds().addAll(a.getMemberIds());
			ret.getMemberIds().retainAll(b.getMemberIds());
		}
		return ret;
	}


    public static EncounterGroup intersect(EncounterGroup a, Cohort b){
    	EncounterGroup ret = new EncounterGroup();
    	for (Integer[] memberId : a.getMemberIds()){
    		if (b.getMemberIds().contains(memberId[1]))
    			ret.addMemberId(memberId);
    	}
    	return ret;
	}
    
    public Cohort getCohort(){
    	Cohort cohort = new Cohort();
    	for (Integer[] i : this.memberIds){
    		cohort.addMember(i[1]);
    	}
    	return cohort;
    }
	
}
