package com.dataline.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.dataline.dao.*;
import com.dataline.model.*;

@Transactional
@Service
public class ApprovalService {
	@Autowired
	private DataDaoImpl dataDaoImpl;
	
	 public String approveProject(String projectCode, String randCode) throws SQLException, IOException {
		 return dataDaoImpl.approveProject(projectCode, randCode);
	 }

}
