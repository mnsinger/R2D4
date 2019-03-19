<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>DataLine Projects Dashboard</title>
  <link rel="icon" 
      type="image/png" 
      href="<%=request.getContextPath() %>/images/r2-d21600.png">
  <link rel="stylesheet" href="<%=request.getContextPath() %>/CSS/jquery-ui.css" type="text/css" />
  <link rel="stylesheet" href="<%=request.getContextPath() %>/CSS/theme.blue.css" type="text/css" />
  <link rel="stylesheet" href="<%=request.getContextPath() %>/CSS/style.css" type="text/css" />
  <!--  FREE ICONS: https://www.iconfinder.com/free_icons -->
  <!-- Fugue Icons: http://p.yusukekamiyamane.com/icons/attribution/ -->
  
  <script type="text/javascript" src="<%=request.getContextPath() %>/JS/jquery-3.1.1.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath() %>/JS/jquery-ui.min-new.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath() %>/JS/dashboard.js"></script>
  
  <!-- Google Charts -->
  <!-- script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script-->
  
  <!-- script type="text/javascript" src="<%=request.getContextPath() %>/JS/jquery.tablesorter.combined.js"></script-->
  
  <script>
	var directories = [];
	var changed=false;
	var csrf_val = "<%=session.getAttribute("csrf_val")%>"

	  // Shorthand for $( document ).ready()

	$( function() {
		
	    $( "#dialog-message" ).dialog({
	        autoOpen: false,
	        buttons: {
	            "Ok": function() {
	              $( this ).dialog( "close" );
	            },
	            Cancel: function() {
	              $( this ).dialog( "close" );
	            }
	        }
	      }).prev(".ui-dialog-titlebar").css("background","lightgreen");
		  
	     $.datepicker.setDefaults({
	    	 onSelect: function(date) {
		    	if (!changed && $(this)[0].id != 'amendment_date') { 
		    		//$('#commit_butt' ).css('background','lightgreen'); 
		        	//changed = true;
		        	enableCommit();
		    	}
	         }
	     });
	    
		 $('#reqd_deliv_date').datepicker({dateFormat: "yy-mm-dd"});
		 $('#received_date').datepicker({dateFormat: "yy-mm-dd"});
		 $('#est_delivery_dte').datepicker({dateFormat: "yy-mm-dd"});
		 $('#start_date').datepicker({dateFormat: "yy-mm-dd"});
		 $('#delivery_date').datepicker({dateFormat: "yy-mm-dd"});
		 $('#amendment_date').datepicker({dateFormat: "yy-mm-dd"});
		 
		 $("textarea").resizable({
			    handles: "se" // place handle only in 'south-east' of textarea
			});
		  
		var projectList;
		var tabsDict    = { "Developer Dashboard": 2 };
		var tabsDictVis = { "Developer Dashboard": 'vis' };
		var tline = [];
		
		function populateTablesUsed(tableList) {
			$('#all_tables_textarea').val(tableList);
		}
		
		function populateApprovals(custodianList) {
			populateApprovalsHTML(custodianList, 1);
			
			// obsolete - removeAllApprovals doesn't do anything anymore - need to revisit
			/*$.ajax({
				url:  'removeAllApprovals',
				type: 'POST',
				cache: false,
				data:{ sn: $('#sn').val(), sn_code: $('#sn_code').val(), csrf_val: csrf_val },
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					populateApprovalsHTML(custodianList, 1)
				}
			});*/
		}
		
		function populateApprovalsHTML(custodianList, addToDatabase) {
			$('#approvals tr.auto').remove();
			$('#approvals tr.manual').remove();
			$('#missing_custodian tr.auto').remove();
			//console.log('pop approvals html');
			//console.log(custodianList);
			//console.log("addToDatabase: " + addToDatabase);
			var prev_cust = "";
			var curr_cust = "";
			var table_set = new Set();
			$.each( custodianList, function( i, custodian ) {
				curr_cust = custodian.cust_name ? custodian.cust_name.trim() : '';
				//console.log('pop approvals ' + curr_cust);
				var cust_table = custodian.cust_table ? custodian.cust_table.trim() : '';
				if (cust_table.length > 0) {
					table_set.add(cust_table);
				}
				var butt = "<button style='vertical-align:middle;margin-bottom:0px;background:tomato;'>-</button>"
				var y_n = custodian.approved == '1' ? '<span style="color:green">Y</span>' : '<span style="color:red">N</span>';
				var override = custodian.override == '1' ? '<span style="color:green">Y</span>' : '<span style="color:red">N</span>';
				if (prev_cust == curr_cust) {
					//curr_cust = "";
					//butt = "&nbsp;"
					y_n = "&nbsp;";
					override = "&nbsp;";
				}
		        var $tr = $('<tr class="auto">').append(
		                $('<td style="background:white;text-align:center;font-weight:bold;">').html(y_n),
		                $('<td ' + (y_n == "&nbsp;" ? '' : 'class="override" id="' + curr_cust + '"') + ' style="background:white;text-align:center;font-weight:bold;">').html(override),
		                $('<td style="background:white;text-align:left;font-weight:normal;">').html(curr_cust),
		                $('<td style="background:white;text-align:left;font-weight:normal;">').html(cust_table),
		                $('<td style="text-align:left;background:white;">').html("<button class='removeApprover' style='vertical-align:middle;margin-bottom:0px;background:lightgrey;color:red;font-weight:bold;'>-</button>"),
		                $('<td style="display:none;">').html(custodian.custodian_email)
				).appendTo('#approvals');
		        //console.log("custodian email: " + custodian.custodian_email);
		        prev_cust = custodian.cust_name ? custodian.cust_name.trim() : '';
		        if (addToDatabase == 1) addApprovals($('#sn').val(), curr_cust, cust_table, $('#sn_code').val());
			});
			
			var tables_used;
			if (typeof $('#all_tables_textarea').val() != 'undefined' && $('#all_tables_textarea').val().length > 0) {
				tables_used = $('#all_tables_textarea').val().split("\n");
	            $.each(tables_used, function(i){
	                if (!table_set.has(tables_used[i].trim().toUpperCase()) && typeof tables_used[i] != 'undefined' && tables_used[i].length > 0) {
	            	   var $tr = $('<tr class="auto">').append(
	   		                $('<td style="background:white;text-align:left;font-weight:normal;">').html(tables_used[i].toUpperCase())
	   					).appendTo('#missing_custodian');               
	            	}
	            });
			}
			if (custodianList.length > 0) { $('#remove_all_approvals').show(); }
			else { $('#remove_all_approvals').hide(); }

		}
		
		function generateCompletionStatus() {
			
			var completionStatus = "";
			
			var statusFilters = ["unassigned", "open", "delivered", "onhold", "ongoing", "cancelled"];
			var arrayLength = statusFilters.length;
			for (var i = 0; i < arrayLength; i++) {
				if ($('#' + statusFilters[i]).is(':checked')) completionStatus += "1";
				else completionStatus += "0";
			}
			return completionStatus;
		}
		
		function populateProjectList(projectList, directories) {
			var currentProject = '#' + $('#sn').val();
			//console.log(currentProject);
			var c = 0;
			$.each( projectList, function( i, project ) {
				var folder_text = "";
				if (!project.cross_ref || project.cross_ref.trim().indexOf(" ") >= 0 || !/\d/.test(project.cross_ref)) { }
				else {
					folder_text = '<span class="folder"><img id="' + project.cross_ref.trim() + '" src="<%=request.getContextPath() %>/images/folder-open.png" style="position:relative;float:right;top:2px;"></span>';
				}
				if (project.developer_primary === undefined) project.developer_primary = "";
		        var $tr = $('<tr id="' + project.serial_number + '" style="height:40px">').append(
		                $('<td style="min-width:85px;max-width:85px;vertical-align:top;text-align:left;">').html(
		                		'<span style="font-size:.8em;" class="projectCode" id="' + project.serial_number + '">' +
		                		project.code + '</span><span class="folder"><img id="' + project.code + '" src="<%=request.getContextPath() %>/images/folder-horizontal-open.png" style="height:15px;float:right"></span><br>' + 
		                		(project.cross_ref ? '&#x2EA; <span style="font-size:.7em;" class="projectCodeX ' + project.cross_ref.replace(/\D/g,'')  + '">' + project.cross_ref + '</span>' + folder_text : '')
		                		),
		                $('<td style="min-width:126px;width:125px;vertical-align:top;text-align:left;">').html(
		                		'<span style="font-size:.8em;"><a class="requester" href="mailto:' + project.email + '?subject=DataLine Request - ' + project.code + '">' + project.requester + '</a>' + 
		                		'</span><br><span style="font-size:.7em"><span id="' + project.code + '_department_name" style="display:inline-block;margin-bottom:-2px;overflow:hidden;width:120px;white-space:nowrap;">' + project.department_name + '</span>' +
		                		//'<br><div class="tooltip">' + project.request_date + '<span class="tooltiptext">Requested date</span></div>' + 
		                		//'<br><div class="tooltip">' + project.required_date + '<span class="tooltiptext">Required date</span></div>' + 
		                		'</span>'
		                		),
		                $('<td style="min-width:70px;width:70px;vertical-align:top;text-align:left;">').html('<span style="font-size:.7em">' + project.developer_primary + 
		                		'<br>' + project.status + 
		                		'</span>'),
		                $('<td style="min-width:230px;max-width:400px;vertical-align:top;text-align:left;font-size:.8em">').text(project.description.substring(0, 80))
		            ).appendTo('#projectList');
		        c++;
		        
			});
			if (currentProject.length > 1) {
				$( currentProject ).css('background-color','lightgreen');
				$( ".projectCodeX." + currentProject.substring(1)).css('background-color','lightgreen');
			}
			if (c==100) c = '100+';
			$('#projectCount').val(c);
		}
		
		function populateAmendments(projectid, sn, version) {

			$.ajax({ 
				url:   'getProjectInfo',
						type: 'POST',
						cache: false,
						data:{ projectCode: projectid, sn: sn, version: version, csrf_val: csrf_val },
						beforeSend: function( xhr ) {
							//var sn_code = $('#sn_code').val();
							//if (sn_code.length != 0) 
							//	$('#' + sn_code).css('background-color','');
						},
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
							
						},
						success: function (data) {
							
							project = jQuery.parseJSON(data);
							
							if (project.code == -1) {
								errorMsg(285, "", "");
								return;
							}
							$("#amendments").find("tr:gt(0)").remove();
														
							$.each( project.amendments, function( i, amendment ) {
								if (amendment.id != 0) {
							        var $tr = $('<tr id="' + amendment.id + '">').append(
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;">').html(
							                		amendment.developer
							                ),
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;">').html(
							                		amendment.delivery_date
							                ),
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;max-width:185px;">').html(
							                		amendment.amendment_note
							                ),
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;max-width:185px;">').html(
							                		'<button class="removeAmendment" type="button" style="vertical-align:middle;margin-bottom:0px;background:tomato;" value=' + amendment.id + '>-</button>'
							                )
							            ).appendTo('#amendments');
								}
							});
							
						}
			});

		}
		
		function populateProjectInfo(projectid, sn, version) {
			$('#excel_div').empty();
			$('#dead_excel').hide();
			
			$('#email_list').empty();
			$('#related_email_body').html('');

			$.ajax({
				url:   'getProjectInfo',
						type: 'POST',
						cache: false,
						data:{ projectCode: projectid, sn: sn, version: version, csrf_val: csrf_val },
						beforeSend: function( xhr ) {
							var sn_code = $('#sn_code').val();
							var sn = $('#sn').val();
							if (sn_code.length != 0) {
								$('#' + sn).css('background-color','');
								$('.projectCodeX.' + sn).css('background-color', '');
							}
						},
						error: function (jqXHR, textStatus, errorThrown ) {
							//console.log(jqXHR, textStatus, errorThrown);
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							
							disableCommit();
							
							$('#amendment_developer').val('Developer');
							$('#amendment_date').val('');
							$('#amendment_note').val('');

							project = jQuery.parseJSON(data);
							
							if (project.code == -1) {
								$('#' + $('#sn').val()).css('background-color', 'lightgreen');
								$('.projectCodeX.' + $('#sn').val()).css('background-color', 'lightgreen');
								errorMsg(285, "", "");
								return;
							}
							
							//console.log(project);
							
							//$('#amendments tr').remove();
							$("#amendments").find("tr:gt(0)").remove();
							$("#approvals").find("tr:gt(0)").remove();
							$("#missing_custodian").find("tr:gt(0)").remove();
														
							$('#' + project.serial_number).css('background-color','lightgreen');
							$('.projectCodeX.' + project.serial_number).css('background-color', 'lightgreen');
							
							$('#sn').val(project.serial_number);
							$('#sn_code').val(project.code);
							$("#request_type").val(project.request_type);
							$("#received_date").val(project.request_date);
							$("#reqd_deliv_date").val(project.required_date);
							$("#est_delivery_dte").val(project.est_delivery_dte);
							$("#developer").val(project.developer_primary);
							$("#schedule_status").val(project.schedule_status);
							$("#priority").val(project.priority);
							$("#received_by").val(project.received_by);
							
							$("#start_date").val(project.start_date);
							$("#delivery_date").val(project.delivery_date);
							$("#status").val(project.status);
							$("#objs_delivered").val(project.objs_delivered);
							$("#second_developer").val(project.second_developer);
							$("#automation").val(project.automation);
							$("#estimated_hours").val(project.estimated_hours);
							
							$("#requester").val(project.requester_id);
							$("#requesterFirstName").val(project.requester_first_name);
							$("#requesterLastName").val(project.requester_last_name);
							$("#requesterName").val(project.requester_first_name + " " + project.requester_last_name);
							$("#requester_email").val(project.requester_email);
							$("#requester_title").val(project.requester_title);
							$("#request_description").val(project.request_description);
							$("#privacy_type").val(project.privacy_type);
							$("#irb_waiver").val(project.irb_waiver);
							$("#cross_ref").val(project.cross_ref);
							$("#description").val(project.description);
							$("#purpose_of_request").val(project.purpose_of_request);
							
							$("#data_elements").val(project.data_elements);
							$("#criteria").val(project.criteria);
							$("#program_specs").val(project.program_specs);
							
							color = "red";
							if (project.requester_approval == 'Y') color = "green";
							$('#requester_approvals tr:eq(1) td:eq(0)').html("<font color='" + color + "'><b>" + project.requester_approval + "</b></font>");
							
							color = "red";
							if (project.override == 'Y') color = "green";
							$('#requester_approvals tr:eq(1) td:eq(1)').html("<font color='" + color + "'><b>" + project.override + "</b></font>");
							$('#requester_approvals tr:eq(1) td:eq(2)').html(project.requester_first_name + " " + project.requester_last_name);
							
							//console.log("project.program_specs: " + project.program_specs);
							
							/*if (project.program_specs && project.program_specs.length > 0) {
								$("#delivery_plan").show();
								console.log("showing delivery plan");
							}
							else {
								$("#delivery_plan").hide();
							}*/
							
							$("#technical_specs").val(project.technical_specs);
							$("#test_plan").val(project.test_plan);
							$("#project_notes").val(project.project_notes);
							$("#dev_notes").val(project.dev_notes);
							$("#sql").val(project.sql);
							$("#email").val(project.email);

							$("#all_tables_textarea").val('');
							
							// following line calls code that
							// populates "Tables Used" textarea
						    $("#sql").trigger("keyup");
							
							//console.log('calling populate approvals');
							//console.log(project.custodians);
							//console.log(project.custodians.length);
							
							if (project.custodians && project.custodians.length > 0)
								populateApprovalsHTML(project.custodians, 0);
							else
								$('#remove_all_approvals').hide();

							$.each( project.amendments, function( i, amendment ) {
								if (amendment.id != 0) {
							        var $tr = $('<tr id="' + amendment.id + '">').append(
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;">').html(
							                		amendment.developer
							                ),
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;">').html(
							                		amendment.delivery_date
							                ),
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;max-width:185px;">').html(
							                		amendment.amendment_note
							                ),
							                $('<td style="vertical-align:top;text-align:left;font-family:normal;font-size:.9em;background:white;max-width:185px;">').html(
							                		'<button class="removeAmendment" type="button" style="vertical-align:middle;margin-bottom:0px;background:tomato;" value=' + amendment.id + '>-</button>'
							                )
							            ).appendTo('#amendments');
								}
							});
							
							// only remove version options if new project selected
							if (version == 'All') {
								$('#version')
								.find('option')
							    .remove()
							    .end();
								
								$.each( project.commits, function( i, commit ) { 
									if (commit.date_time.substring(0,4) == '1900') commit.date_time = "Original";
									$('#version').append($('<option>', {
										value: commit.date_time,
										text : (commit.date_time.substring(0,4) == '1800' ? "Original" : commit.date_time)
									}))

								});
							}
							// if delete_version button exists
							// hide it when Original is selected
							if ( $('#delete_version_butt').length ) {
								//console.log("delete_version_butt exists");
								if ( $('#version option:selected').val() == 'Original' || $('#version option:selected').val().substring(0,4) == '1800') {
									$('#delete_version_butt').hide();
									//console.log("delete_version_butt exists - Original");
								}
								else {
									$('#delete_version_butt').show();
									//console.log("delete_version_butt exists - Specific Version");
								}
							}
							// button doesn't exist
							else {
								//console.log("delete_version_butt DOES NOT exist");
								$('#commit_butt').after('<button id="delete_version_butt" type="button" style="float:left;margin-left:3px;margin-top:3px;background:tomato;">Delete Version</button>');
								if ( $('#version option:selected').val() == 'Original' ) {
									$('#delete_version_butt').hide();
									//console.log("delete_version_butt DOES NOT exist - Original");
								}
								else {
									$('#delete_version_butt').show();
									//console.log("delete_version_butt DOES NOT exist - Specific Version");
								}
							}
							
							$("#getProjectEmails").trigger("click");

						}
			});

		}
		
		function populateDevelopers(developers) {
			
			//devs = jQuery.parseJSON(developer);

			$.each( developers, function( i, dev ) {
				
				var $select = $('#developer');		
				$select.append('<option value=' + dev.developer_primary + '>' + dev.developer_primary + '</option>');
				
				$select = $('#second_developer');		
				$select.append('<option value=' + dev.developer_primary + '>' + dev.developer_primary + '</option>');
				
				$select = $('#amendment_developer');		
				$select.append('<option value=' + dev.developer_primary + '>' + dev.developer_primary + '</option>');
			
			});

		}
		  
		function populateAutomation(automation) {
			
			//devs = jQuery.parseJSON(developer);

			$.each( automation, function( i, auto ) {
				
				var $select = $('#automation');		
				$select.append('<option value="' + auto.automation + '">' + auto.automation + '</option>');
			
			});

		}
		  
		function populatePrivacyTypes(privacyTypes) {
			
			//devs = jQuery.parseJSON(developer);

			$.each( privacyTypes, function( i, privacyType ) {
				
				var $select = $('#privacy_type');		
				$select.append('<option value="' + privacyType.privacy_type + '">' + privacyType.privacy_type + '</option>');
			
			});

		}
		  
		function populateRequesters(requestersHTML) {
			
			//devs = jQuery.parseJSON(developer);
			var $select = $('#requester');
			$select.append(requestersHTML);
			//$.each( requesters, function( i, requester ) {
				//$select.append('<option value="' + requester.id + '">' + requester.last_name + ", " + requester.first_name + " | " + requester.title  + " | " + requester.dept_name + '</option>');
				//$select.append($("<option />").val(requester.id).text(requester.last_name + ", " + requester.first_name + " | " + requester.title  + " | " + requester.dept_name));
			//});

		}
		  
		function addApprovals(sn, custodian, cust_table, sn_code) {
			var return_val = "";
     		$.ajax({
				url: 'addApproval',
				type: "POST",					
				data: { sn: sn, custodian: custodian, cust_table: cust_table, sn_code: sn_code, csrf_val: csrf_val },
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					return_val = data;
					//console.log("addApprovals data: " + data);
					//console.log('successfully added custodian: ' +  custodian + ', table: ' + cust_table);
				}
			});	
		}
		
		function enableCommit() {
	    	if (!changed) { 
	    		$('#commit_butt').css('background','lightgreen');
	    		$('#commit_butt').prop('disabled',false);
	    	}
	        changed = true; 	
		}
		  
		function disableCommit() {			
	        changed = false; 	
			$('#commit_butt' ).css('background','#cdcdcd');
	    	$('#commit_butt').prop('disabled',true);
		}
		  
		var raceCondition = false;
		
		$.ajax({
			url:   'getProjects',
					type: 'POST',
					//contentType: "application/json; charset=utf-8",
					cache: false,
					data: { 
						
						username: "${loggedInUser}", 
						
						projectCode: $('#code_search').val(), 
						
						requester: $('#requester_search').val(), 
						developer: "${loggedInUser}", 
						description: $('#description_search').val(), 
						completionStatus: "010000",
												
						irb_waiver: $('#irb_waiver_search').val(),
						cross_ref: $('#cross_ref_search').val(),
						purpose_of_req: $('#purpose_of_req_search').val(),
						delivery_plan: $('#delivery_plan_search').val(),
						
						//criteria: $('#criteria_search').val(),
						technical_specs: $('#technical_specs_search').val(),
						test_plan: $('#test_plan_search').val(),
						sql: $('#sql_search').val(),
						any: $('#any_search').val(),
						
						and_or: $('#and_or_search').val(),
						
						csrf_val: csrf_val

					},
					beforeSend: function( xhr ) {
						
					},
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
												
						projectList = jQuery.parseJSON(data);
						
						populateProjectList(projectList);
						
						if (raceCondition) {
							//console.log('race condition');
							populateProjectInfo(projectList[0].code, projectList[0].serial_number, "All");
						}
						
					}
		});

		$.ajax({
			url:   'initialRetrieve',
					type: 'POST',
					cache: false,
					data:{  username: "${loggedInUser}"
					},
					beforeSend: function( xhr ) {
						
					},
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						
						//console.log("back from db - initial");
						
						developers = jQuery.parseJSON(data);
						
						requestersHTML = developers.splice(-1);
						
						requesters = developers.splice(-1);
						
						privacyTypes = developers.splice(-1);
						
						automation = developers.splice(-1);
						
						directories = developers.splice(-1)[0];
						
						//This line is slow especially in IE
						populateRequesters(requestersHTML);
																		
						populatePrivacyTypes(privacyTypes[0]);
						
						populateDevelopers(developers[0]);
						
						populateAutomation(automation[0]);
						
						//console.log(typeof projectList);
						if (typeof projectList === 'undefined') {
							raceCondition = true;
						} 
						else if (projectList[0] !== undefined) {
							populateProjectInfo(projectList[0].code, projectList[0].serial_number, "All");
						}
						
						$('.loader').hide();

					}
		});		
		
		
		  
	    var tabTitle = $( "#tab_title" ),
	      tabContent = $( "#tab_content" ),
	      tabTemplate = "<li><a href='#[href]'><img src='<%=request.getContextPath() %>/images/#[image]' class='tab-image' valign='middle'>#[label]</a> <span class='ui-icon ui-icon-close' role='presentation'>Remove Tab</span></li>",
	      tabCounter = 6;
	 
	    var tabs = $( "#tabs" ).tabs();
	    tabs.find( ".ui-tabs-nav" ).sortable({
	        axis: "x",
	        stop: function() {
	          tabs.tabs( "refresh" );
	        }
	    });
	 
	    // use window to make it global
	    window.addTab1 = function(label, image, v1) {
	    	//console.log("label: " + label + ", image: " + image + ", v1: " + v1 + ", tabsDictVis[" + label + "]: " + tabsDictVis[label] + ", tabsDict[" + label + "]: " + tabsDict[label]);
	        var id = "tabs-" + tabCounter,
	          //li = $( tabTemplate.replace( /#\[href\]/g, "#" + id ).replace( /#\[label\]/g, label ).replace( /#\[id\]/g, id ) ),
	          li = $( tabTemplate.replace( /#\[href\]/g, "#" + id ).replace( /#\[label\]/g, label ).replace( /#\[image\]/g, image ) ),
	          tabContentHtml;
	        
	        // tab prev loaded and not closed
	        if (tabsDictVis[label] == 'vis' && tabsDict[label] > 0) {
	        	//console.log("tab prev loaded and not closed");
	        	$( "#ui-id-" + tabsDict[label] ).trigger('click');
		        window.scrollTo(0, 0);
	        }
	        // tab prev loaded but closed
	        else if (tabsDictVis[label] == 'hid' && tabsDict[label] > 0) {
	        	// append tab with existing id (other instances simply add new tab with new id number) 
		        tabs.find( ".ui-tabs-nav" ).append( "<li><a href='#tabs-" + tabsDict[label] + "' id='ui-id-" + tabsDict[label] + "'><img src='<%=request.getContextPath() %>/images/" + image + "' class='tab-image' valign='middle'>" + label + "</a> <span class='ui-icon ui-icon-close' role='presentation'>Remove Tab</span></li>" );
		        //tabs.find( ".ui-tabs-nav" ).append( "<li><a href='#tabs-" + tabsDict[label] + "' id='ui-id-" + tabsDict[label] + "'>" + label + "</a> <span class='ui-icon ui-icon-close' role='presentation'>Remove Tab</span></li>" );
		        tabs.tabs( "refresh" );
		        if ($('#scheduler_button').val() == 'Create New Job') {
			        $('#project_code').val($('#sn_code').val());
		        }
		        //console.log("project_code: " + $('#project_code').val());
		        //console.log("sn_code: " + $('#sn_code').val());
		        $( "#ui-id-" + tabsDict[label] ).trigger('click');
		        window.scrollTo(0, 0);
	        }
	        else if (label == 'All Projects Report') {
	        	alert('Under Construction');
	        }
	        else {
	        	var url;
			    if (label == 'User Prefs') {
			    	url = 'preferences.html';
		        }
			    else if (label == 'Advanced Search') {
			    	url = 'preferences.html';
		        }
			    else if (label == 'Scheduler') {
			    	url = 'scheduler.html';
		        }
		        $.ajax({
					url: url,
					type: 'POST',
					//contentType: "application/json; charset=utf-8",
					data:{ username: "${loggedInUser}", v1: v1, csrf_val: csrf_val },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						tabContentHtml = data;
						
						//console.log(tabCounter);
				        tabs.find( ".ui-tabs-nav" ).append( li );
				        tabs.append( "<div id='" + id + "'><p>" + tabContentHtml + "</p></div>" );
				        tabs.tabs( "refresh" );
				        //console.log("#ui-id-" + tabCounter);
				        tabsDict[label] = tabCounter;
				        window.scrollTo(0, 0);
				        $( "#ui-id-" + tabCounter ).trigger('click');
				        tabCounter++;
						
					}
				});
	        }
	        tabsDictVis[label] = 'vis';
	   
	    }
	    
	    window.resetLayout = function() {
	    	
	    	$( "textarea#sql.ui-resizable" ).css({"width": "400", "height": "80"});
	    	$( "textarea#sql.ui-resizable" ).parent().css({"width": "406", "height": "86"});
	    	
	    	var elements = [ "description", "purpose_of_request", 
	    		"all_tables_textarea", "data_elements", "criteria", 
	    		"technical_specs", "test_plan", "project_notes", 
	    		"dev_notes", "request_description" ];
	    	
	    	$.each( elements, function( i, element ) {
		    	$( "textarea#" + element + ".ui-resizable" ).css({"width": "400", "height": "34"});
		    	$( "textarea#" + element + ".ui-resizable" ).parent().css({"width": "406", "height": "40"});
	    	});
	    	
	    	$( "textarea#amendment_note.ui-resizable" ).css({"width": "175", "height": "40"});
	    	$( "textarea#amendment_note.ui-resizable" ).parent().css({"width": "181", "height": "46"});
	    	
	    	$( "textarea#program_specs.ui-resizable" ).css({"width": "400", "height": "155"});
	    	$( "textarea#program_specs.ui-resizable" ).parent().css({"width": "406", "height": "161"});
	    	
	    	//$( "#delivery_plan_text" ).hide();
	    	
	    }
	    
	    // AddTab button: just opens the dialog
	    /*$( "#add_tab" )
	      .button()
	      .on( "click", function() {
	        dialog.dialog( "open" );
	      });
	    */
	    
	    // change color of commit button when changes occur
	    $('#sn_code, #received_by, #estimated_hours, #cross_ref, #irb_waiver, #request_description, #description, ' +
	    		'#purpose_of_request, #program_specs, #technical_specs, #test_plan, #project_notes, #dev_notes, #sql').keyup(function(e) {
	    	//console.log('change keyup');
	    	//console.log(e);
	    	// ignore ctrl-c (copy) key combos
	    	if ((e.ctrlKey && (e.key == "c" || e.key == "a")) || e.key == "Control" || typeof e.key == 'undefined' ) {
	    		
	    	}
	    	else {
		    	enableCommit();
	    	}
	    	//if (!changed) { $('#commit_butt' ).css('background','lightgreen'); }
	        //changed = true; 
	    }); 
	    
	    $('select:not("#amendment_developer, #and_or_search, #email_list, #excel_delimiter, #sql_database")').on('change', function() {
	    	//console.log('change select');
	    	enableCommit();
	    }); 
	    
	    // Close icon: removing the tab on click
	    tabs.on( "click", "span.ui-icon-close", function() {
	      var panelId = $( this ).closest( "li" ).remove().attr( "aria-controls" );
	      //console.log("tab close clicked");
	      // removed by me //
	      //$( "#" + panelId ).remove();
	      // removed by me //
	      
	      // added by me //
	      $ ( '#' + panelId ).hide();
	      var removeTabFromDict = $( this ).closest( "li" )[0].children[0].innerText.trim();
	      tabsDictVis[removeTabFromDict] = 'hid';
	      //delete tabsDict[removeTabFromDict];
	      // added by me //
	      
	      tabs.tabs( "refresh" );
	    });
	 
	    tabs.on( "keyup", function( event ) {
	      if ( event.altKey && event.keyCode === $.ui.keyCode.BACKSPACE ) {
	        var panelId = tabs.find( ".ui-tabs-active" ).remove().attr( "aria-controls" );
	        $( "#" + panelId ).remove();
	        tabs.tabs( "refresh" );
	      }
	    });
	    		  
	    
	    $( "#menu1" ).click(function() {
	        $('#menu1dd').toggle();
	        //$('#menu2dd').hide();
	    });
	    
	    /*$( "#menu2" ).click(function() {
	        $('#menu1dd').hide();
	        $('#menu2dd').toggle();
	    });*/
	    
	    $( "#menu1dd" ).menu();
	    //$( "#menu2dd" ).menu();
	
	    $( "#dd1-1" ).click(function() {
	    	var label = "Developer Dashboard";
	        addTab1(label, 'monitor-window-3d.png');
	        $('#menu1dd').hide();
	    });
	    
	    $( "#dd1-2" ).click(function() {
	    	var label = "Scheduler";
		    addTab1(label, 'calendar-month.png', '');
	        $('#menu1dd').hide();
	    });
	    
	    $( "#dd1-3" ).click(function() {
	    	var label = "User Prefs";
		    addTab1(label, 'user-business.png', '');
	        $('#menu1dd').hide();
	    });
	    
	    $( "#dd1-4, #layout_butt" ).click(function() {
	    	resetLayout();
	        //$('#menu1dd').hide();
	    });
	    
	    $( "#refresh_from_projects_butt" ).click(function() {
	    	
			$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
			$( "#dialog-message" ).dialog('option', 'title', 'Refresh Project from R2D3');
			$( "#dialog-message" ).html("<p>This will populate the current project with data from R2D3. Ok?</p>");
			$( "#dialog-message" ).dialog({
				buttons: {
				    "Yes": function() {
				      	$( this ).dialog( "close" );
			     		$.ajax({
							url: 'refreshProjectFromR2d3',
							type: "POST",					
							data: { sn: $('#sn').val(), csrf_val: csrf_val },
							error: function (jqXHR, textStatus, errorThrown ) {
								errorMsg(jqXHR.status, textStatus, errorThrown);
							},
							success: function (data) {
								project = jQuery.parseJSON(data);
								
								//console.log(project);
								
								//$('#amendments tr').remove();
								//$("#amendments").find("tr:gt(0)").remove();
								//$("#approvals").find("tr:gt(0)").remove();
															
								//$('#' + project.code).css('background-color','lightgreen');
								
								$('#sn').val(project.serial_number);
								$('#sn_code').val(project.code);
								$("#request_type").val(project.request_type);
								$("#received_date").val(project.request_date);
								$("#reqd_deliv_date").val(project.required_date);
								$("#est_delivery_dte").val(project.est_delivery_dte);
								$("#developer").val(project.developer_primary);
								$("#schedule_status").val(project.schedule_status);
								$("#priority").val(project.priority);
								$("#received_by").val(project.received_by);
								
								$("#start_date").val(project.start_date);
								$("#delivery_date").val(project.delivery_date);
								$("#status").val(project.status);
								$("#objs_delivered").val(project.objs_delivered);
								$("#second_developer").val(project.second_developer);
								$("#automation").val(project.automation);
								$("#estimated_hours").val(project.estimated_hours);
								
								$("#requester").val(project.requester_id);
								$("#requesterFirstName").val(project.requester_first_name);
								$("#requesterLastName").val(project.requester_last_name);
								$("#requesterName").val(project.requester_first_name + " " + project.requester_last_name);
								$("#requester_email").val(project.requester_email);
								$("#requester_title").val(project.requester_title);
								$("#request_description").val(project.request_description);
								$("#privacy_type").val(project.privacy_type);
								$("#irb_waiver").val(project.irb_waiver);
								$("#cross_ref").val(project.cross_ref);
								$("#description").val(project.description);
								$("#purpose_of_request").val(project.purpose_of_request);
								
								$("#data_elements").val(project.data_elements);
								$("#criteria").val(project.criteria);
								$("#program_specs").val(project.program_specs);
								
								//console.log("project.program_specs: " + project.program_specs);
								
								/*if (project.program_specs && project.program_specs.length > 0) {
									$("#delivery_plan").show();
									console.log("showing delivery plan");
								}
								else {
									$("#delivery_plan").hide();
								}*/
								
								$("#technical_specs").val(project.technical_specs);
								$("#test_plan").val(project.test_plan);
								$("#project_notes").val(project.project_notes);
								$("#dev_notes").val(project.dev_notes);
								$("#sql").val(project.sql);
								$("#email").val(project.email);

								$("#all_tables_textarea").val('');
								
								// following line calls code that
								// populates "Tables Used" textarea
							    $("#sql").trigger("keyup");
								
								enableCommit();

							}
						});				      	
				    },
				    Cancel: function() {
				      	$( this ).dialog( "close" );
				    }
				}
			});
			$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","yellow");
			
	    });
	    
	    $( "#excel_butt" ).click(function() {
	    	
	    	// return if sql field is blank
	    	if ( $('#sql').val().trim() == '' ) return;
	    	
			$('#excel_div').empty();
			$('.excel_loader').show();
			$('#dead_excel').hide();
	    	
	    	var form = $('#project_info_form')[0];
	    	
	    	var formData = {};
	        $.each(form, function(i, v){
	        	var input = $(v);
	        	//console.log( input );
	        	formData[input.attr("id")] = input.val();
			});
	        
	        formData["folder_path"] = directories[$('#sn_code').val().replace(/[^A-Za-z]/g, '')] + $('#sn_code').val();
	    	
	        /*var request = new XMLHttpRequest();
	        request.open('GET', 'downloadExcel', true);
	        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
	        request.responseType = 'blob';

	        request.onload = function(e) {
	            if (this.status === 200) {
	                var blob = this.response;
	                if(window.navigator.msSaveOrOpenBlob) {
	                    window.navigator.msSaveBlob(blob, fileName);
	                }
	                else{
	                    var downloadLink = window.document.createElement('a');
	                    var contentTypeHeader = request.getResponseHeader("Content-Type");
	                    downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: contentTypeHeader }));
	                    downloadLink.download = fileName;
	                    document.body.appendChild(downloadLink);
	                    downloadLink.click();
	                    document.body.removeChild(downloadLink);
	                   }
	               }
	           };
	         request.send();*/
	        
	        $.ajax({
				url: 'generateExcel',
				contentType: "application/json; charset=utf-8",
				type: "POST",
				data: JSON.stringify(formData),
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					$('.excel_loader').hide();
					if (data.substring(0, 5) != "error") {
						//$('#excel_div').append( "<a href='/DataLine/downloadExcel?projectCode=" + $('#sn_code').val() + "'>Download Excel</a><br><br>" );
					}
					else {
						console.log("CAUGHT ERROR");
						//$("#dead_excel_error").height(setHeight);
						$('#dead_excel_error').html('<br><br>' + data.substring(7));
						$('#dead_excel').show();
					}
				},
				error: function (jqXHR, textStatus, errorThrown ) {
					console.log("ERROR");
					$('#dead_excel').show();
					$('.excel_loader').hide();
				}
			});
	    });
	    
	    $( "#word_butt" ).click(function() {
	    	
	    	var form = $('#project_info_form')[0];
	    	
	    	var formData = {};
	        $.each(form, function(i, v){
	        	var input = $(v);
	        	//console.log( input );
	        	formData[input.attr("id")] = input.val();
			});
	        
	        formData["department_name"] = $('#' + $('#sn_code').val() + '_department_name').text();
	        //formData["department_name"] = $('#requester option:selected').text();
	        formData["reqd_deliv_date"] = $('#reqd_deliv_date').val() == '' ? 'Earliest Convenience' : $('#reqd_deliv_date').val();
	        formData["all_tables"] = $('#all_tables_textarea').val();
	        
	        var s = new Set();
	        $('#approvals tr').each(function(i,e){
	        	//console.log("In word_butt, adding: " + $(this).find("td:nth-child(3)").text().trim());
	        	if ($(this).find("td:nth-child(3)").text().trim() != "Custodian") {
		        	s.add($(this).find("td:nth-child(3)").text().trim());
	        	}
	    	});
	    	
	        c=1;
	    	s.forEach(function(e){
	    		//console.log("In word_butt, adding: " + e + ", custodian" + c);
	    		formData["custodian" + c] = e;
	    		c++;
	    	});
	    	
	    	for (; c <= 8; c++) {
	    		//console.log("In word_butt, adding: , custodian" + c);
	    		formData["custodian" + c] = "";
	    	}
	    	
	    	formData["folder_path"] = directories[$('#sn_code').val().replace(/[^A-Za-z]/g, '')] + $('#sn_code').val();
	    	
	        $.ajax({
				url: 'generateWord',
				contentType: "application/json; charset=utf-8",
				type: "POST",
				data: JSON.stringify(formData),
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					//$('.excel_loader').hide();
					var message, color;
					if (data == "file exists") {
						message = "<p>Project Plan not created. File already exists.</p>";
						color = "yellow";
					}
					else {
						message = "<p>Project Plan created successfully.</p>";
						color = "lightgreen";
					}
					$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
					$( "#dialog-message" ).html(message);
					$( "#dialog-message" ).dialog({
						buttons: {
						    "Ok": function() {
						      	$( this ).dialog( "close" );
						    }
						}
					});
					$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background",color);
				}
			});
	    });
	    
	    $( "#dd1-5" ).click(function() {
			//window.location = window.location.href;
			window.location = '/DataLine/logout.html';
	    });
	    
	    /*$( ".under-construction" ).click(function() {
	    	var label = "All Projects Report";
		    addTab1(label);
	        $('#menu2dd').hide();
	    });*/
	    
	    $( "#resetMap" ).click(function() {
			var latlng = {lat: 40.7731295, lng: -73.957734};
			map.setCenter(latlng);
			map.setZoom(4);
	    });
	    
	  	//This must be a hyperlink
	    $("#exportProjectSpreadsheet").on('click', function (event) {
	    	
	        exportTableToExcel.apply(this, ['#projectList', 'ProjectSummarySpreadsheet.xls']);
	        
	        // IF CSV, don't do event.preventDefault() or return false
	        // We actually need this to be a typical hyperlink
	    });
	    // CALLBACK FOR ANY FILTERING TO SPREADSHEET
	    /*$('#projectList').bind('filterEnd', function(event, config){
	    	// UPDATE NUMBER OF RECORDS SHOWN
	    	//$('#recordCount').val( $('#tbodyid tr').length - $('.filtered').length + ' records' );
	    	
	    	for (var x=0; x < 6; x++) {
		    	// UPDATE COLORS ON FILTERED FIELDS
		    	if ($(".tablesorter-filter[data-column='" + x + "']").val() == '') {
		    		$(".tablesorter-filter[data-column='" + x + "']").css('background-color', 'white');
		    	}
		    	else {
		    		$(".tablesorter-filter[data-column='" + x + "']").css('background-color', '#f7cfcf');
		    	}
		    }
	    });*/
	    
	    $(document).mouseup(function (e)
	    		{
	    		    var container = $( "#menu1dd" );
	    		    if (!container.is(e.target) // if the target of the click isn't the container...
	    		        && container.has(e.target).length === 0) // ... nor a descendant of the container
	    		    {
	    		        container.hide();
	    		    }
	    		    
	    		    /*container = $( "#menu2dd" );
	    		    if (!container.is(e.target) // if the target of the click isn't the container...
	    		        && container.has(e.target).length === 0) // ... nor a descendant of the container
	    		    {
	    		        container.hide();
	    		    }*/
	    		    
	    		});
	    
	    $(document).on('click','.projectCode',function(e){
	    	var result = true;
	    	if (changed) { 
	    		var result = confirm("Want to go without saving?");
	    	}
    		if (result) {
    			//console.log($(e.target).attr("id"));
    			populateProjectInfo($(e.target).text(), $(e.target).attr("id"), "All");
    		}	    		
	    });
	    
	    $(document).on('click','.projectCodeX',function(e){
	    	var result = true;
	    	if (changed) { 
	    		var result = confirm("Want to go without saving?");
	    	}
    		if (result) {
    			var id = $(e.target).attr("class").split(" ");
    			//console.log($(e.target).attr("class"));
    			//console.log(id[1]);

    			if (id.length > 1) {
    				populateProjectInfo('', id[1], "All");
    			}
    			
    		}	    		
	    });
	    
	    // on ENTER from search
	    $('.search').keyup(function(e){
	    	
    	    if(e.keyCode == 13) {
    	    	
    	    	$('.search_loader').show();
    	    	
    	    	//$(this).trigger("enterKey");
    	    	$("#projectList tr").remove();
    	    	
    	    	completionStatus = generateCompletionStatus();
    	    	
    	        $.ajax({
    				url: 'getProjects',
    				type: "POST",
    				//contentType: "application/json; charset=utf-8",
    				data: { username: "${loggedInUser}", 
    					
    						projectCode: $('#code_search').val(), 
    						requester: $('#requester_search').val(), 
    						developer: $('#developer_search').val(), 
    						description: $('#description_search').val(), 
    						completionStatus: completionStatus,
    						
    						irb_waiver: $('#irb_waiver_search').val(),
    						cross_ref: $('#cross_ref_search').val(),
    						purpose_of_req: $('#purpose_of_req_search').val(),
    						delivery_plan: $('#delivery_plan_search').val(),
    						
    						//criteria: $('#criteria_search').val(),
    						technical_specs: $('#technical_specs_search').val(),
    						test_plan: $('#test_plan_search').val(),
    						sql: $('#sql_search').val(),
    						any: $('#any_search').val(),
    						
    						and_or: $('#and_or_search').val(),
    						
    						csrf_val: csrf_val
    						
    					 },
 						error: function (jqXHR, textStatus, errorThrown ) {
 							errorMsg(jqXHR.status, textStatus, errorThrown);
 						},
    				success: function (data) {
    					$('.search_loader').hide();
						projectList = jQuery.parseJSON(data);
						populateProjectList(projectList);
    				}
    			});
    	        
    	    	if ( $( '#' + e.target.id ).val().length > 0 ) {
    	    		$( '#' + e.target.id ).css({'background-color' : 'LightGreen'});
    	    		$('.projectCodeX.' + e.target.id).css('background-color', 'lightgreen');
    	    	}
    	    	else {
    	    		$( '#' + e.target.id ).css({'background-color' : 'white'});
    	    		$('.projectCodeX.' + e.target.id).css('background-color', 'white');
    	    	}
    	        
    	    }
    	    
    	    else {
    	    	
    	    	var searchCol = 1;
    	    	if (e.target.id == "requester_search") {
    	    		searchCol = 2;
    	    	}
    	    	else if (e.target.id == "developer_search") {
    	    		searchCol = 3;
    	    	}
    	    	else if (e.target.id == "description_search") {
    	    		searchCol = 4;
    	    	}
    	    	else {
    	    		searchCol = 99;
    	    	}
    	    	//console.log(searchCol);
    	    	if (searchCol <= 4) {
        	        $("#projectList tr").each(function(index) {

       	                $row = $(this);
       	                
       	                // only check visible rows
       	                if ($(this).is(":visible") ) {
       	                	
           	                //var id = $row.find("td:" + searchCol).text();
           	                var id = $row.find("td:nth-child(" + searchCol + ")").text().toLowerCase();
           	                
           	                if (id.indexOf(e.target.value.toLowerCase()) >= 0) {
           	                    $(this).show();
           	                }
           	                else {
           	                    $(this).hide();
           	                }
           	                
       	                }
       	                // if invisible row and the keypress is delete
       	                // it may need to be shown
       	                else {
       	                	
       	                	if (e.keyCode == 46 || e.keyCode == 8) {
               	                var id = $row.find("td:nth-child(" + searchCol + ")").text().toLowerCase();
       	                		
       	                		// if this column matches - check all other columns
               	                if (id.indexOf(e.target.value.toLowerCase()) >= 0) {
               	                	
               	                	id = $row.find("td:nth-child(1)").text().toLowerCase();
               	                	if (id.indexOf( $('#code_search').val().toLowerCase() ) >= 0) {
               	                		
               	                		id = $row.find("td:nth-child(2)").text().toLowerCase();
               	                		if (id.indexOf( $('#requester_search').val().toLowerCase() ) >= 0) {
               	                			
                   	                		id = $row.find("td:nth-child(3)").text().toLowerCase();
                   	                		if (id.indexOf( $('#developer_search').val().toLowerCase() ) >= 0) {
                   	                			
                       	                		id = $row.find("td:nth-child(4)").text().toLowerCase();
                       	                		if (id.indexOf( $('#description_search').val().toLowerCase() ) >= 0) {
                       	                			$(this).show();
                       	                		}
                   	                		}
               	                		}
               	                	}
               	                }
               	            }
       	                }    	                	
        	        });
    	    	}
    	    	var c = $("#projectList tr:visible").length;
    	    	if (c == 100) c = '100+'
    	    	$('#projectCount').val(c);
    	    }
    	});

	    $('.search').blur(function(e){
	    	
	    	if ( $( '#' + e.target.id ).val().length > 0 ) {
	    		$( '#' + e.target.id ).css({'background' : 'LightGreen', 'background-size': '', 'background-clip': '', 'background-origin': ''});
	    	}
	    	else {
	    		$( '#' + e.target.id ).css({'background' : ''});
	    	}
	    	
	    });
	    
	    /*$('.checkfilters').change(function(){

	        if($(this).is(':checked')){
	            $(this)[0].parentElement.style.backgroundColor='lightgreen';
	        }
	        else
	        {
	            $(this)[0].parentElement.style.backgroundColor='#ebf2fa';
	        }
	        
	        completionStatus = generateCompletionStatus();
	        
	        $.ajax({
				url: 'getProjects',
				contentType: "application/json; charset=utf-8",
				data:{  username: "${loggedInUser}", projectCode: $('#code_search').val(), requester: $('#requester_search').val(), developer: $('#developer_search').val(), description: $('#description_search').val(), completionStatus: completionStatus },
				success: function (data) {
					projectList = jQuery.parseJSON(data);
					$("#projectList tr").remove();
					populateProjectList(projectList);
				}
			});

	    });*/

	    $(document).on('click','#open_folder', function(e){
	    	
			var projectCodeSearchText = $('#code_search').val().trim(); 
			var projectDept = $('#code_search').val().replace(/[^A-Za-z]/g, '');
			
			// IF DEPT NAME IS IN SEARCH FIELD
			if (directories[projectDept]) {
				$('<span class="folder" id="' + projectCodeSearchText + '" style="display:none;">').appendTo( "body" ).click();
			}
			// IF ONLY SN IS IN SEARCH FIELD
			else {
	    		$.ajax({
	    			url: 'searchSn',
	    			type: "POST",
					//contentType: "application/json; charset=utf-8",
	    			data: { sn: projectCodeSearchText.replace(/[A-Za-z]/g, '') },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
	    			success: function(data) {
	    				if (data == "no match") {
	    					//console.log("no match");
	    				}
	    				else {
	    					$('<span class="folder" id="' + data + '" style="display:none;">').appendTo( "body" ).click();
	    				}
   					} // success
	    		}); // ajax
			}
			
	    });
	    
	    $(document).on('click','.folder', function(e){

	    	var folderPath = directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id;
	    	
	    	console.log(folderPath);
	    	
	    	// copy the folder location to the clipboard only on non-IE browsers
	    	if (window.navigator.msSaveBlob) { /* IE 10+ */ } 
		   	else {
	    	
		    	var temp = $('#projectCount').val();
		    	
		   	  	var copyText = document.getElementById("projectCount");
		   	  	copyText.value = folderPath;
				copyText.select();
		    	document.execCommand('copy');
		    	
		        $('#projectCount').val(temp);
	        
		   	}
	    	
	    	console.log("FOLDER CLICKED!");
	    	console.log(e);
	    	console.log(e.target.id.replace(/[^A-Za-z]/g, ''));
	    	console.log(e.target.id);
	    	console.log(folderPath);
	    	
	    	var folder_prefix = e.target.id.replace(/[^A-Za-z]/g, '');
	    	
	    	if (folder_prefix == 'TBD') {
				$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
				$( "#dialog-message" ).html("<p>Project Folders for TBD Projects are not allowed.</p>");
				$( "#dialog-message" ).dialog({
					buttons: {
					    "Ok": function() {
					    	//console.log("create dir");
					      	$( this ).dialog( "close" );
					    },
					    Cancel: function() {
					    	//console.log("nothing");
					      	$( this ).dialog( "close" );
					    }
					}
				});
				$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","red");
	    	}
	    	
	    	else {
	    		
	    		console.log('e: ' + e + "is not equal to TBD");

		        $.ajax({
					url: 'determineIfFolderExists',
					cache: false,
					contentType: "application/json; charset=utf-8",
					data: { folder: directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						
						if (data == "dne") {
							$( "#dialog-message" ).dialog({ width: 900, height: "auto" });
							$( "#dialog-message" ).dialog('option', 'title', 'Create Project Directory');
							$( "#dialog-message" ).html("<p>Project directory does not currently exist. <br><br> Create?<br><br>" + directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id + "</p>");
							$( "#dialog-message" ).dialog({
								buttons: {
								    "Ok": function() {
								    	//console.log("create dir");
								      	$( this ).dialog( "close" );
								      	
								        $.ajax({
											url: 'createProjectFolder',
											contentType: "application/json; charset=utf-8",
											data: { folder: directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id },
											error: function (jqXHR, textStatus, errorThrown ) {
												errorMsg(jqXHR.status, textStatus, errorThrown);
											},
											success: function (data) {
												if (data == "ok") { 
											    	if (window.navigator.msSaveBlob) { // IE 10+
												    	window.open(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
												   	} 
												   	else {
												   		window.location.href = 'localexplorer:' + directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id;

												   		//alert(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						   								//window.open('http://www.cnn.com')
												   		//window.open(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
												   	}
													/*$( "#dialog-message" ).html("<p>Project directory created successfully.</p>");
													$( "#dialog-message" ).dialog({
														buttons: {
														    "Ok": function() {
														    	console.log("create dir");
														      	$( this ).dialog( "close" );
														    },
														    Cancel: function() {
														    	console.log("nothing");
														      	$( this ).dialog( "close" );
														    }
														}
													});
													$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","lightgreen");*/
												}
												else {
													$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
													$( "#dialog-message" ).html("<p>Project directory not created.</p>");
													$( "#dialog-message" ).dialog({
														buttons: {
														    "Ok": function() {
														    	//console.log("create dir");
														      	$( this ).dialog( "close" );
														    },
														    Cancel: function() {
														    	//console.log("nothing");
														      	$( this ).dialog( "close" );
														    }
														}
													});
													$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","red");
												}
	
												//console.log(data);
											},
											done : function(e) {
												//console.log("DONE");
											}
										});
								      	
								    },
								    Cancel: function() {
								    	//console.log("nothing");
								      	$( this ).dialog( "close" );
								    }
								}
							});
							$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","yellow");
						}
						else {
					    	if (window.navigator.msSaveBlob) { // IE 10+
						    	window.open(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						    	//window.navigator.msSaveOrOpenBlob(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						   	} 
						   	else {
						   		//alert(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						   		//window.open('http://www.cnn.com')
						   		//console.log('chrome');
						   		//console.log(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
								window.location.href = 'localexplorer:' + directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id;

						   		//console.log('file:' + directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						   		//window.open('file:' + directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						   		
						   		//window.open('localexplorer:' + directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id);
						   	}						
						}
	
						//console.log(data);
					},
					done : function(e) {
						//console.log("DONE");
					}
				});
	        
	    	} // end if / else e == 'TBD'
	        
		   	/*else {
		   		alert(directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id + " does not yet exist.");
		   	}*/
	    });
	    
	    var typingTimer;
	    var doneTypingInterval = 3000;

	    $( "#sql" ).keyup(function(){
	    	//console.log("typingTimer: " + typingTimer);
	    	//console.log("clearing timer");
	    	clearTimeout(typingTimer);
	    	//console.log("typingTimer: " + typingTimer);
	    	typingTimer = setTimeout(doneTyping, doneTypingInterval);
	    	//console.log("typingTimer KEYUP: " + typingTimer);
	    });
	    	    
	    function doneTyping() {
	    	var formData = {
	    			username: "${loggedInUser}",
	    			sqlCode: $('#sql').val(),
	    			currentTables: $('#all_tables_textarea').val()
	    	};
	    	
	    	// used POST because get has a string length limit
	        $.ajax({
				url: 'determineTablesFromSQLText',
				contentType: "application/json; charset=utf-8",
				//data:{  username: "${loggedInUser}", sqlCode: $('#sql').val() },
				data: JSON.stringify($('#sql').val()),
				type: "POST",
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status);
				},
				success: function (data) {
					if ($('#sql').val().length > 0) {
						populateTablesUsed(data);
						$('#all_tables_textarea').keyup();
					}
				},
				done : function(e) {
					//console.log("DONE");
				}
			});
	    }
	    
	    $(document).on('click','.infoLabel', function(e){
	    	
	    	//console.log($(this)[0].children[6].innerText);
	    	
	    	var next_td = $(this).closest('td').next('td').find('textarea,input[type=text]');	
	    	//console.log(next_td);
	    	
	    	if (next_td.length == 1 && next_td[0].id != 'sql' ) {
	    		$(this).closest('td').next('td').find('textarea,input[type=text]').filter(':visible:first').select();
	    		//console.log('next_td[0].id: ' + next_td[0].id);
	    		//console.log('dead_excel_error.text(): ' + $('#dead_excel_error').text());
	    	}
	    	else if (next_td.length > 0 && next_td[0].id == 'sql' && $('#dead_excel_error').text().indexOf('DB2 SQL Error') == -1) {
	    		$(this).closest('td').next('td').find('textarea,input[type=text]').filter(':visible:first').select();
	    	}
	    	else if (next_td.length == 2) {
	    		$(this).closest('td').next('td').find('textarea').select();
	    	}
	    	/*else {
	    		$('#requester_title').select();
	    	}*/
	    	
	    });
	    
	    $( "#all_tables" ).autocomplete({
	    	minLength: 3,
	    	position: { my : "left top", at: "left bottom" },
	    	source: function(request, response) {
	    		$.ajax({
	    			url: 'custodianTablesSearch',
	    			data: { term: request.term },
	    			dataType: "json",
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
	    			success: function(data) {
	    				response($.map(data, function(v,i) {
							    				return {
								    				label: v.label,
								    				value: v.label,
								    				id: v.id
								    			};
	    				})); // response
   					} // success
	    		}); // ajax
        	} // source
    	}); // autocomplete

	    $( "#cust_table" ).autocomplete({
	    	minLength: 3,
	    	source: function(request, response) {
	    		$.ajax({
	    			url: 'custodianTablesSearch',
	    			data: { term: request.term },
	    			dataType: "json",
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
	    			success: function(data) {
	    				response($.map(data, function(v,i) {
							    				return {
								    				label: v.label,
								    				value: v.label,
								    				id: v.id
								    			};
	    				})); // response
   					} // success
	    		}); // ajax
        	}, // source
            select: function( event, ui ) {
                //console.log( "Selected: " + ui.item.value + " aka " + ui.item.id );
                $( '#custodian' ).val(ui.item.id);
            }
    	}); // autocomplete

	    $( "#all_tables" ).on( "autocompleteselect", function( event, ui ) {
	    
	    	$('#all_tables_textarea').val( $('#all_tables_textarea').val() + ui.item.value + "\n" );
	    	$('#all_tables_textarea').keyup();
	    	$(this).val(''); 
	    	return false;
	    	
	    });
    	
	    /*$( "#all_tables" ).on( "autocompletechange", function( event, ui ) {
	    	$('#all_tables').val('poo');
	    });*/
	    
	    var typingTimer;                //timer identifier

	    $( "#all_tables_textarea" ).keyup(function(){
	    
	    	/*clearTimeout(typingTimer);
	    	var all_tables_textarea = $('#all_tables_textarea').val();
	    	var sn_code = $('#sn_code').val();
	    	//setTimeout(determineCustodians(all_tables_textarea, sn_code), 3000);
	    	
	    	setTimeout(function() {
	    		determineCustodians(all_tables_textarea, sn_code);
	    	}, 3000)*/
	    	
	    	determineCustodians($('#all_tables_textarea').val(), $('#sn_code').val());
	    });

	    function determineCustodians(all_tables_textarea, sn_code) {
	    	var postData = { "tableList": all_tables_textarea, "projectCode": sn_code, "csrf_val": csrf_val };
	    	
	    	//console.log('calling determineCustodians')
	    	
	    	// used POST because get has a string length limit
	        $.ajax({
				url: 'determineCustodians',
				contentType: "application/json; charset=utf-8",
				//data: { projectCode: $('#sn_code'), tableList: $('#all_tables_textarea').val() },
				//data: JSON.stringify($('#all_tables_textarea').val()),
				data: JSON.stringify(postData),
				type: "POST",
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					//console.log("data from determineCustodians: ");
					//console.log(data);
					// only call populateApprovals if the project code called before the timeout is equal to the current project code
					if (sn_code == $('#sn_code').val()) 
						populateApprovals(jQuery.parseJSON(data));
				},
				done : function(e) {
					//console.log("DONE");
				}
			});
	    }
	    
	    function checkIfRequesterChanged(sn_code) {
	    	
	    	//var sn_code = $('#sn_code').val();
	    	var requesterInProjectList = $('#' + $('#sn_code').val() + ' td:nth-child(2) .requester').text();
	    	//var requester_after_submit = $('#requester option:selected').text();
    		var reqNameInProjectInfo = $('#requester option:selected').text().split("|")[0];
    		var fName = reqNameInProjectInfo.substring(reqNameInProjectInfo.lastIndexOf(",") + 1).trim();
    		var lName = reqNameInProjectInfo.substring(0, reqNameInProjectInfo.lastIndexOf(",")).trim();
    		var requesterAfterSubmit = fName + " " + lName;
	    	
	    	//console.log("requester tr in project list: " + $('#' + $('#sn_code').val() + ' td:nth-child(2) .requester').text());
	    	
	    	if (requesterInProjectList === requesterAfterSubmit) { 
	    		//console.log("All good: " + requesterInProjectList + " != " + requesterAfterSubmit);
	    	}
	    	else {
	    		// string.substring(string.lastIndexOf("/") + 1);
	    		//console.log("Mismatch: " + requesterInProjectList + " != " + requesterAfterSubmit);
	    		$('#' + sn_code + ' td:nth-child(2) .requester').text( requesterAfterSubmit );
	    	}
	    	
	    }

	    $(document).on('click','.checkfilters_td', function(e){
	    	
	    	//console.log(e);
	    	
	    	//console.log($(this).closest('td')[0]);
	    	checkbox = $(this).closest('td').find('[type=checkbox]');
	    	
	    	if (e.target.className == "checkfilters_td") {
	    		checkbox.prop("checked", !checkbox.prop("checked"));
	    	}
	    	
	    	if(checkbox.is(':checked')){
	    		//console.log(checkbox[0].id);
	    		$(this).closest('td').css("background-color", "lightgreen");
	    		if (checkbox[0].id == 'unassigned') {
	    			$('#developer_search').val('');
	    			$('#developer_search').css({'background-color' : 'white'});
	    			//$('.search').keyup();
	    		}
	        }
	        else
	        {
	        	$(this).closest('td').css("background-color", "#ebf2fa");
	        }
	        
	        completionStatus = generateCompletionStatus();
	        
	        $.ajax({
				url: 'getProjects',
				//contentType: "application/json; charset=utf-8",
				type: "POST",
				data: {  
					
					username: "${loggedInUser}", 
					projectCode: $('#code_search').val(), 
					requester: $('#requester_search').val(), 
					developer: $('#developer_search').val(), 
					description: $('#description_search').val(), 
					completionStatus: completionStatus,
					
					irb_waiver: $('#irb_waiver_search').val(),
					cross_ref: $('#cross_ref_search').val(),
					purpose_of_req: $('#purpose_of_req_search').val(),
					delivery_plan: $('#delivery_plan_search').val(),
					
					//criteria: $('#criteria_search').val(),
					technical_specs: $('#technical_specs_search').val(),
					test_plan: $('#test_plan_search').val(),
					sql: $('#sql_search').val(),
					any: $('#any_search').val(),
					
					and_or: $('#and_or_search').val(),
					
					csrf_val: csrf_val
				
				},
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					projectList = jQuery.parseJSON(data);
					$("#projectList tr").remove();
					populateProjectList(projectList);
				}
			});

	    	
	    });
	    
	    $(document).on('click','.removeApprover', function(e){
	    	//console.log('start of removeApprover');
	    	//var this_row_first_td = $(this).closest('tr').find('td:first');
	    	//var this_row_second_td = $(this).closest('tr').find('td:nth-child(2)');
	    	//var this_row_third_td = $(this).closest('tr').find('td:nth-child(3)');

	    	//var next_row_first_td = $(this).closest('tr').next('tr').find('td:first');
	    	//var next_row_second_td = $(this).closest('tr').next('tr').find('td:first'); //.next('td');
	    	
	    	/*console.log("this row: " + this_row_first_td.html());
	    	console.log("this row 2: " + this_row_second_td.html());
	    	console.log("next row: " + next_row_second_td.html());
	    	console.log("next row len: " + next_row_first_td.html().length);*/
	    	
	    	// if next row is blank then must move name down after delete
	    	//if (typeof next_row_first_td.html() != "undefined" && next_row_first_td.html().length == 0) {
	    		
	    		//next_row_first_td.html( this_row_first_td.html() );
	    		
	    	//}
	    	
	    	var custodian = $(this).closest('tr').find('td:nth-child(3)').text();
	    	var cust_table = $(this).closest('tr').find('td:nth-child(4)').text();
	    	
	    	$(this).closest('tr').remove();
	    	
			var sn = $('#sn').val();
			var sn_code = $('#sn_code').val();
     		//var custodian = this_row_second_td.html();
     		//var cust_table = this_row_third_td.html();
	    	//var custodian = $(this).closest('tr').find('td:nth-child(3)');
	    	//var cust_table = $(this).closest('tr').find('td:nth-child(4)');

     		$.ajax({
				url: 'removeApproval',
				type: "POST",					
				data: { sn: sn, custodian: custodian, cust_table: cust_table, sn_code: sn_code, csrf_val: csrf_val },
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					//console.log('successfully removed custodian: ' +  custodian + ', table: ' + cust_table);
				}
			});

	    	
	    	
	    });
	    
	    $(document).on('submit', '#project_info_form', function(e) {
	    	
	    	e.preventDefault();
	    	
	    	var form = this;
	    	
	    	var formData = {};
	        $.each(form, function(i, v){
	        	var input = $(v);
	        	//console.log( input );
	        	formData[input.attr("id")] = input.val();
			});
	        
	        var custodianList = "";
	        var tableList = "";
	        $('#approvals tr').each(function(i,e){
	        	//console.log("In submit. $(this).find('td:nth-child(1)').html().trim() = " + $(this).find("td:nth-child(1)").html().trim());
	    	    if ( $(this).find("td:nth-child(3)").html().trim() != 'Custodian' ) {
	    	    	custodianList += $(this).find("td:nth-child(3)").html().trim() + '|';
	    	    	tableList += $(this).find("td:nth-child(4)").html().trim() + '|';
	    	    }
	    	})
	    	
	        /*$('#approvals tr td:first-child').each(function () {
	        	if ($(this).text() != 'Custodian') {
	        		console.log($(this).text());
	        		custodianList += $(this).text() + "|";
	        	}
	        });
	        
	        $('#approvals tr td:nth-child(2)').each(function () {
	        	if ($(this).text() != 'Table') {
	        		console.log($(this).text());
	        		tableList += $(this).text() + "|";
	        	}
	        });*/
	        formData["custodianList"] = custodianList.slice(0, -1);
	        formData["tableList"] = tableList.slice(0, -1);

	        //console.log(formData);
	        
	        if (!formData["sn"].length > 0) {
				$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
				$( "#dialog-message" ).dialog('option', 'title', 'Commit Issue');
				$( "#dialog-message" ).html( "Serial Number can not be blank." );
				$( "#dialog-message" ).dialog( "open" );
	        }
	        else {
	          $.ajax({
				url: 'projectCommit',
				contentType: "application/json; charset=utf-8",
				type: "POST",
				data: JSON.stringify(formData),
				error: function (jqXHR, textStatus, errorThrown ) {
					errorMsg(jqXHR.status, textStatus, errorThrown);
				},
				success: function (data) {
					var sn = $('#sn').val();
			    	var sn_code = $('#sn_code').val();
			    	/*var requester_before_submit = $('#' + $('#sn_code').val() + ' td:nth-child(2) .requester').text();
			    	//var requester_after_submit = $('#requester option:selected').text();
		    		var req_name = $('#requester option:selected').text().split("|")[0];
		    		var first_name = req_name.substring(req_name.lastIndexOf(",") + 1).trim();
		    		var last_name = req_name.substring(0, req_name.lastIndexOf(",")).trim();
		    		var requester_after_submit = first_name + " " + last_name;
			    	
			    	console.log("requester tr in project list: " + $('#' + $('#sn_code').val() + ' td:nth-child(2) .requester').text());
			    	
			    	if (requester_before_submit === requester_after_submit) { 
			    		console.log("All good: " + requester_before_submit + " != " + requester_after_submit);
			    	}
			    	else {
			    		// string.substring(string.lastIndexOf("/") + 1);
			    		console.log("Mismatch: " + requester_before_submit + " != " + requester_after_submit);
			    		$('#' + $('#sn_code').val() + ' td:nth-child(2) .requester').text( requester_after_submit );
			    	}*/
			    	
			    	checkIfRequesterChanged(sn_code);	
			    	populateProjectInfo(sn_code, sn, "All");
				}
			  });
	        }
	    	
	    });
	    
	    $(document).on('change', '#version', function(e) {
	    	
	    	var sn_code = $('#sn_code').val();
	    	var sn = $('#sn').val();
	    	var version = $('#version').val();
	    	
	    	populateProjectInfo(sn_code, sn, version);
	    	
	    });
	    
	    $(document).on('click', '#add_amendment', function(e) {
	    	
			var sn = $('#sn').val();
     		var amendment_developer = $('#amendment_developer').val();
     		var amendment_date = $('#amendment_date').val();
     		var amendment_note = $('#amendment_note').val();
     		
     		if (amendment_developer != "Developer") {
     			
         		$.ajax({
    				url: 'addAmendment',
    				type: "POST",					
    				data: { sn: sn, amendment_developer: amendment_developer, amendment_date: amendment_date, amendment_note: amendment_note, csrf_val: csrf_val },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
    				success: function (data) {
    			    	var sn_code = $('#sn_code').val();
    			    	var sn = $('#sn').val();
    			    	var version = $('#version').val();
    			    	
    			    	populateAmendments(sn_code, sn, version);
    			    	$('#amendment_developer').val('Developer');
    			    	$('#amendment_date').val('');
    			    	$('#amendment_note').val('');
    				}
    			});
         		
     		}
     		
	    });
	    
	    $(document).on('click','.removeAmendment', function(e){
	    	
	    	var amendmentID = $(this).val();
	    	
			$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
			$( "#dialog-message" ).dialog('option', 'title', 'Delete Amendment');
			$( "#dialog-message" ).html("<p>Are you sure you want to delete this amendment?</p>");
			$( "#dialog-message" ).dialog({
				buttons: {
				    "Yes": function() {
				      	$( this ).dialog( "close" );
			     		$.ajax({
							url: 'removeAmendment',
							type: "POST",					
							data: { id: amendmentID },
							error: function (jqXHR, textStatus, errorThrown ) {
								errorMsg(jqXHR.status, textStatus, errorThrown);
							},
							success: function (data) {
								$('table#amendments tr#' + amendmentID).remove();
							}
						});				      	
				    },
				    Cancel: function() {
				      	$( this ).dialog( "close" );
				    }
				}
			});
			$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","yellow");

	    });
	    
	    /*$(document).on('click','.plan', function(e){
	    	console.log(e.target.id);
	        $.ajax({
				url: 'createPlan',
				contentType: "application/json; charset=utf-8",
				data:{ projectCode: e.target.id, path: directories[e.target.id.replace(/[^A-Za-z]/g, '')] + e.target.id },
				success: function (data) {
					$( "#dialog-message" ).dialog('option', 'title', 'Create Project Plan');
					$( "#dialog-message" ).html("<p>File was created successfully.</p>");
					$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","lightgreen");
				}
			});
	    });*/
	    
	    $( "#custodian" ).autocomplete({
	    	minLength: 3,
	    	source: function(request, response) {
	    		$.ajax({
	    			url: 'custodianSearch',
	    			type: 'POST',
	    			data: { term: request.term },
	    			dataType: "json",
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
	    			success: function(data) {
	    				response($.map(data, function(v,i) {
							    				return {
								    				label: v.label,
								    				value: v.label,
								    				id: v.id
								    			};
	    				})); // response
   					} // success
	    		}); // ajax
        	} // source
    	}); // autocomplete

	    $(document).on('click', '#add_approval', function(e) {
	    	
	    	var exists = false;
	    	$('#approvals tr').each(function(i,e){
	    		//console.log($(this).find("td:nth-child(3)").html().trim());
	    		//console.log($( '#custodian' ).val().trim());
	    		//console.log();
	    		//console.log($(this).find($(this).find("td:nth-child(4)").html().trim()));
	    		//console.log($( '#cust_table' ).val().trim());
	    	    if ( $(this).find("td:nth-child(3)").html().trim() == $( '#custodian' ).val().trim() && $(this).find("td:nth-child(4)").html().trim() == $( '#cust_table' ).val().trim() )
	    	    	exists = true;
	    	})
	    	
	    	// only call add approver if custodian name has a value
	    	if (  !exists && $( '#custodian' ).val().trim().length > 0 ) {
				var sn = $('#sn').val();
				var sn_code = $('#sn_code').val();
	     		var custodian = $('#custodian').val();
	     		var cust_table = $('#cust_table').val();
	     		    		
	     		$.ajax({
					url: 'addApproval',
					type: "POST",					
					data: { sn: sn, custodian: custodian, cust_table: cust_table, sn_code: sn_code, csrf_val: csrf_val },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
				    	if ( data != "false" ) {
							$('<tr class="manual">').append(
					                $('<td style="background:white;text-align:center;font-weight:bold;color:red;">').html("N"),
					                $('<td class="override" style="background:white;text-align:center;font-weight:bold;color:red;">').html("N"),
					                $('<td style="background:white;text-align:left;min-width:150px;font-weight:normal;">').html( custodian ),
					                $('<td style="background:white;text-align:left;min-width:150px;font-weight:normal;">').html( cust_table ),
					                $('<td style="background:white;text-align:left;">').html("<button class='removeApprover' style='vertical-align:middle;margin-bottom:0px;background:lightgrey;color:red;font-weight:bold;'>-</button>"),
					                $('<td style="display:none;">').html( data )
							).appendTo('#approvals');
				    	}
					}
				});	

	    	}
     		
     		$( '#cust_table' ).val('');
     		$( '#custodian' ).val('');
     		
	    });
	    
	    $(document).on('change', '#requester', function(e) {
	    	
	    	$('#requester_title').val( $('#requester option:selected').text().split("|")[1] );
     		
	    });
	    
	    $(document).on('click', '#delete_version_butt', function(e) {
	    	
			$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
			$( "#dialog-message" ).dialog('option', 'title', 'Delete Version');
			$( "#dialog-message" ).html("<p>Are you sure you want to delete this version?<br><br>" + $('#sn_code').val() + "<br><br>" + $('#version option:selected').text() + "</p>");
			$( "#dialog-message" ).dialog({
				buttons: {
				    "Yes": function() {
				    	//console.log("delete");
				      	$( this ).dialog( "close" );
				      	
				        $.ajax({
							url: 'deleteVersion',
							type: "POST",					
							data:{ projectCode: $('#sn_code').val(), sn: $('#sn').val(), version: $('#version option:selected').text() },
							error: function (jqXHR, textStatus, errorThrown ) {
								errorMsg(jqXHR.status, textStatus, errorThrown);
							},
							success: function (data) {
								populateProjectInfo($('#sn_code').val(), $('#sn').val(), "All");
								
								$( "#dialog-message" ).html(data);
								$( "#dialog-message" ).dialog({buttons: { "Ok": function() { checkIfRequesterChanged($('#sn_code').val()); $( this ).dialog( "close" ); } } });
								$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","tomato");
							},
							error: function(e) {
								///console.log("ERROR: ", e);
							},
							done : function(e) {
								//console.log("DONE");
							}
						});
				      	
				    },
				    Cancel: function() {
				    	//console.log("nothing");
				      	$( this ).dialog( "close" );
				    }
				}
			});
			$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","yellow");

	    });
    	
	    $(document).on('click', '#send_email', function(e) {
	    	
	    	//console.log("send_email");
	    	
	    	var emailTo = "";
	    	
	    	//console.log("email address: " + $('input[name=email_radio]:checked').val());
	    	switch($('input[name=email_radio]:checked').val()) {
	    	
	    	case "request_received":
	    		emailTo = $('#email').val();
	    		break;
	    	case "first_contact":
	    		emailTo = $('#email').val();
	    		break;
	    	case "requester":
	    		emailTo = $('#email').val();
	    		break;
	    	case "custodians":
		        $('#approvals tr td:nth-child(3)').each(function () {
		        	if ($(this).text().length > 2 && $(this).text() != 'Custodian') {
		        		emailTo += $(this).text().trim() + "|";
		        	}
		        });
	    		break;
	    	case "requester_and_custodians":
	    		console.log('requester_and_custodians');
	    		console.log($('#requester_email').val());
	    		emailTo = $('#requester_email').val() + "|";
		        $('#approvals tr td:nth-child(3)').each(function () {
		        	if ($(this).text().length > 2 && $(this).text() != 'Custodian') {
		        		emailTo += $(this).text().trim() + "|";
		        	}
		        });
	    		break;
	    	case "delivery":
	    		emailTo = $('#email').val();
	    		//window.location = 'mailto:' + emailTo + '?subject=Delivery of DataLine Request ' + $('#sn_code').val() + '&body=' + $('#requesterName').val() + ',%0D%0A%0D%0A' +
	    		//		'Please see the attached results to DataLine request ' + $('#sn_code').val() + '. Let us know if you have any questions or concerns.%0D%0A%0D%0A' +
	    		//		'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		$('#send_outlook').trigger('click');
	    		return;
	    		break;
    		default:
    			emailTo = "";
	    	
	    	}
	    	
	    	if ($('input[name=email_radio]:checked').val() != "delivery") {
		    	
		    	var form = $('#project_info_form')[0];
		    	
		    	var formData = {};
		        $.each(form, function(i, v){
		        	var input = $(v);
		        	//console.log( input );
		        	formData[input.attr("id")] = input.val();
				});
		        formData["emailTo"] = emailTo;
		        console.log("emailTo: " + emailTo);
		        formData["developerFromAddress"] = '${developerFromAddress}';
		        formData["emailType"] = $('input[name=email_radio]:checked').val();
		        formData["requesterDepartment"] = $('#requester option:selected').text().split("|")[2];
		        //formData[]
	
		        //console.log("emailTo: " + emailTo);
		        //console.log("emailTo length: " + emailTo.length);
		        
		        $.ajax({
					url: 'sendEmailDialog',
					contentType: "application/json; charset=utf-8",
					type: "POST",					
					data:  /*to: emailTo, 
							projectCode: $('#sn_code').val(), 
							type: $('input[name=email_radio]:checked').val(),*/ 
							JSON.stringify(formData)
					,
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						//console.log(data);
						//window.location.href = data;
						$( "#dialog-message" ).dialog({ width: 900, height: "auto" });
						$( "#dialog-message" ).dialog('option', 'title', 'Send Email');
						$( "#dialog-message" ).html(data);
						$( "#dialog-message" ).dialog({buttons: {
				            "Send": function() {
				            	formData["sendToPrivacy"] = $('#privacy_checkbox_dialog').prop('checked');
				            	formData["emailFrom"] = $('#email_from').val();
				            	formData["emailTo"] = $('#email_to').val();
				            	formData["emailCc"] = $('#email_cc').val();
				            	formData["emailBcc"] = $('#email_bcc').val();
				            	formData["emailSubject"] = $('#email_subject').val();
				            	formData["emailBody"] = $('#email_body').val();
				    	        $.ajax({
				    				url: 'sendEmailDialogMessage',
				    				contentType: "application/json; charset=utf-8",
				    				type: "POST",					
				    				data:  /*to: emailTo, 
				    						projectCode: $('#sn_code').val(), 
				    						type: $('input[name=email_radio]:checked').val(),*/ 
				    						JSON.stringify(formData)
				    				,
				    				error: function (jqXHR, textStatus, errorThrown ) {
				    					errorMsg(jqXHR.status, textStatus, errorThrown);
				    				},
				    				success: function (data) {
				    					//console.log(data);
				    					//window.location.href = data;
										$( "#dialog-message" ).html("<p>Email sent.</p>");
										$( "#dialog-message" ).dialog({
											buttons: {
											    "Ok": function() {
											    	$( this ).dialog( "close" );
											    },
											    Cancel: function() {
											    	$( this ).dialog( "close" );
											    }
											}
										});
										$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","lightgreen");
										populateProjectInfo($('#sn_code').val(), $('#sn').val(), "All");
				    				},
				    				done : function(e) {
				    					//console.log("DONE");
				    				}
				    			});
	
					              $( this ).dialog( "close" );
					            },
					        Cancel: function() {
					              $( this ).dialog( "close" );
					            }
						} });
						$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","rgb(94, 158, 214)");
						
						//$( "textarea" ).resizable({ handles: "se" });
						$("#email_body").resizable({ handles: "se" });
		
					},
					error: function(e) {
						//console.log("ERROR: ", e);
					},
					done : function(e) {
						//console.log("DONE");
					}
				});
	        
	    	}
	    	
	    });
	        	
	    $(document).on('click', '#send_outlook', function(e) {
	    	
	    	//console.log("send_email");
	    	
	    	var emailTo = "";
	    	var subject = "";
	    	var message = "";
	    	var project_info_text = 'Request Description:%0D%0A' + $('#request_description').val() + '%0D%0A%0D%0A' +
			'Request Purpose:%0D%0A' + $('#purpose_of_request').val() + '%0D%0A%0D%0A' +				
			$('#program_specs').val();	
	    	
	    	project_info_text = project_info_text.replace(/\r?\n/g, '%0D%0A').replace(/#/g, '%23').replace(/&/g, '%26').replace(/"/g, '%22');

	    	
	    	//console.log("email address: " + $('input[name=email_radio]:checked').val());
	    	switch($('input[name=email_radio]:checked').val()) {
	    	
	    	case "request_received":
	    		emailTo = $('#email').val();
	    		subject = "DataLine Request " + $('#sn_code').val();
	    		message = $('#requesterName').val() + ',%0D%0A%0D%0A' +
				'We have received your DataLine request and assigned it project code ' + $('#sn_code').val() + '. The analyst who will be handling your request will contact you within the next 5 business days. If you require more immediate assistance, please contact data@mskcc.org.%0D%0A%0D%0A' +
				'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		break;
	    	case "first_contact":
	    		emailTo = $('#email').val();
	    		subject = "RE: New DataLine Request " + $('#sn_code').val();
	    		message = $('#requesterName').val() + ',%0D%0A%0D%0A' +
				'I have been assigned to your recent DataLine request ' + $('#sn_code').val() + '. I will be in touch shortly with next steps.%0D%0A%0D%0A' +
				project_info_text  + '%0D%0A%0D%0A' +
				'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		console.log("message: " + message);
	    		console.log("project_info_text: " + project_info_text);
	    		break;
	    	case "requester":
	    		emailTo = $('#email').val();
	    		subject = "DataLine Plan " + $('#sn_code').val() + " for your approval";
	    		message = $('#requesterName').val() + ',%0D%0A%0D%0A' +
				'Please see the attached DataLine request plan ' + $('#sn_code').val() + ' for your approval.%0D%0A%0D%0A' +
				'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		break;
	    	case "custodians":
	    		subject = "DataLine Plan " + $('#sn_code').val() + " for your approval";
		    	var custodians = new Set();
		        $('#approvals tr').each(function(i, e){
		        	var $tds = $(this).find('td'), custodian = $tds.eq(2).text().trim().toLowerCase(), custodian_email = $tds.eq(5).text().trim().toLowerCase();
		        	if (custodian != 'michael lyublinsky') custodians.add(custodian_email);
		        	if (custodian == 'terry mcallister') { custodians.add('reynoldr@mskcc.org'); custodians.add('harringk@mskcc.org'); }
		    	});
		        if ($('#privacy_checkbox').prop('checked'))
			        custodians.add('privacy@mskcc.org');
		        
		        custodians.forEach(function(e){
		        	emailTo += e + "; "
		    	});
	    		message = 'All,%0D%0A%0D%0A' +
				'Please see the attached DataLine plan ' + $('#sn_code').val() + ' for your approval.%0D%0A%0D%0A' +
				'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		break;
	    	case "requester_and_custodians":
	    		emailTo = $('#email').val();
	    		subject = "DataLine Plan " + $('#sn_code').val() + " for your approval";
		    	var custodians = new Set();
		        $('#approvals tr').each(function(i, e){
		        	var $tds = $(this).find('td'), custodian = $tds.eq(2).text().trim().toLowerCase(), custodian_email = $tds.eq(5).text().trim().toLowerCase();
		        	if (custodian != 'michael lyublinsky') custodians.add(custodian_email);
		        	if (custodian == 'terry mcallister') { custodians.add('reynoldr@mskcc.org'); custodians.add('harringk@mskcc.org'); }
		    	});
		        if ($('#privacy_checkbox').prop('checked'))
			        custodians.add('privacy@mskcc.org');
		        
		        custodians.forEach(function(e){
		        	emailTo += e + "; "
		    	});
	    		subject = "DataLine Plan " + $('#sn_code').val() + " for your approval";
	    		message = 'All,%0D%0A%0D%0A' +
				'Please see the attached DataLine plan ' + $('#sn_code').val() + ' for your approval.%0D%0A%0D%0A' +
				'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		break;
	    	case "delivery":
	    		emailTo = $('#email').val();
	    		subject = "Delivery of DataLine Request " + $('#sn_code').val();
	    		message = $('#requesterName').val() + ',%0D%0A%0D%0A' +
				'Please see the attached results to DataLine request ' + $('#sn_code').val() + '. Let us know if you have any questions or concerns.%0D%0A%0D%0A' +
				'Thanks,%0D%0A' + $('#developerName').val() + '%0D%0A' + $('#developerEmail').val();
	    		break;
    		default:
    			emailTo = "";
	    	
	    	}
	    	
			var settings = {
					url: 'sendOutlookUserPrefs',
					type: 'POST',
					cache: false,
					data:{ message_type: $('input[name=email_radio]:checked').val(), csrf_val: csrf_val },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(158, textStatus, errorThrown);
					},
					success: function (data) {
						console.log("requester first name: " + $('#requesterFirstName').val());
						message = (data == "" ? message : data.replace(/\<requester_first_name\>/g, $('#requesterFirstName').val()).replace(/\<requester_last_name\>/g, $('#requesterLastName').val()).replace(/\<requester_full_name\>/g, $('#requesterName').val()).replace(/\<project_code\>/g, $('#sn_code').val()).replace(/\<project_info\>/g, project_info_text));
						console.log('mailto:' + emailTo + '?subject=' + subject + '&body=' + message);
						var t = 'mailto:' + emailTo + '?subject=' + subject + '&body=' + message;
						console.log(t.length);
						window.location = 'mailto:' + emailTo + '?subject=' + subject + '&body=' + message;
					}
			};
			
			$.ajax(settings);

	    });
	        	
	    $(document).on('click', '#advanced_search_arrow', function(e) {
	    	var current_text = $('#advanced_search_arrow').text();
			var action = "";
	    	if (current_text.charCodeAt(0) == 9658) {
	    		$('#advanced_search_arrow').html('&#x25BC;').text();
	    		// action = "open";
	    	}
	    	else {
	    		$('#advanced_search_arrow').html('&#x25BA;').text();
	    		// action = "close";
	    		$('#any_search').val('');
	    		
	    		$('#irb_waiver_search').val('');
	    		$('#cross_ref_search').val('');
	    		$('#purpose_of_req_search').val('');
	    		$('#delivery_plan_search').val('');

	    		//$('#criteria_search').val('');
	    		$('#technical_specs_search').val('');
	    		$('#test_plan_search').val('');
	    		$('#sql_search').val('');
	    	}
	    	
    		$( '#projectListAdvancedSearch' ).toggle();

	    });
	    
	    $(document).on('click', '#copy_butt', function(e) {
	    	
	    	//var form = this;
	    	var form = $('#project_info_form')[0];
	    	
	    	var formData = {};
	        $.each(form, function(i, v){
	        	var input = $(v);
	        	//console.log( input );
	        	formData[input.attr("id")] = input.val();
			});
	        
	        var custodianList = "";
	        var tableList = "";
	        $('#approvals tr').each(function(i,e){
	    	    if ( $(this).find("td:nth-child(3)").html().trim() != 'Custodian' ) {
	    	    	custodianList += $(this).find("td:nth-child(3)").html().trim() + '|';
	    	    	tableList += $(this).find("td:nth-child(4)").html().trim() + '|';
	    	    }
	    	})
	    	
	        formData["custodianList"] = custodianList.slice(0, -1);
	        formData["tableList"] = tableList.slice(0, -1);

	        //console.log(formData);
	        
	        if (!formData["sn"].length > 0) {
				$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
				$( "#dialog-message" ).dialog('option', 'title', 'Commit Issue');
				$( "#dialog-message" ).html( "Serial Number can not be blank." );
				$( "#dialog-message" ).dialog( "open" );
	        }
	        else {
				$( "#dialog-message" ).dialog({ width: 900, height: "auto" });
				$( "#dialog-message" ).dialog('option', 'title', 'Copy Project as New');
				$( "#dialog-message" ).html("<p>Are you sure you'd like to copy the current data as a new project?</p>");
				$( "#dialog-message" ).dialog({
					buttons: {
					    "Ok": function() {
					    	//console.log("create dir");
					      	$( this ).dialog( "close" );
					        $.ajax({
									url: 'projectCopy',
									contentType: "application/json; charset=utf-8",
									type: "POST",
									data: JSON.stringify(formData),
									error: function (jqXHR, textStatus, errorThrown ) {
										errorMsg(jqXHR.status, textStatus, errorThrown);
									},
									success: function (data) {
										//console.log(data);
										if (data.length >= 7) {
											$( "#dialog-message" ).html("<p>Project created as '" + data + "' successfully.</p>");
											$( "#dialog-message" ).dialog({
												buttons: {
												    "Ok": function() {
												      	$( this ).dialog( "close" );
												    },
												    Cancel: function() {
												      	$( this ).dialog( "close" );
												    }
												}
											});
											$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","lightgreen");
										}
										else {
											$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
											$( "#dialog-message" ).html("<p>Project not copied successfully.</p>");
											$( "#dialog-message" ).dialog({
												buttons: {
												    "Ok": function() {
												      	$( this ).dialog( "close" );
												    },
												    Cancel: function() {
												      	$( this ).dialog( "close" );
												    }
												}
											});
											$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","red");
										}

								    	//checkIfRequesterChanged(sn_code);	
								    	//populateProjectInfo(sn_code, "All");
									},
									error: function(e) {
										//console.log("ERROR: ", e);
									},
									done : function(e) {
										//console.log("DONE");
									}

							});					      	
					    },
					    Cancel: function() {
					    	//console.log("nothing");
					      	$( this ).dialog( "close" );
					    }
					}
				});
				$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","yellow");
	        }
	        
	    });
	    
		/*$( "#delivery_plan_link" ).click(function() {
			  $( "#delivery_plan_text" ).toggle();
		});*/

	    window.onbeforeunload = function(){
	    	if (changed) return 'Want to leave without saving?';
	    };
	    
	    $(document).on('click', '.override', function(e) {
	    	
	    	var custodian = $(this).closest('td').next('td').text();
	    	current_override = $(this).closest('td').text();
	    	sn = $('#sn').val();
	    	//console.log("custodian: " + custodian + ", current_override: " + current_override + ", sn: " + sn);

	    	if (current_override == "N") {
				$(this).closest('td').html('<span style="color:green">Y</span>');
			}
			else {
				$(this).closest('td').html('<span style="color:red">N</span>');
			}
	    	
	    	$.ajax({
				url:   'overrideCustodian',
						type: 'POST',
						cache: false,
						data:{ sn: sn, custodian: custodian, currentOverride: current_override },
						beforeSend: function( xhr ) {
						},
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							//console.log("data: " + data);
							/*if (data == "false") {
								$(this).closest('td').html('<span style="color:green">Y</span>');
							}
							else {
								$(this).closest('td').html('<span style="color:red">N</span>');
							}*/
						}
			});
	    	
	    });

	    $(document).on('click', '#requester_override', function(e) {
	    	
	    	var current_override = $(this).closest('td').text();
	    	var sn = $('#sn').val();
	    	var projectCode = $('#sn_code').val();
	    	//console.log("requester... current_override: " + current_override + ", sn: " + sn);

	    	if (current_override == "N") {
				$(this).closest('td').html('<span style="color:green">Y</span>');
			}
			else {
				$(this).closest('td').html('<span style="color:red">N</span>');
			}
	    	
	    	$.ajax({
				url:   'overrideRequester',
						type: 'POST',
						cache: false,
						data:{ sn: sn, projectCode: projectCode, currentOverride: current_override },
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							//console.log("data: " + data);
						}
			});
	    	
	    });

	    $(document).on('click', '#getProjectEmails', function(e) {
	    	
	    	$.ajax({
				url:   'getProjectEmails',
						type: 'POST',
						cache: false,
						data:{ projectCode: $('#sn_code').val(), csrf_val: csrf_val },
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							
							emails = jQuery.parseJSON(data);

							var $dropdown = $("#email_list");
							$("#email_list").empty();
							$.each(emails, function() {
							    $dropdown.append($("<option />").val(this.id).text(this.from + ' | ' + this.subject + ' | ' + this.timestamp));
							});
						}
			});
	    	
	    });
	    
	    $('#email_list').on('change', function() {
	    	
	    	$.ajax({
				url:   'getProjectEmailById',
						type: 'POST',
						cache: false,
						data:{ emailId: this.value, csrf_val: csrf_val },
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							
							$('#related_email_body').html(data);
							
						}
			});
	    	
	    }); 
	    
	    $(document).on('click', '#copy_selected_emails', function(e) {
	    	//console.log($('#email_list').val());
	    	if ($('#email_list').val().length > 0) {
	    		
		    	$.ajax({
					url:   'copyEmailById',
							type: 'POST',
							cache: false,
							data:{ emailIds: $('#email_list').val(), folder: directories[$('#sn_code').val().replace(/[^A-Za-z]/g, '')] + $('#sn_code').val(), csrf_val: csrf_val },
							error: function (jqXHR, textStatus, errorThrown ) {
								errorMsg(jqXHR.status, textStatus, errorThrown);
							},
							success: function (data) {
								/*if (data == "ok") {
									$( "#dialog-message" ).html("<p>Emails copied successfully.</p>");
									$( "#dialog-message" ).dialog({
										buttons: {
										    "Ok": function() {
										      	$( this ).dialog( "close" );
										    }
										}
									});
									$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","lightgreen");
								}
								else {
									$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
									$( "#dialog-message" ).html("<p>Emails NOT copied successfully.</p>");
									$( "#dialog-message" ).dialog({
										buttons: {
										    "Ok": function() {
										      	$( this ).dialog( "close" );
										    }
										}
									});
									$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","red");
								}*/
							}
				});
		    	
	    	}
	    	
	    });
	    
	    $(document).on('click', '#copy_all_emails', function(e) {
	    	$.ajax({
				url:   'copyAllEmails',
						type: 'POST',
						cache: false,
						data:{ projectCode: $('#sn_code').val(), folder: directories[$('#sn_code').val().replace(/[^A-Za-z]/g, '')] + $('#sn_code').val(), csrf_val: csrf_val },
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							
						}
			});
	    	
	    });
	    
	    $(document).on('click', '#add_tables_to_tech_specs', function(e) {
	        var curr_technical_specs = $("#technical_specs").val();
	        var tables = [];
	        $('#approvals tr').each(function(i,e){
	        	//console.log("In word_butt, adding: " + $(this).find("td:nth-child(3)").text().trim());
	        	if ($(this).find("td:nth-child(4)").text().trim() != "Table") {
	        		//tables += $(this).find("td:nth-child(4)").text().trim() + "\n";
	        		tables.push($(this).find("td:nth-child(4)").text().trim());
	        	}
	    	});
	        $('#missing_custodian tr').each(function(i,e){
	        	//console.log("In word_butt, adding: " + $(this).find("td:nth-child(3)").text().trim());
	        	if ($(this).find("td:nth-child(1)").text().trim() != "Tables Missing Custodian") {
	        		//tables += $(this).find("td:nth-child(4)").text().trim() + "\n";
	        		tables.push($(this).find("td:nth-child(1)").text().trim());
	        	}
	    	});
	        tables = tables.sort().join("\n");
	        if (tables != "") {
		        $("#technical_specs").val(curr_technical_specs + "\n\nTables Used:\n" + tables);
		        enableCommit();
	        }
	        
	    });
	    
	    
	    
	    $(document).on('click', '#clear_search_fields', function(e) {
	    	$('.checkfilters').prop('checked', false);
	    	$('.search').val('');
	    	$('.search').css({"background" : ""});
	    	//$('.search').removeClass("search").addClass("search");
	    	$('.checkfilters_td').css({'background-color' : "#ebf2fa"});
	    });
    
	    $(document).on('click', '#reset_search_fields', function(e) {
	    	$('#clear_search_fields').click();
	    	$('#developer_search').val($('#developerUsername').val());
	    	$('#developer_search').css({'background-color' : "lightgreen"});
	    	$('#open').click();
	    });
    
	    $(document).on('click', '#add_requester', function(e) {
	    	$('#add_requester_div').toggle();
	    	//$('#add_requester_email').focus();
	    });
    
	    $(document).on('click', '#add_requester_butt', function(e) {
			var settings = {
					url:   'addRequester',
					type: 'POST',
					cache: false,
					data:{ add_requester_email: $('#add_requester_email').val(), csrf_val: csrf_val },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(382, textStatus, errorThrown);
					},
					success: function (data) {
						requester = jQuery.parseJSON(data);
						
						if (requester.requester_id == "-1") {
							errorMsg("Requester not found in LDAP", undefined, undefined)
						}
						else {
							var current_requester = $('#requester').val();
							
							$('#requester')
						    .append(requester.requesterHTML)
						    .val(requester.requester_id);
							
							$('#requester_title').val(requester.requester_title);
							
							if (current_requester != requester.requester_id) {
								enableCommit();
							}
						}
						
					}
			};
			
			$.ajax(settings);
	    });
    
	    $(document).on('click', '#remove_all_approvals', function(e) {
			var ajax_settings = {
					url:   'removeAllApprovals',
					type: 'POST',
					cache: false,
					data:{ sn: $('#sn').val(), csrf_val: csrf_val },
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						//jQuery.parseJSON(data);
						
						$('#approvals tr.auto').remove();
						$('#approvals tr.manual').remove();
						$('#remove_all_approvals').hide();
						
					}
			};
			
	    	var dialog_settings = {
	    			width: 300,
	    			height: "auto",
	    			title: "Remove All Custodians?",
					buttons: {
					    "Ok": function() {
					      	$( this ).dialog( "close" );
				     		$.ajax(ajax_settings);				      	
					    },
					    Cancel: function() {
					      	$( this ).dialog( "close" );
					    }
					}

	    	};
			$( "#dialog-message" ).dialog(dialog_settings);
			$( "#dialog-message" ).html("<p>Remove all custodians from project " + $('#sn_code').val() + "?</p>");
			$( "#dialog-message" ).dialog( "open" ).prev(".ui-dialog-titlebar").css("background","red");

	    });
	    
	    // USED FOR OTHER TABS (PREFS, SCHEDULER)
	    $(document).on('click', ".div_arrow", function() {
	    	var current_text = $(this).text();
			var action = "";
	    	if (current_text.charCodeAt(0) == 9658) {
	    		$(this).html('&#x25BC;').text();
	    		// action = "open";
	    	}
	    	else {
	    		$(this).html('&#x25BA;').text();
	    	}
	        $(this).nextAll(".expand_collapse_div").eq(0).toggle();
	    });
		
	    // USED FOR OTHER TABS (PREFS, SCHEDULER)
	    $(document).on('click', ".div_header", function() {
			$(this).next( ".div_arrow" ).trigger('click');
	    });

	}); // document.ready
	  
	
  </script>
