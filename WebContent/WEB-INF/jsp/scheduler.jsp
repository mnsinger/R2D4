  <style>
  	.table_scheduler_class table,
  	.table_scheduler_class th, 
  	.table_scheduler_class td { 
  		/*vertical-align:top;*/ 
  		/*border-collapse: collapse;*/
  		/*border: 1px solid black;*/ 
  		border-spacing: 0px;
  		border: none;
  		padding: 1px;
  	}
  	
  	.table_scheduler_class td { 
  		border: 1px solid black;
  	}

	.table_scheduler_class tr:nth-child(odd) { background-color:#ebf2fa; }
	.table_scheduler_class tr:nth-child(even) { background-color:#ffffff; }
	
	.table_scheduler_class th { background-color:#92aac7; }
	
	#table_scheduler_results th { background-color:#a1d6e2; }

  	
  </style>
  <script>
  
  Number.prototype.pad = function(size) {
	  var s = String(this);
	  while (s.length < (size || 2)) {s = "0" + s;}
	  return s;
	}
  
	//var directories = [];
	
	  // Shorthand for $( document ).ready()

		$( function() {
						
			var delivery_types = {};
			var dt = new Date();
			// slice gets last 2 chars - so either pads or removes extra 0
			var dte = dt.getFullYear() + "-" + ('0' + (dt.getMonth()+1)).slice(-2) + "-" + ('0' + dt.getDate()).slice(-2);
			var hour = ('0' + dt.getHours()).slice(-2);
			var minute = ('0' + dt.getMinutes()).slice(-2);
			
			//console.log("$('#project_code').val(): " + $('#project_code').val());
			
			if ($('#project_code').val() == '') {
				
				$('#project_code').val( $('#sn_code').val() );
				
				$('#scheduler_start_date').val(dte);
				$('#scheduler_start_hour').val(hour);
				$('#scheduler_start_minute').val(minute);
				
				$('#scheduler_run_hour').val(hour);
				$('#scheduler_run_minute').val(minute);
				
				$('#scheduler_start_date').datepicker({dateFormat: "yy-mm-dd"});
				
			}
			
			function formValidation() {
				valid = true;
				message = "";
				if ($('#scheduler_database').val() == 'SELECT') {
					valid = false;
					message += "Please select a 'Database'.\n";
				}
				if ($('#project_code').val().trim() == '') {
					valid = false;
					message += "Please enter a 'Project Code'.\n";
				}
				if ($('#scheduler_delivery_type').val() == 'SELECT') {
					valid = false;
					message += "Please select a 'Delivery Type'.\n";
				}
				/*if (($('#scheduler_delivery_type').val() == 1 || $('#scheduler_delivery_type').val() == 2) && $('#scheduler_recipients').val().trim() == '') {
					valid = false;
					message += "Please enter valid 'Delivery Type - Email Recipients'.\n";
				}*/
				if ($('#scheduler_interval').val() == 'SELECT') {
					valid = false;
					message += "Please select an 'Interval'.\n";
				}
				if ($('#scheduler_days_of_month').val() == 'SPECIFIC' && $('#scheduler_days_of_month_list').val().trim().length == 0) {
					valid = false;
					message += "Please enter valid 'Days of Month List'.\n";
				}
				if (!valid) { alert(message); }
				return valid;
			}
			
			function updateExistingJob(formData) {
				  
		          $.ajax({
						url: 'schedulerUpdateExistingJob',
						contentType: "application/json; charset=utf-8",
						type: "POST",
						data: JSON.stringify(formData),
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							// should refresh job list
							console.log("successful update");
							refreshExistingJob();
							
						}
					  });
		          
			}
			
			function refreshExistingJob() {
				//var tr_existing_string = "#tr_job_" + $('#scheduler_tr').find("td:first").text();
				//$(tr_existing_string).find("td:nth-child(2)").text($('#scheduler_database').val());
				$("#table_scheduler_results > tbody").html("");
				getSchedule();
				//resetSchedulerTr();
			}
			
			function createNewJob(formData) {
		          $.ajax({
						url: 'schedulerCreateNewJob',
						contentType: "application/json; charset=utf-8",
						type: "POST",
						data: JSON.stringify(formData),
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							// should refresh job list
							$("#table_scheduler_results > tbody").html("");
							getSchedule();
							resetSchedulerTr();
						}
					  });
			}
			
			function resetSchedulerTr() {
				$('#scheduler_tr').css('background-color','');
				
				$('#scheduler_tr').find("td:first").text('-');
				//$('#scheduler_tr').find("td:nth-child(4)").text('');
			
				$('#scheduler_start_div').text('');
				
				$('#scheduler_button').val('Create New Job');
				
				$('#scheduler_database').val('SELECT');
				//if ($('#project_code').val() == '') {
					$('#project_code').val( $('#sn_code').val() );
				//}
				
				$('#div_delivery_type').hide();
				$('#div_delivery_subject').hide();
				$('#scheduler_delivery_type').val('SELECT');
				$('#scheduler_recipients').val('');
				
				$('#scheduler_start').show();
				$('#scheduler_start').val('NOW');
				$('.col_start_date').hide();
				$('.col_start_hour').hide();
				$('.col_start_minute').hide();
				
				$('#scheduler_interval').val('SELECT');
				$('#scheduler_interval_n').val('');
				$('#div_interval_n').hide();
				$('#scheduler_n_week_of_month').val('');
				$('#div_n_week_of_month').hide();
								
				$('#weekday-sun').prop( "checked", false);			
				$('#weekday-mon').prop( "checked", true);
				$('#weekday-tue').prop( "checked", true);
				$('#weekday-wed').prop( "checked", true);
				$('#weekday-thu').prop( "checked", true);
				$('#weekday-fri').prop( "checked", true);
				$('#weekday-sat').prop( "checked", false);
				$('#div_scheduler_days_of_week').show();
				
				$('#scheduler_days_of_month').val('ANY');
				$('#scheduler_days_of_month_list').val('');
				$('.col_days_of_month_list').hide();

				$('#scheduler_run_hour').val(hour);
				$('#scheduler_run_minute').val(minute);

			}
			
			function populateInitialSchedule(scheduleInitialList) {
				console.log(scheduleInitialList);
				var dropdown = $('#scheduler_delivery_type');
				$.each( scheduleInitialList.delivery_types, function( i, delivery_option ) {
					dropdown.append($('<option></option>').attr('value', delivery_option.delivery_type_id).text(delivery_option.delivery_type));
					delivery_types[delivery_option.delivery_type] = delivery_option.delivery_type_id;
				});
				dropdown = $('#scheduler_prereq');
				$.each( scheduleInitialList.prereqs, function( i, prereq_option ) {
					dropdown.append($('<option></option>').attr('value', prereq_option.prereq).text(prereq_option.prereq));
				});
			}
			
			function populateScheduleList(scheduleList, selected_id) {
				$('#hidden_vals').empty();
				$('<tbody>').appendTo('#table_scheduler_results');
				$.each( scheduleList, function( i, job ) {
					var daysOfWeekDisplay = "block";
					if (job.days_of_month.trim().length > 0) daysOfWeekDisplay = "none";
					var selected_text = '';
					if (job.id == selected_id) {
						selected_text = 'style="background-color: lightgreen;"';
					}
			        var $tr = $('<tr id="tr_job_' + job.id + '" ' + selected_text + '>').append(
			                $('<td>').html(
			                		'<span style="font-size:.8em;">' + job.id + 
			                		'</span>'
			                ),
			                $('<td>').html(
			                		'<span style="font-size:.8em;">' + job.database + 
			                		'</span>'
			                ),
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.project_code + 
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.project_description + 
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:.8em"><div style="max-width:200px;overflow-x:auto;">' + job.delivery_type + (job.recipients.trim().length > 0 ? ' - ' + job.recipients : '') +
			                		'</div></span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.prereq +
			                		'</span>'
			                		),			                		
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.start_time + 
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.interval + (job.interval_n != 0 ? ' - ' + job.interval_n : '') +
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'					<div class="weekDays-selector" style="display:' + daysOfWeekDisplay + '">' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-sun" class="weekday" ' + (job.sunday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-sun" style="font-size:.8em;cursor:default;">S</label>' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-mon" class="weekday" ' + (job.monday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-mon" style="font-size:.8em;cursor:default;">M</label>' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-tue" class="weekday" ' + (job.tuesday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-tue" style="font-size:.8em;cursor:default;">T</label>' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-wed" class="weekday" ' + (job.wednesday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-wed" style="font-size:.8em;cursor:default;">W</label>' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-thu" class="weekday" ' + (job.thursday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-thu" style="font-size:.8em;cursor:default;">T</label>' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-fri" class="weekday" ' + (job.friday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-fri" style="font-size:.8em;cursor:default;">F</label>' +
			                		'					  <input type="checkbox" id="'+ job.id +'-weekday-sat" class="weekday" ' + (job.saturday ? 'checked' : '') + ' disabled/>' +
			                		'					  <label for="'+ job.id +'-weekday-sat" style="font-size:.8em;cursor:default;">S</label>' +
			                		'					</div>'
			                		),			                		
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.days_of_month + 
			                		'</span>'
			                		),			                		
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.hour + 
			                		'</span>'
			                		),			                		
			                $('<td>').html(
			                		'<span style="font-size:.8em">' + job.minute.pad(2)  + 
			                		'</span>'
			                		),	
			                $('<td>').html(
					                		'					<div class="weekDays-selector">' +
					                		'					  <input type="checkbox" id="'+ job.id +'-enable" class="weekday" ' + (job.enabled ? 'checked' : '') + ' disabled/>' +
					                		'					  <label for="'+ job.id +'-enable" style="cursor:default"> </label>' +
					                		'					</div>'
					        ),
			                $('<td>').html(
			                		//'<input id="'+ job.id +'_delete_job" class="delete_job_button" style="background:#ff1717;margin-left:2px;margin-right:2px;margin-top:2px;color:white;font-size:.8em;height:20px;width:20px;center;" type="button" value="X">'
			                		'<div class="tooltip"><span style="font-size:24px;"><img id="'+ job.id +'_delete_job" class="delete_job_button" src="<%=request.getContextPath() %>/images/cross-button.png" style="cursor:pointer;margin-left:2px;margin-right:2px;margin-top:3px;height:20px;width:20px"></span><span class="tooltiptext" style="width:38px;margin-left:-24px;bottom:100%;padding:5px 5px;">Delete job</span></div>'
			                ),
			                $('<td>').html(
			                		'<div class="tooltip"><span style="font-size:24px;"><img id="'+ job.id +'_run_job" class="run_job_button" src="<%=request.getContextPath() %>/images/plus-button.png" style="cursor:pointer;margin-left:2px;margin-right:2px;margin-top:3px;height:20px;width:20px"></span><span class="tooltiptext" style="width:30px;margin-left:-20px;bottom:100%;padding:5px 5px;">Run job now</span></div>'
			                )
			            ).appendTo('#table_scheduler_results');
			        $('#hidden_vals').append("<input id='job_subject_" + job.id + "' type='hidden' value='" + job.subject + "'>")
			        console.log(job.enabled);
				});
			}
			
			function populateScheduleLogList(scheduleLogList, selected_id) {
				$('<tbody>').appendTo('#table_scheduler_log_results');
				$.each( scheduleLogList, function( i, job ) {
					var selected_text = '';
					if (job.id == selected_id) {
						selected_text = 'style="background-color: lightgreen;"';
					}
			        var $tr = $('<tr class="tr_log_job_' + job.id + '" ' + selected_text + '>').append(
			                $('<td>').html(
			                		'<span style="font-size:1.0em;">' + job.id + 
			                		'</span>'
			                ),
			                $('<td>').html(
			                		'<span style="font-size:1.0em">' + job.project_code + 
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:1.0em">' + job.start_time + 
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:1.0em">' + job.end_time + 
			                		'</span>'
			                		),
			                $('<td>').html(
			                		'<span style="font-size:1.0em">' + job.duration + 
			                		'</span>'
			                		),
					        $('<td>').html(
			                		'<span style="font-size:1.0em">' + (job.run_notes ? job.run_notes : ' ') + 
			                		'</span>'
			                		)
			            ).appendTo('#table_scheduler_log_results');
			        console.log(job.enabled);
				});
			}
			
		    $(document).on('submit', '#scheduler_form', function(e) {
		    	
		    	console.log("SUBMITTED");
		    	
		    	e.preventDefault();
		    	
		    	if (!formValidation()) return;
		    	
		        if ($('#scheduler_days_of_month').val() == "SPECIFIC") {
					$('#weekday-sun').prop('checked', false);
					$('#weekday-mon').prop('checked', false);
					$('#weekday-tue').prop('checked', false);
					$('#weekday-wed').prop('checked', false);
					$('#weekday-thu').prop('checked', false);
					$('#weekday-fri').prop('checked', false);
					$('#weekday-sat').prop('checked', false);
		        }
		        else {
		        	$('#scheduler_days_of_month_list').val('');
		        }
		        
		        if ($('#scheduler_interval').val() == "HOURLY" || $('#scheduler_interval').val() == "N_MONTHS") {
		        	if ($('#scheduler_interval_n').val().trim() == "") {
		        		$('#scheduler_interval_n').val('1');
		        	}
		        }
		        else {
		        	$('#scheduler_interval_n').val('');
		        }
		        
		    	var form = this;
		    	
		    	var formData = {};
		        $.each(form, function(i, v){
		        	var input = $(v);
		        	formData[input.attr("id")] = input.val();
				});
		        
		        formData["weekday-sun"] = ($("#weekday-sun").prop("checked") ? "1" : "0");
		        formData["weekday-mon"] = ($("#weekday-mon").prop("checked") ? "1" : "0");
		        formData["weekday-tue"] = ($("#weekday-tue").prop("checked") ? "1" : "0");
		        formData["weekday-wed"] = ($("#weekday-wed").prop("checked") ? "1" : "0");
		        formData["weekday-thu"] = ($("#weekday-thu").prop("checked") ? "1" : "0");
		        formData["weekday-fri"] = ($("#weekday-fri").prop("checked") ? "1" : "0");
		        formData["weekday-sat"] = ($("#weekday-sat").prop("checked") ? "1" : "0");
		        
		        formData["scheduler_enabled"] = ($("#scheduler_enabled").prop("checked") ? "1" : "0");
		        
		        formData["scheduler_id"] = $("#scheduler_tr").find("td:first").text();
		        
		        console.log(formData);
		        console.log($('#scheduler_button').val());
		        if ($('#scheduler_button').val() == 'Save Changes') {
		        	updateExistingJob(formData);
		        } else if ($('#scheduler_button').val() == 'Create New Job') {
		        	createNewJob(formData);
		        }		        
		        
		    });
		    
		    $(document).on('click','#table_scheduler_log_results tbody tr', function(e){
		    
		    	var scheduler_id = $(this).find("td:first").text();
		    	$('#tr_job_' + scheduler_id).trigger('click');
		    	
			});
			
			$(document).on('click','#table_scheduler_results tbody tr', function(e){	
				
				console.log($(this));
				
				var job_id = $(this).find("td:first").text();
				var job_database = $(this).find("td:nth-child(2)").text();
				var job_project_code = $(this).find("td:nth-child(3)").text();
				var job_delivery_type = $(this).find("td:nth-child(5)").text();
				var job_prereq = $(this).find("td:nth-child(6)").text();
				var job_start = $(this).find("td:nth-child(7)").text();
				var job_interval = $(this).find("td:nth-child(8)").text();
				var job_days_of_week = $(this).find("td:nth-child(9)");
				var job_enabled = $(this).find("td:nth-child(13)");
				var job_days_of_month = $(this).find("td:nth-child(10)");
				var job_run_hour = $(this).find("td:nth-child(11)").text();
				var job_run_minute = $(this).find("td:nth-child(12)").text();
				var job_subject = $("#job_subject_" + job_id).val();
				
				var previously_selected_job = $('#scheduler_tr').find("td:first").text();
				$('#tr_job_' + previously_selected_job).css('background-color','');
				$('.tr_log_job_' + previously_selected_job).css('background-color','');
				
				if (previously_selected_job == job_id) {
					$('#tr_job_' + previously_selected_job).css('background-color','');
					$('.tr_log_job_' + previously_selected_job).css('background-color','');
					resetSchedulerTr();
					return;
				}
				
				$(this).css('background-color','lightgreen');
				$('#scheduler_tr').css('background-color','lightgreen');
				$('.tr_log_job_' + job_id).css('background-color','lightgreen');
				
				$('#scheduler_tr').find("td:first").text(job_id);
				
				$('#scheduler_button').val('Save Changes');
				
				$('#scheduler_database').val(job_database);
				$('#project_code').val(job_project_code);
				
				//console.log($(this).find("td:nth-child(4)").html());
				
				var delivery_type_td = job_delivery_type;
				var delivery_type_id = delivery_type_td;
				if (delivery_type_td.indexOf('-') > -1) 
					delivery_type_id = delivery_type_td.substring(0, delivery_type_td.indexOf('-')-1);
				
				$('#scheduler_delivery_type').val(delivery_types[delivery_type_id]);
				
				var scheduler_recipients = (delivery_type_td.indexOf('-') > -1 ? delivery_type_td.substring(delivery_type_td.indexOf('-')+2, delivery_type_td.length) : "");
				
				console.log(delivery_types[delivery_type_id]);
				
				//if (delivery_types[delivery_type_id] == '1' || delivery_types[delivery_type_id] == '4') {
					$('#scheduler_recipients').val(scheduler_recipients);
					$('#scheduler_subject').val(job_subject);
					$('#div_delivery_type').show();
					$('#div_delivery_subject').show();
				/*}
				else {
					$('#scheduler_recipients').val('');
					$('#div_delivery_type').hide();
				}*/
				
				$('#scheduler_prereq').val(job_prereq);
				
				//$('#scheduler_recipients').val(scheduler_recipients);
				//$('#div_delivery_type').show();
				
				$('#scheduler_start').hide();
				//$('#scheduler_tr').find("td:nth-child(4)").text($(this).find("td:nth-child(4)").text());
				$('#scheduler_start_div').text(job_start);
				
				var interval_td = job_interval;
				var interval_id = interval_td;
				if (interval_td.indexOf('-') > -1) 
					interval_id = interval_td.substring(0, interval_td.indexOf('-')-1);
				
				$('#scheduler_interval').val(interval_id);
				
				var interval_n = (interval_td.indexOf('-') > -1 ? interval_td.substring(interval_td.indexOf('-')+2, interval_td.length) : "");
				
				console.log("interval_n: " + interval_n);
				
				$('#scheduler_interval_n').val(interval_n);
				$('#div_interval_n').hide();
				
				if (interval_td.indexOf('-') > -1) {
					$('#div_interval_n').show();
				}
				
				$('#scheduler_n_week_of_month').val('');
				$('#div_n_week_of_month').hide();
				//$('#scheduler_run_hour').show();
				
				console.log("interval_td.substring(0, 8): " + interval_td.substring(0, 8));
				
				if (interval_td.substring(0, 6) == 'HOURLY') {
					var text = $('#div_interval_n').html();
					text = text.substring(0, text.length - 8);
					$('#interval_measure').text("hour(s)");
					$('#div_interval_n').show();
				}
				else if (interval_td.substring(0, 8) == 'N_MONTHS') {
					var text = $('#div_interval_n').html();
					text = text.substring(0, text.length - 8);
					$('#interval_measure').text("month(s)");
					$('#div_interval_n').show();
				}
				else if (interval_td == 'N WEEK OF MONTH') {
					//$('#scheduler_n_week_of_month').val($(this).find("td:nth-child(10)").text());
					$('#div_n_week_of_month').show();
				}
				
				var bool_checked = job_days_of_week.find('input:checkbox:first').prop("checked");
				$('#weekday-sun').prop( "checked", bool_checked);
				
				var bool_checked = job_days_of_week.find('input:checkbox:nth-child(3)').prop("checked");
				$('#weekday-mon').prop( "checked", bool_checked);
				
				var bool_checked = job_days_of_week.find('input:checkbox:nth-child(5)').prop("checked");
				$('#weekday-tue').prop( "checked", bool_checked);
				
				var bool_checked = job_days_of_week.find('input:checkbox:nth-child(7)').prop("checked");
				$('#weekday-wed').prop( "checked", bool_checked);
				
				var bool_checked = job_days_of_week.find('input:checkbox:nth-child(9)').prop("checked");
				$('#weekday-thu').prop( "checked", bool_checked);
				
				var bool_checked = job_days_of_week.find('input:checkbox:nth-child(11)').prop("checked");
				$('#weekday-fri').prop( "checked", bool_checked);
				
				var bool_checked = job_days_of_week.find('input:checkbox:nth-child(13)').prop("checked");
				$('#weekday-sat').prop( "checked", bool_checked);
				
				var enabled = job_enabled.find('input:checkbox:first').prop("checked");
				$('#scheduler_enabled').prop( "checked", enabled);
				
				if (job_days_of_month.text() == '') {
					$('#scheduler_days_of_month').val('ANY');
					$('#scheduler_days_of_month_list').val('');
					$('#div_scheduler_days_of_week').show();
					$('.col_days_of_month_list').hide();
				}
				else {
					$('#scheduler_days_of_month').val('SPECIFIC');
					$("#scheduler_days_of_month").change();
					$('#scheduler_days_of_month_list').val(job_days_of_month.text());
					$('#div_scheduler_days_of_week').hide();
				}
				if (job_interval == 'HOURLY') {
					$('#scheduler_run_hour').val('');
				}
				else {
					$('#scheduler_run_hour').val(job_run_hour);
				}
				$('#scheduler_run_minute').val(job_run_minute);
				
			}); // #edit_button - click func
			
			$(document).on('click','.row_button', function(e){	
				console.log($(this));
				$(this).show();
				
			}); // #edit_button - click func
			
			$( '#scheduler_start' ).change(function() {
				var selected_option = $( '#scheduler_start' ).val();
				if (selected_option == 'FUTURE') {
					$('.col_start_date').show();
					$('.col_start_hour').show();
					$('.col_start_minute').show();
				}
				else if (selected_option == 'NOW') {
					$('.col_start_date').hide();
					$('.col_start_hour').hide();
					$('.col_start_minute').hide();
				}
			}); // #scheduler_start - change func

			$( "#scheduler_interval" ).change(function() {
				var selected_option = $( "#scheduler_interval" ).val();
				$('#scheduler_interval_unit').val('');
				
			    $('#div_interval_n').hide();
			    $('#div_n_week_of_month').hide();
				$('#scheduler_run_hour').show();
				switch(selected_option) {
				   case 'HOURLY':
					   var text = $('#div_interval_n').html();
					   text = text.substring(0, text.length - 8);
					   $('#interval_measure').text("hour(s)");
					   $('#div_interval_n').show();
				       break;
				   case 'N_MONTHS':
					   var text = $('#div_interval_n').html();
					   text = text.substring(0, text.length - 8);
					   $('#interval_measure').text("month(s)");
					   $('#div_interval_n').show();
				       break;
				   case 'N_WEEK_OF_MONTH':
					   $('#div_n_week_of_month').show();
				       break;
				   default:
			       //code block
				}
				
			}); // #scheduler_interval - change func
			
			$( '#scheduler_days_of_month' ).change(function() {
				var selected_option = $( '#scheduler_days_of_month' ).val();
				if (selected_option == 'SPECIFIC') {
					$('.col_days_of_month_list').show();
					$('#div_scheduler_days_of_week').hide();
				}
				else if (selected_option == 'ANY') {
					$('#div_scheduler_days_of_week').show();
					$('.col_days_of_month_list').hide();
				}
				
			}); // #scheduler_days_of_month - change func
			
			$( '#scheduler_delivery_type' ).change(function() {
				var selected_option = $( '#scheduler_delivery_type' ).val();
				if (selected_option == 'SELECT') {
					$('#div_delivery_type').hide();
					$('#div_delivery_subject').hide();
				}
				else if (selected_option == 5) { // Network Share
					$('#scheduler_recipients').attr('placeholder','(i.e. \\\\mskcc.root.mskcc.org\\dfsroot\\Hospital Administration\\Admitt\\Inhouse - MD List)');
					$('#div_delivery_type').show();
					$('#div_delivery_subject').show();
				}
				else {
					$('#scheduler_recipients').attr('placeholder','Email To: (i.e. singerm, willsj)');
					$('#div_delivery_type').show();
					$('#div_delivery_subject').show();
				}
			});

		    $(document).on('click','#refresh_sched', function(e) {
		    	getSchedule();
		    });
		    
		    $(document).on('click','#refresh_log', function(e) {
		    	getLog();
		    });
		    
		    /*$(document).on('click', '#create_job_arrow', function(e) {
		    	var current_text = $('#create_job_arrow').text();
				var action = "";
		    	if (current_text.charCodeAt(0) == 9658) {
		    		$('#create_job_arrow').html('&#x25BC;').text();
		    		// action = "open";
		    	}
		    	else {
		    		$('#create_job_arrow').html('&#x25BA;').text();
		    	}
	    		$( '#create_job_div' ).toggle();
		    });
			
		    $(document).on('click', '#existing_jobs_arrow', function(e) {
		    	var current_text = $('#existing_jobs_arrow').text();
				var action = "";
		    	if (current_text.charCodeAt(0) == 9658) {
		    		$('#existing_jobs_arrow').html('&#x25BC;').text();
		    		// action = "open";
		    	}
		    	else {
		    		$('#existing_jobs_arrow').html('&#x25BA;').text();
		    	}
	    		$( '#existing_jobs_div' ).toggle();
		    });
			
		    $(document).on('click', '#run_jobs_arrow', function(e) {
		    	var current_text = $('#run_jobs_arrow').text();
				var action = "";
		    	if (current_text.charCodeAt(0) == 9658) {
		    		$('#run_jobs_arrow').html('&#x25BC;').text();
		    		// action = "open";
		    	}
		    	else {
		    		$('#run_jobs_arrow').html('&#x25BA;').text();
		    	}
	    		$( '#run_jobs_div' ).toggle();
		    });*/
			
		    $(document).on('click','.delete_job_button', function(e) {
		    	
		    	e.stopPropagation();
		    	
		    	var jobId = e.target.id.replace("_delete_job", "");
		    	console.log(jobId);
		    	
				$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
				$( "#dialog-message" ).dialog('option', 'title', 'Delete Job');
				$( "#dialog-message" ).html("<p>Are you sure you want to delete job " + jobId + "?</p>");
				$( "#dialog-message" ).dialog({
					buttons: {
					    "Yes": function() {
					      	$( this ).dialog( "close" );
				     		$.ajax({
								url: 'deleteJob',
								type: "POST",					
								data: { id: jobId },
								error: function (jqXHR, textStatus, errorThrown ) {
									errorMsg(jqXHR.status, textStatus, errorThrown);
								},
								success: function (data) {
									$('table#table_scheduler_results tr#tr_job_' + jobId).remove();
									resetSchedulerTr();
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
			
		    $(document).on('click','.run_job_button', function(e) {
		    	e.stopPropagation();
		    	
		    	var jobId = e.target.id.replace("_run_job", "");
		    	console.log(jobId);
		    	
				$( "#dialog-message" ).dialog({ width: 300, height: "auto" });
				$( "#dialog-message" ).dialog('option', 'title', 'Run Job?');
				$( "#dialog-message" ).html("<p>Are you sure you want to run job" + jobId + "?</p>");
				$( "#dialog-message" ).dialog({
					buttons: {
					    "Yes": function() {
					      	$( this ).dialog( "close" );
				     		$.ajax({
								url: 'runJob',
								type: "POST",					
								data: { id: jobId },
								error: function (jqXHR, textStatus, errorThrown ) {
									errorMsg(jqXHR.status, textStatus, errorThrown);
								},
								success: function (data) {
									
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
			
			$.ajax({
				url:   'schedulerInitialRetrieve',
						type: 'POST',
						cache: false,
						data:{  
							username: "${loggedInUser}"
						},
						beforeSend: function( xhr ) {
							
						},
						error: function (jqXHR, textStatus, errorThrown ) {
							errorMsg(jqXHR.status, textStatus, errorThrown);
						},
						success: function (data) {
							scheduleInitialList = jQuery.parseJSON(data);
							//console.log(scheduleList);
							populateInitialSchedule(scheduleInitialList);
						}
			});

			function getSchedule() {
				//console.log($('#scheduler_tr').find("td:first").text());
				
				var selected_id = $('#scheduler_tr').find("td:first").text();
				
				
				$.ajax({
					url:   'getSchedule',
							type: 'POST',
							cache: false,
							data:{  //username: "${loggedInUser}", projectCode: "", requester: "", developer: "${loggedInUser}", description: "", completionStatus: "010000"
								username: "${loggedInUser}"
							},
							beforeSend: function( xhr ) {
								
							},
							error: function (jqXHR, textStatus, errorThrown ) {
								errorMsg(jqXHR.status, textStatus, errorThrown);
							},
							success: function (data) {
								$("#table_scheduler_results > tbody").fadeOut( "slow" );
								$("#table_scheduler_results > tbody").html("");
								
								scheduleList = jQuery.parseJSON(data);
								//console.log(scheduleList);
								populateScheduleList(scheduleList, selected_id);
								$("#table_scheduler_results > tbody").fadeIn( "slow" );
							}
				});
			}
			
			function getLog() {
				//console.log($('#scheduler_tr').find("td:first").text());
				
				var selected_id = $('#scheduler_tr').find("td:first").text();
				
				$.ajax({
					url:   'getScheduleLog',
							type: 'POST',
							cache: false,
							data:{  //username: "${loggedInUser}", projectCode: "", requester: "", developer: "${loggedInUser}", description: "", completionStatus: "010000"
								username: "${loggedInUser}"
							},
							beforeSend: function( xhr ) {
								
							},
							error: function (jqXHR, textStatus, errorThrown ) {
								errorMsg(jqXHR.status, textStatus, errorThrown);
							},
							success: function (data) {
								$("#table_scheduler_log_results > tbody").fadeOut( "slow" );
								$("#table_scheduler_log_results > tbody").html("");
								
								scheduleLogList = jQuery.parseJSON(data);
								//console.log(scheduleLogList);
								populateScheduleLogList(scheduleLogList, selected_id);
								$("#table_scheduler_log_results > tbody").fadeIn( "slow" );
							}
				});
			}
			
			getSchedule();
			
			getLog();
			
		}); // document.ready
	  
	  </script>


<body>${message}
	<form id="scheduler_form">
	<input type="hidden" id="csrf_val" value='<%=session.getAttribute("csrf_val")%>'>
 	<div style="font-family: verdana; padding: 10px; border-radius: 10px; font-size: 12px; text-align:center;">
 		<div style="border: 1px solid black;margin:auto;">
 		<br>
		<div class="tooltip"><span style="font-size:24px;"><b>&#9432;</b></span><span class="tooltiptext" style="width:500px;margin-left:-255px;bottom:100%;padding:5px 5px;">The R2D4 scheduler is a python 3.4 script that runs every minute. Scheduled 'Email Attachment' jobs create an Excel file and email it using the 'SQL' field from the Project. Scheduled 'Python Script' jobs called from the scheduler run in Python 3.6.</span></div>
 		<b class="div_header">Create or Edit Job</b>	
 		<span id="create_job_arrow" class="div_arrow" style="cursor:pointer;">&#x25BC;</span>
 		<br><br>
 		<div id="create_job_div" class="expand_collapse_div" style="display:block;">
	 		<table id="table_scheduler" class="table_scheduler_class" style="margin:auto;width:85%;border-collapse:collapse;border-spacing:0px;">
	 			<thead style="background: #c3b6ea;"><th>ID</th><th>Database</th><th>Project<br>Code</th><th>Delivery<br>Type</th><th>Prereq</th><th>Schedule<br>Start</th><th class="col_start_date" style="display:none;">Start<br>Date</th><th class="col_start_hour" style="display:none;">Start<br>Hour</th><th class="col_start_minute" style="display:none;">Start<br>Minute</th><th>Interval</th><th>Days of<br>Week</th><th>Days of<br>Month</th><th class="col_days_of_month_list" style="display:none;">Days of<br>Month List</th><th>Run<br>Hour</th><th>Run<br>Minute</th><th>Enabled?</th></thead>
	 			<tr id="scheduler_tr">
	 				<td>
	 					-
	 				</td>
	 				<td>
				 		<select id="scheduler_database">
				 			<option value="SELECT">Select Database</option>
				 			<option value="IDB">IDB</option>
				 			<option value="DARWIN">DARWIN</option>
						</select>
	 				</td>
	 				<td>
	 					<input type="text" id="project_code" placeholder="Project Code" style="width:70px;">
	 				</td>
	  				<td>
	  					<div class="tooltip"><span style="font-size:14px;"><b>&#9432;</b></span><span class="tooltiptext" style="width:600px;margin-left:-305px;bottom:100%;padding:5px 5px;text-align:left;"><ul><li>'Email Attachment' attaches an Excel file with the SQL output and sends an email</li><li>'Email Message' sends an email with the SQL output as the message body (no attachment)</li><li>'Email Message + Attachment' sends an email with the SQL output as the message body and also attaches an Excel file with the same SQL output</li><li>'Python Script' executes a script in the C:\DataLine folder on the vsmsktdvbi server</li><li>'Network Drive' creates Excel from the project SQL and moves the file to the specified network drive</li></ul></span></div>
				 		<select id="scheduler_delivery_type">
				 			<option value="SELECT">Select Delivery Type</option>
						</select>
						<div id="div_delivery_type" style="display:none;"><input id="scheduler_recipients" style="width:250px;" placeholder="Email To: (i.e. singerm, willsj)"></div>
						<div id="div_delivery_subject" style="display:none;"><input id="scheduler_subject" style="width:250px;" maxlength="255" placeholder="Email Subject: (i.e. DC Report (IS99999))"></div>
	 				</td>
	 				<td>
	  					<div class="tooltip"><span style="font-size:14px;"><b>&#9432;</b></span><span class="tooltiptext" style="width:500px;margin-left:-255px;bottom:100%;padding:5px 5px;">These subject areas are defined in the IDB.AVAILABILITY table. Jobs with a specified prereq will be held until the subject area has completed on that day.</span></div>
				 		<select id="scheduler_prereq">
				 			<option value="">Select Prereq</option>
						</select>
	 				</td>
	 				<td><div id="scheduler_start_div"></div>
				 		<select id="scheduler_start">
				 			<option value="NOW">Now</option>
				 			<option value="FUTURE">Future</option>
				 		</select>
	 				</td>
	 				<td class="col_start_date" style="display:none;">
	 					<input id="scheduler_start_date" type="text" placeholder="Start Date" style="width:70px;">
	 				</td>
	 				<td class="col_start_hour" style="display:none;">
	 					<input id="scheduler_start_hour" type="text" placeholder="Start Hour (0-23)" style="width:30px;">
	 				</td>
	 				<td class="col_start_minute" style="display:none;">
	 					<input id="scheduler_start_minute" type="text" placeholder="Start Minute (0-60)" style="width:30px;">
	 				</td>
	 				<td>
				 		<select id="scheduler_interval">
				 			<option value="SELECT">Select Interval</option>
				 			<option value="HOURLY">HOURLY</option>
				 			<option value="DAILY">DAILY / WEEKLY</option>
				 			<option value="BI_WEEKLY">BI-WEEKLY</option>
				 			<option value="MONTHLY">MONTHLY</option>
				 			<option value="N_MONTHS">N_MONTHS</option>
				 		</select>
				 		<div id="div_interval_n" style="display:none;">every <input id="scheduler_interval_n" style="width:30px;" value="1"> <span id="interval_measure">hour(s)</span> </div>
					 		<!-- select id="scheduler_n_week_of_month">
					 			<option value="1">1st</option>
					 			<option value="2">2nd</option>
					 			<option value="3">3rd</option>
					 			<option value="4">4th</option>
					 		</select> week of the month -->
				 		</div>
	 				</td>
	 				<td>
	 					<div id="div_scheduler_days_of_week">
						<div class="weekDays-selector">
						  <input type="checkbox" id="weekday-sun" class="weekday weekday-editable" />
						  <label for="weekday-sun">S</label>
						  <input type="checkbox" id="weekday-mon" class="weekday weekday-editable" checked/>
						  <label for="weekday-mon">M</label>
						  <input type="checkbox" id="weekday-tue" class="weekday weekday-editable" checked/>
						  <label for="weekday-tue">T</label>
						  <input type="checkbox" id="weekday-wed" class="weekday weekday-editable" checked/>
						  <label for="weekday-wed">W</label>
						  <input type="checkbox" id="weekday-thu" class="weekday weekday-editable" checked/>
						  <label for="weekday-thu">T</label>
						  <input type="checkbox" id="weekday-fri" class="weekday weekday-editable" checked/>
						  <label for="weekday-fri">F</label>
						  <input type="checkbox" id="weekday-sat" class="weekday weekday-editable" />
						  <label for="weekday-sat">S</label>
						</div>
						</div>
	 				</td>
	 				<td>
				 		<select id="scheduler_days_of_month">
				 			<option value="ANY">Any</option>
				 			<option value="SPECIFIC">Specific</option>
				 		</select>
	 				</td>
	  				<td class="col_days_of_month_list" style="display:none;">
	 					<input id="scheduler_days_of_month_list" type="text" placeholder="Comma separated list..." style="width:150px;">
	 				</td>
	 				<td>
		 				<div class="tooltip"><span style="font-size:14px;"><b>&#9432;</b></span><span class="tooltiptext" style="width:600px;margin-left:-305px;bottom:100%;padding:5px 5px;text-align:left;"><ul><li>Avoid setting times between 2 - 2:59 am because they will be skipped at the beginning of Daylight Savings Time (at 2 am, clocks jump to 3 am).</li><li>Avoid setting times between 1 - 1:59 am because they will be run twice at the end of Daylight Savings Time (at 2 am, clocks skip back to 1 am).</li></ul></span></div>
	 					<input id="scheduler_run_hour" type="text" placeholder="(0-23)" style="width:35px;">
	 				</td>
	 				<td>
	 					<input id="scheduler_run_minute" type="text" placeholder="(0-60)" style="width:35px;">
	 				</td>
	 				<td>
						<div class="weekDays-selector">
						  <input type="checkbox" id="scheduler_enabled" class="weekday" checked/>
						  <label for="scheduler_enabled"> </label>
						</div>
	 				</td>
	 				<td>
	 					<input id="scheduler_button" style="background:#171eff;margin-left:3px;margin-top:3px;color:white;" type="submit" value="Create New Job">
	 				</td>
	 			</tr>
			</table>
		<br><br>
		</div>
		</form>
		</div>
		<br><br>
  		<div style="border: 1px solid black;margin:auto;">
	 		<br>
	 		<b class="div_header">Existing Jobs</b> <span id="existing_jobs_arrow" class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 	<br><br>
	 		<div id="existing_jobs_div" class="expand_collapse_div" style="display:none;">
		 		<img id="refresh_sched" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle">
		 		<br>
		 		<table id="table_scheduler_results" class="table_scheduler_class" style="margin:auto;width:95%;border-collapse:collapse;border-spacing:0px;">
		 			<thead style=""><th>ID</th><th style="min-width:40px;">DB</th><th style="min-width:85px;">Project<br>Code</th><th style="max-width:500px">Project<br>Description</th><th>Delivery Type - Recipients<br></th><th>Prereq<br/></th><th>Schedule<br>Start</th><th>Interval</th><th style="min-width:200px;">Days of<br>Week</th><th>Days of<br>Month</th><th>Run<br>Hour</th><th>Run<br>Minute</th><th>Enabled?</th></thead>
		 		</table>
		 		<br><br>
		 		<div id="hidden_vals" style="display:none;"></div>
	 		</div>
 		</div>
 		<br><br>
  		<div style="border: 1px solid black;margin:auto;">
	 		<br>
	 		<b class="div_header">Run Log</b> <span id="run_jobs_arrow" class="div_arrow" style="cursor:pointer;">&#x25BA;</span>
		 	<br><br>
	 		<div id="run_jobs_div" class="expand_collapse_div" style="display:none;">
		 		<img id="refresh_log" src="<%=request.getContextPath() %>/images/arrow-circle-double-135.png" style="align:center;padding:3px;cursor:pointer;" valign="middle">
		 		<br>
		 		<table id="table_scheduler_log_results" class="table_scheduler_class" style="margin:auto;width:45%;border-collapse:collapse;border-spacing:0px;">
		 			<thead style=""><th>ID</th><th style="min-width:40px;">Project Code</th><th style="min-width:40px;">Start Time</th><th style="min-width:40px;">End Time</th><th style="min-width:40px;">Duration (s)</th><th style="min-width:40px;">Run Notes</th></thead>
		 		</table>
		 		<br><br>
	 		</div>
 		</div>
	</div>
</body>