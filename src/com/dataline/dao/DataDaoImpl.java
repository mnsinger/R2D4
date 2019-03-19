package com.dataline.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.sql.DataSource;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTextBox;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;


public class DataDaoImpl implements DataDao {
	
	public DataDaoImpl(DataSource dataSource) throws IOException {
		super();
		this.dataSource = dataSource;
		
        FileReader reader = new FileReader("C:\\R2D4\\eclipse-workspace\\r2d4.properties");
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line;

        while ((line = bufferedReader.readLine()) != null) {
        	this.propMap.put(line.split("=")[0], line.split("=")[1]);
        }
        
    	//System.out.println("propMap[idb_pswd]=" + propMap.get("idb_pswd"));
    	//System.out.println("propMap[darwin_apps_pswd]=" + propMap.get("darwin_apps_pswd"));
        reader.close();

	}

	public DataSource dataSource;
	
	public HashMap<String, String> propMap = new HashMap<String, String>();
		
	public void setDataSource(DataSource dataSource) {
		System.out.println("HEEEE " + dataSource.toString());
		this.dataSource = dataSource;
	}
	
	public class EmailMessageMSK {
		public String messageId, messageType, senderEmail, subject;
		public Date messageDate;
		
		EmailMessageMSK(String messageId, String messageType, Date messageDate, String senderEmail, String subject) {
			this.messageId = messageId;
			this.messageType = messageType;
			this.messageDate = messageDate;
			this.senderEmail = senderEmail;
			this.subject = subject;
		}
		
	     public int compare(EmailMessageMSK m1, EmailMessageMSK m2) {
	         return m1.messageDate.compareTo(m2.messageDate);
	     }
	     
	     //public String toString() {
	    //	 return "messageId: " + this.messageId
	     //}

	}
	
	public JSONArray initialRetrieve(String username) throws SQLException, InterruptedException {
		Connection conn = null;
		JSONArray arrayJSON = new JSONArray();
		conn = dataSource.getConnection();
		
		//Thread.sleep(5000);

		// DEVELOPERS
		PreparedStatement ps = conn.prepareStatement("select * from dbo.developer_names /*where active = 1*/ order by 1");
		ResultSet rs = ps.executeQuery();

		JSONArray arrayDevelopersJSON = new JSONArray();
		while (rs.next()) {				
			JSONObject projJSONObj = new JSONObject();
			projJSONObj.put("username", rs.getString("username"));
			projJSONObj.put("developer_primary", rs.getString("developer primary"));
			projJSONObj.put("display_name", rs.getString("displayname"));
			arrayDevelopersJSON.put(projJSONObj);
		}
		arrayJSON.put(arrayDevelopersJSON);

		// DIRECTORIES 
		ps = conn.prepareStatement("select * from dbo.deptdirectorytranslation");
		rs = ps.executeQuery();

		JSONObject directoryJSONObj = new JSONObject();

		while (rs.next()) {				
			directoryJSONObj.put(rs.getString("abbrev"), rs.getString("directoryname"));
		}
		arrayJSON.put(directoryJSONObj);

		// AUTOMATION STATUS
		ps = conn.prepareStatement("select * from dbo.automation_status order by 1");
		rs = ps.executeQuery();

		JSONArray arrayAutomationStatusJSON = new JSONArray();
		while (rs.next()) {				
			JSONObject projJSONObj = new JSONObject();
			projJSONObj.put("automation", rs.getString("project manAuto"));
			arrayAutomationStatusJSON.put(projJSONObj);
		}
		arrayJSON.put(arrayAutomationStatusJSON);

		// PRIVACY TYPES
		ps = conn.prepareStatement("select * from dbo.projreqtype order by 1");
		rs = ps.executeQuery();

		JSONArray arrayProjTypeJSON = new JSONArray();
		while (rs.next()) {				
			JSONObject projJSONObj = new JSONObject();
			projJSONObj.put("privacy_type", rs.getString("request type"));
			arrayProjTypeJSON.put(projJSONObj);
		}
		arrayJSON.put(arrayProjTypeJSON);

		// REQUESTERS
		ps = conn.prepareStatement("select \"requester id\", \"requester last name\", \"requester first name\", isnull(\"department name\", '') dept_name, isnull(\"requester title\", '') title from dbo.requestr order by 2");
		rs = ps.executeQuery();

		JSONArray arrayReqTypeJSON = new JSONArray();
		String requesterHTML = "";
		while (rs.next()) {				
			JSONObject projJSONObj = new JSONObject();
			projJSONObj.put("id", rs.getString("requester id"));
			projJSONObj.put("last_name", rs.getString("requester last name"));
			projJSONObj.put("first_name", rs.getString("requester first name"));
			projJSONObj.put("dept_name", rs.getString("dept_name"));
			projJSONObj.put("title", rs.getString("title"));
			arrayReqTypeJSON.put(projJSONObj);
			requesterHTML += "<option value='" + rs.getString("requester id") + "'>" + rs.getString("requester last name") + ", " + rs.getString("requester first name") + " | " + rs.getString("title")  + " | " + rs.getString("dept_name") + "</option>";
		}
		arrayJSON.put(arrayReqTypeJSON);
		arrayJSON.put(requesterHTML);

		conn.close();

		return arrayJSON;

	}
	
