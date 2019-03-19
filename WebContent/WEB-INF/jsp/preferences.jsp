    <style>
  	.table_user_pref_class table,
  	.table_user_pref_class th, 
  	.table_user_pref_class td { 
  		/*vertical-align:top;*/
  		margin:auto;
  		border-collapse: collapse;
  		border: 1px solid black; 
  		border-spacing: 2px;
  		border: none;
  		padding: 2px;
  	}
  	
  	.table_user_pref_class td { 
  		border: 1px solid black;
  	}

	.table_user_pref_class tr:nth-child(odd) { background-color:#ebf2fa; }
	.table_user_pref_class tr:nth-child(even) { background-color:#ffffff; }
	
	.table_user_pref_class th { background-color:#92aac7; }
	
	.expand_collapse:hover {
		color: #007fff;
	}
	  	
  </style>
  <script>
  
	  var emailMessageLegends = document.getElementsByClassName("email_message_legend");
	
	  for (var i = 0; i < emailMessageLegends.length; i++) {
		  emailMessageLegends[i].innerHTML = "<table style='border-collapse:collapse;border-width:1;margin:auto;width:400px;'><tr><td colspan='2'>&nbsp;</td></tr>" + 
		  "<tr style='background-color:MediumSpringGreen;'><td style='border: 1px solid black;font-family;'><b>Code</b></td><td style='border: 1px solid black;'><b>Definition</b></td></tr>" +
		  "<tr><td style='border: 1px solid black;font-family:Courier New'>&#60;requester_first_name&#62;</td><td style='border: 1px solid black;'>Requester's First Name</td></tr>" +
		  "<tr><td style='border: 1px solid black;font-family:Courier New'>&#60;requester_last_name&#62;</td><td style='border: 1px solid black;'>Requester's Last Name</td></tr>" +
		  "<tr><td style='border: 1px solid black;font-family:Courier New'>&#60;requester_full_name&#62;</td><td style='border: 1px solid black;'>Requester's Full Name</td></tr>" +
		  "<tr><td style='border: 1px solid black;font-family:Courier New'>&#60;project_code&#62;</td><td style='border: 1px solid black;'>Project Code (i.e. MED12345)</td></tr>" +
		  "<tr><td style='border: 1px solid black;font-family:Courier New'>&#60;project_info&#62;</td><td style='border: 1px solid black;'>Project info shown in default First Contact message (Request Description, Purpose, Delivery Plan)</td></tr>" +
		  "</table>"
		  ;
		  
		  
	  }
  
	    // Shorthand for $( document ).ready()

		$( function() {
			
			var email_folder_lookup = [];

			var changed = false;
			function enableUpdate() {
		    	if (!changed) { 
		    		$('#update_butt').css('background','lightgreen').css('color','black');
		    		$('#update_butt').prop('disabled', false);
		    	}
		        changed = true; 	
			}
			
			var initial_settings = {
					url:   'initialUserPrefsRetrieve',
					type: 'POST',
					cache: false,
					data:{  csrf_val: csrf_val	},
					beforeSend: function( xhr ) {
						$('.hide_for_load').hide();
						$('.excel_loader').show();
					},
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						
						//console.log(data);
						
						// EMAIL FOLDERS SECTION - START
						
						allData = jQuery.parseJSON(data);
						
						console.log(allData);
						
						var emailFolderList;
						if (allData.hasOwnProperty('emailFolders'))
							emailFolderList = allData.emailFolders;
						
						//$('#request_received_message').val(request_received_email_text);
						
						//console.log(emailFolderList);
						
						$.each( emailFolderList, function( i, folder ) {
							var checked = (folder.folder_search == 'Y' ? "checked" : "");
							email_folder_lookup[folder.folder_num] = folder.folder_name;
					        var $tr = $('<tr class="email_folders">').append(
					                $('<td>').html("<span>&nbsp;&nbsp;" + folder.folder_name + "&nbsp;&nbsp;</span>"),
					                $('<td>').html("<div class='weekDays-selector'><input type='checkbox' id='email_folder_" + folder.folder_num + "' value = '" + folder.folder_id + "' class='weekday' " + checked + "/><label for='email_folder_" + folder.folder_num + "'></label></div>")
					            ).appendTo('#table_email_folders');
					        
						});
						
						$('.weekday').bind('click', function (v) {
				        	console.log('change label');
						    enableUpdate();
					    });
						
						$('#update_butt').css('background','#cdcdcd').css('color','graytext');
			    		$('#update_butt').prop('disabled', true);
			    		changed = false;
						
						// EMAIL FOLDERS SECTION - END
						
						// EMAIL MESSAGES SECTION - BEGIN
						
						var email_types = ["request_received_message", "first_contact_message", "requester_message", "custodians_message", "requester_and_custodians_message", "delivery_message"];
						var email_lookup;
						
						for (var i=0; i < email_types.length; i++) {
							var email_type = email_types[i];
							if (allData.hasOwnProperty(email_type))
								$('#' + email_type).val(allData[email_type].pref_value_1);
						}
						
						$('.excel_loader').hide();
						$('.excel_loader_tr').hide();
						$('.hide_for_load').show();
					}
			};
			
			$.ajax(initial_settings); // initial load
			
		    $(document).on('submit', '#user_pref_form', function(e) {
		    	
		    	console.log("SUBMITTED");
		    	
		    	e.preventDefault();
		    	
		    	var form = this;
		    	var formData = {};
		    	var email_folders = [];
		    	var email_folders_sql_string = "";
		        $.each(form, function(i, v){
		        	var input = $(v);
		        	console.log(input.attr("id"));
		        	if (input.attr("id").substring(0,13) == "email_folder_") {
		        		if ( $("#" + input.attr("id")).prop("checked") ) {
		        			console.log(input.attr("id") + " is checked");
		        			email_folders.push( { id: $("#" + input.attr("id")).val(), name: email_folder_lookup[input.attr("id").substring(13)] } );
		        			email_folders_sql_string += "'" + email_folder_lookup[input.attr("id").substring(13)] + "', ";
		        		}
		        	}
		        	else {
		        		formData[input.attr("id")] = input.val();
		        	}
		        	formData["email_folders"] = JSON.stringify(email_folders);
		        	formData["email_folders_sql_string"] = email_folders_sql_string.substring(0, email_folders_sql_string.length - 2);
				});
		        
		        console.log(formData);
		        
				$.ajax({
					url: 'updateUserPrefs',
					contentType: "application/json; charset=utf-8",
					type: "POST",
					data: JSON.stringify(formData),
					error: function (jqXHR, textStatus, errorThrown ) {
						errorMsg(jqXHR.status, textStatus, errorThrown);
					},
					success: function (data) {
						// should refresh job list
						console.log("successful update. data: " + data);
						$('#update_butt').css('background','#cdcdcd').css('color','graytext');
			    		$('#update_butt').prop('disabled', true);
			    		changed = false;
						
					}
				});
		        		        
		    });
		    
		    $(".refresh_user_prefs").click(function() {
				$('.excel_loader_tr').show();
				$('.email_folders').remove();
				$('.excel_loader').show();
				$.ajax(initial_settings); // initial load
		    });
		    
		    $("#request_received_message, #first_contact_message, #requester_message, #custodians_message, #requester_and_custodians_message, #delivery_message").keyup(function() {
		    	enableUpdate();
		    });
			
		    $("#collapse_all").click(function() {
				$(".div_arrow").html('&#x25BA;').text();
				$('.expand_collapse_div').hide();
		    });
			
		    $("#expand_all").click(function() {
				$(".div_arrow").html('&#x25BC;').text();
				$('.expand_collapse_div').show();
		    });
			
		}); // document.ready
	  
	  </script>

<body>${message}
 
	<form id="user_pref_form">
		<input type="hidden" id="csrf_val" value='<%=session.getAttribute("csrf_val")%>'>
 		<div style="font-family:verdana;padding:10px;border-radius:10px;font-size:12px;text-align:center;">
	 		<div style="border: 1px solid black;margin:auto;min-height:700px;">
		 		<br>
		 		<table align="center""><tr><td style="border:1px solid black;margin:auto;"><b id="expand_all" class="expand_collapse" style="cursor:pointer;">Expand All &#x25BC;</b></td><td style="border:1px solid black;margin:auto;"><b id="collapse_all" class="expand_collapse" style="cursor:pointer;">Collapse All &#x25BA;</b></td></tr></table>
		 		<br><br>
		 		<b class="div_header">Email Folders to Search for Project Codes</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 		<table id="table_email_folders" class="table_user_pref_class" style="margin:auto;border-collapse:collapse;border-spacing:2px;">
			 			<thead style="background:#c3b6ee;color:white;"><th>&nbsp;Folder Name&nbsp;</th><th>&nbsp;Status&nbsp;</th></thead>
			 			<tbody><tr class="excel_loader_tr"><td colspan="2"><div class="excel_loader"></div></td></tr></tbody>
					</table><br><br>
				</div>
				
		 		<br>
		 		<b class="div_header">Email Message for Request Received</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 	<div class="excel_loader"></div>
				<textarea id="request_received_message" maxlength="255" class="hide_for_load" style="width:600px;height:300px;" placeholder="Enter entire email message text here..."></textarea>
				<div class="email_message_legend"></div>
				</div>
				
		 		<br>
		 		<b class="div_header">Email Message for First Contact</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 	<div class="excel_loader"></div>
				<textarea id="first_contact_message" maxlength="255" class="hide_for_load" style="width:600px;height:300px;" placeholder="Enter entire email message text here..."></textarea>
				<div class="email_message_legend"></div>
				</div>
				
		 		<br>
		 		<b class="div_header">Email Message for Requester Approval</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 	<div class="excel_loader"></div>
				<textarea id="requester_message" maxlength="255" class="hide_for_load" style="width:600px;height:300px;" placeholder="Enter entire email message text here..."></textarea>
				<div class="email_message_legend"></div>
				</div>
				
		 		<br>
		 		<b class="div_header">Email Message for Custodians Approval</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 	<div class="excel_loader"></div>
				<textarea id="custodians_message" maxlength="255" class="hide_for_load" style="width:600px;height:300px;" placeholder="Enter entire email message text here..."></textarea>
				<div class="email_message_legend"></div>
				</div>
				
		 		<br>
		 		<b class="div_header">Email Message for Requester and Custodians Approval</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 	<div class="excel_loader"></div>
				<textarea id="requester_and_custodians_message" maxlength="255" class="hide_for_load" style="width:600px;height:300px;" placeholder="Enter entire email message text here..."></textarea>
				<div class="email_message_legend"></div>
				</div>
				
		 		<br>
		 		<b class="div_header">Email Message for Delivery</b>	
		 		<span class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 		<br><br>
		 		<div class="expand_collapse_div" style="display:none;">
		 		<img class="refresh_user_prefs" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle"><br>
			 	<div class="excel_loader"></div>
				<textarea id="delivery_message" maxlength="255" class="hide_for_load" style="width:600px;height:300px;" placeholder="Enter entire email message text here..."></textarea>
				<div class="email_message_legend"></div>
				</div>
				
				<br>
			</div><br><br><!-- #171eff -->
			<input id="update_butt" style="background:#cdcdcd;color:graytext;margin-left:3px;margin-top:3px;font-family:Arial,Helvetica,sans-serif;font-size:1em;" type="submit" value="Update" disabled>
		</div>
	</form>
</body>
<!-- /html-->