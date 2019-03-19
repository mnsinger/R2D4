<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>

  <title>DataLine Dashboard</title>
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/CSS/style.login.css" />
  <link rel="icon" 
      type="image/png" 
      href="<%=request.getContextPath() %>/images/r2-d21600.png">
  <script type="text/javascript" src="<%=request.getContextPath() %>/JS/jquery-3.2.1.slim.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath() %>/JS/js.cookie.js"></script>
  <script>
      function validateEmail(email) {
          var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
          return re.test(email);
      }
      function validate(type) {
        //console.log( $("#username_reg").val() );
        console.log( $("#tab1").is(":visible") );
        console.log( $("#tab2").is(":visible") );
        console.log( $('form[name="register"]') );
        // login
        if ($("#tab1").is(":visible") ) {
          console.log("login");
          //if (!validateEmail( $("#username").val() )) { alert('Not a valid email.'); return false; }
          //if ($("#password").val() == 'dl')
          Cookies.set('username', $('#username').val(), { expires: 3650 });
          document.getElementById("login").submit();
          //return false;
        }
        // sign-up
        else {
          console.log("register");
          console.log( $("#username_reg").val() );
          console.log( $("#password_reg").val() );
          if (!validateEmail( $("#username_reg").val() )) { alert('Not a valid email.'); return false; }
          if ( $("#password_reg").val() != $("#password_reg_re").val() || $("#password_reg").val().length == 0 ) { alert('Not a valid password.'); return false; }
          //$('input[name=username]').val( $("#username_reg").val() );
          $('#register input[name=username]').val( $("#username_reg").val() );
          $('input[name=password]').val( $("#password_reg").val() );
          document.getElementById("register").submit();
          //return false;
        }
      }
      // Wait until the DOM has loaded before querying the document
      $(document).ready(function() {
    	  
    	  
    	  var uCookie = Cookies.get('username');
    	  if (uCookie != null) {
    		  $('#username').val(uCookie);
    		  $('#password').focus();
    	  }
    	  else {
        	  $('#username').focus();
    	  }

    	  $(document).keypress(function(e) {
    		  if (e.which == 13) {
    			  validate("log");
    		  }
    	  });
    	  
      	$('ul.tabs').each(function() {
      		// For each set of tabs, we want to keep track of
      		// which tab is active and its associated content
      		var $active, $content, $links = $(this).find('a');
      
      		// If the location.hash matches one of the links, use that as the active tab.
      		// If no match is found, use the first link as the initial active tab.
      		$active = $($links.filter('[href="'+location.hash+'"]')[0] || $links[0]);
      		$active.addClass('active');
      
      		$content = $($active[0].hash);
      
      		// Hide the remaining content
      		$links.not($active).each(function () {
      			$(this.hash).hide();
      		});
      
      		// Bind the click event handler
      		$(this).on('click', 'a', function(e){
      			// Make the old tab inactive.
      			$active.removeClass('active');
      			$content.hide();
      
      			// Update the variables with the new link and content
      			$active = $(this);
      			$content = $(this.hash);
      
      			// Make the tab active.
      			$active.addClass('active');
      			$content.show();
      
      			// Prevent the anchor's default click action
      			e.preventDefault();
      		});
      	});
      });
  </script>

