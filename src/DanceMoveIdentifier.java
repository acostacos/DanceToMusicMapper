import java.util.ArrayList;

//Goal of class is to identify what moves are present
public class DanceMoveIdentifier {
	//Make functions for each move. Calls Excel to extract data from the necessary columns
	
	public static Excel ex;
	
	public DanceMoveIdentifier() {
		ex = new Excel();
	}
	
	public boolean IsHead() {
		ArrayList<double[]> j = ex.GetColumnsForJoint("Position", "Head");
		//Counts how many movements pass the 5 cm threshold
		int sigMoveCount = 0;
		
		for(int i=0; i<j.size()-1; i++) {
			double[] r = j.get(i);
			double[] r2 = j.get(i+1);
			if(Math.sqrt(Math.pow(r[1]-r2[1], 2)+Math.pow(r[2]-r2[2], 2)+Math.pow(r[3]-r2[3], 2))>=0.05){
				sigMoveCount++;
			}
		}
		return sigMoveCount>4;
	}
	
	public boolean IsHips() {
		//Only check's hip, not anymore left hip and right hip since redundant
		ArrayList<double[]> j = ex.GetColumnsForJoint("Position", "Hips");
		//Counts how many movements pass the 5 cm threshold
		int sigMoveCount = 0;
		
		for(int i=0; i<j.size()-1; i++) {
			double[] r = j.get(i);
			double[] r2 = j.get(i+1);
			if(Math.sqrt(Math.pow(r[1]-r2[1], 2)+Math.pow(r[2]-r2[2], 2)+Math.pow(r[3]-r2[3], 2))>=0.05){
				sigMoveCount++;
			}
		}
		return sigMoveCount>4;
	}
	
	public boolean IsGroove() {
		ArrayList<double[]> j = ex.GetColumnsForJoint("Position", "ShoulderCenter");		
		//Counts how many movements pass the 5 cm threshold
		int sigMoveCount = 0;
		
		for(int i=0; i<j.size()-1; i++) {
			double[] r = j.get(i);
			double[] r2 = j.get(i+1);
			//See if the vector created has a modulus greater than or equal to 5
			if(Math.sqrt(Math.pow(r[1]-r2[1], 2)+Math.pow(r[2]-r2[2], 2)+Math.pow(r[3]-r2[3], 2))>=0.05){
				sigMoveCount++;
			}
		}
		return sigMoveCount>4;
	}
	
	public int IsStepOrHop() {
		boolean step = IsStep();
		boolean hop = IsHop();
		if(step && hop) {
			return 1;
		}
		else if(step) {
			return 2;
		}
		return 0;
	}
	
	public boolean IsStep() {
		String[] joints = {"KneeLeft", "AnkleLeft", "KneeRight", "AngleRight"};
		ArrayList<ArrayList<double[]>> j = new ArrayList<ArrayList<double[]>>();
		for(int i=0; i<joints.length; i++) {
			j.add(ex.GetColumnsForJoint("Position", joints[i]));
		}
		
		//Counts how many movements pass the 5 cm threshold
		int sigMoveCount = 0;
		//Keep repeating for all the rows recorded
		for(int a=0; a<j.size(); a++) {
			ArrayList<double[]> tempJ = j.get(a);
			//checks if all joints move
			boolean jointsAllMove = true;
			for(int i=0; i<tempJ.size()-1; i++) {
				double[] r = tempJ.get(i);
				double[] r2 = tempJ.get(i+1);
				//See if the vector created has a modulus that is NOT greater than or equal to 5
				if(!(Math.sqrt(Math.pow(r[1]-r2[1], 2)+Math.pow(r[2]-r2[2], 2)+Math.pow(r[3]-r2[3], 2))>=0.05)){
					jointsAllMove = false;
				}
			}
			if(jointsAllMove) {
				sigMoveCount++;
			}
		}
		
		return sigMoveCount>4;
	}
	
