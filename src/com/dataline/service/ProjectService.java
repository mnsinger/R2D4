package com.dataline.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.dataline.dao.*;
import com.dataline.model.*;

@Transactional
@Service
public class ProjectService {
	@Autowired
	private DataDaoImpl dataDaoImpl;
	
	 public JSONArray initialRetrieve(String username) throws SQLException, InterruptedException {
		 return dataDaoImpl.initialRetrieve(username);
	 }

	 public JSONArray getProjects(String username, String projectCode, String requester, String developer, String description, String completionStatus, 
			 String irbWaiver, String crossRef, String purposeOfReq, String delivery_plan, String technicalSpecs, String testPlan, String sql, String any, String and_or) throws InterruptedException, SQLException {
		 return dataDaoImpl.getProjects(username, projectCode, requester, developer, description, completionStatus,
				 irbWaiver, crossRef, purposeOfReq, delivery_plan, technicalSpecs, testPlan, sql, any, and_or);
	 }

	 public JSONObject getProjectInfo(String projectid, String sn, String version) throws SQLException {
		 return dataDaoImpl.getProjectInfo(projectid, sn, version);
	 }

	 public JSONArray determineCustodians(String username, String tableList, String projectCode) {
		 return dataDaoImpl.determineCustodians(username, tableList, projectCode);
	 }

	 public String determineTablesFromSQLText(String username, String sqlCode, String currentTables) {
		 return dataDaoImpl.determineTablesFromSQLText(username, sqlCode, currentTables);
	 }

	 public JSONArray custodianTablesSearch(String searchString) {
		 return dataDaoImpl.custodianTablesSearch(searchString);
	 }

	 public String projectCommit(String formData) throws SQLException {
		 return dataDaoImpl.projectCommit(formData);
	 }

	 public String addAmendment(String sn, String amendmentDeveloper, String amendmentDate, String amendmentNote) throws SQLException {
		 return dataDaoImpl.addAmendment(sn, amendmentDeveloper, amendmentDate, amendmentNote);
	 }

	 public String removeAmendment(String id) throws SQLException {
		 return dataDaoImpl.removeAmendment(id);
	 }

	 /*public String createPlan(String projectCode, String path) throws IOException, SQLException {
		 return dataDaoImpl.createPlan(projectCode, path);
	 }*/

	 public JSONArray custodianSearch(String searchString) throws SQLException {
		 return dataDaoImpl.custodianSearch(searchString);
	 }

	 public String deleteVersion(String projectCode, String sn, String version) throws SQLException {
		 return dataDaoImpl.deleteVersion(projectCode, sn, version);
	 }

	 public String sendMail(String from, String to, String msg) {
		 return dataDaoImpl.sendMail(from, to, msg);
	 }

	 public String determineIfFolderExists(String folder) {
		 return dataDaoImpl.determineIfFolderExists(folder);
	 }

	 public String createProjectFolder(String folder) throws IOException {
		 return dataDaoImpl.createProjectFolder(folder);
	 }

	 public String sendEmailDialog(String formData, String username) throws SQLException, IOException {
		 return dataDaoImpl.sendEmailDialog(formData, username);
	 }

	 public String sendEmailDialogMessage(String formData) throws SQLException, IOException {
		 return dataDaoImpl.sendEmailDialogMessage(formData);
	 }

	public String addApproval(int sn, String custodian, String table, String projectCode) throws SQLException {
		return dataDaoImpl.addApproval(sn, custodian, table, projectCode);
	}

	public String removeApproval(int sn, String custodian, String table, String projectCode) throws SQLException {
		return dataDaoImpl.removeApproval(sn, custodian, table, projectCode);	
	}

	public String removeAllApprovals(int sn) throws SQLException {
		return dataDaoImpl.removeAllApprovals(sn);	
	}

	public String searchSn(String sn) throws SQLException {
		return dataDaoImpl.searchSn(sn);
	}

	public JSONArray getSchedule() throws SQLException {
		return dataDaoImpl.getSchedule();
	}

	public String schedulerCreateNewJob(String formData) throws SQLException {
		return dataDaoImpl.schedulerCreateNewJob(formData);
	}

	public JSONObject schedulerInitialRetrieve() throws SQLException {
		return dataDaoImpl.schedulerInitialRetrieve();
	}

	public String schedulerUpdateExistingJob(String formData) throws SQLException {
		return dataDaoImpl.schedulerUpdateExistingJob(formData);
	}

	public String deleteJob(String id) throws SQLException {
		return dataDaoImpl.deleteJob(id);
	}

	public String generateExcel(String formData) throws IOException, SQLException {
		return dataDaoImpl.generateExcel(formData);
	}

	public String projectCopy(String formData) throws SQLException {
		 return dataDaoImpl.projectCopy(formData);
	}

	public JSONArray getScheduleLog() throws SQLException {
		return dataDaoImpl.getScheduleLog();
	}

	public JSONObject refreshProjectFromR2d3(String sn) throws SQLException {
		return dataDaoImpl.refreshProjectFromR2d3(sn);
	}

	public String overrideCustodian(int sn, String custodian, String currentOverride) throws SQLException {
		return dataDaoImpl.overrideCustodian(sn, custodian, currentOverride);
	}

	public String overrideRequester(int sn, String projectCode, String currentOverride) throws SQLException {
		return dataDaoImpl.overrideRequester(sn, projectCode, currentOverride);
	}

	public String runJob(String id) throws SQLException {
		return dataDaoImpl.runJob(id);
	}

	public String generateWord(String formData) throws InvalidFormatException, IOException {
		return dataDaoImpl.generateWord(formData);
	}

	public JSONArray getProjectEmails(String projectCode, String username, String developerEmail, String password) throws Exception {
		return dataDaoImpl.getProjectEmails(projectCode, username, developerEmail, password);
	}

	public JSONObject getProjectEmailById(String emailId, String developerEmail, String password) throws Exception {
		return dataDaoImpl.getProjectEmailById(emailId, developerEmail, password);
	}

	public String copyEmailById(String emailId, String folder, String developerEmail, String password) throws Exception {
		return dataDaoImpl.copyEmailById(emailId, folder, developerEmail, password);
	}

	public String copyAllEmails(String folder, JSONArray emailsArrayJSON, String developerEmail, String password) throws JSONException, Exception {
		return dataDaoImpl.copyAllEmails(folder, emailsArrayJSON, developerEmail, password);
	}

	public JSONObject initialUserPrefsRetrieve(String username, String developerEmail, String password) throws Exception {
		// TODO Auto-generated method stub
		return dataDaoImpl.initialUserPrefsRetrieve(username, developerEmail, password);
	}

	public String updateUserPrefs(String formData, String username) throws SQLException {
		return dataDaoImpl.updateUserPrefs(formData, username);
	}

	public String addRequester(String add_requester_email) throws SQLException {
		return dataDaoImpl.addRequester(add_requester_email);
	}

	public String sendOutlookUserPrefs(String username, String message_type) throws SQLException {
		return dataDaoImpl.sendOutlookUserPrefs(username, message_type);
	}

}