</head>

	<body>
	<!-- <%=request.getContextPath() %> -->
	<br><br>
             <div class="center">
		<ul class='tabs'>
			<li><a href='#tab1'>Login</a></li>
			<!-- li><a href='#tab2'>Sign Up</a></li>
			<li><a href='#tab3'>FAQ</a></li> <!-- onClick='window.location="faq.html";' -->
		</ul>
		<div id='tab1' style='border:1px solid black;text-align:center;'>
                    <form:form id='login' action='login.html' method='post' accept-charset='UTF-8' modelAttribute="loginBean">
                    <fieldset style="padding:10px; border:2px solid; width:100%">
                    <legend>Account Info</legend>
                    <input type='hidden' name='submitted_log' id='submitted_log' value='1'/>
                    <br>
                    Username:<br>
                    <form:input type='text' id='username' path='username' size='30' value='' placeholder='' />
                    <br><br>
                    Password:<br>
                    <form:input type='password' id='password' path='password' size='30' value='' placeholder='' />
                    <br><br>
                    <div style="text-align:center;"><a class='blueButton' onClick='validate("log");return false;'>Submit</a></div>
                    <br>
                    <div style="font-size:11px;font-style:italic;color:red;">${message}</div>
                    </fieldset>
                    </form:form><br><br>
                    <!-- div class='center' style='text-align:center'><a href='temp_user.php'>Skip Login and Check Out Site Anonymously >></a></div-->
		</div>
		<!-- div id='tab2' style='border:1px solid black;text-align:center;'>
                    <form id='register' action='login.php' method='post' accept-charset='UTF-8'>
                    <fieldset style="padding:10px; border:2px solid; width:100%">
                    <legend>Register</legend>
                    <input type='hidden' name='submitted_reg' id='submitted_reg' value='1'/>
                    <input type='hidden' name='username' id='username_r'/>
                    <br>
                    Unique ID / Email:<br>
                    <input type='text' id='username_reg' size="30" />
                    <br><br> 
                    <div style="text-align:center;"><a class='blueButton' onClick='validate("reg");return false;'>Submit</a></div>
                    <br><br>
                    </fieldset>
                    </form>
		</div>
		<div id='tab3' style='border:1px solid black;text-align:center;'>
		<div class='div_faq'><br><br>
  Q: What is the purpose of this site?<br>
  A: <span style="font-size:16px;">I wanted a place where I could take my Yahoo! Fantasy NBA team and ESPN Fantasy NBA team and compare them with the other teams in my league. I also wanted to bone up on my programming skills since I made a career redirection from Oracle DBA to more of a full-stack developer.</span>