</head>
<body>
	<div class="loader"></div> <!-- SPINNER -->
	 <div style="margin-top:0em;width:100%; margin:auto;">
	 	
	 	<div id="menu1" class="menu-buttons" style="float: left;"><img src="<%=request.getContextPath()%>/images/monitor-window.png" class="tab-image"> Menu <img src="<%=request.getContextPath() %>/images/asc.png"></div>
	 	<div id="menu2" class="menu-buttons" style="margin-left:207px;visibility:hidden;"><img src='<%=request.getContextPath()%>/images/application-table.png' class="tab-image"> Reports <img src="<%=request.getContextPath() %>/images/asc.png"></div>
	 	
		 <ul id="menu1dd" role="menu" tabindex="0" class="ui-menu ui-widget ui-widget-content ui-menu-icons" aria-activedescendant="ui-id-8" style="position:fixed;display:none;z-index:1;">
		  <li class="ui-menu-item">
		    <div id="dd1-1" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-1"></span>Developer Dashboard</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd1-2" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-2"></span>Scheduler</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd1-3" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-3"></span>User Prefs</div>
		  </li>
		  <!-- li class="ui-menu-item">
		    <div id="dd1-3" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-3"></span>Preferences</div>
		  </li-->
		  <!-- li class="ui-menu-item">
		    <div id="dd1-4" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-4"></span>Reset Layout</div>
		  </li-->
		  <!-- li class="ui-menu-item">
		    <div id="dd1-3" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-3"></span>Vendor Prime Dashboard</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd1-4" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-4"></span>Vendor Subcontractor Dashboard</div>
		  </li-->
		  <li class="ui-menu-item">
		    <div id="dd1-5" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper" style="text-indent:20px;"><span class="menu-item menu-item-6"></span>Sign Out</div>
		  </li>
		</ul> 
	
		<!--ul id="menu2dd" role="menu" tabindex="0" class="ui-menu ui-widget ui-widget-content ui-menu-icons" aria-activedescendant="ui-id-08" style="margin-left: 207px;position:fixed;display:none;z-index:1;">
		  <li class="ui-menu-item">
		    <div id="dd2-1" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper under-construction" style="text-indent:20px;"><span class="menu-item menu-item-5"></span>Monthly/Yearly Request Stats</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd2-2" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper under-construction" style="text-indent:20px;"><span class="menu-item menu-item-5"></span>Executive Report</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd2-3" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper under-construction" style="text-indent:20px;"><span class="menu-item menu-item-5"></span>FAR Funding Report</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd2-4" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper under-construction" style="text-indent:20px;"><span class="menu-item menu-item-5"></span>Change Order and Amendment Report</div>
		  </li>
		  <li class="ui-menu-item">
		    <div id="dd2-5" tabindex="-1" role="menuitem" class="ui-menu-item-wrapper under-construction" style="text-indent:20px;"><span class="menu-item menu-item-5"></span>Vendor Commitment Summary</div>
		  </li>
		</ul--> 
	</div>
	
