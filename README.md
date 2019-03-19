### What is This?

This is a Java Spring web application that is currently called [R2D4](https://dataline.mskcc.org/DataLine). It is used to manage and automate DataLine requests from researchers and administrators (entered through the [DataLine Request Form](http://darwin.mskcc.org/resources/dataline_request.htm)).

### Login Screen
![](2019-02-23%2014_54_13-Window.png)

### Developer Dashboard
![](https://github.mskcc.org/singerm/R2D4/blob/master/2019-02-23%2014_57_53-Window.png)
- The left-hand side lists all requests and has extensive search options to look up past requests (including advanced search). Clicking on the folder icon will open up the network folder for that request (IE only)
- The right-hand side shows information about the request selected (from the left-hand side) and shows related email (middle-column bottom - based on email subject containing the request code (i.e. RAD17547))
- The SQL section (right-side middle) allows a developer to paste in SQL and click on the Excel button to create an Excel file with the data returned from the SQL statement (in the network folder)
- Pasting in SQL also auto-populates the custodians section by extracting table names from the SQL and looking up table custodians
- The email sending section (right-side bottom) automates email creation by opening Outlook with "To:" fields for Requester and/or Custodians and populating the "Body" text with text from the request

### Existing Scheduled Jobs
![](https://github.mskcc.org/singerm/R2D4/blob/master/2019-02-23%2014_59_18-Window.png)
- The top panel allows for creating or editing a scheduled job. Jobs can be in SQL or Python and can email or move a file to a network location. Jobs can be run daily, weekly, bi-weekly, monthly or every N months.
- Jobs can also have a specified ETL pre-requisite in order to run. If that ETL pre-req is not completed, the job will be held until the pre-req is complete (checking every minute)
- There is also the ability to run a job now by clicking the green plus for the respective job.

### Scheduled Jobs Run Log
![](https://github.mskcc.org/singerm/R2D4/blob/master/2019-02-23%2014_59_45-Window.png)
- After a job is run, the start and end times, duration and any errors or warnings are recorded in the run log table.

### User Preferences
![](https://github.mskcc.org/singerm/R2D4/blob/master/2019-02-23%2015_01_28-Window.png)
+ Users can specifiy:
   + Email folders and sub-folders to search for related email messages (a related email has a subject that contains a DataLine Project Code (i.e. MED16440))
   + Template for various emails sent to requesters and custodians (i.e. putting &lt;requester_first_name&gt; will insert the requester's first name for the respective request)# R2D4
