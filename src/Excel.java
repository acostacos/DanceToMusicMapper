import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Excel 
{
	private static FileInputStream fis;
	private static Workbook wb;
	private static Sheet sh;
	
	public Excel() {
		try {
			fis = new FileInputStream("./files/Test1.xlsx");
			wb = WorkbookFactory.create(fis);
		}
		catch(IOException io) {
			System.out.println("Cannot find file");
			System.out.println(io);
		}
	}
	
	public ArrayList<double[]> GetAllColumnsFromSheet(String sheet) {
		if(!this.ValidateSheetName(sheet)) {
			return null;
		}
		
		sh = wb.getSheet("Kinect Data (" + sheet + ")");
		ArrayList<double[]> data = new ArrayList<double[]>(); 
		double[] dataRow;
		
		for(int i=1; i<sh.getLastRowNum(); i++) {
			Row r = sh.getRow(i);
			dataRow = new double[r.getLastCellNum()];
			for(int j=0; j<r.getLastCellNum(); j++) {
				Cell c = r.getCell(j);
				dataRow[j] = c.getNumericCellValue();
			}
			data.add(dataRow);
		}
		
		return data;
	}
	
	public ArrayList<double[]> GetColumnsForJoint(String sheet, String joint){
		if(!this.ValidateSheetName(sheet)) {
			return null;
		}
		
		sh = wb.getSheet("Kinect Data (" + sheet + ")");
		int jointIndex = this.GetJointIndex(joint);
		ArrayList<double[]> data = new ArrayList<double[]>(); 
		double[] dataRow;
		
		for(int i=1; i<sh.getLastRowNum(); i++) {
			Row r = sh.getRow(i);
			dataRow = new double[4];
			//add a row for the corresponding time
			dataRow[0] = r.getCell(0).getNumericCellValue();
			int counter = 1;
			for(int j=jointIndex; j<(jointIndex+3); j++) {
				Cell c = r.getCell(j);
				dataRow[counter] = c.getNumericCellValue();
				counter++;
			}
			data.add(dataRow);
		}
		
		return data;
	}
	
	//include start time but excludes end time
	public ArrayList<double[]> GetColumnsForJointByTime(String sheet, String joint, double startTime, double endTime){
		if(!this.ValidateSheetName(sheet)) {
			return null;
		}
		
		sh = wb.getSheet("Kinect Data (" + sheet + ")");
		int jointIndex = this.GetJointIndex(joint);
		ArrayList<double[]> data = new ArrayList<double[]>(); 
		double[] dataRow;
		
		for(int i=1; i<sh.getLastRowNum()+1; i++) {
			Row r = sh.getRow(i);
			if(r.getCell(0).getNumericCellValue()>=endTime) {
				break;
			}
			if(r.getCell(0).getNumericCellValue()>=startTime) {
				dataRow = new double[4];
				//add a row for the corresponding time
				dataRow[0] = r.getCell(0).getNumericCellValue();
				int counter = 1;
				for(int j=jointIndex; j<(jointIndex+3); j++) {
					Cell c = r.getCell(j);
					dataRow[counter] = c.getNumericCellValue();
					counter++;
				}
				data.add(dataRow);
			}
		}
		
		return data;
	}
	
	public ArrayList<double[]> GetRowsForTime(String sheet, double time){
		if(!this.ValidateSheetName(sheet)) {
			return null;
		}
		
		sh = wb.getSheet("Kinect Data (" + sheet + ")");
		ArrayList<double[]> data = new ArrayList<double[]>(); 
		double[] dataRow;
		
		for(int i=1; i<sh.getLastRowNum(); i++) {
			Row r = sh.getRow(i);
			if(r.getCell(0).getNumericCellValue()==time) {
				for(int j=1; j<r.getLastCellNum(); j+=3) {
					dataRow = new double[4];
					
					dataRow[0] = r.getCell(0).getNumericCellValue();
					dataRow[1] = r.getCell(j).getNumericCellValue();
					dataRow[2] = r.getCell(j+1).getNumericCellValue();
					dataRow[3] = r.getCell(j+2).getNumericCellValue();
					
					data.add(dataRow);
				}
				break;
			}
		}
			
		return data;
	}
	
	//Helper Functions
	private boolean ValidateSheetName(String sheet) {
		return sheet == "Position" || sheet == "Velocity" || sheet == "Acceleration";
	}
	
	private int GetJointIndex(String joint) {		
		switch(joint) {
			case "Hip":
				return 1;
			case "Spine":
				return 4;
			case "ShoulderCenter":
				return 7;
			case "Head":
				return 10;
			case "ShoulderLeft":
				return 13;
			case "ElbowLeft":
				return 16;
			case "WristLeft":
				return 19;
			case "HandLeft":
				return 22;
			case "ShoulderRight":
				return 25;
			case "ElbowRight":
				return 28;
			case "WristRight":
				return 31;
			case "HandRight":
				return 34;
			case "HipLeft":
				return 37;
			case "KneeLeft":
				return 40;
			case "AnkleLeft":
				return 43;
			case "FootLeft":
				return 46;
			case "HipRight":
				return 49;
			case "KneeRight":
				return 52;
			case "AnkleRight":
				return 55;
			case "FootRight":
				return 58;
		}
		
		return 0;
	}
}
