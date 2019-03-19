package com.dataline.controller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.dataline.delegate.LoginDelegate;
import com.dataline.loginbean.LoginBean;
import com.dataline.model.Project;
import com.dataline.service.ApprovalService;
import com.dataline.service.ProjectService;
import com.dataline.service.UserService;

@Controller
public class DatalineController {
	
	@Autowired
	private LoginDelegate loginDelegate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	ApprovalService approvalService;
	
	int timeout = 60 * 120; // 2 hours
	
	//int timeout = 60 * 1; // 1 minute (for testing)
	
	public Boolean checkSessionCsrf(HttpServletRequest request, HttpServletResponse response, String csrf_val) throws IOException {
		//System.out.println(request.getSession().getAttribute("csrf_val") + ", " + csrf_val + !csrf_val.equals(request.getSession().getAttribute("csrf_val").toString()));
		
		if (request.getSession().getAttribute("csrf_val") == null || (Boolean)request.getSession().getAttribute("timeout") || csrf_val == null || !csrf_val.equals(request.getSession().getAttribute("csrf_val").toString())) {
			System.out.print("SESSION FAILURE - CSRF. ");
			if (request.getSession().getAttribute("csrf_val") == null) {
				System.out.println("session variables null");
			}
			else if ((Boolean)request.getSession().getAttribute("timeout")) {
				System.out.println("timeout session variable had been set");
			}
			else if (csrf_val == null) {
				System.out.println("csrf value from html wasn't set");
			}
			else if (!csrf_val.equals(request.getSession().getAttribute("csrf_val").toString())) {
				System.out.println("csrf values weren't equal. csrf_val: " + csrf_val + ", session: " + request.getSession().getAttribute("csrf_val").toString());
			}
			request.getSession().setAttribute("timeout", true);
			return false;
		}
		
		return checkSession(request, response);
	}

	
	public Boolean checkSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Boolean b = request.isRequestedSessionIdFromCookie();
		System.out.println("Cookie: " + b.toString());
		Date lastAccessed = new Date(request.getSession().getLastAccessedTime());
		Date now = new Date(System.currentTimeMillis());
		System.out.println("checkSession. Last Accessed: " + lastAccessed.toString() + ", now: " + now.toString());
		
		System.out.println("checkSession. Session Timeout: " + request.getSession().getMaxInactiveInterval());
		//long diffMinutes =  (System.currentTimeMillis() - request.getSession().getLastAccessedTime()) / (60 * 1000) % 60; 
		
		// https://stackoverflow.com/questions/13463036/tomcat-session-timeout-web-xml
		// request.getSession(false) == null, || request.getSession(true).isNew()
		//if (diffMinutes >= 0) {
		System.out.println("request.getSession(false): " + request.getSession(false));
		if (request.getSession(false) == null) {
			System.out.println("SESSION TIMEOUT - SESSION IS NULL");
			request.getSession().setAttribute("timeout", true);
			return false;
			//response.setHeader("Location", "/login");
			//response.flushBuffer();
			//response.sendRedirect("http://www.google.com");
		}
		else if (request.getSession().getAttribute("timeout") == null || (Boolean)request.getSession().getAttribute("timeout")) {
			System.out.println("SESSION TIMEOUT - SESSION TIMEOUT VARIABLE SET");
			request.getSession().setAttribute("timeout", true);
			return false;
		}
		
