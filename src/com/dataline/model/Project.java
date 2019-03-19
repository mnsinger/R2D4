package com.dataline.model;

import java.util.Date;
import java.util.HashMap;

public class Project {
	
	static HashMap<String, String> colMap;

	public Project() { 

		colMap = new HashMap<String, String>();
		
		colMap.put("automation", "");
		colMap.put("cross_ref", "");
		colMap.put("delivery_date", "");
		colMap.put("description", "Project Description");
		colMap.put("dev_notes", "");
		colMap.put("developer", "");
		colMap.put("est_delivery_dte", "");
		colMap.put("estimated_hours", "");
		colMap.put("irb_waiver", "");
		colMap.put("objs_delivered", "");
		colMap.put("priority", "");
		colMap.put("privacy_type", "");
		colMap.put("program_specs", "");
		colMap.put("project_notes", "");
		colMap.put("purpose_of_request", "");
		colMap.put("received_by", "");
		colMap.put("received_date", "");
		colMap.put("reqd_deliv_date", "");
		colMap.put("request_description", "");
		colMap.put("request_type", "");
		colMap.put("requester", "Requester_ID");
		colMap.put("requester_title", "");
		colMap.put("schedule_status", "");
		colMap.put("second_developer", "");
		colMap.put("sn_code", "");
		colMap.put("sql", "");
		colMap.put("start_date", "");
		colMap.put("status", "");
		colMap.put("technical_specs", "");
		colMap.put("test_plan", "");
	
	}

	//colMap.put("a", "a");
	
	//.put("cross_ref", "Project Code Cross Reference");
	
	//public String sn_code="";
	
	/*

	String projectid, projectname, pmname, fundsource, projectphase, location, address, latlng;
	
	public String getPmname() {
		return pmname;
	}

	public void setPmname(String pmname) {
		this.pmname = pmname;
	}

	double fars, etpc;
	
	Date projectstart, beneficialocc;
	
	public Project() { super(); }
	
	public Project(String projectid, String projectname, String pmname, String fundsource, String projectphase, String location, 
			double fars, double etpc, Date projectstart, Date beneficialocc,
			String address, String latlng) {
		super();
		
		this.projectid = projectid;
		this.pmname = pmname;
		this.projectname = projectname;
		this.fundsource = fundsource;
		this.projectphase = projectphase;
		this.location = location;
		this.address = address;
		this.latlng = latlng;
		
		this.fars = fars;
		this.etpc = etpc;
		
		this.beneficialocc = beneficialocc;
		this.projectstart = projectstart;
	}

	public String getProjectid() {
		return projectid;
	}

	public void setProjectid(String projectid) {
		this.projectid = projectid;
	}

	public String getProjectname() {
		return projectname;
	}

	public void setProjectname(String projectname) {
		this.projectname = projectname;
	}

	public String getFundsource() {
		return fundsource;
	}

	public void setFundsource(String fundsource) {
		this.fundsource = fundsource;
	}

	public String getProjectphase() {
		return projectphase;
	}

	public void setProjectphase(String projectphase) {
		this.projectphase = projectphase;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLatlng() {
		return latlng;
	}

	public void setLatlng(String latlng) {
		this.latlng = latlng;
	}

	public double getFars() {
		return fars;
	}

	public void setFars(double fars) {
		this.fars = fars;
	}

	public double getEtpc() {
		return etpc;
	}

	public void setEtpc(double etpc) {
		this.etpc = etpc;
	}

	public Date getProjectstart() {
		return projectstart;
	}

	public void setProjectstart(Date projectstart) {
		this.projectstart = projectstart;
	}

	public Date getBeneficialocc() {
		return beneficialocc;
	}

	public void setBeneficialocc(Date beneficialocc) {
		this.beneficialocc = beneficialocc;
	}
	
	*/

}