<!-- button id="add_tab" class="ui-button ui-corner-all ui-widget">Add Tab</button-->
<div style="height:3px;"><!-- spacer at top of tabs --></div>
<div id="tabs">
  <ul>
    <!-- li><a href="#tabs-2"><img src="<%=request.getContextPath()%>/images/monitor-window-3d.png" class="tab-image" valign="middle">Developer Dashboard</a> <span class="ui-icon ui-icon-close" role="presentation">Remove Tab</span></li-->
    <li><a href="#tabs-2"><img src="<%=request.getContextPath()%>/images/monitor-window-3d.png" class="tab-image" valign="middle">Developer Dashboard</a> </li>
  </ul>
  
  <div id="tabs-2" style="min-width:1244px;"><p>

    <table style="width:100%;min-width:1200px;border-collapse:collapse;border:1px solid black;">
    	<tr>
    		<td style="width:600px;border-collapse:collapse;border:1px solid black;vertical-align:top;">    			
				<div style="overflow:visible;">
					<table id="" style="margin:0px;width:100%;background:#ebf2fa;font-size:.6em;">
						<tbody>
							<tr>
								<td class="checkfilters_td"><input id="unassigned" type="checkbox" class="checkfilters"><br>Unassigned</td>
								<td style="background:lightgreen;" class="checkfilters_td"><input id="open" type="checkbox" class="checkfilters" checked><br>Open</td>
								<td class="checkfilters_td"><input id="delivered" type="checkbox" class="checkfilters"><br>Delivered</td>
								<td class="checkfilters_td"><input id="onhold" type="checkbox" class="checkfilters"><br>On hold</td>
								<td class="checkfilters_td"><input id="ongoing" type="checkbox" class="checkfilters"><br>On going</td>
								<td class="checkfilters_td"><input id="cancelled" type="checkbox" class="checkfilters"><br>Cancelled</td>
							</tr>
						</tbody>
					</table>
					<table id="projectListHeader" style="margin:0px;border-spacing:2px;">
						<tbody>
							<tr style="min-width:570px;">
								<td style="min-width:85px;"><img id="open_folder" src="/DataLine/images/folder-horizontal-open.png" style="height:15px;vertical-align:middle;cursor:pointer;"><input id="code_search" type="text" style="width:67px;font-size:.7em;" placeholder="code" class="search"></td>
								<td style="min-width:125px;"><input id="requester_search" type="text" style="width:122px;font-size:.7em;" placeholder="requester" class="search"></td>
								<td style="min-width:70px;max-width:70px;"><input id="developer_search" type="text" style="width:68px;font-size:.7em;background:lightgreen;" placeholder="developer" class="search" value="${loggedInUser}"></td>
								<td style="min-width:300px;text-align:left;"><input id="description_search" type="text" style="width:220px;max-width:290px;font-size:.7em;" placeholder="description" class="search"> <div class="tooltip" id="reset_search_fields" style="cursor:pointer;"><img src="<%=request.getContextPath()%>/images/arrow-circle-315.png" style="position:relative;top:3px"><span class="tooltiptext">Reset Search</span></div> <div class="tooltip" id="clear_search_fields" style="cursor:pointer;"><img src="<%=request.getContextPath()%>/images/cross-white.png" style="position:relative;top:3px"><span class="tooltiptext">Clear Search Fields</span></div> <div class="tooltip"><span id="advanced_search_arrow" style="cursor:pointer;">&#x25BA;</span><span class="tooltiptext">Advanced Search</span></div><!--  &#x25BC; --> </td>
							</tr>
						</tbody>
					</table>
					<table id="projectListAdvancedSearch" style="margin:0px;display:none;border-spacing:0px;padding:0px;">
						<tbody>
							<tr style="min-width:570px;">
								<td style="" colspan="4"><input id="any_search" type="text" style="width:99%;font-size:.7em;" placeholder="search all fields" class="search"></td>
							</tr>
							<tr>
								<td style="min-width:85px;"><input id="irb_waiver_search" type="text" style="width:82px;font-size:.7em;" placeholder="irb" class="search"></td>
								<td style="min-width:125px;"><input id="cross_ref_search" type="text" style="width:122px;font-size:.7em;" placeholder="cross ref" class="search"></td>
								<td style="min-width:70px;"><input id="purpose_of_req_search" type="text" style="width:68px;font-size:.7em;" placeholder="purpose of req" class="search"></td>
								<!-- td style="min-width:300px;"><input id="elements_search" type="text" style="width:300px;font-size:.7em;" placeholder="elements" class="search"></td-->
								<td style="min-width:300px;"><input id="delivery_plan_search" type="text" style="width:300px;font-size:.7em;" placeholder="delivery plan" class="search"></td>
							</tr>
							<tr>
								<td style="min-width:85px;"><select style="vertical-align:bottom;font-size:9px;height:18px;width:82px;" id="and_or_search"><option style="font-size:9px;" value="AND">AND</option><option style="font-size:9px;"  value="OR">OR</option></select><!-- input id="criteria_search" type="text" style="width:85px;font-size:.7em;" placeholder="" class="search"!--></td>
								<td style="min-width:125px;"><input id="technical_specs_search" type="text" style="width:122px;font-size:.7em;" placeholder="technical specs" class="search"></td>
								<td style="min-width:70px;"><input id="test_plan_search" type="text" style="width:68px;font-size:.7em;" placeholder="test plan" class="search"></td>
								<td style="min-width:300px;"><input id="sql_search" type="text" style="width:300px;font-size:.7em;" placeholder="sql" class="search"></td>
							</tr>
						</tbody>
					</table>
				</div>
				<div id="requestList" style="overflow:scroll;height:700px;">
					<div class="search_loader"></div>
					<table id="projectList" style="width:100%;margin:0px;">
						<tbody>
							<!-- tr>
								<td style="width:70px;"><input id="code_search" type="text" maxlength="8" style="width:100%;font-size:.7em;" placeholder="code" class="search"></td>
								<td style="width:125px;"><input id="requester_search" type="text" maxlength="8" style="width:100%;font-size:.7em;" placeholder="requester" class="search"></td>
								<td style="width:70px;"><input id="developer_search" type="text" maxlength="8" style="width:100%;font-size:.7em;" placeholder="developer" class="search"></td>
								<td style="width:250px;"><input id="description_search" type="text" maxlength="8" style="width:100%;font-size:.7em;" placeholder="description" class="search"></td>
							</tr-->
						</tbody>
					</table>
				</div>
				<input id="projectCount" style="width:27px;font-family:Arial,Helvetica,sans-serif;font-size:.75em;"/> <font style="width:17px;font-family:Arial,Helvetica,sans-serif;font-size:.75em;">projects</font>
    			<!-- br><a href="#" id="exportProjectSpreadsheet" class="exportTableButton">Export Table data into Excel</a><br><br-->
    		</td>
    		<td style="border-collapse:collapse;border:1px solid black;vertical-align:top;">
    			<form id="project_info_form">
    				<input type="hidden" id="developerName" value="${developerName}">
    				<input type="hidden" id="developerUsername" value="${loggedInUser}">
    				<input type="hidden" id="developerEmail" value="${developerEmail}">
    				<input type="hidden" id="requesterFirstName">
    				<input type="hidden" id="requesterLastName">
    				<input type="hidden" id="requesterName">
    				<input type="hidden" id="requester_email">
    				<input type="hidden" id="custodian_emails">
    				<input type="hidden" id="data_elements">
    				<input type="hidden" id="criteria">
    				<input type="hidden" id="csrf_val" value='<%=session.getAttribute("csrf_val")%>'>
	    			<input id="commit_butt" style="float:left;background:#cdcdcd;margin-left:3px;margin-top:3px;" type="submit" value="Commit" disabled>
	    			<input id="copy_butt" style="float:left;background:#007fff;margin-left:3px;margin-top:3px;color:white;" type="button" value="Copy as New">
	    			<div class="tooltip" style="float:left;margin-left:3px;"><img id="layout_butt" src="<%=request.getContextPath()%>/images/seo-36-48.png" style="height:29px;top:0px;left:5px;cursor:pointer;" valign="middle"><span class="tooltiptext">Reset Layout</span></div>
	    			<div class="tooltip" style="float:left;margin-left:3px;"><img id="word_butt" src="<%=request.getContextPath()%>/images/msword.png" style="height:29px;top:0px;left:5px;cursor:pointer;" valign="middle"><span class="tooltiptext">Generate Word Document Plan</span></div>
	    			<!-- div class="tooltip" style="float:left;margin-left:3px;position:relative;top:10px;"><span id="refresh_from_projects_butt" style="cursor:pointer;">Refresh from R2D3</span><span class="tooltiptext">Refresh values from Project table.</span></div-->
	    			<!-- img id="layout_butt" src="<%=request.getContextPath()%>/images/seo-36-48.png" style="height:30px;top:0px;left:3px;cursor:pointer;" valign="middle"-->
	    			<input id="email" type="hidden">
	    			<span style="padding:5px;border-collapse:collapse;border-width:1px;text-align:right;float:right;">
	    				<b>Version: </b><select id="version" style="background:lightgreen;"></select><!-- button id="delete_version" type="button" style="vertical-align:top;margin-bottom:0px;background:tomato;">-</button--><br>
	    			</span>
	    			<table id="requestInfo" style="width:100%;margin:0px;border-collapse:collapse;border:0px solid black;font-size:.75em;font-weight:bold;">
	
	    				<tr><td style="border-left:0px solid black;" class="infoLabel">SN &amp; Code</td><td><input id="sn" type="text" style="width:40px" readonly><input id="sn_code" type="text" style="width:106px"></td><td class="infoLabel">Received By</td><td><input id="received_by" type="text"></td><td  rowspan="6" class="infoLabel">Delivery Plan</td><td rowspan="6"><textarea style="width:400px;height:155px;" id="program_specs" placeholder="maxlength of 2,147,483,647"></textarea></td><!-- td rowspan="2" class="infoLabel">Data Elements</td><td rowspan="2"><textarea style="height:34px;width:400px;" id="data_elements" placecholder="maxlength of 2,147,483,647"></textarea></td--></tr>
	    				<tr><td style="border:0px solid black;">Request Type</td><td><select id="request_type"><option value="ASAP">ASAP</option><option value="Date">Date</option><option value="Earliest Convenience">Earliest Convenience</option></select></td><td class="infoLabel">Start Date</td><td><input id="start_date" type="text" autocomplete="new-password"></td></tr>
	    				<tr><td style="border:0px solid black;" class="infoLabel">Received Date</td><td><input id="received_date" type="text" autocomplete="new-password"></td><td class="infoLabel">Delivery Date</td><td><input id="delivery_date" type="text" autocomplete="new-password"></td><!-- td rowspan="2" class="infoLabel">Criteria</td><td rowspan="2"><textarea style="height:34px;width:400px;" id="criteria" placecholder="maxlength of 2,147,483,647"></textarea></td--></tr>
	    				<tr><td style="border:0px solid black;" class="infoLabel">Req'd Deliv Date</td><td><input id="reqd_deliv_date" type="text" autocomplete="new-password"></td><td>Project Status</td><td><select id="status"><option></option><option>On hold</option><option>On-going</option><option>Cancelled</option></select></td></tr>

	    				<!-- tr><td style="border-left:0px solid black;" class="infoLabel">Est Delivery Dte</td><td><input id="est_delivery_dte" type="text"></td><td class="infoLabel">Objs Delivered</td><td><select id="objs_delivered"><option value=""></option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option><option value="5">5</option><option value="10">More than 5</option></select></td><td  rowspan="2" class="infoLabel">Technical Specs</td><td rowspan="2"><textarea style="width:400px;" id="technical_specs" placeholder="maxlength of 2,147,483,647"></textarea></td></tr-->
	    				<tr><td style="border-left:0px solid black;" class="infoLabel">Est Delivery Dte</td><td><input id="est_delivery_dte" type="text" autocomplete="new-password"></td><td class="infoLabel">Objs Delivered</td><td><select id="objs_delivered"><option value=""></option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option><option value="5">5</option><option value="10">More than 5</option></select></td><!-- td  rowspan="2" class="infoLabel">Delivery Plan</td><td rowspan="2"><textarea style="width:400px;" id="program_specs" placeholder="maxlength of 2,147,483,647" readonly></textarea></td--></tr>

	   					<tr><td style="border:0px solid black;">Developer</td><td><select id="developer"><option></option></select></td><td>2nd Developer</td><td><select id="second_developer"><option></option></select></td></tr>

	   					<!-- >tr><td style="border:0px solid black;">Schedule Status</td><td><select id="schedule_status"><option value="Annually">Annually</option><option value="Bi-annually">Bi-annually</option><option value="Bi-monthly">Bi-monthly</option><option value="Bi-weekly">Bi-weekly</option><option value="Daily">Daily</option><option value="Monthly">Monthly</option><option value="On request">On request</option><option value="Quarterly">Quarterly</option><option value="Weekly">Weekly</option></select></td><td>Automation</td><td><select id="automation"><option></option></select></td><td rowspan="2" class="infoLabel">Test Plan</td><td rowspan="2"><textarea id="test_plan" style="height:34px;width:400px;" placeholder="maxlength of 2,147,483,647"></textarea></td></tr-->
	   					<tr><td style="border:0px solid black;">Schedule Status</td><td><select id="schedule_status"><option value="Annually">Annually</option><option value="Bi-annually">Bi-annually</option><option value="Bi-monthly">Bi-monthly</option><option value="Bi-weekly">Bi-weekly</option><option value="Daily">Daily</option><option value="Monthly">Monthly</option><option value="On request">On request</option><option value="Quarterly">Quarterly</option><option value="Weekly">Weekly</option></select></td><td>Automation</td><td><select id="automation"><option></option></select></td><td  rowspan="2" class="infoLabel">Technical Specs<br><div class="tooltip"><img id="add_tables_to_tech_specs" src='<%=request.getContextPath() %>/images/arrow-turn-000-left.png' style="cursor:pointer;"><span class="tooltiptext" style="width:350px;margin-left:-180px;bottom:150%;padding:5px 5px;">Add custodian tables to Technical Specs</span></div></td><td rowspan="2"><textarea style="width:400px;" id="technical_specs" placeholder="maxlength of 2,147,483,647"></textarea></td></tr>

	     				<tr><td style="border:0px solid black;" class="infoLabel">Priority</td><td><select id="priority"><option value=""></option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option><option value="5">5</option></select></td><td class="infoLabel">Estimated Hours</td><td><input id="estimated_hours" type="text"></td></tr>

	    				<!-- >tr><td style="border:0px solid black;" rowspan="2" class="infoLabel">Requester<br>Information</td><td colspan="3" style="vertical-align:bottom;"><select id="requester" style="width:330px;"><option></option></select></td><td rowspan="2" class="infoLabel">Project Notes<br>(on Plan)</td><td rowspan="2"><textarea id="project_notes" style="height:34px;width:400px;" placeholder="maxlength of 2,147,483,647"></textarea></td></tr-->
	    				<tr><td style="border:0px solid black;" rowspan="2" class="infoLabel">Requester<br>Information<br><div class="tooltip"><img id="add_requester" src="/DataLine/images/user--plus.png" style="cursor:pointer;"><span class="tooltiptext" style="width:200px;margin-left:-105px;bottom:150%;padding:5px 5px;">Add Missing Requester</span></div></td><td colspan="3" style="vertical-align:bottom;"><select id="requester" style="width:330px;"><option></option></select></td><td rowspan="2" class="infoLabel">Test Plan</td><td rowspan="2"><textarea id="test_plan" style="height:34px;width:400px;" placeholder="maxlength of 2,147,483,647"></textarea></td></tr>

	      				<tr><td style="background:white;vertical-align:top;" colspan="3"><input id="requester_title" type="text" style="width:326px;"><div id="add_requester_div" style="display:none"><br><br><table style="border-spacing:0px;"><tr><td><input id="add_requester_email" type="input" placeholder="Requester Email" autocomplete="new-password"></td><td><button id="add_requester_butt" type="button" style="background:lightgreen;">+</button></td></tr></table></div></td></tr>
	      				
	     				<!-- tr><td style="border-left:0px solid black;" rowspan="2" class="infoLabel">Request<br>Description<br>(Original Req)</td><td colspan="3" rowspan="2"><textarea id="request_description" style="height:90px;width:400px;float:left;"></textarea></td><td class="infoLabel">Dev Notes<br>(Not on Plan)</td><td><textarea id="dev_notes" style="height:34px;width:400px;" maxlength="255" placeholder="maxlength of 255"></textarea></td></tr -->
	     				<tr><td style="border-left:0px solid black;" rowspan="1" class="infoLabel">Request<br>Description<br>(Orig)</td><td colspan="3" rowspan="1"><textarea id="request_description" style="height:34px;width:400px;float:left;"></textarea></td><td rowspan="1" class="infoLabel">Project Notes<br>(on Plan)</td><td rowspan="1"><textarea id="project_notes" style="height:34px;width:400px;" placeholder="maxlength of 2,147,483,647"></textarea></td></tr>
	     				
	      				<tr><td style="border:0px solid black;">Privacy Type</td><td><select id="privacy_type"><option></option></select></td><td style="border:0px solid black;" class="infoLabel">IRB/Waiver #</td><td><input id="irb_waiver" type="text"></td><td class="infoLabel">Dev Notes<br>(Not on Plan)</td><td><textarea id="dev_notes" style="height:34px;width:400px;" maxlength="255" placeholder="maxlength of 255"></textarea></td></tr>
	      				<tr><td style="border:0px solid black;" class="infoLabel">Project Cross<br>Ref</td><td colspan="3"><input id="cross_ref" type="text" style="width:300px"></td><td class="infoLabel" style="margin:auto;" rowspan="2">SQL<br><br><div class="tooltip"><img id="excel_butt" src="/DataLine/images/excel-logo-2013.png" style="height:20px;top:0px;left:0px;cursor:pointer;margin:auto;"><span class="tooltiptext" style="width:350px;margin-left:-180px;bottom:150%;padding:5px 5px;">Create an Excel file from SQL.<br>Select a delimiter to separate multiple spreadsheets.<br><br>Use:<br><br> /*worksheet: Worksheet Name*/ <br><br> to specify worksheet name(s) (defaults to 'Results 1').</span></div><br><br><div class="excel_loader"></div><div id="dead_excel" style="display:none;" class="tooltip"><img src='<%=request.getContextPath() %>/images/cross-circle.png'><span id="dead_excel_error" style="width:600px;height:600px;left:-230px;overflow-y:scroll;bottom:-3px;" class="tooltiptext"></span></div></td><td rowspan="2"><textarea id="sql" style="height:80px;width:400px;" placeholder="Enter SQL to automatically populate custodians.&#013;&#010;maxlength of 2,147,483,647"></textarea><div id="below_sql" style="position:relative;bottom:10px;float:left;">Sheet Delimiter: <select id="excel_delimiter" style="position:relative;float:none;width:30px;bottom:2px;"><option value=";">;</option><option value="!">!</option><option value="^">^</option></select>&nbsp;&nbsp;DB: <select id="sql_database" style="position:relative;float:none;width:50px;bottom:2px;"><option value="Auto">Auto</option><option value="IDB">IDB</option><option value="Darwin">Darwin</option></select><!-- &nbsp;&nbsp;Format: --><select id="sql_format" style="position:relative;float:none;width:55px;bottom:2px;display:None;"><option value="Excel" selected>Excel</option><option value="JSON">JSON</option></select></div><div id="excel_div" style="width:410px"></div></td></tr>
	    				<tr><td style="border-left:0px solid black;" class="infoLabel">Project<br>Description</td><td colspan="3"><textarea id="description" style="width:400px;float:left;"></textarea></td><td></td><td></td></tr>
	     				<tr><td style="border-left:0px solid black;" class="infoLabel">Purpose<br>of Request</td><td colspan="3"><textarea id="purpose_of_request" style="width:400px;float:left;"></textarea></td><td style="vertical-align:top;"><br>Amendments</td><td><div style="max-height:200px;overflow-y:auto;"><table id="amendments"><thead><tr><td style="vertical-align:top;background:white;"><select id="amendment_developer" style="width:100px;"><option value="Developer">Developer</option></select></td><td style="vertical-align:top;background:white;"><input id="amendment_date" type="text" placeholder="Delivery Date" style="width:85px;"></td><td style="min-width:150px;background:white;"><textarea id="amendment_note" placeholder="Amendment Note" style="height:40px;width:175px;"></textarea></td><td><button id="add_amendment" type="button" style="vertical-align:middle;margin-bottom:20px;background:lightgreen;">+</button></td></tr></thead></table></div></td></tr>
	      				<tr><td style="border-left:0px solid black;" class="infoLabel">Tables<br>Used</td><td colspan="3"><div style="display:table;"><input id="all_tables" type="text" placeholder="table search (3 chars min)" style="width:300px;"></div><div style="display:table;"><textarea id="all_tables_textarea" style="width:400px;float:left;" placeholder="Tables populate Approvals section. One table per line."></textarea></div></td><td rowspan="4" style="vertical-align:top;"><br>Approvals</td>
		      				<td rowspan="4" style="vertical-align:top;">
		      					<br>
		      					<table id="requester_approvals" style="width:406px">
		      						<tr><td style="background:white;text-align:center;">Y/N</td><td style="background:white;text-align:center;">Override</td><td style="background:white;text-align:left;min-width:150px;">Requester</td><td style="text-align:left;min-width:150px;background:white;"></td><td style="background:white;text-align:left;">&nbsp;</td></tr>
		      						<tr><td style="background:white;text-align:center;font-weight:normal;"></td><td id="requester_override" style="cursor:pointer;background:white;text-align:center;"></td><td style="text-align:left;min-width:150px;background:white;font-weight:normal;"></td><td style="background:white;text-align:left;">&nbsp;</td></tr>
		      					</table>
								<br><br>
								<div style="max-height:400px;overflow-y:auto;overflow-x: hidden;">
									<table id="approvals" style="width:406px">
										<tr><td style="background:white;text-align:center;">Y/N</td><td style="background:white;text-align:center;">Override</td><td style="background:white;text-align:left;min-width:150px;">Custodian</td><td style="text-align:left;min-width:150px;background:white;">Table</td><td style="background:white;text-align:center;"><div id="remove_all_approvals" style="display:none;"><div class="tooltip" style="cursor:pointer;"><img src="<%= request.getContextPath() %>/images/cross.png"><span class="tooltiptext_bottom" style="top:100%;left:13px;width:100px;padding:5px 5px;">Remove All</span></div></div></td></tr>
									</table>
								</div>
		      					<br>
		      					<table align="center">
		      						<tr><td style="background:white;text-align:left;min-width:95px;"><input type="text" id="custodian" placeholder="Custodian (3 chars min)"></td><td style="text-align:left;min-width:95px;"><input type="text" id="cust_table" placeholder="Table (3 chars min)"></td><td><button id="add_approval" type="button" style="vertical-align:middle;margin-bottom:0px;background:lightgreen;">+</button></td><td style="background:white;text-align:left;">&nbsp;</td></tr>
		      					</table>
								<br><br>
		      					<table id="missing_custodian"align="center"><tr><td style="background:white;text-align:left;min-width:235px;">Tables Missing Custodian</td></tr></table>
		      					<br>
		      					<input style="width:20px;transform:scale(.9);" type="checkbox" id="privacy_checkbox" checked><span style="float:left">Include Privacy?</span>
		      					<br><br>		      					
		      					<table style="width:406px;font-size:10px;" align="center">
			      					<tr>
			      						<td style="background:white;"><input id="email_radio" name="email_radio" type="radio" style="float:none;width:12px;" value="request_received" checked></td><td style="text-align:left;">Request Received</input></td>
			      						<td style="background:white;"><input id="email_radio" name="email_radio" type="radio" style="float:none;width:12px;" value="first_contact"></td><td style="text-align:left;">First Contact</input></td>
			      						<td style="background:white;"><input id="email_radio" name="email_radio" type="radio" style="float:none;width:12px;" value="requester"></td><td style="text-align:left;">Send Plan to Requester</input></td>
			      					</tr>
			      					<tr>
			      						<td style="background:white;"><input id="email_radio" name="email_radio" type="radio" style="float:none;width:12px;" value="custodians"></td><td style="text-align:left;">Send Plan to Custodians</input></td>
			      						<td style="background:white;"><input id="email_radio" name="email_radio" type="radio" style="float:none;width:12px;" value="delivery"></td><td style="text-align:left;">Delivery</input></td>
			      						<td style="background:white;"><input id="email_radio" name="email_radio" type="radio" style="float:none;width:12px;" value="requester_and_custodians"></td><td style="text-align:left;">Send Plan to Requester and Custodians</input></td>
			      					</tr>
			      					<tr>
			      						<td colspan="6" style="text-align:center;background:white;"><div class="tooltip"><img id="send_email" src='<%= request.getContextPath() %>/images/email.png' style="width:48px;height:48px;" alt="Send Email (Opens Dialog with Message)"><span class="tooltiptext">Send Email<br>(Opens Dialog with Message)</span></div>&nbsp;&nbsp;&nbsp;<div class="tooltip"><img id="send_outlook" src='<%=request.getContextPath() %>/images/outlook.png' style="cursor:pointer;width:48px;height:48px;" alt="Send Email (Opens Outlook)"><span class="tooltiptext">Send Email<br>(Opens Outlook)</span></div></td>
			      					</tr>
		      					</table>
		      					<br>
		      					
		      				</td></tr>
	      				<tr><td></td><td colspan="3"></td><!-- td style="border-left:0px solid black;" class="infoLabel">Purpose<br>of Request</td><td colspan="3"><textarea id="purpose_of_request" style="width:400px;float:left;"></textarea></td--></tr>
	      				<tr><td></td><td colspan="3"></td><!-- td style="border-left:0px solid black;" class="infoLabel">Tables<br>Used</td><td colspan="3"><div style="display:table;"><input id="all_tables" type="text" placeholder="table search (3 chars min)" style="width:300px;"></div><div style="display:table;"><textarea id="all_tables_textarea" style="width:400px;float:left;" placeholder="Tables populate Approvals section. One table per line."></textarea></div></td--></tr>
	      				<tr><td valign="top" style="border-left:0px solid black;" class="infoLabel"><br><br>Related<br>Email<br><div class="tooltip"><img id="getProjectEmails" src="<%= request.getContextPath() %>/images/arrow-circle-double-135.png" style="cursor:pointer;align:center;padding:3px;" valign="middle"><span class="tooltiptext">Refresh email list from Inbox.</span></div><br><br><br>Copy<br>Selected<br><div class="tooltip"><img id="copy_selected_emails" src='<%=request.getContextPath() %>/images/documents.png' style="cursor:pointer;width:24px;height:24px;"><span class="tooltiptext">Copy selected emails to project folder.</span></div><br><br><br>Copy<br>All<br><div class="tooltip"><img id="copy_all_emails" src="<%=request.getContextPath() %>/images/documents-stack.png" style="cursor:pointer;width:24px;height:24px;"><span class="tooltiptext">Copy all emails to project folder.</span></div></td><td colspan="3" valign="top"><div style="display:table;"><select id="email_list" size="5" style="width:452px;float:left;" multiple></select></div><br><div style="display:table;"><div id="related_email_body" contenteditable="true" style="text-align:left;width:450px;height:300px;float:left;overflow:scroll;border-width:1px;border-style:solid;border-color:rgb(169,169,169);" placeholder="Email body."></div></div></td></tr>
	    			</table>
    			</form>
    		</td>
    	</tr>
    </table>
    
    
  </p></div>
  
</div>
 
 	 <div id="dialog-message" title="Dialog" style="background-color:#f2f2f2;">
	   <p>This is an animated dialog which is useful for displaying information. The dialog window can be moved, resized and closed with the 'x' icon.</p>
	 </div>

 
</body>
</html>
