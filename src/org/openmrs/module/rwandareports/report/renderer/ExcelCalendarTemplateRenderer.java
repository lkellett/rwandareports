package org.openmrs.module.rwandareports.report.renderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ExcelUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationUtil;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.renderer.ExcelStyleHelper;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.renderer.ReportTemplateRenderer;
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;


@Handler
public class ExcelCalendarTemplateRenderer extends ReportTemplateRenderer {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private static final String DRAW_CALENDAR_HERE = "DRAW CALENDAR WIDGET HERE";
	
	private static final SimpleDateFormat sdfIndicatorVar = new SimpleDateFormat("yyyyMMdd");
	
	//TODO: internationalize
	private static final String[] days = {
        "Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday"};

	private static final String[]  months = {
        "January", "February", "March","April", "May", "June","July", "August",
        "September","October", "November", "December"};


	public ExcelCalendarTemplateRenderer() {
		super();
	}
	
	/** 
	 * @see ReportRenderer#render(ReportData, String, OutputStream)
	 */
	@SuppressWarnings("unchecked")
	public void render(ReportData reportData, String argument, OutputStream out) throws IOException, RenderingException {
		
		log.debug("Attempting to render report with ExcelTemplateRenderer");
		InputStream is = null;
		SimpleDateFormat localDF = Context.getDateFormat();
		try {
			ReportDesign design = getDesign(argument);
			ReportDesignResource r = getTemplate(design);
			is = new ByteArrayInputStream(r.getContents());
			POIFSFileSystem fs = new POIFSFileSystem(is);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			ExcelStyleHelper styleHelper = new ExcelStyleHelper(wb);
			log.debug("New Workbook Constructed");
			
			HSSFSheet sheet = wb.getSheetAt(0);
			sheet.setForceFormulaRecalculation(true);
			//HERE:  insert a calendar
			
			
			{
				Date startDate = (Date) reportData.getContext().getParameterValue("startDate");
				Date endDate = (Date) reportData.getContext().getParameterValue("endDate");
				Calendar cal = new GregorianCalendar();
				cal.setTime(startDate);
				
				if (startDate.getTime() >= endDate.getTime())
					throw new IllegalArgumentException("start date must be before end date!");
				
				HSSFCell calStartCell = getCellWithCalendarInsert(sheet);
				//these are 0-based indexes
				if (calStartCell == null)
						throw new RuntimeException("Unable to find calendar pointer cell");
				int colWeekStartNum = calStartCell.getColumnIndex() + 1;
				int dateCol = calStartCell.getColumnIndex();
				int colRowStartNum = calStartCell.getRowIndex();
				
				//clear the calendar insert cell
				calStartCell.setCellValue(new HSSFRichTextString(""));
				
				HSSFRow insertRow = sheet.getRow(calStartCell.getRowIndex());			
				//add buffer rows above and below where the calendar goes?
				//sheet.createRow(colRowStartNum+1);
				//sheet.createRow(colRowStartNum);

				//headers
				HSSFCellStyle headerStyle = wb.createCellStyle();
                headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                HSSFFont fontBold = wb.createFont();
                fontBold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                headerStyle.setFont(fontBold);
                int colWeekStartNumIter = colWeekStartNum;
	            for (int i = 0; i < days.length; i++) {
	                //set column widths, the width is measured in units of 1/256th of a character width
	                //sheet.setColumnWidth(i*2, 5*256); //the column is 5 characters wide
	                //sheet.setColumnWidth(i*2 + 1, 13*256); //the column is 13 characters wide
	                HSSFCell monthCell = insertRow.createCell(colWeekStartNumIter);
	                monthCell.setCellType(HSSFCell.CELL_TYPE_STRING);
	                monthCell.setCellStyle(headerStyle);
	                HSSFRichTextString str = new HSSFRichTextString(days[i]);
	                str.applyFont(fontBold);
	                monthCell.setCellValue(str);
	                sheet.addMergedRegion(new CellRangeAddress(insertRow.getRowNum(), insertRow.getRowNum(), colWeekStartNumIter, colWeekStartNumIter + 2));
	                colWeekStartNumIter = colWeekStartNumIter + 3;
	            }
	            
	            

	            HSSFCellStyle oddStyle = wb.createCellStyle();
	            oddStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
	            HSSFPalette palette = wb.getCustomPalette();
	            palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index,
	                    (byte) 225,  //RGB red (0-255)
	                    (byte) 225,    //RGB green
	                    (byte) 245     //RGB blue
	            );
	            oddStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
	            oddStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	            
	            HSSFCellStyle evenStyle = wb.createCellStyle();
	            evenStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
	     
	            
	            //Now create the week rows, starting with sunday before or equal to start date
	            Calendar calendarStart = RwandaReportsUtil.findSundayBeforeOrEqualToStartDate(startDate);
	            while (calendarStart.getTime().getTime() <= endDate.getTime()){
	            	//draw a whole week:  0-based
	            	//shift all contents of spreadsheet down 1 row:
	            	insertRow = sheet.createRow(insertRow.getRowNum() + 1);
	            	sheet.shiftRows(insertRow.getRowNum(), sheet.getLastRowNum(), 1);
	            	HSSFCell dateCell = insertRow.createCell(dateCol);
	            		
	            	dateCell.setCellType(HSSFCell.CELL_TYPE_STRING);
	            	if (dateCell.getRowIndex() % 2 == 0)
	            		dateCell.setCellStyle(oddStyle);
	            	else
	            		dateCell.setCellStyle(evenStyle);
	            	HSSFRichTextString weekDate = new HSSFRichTextString(localDF.format(calendarStart.getTime()));
	            	weekDate.applyFont(fontBold);
	            	dateCell.setCellValue(weekDate);
	            	colWeekStartNumIter = colWeekStartNum;
	            	Calendar weeklyCal = new GregorianCalendar();
	            	weeklyCal.setTime(calendarStart.getTime());
	            	for (int i = 0; i < days.length; i++) {
	            		HSSFCell monthCell = insertRow.createCell(colWeekStartNumIter);
		                monthCell.setCellType(HSSFCell.CELL_TYPE_STRING);
		            	if (insertRow.getRowNum() % 2 == 0)
		            		monthCell.setCellStyle(oddStyle);
		            	else
		            		monthCell.setCellStyle(evenStyle);
		                monthCell.setCellValue(new HSSFRichTextString("#cal_" + sdfIndicatorVar.format(weeklyCal.getTime()) + "#")); //indicator_name
		                sheet.addMergedRegion(new CellRangeAddress(insertRow.getRowNum(), insertRow.getRowNum(), colWeekStartNumIter, colWeekStartNumIter + 2));
		                colWeekStartNumIter = colWeekStartNumIter + 3;
		                weeklyCal.add(Calendar.DATE, 1);
		            }
	            	calendarStart.add(Calendar.DATE, 7);
	            }

			}
			
			
			// TODO: Implement more complex logic around multiple sheets for multiple rows / multiple datasets
			if (reportData.getDataSets().size() != 1) {
				throw new RuntimeException("Currently only one dataset is supported.");
			}
			Iterator<Map.Entry<String, DataSet>> datSetEntryIterator = reportData.getDataSets().entrySet().iterator();
			Map.Entry<String, DataSet> dataSetEntry = datSetEntryIterator.next();
			DataSetRow dataSetRow = (DataSetRow)dataSetEntry.getValue().iterator().next();
			if (datSetEntryIterator.hasNext()) {
				throw new RuntimeException("Currently only one dataset with one row is supported.");
			}
			
			Map<String, Object> replacements = getReplacementData(reportData, design, dataSetEntry.getKey(), dataSetRow);
			
			String prefix = getExpressionPrefix(design);
			String suffix = getExpressionSuffix(design);
			
			for (Iterator<HSSFRow> rowIter = sheet.rowIterator(); rowIter.hasNext();) {
				HSSFRow row = rowIter.next();
				for (Iterator<HSSFCell> cellIter = row.cellIterator(); cellIter.hasNext();) {
					HSSFCell cell = cellIter.next();
			    	String contents = ExcelUtil.getCellContentsAsString(cell);
			    	if (StringUtils.isNotEmpty(contents)) {
			    		Object newContent = EvaluationUtil.evaluateExpression(contents, replacements, prefix, suffix);
			    		ExcelUtil.setCellContents(styleHelper, cell, newContent);
			    	}
				}
			}
			wb.write(out);
		}
		catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}
	
	private HSSFCell getCellWithCalendarInsert(HSSFSheet sheet){
		for (Iterator<HSSFRow> rit = sheet.rowIterator(); rit.hasNext(); ) {
			HSSFRow row = rit.next();
			for (Iterator<HSSFCell> cit = row.cellIterator(); cit.hasNext(); ) {
				HSSFCell cell = cit.next();
				if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING && cell.getRichStringCellValue().getString().equals(DRAW_CALENDAR_HERE)){
					return cell;	
				}
			}
		}
		log.info("Unable to find " + DRAW_CALENDAR_HERE + " in template.");
		return null;
	}
	
}
