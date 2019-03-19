Number.prototype.formatMoney = function(c, d, t){
	var n = this, 
	    c = isNaN(c = Math.abs(c)) ? 2 : c, 
	    d = d == undefined ? "." : d, 
	    t = t == undefined ? "," : t, 
	    s = n < 0 ? "-" : "", 
	    i = String(parseInt(n = Math.abs(Number(n) || 0).toFixed(c))), 
	    j = (j = i.length) > 3 ? j % 3 : 0;
	   return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
 };
 
 function exportTableToExcel(tablename, filename) {
	
	var excelData = '<?xml version="1.0"?><?mso-application progid="Excel.Sheet"?><Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet" xmlns:html="http://www.w3.org/TR/REC-html40"> <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office"> <Version>12.00</Version> </DocumentProperties> <ExcelWorkbook xmlns="urn:schemas-microsoft-com:office:excel"> <WindowHeight>8130</WindowHeight> <WindowWidth>15135</WindowWidth> <WindowTopX>120</WindowTopX> <WindowTopY>45</WindowTopY> <ProtectStructure>False</ProtectStructure> <ProtectWindows>False</ProtectWindows> </ExcelWorkbook> <Styles><Style ss:ID="xls-style-1" ss:Name="xls-style-1"><Alignment ss:Vertical="Bottom" ss:Horizontal="Center"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#FFFFFF" /><Interior ss:Color="#2875B4" ss:Pattern="Solid"/></Style><Style ss:ID="xls-style-2" ss:Name="xls-style-2"><Alignment ss:Vertical="Bottom" ss:Horizontal="Center"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#DEEDF5" ss:Pattern="Solid"/></Style><Style ss:ID="xls-style-3" ss:Name="xls-style-3"><Alignment ss:Vertical="Bottom" ss:Horizontal="Center"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/></Style><Style ss:ID="xls-style-4" ss:Name="xls-style-4"><Alignment ss:Vertical="Bottom" ss:Horizontal="Center"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/><NumberFormat ss:Format="Currency"/></Style><Style ss:ID="xls-style-5" ss:Name="xls-style-5"><Alignment ss:Vertical="Bottom" ss:Horizontal="Center"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/><NumberFormat ss:Format="Short Date"/></Style><Style ss:ID="xls-style-6" ss:Name="xls-style-6"><Alignment ss:Vertical="Bottom" ss:Horizontal="Center"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#FFFFFF" ss:Pattern="Solid"/></Style><Style ss:ID="xls-style-7" ss:Name="xls-style-7"><Alignment ss:Vertical="Bottom" ss:Horizontal="Left"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#DEEDF5" ss:Pattern="Solid"/><NumberFormat ss:Format="Currency"/></Style><Style ss:ID="xls-style-8" ss:Name="xls-style-8"><Alignment ss:Vertical="Bottom" ss:Horizontal="Left"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#DEEDF5" ss:Pattern="Solid"/><NumberFormat ss:Format="Short Date"/></Style><Style ss:ID="xls-style-9" ss:Name="xls-style-9"><Alignment ss:Vertical="Bottom" ss:Horizontal="Left"/><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/><Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#60AAD2"/></Borders><Font ss:Color="#222222" /><Interior ss:Color="#DEEDF5" ss:Pattern="Solid"/></Style></Styles><Worksheet ss:Name="Sheet1"><Table><Column ss:Width="100"/><Column ss:Width="200"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="148"/><Column ss:Width="136"/><Column ss:Width="126"/><Column ss:Width="103"/><Column ss:Width="108"/><Column ss:Width="193"/><Column ss:Width="256"/><Column ss:Width="98"/><Column ss:Width="128"/><Column ss:Width="102"/><Column ss:Width="183"/><Column ss:Width="179"/><Column ss:Width="170"/><Column ss:Width="220"/><Column ss:Width="217"/><Column ss:Width="208"/><Column ss:Width="262"/><Column ss:Width="258"/><Column ss:Width="249"/><Column ss:Width="245"/><Column ss:Width="241"/><Column ss:Width="233"/><Column ss:Width="224"/><Column ss:Width="220"/><Column ss:Width="211"/><Column ss:Width="129"/><Column ss:Width="70"/><Column ss:Width="123"/><Column ss:Width="190"/><Column ss:Width="244"/><Column ss:Width="154"/><Column ss:Width="207"/><Column ss:Width="68"/><Column ss:Width="122"/><Column ss:Width="120"/><Column ss:Width="161"/><Column ss:Width="97"/><Column ss:Width="158"/><Column ss:Width="102"/><Column ss:Width="155"/><Column ss:Width="201"/><Column ss:Width="133"/><Column ss:Width="66"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="136"/><Column ss:Width="123"/><Column ss:Width="108"/><Column ss:Width="156"/><Column ss:Width="106"/><Column ss:Width="193"/><Column ss:Width="169"/><Column ss:Width="133"/><Column ss:Width="157"/><Column ss:Width="122"/><Column ss:Width="174"/><Column ss:Width="121"/><Column ss:Width="156"/><Column ss:Width="93"/><Column ss:Width="163"/><Column ss:Width="148"/><Column ss:Width="126"/><Column ss:Width="155"/><Column ss:Width="116"/><Column ss:Width="124"/><Column ss:Width="104"/><Column ss:Width="146"/><Column ss:Width="56"/><Column ss:Width="63"/><Column ss:Width="52"/>';
	
	var headerRow = '<Row>\n';
	
	console.log(tablename);
	
	$.each( $( tablename + ' .tablesorter-headerRow th:visible' ), function(key, val) { 
		headerRow += '<Cell ss:StyleID="xls-style-1"><Data ss:Type="String">' + $(val)[0].textContent + '</Data></Cell>\n';
	});
	headerRow += '</Row>';
	
	var c=1;
	var cellRow = '';
	var odd = true;
	$.each( $( tablename + " .tableRow"), function(key, val) {
		cellRow += '<Row>';
		var odd = (c % 2 != 0) ? true : false;
		$.each( $(val)[0].cells, function(key1, val1) {
			if ( $(val1).css('display') != 'none' ) {
				var style = (odd) ? 'xls-style-3' : 'xls-style-2';
				cellRow += '<Cell ss:StyleID="' + style + '"><Data ss:Type="String">' + val1.textContent + '</Data></Cell>';
			}
		});
		cellRow += '</Row>';
		c++;	
	});
	
	fileEnd = '</Table></Worksheet></Workbook>';
	
	var blob = new Blob([excelData + headerRow + cellRow + fileEnd], {type: "application/vnd.ms-excel"});
	if (window.navigator.msSaveBlob) { // IE 10+
		window.navigator.msSaveOrOpenBlob(blob, filename);
	} 
	else {
		var downloadUrl = URL.createObjectURL(blob);
		$(this).attr({ 'download': filename, 'href': downloadUrl, 'target': '_blank' });
	}
}

function errorMsg(status, textStatus, errorThrown) {
	//console.log("in errorMsg");
	//alert('Error Timeout. Logout and then login again.\n status: ' + );
	//alert('Error.\nstatus: ' + status + ', textStatus: ' + textStatus + ', errorThrown: ' + errorThrown);
	if (status == 408) {
		alert('Error: Session Timeout. Logout and then login again, please.\nStatus: ' + status);
	}
	else if (status == 382) {
		alert('Error: Email address not found in Active Directory.\nStatus: ' + status);
	}
	else if (status == 285) {
		alert('Error: Project not found.\nStatus: ' + status);
	}
	else {
		alert('Error.\nstatus: ' + status);
	}
}