	public JSONArray getProjects(String username, String projectCode, String requester, String developer, String description, String completionStatus, 
			String irbWaiver, String crossRef, String purposeOfReq, String delivery_plan, String technicalSpecs, String testPlan, String sql, String any, String and_or) throws InterruptedException, SQLException {
		Connection conn = null;
		JSONArray arrayJSON = new JSONArray();
		
		String requestDescription = "%";
		
		//Thread.sleep(5000);
		projectCode = (projectCode.trim().length() == 0) ? "%" : "%" + projectCode.trim() + "%";
		requester = (requester.trim().length() == 0) ? "%" : "%" + requester.trim() + "%";
		developer = (developer.trim().length() == 0) ? "%" : "%" + developer.trim() + "%";
		description = (description.trim().length() == 0) ? "%" : "%" + description.trim() + "%";

		if (and_or.equals("OR")) {
			projectCode = (projectCode.equals("%")) ? "" : projectCode;
			requester = (requester.equals("%")) ? "" : requester;
			developer = (developer.equals("%")) ? "" : developer;
			description = (description.equals("%")) ? "" : description;
		}
		
		boolean advancedSearch = false;
		if (!any.isEmpty() || !irbWaiver.isEmpty() || !crossRef.isEmpty() || !purposeOfReq.isEmpty() || !delivery_plan.isEmpty() || !technicalSpecs.isEmpty() || !testPlan.isEmpty() || !sql.isEmpty()) {
			advancedSearch = true;
		}
		
		if (advancedSearch) {
			irbWaiver = (irbWaiver.trim().length() == 0) ? "%" : "%" + irbWaiver + "%";
			crossRef = (crossRef.trim().length() == 0) ? "%" : "%" + crossRef + "%";
			purposeOfReq = (purposeOfReq.trim().length() == 0) ? "%" : "%" + purposeOfReq + "%";
			delivery_plan = (delivery_plan.trim().length() == 0) ? "%" : "%" + delivery_plan + "%";
			
			//criteria = (criteria.trim().length() == 0) ? "%" : "%" + criteria + "%";
			technicalSpecs = (technicalSpecs.trim().length() == 0) ? "%" : "%" + technicalSpecs + "%";
			testPlan = (testPlan.trim().length() == 0) ? "%" : "%" + testPlan + "%";
			sql = (sql.trim().length() == 0) ? "%" : "%" + sql + "%";
			
			if (and_or.equals("OR")) {
				irbWaiver = (irbWaiver.equals("%")) ? "" : irbWaiver;
				crossRef = (crossRef.equals("%")) ? "" : crossRef;
				purposeOfReq = (purposeOfReq.equals("%")) ? "" : purposeOfReq;
				delivery_plan = (delivery_plan.equals("%")) ? "" : delivery_plan;
				
				//criteria = (criteria.equals("%")) ? "" : criteria;
				technicalSpecs = (technicalSpecs.equals("%")) ? "" : technicalSpecs;
				testPlan = (testPlan.equals("%")) ? "" : testPlan;
				sql = (sql.equals("%")) ? "" : sql;
			}
		}
		
		//System.out.println(projectCode + requester + developer + description);
		
		String completionStatusWhere = " ";
		
		//System.out.println("completionStatus= " + completionStatus);
		
		// unassigned // open // delivered // on hold // on going // cancelled
		for (int i = 0; i < completionStatus.length(); i++) {
		    char c = completionStatus.charAt(i);    
		    // 0 or 1
	    	System.out.println("i, c" + i + ", " + c);
		    if (c == '1') {
		    	// unassigned
		    	if (i == 0) completionStatusWhere += " (\"developer primary\" is null and \"project completion status\" is null and \"project delivery date\" is null) or ";
		    	
		    	// open (but not unassigned)
		    	if (i == 1) completionStatusWhere += " (\"project completion status\" is null and \"project delivery date\" is null) or ";
		    	
		    	// delivered
		    	if (i == 2) completionStatusWhere += "\"project delivery date\" is not null or ";
		    	
		    	// on hold
		    	if (i == 3) completionStatusWhere += "\"project completion status\" = 'On hold' or ";
		    	
		    	// on going 
		    	if (i == 4) completionStatusWhere += "\"project completion status\" = 'On-going' or ";
		    	
		    	// cancelled
		    	if (i == 5) completionStatusWhere += "\"project completion status\" = 'Cancelled' or ";
		    }
		}
		
		completionStatusWhere = (completionStatusWhere.length() > 1) ? " and (" + completionStatusWhere.substring(0, completionStatusWhere.length() - 3) + ")" : completionStatusWhere;

		System.out.println(completionStatusWhere);
		
		//try {
			conn = dataSource.getConnection();
			
			String and_or_string = "and";
			if (and_or.equals("OR")) {
				and_or_string = "or ";
			}

			String sql_string = " "
					+ "select top 100 [project code], [serial number], [project description], [requester first name], [requester last name], [department name], [project request date], [project req_delv date], [project type], [developer primary], [developer secondary], [project delivery date], [project completion status], [email], upper([Project Code Cross Reference]) cross_ref /*, COALESCE(open_amend, 'N') open_amend*/ from dbo.projects_r2d4_v projects_v "
					+ "left join dbo.requestr on \"requester id\"=requester_id "
					//+ "left join (select distinct [serial number] amend_sn, 'Y' open_amend from dbo.projamendments where deliverydate is null) open_via_amendment on amend_sn=[serial number] "
					;
			
			sql_string += "where \"project code\" like ? "
					+ and_or_string + " COALESCE(ltrim(\"requester first name\" + ' ' + \"requester last name\"), '-') like ? "
					+ and_or_string + " COALESCE(\"project description\", '-') like ? "
					+ and_or_string + " (COALESCE(\"developer primary\", '-') like ? or COALESCE(\"developer secondary\", '-') like ?) ";
						
			// if 'Unassigned' is checked or developer input is blank - then remove developer part from where clause
			//if ((completionStatus.charAt(0) == '1' && developer.equals("%")) || (completionStatus.equals("000000") && developer.equals("%"))) {
			//	sql_string = sql_string.substring(0, sql_string.length() - 95);
			//}
			//sql += "where \"project code\" like ? and ltrim(\"requester first name\" + ' ' + \"requester last name\") like ? ";
			sql_string += "";
			sql_string += completionStatusWhere;
			
			if (advancedSearch) {
				sql_string += "and (COALESCE(\"Project IRB/Waiver #\", ' ') like ? "
						+ and_or_string + " COALESCE(\"Project Code Cross Reference\", ' ') like ? "
						+ and_or_string + " COALESCE(\"Request Purpose\", ' ') like ? "
						//+ and_or_string + " COALESCE(\"Data Elements\", ' ') like ? "
						+ and_or_string + " COALESCE(\"Delivery Plan\", ' ') like ? "
						//+ and_or_string + " COALESCE(Criteria, ' ') like ? "
						+ and_or_string + " COALESCE(\"Project Tech Specs\", ' ') like ? "
						+ and_or_string + " COALESCE(\"Project Test Plan\", ' ') like ? "
						+ and_or_string + " COALESCE(\"Project SQL\", ' ') like ? ) ";
			}
			
			if (any.trim().length() > 0) {
				
				any = "%" + any + "%";
				
				sql_string += and_or_string + 
				" (\"project code\" like ? or " +
				"ltrim(\"requester first name\" + ' ' + \"requester last name\") like ? or " +
				"\"developer primary\" like ? or \"developer secondary\" like ? or " +
				"COALESCE(\"project description\", ' ') like ? or " +
				"COALESCE(\"request description\", ' ') like ? or " +
				"COALESCE(\"Project IRB/Waiver #\", ' ') like ? or " +
				"COALESCE(\"Project Code Cross Reference\", ' ') like ? or " +
				"COALESCE(\"Request Purpose\", ' ') like ? or " +
				"COALESCE(\"Delivery Plan\", ' ') like ? or " +
				//"COALESCE(\"Data Elements\", ' ') like  or " +
				//"COALESCE(Criteria, ' ') like  or " +
				"COALESCE(\"Project Tech Specs\", ' ') like ? or " +
				"COALESCE(\"Project Test Plan\", ' ') like ? or " +
				"COALESCE(\"Project SQL\", ' ') like ?) "; 
			}
			
			//sql_string += " order by \"project request date\" desc ";
			sql_string += " order by \"serial number\" desc";
			//sql += (whereString.trim().length() > 0) ? "where " + whereString : "";
			System.out.println("right before prepared sql: " + sql_string);
			PreparedStatement ps = conn.prepareStatement(sql_string);
			System.out.println("prepared sql: " + sql_string);
			int c=1;
			if (username.trim().length() > 0) {
				ps.setString(c, projectCode);c++;
				sql_string = sql_string.replaceFirst("\\?", "'" + projectCode + "'");
				ps.setString(c, requester);c++;
				sql_string = sql_string.replaceFirst("\\?", "'" + requester + "'");
				ps.setString(c, description);c++;
				sql_string = sql_string.replaceFirst("\\?", "'" + description + "'");
				//if ((completionStatus.charAt(0) == '1' && developer.equals("%")) || (completionStatus.equals("000000") && developer.equals("%"))) {
				//	System.out.println("Trying to get unassigned");
				//}
				//else {
					ps.setString(c, developer);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + developer + "'");
					ps.setString(c, developer);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + developer + "'");
				//}
				if (advancedSearch) {
					System.out.println("irbWaiver: " + irbWaiver + ", c: " + c);
					ps.setString(c, irbWaiver);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + irbWaiver + "'");
					System.out.println("crossRef: " + crossRef + ", c: " + c);
					ps.setString(c, crossRef);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + crossRef + "'");
					System.out.println("purposeOfReq: " + purposeOfReq + ", c: " + c);
					ps.setString(c, purposeOfReq);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + purposeOfReq + "'");
					System.out.println("delivery_plan: " + delivery_plan + ", c: " + c);
					ps.setString(c, delivery_plan);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + delivery_plan + "'");
					//System.out.println("criteria: " + criteria + ", c: " + c);
					//ps.setString(c, criteria);c++;
					//sql_string = sql_string.replaceFirst("\\?", "'" + criteria + "'");
					System.out.println("technicalSpecs: " + technicalSpecs + ", c: " + c);
					ps.setString(c, technicalSpecs);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + technicalSpecs + "'");
					System.out.println("testPlan: " + testPlan + ", c: " + c);
					ps.setString(c, testPlan);c++;
					sql_string = sql_string.replaceFirst("\\?", "'" + testPlan + "'");
					System.out.println("sql: " + sql + ", c: " + c);
					ps.setString(c, sql);
					sql_string = sql_string.replaceFirst("\\?", "'" + sql + "'");
				}
				if (any.trim().length() > 0) {
					for (int d=c+1; d < 14 + c; d++) {
						System.out.println("sql: " + sql + ", d: " + d);
						ps.setString(d, any);
						sql_string = sql_string.replaceFirst("\\?", "'" + any + "'");
					}
				}
			}
			System.out.println("GET PROJECTS: " + sql_string.replace("?", "'%'"));
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				//System.out.print("In project loop: " + rs.getString("project code"));
				
				JSONObject projJSONObj = new JSONObject();
				
				projJSONObj.put("code", rs.getString("project code"));
				projJSONObj.put("serial_number", rs.getInt("serial number"));
				projJSONObj.put("description", rs.getString("project description") != null ? rs.getString("project description") : ' ');
				projJSONObj.put("requester", rs.getString("requester first name") + " " + rs.getString("requester last name") );
				projJSONObj.put("department_name", rs.getString("department name"));
				if (rs.wasNull()) 
					projJSONObj.put("department_name", "");
				projJSONObj.put("request_date", rs.getString("project request date"));
				projJSONObj.put("required_date", "Date - " + rs.getString("project req_delv date"));
				if (rs.wasNull()) 
					projJSONObj.put("required_date", rs.getString("project type"));
					if (rs.wasNull())
						projJSONObj.put("required_date", "");
				projJSONObj.put("developer_primary", rs.getString("developer primary"));
				projJSONObj.put("developer_secondary", rs.getString("developer secondary"));
				projJSONObj.put("delivery_date", rs.getString("project delivery date"));
				if (rs.wasNull()) { 
					projJSONObj.put("status", rs.getString("project completion status"));
					if (rs.wasNull())
						projJSONObj.put("status", "Open");
				}
				//else if (rs.getString("open_amend").equals("Y")) {
				//	projJSONObj.put("status", "Open");
				//}
				else
					projJSONObj.put("status", rs.getString("project delivery date"));
				
				projJSONObj.put("email", rs.getString("email"));
				
				HashSet ignoreXRef = new HashSet(Arrays.asList("na", "none", "n/a", "no"));
				
				String cross_ref = rs.getString("cross_ref");
				if (cross_ref != null && ignoreXRef.contains(cross_ref.trim().toLowerCase())) {
					projJSONObj.put("cross_ref", "");
				}
				else {
					projJSONObj.put("cross_ref", rs.getString("cross_ref"));
				}
				
				arrayJSON.put(projJSONObj);
										
			}
			
		/*} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}*/
		
		return arrayJSON;
	}
	
	public JSONObject getProjectInfo(String projectid, String sn, String version) throws SQLException {
		Connection conn = null;
		String versionDate = "", versionTime = "";
		
		//System.out.println(version);
		
		if (version.equals("All")) { versionDate = "%"; versionTime = "%"; }
		else if (version.equals("Original")) { versionDate = "1900-01-01"; versionTime = "%"; }
		else { 
			String[] versionSplit = version.split(" ");
			versionDate = versionSplit[0]; versionTime = versionSplit[1]; 
		}
		
		//try {
			conn = dataSource.getConnection();
			String sql = "select * "
					+ "from projects "
					+ "join requestr on [requester id]=requester_id "
					+ "left join projamendments on projamendments.[serial number]=projects.[serial number] "
					+ "where [Project Code]=? "
					+ "order by deliverydate desc";
			
			sql = "select projects.*, CONVERT(DATE, '1900-01-01') as COMMIT_DATE, CONVERT(TIME, GETDATE()) as COMMIT_TIME, requestr.*, projamendments.*, requester_approvals_r2d4.[Approved_Time] as REQUESTER_APPROVAL, requester_approvals_r2d4.override " +
			"from projects " +
			"left join requestr on [requester id]=requester_id " +
			"left join projamendments on projamendments.[serial number]=projects.[serial number] " +
			"left join requester_approvals_r2d4 on requester_approvals_r2d4.[serial number]=projects.[serial number] " +
			//"left join (select * from projamendments_r2d4 union all select * from projamendments) projamendments on projamendments.[serial number]=projects.[serial number] " +
			"where projects.[Serial Number]=? AND projects.[Serial Number] not in (select projects_r2d4_commits.[Serial Number] from projects_r2d4_commits)" +
			"" +
			"union all " +
			"" +
			"select projects_r2d4_commits.*, requestr.*, projamendments.*, requester_approvals_r2d4.[Approved_Time] as REQUESTER_APPROVAL, requester_approvals_r2d4.override " +
			"from projects_r2d4_commits " +
			"left join requestr on [requester id]=requester_id " +
			"left join projamendments on projamendments.[serial number]=projects_r2d4_commits.[serial number] " +
			"left join requester_approvals_r2d4 on requester_approvals_r2d4.[serial number]=projects_r2d4_commits.[serial number] " +
			//"left join (select * from projamendments_r2d4 union all select * from projamendments) projamendments on projamendments.[serial number]=projects_r2d4_commits.[serial number] " +
			"where projects_r2d4_commits.[Serial Number]=?  and [Commit Date] like ? and [Commit Time] like ? " +
			"" +
			"order by COMMIT_DATE desc, COMMIT_TIME desc, deliverydate desc" ;
			System.out.println(sql);
			PreparedStatement ps = conn.prepareStatement(sql);
			sql = sql.replaceFirst("\\?", "'" + sn + "'");
			sql = sql.replaceFirst("\\?", "'" + sn + "'");
			sql = sql.replaceFirst("\\?", "'" + versionDate + "'");
			sql = sql.replaceFirst("\\?", "'" + versionTime + "'");
			System.out.println(sql);
			ps.setString(1, sn);
			ps.setString(2, sn);
			ps.setString(3, versionDate);
			ps.setString(4, versionTime);
			
			//int sn = 0;
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				
				//sn = rs.getInt("serial number");
				
				JSONObject projJSONObj = new JSONObject();
				
				projJSONObj.put("code", rs.getString("project code"));
				projJSONObj.put("serial_number", rs.getInt("serial number"));
				projJSONObj.put("request_type", rs.getString("project type"));
				projJSONObj.put("request_date", rs.getString("project request date"));
				projJSONObj.put("required_date", rs.getString("project req_delv date"));
				projJSONObj.put("est_delivery_dte", rs.getString("project completion date"));
				projJSONObj.put("developer_primary", rs.getString("developer primary"));
				projJSONObj.put("schedule_status", rs.getString("schedule status"));
				projJSONObj.put("priority", rs.getString("project priority"));

				projJSONObj.put("received_by", rs.getString("project req_rec by"));
				projJSONObj.put("start_date", rs.getString("project req_rec date"));
				projJSONObj.put("delivery_date", rs.getString("project delivery date"));
				projJSONObj.put("status", rs.getString("project completion status"));
				projJSONObj.put("objs_delivered", rs.getInt("objectsdelivered") == 0 ? "" : rs.getInt("objectsdelivered"));
				projJSONObj.put("second_developer", rs.getString("developer secondary"));
				projJSONObj.put("automation", rs.getString("project manauto"));
				projJSONObj.put("estimated_hours", rs.getString("project effort"));
				
				projJSONObj.put("requester_id", rs.getString("requester_id"));
				projJSONObj.put("requester_last_name", rs.getString("requester last name"));
				projJSONObj.put("requester_first_name", rs.getString("requester first name"));
				projJSONObj.put("requester_dept_name", rs.getString("department name"));
				projJSONObj.put("requester_title", rs.getString("requester title"));
				projJSONObj.put("requester_address", rs.getString("requester address"));
				projJSONObj.put("requester_email", rs.getString("email"));
				
				projJSONObj.put("request_description", rs.getString("request description"));
				projJSONObj.put("privacy_type", rs.getString("project request type"));
				projJSONObj.put("irb_waiver", rs.getString("project irb/waiver #"));
				projJSONObj.put("cross_ref", rs.getString("project code cross reference"));				
				projJSONObj.put("description", rs.getString("project description"));
				projJSONObj.put("purpose_of_request", rs.getString("request purpose"));
				
				//System.out.println("ABOUT TO ADD DATA_ELEMENTS!!!");
				
				projJSONObj.put("program_specs", rs.getString("delivery plan"));
				projJSONObj.put("data_elements", rs.getString("data elements"));
				projJSONObj.put("criteria", rs.getString("criteria"));
				
				projJSONObj.put("technical_specs", rs.getString("project tech specs"));
				projJSONObj.put("test_plan", rs.getString("project test plan"));
				projJSONObj.put("project_notes", rs.getString("project notes"));
				projJSONObj.put("dev_notes", rs.getString("developernotes"));
				projJSONObj.put("sql", rs.getString("project sql"));

				//if (rs.wasNull()) 
				//	projJSONObj.put("required_date", rs.getString("project type"));
				//	if (rs.wasNull())
				//		projJSONObj.put("required_date", "");
				
				
				projJSONObj.put("description", rs.getString("project description"));
				projJSONObj.put("requester", rs.getString("requester first name") + " " + rs.getString("requester last name") );
				projJSONObj.put("email", rs.getString("email"));
				
				projJSONObj.put("department_name", rs.getString("department name"));
				if (rs.wasNull()) 
					projJSONObj.put("department_name", "");
				
				projJSONObj.put("requester_approval", rs.getString("requester_approval"));
				if (rs.wasNull()) 
					projJSONObj.put("requester_approval", "N");
				else
					projJSONObj.put("requester_approval", "Y");
				
				// if delivery date is null then
				//    put project status
				//    if project status is null then
				//       put "Open"
				
				projJSONObj.put("delivery_date", rs.getString("project delivery date"));
				if (rs.wasNull()) { 
					projJSONObj.put("status", rs.getString("project completion status"));
					if (rs.wasNull())
						projJSONObj.put("status", "Open");
				}
				else
					projJSONObj.put("status", "Delivered<br>" + rs.getString("project delivery date"));
				
				projJSONObj.put("override", rs.getBoolean("override"));
				if (!rs.wasNull() && rs.getBoolean("override")) 
					projJSONObj.put("override", "Y");
				else
					projJSONObj.put("override", "N");

				JSONArray arrayJSONamendments = new JSONArray();
				JSONArray arrayJSONcommits = new JSONArray();
				
				// eliminate duplicate amendment ids
				HashSet<Integer> amendmentIdSet = new HashSet<Integer>();
				
				// eliminate duplicate commits (versions)
				HashSet<String> commitSet = new HashSet<String>();
				
				do {
					
					if (!commitSet.contains(rs.getString("commit_date") + " " + rs.getString("commit_time"))) {
						JSONObject commitsJSONObj = new JSONObject();
						commitsJSONObj.put("date_time", rs.getString("commit_date") + " " + rs.getString("commit_time"));
						
						arrayJSONcommits.put(commitsJSONObj);
						commitSet.add(rs.getString("commit_date") + " " + rs.getString("commit_time"));
					}
					
					if (!amendmentIdSet.contains(rs.getInt("id"))) {
						JSONObject amendmentJSONObj = new JSONObject();
						amendmentJSONObj.put("id", rs.getInt("id"));
						amendmentJSONObj.put("developer", rs.getString("developer"));
						amendmentJSONObj.put("delivery_date", rs.getDate("deliveryDate"));
						amendmentJSONObj.put("amendment_note", rs.getString("amendment note"));	
						
						arrayJSONamendments.put(amendmentJSONObj);
						amendmentIdSet.add(rs.getInt("id"));
					}
					
				} while (rs.next());
				
				projJSONObj.put("amendments", arrayJSONamendments);
				
				projJSONObj.put("commits", arrayJSONcommits);
				
				// get custodians
				sql = "select rtrim(projcust_r2d4.\"Data Custodian\") cust_name, rtrim(\"Table Schema\") + '.' + rtrim(\"Table Name\") cust_table, status, approved, override, rtrim(custodian_cn)+'@mskcc.org' custodian_email from projcust_r2d4 "
						+ "left join projcusttables_r2d4 on projcust_r2d4.\"Data Custodian\"=projcusttables_r2d4.\"Data Custodian\" and projcust_r2d4.\"Serial Number\"=projcusttables_r2d4.\"Serial Number\" "
						+ "left join custodian_cn on \"Custodian_Name\"=projcust_r2d4.\"Data Custodian\" "
						+ "where (projcust_r2d4.\"Serial Number\"=? or projcust_r2d4.\"Project Code\"=?) and projcust_r2d4.status <> 1 "
						+ "order by projcust_r2d4.\"Data Custodian\", \"Table Name\" " ;

				System.out.println("GETTING APPROVALS: " + sql);
				ps = conn.prepareStatement(sql);
				try {
					ps.setInt(1, Integer.valueOf(sn));
					ps.setString(2, projectid);

					rs = ps.executeQuery();
				}
				catch (Exception e) {
					return new JSONObject().put("code", -1);
				}
				JSONArray arrayJSONcustodians = new JSONArray();
				while (rs.next()) {
					JSONObject custodiansJSONObj = new JSONObject();
					custodiansJSONObj.put("cust_name", rs.getString("cust_name"));
					custodiansJSONObj.put("cust_table", rs.getString("cust_table"));
					custodiansJSONObj.put("status", rs.getInt("status"));
					custodiansJSONObj.put("approved", rs.getBoolean("approved"));
					custodiansJSONObj.put("override", rs.getBoolean("override"));
					custodiansJSONObj.put("custodian_email", rs.getString("custodian_email"));
					
					arrayJSONcustodians.put(custodiansJSONObj);
				}

				projJSONObj.put("custodians", arrayJSONcustodians);
				
				return projJSONObj;
			}
			else {
				return new JSONObject().put("code", -1);
			}		
	}
	
	public JSONArray determineCustodians(String username, String tableList, String projectCode) {
		
		Connection conn = null;
		JSONArray arrayJSON = new JSONArray();
		
		//System.out.println();
		//System.out.println();
		//System.out.println("DataDaoImpl.java - determineCustodians");

//		// convert newlines to spaces
//		sqlCode = " from " + sqlCode.replaceAll("\\n", " from ");
//		sqlCode = " from " + sqlCode.replaceAll("\\\\n", " from ");
//		
//		System.out.println("sqlCode lines to spaces: " + sqlCode);
//		
//		// remove comments (non-greedy)
//		sqlCode = sqlCode.replaceAll("/\\*.*?\\*/", "");
//		
//		System.out.println("sqlCode multi-line comments removed: " + sqlCode);
//		
//		// remove single-line comments (non-greedy)
//		sqlCode = sqlCode.replaceAll("--.*?\\\\n?", "");
//		
//		System.out.println();
//		System.out.println();
//		System.out.println("sqlCode single line comments removed: " + sqlCode);
//
//		// remove double-quotes
//		sqlCode = sqlCode.replaceAll("\"", "");
//
//		System.out.println();
//		System.out.println();
//		System.out.println("sqlCode quotes removed: " + sqlCode);
//
//		// get [from schema.tablename] OR [join schema.tablename] from string 
//		Pattern p = Pattern.compile("(?i)(from|join)\\s*(\\w+\\.\\w+)");
//		Matcher m = p.matcher(sqlCode);
//
//		System.out.println(sqlCode);
//		String tables = "";
//		while (m.find()) {
//		    System.out.println(m.group(1) + "\t" + m.group(2));
//		    // add schema.tablename and just tablename
//		    tables += "'" + m.group(2) + "',";
//		    tables += "'" + m.group(2).replaceAll(".*\\.", "") + "',";
//		}
//		
//		tables = (tables.length() > 0) ? tables.substring(0,  tables.length()-1) : "''";
//
//		System.out.println("tables: " + tables);
		
		//String[] tables = tableList.replaceAll("\\w+\\.", "").split("\\s+");
		String[] tables = tableList.split("\\s+");
		String tableQueryList = "";
		String whereClause1 = "(upper(cust_table) = 'FAKE' and cust_notes = 'FAKE') OR ";
		
		for (String table : tables) {
			//tableQueryList += "'" + table + "',";
			if (table.split("\\.").length == 2) {
				whereClause1 += "(upper(cust_table) = '" + table.split("\\.")[1].toUpperCase() + "' and cust_notes = '" + table.split("\\.")[0].toUpperCase() + "') OR ";
			}
		}
		//tableQueryList = (tableQueryList.length() > 0) ? tableQueryList.substring(0,  tableQueryList.length()-1) : "''";
		//whereClause1 = (whereClause1.length() > 0) ? whereClause1.substring(0,  whereClause1.length()-3) : "1 = 1";
		whereClause1 = whereClause1.substring(0,  whereClause1.length()-3);
		
		try {
			conn = dataSource.getConnection();

			String sql = "select distinct cust_name, rtrim(cust_table) cust_table, rtrim(cust_notes) cust_schema, approved, override, rtrim(custodian_cn)+'@mskcc.org' custodian_email "
					+ "from data_custodians  "
					+ "left join projcust_r2d4 on \"Serial Number\"=? AND cust_name = \"Data Custodian\" "
					+ "left join custodian_cn on \"Custodian_Name\"=\"Data Custodian\" "
					//+ "where CUST_NOTES IN ('IDB', 'DADM', 'DV', 'DVIDIC') AND (" + whereClause1 + ") "
					+ "where (" + whereClause1 + ") "
					//+ "where upper(cust_table) in (" + tableQueryList.toUpperCase() + ") or "
					+ " OR rtrim(cust_notes) + '.' + rtrim(cust_table) in (select [table schema] + '.' + [table name] from PROJCUSTTABLES_R2D4 where [Serial Number] = ?) "
					+ "order by cust_name, cust_table"
					;
			System.out.println("determineCustodians: " + sql + "projectCode: " + projectCode + ", sn: " + projectCode.replaceAll("[^\\d.]", ""));
			//sql += (whereString.trim().length() > 0) ? "where " + whereString : "";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, projectCode.replaceAll("[^\\d.]", ""));
			ps.setString(2, projectCode.replaceAll("[^\\d.]", ""));
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				JSONObject projJSONObj = new JSONObject();
				
				projJSONObj.put("cust_name", rs.getString("cust_name"));
				projJSONObj.put("custodian_email", rs.getString("custodian_email"));
				projJSONObj.put("cust_table", rs.getString("cust_schema") + "." + rs.getString("cust_table"));
				projJSONObj.put("approved", rs.getString("approved"));
				projJSONObj.put("override", rs.getString("override"));

				/*projJSONObj.put("cust_id", rs.getInt("cust_id"));
				projJSONObj.put("cust_name", rs.getString("cust_name"));
				projJSONObj.put("cust_backup", rs.getString("cust_backup"));
				projJSONObj.put("cust_backup2", rs.getString("cust_backup2"));
				projJSONObj.put("cust_expert", rs.getString("cust_expert"));
				projJSONObj.put("cust_data", rs.getString("cust_data"));
				projJSONObj.put("cust_table", rs.getString("cust_table"));
				projJSONObj.put("cust_notes", rs.getString("cust_notes"));
				projJSONObj.put("cust_content", rs.getString("cust_content"));
				projJSONObj.put("cust_dte", rs.getDate("cust_dte"));
				projJSONObj.put("cust_table_alt", rs.getString("cust_table_alt"));*/
				
				arrayJSON.put(projJSONObj);
										
			}			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		//System.out.println(arrayJSON.toString());
		
		return arrayJSON;
		
	}
	
	public String determineTablesFromSQLText(String username, String sqlCode, String currentTables) {
		
		Connection conn = null;
		JSONArray arrayJSON = new JSONArray();
		
		sqlCode = sqlCode + currentTables.replaceAll("\\\\n", " from ");
		
		//System.out.println();
		//System.out.println();
		//System.out.println("DataDaoImpl.java - determineTablesFromSQLText");
		//System.out.println("sqlCode: " + sqlCode);

		// remove comments (non-greedy)
		sqlCode = sqlCode.replaceAll("/\\*.*?\\*/", "");
		
		//System.out.println("sqlCode multi-line comments removed: " + sqlCode);
		
		// remove single-line comments (non-greedy)
		sqlCode = sqlCode.replaceAll("--.*?\\\\n?", "");
		
		//System.out.println("sqlCode single-line comments removed: " + sqlCode);
		
		// convert newlines to spaces
		sqlCode = sqlCode.replaceAll("\\\\n", " ");
		
		//System.out.println("sqlCode lines to spaces: " + sqlCode);

		// remove double-quotes (first line removes \" and second line removes regular ")
		sqlCode = sqlCode.replaceAll("\\\\\"", "");
		sqlCode = sqlCode.replaceAll("\"", "");

		//System.out.println("sqlCode double quotes removed: " + sqlCode);

		// get [from schema.tablename] OR [join schema.tablename] from string 
		Pattern p = Pattern.compile("(?i)(from|join)\\s*(\\w+\\.\\w+)");
		Matcher m = p.matcher(sqlCode);

		//System.out.println(sqlCode);
		TreeSet<String> tables = new TreeSet<String>(); 
		//String tables = "";
		while (m.find()) {
		    //System.out.println(m.group(1) + "\t" + m.group(2));
		    // add schema.tablename and just tablename
		    tables.add(m.group(2).toUpperCase());
		    //tables += "'" + m.group(2) + "',";
		    //tables += "'" + m.group(2).replaceAll(".*\\.", "") + "',";
		}
		
		String tablesString = "";
		for (String table : tables) {
			//JSONObject tableJSONObj = new JSONObject();
			//tableJSONObj.put("table", table);
			
			//arrayJSON.put(tableJSONObj);
			tablesString += table + "\n";
		}
		
		//tables = (tables.length() > 0) ? tables.substring(0,  tables.length()-1) : "''";

		//System.out.println("tables: " + tables);
		//System.out.println("tablesString: " + tablesString);
		
		return tablesString.toUpperCase();
		
	}
	
	public JSONArray custodianTablesSearch(String searchString) {
		
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement("select top 20 (upper(rtrim(cust_notes)) + '.' + rtrim(cust_table)) cust_table, rtrim(cust_name) cust_name " + 
					"from data_custodians " + 
					"where CUST_NOTES IN ('IDB', 'DADM', 'DV', 'DVIDIC') and lower(cust_table) like ? and lower(cust_notes) in ('idb', 'dadm', 'dv', 'dvidic') ");
			ps.setString(1, '%' + searchString.toLowerCase().replace("idb.", "").replace("dv.", "") + '%');
			ResultSet rs = ps.executeQuery();
			
			JSONArray arrayJSON = new JSONArray();
			while (rs.next()) {
				JSONObject tableJSONObj = new JSONObject();
				
				tableJSONObj.put("label", rs.getString("cust_table"));
				tableJSONObj.put("id", rs.getString("cust_name"));
				
				arrayJSON.put(tableJSONObj);

			}
			return arrayJSON;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}

		
	}

	public String projectCommit(String formData) throws SQLException {

		JSONObject jsonObject = new JSONObject(formData);

		// mapping html form id to database column name
		HashMap<String, String> colMap = new HashMap<String, String>();

		colMap.put("automation", "[Project ManAuto]");
		colMap.put("cross_ref", "[Project Code Cross Reference]");
		colMap.put("delivery_date", "[Project Delivery Date]");
		colMap.put("description", "[Project Description]");
		colMap.put("developer", "[Developer Primary]");
		colMap.put("irb_waiver", "[Project IRB/Waiver #]");
		colMap.put("objs_delivered", "ObjectsDelivered");
		colMap.put("sn", "[Serial Number]");
		colMap.put("dev_notes", "DeveloperNotes");

		colMap.put("est_delivery_dte", "[Project Completion Date]");
		colMap.put("estimated_hours", "[Project Effort]");
		colMap.put("priority", "[Project Priority]");
		colMap.put("privacy_type", "[Project Request Type]");
		colMap.put("program_specs", "[Delivery Plan]"); // ADDED BACK TO FORM IN 7/20/18
		colMap.put("project_notes", "[Project Notes]");
		colMap.put("purpose_of_request", "[Request Purpose]");
		colMap.put("received_by", "[Project Req_rec By]");
		colMap.put("received_date", "[Project Request Date]");
		colMap.put("reqd_deliv_date", "[Project Req_delv Date]");
		colMap.put("request_description", "[Request Description]");
		colMap.put("request_type", "[Project Type]");
		colMap.put("requester", "Requester_ID");

		// colMap.put("requester_title", ""); this is shown on screen but a person's title is never changed in db

		colMap.put("schedule_status", "[schedule status]");
		colMap.put("second_developer", "[developer secondary]");
		colMap.put("sn_code", "[project code]");
		colMap.put("sql", "[project sql]");
		colMap.put("start_date", "[project req_rec date]");
		colMap.put("status", "[project completion status]");
		colMap.put("technical_specs", "[project tech specs]");
		colMap.put("test_plan", "[project test plan]");

		colMap.put("data_elements", "[data elements]");
		colMap.put("criteria", "criteria");

		//System.out.println(jsonObject.get("developer"));
		//System.out.println(jsonObject.toString());

		// To insert a committed row...
		// Check if row exists in DBO.PROJECTS_R2D4_COMMITS for committed project data
		// If it does not, then must:
		// 1. insert original record from DBO.PROJECTS into DBO.PROJECTS_R2D4_COMMITS 
		// 2. add new committed data into DBO.PROJECTS_R2D4_COMMITS
		// 3. update original record in DBO.PROJECTS

		// If it does exist:
		// 1. insert into DBO.PROJECTS_R2D4_COMMITS

		// In the application code, must change all logic around deleting commits 
		// Currently any row can be deleted or inserted from DBO.PROJECTS_R2D4_COMMITS
		// Now before showing delete button, must check if that the row does not have 
		// the lowest date in the DBO.PROJECTS_R2D4_COMMITS

		String sql = "SELECT COUNT(*) CNT FROM DBO.PROJECTS_R2D4_COMMITS WHERE [Serial Number] = ?";

		Connection conn = null;
		conn = dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, jsonObject.getString("sn"));
		ResultSet rs = ps.executeQuery();

		JSONArray arrayJSON = new JSONArray();
		if (rs.next()) {
			if (rs.getInt("CNT") == 0) {

				// INSERT original request with date of 1800-01-01
				sql =  "INSERT INTO DBO.PROJECTS_R2D4_COMMITS ([Project Code],[Serial Number],[Project Description],[Requester_ID],[Requester ID Old],[Project Request Date],[Project Req_rec Date],[Project Req_rec By],[Project Req_delv Date],[Project Completion Status],[Project Completion Date],[Project Delivery Date],[Project Delivery Desc],[Project Complexity],[Project Type],[Project Priority],[Project Effort],[Project Volume in Bytes],[Project Volume in Pages],[Project IRB/Waiver #],[Request Description],[Request Purpose],[Delivery Plan],[Data Elements],[Criteria],[Project Notes],[Schedule Status],[Project % Complete],[Project Request Type],[Project ManAuto],[Project Tech Specs],[Project Test Plan],[Developer Primary],[Developer Secondary],[QA Plan Developer],[QA Plan Due Date],[QA Plan Complete],[QA Results Developer],[QA Results Due Date],[QA Results Complete],[Project SQL],[DeveloperNotes],[ObjectsDelivered],[WEBFORM_ID],[WEBFORM_TYPE],[Serial Number Old],[Project Code Cross Reference],[Commit Date],[Commit Time]) ";
				sql += "SELECT [Project Code],[Serial Number],[Project Description],[Requester_ID],[Requester ID Old],[Project Request Date],[Project Req_rec Date],[Project Req_rec By],[Project Req_delv Date],[Project Completion Status],[Project Completion Date],[Project Delivery Date],[Project Delivery Desc],[Project Complexity],[Project Type],[Project Priority],[Project Effort],[Project Volume in Bytes],[Project Volume in Pages],[Project IRB/Waiver #],[Request Description],[Request Purpose],[Delivery Plan],[Data Elements],[Criteria],[Project Notes],[Schedule Status],[Project % Complete],[Project Request Type],[Project ManAuto],[Project Tech Specs],[Project Test Plan],[Developer Primary],[Developer Secondary],[QA Plan Developer],[QA Plan Due Date],[QA Plan Complete],[QA Results Developer],[QA Results Due Date],[QA Results Complete],[Project SQL],[DeveloperNotes],[ObjectsDelivered],[WEBFORM_ID],[WEBFORM_TYPE],[Serial Number Old],[Project Code Cross Reference],'1800-01-01','00:00:00' FROM dbo.PROJECTS WHERE [Serial Number] = ?";
				ps = conn.prepareStatement(sql);
				ps.setString(1, jsonObject.getString("sn"));
				ps.execute();

			}
		}
		
		// Correction of data - can't set Project Completion Status (i.e. On hold) if the project has been delivered
		// This is a problem because when searching for On hold projects, delivered projects will show up (and this is not desired behavior)  
		if (jsonObject.getString("delivery_date").length() == 10) {
			jsonObject.put("status", "");
		}
		
		//if (jsonObject.getString("program_specs").length() == 0) {
		//	jsonObject.put("program_specs", "Elements:\r\n" + jsonObject.getString("data_elements") + "\r\n\r\n" + "Criteria:\r\n" + jsonObject.getString("criteria"));
		//}

		// loop through Project.colMap
		// keep a counter in order to bind with variables on INSERT 
		String sql1 = "INSERT INTO DBO.PROJECTS_R2D4_COMMITS ([Commit Time], [Commit Date], ";
		String sql2 = "values (CONVERT(TIME, GETDATE()), CONVERT(DATE, GETDATE()), ";
		String sql3 = "UPDATE DBO.PROJECTS SET ";
		String sql4 = "WHERE [Serial Number] = ?";
		for (String key : colMap.keySet()) {
			sql1 += colMap.get(key) + ", ";
			//sql2 += "'" + jsonObject.getString(key) + "', ";
			sql2 += "?, "; 
			//System.out.println("Key = " + key);
			sql3 += colMap.get(key) + " = ?, ";
		}

		sql = sql1.substring(0, sql1.length() - 2) + ") " + sql2.substring(0,  sql2.length() - 2) + ")";

		//System.out.println(sql1 + ") ");
		//System.out.println(sql2 + ") ");

		//return "ok";

		ps = conn.prepareStatement(sql);
		
		String sqlUpdate = sql3.substring(0, sql3.length() - 2) + " " + sql4;
		
		System.out.println("sqlUpdate: " + sqlUpdate);
		
		PreparedStatement ps1 = conn.prepareStatement(sqlUpdate);

		//String[] dateColsStr = { "delivery_date", "est_delivery_dte", "received_date", "reqd_deliv_date", "start_date" };
		//HashSet<String> dateCols = new HashSet<String>(Arrays.asList(dateColsStr));

		int c=1;
		for (String key : colMap.keySet()) {
			//System.out.println("key: " + key);
			if (jsonObject.has(key)) {
				//System.out.println("value: " + jsonObject.get(key));
				if (jsonObject.isNull(key) || jsonObject.getString(key).equals("")) {
					//System.out.println("set null");
					ps.setNull(c, Types.VARCHAR);
					ps1.setNull(c, Types.VARCHAR);
				}
				else {
					//System.out.println("set String");
					ps.setString(c, jsonObject.getString(key).replaceAll("\n", "\r\n"));
					ps1.setString(c, jsonObject.getString(key).replaceAll("\n", "\r\n"));
				}
			}
			c++;
		}

		ps.execute();
		
		ps1.setString(c, jsonObject.getString("sn"));
		
		ps1.execute();

		return "ok";

	}
	
	public String addAmendment(String sn, String amendmentDeveloper, String amendmentDate, String amendmentNote) throws SQLException {
		
		String sql = "INSERT INTO DBO.PROJAMENDMENTS ([Serial Number], Developer, DeliveryDate, [Amendment Note]) values (?, ?, ?, ?)";
		
		Connection conn = null;
		conn = dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ps.setString(1, sn);
		ps.setString(2, amendmentDeveloper);
		if (amendmentDate.trim().length() != 10) {
			ps.setNull(3, Types.DATE);
		}
		else {
			ps.setString(3, amendmentDate);
		}
		ps.setString(4, amendmentNote);

		ps.execute();
		
		return "ok";
			
		
	}
	
	public String removeAmendment(String id) throws SQLException {
		
		String sql = "DELETE FROM DBO.PROJAMENDMENTS WHERE ID = ?";
		
		Connection conn = null;
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ps.setString(1, id);

			ps.execute();
			
			return "ok";
			
	}
	
	/*public String createPlan(String projectCode, String path) throws IOException, SQLException {

		JSONObject projectInfo = getProjectInfo(projectCode, "All");
		
		//System.out.println(projectInfo.toString());
		
		//System.out.println("path: " + path);
		
		Path folder = Paths.get(path);
		
		if (!Files.exists(folder)) {
			try {
				Files.createDirectory(folder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<String> lines = Arrays.asList(
				"REQUESTER:", 
				projectInfo.has("requester") ? projectInfo.getString("requester") : "", 
				"","",
				"PROJECT DESCRIPTION:",
				projectInfo.has("description") ? projectInfo.getString("description") : "",
				"","",
				"PURPOSE OF REQUEST:",
				projectInfo.has("purpose_of_request") ? projectInfo.getString("purpose_of_request") : "",
				"","",
				"DATA ELEMENTS:",
				projectInfo.has("data_elements") ? projectInfo.getString("data_elements") : "",
				"","",
				"CRITERIA:",
				projectInfo.has("criteria") ? projectInfo.getString("criteria") : "",
				"",""
				);
		Path file = Paths.get(path + "\\Project Plan.txt");
		
		//System.out.println(path + "\\Project Plan.txt");
		
			int c=1;
			while (Files.exists(file)) {
				file = Paths.get(path + "\\Project Plan (" + c + ").txt");
				c++;
			}
			Files.write(file, lines, Charset.forName("UTF-8"));
		
		
		return "ok";
		
	}*/
	
	public JSONArray custodianSearch(String searchString) throws SQLException {

		Connection conn = null;
		conn = dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement("select distinct top 20 rtrim(cust_name) cust_name " + 
				"from data_custodians " + 
				"where CUST_NOTES IN ('IDB', 'DADM', 'DV', 'DVIDIC') AND lower(cust_name) like ? " +
				"order by 1");
		ps.setString(1, '%' + searchString.toLowerCase() + '%');
		ResultSet rs = ps.executeQuery();

		JSONArray arrayJSON = new JSONArray();
		while (rs.next()) {
			JSONObject tableJSONObj = new JSONObject();

			tableJSONObj.put("label", rs.getString("cust_name"));
			tableJSONObj.put("id", rs.getString("cust_name"));

			arrayJSON.put(tableJSONObj);

		}
		return arrayJSON;
				
	}

	public String deleteVersion(String projectCode, String sn, String version) throws SQLException {
		
		//System.out.println("version to delete: " + version);
		
		// need to determine if version being deleted is the latest version.
		// if so - then you need to also update the PROJECTS table with the latest version.
		
		String sql = "DELETE FROM DBO.PROJECTS_R2D4_COMMITS WHERE [Project Code] = ? and [Serial Number] = ? and [Commit Date] = ? and [Commit Time] = ?";
		
		String[] dateTime = version.split(" ");
		String date = dateTime[0];
		String time = dateTime[1];
		
		Connection conn = null;
		conn = dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ps.setString(1, projectCode);
		ps.setString(2, sn);
		ps.setString(3, date);
		ps.setString(4, time);

		ps.execute();
		
		sql = " UPDATE " + 
				"    P " + 
				" SET " + 
				"	P.[Project Code] = P_V.[Project Code]," + 
				"	P.[Serial Number] = P_V.[Serial Number]," + 
				"	P.[Project Description] = P_V.[Project Description]," + 
				"	P.[Requester_ID] = P_V.[Requester_ID]," + 
				"	P.[Requester ID Old] = P_V.[Requester ID Old]," + 
				"	P.[Project Request Date] = P_V.[Project Request Date]," + 
				"	P.[Project Req_rec Date] = P_V.[Project Req_rec Date]," + 
				"	P.[Project Req_rec By] = P_V.[Project Req_rec By]," + 
				"	P.[Project Req_delv Date] = P_V.[Project Req_delv Date]," + 
				"	P.[Project Completion Status] = P_V.[Project Completion Status]," + 
				"	P.[Project Completion Date] = P_V.[Project Completion Date]," + 
				"	P.[Project Delivery Date] = P_V.[Project Delivery Date]," + 
				"	P.[Project Delivery Desc] = P_V.[Project Delivery Desc]," + 
				"	P.[Project Complexity] = P_V.[Project Complexity]," + 
				"	P.[Project Type] = P_V.[Project Type]," + 
				"	P.[Project Priority] = P_V.[Project Priority]," + 
				"	P.[Project Effort] = P_V.[Project Effort]," + 
				"	P.[Project Volume in Bytes] = P_V.[Project Volume in Bytes]," + 
				"	P.[Project Volume in Pages] = P_V.[Project Volume in Pages]," + 
				"	P.[Project IRB/Waiver #] = P_V.[Project IRB/Waiver #]," + 
				"	P.[Request Description] = P_V.[Request Description]," + 
				"	P.[Request Purpose] = P_V.[Request Purpose]," + 
				"	P.[Delivery Plan] = P_V.[Delivery Plan]," + 
				"	P.[Data Elements] = P_V.[Data Elements]," + 
				"	P.[Criteria] = P_V.[Criteria]," + 
				"	P.[Project Notes] = P_V.[Project Notes]," + 
				"	P.[Schedule Status] = P_V.[Schedule Status]," + 
				"	P.[Project % Complete] = P_V.[Project % Complete]," + 
				"	P.[Project Request Type] = P_V.[Project Request Type]," + 
				"	P.[Project ManAuto] = P_V.[Project ManAuto]," + 
				"	P.[Project Tech Specs] = P_V.[Project Tech Specs]," + 
				"	P.[Project Test Plan] = P_V.[Project Test Plan]," + 
				"	P.[Developer Primary] = P_V.[Developer Primary]," + 
				"	P.[Developer Secondary] = P_V.[Developer Secondary]," + 
				"	P.[QA Plan Developer] = P_V.[QA Plan Developer]," + 
				"	P.[QA Plan Due Date] = P_V.[QA Plan Due Date]," + 
				"	P.[QA Plan Complete] = P_V.[QA Plan Complete]," + 
				"	P.[QA Results Developer] = P_V.[QA Results Developer]," + 
				"	P.[QA Results Due Date] = P_V.[QA Results Due Date]," + 
				"	P.[QA Results Complete] = P_V.[QA Results Complete]," + 
				"	P.[Project SQL] = P_V.[Project SQL]," + 
				"	P.[DeveloperNotes] = P_V.[DeveloperNotes]," + 
				"	P.[ObjectsDelivered] = P_V.[ObjectsDelivered]," + 
				"	P.[WEBFORM_ID] = P_V.[WEBFORM_ID]," + 
				"	P.[WEBFORM_TYPE] = P_V.[WEBFORM_TYPE]," + 
				"	P.[Serial Number Old] = P_V.[Serial Number Old]," + 
				"	P.[Project Code Cross Reference] = P_V.[Project Code Cross Reference]" + 
				" FROM " + 
				"    dbo.PROJECTS AS P " + 
				"    INNER JOIN dbo.PROJECTS_R2D4_v AS P_V ON P.[Serial Number] = P_V.[Serial Number] " + 
				" WHERE " + 
				"    P.[Project Code] = ? and P.[Serial Number] = ? ";
		
		PreparedStatement ps1 = conn.prepareStatement(sql);
		
		ps1.setString(1, projectCode);
		ps1.setString(2, sn);

		ps1.execute();
		
		if (ps.getUpdateCount() > 0) {
			return "Successfully deleted " + ps.getUpdateCount() + " version.";
		}
		else 
			return "Error: " + ps.getUpdateCount() + " rows deleted.";
			
	}
	
	public String sendMail(String from, String to, String msg) {
		Properties properties = new Properties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.host", "outlookmail.mskcc.org");
		properties.put("mail.smtp.port", "25");
		properties.put("mail.smtp.auth", "true");

		final String username = "";
		final String password = "";
		Authenticator authenticator = new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(username, password);
		    }
		};

		Transport transport = null;

		try {
		    Session session = Session.getDefaultInstance(properties, authenticator);
		    MimeMessage mimeMessage = new MimeMessage(session);
		    transport = session.getTransport();
		    transport.connect(username, password);
		    transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    if (transport != null) try { transport.close(); } catch (MessagingException logOrIgnore) {}
		}

		return "ok";
	}
	
	 public String determineIfFolderExists(String folder) {
		 
			Path folderPath = Paths.get(folder);
			
			System.out.println("In determineIfFolderExists. Checking if folderPath exists: " + folderPath + ", exists: " + Files.exists(folderPath) + ", not exists: " + Files.notExists(folderPath));
			
			if (Files.exists(folderPath)) {
				return "ok";
			}
			
			return "dne";
		 
	 }
	
	 public String createProjectFolder(String folder) throws IOException {
		 
			Path folderPath = Paths.get(folder);
			
			//System.out.println("In createProjectFolder. Checking if folderPath exists: " + folderPath + ", exists: " + Files.exists(folderPath) + ", not exists: " + Files.notExists(folderPath));

			if (Files.notExists(folderPath)) {
				Files.createDirectory(folderPath);
			}

			if (Files.notExists(folderPath)) {
				//Files.createDirectory(folderPath);
				return "not ok";
			}

			return "ok";
		 
	 }
	 
	 public String sendEmailDialog(String formData, String username) throws SQLException, IOException {
		JSONObject jsonObject = new JSONObject(formData);
		HashMap<String, Object> userPrefs = getAllUserPrefs(username);
		String message_type = jsonObject.getString("emailType");
		
		String message_body = "";
		if (userPrefs.containsKey(message_type + "_message")) {
			JSONObject prefJsonObject = (JSONObject) userPrefs.get(message_type + "_message");
			message_body = (String) prefJsonObject.getString("pref_value_1");		
		}

		 String[] version = jsonObject.getString("version").split("\\.");
		 		 
		 if (version.length > 0) {
			 jsonObject.put("version", version[0]);
		 }

		 //String developerFullFromAddress = "\"" + jsonObject.getString("developerFromAddress") + "\" <" + jsonObject.getString("developerUsername").toLowerCase() + "@mskcc.org>";
		 String developerFullFromAddress = "\"" + jsonObject.getString("developerFromAddress") + "\" <" + jsonObject.getString("developerEmail").toLowerCase() + ">";
		 String subject = "DataLine Plan " + jsonObject.getString("sn_code");
		 String body = "";
		 jsonObject.put("privacy", "");

		 Path msgFile = null;
		 Path projectInfoFile = null;

		 int emailHeaderWidth = 600;

		 if (jsonObject.getString("emailType").equals("request_received")) {
			 subject = "New DataLine Request " + jsonObject.getString("sn_code");

			 String emailToRcv = jsonObject.getString("emailTo");

			 jsonObject.put("emailTo", "\"" + ldapLookup(emailToRcv.substring(0, emailToRcv.indexOf('@')), "cn", "displayName") + "\" <" + emailToRcv + ">; ");

			 msgFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\received_message.html");
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\request.html");

		 }
		 else if (jsonObject.getString("emailType").equals("first_contact")) { 
			 subject = "DataLine Request " + jsonObject.getString("sn_code");

			 String emailToCon = jsonObject.getString("emailTo");

			 jsonObject.put("emailTo", "\"" + ldapLookup(emailToCon.substring(0, emailToCon.indexOf('@')), "cn", "displayName") + "\" <" + emailToCon + ">; ");

			 msgFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\contact_message.html");
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\request.html");

		 }
		 else if (jsonObject.getString("emailType").equals("requester")) { 
			 subject = "DataLine Plan " + jsonObject.getString("sn_code") + " for your approval";

			 String emailToReq = jsonObject.getString("emailTo");

			 jsonObject.put("emailTo", "\"" + ldapLookup(emailToReq.substring(0, emailToReq.indexOf('@')), "cn", "displayName") + "\" <" + emailToReq + ">; ");
			 jsonObject.put("requesterOrCustodianName", jsonObject.getString("requesterName"));
			 //jsonObject.put("approvalLink", "");

			 msgFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\requester_or_custodian_message.html");
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\plan.html");

		 }
		 else if (jsonObject.getString("emailType").equals("requester_and_custodians") || jsonObject.getString("emailType").equals("custodians")) { 
			 subject = "DataLine Plan " + jsonObject.getString("sn_code") + " for your approval";

			 String[] emailTos = jsonObject.getString("emailTo").split("\\|");
			 
			 System.out.println("emailTo string: " + jsonObject.getString("emailTo"));

			 HashSet<String> emailToSet = new HashSet<String>();
			 for (String emailTo : emailTos) {
				 emailToSet.add(emailTo);
			 }

			 String emailToString = "";
			 Connection conn = dataSource.getConnection();
			 for (String emailTo : emailToSet) {
				 PreparedStatement ps = conn.prepareStatement("SELECT rtrim(custodian_cn) custodian_cn  " + 
						 "FROM     dbo.custodian_cn cust " + 
						 "WHERE    custodian_name = ? and custodian_cn <> 'Privacy'");
				 ps.setString(1, emailTo);
				 ResultSet rs = ps.executeQuery();

				 if (rs.next()) {
					 emailToString += "\"" + ldapLookup(rs.getString("custodian_cn"), "cn", "displayName") + "\" <" + rs.getString("custodian_cn").toLowerCase() + "@mskcc.org>; ";
				 }
				 else if (jsonObject.getString("emailType").equals("requester_and_custodians")) {
					 emailToString += "\"" + ldapLookup(emailTo, "mail", "displayName") + "\" <" + emailTo + ">;";
				 }
				 rs.close();
			 }

			 conn.close();

			 jsonObject.put("emailTo", emailToString.length() > 0 ? emailToString.substring(0, emailToString.length()-1) : "");
			 jsonObject.put("requesterOrCustodianName", "{dataline_approver}");
			 jsonObject.put("privacy", "<input id='privacy_checkbox_dialog' type='checkbox' style='float:none;width:20px;height:20px;' value='privacy' checked>Include Privacy</input>");
			 //jsonObject.put("approvalLink", "<br>http://vsmsktdvbi1:8080/DataLine/approve.html?projectCode=" + jsonObject.getString("sn_code") + "&randCode=" + approvalLink + "<br>");

			 msgFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\requester_or_custodian_message.html");
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\plan.html");

		 }
		 else if (jsonObject.getString("emailType").equals("delivery")) { 
			 subject = "Delivery of DataLine Request " + jsonObject.getString("sn_code");
		 }

		 body = "<table><tr><td>From:</td><td><input id='email_from' type='text' style='width:" + emailHeaderWidth + "px;' value='" + developerFullFromAddress + "'></td><td>{privacy}</td></tr>"
				 + "<tr><td>To:</td><td><input id='email_to' type='text' style='width:" + emailHeaderWidth + "px;' value='" + jsonObject.getString("emailTo") + "'></td><td></td></tr>"
				 + "<tr><td>CC:</td><td><input id='email_cc' type='text' style='width:" + emailHeaderWidth + "px;' value=''></td><td></td></tr>"
				 + "<tr><td>BCC:</td><td><input id='email_bcc' type='text' style='width:" + emailHeaderWidth + "px;' value='" + developerFullFromAddress + "'></td><td></td></tr>"
				 + "<tr><td>Subject:</td><td><input id='email_subject' type='text' style='width:" + emailHeaderWidth + "px;' value='" + subject + "'></td><td></td></tr></table>";

		 if (message_body.equals("")) {
			 List fileMsgList = null;
			 
			 System.out.println("msgFile: " + msgFile);
			 fileMsgList = Files.readAllLines(msgFile);

			 body += String.join("", fileMsgList);

		 }
		 else {
			 body += "<div style='text-align:left;background-color:rgb(255, 255, 255);padding:5px;'><span style='font-family:Calibri;font-size:11pt;'>" 
					 + "{input_open}" 
					 + message_body
					 .replaceAll("<project_code>", jsonObject.getString("sn_code"))
					 .replaceAll("<project_info>", "")
					 .replaceAll("<requester_first_name>", jsonObject.getString("requesterFirstName"))
					 .replaceAll("<requester_last_name>", jsonObject.getString("requesterLastName"))
					 .replaceAll("<requester_full_name>", jsonObject.getString("requesterName"))
					 + "{input_close}"
					 + "</span></div>";			 
		 }

		 List projectInfoFileList = null;
		 projectInfoFileList = Files.readAllLines(projectInfoFile);
		 body += String.join("", projectInfoFileList);

		 //System.out.println(body);

		 for (String key : jsonObject.keySet()) {
			 body = body.replaceAll("\\{" + key + "\\}", jsonObject.get(key).toString().replaceAll("\\n", "<br>"));
		 }

		 body = body.replaceAll("\\{input_open\\}", "<textarea id='email_body' style='width:850px;height:105px;'>");
		 body = body.replaceAll("\\{input_close\\}", "</textarea>");
		 body = body.replaceAll("\\{spacer\\}", "<br>");

		 return body;
	 }

	 public String sendEmailDialogMessage(String formData) throws SQLException, IOException {
		 JSONObject jsonObject = new JSONObject(formData);

		 String[] version = jsonObject.getString("version").split("\\.");
		 
		 if (version.length > 0) {
			 jsonObject.put("version", version[0]);
		 }

		 Path projectInfoFile = null;
		 
		 // remove trailing semi-colons
		 String toAddresses = jsonObject.getString("emailTo").replaceAll(";+$", "");

		 if (jsonObject.getString("emailType").equals("received")) {
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\request.html");
		 }
		 else if (jsonObject.getString("emailType").equals("contact")) {
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\request.html");
		 }
		 else if (jsonObject.getString("emailType").equals("requester")) {
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\plan.html");
		 }
		 else if (jsonObject.getString("emailType").equals("custodians") || jsonObject.getString("emailType").equals("requester_and_custodians")) {
			 // if privacy checked
			 //System.out.println("privacy checkbox: " + jsonObject.getBoolean("sendToPrivacy")); 
			 if (jsonObject.getBoolean("sendToPrivacy")) {
				 String res = addApproval(jsonObject.getInt("sn"), "Privacy", "", jsonObject.getString("sn_code"));
				 
				 toAddresses = "\"Privacy Office/Hospital Administration\" <privacy@mskcc.org>;" + toAddresses;
			 }
			 projectInfoFile = Paths.get("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\html\\plan.html");
		 }
		 
		 System.out.println("toAddresses: " + toAddresses);

		 String[] toList = toAddresses.split(";");

		 for (String toAddress : toList) {
			 System.out.println("In loop of toADdresses");
			 if (toAddress.contains("@")) {
				 System.out.println("toAddress contains @ sign");

				 String body = jsonObject.getString("emailBody").replaceAll("\\n", "<br>");
				 String approvalLink = "";

				 List projectInfoFileList = null;
				 projectInfoFileList = Files.readAllLines(projectInfoFile);

				 body += String.join("", projectInfoFileList);

				 for (String key : jsonObject.keySet()) {
					 body = body.replaceAll("\\{" + key + "\\}", jsonObject.get(key).toString().replaceAll("\\n", "<br>"));
				 }

				 System.out.println("toAddress: " + toAddress);
				 String[] custodian_address = toAddress.split("<");
				 String custodian_cn = custodian_address.length > 1 ? custodian_address[1].trim().substring(0, custodian_address[1].indexOf("@")).toUpperCase() : "";
				 String custodianName = "";
				 System.out.println(custodian_cn);
				 System.out.println("emailType: " + jsonObject.getString("emailType"));

				 if (jsonObject.getString("emailType").equals("custodians") || jsonObject.getString("emailType").equals("requester_and_custodians")) {

					 Connection conn = null; 
					 conn = dataSource.getConnection();
					 String sql = "SELECT rtrim(custodian_cn) custodian_cn, [URL Code] as link_num, custodian_name  " + 
							 "FROM     dbo.custodian_cn cust " + 
							 "         left join dbo.PROJCUST_R2D4 proj on proj.[Data Custodian]=cust.[CUSTODIAN_NAME] " + 
							 "WHERE    [Serial Number]=? and upper(custodian_cn) = ? --and Status = 0 and approved = 0";
					 System.out.println("custodian sql: " + sql + ", sn: " + jsonObject.getInt("sn") + ", upper(cn): " + custodian_cn);
					 PreparedStatement ps = conn.prepareStatement(sql);
					 ps.setInt(1, jsonObject.getInt("sn"));
					 ps.setString(2, custodian_cn);
					 ResultSet rs = ps.executeQuery();

					 if (rs.next()) {
						 custodianName = rs.getString("custodian_name");
						 approvalLink = "<br><br>In order to approve this request for " + custodianName + ", click here:<br>" +
								 "<a href=\"http://vsmsktdvbi1:8080/DataLine/approve.html?projectCode=" + jsonObject.getString("sn_code") + "&randCode=" + 
								 rs.getInt("link_num") + "\">Link</a><br><br>";
					 }
					 body = body.replaceAll("\\{approvalLink\\}", "<br>" + approvalLink + "<br>");
					 body = body.replaceAll("\\{dataline_approver\\}", custodianName);
				 }
				 
				 else if (jsonObject.getString("emailType").equals("requester")) {
					 approvalLink = "<br><br>In order to approve this request for " + jsonObject.getString("requesterName") + ", click here:<br>" +
							 "<a href=\"http://vsmsktdvbi1:8080/DataLine/approve.html?projectCode=" + jsonObject.getString("sn_code") + "&randCode=REQ" + 
							 jsonObject.getString("sn") + "\">Link</a><br><br>";
					 body = body.replaceAll("\\{approvalLink\\}", "<br>" + approvalLink + "<br>");
				 }
				 
				 body = body.replaceAll("\\{spacer\\}", "<br><br>");

				 //body += String.join("", projectInfoFile);

				 //String fromAddress = jsonObject.getString("developerUsername") + " <" + jsonObject.getString("emailFrom") + ">";

				 //String res = sendEmail(jsonObject.getString("emailFrom"), toAddress, jsonObject.getString("emailCc"), jsonObject.getString("emailBcc"), "Subject", jsonObject.getString("emailBody"));

				 System.out.println("sendEmail(" + jsonObject.getString("emailFrom") + ", " + toAddress + ", " + jsonObject.getString("emailCc") + ", " + jsonObject.getString("emailBcc") + ", " + jsonObject.getString("emailBcc") + ", " + jsonObject.getString("emailBody").replaceAll("\\n", "<br>") + ");");

				 String res = sendEmail(jsonObject.getString("emailFrom"), toAddress, jsonObject.getString("emailCc"), jsonObject.getString("emailBcc"), jsonObject.getString("emailSubject"), body);

			 }
		 }


		 return "";
	 }

	 
	public String sendEmail(String from, String to, String cc, String bcc, String subject, String body) throws SQLException, IOException {

		DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());

		String url = "jdbc:db2://pidvudb1:51013/DVPDB01";
		/*String user = "singerm";

		Path file = Paths.get("C:\\R2D4\\eclipse-workspace\\dataline.properties");

		List fileAsList = Files.readAllLines(file);

		String fileAsStr = (String) fileAsList.get(0);

		String password = fileAsStr;*/

    	/*HashMap<String, String> propMap = new HashMap<String, String>();
        FileReader reader = new FileReader("C:\\R2D4\\eclipse-workspace\\r2d4.properties");
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line;

        while ((line = bufferedReader.readLine()) != null) {
        	propMap.put(line.split("=")[0], line.split("=")[1]);
        }
    	System.out.println("propMap[idb_pswd]=" + propMap.get("idb_pswd"));
    	System.out.println("propMap[darwin_apps_pswd]=" + propMap.get("darwin_apps_pswd"));
        reader.close();*/

		Connection con = DriverManager.getConnection(url, propMap.get("darwin_apps_user"), propMap.get("darwin_apps_pswd"));
		con.setAutoCommit(true);

		//Statement stmt = con.createStatement();       
		
			//String sql = "select DV.SENDJAVAXMAIL('singerm@mskcc.org','singerm@mskcc.org','','','project approved','<html><body><h2 style=\"color:red;\">Hi</h2></body></html>','text/html;charset=utf-8') " +
			String sql = "select DV.SENDJAVAXMAIL(?,?,?,?,?,?,'text/html;charset=utf-8') " +
					"from SYSIBM.SYSDUMMY1";
	
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, from);
			//ps.setString(2, to);
			ps.setString(2, "\"Singer, Michael N./Information Systems\" <singerm@mskcc.org>");
			ps.setString(3, cc);
			ps.setString(4, bcc);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, subject);
			ps.setString(6, body);
	
			ResultSet rs = ps.executeQuery();
			
			//rs = stmt.executeQuery(sql);
	
			//String resultSet = "";
	
			//while (rs.next()) {
			//	resultSet = rs.getString(1);
			//}
	
			rs.close();
	
			//stmt.close();

		con.close();                                                             

		return "ok";
	}

	 public String addApproval(int sn, String custodian, String table, String projectCode) throws SQLException {
		 
		 String custodian_email;
		 String sql = "";
		 String schema = "";
		 PreparedStatement ps = null;
		 Connection conn = dataSource.getConnection();

		 ResultSet rs = null;
		 
		 if (!custodian.equals("Privacy")) {
				 
			 // CHECK IF PERSON IS ACTUAL CUSTODIAN
			 // IF NOT, THEN DON'T ADD TO DATABASE
			 sql = 
					"SELECT DISTINCT CUST_NOTES AS TABLE_SCHEMA, CUST_NAME, CUST_TABLE, rtrim(custodian_cn)+'@mskcc.org' custodian_email "
					+ "FROM DBO.DATA_CUSTODIANS "
					+ "JOIN custodian_cn on Custodian_Name=cust_name "
					+ "WHERE CUST_NAME = ? AND CUST_TABLE = ? AND CUST_NOTES = ? AND CUST_NOTES IN ('IDB', 'DADM', 'DV', 'DVIDIC') ";
	
			 //System.out.println("sql: " + sql + ", sn: " + sn + ", cust: " + custodian + ", table: " + table);
			 
			 ps = conn.prepareStatement(sql);
	
			 if (table.contains(".")) {
				 schema = table.split("\\.")[0];
				 table = table.split("\\.")[1];
			 }
			 
			 ps.setString(1, custodian);
			 ps.setString(2, table);
			 ps.setString(3, schema);
	
			 rs = ps.executeQuery();
	
			 if (!rs.next()) {
				return "false";
			 }
			 else {
				 custodian_email = rs.getString("custodian_email");
			 }
		 }
		 else {
			 custodian_email = "privacy@mskcc.org";
		 }
		 
		 // INSERT CUSTODIAN TABLE ROWS
		 sql = 
				 "INSERT INTO DBO.PROJCUSTTABLES_R2D4 (\"Serial Number\", \"Data Custodian\", \"Table Name\", \"Table Schema\" ) "
						 + "SELECT ?, ?, ?, ?  "
						 + "EXCEPT " // SAME AS SET MINUS 
						 + "SELECT \"Serial Number\", \"Data Custodian\", \"Table Name\", \"Table Schema\" "
						 + "FROM DBO.PROJCUSTTABLES_R2D4"
						 ;

		 System.out.println("sql: " + sql + ", sn: " + sn + ", cust: " + custodian + ", table: " + table + ", schema: " + schema);

		 ps = conn.prepareStatement(sql);

		 ps.setInt(1, sn);
		 ps.setString(2, custodian);
		 ps.setString(3, table);
		 ps.setString(4, schema);

		 ps.execute();

		 // SET STATUS TO ACTIVE IF CUSTODIAN WAS DROPPED AND NOW RE-ADDED	
		 sql = "UPDATE DBO.PROJCUST_R2D4 SET STATUS = 0 "
				 + "WHERE [Serial Number] = ? and "
				 + "[Data Custodian] = ?";

		 //System.out.println("sql: " + sql + ", sn: " + sn + ", custodian: " + custodian);

		 ps = conn.prepareStatement(sql);

		 ps.setInt(1, sn);
		 ps.setString(2, custodian);

		 ps.execute();

		 // GET LIST FOR CREATING APPROVAL LINKS
		 sql = "select * from DBO.PROJCUST_R2D4 where [Serial Number] = ?";

		 //System.out.println("sql: " + sql + ", sn: " + sn);

		 conn = dataSource.getConnection();
		 ps= conn.prepareStatement(sql);

		 ps.setInt(1, sn);

		 rs = ps.executeQuery();

		 HashSet<Integer> randomNumSet = new HashSet<Integer>();
		 HashSet<String> existingCustodianSet = new HashSet<String>();
		 while (rs.next()) {
			 randomNumSet.add(rs.getInt("url code"));
			 existingCustodianSet.add(rs.getString("data custodian"));
		 }

		 //System.out.println("existingCustodianSet: " + existingCustodianSet.toString());
		 //System.out.println("custodian: " + custodian);
		 
		 if (!existingCustodianSet.contains(custodian)) {

			 Random rand = new Random();

			 int n = rand.nextInt(500000) + 1;

			 while (randomNumSet.contains(n)) {
				 n = rand.nextInt(500000) + 1;
			 }

			 sql = "INSERT INTO DBO.PROJCUST_R2D4 (\"Serial Number\", \"Data Custodian\", \"Project Code\", \"URL Code\", Status, Approved) "
					 + "VALUES "
					 + "(?, ?, ?, ?, ?, ?)";

			 System.out.println("sql: " + sql + ", sn: " + sn + ", custodian: " + custodian + ", projectCode: " + projectCode + ", n: " + n);

			 conn = dataSource.getConnection();
			 ps = conn.prepareStatement(sql);

			 ps.setInt(1, sn);
			 ps.setString(2, custodian);
			 ps.setString(3, projectCode);
			 ps.setInt(4, n);
			 ps.setInt(5, 0);
			 ps.setInt(6, 0);

			 // AVOID RACE CONDITION WHEN IT TRIES TO INSERT ROW THAT ALREADY EXISTS
			 try {
				 ps.execute();
			 }
			 catch (SQLException e) {
				 e.printStackTrace();
			 }

			 conn.close();

		 }

		 conn.close();

		 return custodian_email;

	 }

	 public String removeApproval(int sn, String custodian, String table, String projectCode) throws SQLException {

		 // DELETE ALL CUSTODIAN TABLE ROWS
		 String sql = "DELETE FROM DBO.PROJCUSTTABLES_R2D4 WHERE \"Serial Number\" = ? AND \"Data Custodian\" like ? AND \"Table Name\" = ?  AND \"Table Schema\" = ? ";

		 String schema = table.split("\\.")[0];
		 table = table.split("\\.")[1];
		 
		 System.out.println("sql: " + sql + ", sn: " + sn + ", custodian: " + custodian + ", schema: " + schema + ", table: " + table);

		 Connection conn = dataSource.getConnection();
		 PreparedStatement ps = conn.prepareStatement(sql);

		 ps.setInt(1, sn);
		 ps.setString(2, custodian);
		 ps.setString(3, table);
		 ps.setString(4, schema);

		 ps.execute();
		 
		 boolean deactivate = false;
		 // MAKE SURE NO MORE TABLES BELONG TO CUSTODIAN BEFORE DEACTIVATING THEM
		 sql = "select count(*) c from dbo.projcusttables_r2d4 where \"serial number\" = ? and \"data custodian\" like ? ";
		 System.out.println("sql: " + sql + ", sn: " + sn + ", custodian: " + custodian + ", table: " + table);

		 ps = conn.prepareStatement(sql);

		 ps.setInt(1, sn);
		 ps.setString(2, custodian);
		 
		 ResultSet rs = ps.executeQuery();

		 if (rs.next() && rs.getInt("c") == 0) {
			 System.out.println("c: " + rs.getInt("c"));
			 deactivate = true;
		 }

		 /*if (!custodian.equals("%")) {
		 }
		 else {
			 deactivate = true;
		 }*/
		 
		 if (deactivate) {
			 // NEVER DELETE - JUST SET STATUS - DONT WANT TO LOSE APPROVAL INFO
			 // CHECKS IF CUSTODIAN ONLY HAS ONE TABLE FOR THE PROJECT
			 // OTHERWISE, COULD INCORRECTLY DEACTIVATE CUSTODIAN WHEN HAS MULTIPLE TABLES
			 sql = "UPDATE DBO.PROJCUST_R2D4 SET STATUS = 1 "
					 + "WHERE [Serial Number] = ? and "
					 + "[Data Custodian] like ? ";

			 System.out.println("sql: " + sql + ", sn: " + sn + ", custodian: " + custodian + ", table: " + table);

			 ps = conn.prepareStatement(sql);

			 ps.setInt(1, sn);
			 ps.setString(2, custodian);
			 
			 ps.execute();
		 }

		 conn.close();

		 return "success";
	 }

	 public String removeAllApprovals(int sn) throws SQLException {
		 String sql = "DELETE FROM DBO.PROJCUSTTABLES_R2D4 WHERE \"Serial Number\" = ? ";

		 System.out.println("sql: " + sql + ", sn: " + sn);

		 Connection conn = dataSource.getConnection();
		 PreparedStatement ps = conn.prepareStatement(sql);

		 ps.setInt(1, sn);

		 ps.execute();

		 sql = "UPDATE DBO.PROJCUST_R2D4 SET STATUS = 1 WHERE [Serial Number] = ? ";

		 System.out.println("sql: " + sql + ", sn: " + sn);

		 ps = conn.prepareStatement(sql);

		 ps.setInt(1, sn);

		 ps.execute();

		 return "success";
	 }
		 
		public String approveProject(String projectCode, String randCode) throws SQLException, IOException {

			String sql = "";
			
			Connection conn = dataSource.getConnection();
			PreparedStatement ps = null;
			
			if (randCode.substring(0, 3).equals("REQ")) {
				sql = "SELECT * FROM DBO.REQUESTER_APPROVALS_R2D4 WHERE \"Serial Number\" = ?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, Integer.valueOf(projectCode.replaceAll("[^\\d.]", "")));
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					sql = "UPDATE DBO.REQUESTER_APPROVALS_R2D4 SET APPROVED_TIME = GETDATE() WHERE \"Serial Number\" = ?";
					ps = conn.prepareStatement(sql);
					
					ps.setInt(1, Integer.valueOf(projectCode.replaceAll("[^\\d.]", "")));					 
					ps.execute();
				}
				else {
					sql = "INSERT INTO DBO.REQUESTER_APPROVALS_R2D4 (\"Serial Number\", \"Project Code\", \"URL Code\", Approved_Time) "
						 + "VALUES "
						 + "(?, ?, ?, GETDATE())";
	
					System.out.println("sql: " + sql + ", projectCode(stripped): " + projectCode.replaceAll("[^\\d.]", "") + ", projectCode: " + projectCode + ", randCode: " + randCode);
	
					//conn = dataSource.getConnection();
					ps = conn.prepareStatement(sql);
	
					ps.setInt(1, Integer.valueOf(projectCode.replaceAll("[^\\d.]", "")));
					ps.setString(2, projectCode);
					ps.setInt(3, Integer.valueOf(randCode.replaceAll("[^\\d.]", "")));
					 
					ps.execute();
				}
			}
			else if (randCode.matches("[0-9]+")) {

				sql = "UPDATE DBO.PROJCUST_R2D4 SET APPROVED = 1, APPROVED_TIME = GETDATE() WHERE \"Project Code\" = ? AND \"URL Code\" = ? AND APPROVED = 0";

				System.out.println("sql: " + sql + ", projectCode: " + projectCode + ", randCode: " + randCode);
	
				//conn = dataSource.getConnection();
				ps = conn.prepareStatement(sql);
	
				ps.setString(1, projectCode);
				ps.setString(2, randCode);
	
				ps.execute();
	
				// select all unapproved and undeleted approvals
				sql = "select count(*) c from DBO.PROJCUST_R2D4 WHERE \"Project Code\" = ? AND APPROVED = 0 AND STATUS = 0 ";
	
				System.out.println("sql: " + sql + ", projectCode: " + projectCode);
	
				ps = conn.prepareStatement(sql);
	
				ps.setString(1, projectCode);
	
				ResultSet rs = ps.executeQuery();
	
				if (rs.next() && rs.getInt("c") == 0) {
					System.out.println("c: " + rs.getInt("c"));
					String s = sendEmail("singerm@mskcc.org", "singerm@mskcc.org", "", "", projectCode + " approved", "project approved");
				}
				ps.execute();

			}
				
			return projectCode;
		}
		
		private String ldapLookup(String username, String attributeSent, String attributeReturned) throws SQLException, IOException {
			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());

			String url = "jdbc:db2://pidvudb1:51013/DVPDB01";
			// Set URL for data source
			/*String user = "singerm";

			Path propFile = Paths.get("C:\\R2D4\\eclipse-workspace\\dataline.properties");

			List fileAsList = Files.readAllLines(propFile);

			String fileStr = (String) fileAsList.get(0);

			String password = fileStr;*/

	    	/*HashMap<String, String> propMap = new HashMap<String, String>();
	        FileReader reader = new FileReader("C:\\R2D4\\eclipse-workspace\\r2d4.properties");
	        BufferedReader bufferedReader = new BufferedReader(reader);

	        String line;

	        while ((line = bufferedReader.readLine()) != null) {
	        	propMap.put(line.split("=")[0], line.split("=")[1]);
	        }
	    	System.out.println("propMap[idb_pswd]=" + propMap.get("idb_pswd"));
	    	System.out.println("propMap[darwin_apps_pswd]=" + propMap.get("darwin_apps_pswd"));
	        reader.close();*/

	        String user = propMap.get("darwin_apps_user");
	        Connection con = DriverManager.getConnection(url, propMap.get("darwin_apps_user"), propMap.get("darwin_apps_pswd"));

			con.setAutoCommit(true);

			Statement stmt;
			ResultSet rs;

			stmt = con.createStatement();