		return true;
	}
	
	@RequestMapping(value="/login",method=RequestMethod.GET)
	public ModelAndView displayLogin(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView model = null;
		// already logged in - redirect to dashboard
		if (request.getSession().getAttribute("csrf_val") != null) {
			model = new ModelAndView("dashboard");
			model.setViewName("redirect:dashboard.html");
		}
		else {
			model = new ModelAndView("login");
			LoginBean loginBean = new LoginBean();
			model.addObject("loginBean", loginBean);
		}
		return model;
	}
	
	@RequestMapping(value="/logout")
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getSession().invalidate();
		ModelAndView model = new ModelAndView("login");
		model.setViewName("redirect:login.html");
		return model;
	}
	
	@RequestMapping(value="/login" ,method=RequestMethod.POST )
	public ModelAndView executeLogin(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("loginBean")LoginBean loginBean) throws IOException, NamingException, SQLException {		
		ModelAndView model = null;
		DirContext ldapContext;
		//try {

			//Path file = Paths.get("C:\\R2D4\\eclipse-workspace\\dataline.properties");
			//List fileAsList = Files.readAllLines(file);
			//String fileAsStr = (String) fileAsList.get(0);
			//String password = fileAsStr;

			HashMap<String, String> propMap = new HashMap<String, String>();
			FileReader reader = new FileReader("C:\\R2D4\\eclipse-workspace\\r2d4.properties");
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				propMap.put(line.split("=")[0], line.split("=")[1]);
			}
			System.out.println("propMap[idb_pswd]=" + propMap.get("idb_pswd"));
			System.out.println("propMap[darwin_apps_pswd]=" + propMap.get("darwin_apps_pswd"));
			//System.out.println("propMap[ad_pswd]=" + propMap.get("ad_pswd"));
			reader.close();

			System.out.println("Début du test Active Directory");
			// CN=bellisr,OU=Medicine,OU=MEM,DC=MSKCC,DC=ROOT,DC=MSKCC,DC=ORG
			Hashtable<String, String> ldapEnv = new Hashtable<String, String>(11);
			ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			//ldapEnv.put(Context.PROVIDER_URL,  "ldap://societe.fr:389");
			ldapEnv.put(Context.PROVIDER_URL, "ldap://ldapha.mskcc.root.mskcc.org:3268");
			ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
			//ldapEnv.put(Context.SECURITY_PRINCIPAL, "CN=singerm,OU=Information Systems,OU=MSK,DC=MSKCC,DC=ROOT,DC=MSKCC,DC=ORG");		
			ldapEnv.put(Context.SECURITY_PRINCIPAL, "CN=Data,OU=Mail Service Accts,OU=Service Accts,OU=Resources,DC=MSKCC,DC=ROOT,DC=MSKCC,DC=ORG");
			//ldapEnv.put(Context.SECURITY_CREDENTIALS, propMap.get("ad_pswd"));
			ldapEnv.put(Context.SECURITY_CREDENTIALS, propMap.get("ad_data_pswd"));
			
			ldapContext = new InitialDirContext(ldapEnv);

			// Create the search controls         
			SearchControls searchCtls = new SearchControls();

			//Specify the attributes to return
			String returnedAtts[]={"sn", "givenName", "samAccountName"};
			searchCtls.setReturningAttributes(returnedAtts);

			//Specify the search scope
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			//specify the LDAP search filter
			String searchFilter = "(&(objectclass=person)(sAMAccountName=" + loginBean.getUsername() + "))";

			//Specify the Base for the search
			String searchBase = "OU=MSK,DC=MSKCC,DC=ROOT,DC=MSKCC,DC=ORG";
			//initialize counter to total the results
			int totalResults = 0;

			// Search for objects using the filter
			NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase, searchFilter, searchCtls);

			//Loop through the search results
			String userPath = "";
			if (answer.hasMoreElements())
			{
				SearchResult sr = (SearchResult)answer.next();

				totalResults++;

				userPath = sr.getName();
				System.out.println(">>>" + sr.getName());
				Attributes attrs = sr.getAttributes();
				System.out.println(">>>>>>" + attrs.get("samAccountName"));
				System.out.println(">>>>>>" + attrs.get("memberOf"));
			}

			System.out.println("Total results: " + totalResults);

			String passwd = loginBean.getPassword().trim().length() == 0 ? "nope" : loginBean.getPassword();
			
			ldapEnv = new Hashtable<String, String>(11);
			ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			//ldapEnv.put(Context.PROVIDER_URL,  "ldap://societe.fr:389");
			ldapEnv.put(Context.PROVIDER_URL,  "ldap://ldapha.mskcc.root.mskcc.org:3268");
			ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
			ldapEnv.put(Context.SECURITY_PRINCIPAL, userPath + ",OU=MSK,DC=MSKCC,DC=ROOT,DC=MSKCC,DC=ORG");
			ldapEnv.put(Context.SECURITY_CREDENTIALS, passwd);

			model = new ModelAndView("dashboard");
			model.setViewName("redirect:dashboard.html");
			String usernameAd = loginBean.getUsername();
						
			try {
				String username = userService.getUsernameR2d4(usernameAd);
				
				if (username.equals("Not valid user")) {
					throw new java.lang.Exception("Not a valid user");
				}
				
				ldapContext = new InitialDirContext(ldapEnv);
				//model.setViewName("redirect:dashboard.html");

				request.getSession().setMaxInactiveInterval(timeout);
				request.getSession().setAttribute("timeout", false);

				double rand = Math.random();
				if (request.getSession().getAttribute("csrf_val") == null) {
					request.getSession().setAttribute("csrf_val", rand);
					
					request.getSession().setAttribute("developerName", userService.getDeveloperName(username));
					request.getSession().setAttribute("developerFromAddress", userService.getUserFromAddress(usernameAd));
					request.getSession().setAttribute("developerEmail", usernameAd.toLowerCase() + "@mskcc.org");
					request.getSession().setAttribute("loggedInUser", username);
					request.getSession().setAttribute("password", loginBean.getPassword());
					

				}
			}
			catch (Exception e) {
				System.out.println("Exception: " + e);
				System.out.println("Not a valid user");
				model = new ModelAndView("login");
				model.addObject("loginBean", loginBean);
				model.addObject("message", "Invalid credentials");
				//request.setAttribute("message", "Invalid credentials!!");
			}

			ldapContext.close();
		//}
		//catch (Exception e)
		//{
		//	System.out.println(" Search error: " + e);
		//	e.printStackTrace();
		//	System.exit(-1);
		//}

		return model;
	}	
	
	@RequestMapping(value="/dashboard")
	public ModelAndView dashboard(HttpServletRequest request, HttpServletResponse response) throws IOException {

		System.out.println("in dashboard!");

		if (!checkSession(request, response)) return new ModelAndView("redirect:logout.html");

		ModelAndView model = null;

		model = new ModelAndView("dashboard");

		model.addObject("developerName", request.getSession().getAttribute("developerName"));
		model.addObject("developerFromAddress", request.getSession().getAttribute("developerFromAddress"));
		model.addObject("developerEmail", request.getSession().getAttribute("developerEmail"));
		model.addObject("loggedInUser", request.getSession().getAttribute("loggedInUser"));

		return model;

	}	
	
	@RequestMapping(value="/preferences")
	public ModelAndView preferences(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		System.out.println("in preferences!");
		
		checkSession(request, response);

		ModelAndView model = null;
		
		model = new ModelAndView("preferences");
		
		return model;
		
	}	
	
	@RequestMapping(value="/scheduler")
	public ModelAndView scheduler(HttpServletRequest request, HttpServletResponse response, @RequestParam("csrf_val") String csrf_val) throws IOException {
		
		System.out.println("in scheduler!");
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return null;
		}

		ModelAndView model = null;
		
		model = new ModelAndView("scheduler");
		
		return model;
		
	}	
	
	@RequestMapping("/initialRetrieve")
	public @ResponseBody String initialRetrieve(HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username) throws SQLException, InterruptedException, IOException {
		
		checkSession(request, response);
		
		JSONArray arrayJSON = projectService.initialRetrieve(username);
		
    	return arrayJSON.toString();
	
	}	
	
	@RequestMapping(value = "/getProjects")
	public @ResponseBody String getProjects(HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username, 
			@RequestParam("projectCode") String projectCode, @RequestParam("requester") String requester, @RequestParam("developer") String developer, @RequestParam("description") String description, @RequestParam("completionStatus") String completionStatus, 
			@RequestParam("irb_waiver") String irbWaiver, @RequestParam("cross_ref") String crossRef, @RequestParam("purpose_of_req") String purposeOfReq, @RequestParam("delivery_plan") String delivery_plan, 
			@RequestParam("technical_specs") String technicalSpecs, @RequestParam("test_plan") String testPlan, @RequestParam("sql") String sql, 
			@RequestParam("any") String any, @RequestParam("and_or") String and_or, @RequestParam("csrf_val") String csrf_val) throws InterruptedException, SQLException, IOException {
 
		System.out.println("in /getProjects. and_or: " + and_or);
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			System.out.println("-----TIMEOUT-----");
			return "TIMEOUT";
		}
				
		//if (!any.trim().isEmpty()) {
		//	and_or = "OR";
		//}
		
		JSONArray arrayJSON = projectService.getProjects(username, projectCode, requester, developer, description, completionStatus, 
				irbWaiver, crossRef, purposeOfReq, delivery_plan, technicalSpecs, testPlan, sql, any, and_or);
		
    	return arrayJSON.toString();
	
	}	
	
	@RequestMapping("/getProjectInfo")
	public @ResponseBody String getProjectInfo(HttpServletRequest request, HttpServletResponse response, @RequestParam("projectCode") String projectCode, @RequestParam("sn") String sn, @RequestParam("version") String version, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		System.out.println("In /getProjectInfo, version: " + version + ", projectCode: " + projectCode);
		 
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		JSONObject jsonObject = projectService.getProjectInfo(projectCode, sn, version);
		
    	return jsonObject.toString();
    		
	}	
	
	@RequestMapping(value = "/determineCustodians", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String determineCustodians(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws IOException {
		
		System.out.println("In /determineCustodians");

		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
				
		//System.out.println("In /determineCustodians, sqlCode: " + sqlCode + ", projectCode: " + projectCode);
		
		JSONArray arrayJSON = projectService.determineCustodians("SINGERM", jsonObject.getString("tableList"), jsonObject.getString("projectCode") );
		
    	return arrayJSON.toString();

	}
	
	@RequestMapping(value = "/determineTablesFromSQLText", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String determineTablesFromSQLText(HttpServletRequest request, HttpServletResponse response, @RequestBody String sqlCode, @RequestBody String currentTables) throws IOException {
		
		checkSession(request, response);
		
		//System.out.println("In /determineCustodians, usrename: " + username + ", sqlCode: " + sqlCode);
		System.out.println("In /determineTablesFromSQLText, sqlCode: " + sqlCode);

		String tables = projectService.determineTablesFromSQLText("SINGERM", sqlCode, currentTables);
		
    	return tables;

	}
	
	@RequestMapping("/custodianTablesSearch")
	public @ResponseBody String custodianTablesSearch(HttpServletRequest request, HttpServletResponse response, @RequestParam("term") String searchString) throws IOException {
		
		checkSession(request, response);
		
		System.out.println("In /custodianTablesSearch, searchString: " + searchString);
 
		JSONArray arrayJSON = projectService.custodianTablesSearch(searchString);
		
    	return arrayJSON.toString();
	
	}	
	
	@RequestMapping("/searchSn")
	public @ResponseBody String searchSn(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") String sn) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /searchSn, projectCode: " + sn);
 
		String folderPath = projectService.searchSn(sn);
		
    	return folderPath;
	
	}	
	
	@RequestMapping(value = "/projectCommit", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String projectCommit(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /projectCommit, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
				
    	return projectService.projectCommit(formData);
	
	}	
	
	@RequestMapping(value = "/addAmendment", method = RequestMethod.POST)
	public @ResponseBody String addAmendment(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") String sn, 
			@RequestParam("amendment_developer") String amendmentDeveloper,	@RequestParam("amendment_date") String amendmentDate, 
			@RequestParam("amendment_note") String amendmentNote, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
						
		System.out.println("In /addAmendment, sn: " + sn + ", amendmentDeveloper: " + amendmentDeveloper + ", amendmentDate: " + amendmentDate + ", amendmentNote: " + amendmentNote);
		
    	return projectService.addAmendment(sn, amendmentDeveloper, amendmentDate, amendmentNote);
	
	}	
	
	@RequestMapping(value = "/removeAmendment", method = RequestMethod.POST)
	public @ResponseBody String removeAmendment(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /removeAmendment, id: " + id);
		
    	return projectService.removeAmendment(id);
	
	}	
	
	/*@RequestMapping("/createPlan")
	public @ResponseBody String createPlan(HttpServletRequest request, HttpServletResponse response, @RequestParam("projectCode") String projectCode, @RequestParam("path") String path) throws IOException, SQLException {
		
		checkSession(request, response);
		
		System.out.println("In /createPlan, projectCode: " + projectCode);
		
    	return projectService.createPlan(projectCode, path);
	
	}*/	
	
	@RequestMapping("/custodianSearch")
	public @ResponseBody String custodianSearch(HttpServletRequest request, HttpServletResponse response, @RequestParam("term") String searchString) throws SQLException, IOException {
		
		checkSession(request, response);
		
    	return projectService.custodianSearch(searchString).toString();
	
	}	
	
	@RequestMapping(value = "/deleteVersion", method = RequestMethod.POST)
	public @ResponseBody String deleteVersion(HttpServletRequest request, HttpServletResponse response, @RequestParam("projectCode") String projectCode, @RequestParam("sn") String sn, @RequestParam("version") String version) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /deleteVersion, projectCode: " + projectCode + ", sn: " + sn +  ", version: " + version);
		
    	return projectService.deleteVersion(projectCode, sn, version);
	
	}	
	
	@RequestMapping(value = "/sendMail")
	public @ResponseBody String sendMail(HttpServletRequest request, HttpServletResponse response, @RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("msg") String msg) throws IOException {
		
		checkSession(request, response);
		
		System.out.println("In /sendMail, from: " + from + ", to: " + to +  ", msg: " + msg);
		
    	return projectService.sendMail(from, to, msg);
	
	}	
	
	@RequestMapping(value = "/determineIfFolderExists")
	public @ResponseBody String determineIfFolderExists(HttpServletRequest request, HttpServletResponse response, @RequestParam("folder") String folder) throws IOException {
		
		checkSession(request, response);
		
		System.out.println("In /determineIfFolderExists, folder: " + folder);
		
    	return projectService.determineIfFolderExists(folder);
	
	}	
	
	@RequestMapping(value = "/createProjectFolder")
	public @ResponseBody String createProjectFolder(HttpServletRequest request, HttpServletResponse response, @RequestParam("folder") String folder) throws IOException {
		
		checkSession(request, response);
		
		System.out.println("In /createProjectFolder, folder: " + folder);
		
    	return projectService.createProjectFolder(folder);
	
	}	
	
	@RequestMapping(value = "/sendEmailDialog", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String sendEmailDialog(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /sendEmailDialog, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
						
		//String developerFromAddress = (String) request.getSession().getAttribute("fromAddress");
		String username = (String) request.getSession().getAttribute("loggedInUser");
		
    	return projectService.sendEmailDialog(formData, username);
	
	}	
	
	@RequestMapping(value = "/addApproval", method = RequestMethod.POST)
	public @ResponseBody String addApproval(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") int sn, 
			@RequestParam("custodian") String custodian, @RequestParam("cust_table") String table, @RequestParam("sn_code") String projectCode, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
								
		System.out.println("In /addApproval, sn: " + sn + ", custodian: " + custodian + ", cust_table: " + table);
		
    	return projectService.addApproval(sn, custodian, table,projectCode);
	
	}	
	
	@RequestMapping(value = "/removeApproval", method = RequestMethod.POST)
	public @ResponseBody String removeApproval(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") int sn, 
			@RequestParam("custodian") String custodian, @RequestParam("cust_table") String table, @RequestParam("sn_code") String projectCode, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		System.out.println("In /removeApproval, sn: " + sn + ", custodian: " + custodian + ", cust_table: " + table);
		
    	return projectService.removeApproval(sn, custodian, table,projectCode);
	
	}	
	
	@RequestMapping(value = "/removeAllApprovals", method = RequestMethod.POST)
	public @ResponseBody String removeAllApprovals(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") int sn, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		System.out.println("In /removeApproval, sn: " + sn);
		
    	return projectService.removeAllApprovals(sn);
	
	}	

	@RequestMapping(value="/approve",method=RequestMethod.GET)
	public ModelAndView approve(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		
		ModelAndView model = new ModelAndView("approve");

		String projectCode = request.getParameter("projectCode");
		
		String randCode = request.getParameter("randCode");
		
		model.addObject("projectCode", projectCode);
		
		model.addObject("message", approvalService.approveProject(projectCode, randCode));
		
		System.out.println("In /approve, projectCode: " + projectCode + ", randCode: " + randCode);

		return model;
	}
	
	@RequestMapping(value = "/sendEmailDialogMessage", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String sendEmailDialogMessage(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /sendEmailDialogMessage, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
    	return projectService.sendEmailDialogMessage(formData);
	
	}	
	
	@RequestMapping("/getSchedule")
	public @ResponseBody String getSchedule(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /getSchedule");
 
		JSONArray arrayJSON = projectService.getSchedule();
		
    	return arrayJSON.toString();
	
	}	
	
	@RequestMapping(value = "/schedulerCreateNewJob", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String schedulerCreateNewJob(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /schedulerCreateNewJob, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
    	return projectService.schedulerCreateNewJob(formData);
	
	}	
		
	@RequestMapping("/schedulerInitialRetrieve")
	public @ResponseBody String schedulerInitialRetrieve(HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username) throws SQLException, InterruptedException, IOException {
		
		checkSession(request, response);
		
		JSONObject objectJSON = projectService.schedulerInitialRetrieve();
		
    	return objectJSON.toString();
	
	}	
	
	@RequestMapping(value = "/schedulerUpdateExistingJob", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String schedulerUpdateExistingJob(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /schedulerUpdateExistingJob, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
    	return projectService.schedulerUpdateExistingJob(formData);
	
	}	
		
	@RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
	public @ResponseBody String deleteJob(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /deleteJob, id: " + id);
		
    	return projectService.deleteJob(id);
	
	}	
	
	@RequestMapping(value = "/generateExcel", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String generateExcel(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /generateExcel, sql: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		String filename = projectService.generateExcel(formData);
		
		return filename;
		
	}	
	
	@RequestMapping(value = "/downloadExcel", method = RequestMethod.GET)
	public void downloadExcel(HttpServletRequest request, HttpServletResponse response, String projectCode) throws SQLException, IOException {

		//return new ModelAndView(new ExcelReportView(), "studentList", "");

		System.out.println("In /generateExcel, projectCode: " + projectCode);

		InputStream is = new FileInputStream("C:\\R2D4\\eclipse-workspace\\DataLine\\WebContent\\WEB-INF\\excel\\" + projectCode + ".xlsx");
		// copy it to response's OutputStream
		response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;");
		response.setHeader("Content-Disposition","attachment;filename=\"DataLine Results - " + projectCode + ".xlsx\";");
		org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();


	}	
	
	@RequestMapping(value = "/projectCopy", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String projectCopy(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /projectCopy, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
   	return projectService.projectCopy(formData);
	
	}	
	
	@RequestMapping("/getScheduleLog")
	public @ResponseBody String getScheduleLog(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /getScheduleLog");
 
		JSONArray arrayJSON = projectService.getScheduleLog();
		
    	return arrayJSON.toString();
	
	}	
	
	@RequestMapping("/refreshProjectFromR2d3")
	public @ResponseBody String refreshProjectFromR2d3(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") String sn, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		System.out.println("In /refreshProjectFromR2d3");
		 
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		JSONObject objectJSON = projectService.refreshProjectFromR2d3(sn);
		
    	return objectJSON.toString();
	
	}	
	
	@RequestMapping("/overrideCustodian")
	public @ResponseBody String overrideCustodian(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") int sn, @RequestParam("custodian") String custodian, @RequestParam("currentOverride") String currentOverride) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /overrideCustodian");
 
		String val = projectService.overrideCustodian(sn, custodian, currentOverride);
		
    	return val;
	
	}	
	
	@RequestMapping("/overrideRequester")
	public @ResponseBody String overrideRequester(HttpServletRequest request, HttpServletResponse response, @RequestParam("sn") int sn, @RequestParam("projectCode") String projectCode, @RequestParam("currentOverride") String currentOverride) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /overrideRequester");
 
		String val = projectService.overrideRequester(sn, projectCode, currentOverride);
		
    	return val;
	
	}	
	
	@RequestMapping(value = "/runJob", method = RequestMethod.POST)
	public @ResponseBody String runJob(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) throws SQLException, IOException {
		
		checkSession(request, response);
		
		System.out.println("In /runJob, id: " + id);
		
    	return projectService.runJob(id);
	
	}	
	
	@RequestMapping(value = "/generateWord", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String generateWord(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException, InvalidFormatException {
		
		System.out.println("In /generateWord, sql: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		String filename = projectService.generateWord(formData);
		
		return filename;
		
	}	
	
	@RequestMapping(value = "/getProjectEmails")
	public @ResponseBody String getProjectEmails(HttpServletRequest request, HttpServletResponse response, @RequestParam("projectCode") String projectCode, @RequestParam("csrf_val") String csrf_val) throws Exception {
		
		System.out.println("In /getProjectEmails. projectCode: " + projectCode);
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		String username = (String) request.getSession().getAttribute("loggedInUser");

		JSONArray arrayJSON = projectService.getProjectEmails(projectCode, username, request.getSession().getAttribute("developerEmail").toString(), request.getSession().getAttribute("password").toString());
		
		return arrayJSON.toString();
		
	}	
	
	@RequestMapping(value = "/getProjectEmailById")
	public @ResponseBody String getProjectEmailById(HttpServletRequest request, HttpServletResponse response, @RequestParam("emailId") String emailId, @RequestParam("csrf_val") String csrf_val) throws Exception {
		
		System.out.println("In /getProjectEmailById. id: " + emailId);
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		String body = projectService.getProjectEmailById(emailId, request.getSession().getAttribute("developerEmail").toString(), request.getSession().getAttribute("password").toString()).getString("body");
		
		if (!body.contains("text/html")) {
			 body = "<pre>" + body + "</pre>";
		}
		
		return body;
		
	}	
	
	@RequestMapping(value = "/copyEmailById")
	public @ResponseBody String copyCurrentEmailById(HttpServletRequest request, HttpServletResponse response, @RequestParam(value="emailIds[]") List<String> emailIds, @RequestParam("folder") String folder, @RequestParam("csrf_val") String csrf_val) throws Exception {
		
		System.out.println("In /copyEmailById. id: " + emailIds);
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		System.out.println(emailIds);
		
		for (String emailId : emailIds) {
			projectService.copyEmailById(emailId, folder, request.getSession().getAttribute("developerEmail").toString(), request.getSession().getAttribute("password").toString());
		}
		
		return "ok";
		
	}	
	
	@RequestMapping(value = "/copyAllEmails")
	public @ResponseBody String copyAllEmails(HttpServletRequest request, HttpServletResponse response, @RequestParam("projectCode") String projectCode, @RequestParam("folder") String folder, @RequestParam("csrf_val") String csrf_val) throws Exception {
		
		System.out.println("In /copyAllEmails. projectCode: " + projectCode);
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}

		String username = (String) request.getSession().getAttribute("loggedInUser");

		JSONArray emailsArrayJSON = projectService.getProjectEmails(projectCode, username, request.getSession().getAttribute("developerEmail").toString(), request.getSession().getAttribute("password").toString());
		
		return projectService.copyAllEmails(folder, emailsArrayJSON, request.getSession().getAttribute("developerEmail").toString(), request.getSession().getAttribute("password").toString());
		
	}	
	
	@RequestMapping("/initialUserPrefsRetrieve")
	public @ResponseBody String initialUserPrefsRetrieve(HttpServletRequest request, HttpServletResponse response, @RequestParam("csrf_val") String csrf_val) throws Exception {
		
		System.out.println("In /initialUserPrefsRetrieve");
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		String username = (String) request.getSession().getAttribute("loggedInUser");
		String developerEmail = (String) request.getSession().getAttribute("developerEmail");
		String password = (String) request.getSession().getAttribute("password");
		
		JSONObject jsonObject = projectService.initialUserPrefsRetrieve(username, developerEmail, password);
		
    	return jsonObject.toString();
	
	}	
	
	@RequestMapping(value = "/updateUserPrefs", method = RequestMethod.POST, headers="Content-Type=application/json")
	public @ResponseBody String updateUserPrefs(HttpServletRequest request, HttpServletResponse response, @RequestBody String formData) throws SQLException, IOException {
		
		System.out.println("In /updateUserPrefs, formData: " + formData);
		
		JSONObject jsonObject = new JSONObject(formData);
		
		if (!checkSessionCsrf(request, response, jsonObject.getString("csrf_val"))) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		String username = (String) request.getSession().getAttribute("loggedInUser");

    	return projectService.updateUserPrefs(formData, username);
	
	}	
			
	@RequestMapping(value = "/addRequester", method = RequestMethod.POST)
	public @ResponseBody String addRequester(HttpServletRequest request, HttpServletResponse response, @RequestParam("add_requester_email") String add_requester_email, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		System.out.println("In /addRequester, add_requester_email: " + add_requester_email);
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
    	return projectService.addRequester(add_requester_email);
	
	}	
			
	@RequestMapping(value = "/sendOutlookUserPrefs", method = RequestMethod.POST)
	public @ResponseBody String sendOutlookUserPrefs(HttpServletRequest request, HttpServletResponse response, @RequestParam("message_type") String message_type, @RequestParam("csrf_val") String csrf_val) throws SQLException, IOException {
		
		System.out.println("In /sendOutlookUserPrefs");
		
		if (!checkSessionCsrf(request, response, csrf_val)) {
			response.setStatus((int)HttpServletResponse.SC_REQUEST_TIMEOUT);
			return "TIMEOUT";
		}
		
		String username = (String) request.getSession().getAttribute("loggedInUser");
		
    	return projectService.sendOutlookUserPrefs(username, message_type);
	
	}	
			
	@RequestMapping("/welcome")
	public ModelAndView welcome() {
		System.out.println("HEEEE ");
		
		/*try {
			  Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			  	System.out.println("good");
			  //on classpath
			} catch(ClassNotFoundException e) {
				System.out.print("bad");
			  //not on classpath
			}
		
	      String connectionUrl = "jdbc:sqlserver://PS23A:61692;DatabaseName=DEDGPDLR2D2;user=yDARWIN;Password=***";  
	    	      // Declare the JDBC objects.  
	    	      Connection con = null;  
	    	      Statement stmt = null;  
	    	      ResultSet rs = null;  
	    	      try {  
	    	         // Establish the connection.  
	    	         Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
	    	         con = DriverManager.getConnection(connectionUrl);  
	    	         // Create and execute an SQL statement that returns some data.  
	    	         String SQL = "SELECT TOP 10 * FROM dbo.projects";  
	    	         stmt = con.createStatement();  
	    	         rs = stmt.executeQuery(SQL);  
	    	         // Iterate through the data in the result set and display it.  
	    	         while (rs.next()) {  
	    	            System.out.println(rs.getString(4) + " " + rs.getString(6));  
	    	         }  
	    	      }  
	    	      // Handle any errors that may have occurred.  
	    	      catch (Exception e) {  
	    	         e.printStackTrace();  
	    	      }  
	    	      finally {  
	    	         if (rs != null) try { rs.close(); } catch(Exception e) {}  
	    	         if (stmt != null) try { stmt.close(); } catch(Exception e) {}  
	    	         if (con != null) try { con.close(); } catch(Exception e) {}  
	    	      }  */
		String message = "<br><div style='text-align:center;'>"
				+ "<h3>WELCOME</h3></div><br><br>";
		return new ModelAndView("welcome", "message", message);
	}
	
}