	public boolean IsHop() {
		//Checks joints not in step to see if they are present
		String[] joints = {"HipLeft", "HipRight", "Spine", "ShoulderCenter", "Head"};
		ArrayList<ArrayList<double[]>> j = new ArrayList<ArrayList<double[]>>();
		for(int i=0; i<joints.length; i++) {
			j.add(ex.GetColumnsForJoint("Position", joints[i]));
		}
		
		//Counts how many movements pass the 5 cm threshold
		int sigMoveCount = 0;
		//Keep repeating for all the rows recorded
		for(int a=0; a<j.size(); a++) {
			ArrayList<double[]> tempJ = j.get(a);
			//checks if all joints move
			boolean jointsAllMove = true;
			for(int i=0; i<tempJ.size()-1; i++) {
				double[] r = tempJ.get(i);
				double[] r2 = tempJ.get(i+1);
				//See if the vector created has a modulus that is NOT greater than or equal to 5
				if(!(Math.sqrt(Math.pow(r[1]-r2[1], 2)+Math.pow(r[2]-r2[2], 2)+Math.pow(r[3]-r2[3], 2))>=0.05)){
					jointsAllMove = false;
				}
			}
			if(jointsAllMove) {
				sigMoveCount++;
			}
		}
		
		return sigMoveCount>4;
	}
	
	public int IsWaveOrThrow() {
		if(IsWave()) {
			return 1;
		}
		else if(IsThrow()) {
			return 2;
		}
		return 0;
	}
	
	public boolean IsThrow() {
		String[] joints = {"ShoulderLeft", "ElbowLeft", "WristLeft", "ShoulderRight", "ElbowRight", "WristRight"};
		ArrayList<ArrayList<double[]>> j = new ArrayList<ArrayList<double[]>>();
		for(int i=0; i<joints.length; i++) {
			j.add(ex.GetColumnsForJoint("Position", joints[i]));
		}
		
		//Counts how many movements pass the 10 cm threshold
		int sigMoveCount = 0;
		//Keep repeating for all the rows recorded
		for(int a=0; a<j.size(); a++) {
			ArrayList<double[]> tempJ = j.get(a);
			//checks if all joints move
			boolean jointsAllMove = true;
			for(int i=0; i<tempJ.size()-1; i++) {
				double[] r = tempJ.get(i);
				double[] r2 = tempJ.get(i+1);
				//See if the vector created has a modulus that is NOT greater than or equal to 10
				if(!(Math.sqrt(Math.pow(r[1]-r2[1], 2)+Math.pow(r[2]-r2[2], 2)+Math.pow(r[3]-r2[3], 2))>=0.1)){
					jointsAllMove = false;
				}
			}
			if(jointsAllMove) {
				sigMoveCount++;
			}
		}
		
		return sigMoveCount>4;
	}
	
	public boolean IsWave() {
		String[] leftJoints = {"ShoulderLeft", "ElbowLeft", "WristLeft"};
		String[] rightJoints = {"ShoulderRight", "ElbowRight", "WristRight"};
		
		ArrayList<ArrayList<double[]>> lj = new ArrayList<ArrayList<double[]>>();
		ArrayList<ArrayList<double[]>> rj = new ArrayList<ArrayList<double[]>>();
		
		for(int i=0; i<leftJoints.length; i++) {
			lj.add(ex.GetColumnsForJoint("Position", leftJoints[i]));
			rj.add(ex.GetColumnsForJoint("Position", rightJoints[i]));
		}
		
		int lCounter = 0;
		int rCounter = 0;
		
		//Do it by row and then by column
		for(int a=0; a<lj.get(0).size(); a++) {
			for(int i=0; i<lj.size(); i++) {
				ArrayList<double[]> leftJ = lj.get(i);
				ArrayList<double[]> rightJ = rj.get(i);
				
				double[] lr = leftJ.get(a);
				double[] lr2 = leftJ.get(a+1);
				//Check first joint (Shoulder left/Shoulder right OR  Wrist left/Wrist right) if it moves
				if(Math.sqrt(Math.pow(lr[1]-lr2[1], 2)+Math.pow(lr[2]-lr2[2], 2)+Math.pow(lr[3]-lr2[3], 2))>=0.03){
					//If it does, add 1 to the counter and check the next 
					lCounter++;
				}
				else {
					//else set it back to 0. Thus, you need to get 3 in a row
					lCounter=0;
				}
				
				//Do the same for right
				double[] rr = rightJ.get(a);
				double[] rr2 = rightJ.get(a+1);
				if(Math.sqrt(Math.pow(rr[1]-rr2[1], 2)+Math.pow(rr[2]-rr2[2], 2)+Math.pow(rr[3]-rr2[3], 2))>=0.03){
					rCounter++;
				}
				else {
					rCounter=0;
				}
			}
			//check if either reach 3 in a row
			if(lCounter>=leftJoints.length || rCounter>=rightJoints.length) {
				return true;
			}
			
		}
		
		return false;
	}
}