</div>  
<br><br>
<div class='div_faq'>
  Q: What are the different menu options?<br>
  A: <img src='images/menu.png' align='top'><br><br>
  <ul class='ul_faq'>
    <li> <b>Add New League:</b> You can save multiple leagues and teams under your username. </li>
    <li> <b>Team Rosters:</b> This page will show all the team rosters for a particular league. </li>
    <li> <b>Matchup:</b> This page shows the expected results of a Head-To-Head matchup between two teams in a league. </li>
    <li> <b>Yahoo! Sync:</b> This page enables realtime sync (pull only) of rosters from a Yahoo! League. This uses OAuth (you won't be asked for your Yahoo! password) and only requires read-only access to your Yahoo! Fantasy League. </li>
  </ul>
</div>  
<br><br>
<div class='div_faq'>
  Q: How are the statistics on the Team Rosters page calculated?<br>
  A: <img src='images/stats.png' align='top'><br><br>
  <ul class='ul_faq'><span style="font-size:18px">Time</span><br>
    <li> <b>Season: </b> Calculate player stats and fantasy team scoreboard based on the entire season's stats.</li>
    <li> <b>Last 5: </b> Calculate player stats and fantasy team scoreboard based on only the last 5 games for each respective player. </li>
    <li> <b>Last 10:</b> Calculate player stats and fantasy team scoreboard based on only the last 10 games for each respective player. </li>
  </ul><br><br>
  <ul class='ul_faq'><span style="font-size:18px">Values</span><br>
    <li> <b>Average: </b> Show player stats as a per game average based on the selected time period (Season, Last 5, Last 10).<br><br></li>
    <li> <b>Rating:  </b> <br><br><blockquote class='bq_faq'><b><i>(For all non-percentage fields [points, rebounds, assists, 3-pointers made, steals, blocks, turnovers, etc.])</i></b> A calculation based as a percentage of the league leader in a given category. For instance, if the league leader is averaging 30 ppg, he would be a 100 for PTS and a player averaging 15 ppg would be a 50, because 15 is 50% of 30.</blockquote> <br><blockquote class='bq_faq'><b><i>(For fg% and ft%)</i></b> A calculation based as a percentage of the league leader, but stretched over the range of values with consideration of the player at the lowest percentage in the league. For example, Jamal Crawford leads the league in FT% at 92.2%, if LeBron James is shooting 72.1%, a regular percentage of the league leader would put him at a rating of 78 (72.1/92.2). However, this is not the best calculation because nobody shoots 0% from the free-throw line. We should also consider the person who is last in the league in free throw percentage (with a qualifying number of attempts) and give him a 0. That would be Andre Drummond at 35.9%. A better calculation for LeBron (and everyone else) would be (LeBron - Min) / (Max - Min) ... (72.1 - 35.9) / (92.2 - 35.9) which would give him a rating of 64. The modified calc would also put Drummond at 0 instead of 39.</blockquote><br><blockquote class='bq_faq'><i>Note on Qualifying attempts: </i> In order to be considered for the percentages of league leader or league lagger (and make a real difference in fantasy basketball), you must reach a cut-off. The cut-offs for number of attempts are averages of: 5+ FGAs per game and 2+ FTAs per game. Notice that players who do not qualify can have values above 100 or below 0. Players who don't qualify will also be excluded from the colored shading option. You should ignore values above 100, below 0 or non-shaded fields since they are insignificant and won't impact your team.</blockquote><br></li>
    <li> <b>Ranking:</b> Simple ranking where the league leader in any given category is 1, next person is 2, then 3, etc. Ties are given the same rank but increased accordingly to lower players. For instance Westbrook with 2.3 steals is 1, Curry with 2.2 steals is 2, Rubio with 2.2 steals is also 2 but Chris Paul at 2.1 steals is 4 (nobody is 3).</li>
  </ul><br><br>
  <ul class='ul_faq'><span style="font-size:18px">Ignore</span><br>
    <li> Selecting any stat in the ignore section will exclude that statistic from the Scoreboard, and Matchup sections and will also exclude those stats from the color shading.</li>
  </ul><br><br>
</div>  <br><br>
<div class='div_faq'>
  Q: How are the statistics on the Team Rosters page calculated?<br>
  A: <img src='images/stats.png' align='top'><br><br>
  <ul class='ul_faq'><span style="font-size:18px">Time</span><br>
    <li> <b>Season: </b> Calculate player stats and fantasy team scoreboard based on the entire season's stats.</li>
    <li> <b>Last 5: </b> Calculate player stats and fantasy team scoreboard based on only the last 5 games for each respective player. </li>
    <li> <b>Last 10:</b> Calculate player stats and fantasy team scoreboard based on only the last 10 games for each respective player. </li>
  </ul><br><br>
  <ul class='ul_faq'><span style="font-size:18px">Values</span><br>
    <li> <b>Average: </b> Show player stats as a per game average based on the selected time period (Season, Last 5, Last 10).<br><br></li>
    <li> <b>Rating:  </b> <br><br><blockquote class='bq_faq'><b><i>(For all non-percentage fields [points, rebounds, assists, 3-pointers made, steals, blocks, turnovers, etc.])</i></b> A calculation based as a percentage of the league leader in a given category. For instance, if the league leader is averaging 30 ppg, he would be a 100 for PTS and a player averaging 15 ppg would be a 50, because 15 is 50% of 30.</blockquote> <br><blockquote class='bq_faq'><b><i>(For fg% and ft%)</i></b> A calculation based as a percentage of the league leader, but stretched over the range of values with consideration of the player at the lowest percentage in the league. For example, Jamal Crawford leads the league in FT% at 92.2%, if LeBron James is shooting 72.1%, a regular percentage of the league leader would put him at a rating of 78 (72.1/92.2). However, this is not the best calculation because nobody shoots 0% from the free-throw line. We should also consider the person who is last in the league in free throw percentage (with a qualifying number of attempts) and give him a 0. That would be Andre Drummond at 35.9%. A better calculation for LeBron (and everyone else) would be (LeBron - Min) / (Max - Min) ... (72.1 - 35.9) / (92.2 - 35.9) which would give him a rating of 64. The modified calc would also put Drummond at 0 instead of 39.</blockquote><br></li>
    <li> <b>Ranking:</b> Simple ranking where the league leader in any given category is 1, next person is 2, then 3, etc. Ties are given the same rank but increased accordingly to lower players. For instance Westbrook with 2.3 steals is 1, Curry with 2.2 steals is 2, Rubio with 2.2 steals is also 2 but Chris Paul at 2.1 steals is 4 (nobody is 3).<br><br></li>
    <li> <b>Qualifying attempts: </b> In order to be considered for the percentages (and make a real difference in fantasy basketball), you must reach a cut-off. The cut-offs for number of attempts are averages of: 5+ FGAs per game and 2+ FTAs per game. Notice that players who do not qualify can have values above 100 or below 0. Players who don't qualify will also be excluded from the colored shading. You should ignore these values since they are insignificant and won't impact your team.
  </ul>
</div-->  
		
		</div>
           </div>
	</body>
</html>