//			String getNameSQL = "select dv.ldapquery('" + username + "', 'cn', 'displayName') from sysibm.sysdummy1";
			String getNameSQL = "select dv.ldapquery('" + username + "', '" + attributeSent + "', '" + attributeReturned + "') from sysibm.sysdummy1";

			rs = stmt.executeQuery(getNameSQL);
			//System.out.println("**** Created JDBC ResultSet object");

			String returnValue = "";

			// Print all of the employee numbers to standard output device
			if (rs.next()) {
				returnValue = rs.getString(1);
				//System.out.println("LDAP query result = " + returnValue);
			}
			//System.out.println("**** Fetched all rows from JDBC ResultSet");
			// Close the ResultSet
			rs.close();
			//System.out.println("**** Closed JDBC ResultSet");

			// Close the Statement
			stmt.close();
			//System.out.println("**** Closed JDBC Statement");

			// Close the connection
			con.close();

			//System.out.println("**** Disconnected from data source");

			//System.out.println("**** JDBC Exit from class EzJava - no errors");
			
			return returnValue;

		}

		public String searchSn(String sn) throws SQLException {
			
			Connection conn = null;
			conn = dataSource.getConnection();

			String sql = "";
			
			if (sn.matches("[0-9]+")) {
				sql = "select \"project code\" from dbo.projects where \"serial number\" = ? "; 
			}
			else {
				sql = "select \"project code\" from dbo.projects where \"project code\" = ? "; 
			}
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, sn);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getString("project code");
			}
			return "no match";
		}
		
		/*
		 * 
		 * USE [DEDGPDLR2D2]
			GO
			
			/****** Object:  Table [dbo].[SCHEDULER]    Script Date: 03/06/2018 17:02:12 *****
			SET ANSI_NULLS ON
			GO
			
			SET QUOTED_IDENTIFIER ON
			GO
			
			SET ANSI_PADDING ON
			GO
			
			CREATE TABLE [dbo].[SCHEDULER](
				[id] [int] IDENTITY(1,1) NOT NULL,
				[Delivery_type_id] [tinyint] NULL,
				[Project_Code] [varchar](16) NULL,
				[Database] [varchar](50) NULL,
				[Interval] [varchar](30) NULL,
				[Interval_N] [tinyint] NULL,
				[Start_Time] [datetime2](0) NULL,
				[Sunday] [bit] NULL,
				[Monday] [bit] NULL,
				[Tuesday] [bit] NULL,
				[Wednesday] [bit] NULL,
				[Thursday] [bit] NULL,
				[Friday] [bit] NULL,
				[Saturday] [bit] NULL,
				[Hour] [tinyint] NULL,
				[Minute] [tinyint] NULL,
				[Enabled] [bit] NULL,
			PRIMARY KEY CLUSTERED 
			(
				[id] ASC
			)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
			) ON [PRIMARY]
			
			GO
			
			SET ANSI_PADDING OFF
			GO



		 * 
		 */

		public JSONArray getSchedule() throws SQLException {
			
			JSONArray arrayJSON = new JSONArray();
			
			Connection conn = dataSource.getConnection();
			String sql = "select [id],dt.[Delivery_type_id],dt.[Delivery_type],[Project_Code],[Project Description],[Database],[Interval],[Interval_N],[Start_Time],[Sunday],[Monday],[Tuesday],[Wednesday],[Thursday],[Friday],[Saturday],[Hour],[Minute],[Enabled],[Prereq],"
					//+ "recipient, day_of_month "
					+ " STUFF((SELECT distinct ',' + r.recipient from dbo.SCHEDULER_RECIPIENTS r where s.id = r.scheduler_id FOR XML PATH('')),1,1,'') recipients, "
					+ " STUFF((SELECT distinct ',' + cast(d.day_of_month as varchar(8000)) from dbo.SCHEDULER_DAYS_OF_MONTH d where s.id = d.scheduler_id FOR XML PATH('')),1,1,'') days_of_month, "
					+ " o.option_value_1 email_subject "
					+ " from dbo.scheduler s "
					+ " left join dbo.SCHEDULER_DELIVERY_TYPES dt on dt.delivery_type_id=s.delivery_type_id "
					+ " left join dbo.projects pj on pj.[Project Code]=s.[Project_Code] "
					+ " left join dbo.scheduler_options o on o.scheduler_id=s.id and o.option_name = 'EMAIL_SUBJECT'"
					//+ " left join dbo.scheduler_recipients r on r.scheduler_id = id "
					//+ " left join dbo.days_of_month d on d.scheduler_id = id "
					+ " order by id desc";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			System.out.println(sql);
			
			while (rs.next()) {
				
				JSONObject schedJSONObj = new JSONObject();
				
				schedJSONObj.put("id", rs.getInt("id"));
				schedJSONObj.put("delivery_type_id", rs.getInt("delivery_type_id"));
				schedJSONObj.put("delivery_type", rs.getString("delivery_type"));
				schedJSONObj.put("project_code", rs.getString("project_code"));
				String project_description = rs.getString("project description");
				if (project_description != null) 
					schedJSONObj.put("project_description", (rs.getString("project description").length() > 100 ? rs.getString("project description").substring(0, 100) + "..." : rs.getString("project description")));
				else 
					schedJSONObj.put("project_description", "");
				schedJSONObj.put("database", rs.getString("database"));
				schedJSONObj.put("interval", rs.getString("interval"));
				schedJSONObj.put("interval_n", rs.getString("interval_n"));
				schedJSONObj.put("start_time", rs.getTimestamp("start_time"));
				//schedJSONObj.put("days_of_week", (rs.getString("days_of_week") == null ? "0111110" : rs.getString("days_of_week")));
				schedJSONObj.put("sunday", rs.getBoolean("sunday"));
				schedJSONObj.put("monday", rs.getBoolean("monday"));
				schedJSONObj.put("tuesday", rs.getBoolean("tuesday"));
				schedJSONObj.put("wednesday", rs.getBoolean("wednesday"));
				schedJSONObj.put("thursday", rs.getBoolean("thursday"));
				schedJSONObj.put("friday", rs.getBoolean("friday"));
				schedJSONObj.put("saturday", rs.getBoolean("saturday"));
				
				schedJSONObj.put("hour", (rs.getInt("hour") == 0 ? 1 : rs.getInt("hour")));
				schedJSONObj.put("minute", rs.getInt("minute"));
				schedJSONObj.put("enabled", rs.getBoolean("enabled"));
				schedJSONObj.put("prereq", (rs.getString("prereq") == null ? "" : rs.getString("prereq").trim()));
				
				/*String daysOfMonth = rs.getString("days_of_month");

				String daysOfMonthCsv = "";
				if (daysOfMonth != null) {
					schedJSONObj.put("days_of_month_raw", daysOfMonth);
					int index = daysOfMonth.indexOf("1");
					while (index >= 0) {
						daysOfMonthCsv += String.valueOf(index) + ", ";
					    index = daysOfMonth.indexOf("1", index + 1);
					}
				}
				else {
					schedJSONObj.put("days_of_month_raw", "");
				}
				
				if (daysOfMonthCsv.length() > 0) daysOfMonthCsv = daysOfMonthCsv.substring(0, daysOfMonthCsv.length()-2);
				*/
				
				schedJSONObj.put("days_of_month", (rs.getString("days_of_month") == null ? "" : rs.getString("days_of_month")));
				schedJSONObj.put("recipients", (rs.getString("recipients") == null ? "" : rs.getString("recipients")));
				schedJSONObj.put("subject", (rs.getString("email_subject") == null ? "" : rs.getString("email_subject")));
								
				arrayJSON.put(schedJSONObj);
										
			}

			return arrayJSON;
		}

		public String schedulerCreateNewJob(String formData) throws SQLException {
			JSONObject formJsonObject = new JSONObject(formData);
			Connection conn = null;
			conn = dataSource.getConnection();
			
			// SQL SERVER 2008 (aka 10.5) - THERE ARE NO SEQUENCES
			
			/*String sql = "insert into dbo.scheduler ([id],[Delivery_type],[Project_Code],[Database],[Interval],[Start_Time],[Sunday],[Monday],[Tuesday],[Wednesday],[Thursday],[Friday],[Saturday],[Hour],[Minute],[Enabled]) "
					+ "values ((select max(id) from (select id from dbo.scheduler union select 0) a)+1, ?, ?, CURRENT_TIMESTAMP, ?, ?, 14, 14, 1, 'IDB') ";*/

			String sql = "insert into dbo.scheduler ([Database],[Project_Code],[Delivery_type_id],[Start_Time],[Interval],[Sunday],[Monday],[Tuesday],[Wednesday],[Thursday],[Friday],[Saturday],[Hour],[Minute],[Enabled],[Interval_N],[Prereq]) "
					+ " OUTPUT Inserted.ID "
					+ " values (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
			
			// NEEDS TO HAPPEN BEFORE THE PREPARE BECAUSE I'M MANUALLY REPLACING STRINGS
			if (!formJsonObject.getString("scheduler_start").equals("NOW")) {
				sql = sql.replace("CURRENT_TIMESTAMP", "'" + formJsonObject.getString("scheduler_start_date") + " " + formJsonObject.getString("scheduler_start_hour") + ":" + formJsonObject.getString("scheduler_start_minute") + "'");
			}
			
			System.out.println(sql);
			
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ps.setString(1, formJsonObject.getString("scheduler_database"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_database") + "'");
			
			ps.setString(2, formJsonObject.getString("project_code"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("project_code") + "'");

			ps.setString(3, formJsonObject.getString("scheduler_delivery_type"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_delivery_type") + "'");

			ps.setString(4, formJsonObject.getString("scheduler_interval"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_interval") + "'");
			
			ps.setString(5, formJsonObject.getString("weekday-sun"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-sun"));

			ps.setString(6, formJsonObject.getString("weekday-mon"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-mon"));

			ps.setString(7, formJsonObject.getString("weekday-tue"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-tue"));

			ps.setString(8, formJsonObject.getString("weekday-wed"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-wed"));

			ps.setString(9, formJsonObject.getString("weekday-thu"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-thu"));

			ps.setString(10, formJsonObject.getString("weekday-fri"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-fri"));

			ps.setString(11, formJsonObject.getString("weekday-sat"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-sat"));

			ps.setString(12, formJsonObject.getString("scheduler_run_hour"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_run_hour") + "'");

			ps.setString(13, formJsonObject.getString("scheduler_run_minute"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_run_minute") + "'");
			
			ps.setString(14, formJsonObject.getString("scheduler_enabled"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("scheduler_enabled"));
			
			ps.setString(15, formJsonObject.getString("scheduler_interval_n"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("scheduler_interval_n"));
			
			ps.setString(16, formJsonObject.getString("scheduler_prereq"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("scheduler_prereq"));
			
			System.out.println(sql);
			
			//ps.execute();
			
			ResultSet rs = ps.executeQuery();
			
			int currentId = -1;
			while (rs.next()) {
				currentId = rs.getInt("id");
			}
			
			if (currentId > 0) {
				// EMAIL RECIPIENTS INSERT
				if (formJsonObject.getString("scheduler_recipients").trim().length() > 0) {
					String[] recipientsArray = formJsonObject.getString("scheduler_recipients").trim().split(",");
					if (recipientsArray.length > 0) {
						for (String recipient : recipientsArray) {
							sql = "insert into dbo.scheduler_recipients ([scheduler_id], recipient) "
									+ " values (?, ?) ";
							ps = conn.prepareStatement(sql);
							ps.setInt(1, currentId);
							ps.setString(2, recipient.trim());
							ps.execute();
						}
					}
				}
				
				// INSERT EMAIL SUBJECT
				if (formJsonObject.getString("scheduler_subject").trim().length() > 0) {
					sql = "insert into dbo.scheduler_options (scheduler_id, option_name, option_value_1) values (?, ?, ?)";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, currentId);
					ps.setString(2, "EMAIL_SUBJECT");
					ps.setString(3, formJsonObject.getString("scheduler_subject").trim());
					ps.execute();
				}
				
				// DAYS OF MONTH INSERT
				if (formJsonObject.getString("scheduler_days_of_month_list").trim().length() > 0) {
					String[] daysOfMonthArray = formJsonObject.getString("scheduler_days_of_month_list").trim().split(",");
					if (daysOfMonthArray.length > 0) {
						for (String dayOfMonth : daysOfMonthArray) {
							sql = "insert into dbo.scheduler_days_of_month ([scheduler_id], day_of_month) "
									+ " values (?, ?) ";
							ps = conn.prepareStatement(sql);
							ps.setInt(1, currentId);
							ps.setString(2, dayOfMonth.trim());
							ps.execute();
						}
					}
				}
			}
			
			System.out.println("Current Id: " + currentId);
			
			return "ok";

		}

		public JSONObject schedulerInitialRetrieve() throws SQLException {

			JSONArray delivTypeJSONArr = new JSONArray();
			
			Connection conn = dataSource.getConnection();
			String sql = "select * "
					+ " from dbo.SCHEDULER_DELIVERY_TYPES "
					+ " order by delivery_type_id";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				JSONObject delivTypeJSONObj = new JSONObject();
				
				delivTypeJSONObj.put("delivery_type_id", rs.getInt("delivery_type_id"));
				delivTypeJSONObj.put("delivery_type", rs.getString("delivery_type"));
								
				delivTypeJSONArr.put(delivTypeJSONObj);
										
			}
			
			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());
			String url = "jdbc:db2://ibm3270:3021/NETMSK_DB2P";
			// Set URL for data source
			String user = propMap.get("idb_user");
			String password = propMap.get("idb_pswd");

			Connection con = DriverManager.getConnection(url, user, password);

			Statement stmt = con.createStatement();      
		
			rs = null;

			sql = "select distinct trim(avl_appl) avl_appl from idb.availability";
			
			JSONArray prereqJSONArray = new JSONArray();
			try {
				rs = stmt.executeQuery(sql);

				while (rs.next()) {
					
					JSONObject prereqJSONObj = new JSONObject();
					
					prereqJSONObj.put("prereq", rs.getString("avl_appl"));
									
					prereqJSONArray.put(prereqJSONObj);
											
				}
			
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
			
			JSONObject initialRetrieveJSONObj = new JSONObject();
			initialRetrieveJSONObj.put("delivery_types", delivTypeJSONArr);
			initialRetrieveJSONObj.put("prereqs", prereqJSONArray);

			return initialRetrieveJSONObj;
			
		}

		public String schedulerUpdateExistingJob(String formData) throws SQLException {
			JSONObject formJsonObject = new JSONObject(formData);
			Connection conn = null;
			conn = dataSource.getConnection();
			
			int currentId = Integer.valueOf(formJsonObject.getString("scheduler_id"));
			
			String sql = "update dbo.scheduler "
					+ "set [Database] = ?, " 
					+ "[Project_Code] = ?, "
					+ "[Delivery_type_id] = ?, "
					+ "[Interval] = ?, "
					+ "[Sunday] = ?, "
					+ "[Monday] = ?, "
					+ "[Tuesday] = ?, "
					+ "[Wednesday] = ?, "
					+ "[Thursday] = ?, "
					+ "[Friday] = ?, "
					+ "[Saturday] = ?, "
					+ "[Hour] = ?, "
					+ "[Minute] = ?, "
					+ "[Enabled] = ?, "
					+ "[Interval_n] = ?, "
					+ "[Prereq] = ? "
					+ " WHERE id = ?";
			
			System.out.println(sql);
			
			PreparedStatement ps = conn.prepareStatement(sql);
			
			ps.setString(1, formJsonObject.getString("scheduler_database"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_database") + "'");
			
			ps.setString(2, formJsonObject.getString("project_code"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("project_code") + "'");

			ps.setString(3, formJsonObject.getString("scheduler_delivery_type"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_delivery_type") + "'");

			ps.setString(4, formJsonObject.getString("scheduler_interval"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_interval") + "'");
			
			ps.setString(5, formJsonObject.getString("weekday-sun"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-sun"));

			ps.setString(6, formJsonObject.getString("weekday-mon"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-mon"));

			ps.setString(7, formJsonObject.getString("weekday-tue"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-tue"));

			ps.setString(8, formJsonObject.getString("weekday-wed"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-wed"));

			ps.setString(9, formJsonObject.getString("weekday-thu"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-thu"));

			ps.setString(10, formJsonObject.getString("weekday-fri"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-fri"));

			ps.setString(11, formJsonObject.getString("weekday-sat"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("weekday-sat"));

			ps.setString(12, formJsonObject.getString("scheduler_run_hour"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_run_hour") + "'");

			ps.setString(13, formJsonObject.getString("scheduler_run_minute"));
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_run_minute") + "'");
			
			ps.setString(14, formJsonObject.getString("scheduler_enabled"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("scheduler_enabled"));
			
			ps.setString(15, formJsonObject.getString("scheduler_interval_n"));
			sql = sql.replaceFirst("\\?", formJsonObject.getString("scheduler_interval_n"));
			
			if (formJsonObject.getString("scheduler_prereq").trim().equals("")) {
				ps.setNull(16, Types.VARCHAR);
			}
			else {
				ps.setString(16, formJsonObject.getString("scheduler_prereq"));
			}
			sql = sql.replaceFirst("\\?", "'" + formJsonObject.getString("scheduler_prereq") + "'");
			
			ps.setInt(17, currentId);
			sql = sql.replaceFirst("\\?", String.valueOf(currentId));
			
			System.out.println(sql);
			
			ps.execute();
			
			// EMAIL RECIPIENTS DELETE / INSERT
			sql = "delete from dbo.scheduler_recipients where [scheduler_id] = ? ";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, currentId);
			ps.execute();

			if (formJsonObject.getString("scheduler_recipients").trim().length() > 0) {
				String[] recipientsArray = formJsonObject.getString("scheduler_recipients").trim().split(",");
				if (recipientsArray.length > 0) {
					for (String recipient : recipientsArray) {
						sql = "insert into dbo.scheduler_recipients ([scheduler_id], recipient) "
								+ " values (?, ?) ";
						ps = conn.prepareStatement(sql);
						ps.setInt(1, currentId);
						ps.setString(2, recipient.trim());
						ps.execute();
					}
				}
			}
			
			// EMAIL SUBJECT DELETE / INSERT
			sql = "delete from dbo.scheduler_options where scheduler_id = ? and option_name = 'EMAIL_SUBJECT'";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, currentId);
			ps.execute();

			if (formJsonObject.getString("scheduler_subject").trim().length() > 0) {
				sql = "insert into dbo.scheduler_options (scheduler_id, option_name, option_value_1) values (?, ?, ?)";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, currentId);
				ps.setString(2, "EMAIL_SUBJECT");
				ps.setString(3, formJsonObject.getString("scheduler_subject").trim());
				ps.execute();
			}
			
			// DAYS OF MONTH INSERT
			sql = "delete from dbo.scheduler_days_of_month where [scheduler_id] = ? ";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, currentId);
			ps.execute();

			if (formJsonObject.getString("scheduler_days_of_month_list").trim().length() > 0) {
				String[] daysOfMonthArray = formJsonObject.getString("scheduler_days_of_month_list").trim().split(",");
				if (daysOfMonthArray.length > 0) {
					for (String dayOfMonth : daysOfMonthArray) {
						sql = "insert into dbo.scheduler_days_of_month ([scheduler_id], day_of_month) "
								+ " values (?, ?) ";
						ps = conn.prepareStatement(sql);
						ps.setInt(1, currentId);
						ps.setString(2, dayOfMonth.trim());
						ps.execute();
					}
				}
			}
			
			System.out.println("Current Id: " + currentId);
			
			return "ok";

		}

		public String deleteJob(String id) throws SQLException {
			String sql = "DELETE FROM DBO.SCHEDULER WHERE ID = ?";
			
			Connection conn = null;
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ps.execute();
			
			sql = "delete from dbo.scheduler_recipients where [scheduler_id] = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ps.execute();

			sql = "delete from dbo.scheduler_days_of_month where [scheduler_id] = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ps.execute();

			return "ok";
				
		}
		
		private String translateDB2SQLError(String errorMessage) {
			String errorText = errorMessage;
			
			if (errorText.contains("SQLCODE=-551")) {
				Pattern pErr = Pattern.compile("SQLERRMC=[\\w;\\.]+");
				Matcher mErr = pErr.matcher(errorText);
				
				if (mErr.find()) {
					String matchString = mErr.group().replace("SQLERRMC=", "");
					String[] s = matchString.split(";");
					errorText = "DB2 SQL Error: User " + s[0] + " lacks " + s[1] + " access to " + s[2];
				}
			}
			else if (errorText.contains("SQLCODE=-204")) {
				Pattern pErr = Pattern.compile("SQLERRMC=[\\w;\\.]+");
				Matcher mErr = pErr.matcher(errorText);
				
				if (mErr.find()) {
					String matchString = mErr.group().replace("SQLERRMC=", "");
					errorText = "DB2 SQL Error: Table " + matchString + " not found ";
				}
			}
			
			// Not actaully DB2 Error - Excel error
			else if (errorText.contains("Invalid row number (1048576) outside allowable range")) {
				errorText = "Too many rows for Excel. " + errorText + ".";
			}
			
			// Not actaully DB2 Error - Excel error			
			else if (errorText.contains("The maximum length of cell contents (text) is 32,767 characters")) {
				errorText = "Excel column character length exceeded. " + errorText + ".";
			}

			return errorText;
		}
		
		public String generateExcel(String formData) throws IOException, SQLException {
			
			System.out.println("in DataDao");
			
			JSONObject jsonObject = new JSONObject(formData);
			
			createProjectFolder(jsonObject.getString("folder_path"));
			
			// removes the last character after trimming whitespace if it's a semicolon
			if (jsonObject.getString("sql").trim().substring(jsonObject.getString("sql").trim().length()-1).equals(";")) {
				jsonObject.put("sql", jsonObject.getString("sql").trim().substring(0, jsonObject.getString("sql").trim().length()-1));
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH.mm.ss");
	        Date date = new Date();
	        Timestamp timestamp = new Timestamp(date.getTime());
			
			String FILE_NAME = "C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\excel\\" + jsonObject.getString("sn_code") + ".xlsx";
			String OUTPUT_FILE_NAME = jsonObject.getString("folder_path") + "\\DataLine Results - " + jsonObject.getString("sn_code") + " - " + sdf.format(timestamp) + ".xlsx";

			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());

			String user = "";
			String url = "";
			String password = "";
			
			if (jsonObject.getString("sql_database").equals("Darwin")) {
				System.out.println("switching to Darwin");
				url = "jdbc:db2://pidvudb1:51013/DVPDB01";
				user = propMap.get("darwin_apps_user");
				password = propMap.get("darwin_apps_pswd");
			}
			else {
				System.out.println("switching to IDB");
				url = "jdbc:db2://ibm3270:3021/NETMSK_DB2P";
				user = propMap.get("idb_user");
				password = propMap.get("idb_pswd");
			}
			
			Connection con = DriverManager.getConnection(url, user, password);

			// remove single-line comments
			String strippedSQL = jsonObject.getString("sql").replaceAll("--.*", "");
			
			// Check if name of each spreadsheet is specified in comments (non-greedy)
			// PYTHON regex = '\/\*\s*worksheet\s*:\s*([\w ]+)\s*\*\/'
			// https://stackoverflow.com/questions/13014947/regex-to-match-a-c-style-multiline-comment
			String pat = "/\\*+[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
			Pattern p = Pattern.compile(pat);
			
			Matcher m = p.matcher(strippedSQL);
			String regex = "(?i)/\\*\\s*worksheet\\s*:\\s*([\\w -\\.\\|;'\\&\\^%\\$#@!]+?)\\*/";
			
			while (m.find()) {
				String matchString = m.group();
				System.out.println("comment stripping: " + matchString);
				Pattern p0 = Pattern.compile(regex);
				Matcher m0 = p0.matcher(matchString);
				if (!m0.find()) {
					strippedSQL = strippedSQL.replace(matchString, "");
					System.out.println("removing: " + matchString);
				}
				else {
					System.out.println("following is legal WORKSHEET: format: " + matchString);
				}
			}
			
			String[] sqlArr = strippedSQL.split(Pattern.quote(jsonObject.getString("excel_delimiter")));
			
			boolean workbookCreated = false;
			boolean jsonCreated = false;
			XSSFWorkbook wb = null;
			int sheetCount = 1;
			//Connection con = null;
			Statement stmt = null;
			boolean changedConnection = false;
			SXSSFWorkbook workbook = null;
			
			for (String sql : sqlArr) {
				System.out.println("determining DB2 conn. jsonObject.getString(\"sql_database\") = " + jsonObject.getString("sql_database") + ". url: " + url);
				if (jsonObject.getString("sql_database").equals("Auto") && !url.equals("jdbc:db2://pidvudb1:51013/DVPDB01") && (sql.toUpperCase().contains("DV.") || sql.toUpperCase().contains("DVIDIC."))) {
					System.out.println("switching to Darwin");
					url = "jdbc:db2://pidvudb1:51013/DVPDB01";
					user = propMap.get("darwin_apps_user");
					password = propMap.get("darwin_apps_pswd");
					changedConnection = true;
				}
				else if (jsonObject.getString("sql_database").equals("Auto") && !url.equals("jdbc:db2://ibm3270:3021/NETMSK_DB2P") && !(sql.toUpperCase().contains("DV.") || sql.toUpperCase().contains("DVIDIC."))) {
					System.out.println("switching to IDB");
					url = "jdbc:db2://ibm3270:3021/NETMSK_DB2P";
					user = propMap.get("idb_user");
					password = propMap.get("idb_pswd");
					changedConnection = true;
				}

				if (changedConnection) {
					con = DriverManager.getConnection(url, user, password);
					changedConnection = false;
				}

				stmt = con.createStatement();      
				
				String sheetName = "Results " + sheetCount;
				
				p = Pattern.compile(regex);
				
				m = p.matcher(sql);
				System.out.println("Testing multi-sheet detection. Sql: " + sql);
				System.out.println("Testing multi-sheet detection. Number of groups: " + m.groupCount());
				if (m.find()) {
					System.out.println("Testing multi-sheet detection. Matcher: " + m.group(1));
					sheetName = m.group(1);
				}
				
				System.out.println("trying: " + sql + " as " + user);
				ResultSet rs = null;
				
				// try to run SQL from front-end
				try {
					if (sql.toLowerCase().contains("from") && (sql.toLowerCase().contains("insert into") || sql.toLowerCase().contains("declare global temporary table")))
						stmt.execute(sql);
					else if (sql.toLowerCase().contains("from"))
						rs = stmt.executeQuery(sql);
				}
				catch (Exception e) {
					String errorText = translateDB2SQLError(e.getMessage());
					System.out.println(e.toString());
					return "error: " + errorText + "<br><br>sql: <br><br>" + sql ;
				}

				StringBuilder jsonOutput = new StringBuilder("[");
				if (rs != null && rs.next()) {
					if (jsonObject.getString("sql_format").equals("Excel")) {
					
						if (!workbookCreated) {
							System.out.println("Temp location for Excel creation: " + System.getProperty("java.io.tmpdir"));
							//System.out.println("Now start excel creation: " + sdf.format(new Timestamp(new Date().getTime())));
							wb = new XSSFWorkbook();
							workbookCreated = true;
							
							XSSFSheet xSheet = wb.createSheet("Criteria");
							
							XSSFDrawing draw = xSheet.createDrawingPatriarch();
							
						    XSSFClientAnchor a = new XSSFClientAnchor( 0, 0, 0, 0, (short) 1, 1, (short) 20, 50 );

						    XSSFTextBox textbox1 = draw.createTextbox(a);
						    //textbox1.setText("Data Elements: \n\n" + jsonObject.getString("data_elements") + "\n" + "Criteria: \n\n" + jsonObject.getString("criteria"));
						    textbox1.setText(jsonObject.getString("program_specs"));
						    textbox1.setLineStyleColor(0, 0, 0);
						    textbox1.setLineWidth(1);
						    textbox1.setFillColor(255, 255, 255);
						    								
							xSheet = wb.createSheet("SQL");
							
							draw = xSheet.createDrawingPatriarch();
							
							a = new XSSFClientAnchor( 0, 0, 0, 0, (short) 1, 1, (short) 20, 50 );

						    textbox1 = draw.createTextbox(a);
						    textbox1.setText("SQL: \n\n" + jsonObject.getString("sql"));
						    textbox1.setLineStyleColor(0, 0, 0);
						    textbox1.setLineWidth(1);
						    textbox1.setFillColor(255, 255, 255);
						    
						    workbook = new SXSSFWorkbook(wb);

						}
	
						SXSSFSheet sheet = workbook.createSheet(sheetName);
						sheetName = sheet.getSheetName(); // In case name is truncated or has bad characters removed 
						//sheet.trackAllColumnsForAutoSizing();
						workbook.setSheetOrder(sheetName, sheetCount-1);
						XSSFCellStyle headerStyle = (XSSFCellStyle)workbook.createCellStyle();
						// XSSFColor color = new XSSFColor(new java.awt.Color(215,228,188));
						XSSFColor color = new XSSFColor(new java.awt.Color(197,215,241));
						//headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
						
						
						headerStyle.setFillForegroundColor(color);
						headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						
					    Font font= workbook.createFont();
					    font.setBold(true);
					    
					    CellStyle boldStyle = workbook.createCellStyle();
						boldStyle.setFont(font);
						
						headerStyle.setFont(font);
					    
						int rowNum = 0;
						
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
	
						// DataLine Summary
						Row row = sheet.createRow(rowNum++);
						Cell cell = row.createCell(0);
						int descLen = (jsonObject.getString("description").length() < 250 ? jsonObject.getString("description").length() : 250);
						String dots = (descLen != jsonObject.getString("description").length() ? "..." : "");
						cell.setCellValue(jsonObject.getString("description").substring(0, descLen) + dots);
						cell.setCellStyle(boldStyle);
						
						row = sheet.createRow(rowNum++);
						cell = row.createCell(0);
						cell.setCellValue("DataLine Report " + jsonObject.getString("sn_code"));
						cell.setCellStyle(boldStyle);
						
						row = sheet.createRow(rowNum++);
						cell = row.createCell(0);
						cell.setCellValue("Produced on " + new SimpleDateFormat("MMMMM d, yyyy").format(new java.util.Date()) + " by DataLine in Information Systems");
						
						row = sheet.createRow(rowNum++);
						cell = row.createCell(0);
						cell.setCellValue("See \"Criteria\" sheet for inclusion criteria");
						
						rowNum++;

						// Create Column Headers
						// Columns
						row = sheet.createRow(rowNum);
						//sheet.trackAllColumnsForAutoSizing();
						for (int i=1; i <= columnCount; i++) {
							cell = row.createCell(i-1);
							cell.setCellValue(rsmd.getColumnLabel(i));
							cell.setCellStyle(headerStyle);
							sheet.setColumnWidth(i-1, rsmd.getColumnLabel(i).length()*256 + (256*5));
							//sheet.autoSizeColumn(i);
						}
						
						//sheet.untrackAllColumnsForAutoSizing();
						
						rowNum++;
						
						// Loop through SQL ResultSet
						do {
							// protects against case where more than 1 million rows throws error
							try {
								row = sheet.createRow(rowNum++);
							}
							catch (Exception e) {
								String errorText = translateDB2SQLError(e.getMessage());
								System.out.println(e.toString());
								return "error: " + errorText + "<br><br>sql: <br><br>" + sql ;
							}
							
							int colNum = 0;
							for (int i=1; i <= columnCount; i++) {
								cell = row.createCell(colNum++);
								String s = rs.getString(i);
								if (rs.wasNull()) s = "";
								else s = s.trim();
								try {
									cell.setCellValue(s);
								}
								catch (Exception e) {
									String errorText = translateDB2SQLError(e.getMessage());
									System.out.println(e.toString());
									return "error: " + errorText + "<br><br>sql: <br><br>" + sql ;
								}
							}
							//fromAddress = rs.getString(1);
						} while (rs.next());
						
						//for (int i=1; i <= columnCount; i++) {
						//	sheet.autoSizeColumn(i);
						//}
						
					}
					else { // JSON
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
	
						// Loop through SQL ResultSet
						do {
							String row = "{";
							for (int i=1; i <= columnCount; i++) {
								String colName = rsmd.getColumnLabel(i);
								String s = rs.getString(i);
								
								if (rs.wasNull()) s = "";
								else s = s.trim();
								row += "\"" + colName + "\":\"" + s + "\",";
							}
							if (row.equals("{")) {
								row += "},\n";
							}
							else {
								row = row.substring(0, row.length() - 1) + "},\n";
							}
							jsonOutput.append(row);
						} while (rs.next());
						String fileOutput = null;
						if (jsonOutput.equals("[")) {
							fileOutput = "root:[]";
						}
						else {
							fileOutput = "\"root\": " + jsonOutput.substring(0, jsonOutput.length() - 1) + "]";
						}
						OUTPUT_FILE_NAME = jsonObject.getString("folder_path") + "\\" + sheetName + " - " + jsonObject.getString("sn_code") + " - " + sdf.format(timestamp) + ".json";
						try (FileWriter file = new FileWriter(OUTPUT_FILE_NAME)) {
							file.write(fileOutput);
							jsonCreated = true;
							//System.out.println("Successfully Copied JSON Object to File...");
							//System.out.println("\nJSON Object: " + root.toString());
						}
					}
				}
				if (rs != null)
					rs.close();
				sheetCount++;
			}
 
			if (workbookCreated) {
				workbook.setActiveSheet(0);
				workbook.setSelectedTab(0);
				FileOutputStream outputStream = new FileOutputStream(OUTPUT_FILE_NAME);
				
				workbook.write(outputStream);
				outputStream.close();
				
				//workbook.write(new FileOutputStream(FILE_NAME));
				
				workbook.dispose();
				//System.out.println("Now end excel creation: " + sdf.format(new Timestamp(new Date().getTime())));
				
				// TEST INSERTING A BLOB
				/*if (jsonObject.getString("sn_code").equals("IS17429")) {
					
					url = "jdbc:db2://tidvudb1:51005/DVDDB01";
					// Set URL for data source
					user = "singerm";
					password = "";

					con = DriverManager.getConnection(url, user, password);
					
					PreparedStatement pstmt = con.prepareStatement("insert into dvidic.test_of_blob (id, content) values (?, ?)");

					File blobFile = new File(FILE_NAME);
					InputStream in = new FileInputStream(blobFile);
					
					pstmt.setInt(1, 42);
					pstmt.setBinaryStream(2, in, (int)blobFile.length());
					pstmt.executeUpdate();
					con.commit();

				}*/
				
			}
			else if (jsonCreated) {
				
			}
			else {
				OUTPUT_FILE_NAME = "error";
			}

			stmt.close();

			con.close();
			
			System.out.println("Done");

			return OUTPUT_FILE_NAME;
		}

		public String projectCopy(String formData) throws SQLException {

			JSONObject jsonObject = new JSONObject(formData);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			jsonObject.put("received_date", dateFormat.format(date)); // set received date to current date

			// mapping html form id to database column name
			HashMap<String, String> colMap = new HashMap<String, String>();

			colMap.put("automation", "[Project ManAuto]");
			colMap.put("cross_ref", "[Project Code Cross Reference]");
			colMap.put("delivery_date", "[Project Delivery Date]");
			colMap.put("description", "[Project Description]");
			colMap.put("developer", "[Developer Primary]");
			colMap.put("irb_waiver", "[Project IRB/Waiver #]");
			colMap.put("objs_delivered", "ObjectsDelivered");
			//colMap.put("sn", "[Serial Number]");
			colMap.put("dev_notes", "DeveloperNotes");

			colMap.put("est_delivery_dte", "[Project Completion Date]");
			colMap.put("estimated_hours", "[Project Effort]");
			colMap.put("priority", "[Project Priority]");
			colMap.put("privacy_type", "[Project Request Type]");
			colMap.put("program_specs", "[Delivery Plan]");
			colMap.put("project_notes", "[Project Notes]");
			colMap.put("purpose_of_request", "[Request Purpose]");
			colMap.put("received_by", "[Project Req_rec By]");
			colMap.put("received_date", "[Project Request Date]");
			colMap.put("reqd_deliv_date", "[Project Req_delv Date]");
			colMap.put("request_description", "[Request Description]");
			colMap.put("request_type", "[Project Type]");
			colMap.put("requester", "Requester_ID");

			// colMap.put("requester_title", ""); this is shown on screen but a person's title is never changed in db

			colMap.put("schedule_status", "[schedule status]");
			colMap.put("second_developer", "[developer secondary]");
			colMap.put("sn_code", "[project code]");
			colMap.put("sql", "[project sql]");
			colMap.put("start_date", "[project req_rec date]");
			colMap.put("status", "[project completion status]");
			colMap.put("technical_specs", "[project tech specs]");
			colMap.put("test_plan", "[project test plan]");

			colMap.put("data_elements", "[data elements]");
			colMap.put("criteria", "criteria");

			Connection conn = null;
			conn = dataSource.getConnection();

			jsonObject.put("status", "");
			jsonObject.put("delivery_date", "");
			
			String projectDeptPrefix = jsonObject.getString("sn_code").trim().replaceAll("[^A-Za-z]","");;

			// loop through Project.colMap
			// keep a counter in order to bind with variables on INSERT 
			String sql1 = "INSERT INTO DBO.PROJECTS ([Serial Number], ";
			String sql2 = "values (?, ";
			for (String key : colMap.keySet()) {
				sql1 += colMap.get(key) + ", ";
				//sql2 += "'" + jsonObject.getString(key) + "', ";
				sql2 += "?, "; 
				//System.out.println("Key = " + key);
			}

			String sqlInsert = sql1.substring(0, sql1.length() - 2) + ") " + sql2.substring(0,  sql2.length() - 2) + ")";

			String sql = "select max([Serial Number]) + 1 as maxx from [dbo].[PROJECTS]";
			System.out.println(sql);
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			int serialNumber = 0;
			
			if (rs.next()) {
				serialNumber = rs.getInt("maxx");
				jsonObject.put("sn_code", projectDeptPrefix + serialNumber);
			}
			
			System.out.println(sqlInsert);
			ps = conn.prepareStatement(sqlInsert);
			ps.setInt(1, serialNumber);
			
			int c=2;
			for (String key : colMap.keySet()) {
				System.out.println("key: " + key);
				if (jsonObject.has(key)) {
					System.out.println("value: " + jsonObject.get(key));
					if (jsonObject.isNull(key) || jsonObject.getString(key).equals("")) {
						//System.out.println("set null");
						ps.setNull(c, Types.VARCHAR);
					}
					else {
						//System.out.println("set String");
						ps.setString(c, jsonObject.getString(key));
					}
				}
				c++;
			}

			ps.execute();
			
			//return "ok";
			return jsonObject.getString("sn_code");
		}

		public JSONArray getScheduleLog() throws SQLException {
			
			JSONArray arrayJSON = new JSONArray();
			
			Connection conn = dataSource.getConnection();
			String sql = "select id, project_code, start_time, end_time, datediff(second, start_time, end_time) duration, run_notes "
					+ "from dbo.scheduler_log "
					+ "where start_time >= DATEADD(day, -14, GETDATE()) "
					+ "order by start_time desc";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			System.out.println(sql);
			
			while (rs.next()) {
				
				JSONObject schedJSONObj = new JSONObject();
				
				schedJSONObj.put("id", rs.getInt("id"));
				schedJSONObj.put("project_code", rs.getString("project_code"));
				schedJSONObj.put("start_time", rs.getTimestamp("start_time"));
				schedJSONObj.put("end_time", rs.getTimestamp("end_time"));
				schedJSONObj.put("duration", rs.getInt("duration"));
				schedJSONObj.put("tun_notes", rs.getString("run_notes"));
								
				arrayJSON.put(schedJSONObj);

			}

			return arrayJSON;
		}

		public JSONObject refreshProjectFromR2d3(String projectCode) throws SQLException {

			Connection conn = dataSource.getConnection();
			String sql = "select * "
					+ "from projects "
					+ "join requestr on [requester id]=requester_id "
					+ "where [Serial Number]=? ";
			
			System.out.println(sql);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, Integer.valueOf(projectCode));
			
			int sn = 0;
			
			ResultSet rs = ps.executeQuery();
			
			JSONObject projJSONObj = new JSONObject();
			
			if (rs.next()) {
				
				sn = rs.getInt("serial number");
				
				projJSONObj.put("code", rs.getString("project code"));
				projJSONObj.put("serial_number", rs.getInt("serial number"));
				projJSONObj.put("request_type", rs.getString("project type"));
				projJSONObj.put("request_date", rs.getString("project request date"));
				projJSONObj.put("required_date", rs.getString("project req_delv date"));
				projJSONObj.put("est_delivery_dte", rs.getString("project completion date"));
				projJSONObj.put("developer_primary", rs.getString("developer primary"));
				projJSONObj.put("schedule_status", rs.getString("schedule status"));
				projJSONObj.put("priority", rs.getString("project priority"));

				projJSONObj.put("received_by", rs.getString("project req_rec by"));
				projJSONObj.put("start_date", rs.getString("project req_rec date"));
				projJSONObj.put("delivery_date", rs.getString("project delivery date"));
				projJSONObj.put("status", rs.getString("project completion status"));
				projJSONObj.put("objs_delivered", rs.getInt("objectsdelivered") == 0 ? "" : rs.getInt("objectsdelivered"));
				projJSONObj.put("second_developer", rs.getString("developer secondary"));
				projJSONObj.put("automation", rs.getString("project manauto"));
				projJSONObj.put("estimated_hours", rs.getString("project effort"));
				
				projJSONObj.put("requester_id", rs.getString("requester_id"));
				projJSONObj.put("requester_last_name", rs.getString("requester last name"));
				projJSONObj.put("requester_first_name", rs.getString("requester first name"));
				projJSONObj.put("requester_dept_name", rs.getString("department name"));
				projJSONObj.put("requester_title", rs.getString("requester title"));
				projJSONObj.put("requester_address", rs.getString("requester address"));
				projJSONObj.put("requester_email", rs.getString("email"));
				
				projJSONObj.put("request_description", rs.getString("request description"));
				projJSONObj.put("privacy_type", rs.getString("project request type"));
				projJSONObj.put("irb_waiver", rs.getString("project irb/waiver #"));
				projJSONObj.put("cross_ref", rs.getString("project code cross reference"));
				projJSONObj.put("description", rs.getString("project description"));
				projJSONObj.put("purpose_of_request", rs.getString("request purpose"));
				
				//System.out.println("ABOUT TO ADD DATA_ELEMENTS!!!");
				
				projJSONObj.put("program_specs", rs.getString("delivery plan"));
				projJSONObj.put("data_elements", rs.getString("data elements"));
				projJSONObj.put("criteria", rs.getString("criteria"));
				
				projJSONObj.put("technical_specs", rs.getString("project tech specs"));
				projJSONObj.put("test_plan", rs.getString("project test plan"));
				projJSONObj.put("project_notes", rs.getString("project notes"));
				projJSONObj.put("dev_notes", rs.getString("developernotes"));
				projJSONObj.put("sql", rs.getString("project sql"));

				//if (rs.wasNull()) 
				//	projJSONObj.put("required_date", rs.getString("project type"));
				//	if (rs.wasNull())
				//		projJSONObj.put("required_date", "");
				
				
				projJSONObj.put("description", rs.getString("project description"));
				projJSONObj.put("requester", rs.getString("requester first name") + " " + rs.getString("requester last name") );
				projJSONObj.put("email", rs.getString("email"));
				
				projJSONObj.put("department_name", rs.getString("department name"));
				if (rs.wasNull()) 
					projJSONObj.put("department_name", "");
				
				// if delivery date is null then
				//    put project status
				//    if project status is null then
				//       put "Open"
				
				projJSONObj.put("delivery_date", rs.getString("project delivery date"));
				if (rs.wasNull()) { 
					projJSONObj.put("status", rs.getString("project completion status"));
					if (rs.wasNull())
						projJSONObj.put("status", "Open");
				}
				else
					projJSONObj.put("status", "Delivered<br>" + rs.getString("project delivery date"));
								
			}
			
			return projJSONObj;
			
	}

		public String overrideCustodian(int sn, String custodian, String currentOverride) throws SQLException {
			 Connection conn = null;
			 conn = dataSource.getConnection();
			 
			 boolean setOverride = false;
			 if (currentOverride.equals("N")) {
				 setOverride = true;
			 }

			 String sql = "UPDATE DBO.PROJCUST_R2D4 SET OVERRIDE = ? "
					 + "WHERE [Serial Number] = ? and "
					 + "[Data Custodian] = ?";

			 System.out.println("sql: " + sql);

			 PreparedStatement ps = conn.prepareStatement(sql);

			 ps.setBoolean(1, setOverride);
			 ps.setInt(2, sn);
			 ps.setString(3, custodian);

			 ps.execute();

			 return String.valueOf(setOverride);
		}

		public String overrideRequester(int sn, String projectCode, String currentOverride) throws SQLException {
			boolean setOverride = false;
			if (currentOverride.equals("N")) {
				setOverride = true;
			}

			String sql = "";
			Connection conn = dataSource.getConnection();
			PreparedStatement ps = null;

			sql = "SELECT * FROM DBO.REQUESTER_APPROVALS_R2D4 WHERE \"Serial Number\" = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sn);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				sql = sql = "UPDATE DBO.REQUESTER_APPROVALS_R2D4 SET OVERRIDE = ?  WHERE [Serial Number] = ?";
				ps = conn.prepareStatement(sql);

				ps.setBoolean(1, setOverride);	
				ps.setInt(2, sn);					 
				ps.execute();
			}
			else {
				sql = "INSERT INTO DBO.REQUESTER_APPROVALS_R2D4 (\"Serial Number\", \"Project Code\", \"URL Code\", Override) "
						+ "VALUES "
						+ "(?, ?, ?, ?)";
				ps = conn.prepareStatement(sql);

				ps.setInt(1, sn);					 
				ps.setString(2, projectCode);	
				ps.setInt(3, sn);
				ps.setBoolean(4, setOverride);	
				ps.execute();
			}

			System.out.println("sql: " + sql);

			return String.valueOf(setOverride);
		}

		public String runJob(String id) throws SQLException {
			String sql = "insert into dbo.scheduler_run_now select * from dbo.scheduler s where s.id = ?";
			
			Connection conn = null;
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ps.execute();
			
			return "ok";
				
		}

		public String generateWord(String formData) throws InvalidFormatException, IOException {
			System.out.println("in generateWord DataDao");
			
			JSONObject jsonObject = new JSONObject(formData);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
			Date date = new Date();
			jsonObject.put("todayDate", dateFormat.format(date));
			
			createProjectFolder(jsonObject.getString("folder_path"));
						
			String OUTPUT_FILE_NAME = jsonObject.getString("folder_path") + "\\DataLine Plan - " + jsonObject.getString("sn_code") + ".docx";
			String TEMPLATE_FILE_NAME = "C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\word\\plan_template.docx";
			
			Path folderPath = Paths.get(OUTPUT_FILE_NAME);
			
			if (Files.exists(folderPath)) {
				dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				String NEW_FILE_NAME = jsonObject.getString("folder_path") + "\\DataLine Plan - " + jsonObject.getString("sn_code") + ".old." + dateFormat.format(date) + ".docx";
				Files.move(folderPath, folderPath.resolveSibling(NEW_FILE_NAME));
			}
			
			if (Files.notExists(folderPath)) {
				XWPFDocument doc = new XWPFDocument(OPCPackage.open(TEMPLATE_FILE_NAME));
				for (XWPFParagraph p : doc.getParagraphs()) {
				    List<XWPFRun> runs = p.getRuns();
				    if (runs != null) {
				        for (XWPFRun r : runs) {
				            //String text = r.getText(0);
				            String text = r.text();
				            //System.out.println("line by line word doc:" + text);
				            for (String key : jsonObject.keySet()) {
				            	//System.out.println("key: " + key);
					            if (text != null && text.equals(key)) {
					            	String val = (!jsonObject.isNull(key) ? jsonObject.get(key).toString() : "");
					            	text = text.replace(key, val);
					            	
					                if (text.contains("\n")) {
					                    String[] lines = text.split("\n");
					                    r.setText(lines[0], 0); // set first line into XWPFRun
					                    for(int i=1;i<lines.length;i++){
					                        // add break and insert new text
					                        r.addBreak();
					                        r.setText(lines[i]);
					                    }
					                } else {
					                    r.setText(text, 0);
					                }
	
					            	
					            	//System.out.println("HIT!!!!! key: " + key + ", text: " + val);
					                //text = text.replace(key, val);
					                //r.setText(text, 0);
					            }
				            }
				        }
				    }
				}
				for (XWPFTable tbl : doc.getTables()) {
				   for (XWPFTableRow row : tbl.getRows()) {
				      for (XWPFTableCell cell : row.getTableCells()) {
				         for (XWPFParagraph p : cell.getParagraphs()) {
				            for (XWPFRun r : p.getRuns()) {
				              String text = r.getText(0);
					            for (String key : jsonObject.keySet()) {
						            if (text != null && text.equals(key)) {
						            	String val = (!jsonObject.isNull(key) ? jsonObject.get(key).toString() : "");
						                text = text.replace(key, val);
						                r.setText(text, 0);
						            }
					            }
				            }
				         }
				      }
				   }
				}
				doc.write(new FileOutputStream(OUTPUT_FILE_NAME));
			
			}
			else {
				return "file exists";
			}
			
			return "ok";
		}

		// https://github.com/OfficeDev/ews-java-api/wiki/Getting-Started-Guide
		// https://docs.microsoft.com/en-us/previous-versions/office/developer/exchange-server-2010/dd633645(v%3Dexchg.80)
		public JSONArray getProjectEmails(String projectCode, String username, String developerEmail, String password) throws Exception {
			
			ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
			service.setUrl(new URI("https://mail.mskcc.org/ews/Exchange.asmx"));
			//service.autodiscoverUrl("singerm@mskcc.org");
			
			ExchangeCredentials credentials = new WebCredentials(developerEmail, password);
			service.setCredentials(credentials);
			
			ItemView view = new ItemView(50);
			view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending);
			view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived, EmailMessageSchema.Sender));
			
			
			System.out.println("projectCode: " + projectCode);
			FindItemsResults<Item> findResultsInbox = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.ContainsSubstring(ItemSchema.Subject, projectCode), view);
			FindItemsResults<Item> findResultsSent = service.findItems(WellKnownFolderName.SentItems, new SearchFilter.ContainsSubstring(ItemSchema.Subject, projectCode), view);
			    	//service.findItems(WellKnownFolderName.Inbox, new SearchFilter.ContainsSubstring(ItemSchema.Subject, projectCode), view);
			
	        JSONArray arrayJSON = new JSONArray();
        	ArrayList<EmailMessageMSK> messagesList = new ArrayList<EmailMessageMSK>();
        	
        	// SEARCH INBOX
	        try {
	        	service.loadPropertiesForItems(findResultsInbox, PropertySet.FirstClassProperties);

	        	for (Item item : findResultsInbox.getItems()) {
	        		String senderEmail = "";
	        		if (item instanceof EmailMessage) {
	        			senderEmail = ((EmailMessage) item).getSender().getAddress().toLowerCase().replace("@mskcc.org", ""); 
	        		}
	        		EmailMessageMSK emailMessage = new EmailMessageMSK(item.getId().toString(), "INBOX", item.getDateTimeReceived(), senderEmail, item.getSubject());
	        		messagesList.add(emailMessage);
	        	}
	        }
	        catch (Exception e) {
	        	System.out.println("No emails in INBOX");
	        	//e.printStackTrace();
	        }
		    
	        // SEARCH SENT
	        try {
	        	service.loadPropertiesForItems(findResultsSent, PropertySet.FirstClassProperties);
	        	
		        for (Item item : findResultsSent.getItems()) {
		        	String senderEmail = developerEmail.replace("@mskcc.org", "");
		        	EmailMessageMSK emailMessage = new EmailMessageMSK(item.getId().toString(), "SENT", item.getDateTimeReceived(), senderEmail, item.getSubject());
		        	messagesList.add(emailMessage);
		        }
	        }
	        catch (Exception e) {
	        	System.out.println("No emails in SENT");
	        	//e.printStackTrace();
	        }

	        // SEARCH OTHER SELECTED FOLDERS
			HashMap<String, Object> userPrefs = getAllUserPrefs(username);
			
			HashSet<String> email_folder_ids = new HashSet<String>();  
			if (userPrefs.containsKey("email_folder_ids")) {
				email_folder_ids = (HashSet<String>) userPrefs.get("email_folder_ids");
			}

	        for (String folderId : email_folder_ids) {
	        	//FolderId fi = new FolderId("AAMkAGY5ZTAzYzljLTQ5YzgtNGQ5OC04Yzc2LWZkZTM3NmYxMzY2NgAuAAAAAAC3P7486fuGT5a3QIL8LBTJAQB/+G+OeNksRZZYNPTVl/o3AAAx9G7IAAA=");
	        	System.out.println("folderId: " + folderId);
	        	FolderId fi = new FolderId(folderId);
	        	//Folder folder = service.bindToFolder(fi, new PropertySet(BasePropertySet.IdOnly, EmailMessageSchema.Sender, ItemSchema.Subject, ItemSchema.DateTimeReceived, FolderSchema.DisplayName));
	        	Folder folder = service.bindToFolder(fi, new PropertySet(FolderSchema.DisplayName));
	        	String d = folder.getDisplayName();
	        	System.out.println("checking folder: " + folder.getDisplayName() + ", " + fi.getUniqueId() + ", " + fi.getMailbox()+ ", isValid: " + fi.isValid());
				//FindItemsResults<Item> findResultsCustom = service.findItems(fi, new SearchFilter.ContainsSubstring(ItemSchema.Subject, projectCode), view);
	        	//folder.load(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived, FolderSchema.DisplayName, EmailMessageSchema.Sender));
	        	folder.load(new PropertySet(FolderSchema.DisplayName));
				//FindItemsResults<Item> findResultsCustom = folder.findItems(new SearchFilter.ContainsSubstring(ItemSchema.Subject, projectCode), view);
				FindItemsResults<Item> findResultsCustom = folder.findItems(new SearchFilter.ContainsSubstring(ItemSchema.Subject, projectCode), view);
				//FindItemsResults<Item> findResultsCustom = folder.findItems("System.Message.DateReceived:01/01/2016..01/31/2019", view);
			    try {

		        	//service.loadPropertiesForItems(findResultsCustom, PropertySet.FirstClassProperties);
					//folder.load(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived, FolderSchema.DisplayName));
		        	
			        for (Item item : findResultsCustom.getItems()) {
			        	String senderEmail = "";
		        		if (item instanceof EmailMessage) {
		        			item.load(PropertySet.FirstClassProperties);
		        			senderEmail = ((EmailMessage) item).getSender().getAddress().toLowerCase().replace("@mskcc.org", ""); 
		        			System.out.println("senderEmail: " + senderEmail);
		        			//String fromEmail = ((EmailMessage) item).getFrom().getAddress().toLowerCase().replace("@mskcc.org", ""); 
		        			//String toEmail = ((EmailMessage) item).getDisplayTo().toLowerCase().replace("@mskcc.org", ""); 
		        			//String toEmail = ((EmailMessage) item).h  .getDisplayTo().getAddress().toLowerCase().replace("@mskcc.org", ""); 
		        			
		        			//System.out.println(senderEmail + fromEmail + toEmail);
		        		}
			        	//String senderEmail = developerEmail.replace("@mskcc.org", "");
			        	EmailMessageMSK emailMessage = new EmailMessageMSK(item.getId().toString(), folder.getDisplayName(), item.getDateTimeReceived(), senderEmail, item.getSubject());
			        	messagesList.add(emailMessage);
			        }
		        }
		        catch (Exception e) {
		        	System.out.println("No emails in " + folderId);
		        	e.printStackTrace();
		        }
	        }
	        
	        // ADD MESSAGES FROM messagesList VAR INTO JSONARRAY TO BE RETURNED
	        try {
		        messagesList.sort((o1, o2) -> o2.messageDate.compareTo(o1.messageDate));
		        
		        for (EmailMessageMSK emailMessage : messagesList) {
		        	JSONObject objectJSON = new JSONObject();
		        	
		            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

		            //to convert Date to String, use format method of SimpleDateFormat class.
		            String strDate = dateFormat.format(emailMessage.messageDate);

		        	objectJSON.put("id", emailMessage.messageId);
		        	objectJSON.put("from", emailMessage.senderEmail);
		        	objectJSON.put("subject", emailMessage.subject);
		        	objectJSON.put("timestamp", emailMessage.messageDate);
		        	//objectJSON.put("body", item.getBody());
		        	
					// Do something with the item as shown
					System.out.println("id==========" + emailMessage.messageId);
					System.out.println("sub==========" + emailMessage.subject);
					
					arrayJSON.put(objectJSON);
		        }
		        
		        /*for (Item item : findResults.getItems()) {
		        	JSONObject objectJSON = new JSONObject();
		        	
		            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

		            //to convert Date to String, use format method of SimpleDateFormat class.
		            String strDate = dateFormat.format(item.getDateTimeReceived());

		        	objectJSON.put("id", item.getId().toString());
		        	String senderEmail = "";
		        	if (item instanceof EmailMessage) {
		        		senderEmail = ((EmailMessage) item).getSender().getAddress().toLowerCase().replace("@mskcc.org", ""); 
		        	}
		        	objectJSON.put("from", senderEmail);
		        	objectJSON.put("subject", item.getSubject());
		        	objectJSON.put("timestamp", strDate);
		        	//objectJSON.put("body", item.getBody());
		        	
					// Do something with the item as shown
					System.out.println("id==========" + item.getId());
					System.out.println("sub==========" + item.getSubject());
					
					arrayJSON.put(objectJSON);
				}*/
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	        }
			return arrayJSON;
		}

		public JSONObject getProjectEmailById(String emailId, String developerEmail, String password) throws Exception {
			ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
			service.setUrl(new URI("https://mail.mskcc.org/ews/Exchange.asmx"));

			ExchangeCredentials credentials = new WebCredentials(developerEmail, password);

			service.setCredentials(credentials);
			
			JSONObject objectJSON = new JSONObject();
			
			System.out.println("emailId: " + emailId);
			
			//JSONArray attachmentsArray = new JSONArray();
			
			String body = "";
			String from = "";
			String fromName = "";
			String to = "";
			String cc = "";
			String subject = "";
			String date = "";
			
			objectJSON.put("body", body);
			objectJSON.put("from", from);
			objectJSON.put("fromName", fromName);
			objectJSON.put("to", to);
			objectJSON.put("cc", cc);
			objectJSON.put("subject", subject);
			objectJSON.put("date", date);
			objectJSON.put("hasAttachment", 0);
			
			//objectJSON.put("attachmentsArray", attachmentsArray);

			try {
			
				EmailMessage message = EmailMessage.bind(service, new ItemId(emailId));
				
				body = message.getBody().toString().replace("’", "'").replace("‘", "'").replace("–", "-");
				from = message.getFrom().getAddress().toLowerCase();
				fromName = message.getFrom().getName();
				subject = message.getSubject().toString();
	            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	            date = dateFormat.format(message.getDateTimeReceived());
				
				List<EmailAddress> toAddresses = message.getToRecipients().getItems();
				for (EmailAddress toAddress : toAddresses) {
					to += toAddress.getAddress() + ",";
				}
				
				List<EmailAddress> ccAddresses = message.getCcRecipients().getItems();
				for (EmailAddress ccAddress : ccAddresses) {
					cc += ccAddress.getAddress() + ",";
				}

				if (message.getHasAttachments()) { 
					objectJSON.put("hasAttachment", 1);
					/*
					AttachmentCollection attachmentsCol = message.getAttachments(); 
					System.out.println("attachments: " + attachmentsCol.getCount());
					for (int i = 0; i < attachmentsCol.getCount(); i++) { 
						FileAttachment attachment = (FileAttachment)attachmentsCol.getPropertyAtIndex(i); 
						attachment.load();
						JSONObject attachmentObject = new JSONObject();
						System.out.println("File attachment name: " + attachment.getName());
						System.out.println("File attachment contentType: " + attachment.getContentType());
						
						attachmentObject.put("attachmentName", attachment.getName());
						attachmentObject.put("attachmentContentType", attachment.getContentType());
						attachmentObject.put("attachmentContent", attachment.getContent());
						//attachmentsArray.put(attachmentObject);
					} */
				} 

				System.out.println("email body: " + body);
				objectJSON.put("body", body);
				objectJSON.put("from", from);
				objectJSON.put("fromName", fromName);
				objectJSON.put("to", to);
				objectJSON.put("cc", cc);
				objectJSON.put("subject", subject);
				objectJSON.put("date", date);
								
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			service.close();
			
			return objectJSON;
		}
		
		public String copyEmailById(String emailId, String folder, String developerEmail, String password) throws Exception {
			
			JSONObject objectJSON = getProjectEmailById(emailId, developerEmail, password);
			
		    Properties props = System.getProperties();

		    props.put("mail.mime.splitlongparameters", false);

		    Session session = Session.getInstance(props, null);
			
			createProjectFolder(folder);
			
	        //Message message = new MimeMessage(Session.getInstance(System.getProperties()));
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(objectJSON.getString("from"), objectJSON.getString("fromName")));
	        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(objectJSON.getString("to")));
	        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(objectJSON.getString("cc")));
	        message.setSubject(objectJSON.getString("subject"));
	        message.setSentDate(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").parse(objectJSON.getString("date")));
	        //message.setHe
	        // create the message part 
	        Multipart multipart = new MimeMultipart("mixed");
	        MimeBodyPart content = new MimeBodyPart();
	        // fill message
			if (objectJSON.getString("body").toLowerCase().contains("html")) {
				//content.setContent(content, "text/html; charset=utf-8");
				content.setContent( objectJSON.getString("body"), "text/html; charset=utf-8" );
				//content.setContent(multipart);
			}
			else {
				content.setText(objectJSON.getString("body"), "utf-8");
			}

			multipart.addBodyPart(content);
			
			//for (int i = 0; i < objectJSON.getJSONArray("attachmentsArray").length(); i++) {
			//	JSONObject attachmentObj = objectJSON.getJSONArray("attachmentsArray").getJSONObject(i);
			//	String filename = attachmentObj.getString("attachmentName");
			//	String contentType = attachmentObj.getString("attachmentContentType");
				//FileAttachment attachment = (FileAttachment)attachmentObj.get("attachment");
				
				
			//	javax.activation.DataSource attachmentDS = new  ByteArrayDataSource((byte[])attachmentObj.get("attachmentContent"), contentType);
			//	MimeBodyPart attachmentPart = new MimeBodyPart();
			//	attachmentPart.setDataHandler(new DataHandler(attachmentDS));
			//	attachmentPart.setHeader("Content-Transfer-Encoding", "8bit");
			//	attachmentPart.setHeader("Content-Type", attachmentDS.getContentType() + "; " + attachmentDS.getName());
				
			//	System.out.println("attachment being copied: " + filename + ", content type1: " + contentType + "2, :" + attachmentDS.getContentType());

				if (objectJSON.getInt("hasAttachment") == 1) {
					
					ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
					
					service.setUrl(new URI("https://mail.mskcc.org/ews/Exchange.asmx"));

					ExchangeCredentials credentials = new WebCredentials(developerEmail, password);

					service.setCredentials(credentials);
					
					try {
						
						EmailMessage messageWithAttachment = EmailMessage.bind(service, new ItemId(emailId));
												
							AttachmentCollection attachmentsCol = messageWithAttachment.getAttachments(); 
							System.out.println("attachments: " + attachmentsCol.getCount());
							for (int i = 0; i < attachmentsCol.getCount(); i++) { 
								FileAttachment attachment = (FileAttachment)attachmentsCol.getPropertyAtIndex(i); 

				                if (attachment instanceof FileAttachment || attachment.getIsInline()) {
				                	System.out.println(attachment.getName());
									String FILE_NAME = "C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\email_attachments\\" + attachment.getName();
									attachment.load(FILE_NAME);

						            MimeBodyPart attachmentMime = new MimeBodyPart();
						            attachmentMime.setContent(new MimeMultipart(attachment.getContentType()));
						            javax.activation.DataSource source = new FileDataSource(FILE_NAME);
						            //attachmentMime.setContent(attachment.getContentType());
						            attachmentMime.setDataHandler(new DataHandler(source));
						            attachmentMime.setFileName(attachment.getName());
						            //attachmentMime.setFileName(MimeUtility.encodeText(attachment.getName()));
						            //attachmentMime.setFileName(new String(attachment.getName().getBytes("GBK"), "ISO-8859-1"));
						            multipart.addBodyPart(attachmentMime);

				                } /*else if (attachment instanceof ItemAttachment) { //ItemAttachment - Represents an Exchange item that is attached to another Exchange item.

				                    attachments.putAll(extractItemAttachments(service, attachment, properties, appendedBody));
				                }*/

							}
										
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
					service.close();
					
				}
				
				//InternetHeaders headers = new InternetHeaders();
				//headers.addHeader("Content-Type", attachment.getContentType());
				//MimeBodyPart messageBodyPart = new MimeBodyPart(headers, attachment.getContent()); //new ByteArrayDataSource(attachment.getContent(), attachment.getContentType())
				//attachmentPart.setFileName(filename);
				//multipart.addBodyPart(attachmentPart);

	        // integration
			message.setContent(multipart);
	        message.saveChanges();

	        // store file
	        //message.writeTo(new FileOutputStream(new File("c:/mail.eml")));
	        //System.out.println("to: " + objectJSON.getString("to"));
	        //String path = folder + "\\" + objectJSON.getString("from").replace("@mskcc.org", "") + " - " + objectJSON.getString("subject").replace(":", "") + " - " + objectJSON.getString("date").replace(":", ".") + ".eml";
	        
	        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").parse(objectJSON.getString("date"));
	        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
	        String filename = folder + "\\" + objectJSON.getString("from").replace("@mskcc.org", "").replaceAll("[^ a-zA-Z0-9\\.\\-]", "") + " - " + objectJSON.getString("subject").replaceAll("[^ a-zA-Z0-9\\.\\-]", "") + " - " + dateStr + ".eml";
	        //System.out.println("file location: " + folder + "\\" + objectJSON.getString("subject") + " - " + objectJSON.getString("from").replace("@mskcc.org", "") + ".eml");
	        System.out.println("date: " + objectJSON.getString("date"));
	        message.writeTo(new FileOutputStream(new File(filename)));
	        	        
	        return "ok";
		}

		public String copyAllEmails(String folder, JSONArray emailsArrayJSON, String developerEmail, String password) throws JSONException, Exception {
			for (int i = 0; i < emailsArrayJSON.length(); i++) {
				JSONObject email = emailsArrayJSON.getJSONObject(i);
				copyEmailById(email.getString("id"), folder, developerEmail, password);
			}
			// TODO Auto-generated method stub
			return "ok";
		}

		// https://github.com/OfficeDev/ews-java-api/wiki/Getting-Started-Guide
		// https://docs.microsoft.com/en-us/previous-versions/office/developer/exchange-server-2010/dd633645(v%3Dexchg.80)
		public JSONArray getUserEmailFolders(String developerEmail, String password, HashSet<String> email_folder_ids) throws Exception {
			JSONArray jsonArray = new JSONArray();
			List<JSONObject> jsonArrayAsList = new ArrayList<JSONObject>();

			ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
			service.setUrl(new URI("https://mail.mskcc.org/ews/Exchange.asmx"));
			
			ExchangeCredentials credentials = new WebCredentials(developerEmail, password);
			service.setCredentials(credentials);
			
			FolderView view = new FolderView(1000);
			view.setPropertySet(new PropertySet(FolderSchema.DisplayName,FolderSchema.Id,FolderSchema.ParentFolderId));
			view.setTraversal(FolderTraversal.Deep);
			//SearchFilter searchFilter = new SearchFilter.IsGreaterThan(FolderSchema.TotalCount, -1);
			FindFoldersResults res = service.findFolders(WellKnownFolderName.MsgFolderRoot, view);
			List<Folder> folders = res.getFolders();
			
			System.out.println("email_folder_ids: " + email_folder_ids.toString());
			
			List<String> ignoredFolders = Arrays.asList("Calendar", "Contacts", "Conversation Action Settings", "Conversation History", "Deleted Items", 
					"Drafts", "ExternalContacts", "Files", "Inbox", "Journal", "Junk E-Mail", "Notes", "Outbox", "Quick Step Settings", "RSS Feeds", "Sent Items", "Tasks", "Yammer Root",
					"Companies", "GAL Contacts", "Organizational Contacts", "Feeds", "Inbound", "Outbound", "PeopleCentricConversation Buddies", "Recipient Cache", "Skype for Business Contacts");
			int c=0;
			for (Folder f : folders) {
				if (!ignoredFolders.contains(f.getDisplayName()) && !f.getDisplayName().substring(0, 1).equals("{")) {
					JSONObject jsonObject = new JSONObject();
					String folderName = f.getDisplayName();
					jsonObject.put("folder_search", (email_folder_ids.contains(f.getId().toString()) ? "Y" : "N"));
					jsonObject.put("folder_id", f.getId());
					jsonObject.put("folder_num", c++);
					
					String parentFolderName = "";
					do {
			        	FolderId parentFolderId = new FolderId(f.getParentFolderId().toString());
			        	Folder parentFolder = service.bindToFolder(parentFolderId, new PropertySet(FolderSchema.DisplayName, FolderSchema.ParentFolderId));
			        	parentFolderName = parentFolder.getDisplayName();
			        	if (!parentFolderName.equals("Top of Information Store")) {
			        		folderName = parentFolderName + "/" + folderName;
			        	}
						System.out.println("curr folder: " + f.getDisplayName() + ", parentFolder: " + parentFolder.getDisplayName());
						f = parentFolder;
					} while (!parentFolderName.equals("Top of Information Store"));

					jsonObject.put("folder_name", folderName);
					
					//System.out.println(f.getDisplayName());
					//jsonArray.put(jsonObject);
					jsonArrayAsList.add(jsonObject);
				}
			}
			
			Collections.sort(jsonArrayAsList, new Comparator<JSONObject>() {
			    @Override
			    public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
			        int compare = 0;
			        try
			        {
			            String folderA = jsonObjectA.getString("folder_name");
			            String folderB = jsonObjectB.getString("folder_name");
			            compare = folderA.compareTo(folderB);
			        }
			        catch(JSONException e)
			        {
			            e.printStackTrace();
			        }
			        return compare;
			    }
			});
			
			for (int i = 0; i < jsonArrayAsList.size(); i++) {
			    jsonArray.put(jsonArrayAsList.get(i));
			}
			
			service.close();
			
			return jsonArray;
			
		}
		
		public HashMap<String, Object> getAllUserPrefs(String username) throws SQLException {
			
			HashMap<String, Object> userPrefs = new HashMap<String, Object>();
			
			Connection conn = dataSource.getConnection();
			String sql = "select pref_name, pref_value_1, pref_value_2, pref_mod_date "
					+ "from dbo.user_prefs "
					+ "where pref_username = ? " 
					+ "order by pref_name desc";
			PreparedStatement ps = conn.prepareStatement(sql);
			System.out.println(sql);
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			
			ResultSet rs = ps.executeQuery();
			
			HashSet<String> email_folder_ids = new HashSet<String>(); 
			
			// Loop through prefs and put them in a hashmap
			while (rs.next()) {
				String pref_name = rs.getString("pref_name").toLowerCase();
				String pref_value_1 = rs.getString("pref_value_1");
				String pref_value_2 = rs.getString("pref_value_2");
				// Values with multiple values (i.e. email_folders to be checked for messages)
				if (pref_name.equals("email_folder")) {
					if (!userPrefs.containsKey(pref_name)) {
						JSONArray jsonArray = new JSONArray();
						userPrefs.put(pref_name, jsonArray);
					}
					JSONArray jsonArray = (JSONArray) userPrefs.get(pref_name);
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("pref_value_1", pref_value_1);
					jsonObject.put("pref_value_2", pref_value_2);
					//jsonObject.put("pref_mod_date", pref_value_2);
					System.out.println("Appending existing preference: " + pref_name + ", pref_value_1: " + pref_value_1 + ", pref_value_2: " + pref_value_2);
					jsonArray.put(jsonObject);
					userPrefs.put(pref_name, jsonArray);
				}
				// Values with single values (i.e. message text for Request Received email
				else {
					if (!userPrefs.containsKey(pref_name)) {
						JSONObject jsnObject = new JSONObject();
						userPrefs.put(pref_name, jsnObject);
					}
					JSONObject jsonObject = (JSONObject) userPrefs.get(pref_name);
					jsonObject.put("pref_value_1", pref_value_1);
					jsonObject.put("pref_value_2", pref_value_2);
					//jsonObject.put("pref_mod_date", pref_value_2);
					System.out.println("Appending existing preference: " + pref_name + ", pref_value_1: " + pref_value_1 + ", pref_value_2: " + pref_value_2);
					userPrefs.put(pref_name, jsonObject);
				}
				if (pref_name.equals("email_folder"))
					email_folder_ids.add(pref_value_1);
			}
			
			userPrefs.put("email_folder_ids", email_folder_ids);
			
			return userPrefs;
		}
		
		public JSONObject initialUserPrefsRetrieve(String username, String developerEmail, String password) throws Exception {
			JSONObject jsonUserPrefs = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			
			HashMap<String, Object> userPrefs = getAllUserPrefs(username);
			
			HashSet<String> email_folder_ids = new HashSet<String>();  
			
			for (String prefKey : userPrefs.keySet()) {
				if (prefKey.equals("email_folder_ids")) {
					email_folder_ids = (HashSet<String>) userPrefs.get("email_folder_ids");
					JSONArray emailFolders = getUserEmailFolders(developerEmail, password, email_folder_ids);
					jsonUserPrefs.put("emailFolders", emailFolders);
				}
				else {
					jsonUserPrefs.put(prefKey, userPrefs.get(prefKey));
				}
			}
			
			return jsonUserPrefs;
		}

		public String updateUserPrefs(String formData, String username) throws SQLException {
			JSONObject formJsonObject = new JSONObject(formData);
			
			System.out.println(formJsonObject.getString("email_folders"));
			
			JSONArray jsonArray = new JSONArray(formJsonObject.getString("email_folders"));
		
			String emailFoldersSqlString = formJsonObject.getString("email_folders_sql_string");
			
			// DELETE EMAIL FOLDERS FROM PREFERENCES
			
			String deleteEmailFolders = "DELETE FROM dbo.USER_PREFS where PREF_USERNAME = ? AND PREF_NAME = 'email_folder'";
			
			if (emailFoldersSqlString.length() > 0) {
				deleteEmailFolders += " and PREF_VALUE_2 not in (" + emailFoldersSqlString + ")";
			}
			
			System.out.println(deleteEmailFolders);
			
			Connection conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(deleteEmailFolders);
			
			ps.setString(1, username);

			ps.execute();


			// INSERT EMAIL FOLDERS
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject email_folder = jsonArray.getJSONObject(i);
				String sql = "INSERT INTO DBO.USER_PREFS (PREF_USERNAME, PREF_NAME, PREF_VALUE_1, PREF_VALUE_2) "
						 + "SELECT ?, 'email_folder', ?, ?  "
						 + "EXCEPT " // SAME AS SET MINUS 
						 + "SELECT PREF_USERNAME, PREF_NAME, PREF_VALUE_1, PREF_VALUE_2 "
						 + "FROM DBO.USER_PREFS WHERE PREF_USERNAME = ? AND PREF_NAME = 'email_folder' "
						 ;
				
				System.out.println("sql: " + sql + ", PREF_USERNAME: " + username + ", PREF_VALUE_1: " + email_folder.getString("id") + ", PREF_VALUE_2: " + email_folder.getString("name"));

				ps = conn.prepareStatement(sql);

				ps.setString(1, username);
				ps.setString(2, email_folder.getString("id"));
				ps.setString(3, email_folder.getString("name"));
				ps.setString(4, username);

				ps.execute();

			}
			
			// LOOP THROUGH DIFFERENT MESSAGE TYPES
			// IF BLANK, DELETE FROM DATABASE
			// IF VALUED, INSERT ... WHERE NOT EXISTS - THEN UPDATE
			
			ArrayList<String> emailMessageTypes = new ArrayList<String>(
				    Arrays.asList("request_received_message", "first_contact_message", "requester_message", "custodians_message", "requester_and_custodians_message", "delivery_message"));
			
			for (String messageType : emailMessageTypes) {
				if (formJsonObject.getString(messageType).equals("")) {
					String sql = "DELETE FROM dbo.USER_PREFS where PREF_USERNAME = ? AND PREF_NAME = ? ";
					
					System.out.println(sql);
					
					conn = dataSource.getConnection();
					ps = conn.prepareStatement(sql);
					
					ps.setString(1, username);
					ps.setString(2, messageType);
	
					ps.execute();
				}
				else {
					String sql = "INSERT INTO DBO.USER_PREFS (PREF_USERNAME, PREF_NAME, PREF_VALUE_1) "
							 + "SELECT ?, ?, ? "
							 + "WHERE NOT EXISTS ( "
								 + "SELECT PREF_USERNAME "
								 + "FROM DBO.USER_PREFS WHERE PREF_USERNAME = ? AND PREF_NAME = ? "
							 + ")"
							 ;
					
					System.out.println("sql: " + sql + ", PREF_USERNAME: " + username + ", PREF_VALUE_1: " + formJsonObject.getString(messageType));
	
					ps = conn.prepareStatement(sql);
	
					ps.setString(1, username);
					ps.setString(2, messageType);
					ps.setString(3, formJsonObject.getString(messageType));
					ps.setString(4, username);
					ps.setString(5, messageType);
	
					ps.execute();
					
					sql = "UPDATE DBO.USER_PREFS SET PREF_VALUE_1 = ? WHERE PREF_USERNAME = ? AND PREF_NAME = ? ";
					
					System.out.println("sql: " + sql + ", PREF_USERNAME: " + username + ", PREF_VALUE_1: " + formJsonObject.getString(messageType));
	
					ps = conn.prepareStatement(sql);
	
					ps.setString(1, formJsonObject.getString(messageType));
					ps.setString(2, username);
					ps.setString(3, messageType);
	
					ps.execute();
				}
			}

			ps.close();
			
			return "ok";
		}

		public String addRequester(String add_requester_email) throws SQLException {
			
			add_requester_email = add_requester_email.trim().toLowerCase();
			
			String sql = "select " +
				"'{\"employeeid\":\"' || dv.ldapquery('" + add_requester_email + "', 'mail', 'employeeID') || " +
				"'\",\"department name\":\"' ||dv.ldapquery('" + add_requester_email + "', 'mail', 'department') || " +
				"'\",\"ldap department\":\"' ||dv.ldapquery('" + add_requester_email + "', 'mail', 'department') || " +
				"'\",\"requester title\":\"' ||dv.ldapquery('" + add_requester_email + "', 'mail', 'title') || " +
				"'\",\"requester first name\":\"' ||dv.ldapquery('" + add_requester_email + "', 'mail', 'givenname') || " + 
				"'\",\"requester last name\":\"' ||dv.ldapquery('" + add_requester_email + "', 'mail', 'sn') || '\"}' json_data " + 
			"from sysibm.sysdummy1";
			
			System.out.println(sql);
			
			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());

			String url = "jdbc:db2://pidvudb1:51013/DVPDB01";
			String user = propMap.get("darwin_apps_user");
			String password = propMap.get("darwin_apps_pswd");
			
			Connection con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(true);

			Statement stmt = con.createStatement();                                            

			ResultSet rs = stmt.executeQuery(sql);
			JSONObject jsonObject = null;
			if (rs != null && rs.next()) {
				// Loop through SQL ResultSet
				do {
					jsonObject = new JSONObject(rs.getString(1));
				} while (rs.next());
			}
			
			jsonObject.put("email", add_requester_email);
			
			con.close();
			
			/*INSERT Competitors (cName)
			SELECT DISTINCT Name
			FROM CompResults cr
			WHERE
			   NOT EXISTS (SELECT * FROM Competitors c
			              WHERE cr.Name = c.cName)*/

			
			//sql = "INSERT INTO DBO.REQUESTR ([Requester ID], [Requester Last Name], [Requester First Name], [Department Name], [Requester Title], [Email], [LDAP Department], [EmployeeID]) "
			//		 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			sql = "INSERT INTO DBO.REQUESTR (/*[Requester ID],*/ [?], [?], [?], [?], [?], [?], [?]) "
					+ "SELECT /*(select max([Requester ID])+1 from [dbo].[REQUESTR]) req_id,*/ ?, ?, ?, ?, ?, ?, ? "
					+ "WHERE NOT EXISTS ("
					+ "	SELECT [Requester ID] FROM DBO.REQUESTR WHERE LOWER(EMAIL) = ? "
					+ " UNION "
					+ " SELECT [Requester ID] FROM DBO.REQUESTR WHERE EMPLOYEEID = ? "
					+ ") ";
			
			// TO GENERATE THE COLUMN NAMES AND VALUES IN THE SAME ORDER
			for (String key: jsonObject.keySet()){
				sql = sql.replaceFirst("\\?", key);
			}			
			
			Connection conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			
			System.out.println("sql: " + sql);
			
			String sql_for_print = sql;
			int c=1;
			for (String key: jsonObject.keySet()){
			    System.out.println("key: " + key + ", value: " + jsonObject.getString(key));
				if (jsonObject.isNull(key) || jsonObject.getString(key).equals("")) {
				    sql_for_print = sql_for_print.replaceFirst("\\?", "NULL");
					ps.setNull(c++, Types.VARCHAR);
				}
				else {
				    sql_for_print = sql_for_print.replaceFirst("\\?", "'" + jsonObject.getString(key) + "'");
				    ps.setString(c++, jsonObject.getString(key));
				}
			}
			
			sql_for_print = sql_for_print.replaceFirst("\\?", "'" + jsonObject.getString("email") + "'");
			sql_for_print = sql_for_print.replaceFirst("\\?", "'" + jsonObject.getString("employeeid") + "'");
			
			System.out.println(sql_for_print);
			
			ps.setString(c++, jsonObject.getString("email"));
			ps.setString(c++, jsonObject.getString("employeeid"));
			
			ps.execute();
			
			// REQUESTER
			sql = "select \"requester id\", \"requester last name\", \"requester first name\", isnull(\"department name\", '') dept_name, isnull(\"requester title\", '') title "
					+ "from dbo.requestr "
					+ "where email=? "
					+ "order by 1 desc";
			
			System.out.println(sql);
			ps = conn.prepareStatement(sql);
			ps.setString(1, jsonObject.getString("email"));
			rs = ps.executeQuery();

			JSONObject requesterInfo = new JSONObject();
			requesterInfo.put("requester_id", "-1");

			if (rs.next()) {				
				String requesterHTML = "<option value='" + rs.getString("requester id") + "'>" + rs.getString("requester last name") + ", " + rs.getString("requester first name") + " | " + rs.getString("title")  + " | " + rs.getString("dept_name") + "</option>";
				requesterInfo.put("requester_id", rs.getString("requester id"));
				requesterInfo.put("requester_title", rs.getString("title"));
				requesterInfo.put("requesterHTML", requesterHTML);
			}
			
			conn.close();
			
			return requesterInfo.toString();
		}

		public String sendOutlookUserPrefs(String username, String message_type) throws SQLException {
			HashMap<String, Object> userPrefs = getAllUserPrefs(username);
			
			if (userPrefs.containsKey(message_type + "_message")) {
				JSONObject prefJsonObject = (JSONObject) userPrefs.get(message_type + "_message");
				return (String) prefJsonObject.getString("pref_value_1").replace(System.getProperty("line.separator"), "%0D%0A").replace("\r\n", "%0D%0A").replace("\n", "%0D%0A");		
			}
			return "";
		}

}
