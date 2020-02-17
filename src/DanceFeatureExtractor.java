import java.util.ArrayList;
import java.util.Arrays;

public class DanceFeatureExtractor {
	//make a variable for each feature
	//0,0 x and y is located in the middle at the bottom
	public static double aveVel; //liner summation of average velocities of each joint
	public static double direcChange; //linear summation of angles between two movements
	public static ArrayList<Double> motionKeyFrame; //when the motions stop moving
	public static double aveAccel; //linear summation of the acceleration of each joint
	public static double[] motionDensity; //how much movement there is for each joint
	public static Excel e;
	
	//constants
	public static final String[] joints = {"Hip", "Spine", "ShoulderCenter", "Head", "ShoulderLeft", "ElbowLeft", "WristLeft", "HandLeft", "ShoulderRight", "ElbowRight", "WristRight", "HandRight", "HipLeft", "KneeLeft", "AnkleLeft", "FootLeft", "HipRight", "KneeRight", "AnkleRight", "FootRight"};
	public static final String[] armJoints = Arrays.copyOfRange(joints, 4, 12);
	public static final String[] legJoints = Arrays.copyOfRange(joints, 12, 20);
	public static final String[] coreJoints = Arrays.copyOfRange(joints, 0, 4);
	public static final double massOfSpecimen = 0;
	public static final double[] massOfParts = {massOfSpecimen*0.1117, massOfSpecimen*0.1633, massOfSpecimen*0.1596,  massOfSpecimen*0.0694, massOfSpecimen*0.0271, massOfSpecimen*0.0162, massOfSpecimen*0.00305, massOfSpecimen*0.00305, massOfSpecimen*0.0271, massOfSpecimen*0.0162, massOfSpecimen*0.00305, massOfSpecimen*0.00305, massOfSpecimen*0.0708, massOfSpecimen*0.0708, massOfSpecimen*0.0433, massOfSpecimen*0.0129, massOfSpecimen*0.0708, massOfSpecimen*0.0708, massOfSpecimen*0.0433, massOfSpecimen*0.0129};
	
	
	
	public DanceFeatureExtractor() {
//		aveVel = getAveVel();
//		direcChange = getDirectChange();
//		motionKeyFrame = getMotionKeyFrame();
//		aveAccel = getAveAccel();
//		motionDensity = getMotionDensity();
		e = new Excel();
	}
	
	//FOR CORE ONLY
	//just add average of all velocities for joints
	//return magnitude of resulting sum of each component
	public double getAveVel(double startTime, double endTime) {
		double xSum = 0;
		double ySum = 0;
		double zSum = 0;
		
		for(int a=0; a<coreJoints.length; a++) {
			ArrayList<double[]> jv = new ArrayList<double[]>();
			jv = e.GetColumnsForJointByTime("Velocity", coreJoints[a], startTime, endTime);
			for(int b=0; b<jv.size(); b++) {
				double[] r = jv.get(b);
				
				xSum += r[1];
				ySum += r[2];
				zSum += r[3];
			}
		}
		
		//return magnitude of the sum of the vectors
		return Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
	}
	
	//FOR ARMS ONLY
	//Use formula (dot product) to compute for the angle and add them all up
	public double getDirectChange() {
		int aSum = 0;
		
		for(int a=0; a<armJoints.length; a++) {
			ArrayList<double[]> jv = e.GetColumnsForJoint("Velocity", armJoints[a]);
			for(int b=0; b<jv.size()-1; b++) {
				double[] r1 = jv.get(b);
				double[] r2 = jv.get(b+1);
				
				//formula for angle between two vectors (returns in radians)
				double angle = Math.acos(((r1[1]*r2[1])+(r1[2]*r2[2])+(r1[3]*r2[3]))/(Math.sqrt(Math.pow(r1[1], 2)+Math.pow(r1[2], 2)+Math.pow(r1[3], 2))*Math.sqrt(Math.pow(r2[1], 2)+Math.pow(r2[2], 2)+Math.pow(r2[3], 2))));
				aSum += angle;
			}
		}
		
		return aSum;
	}
	
	//FOR ALL
	//compute for the Laban Weight component, get the local minimums of the graph
	//local minimum of velocity graph
	public ArrayList<Double> getMotionKeyFrame() {
		ArrayList<double[]> momentum = computeMomentum();
		//Time frame where we check for a local minimum: 4 seconds
		double timeFrame = 4;
		
		ArrayList<Double> keyFrameTimes = new ArrayList<Double>();
		
		double timeCounter=momentum.get(0)[0];
		double timeMin=momentum.get(0)[0];
		double valueMin=momentum.get(0)[1];
		//Do a for loop that compares the values and checks for the least momentum
		for(int i=1; i<momentum.size(); i++) {
			if(timeCounter<timeFrame) {
				//Check for minimum value
				if(momentum.get(i)[1]<valueMin) {
					timeMin = momentum.get(i)[0];
				}
				//Add difference between the current and previous time to the time counter
				timeCounter+=(momentum.get(i)[0]-momentum.get(i-1)[0]);
			}
			else {
				//Add minimum value to the list of minimum values 
				keyFrameTimes.add(timeMin);
				//reset counter values
				timeCounter=0;
				timeMin=0;
				valueMin=99999;
			}
		}
		
		return keyFrameTimes;
	}
	
	//FOR ARMS ONLY
	//add average of all accelerations for all joints
	public double getAveAccel() {
		int xSum = 0;
		int ySum = 0;
		int zSum = 0;
		
		for(int a=0; a<armJoints.length; a++) {
			ArrayList<double[]> jv = e.GetColumnsForJoint("Acceleration", armJoints[a]);
			for(int b=0; b<jv.size(); b++) {
				double[] r = jv.get(b);
				
				xSum += r[1];
				ySum += r[2];
				zSum += r[3];
			}
		}
		
		//return magnitude of the sum of the vectors
		return Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
	}

	//FOR LEGS ONLY
	//Motion velocity all over motion span
	//Center position = average of position until that time
	public double[] getMotionDensity() {
		double[] densityArray = new double[legJoints.length];
		
		for(int a=0; a<legJoints.length; a++) {
			ArrayList<double[]> jv = e.GetColumnsForJoint("Position", legJoints[a]);
			//get center position of joint
			double[] center = this.computeCenterPosition(jv);
			
			//compute for motion span of joint
			double totalSpan = 0;
			for(int b=0; b<jv.size(); b++) {
				double[] r = jv.get(b);
				totalSpan+=Math.sqrt(Math.pow(center[1]-r[1], 2)+Math.pow(center[2]-r[2], 2)+Math.pow(center[3]-r[3], 2));
			}
			
			//compute for motion velocity of joint
			double totalVelocity = getLegMotionVelocity();
			
			//motion density = motion velocity / motion span
			densityArray[a] = totalVelocity/totalSpan;	
		}
		return densityArray;
	}
	
	
	
	
	
	//HELPER METHODS
	
	//Computes for the momentum (which is basically Laban Weight component) arranged by the time
	public ArrayList<double[]> computeMomentum() {
		ArrayList<ArrayList<double[]>> j = new ArrayList<ArrayList<double[]>>();
		for(int i=0; i<joints.length; i++) {
			j.add(e.GetColumnsForJoint("Position", joints[i]));
		}
		
		ArrayList<double[]> momentum = new ArrayList<double[]>();
		int xSum = 0;
		int ySum = 0;
		int zSum = 0;
		double time = 0;
		
		for(int a=0; a<j.get(0).size(); a++) {
			for(int b=0; b<j.size(); b++) {
				ArrayList<double[]> jv = j.get(b);
				double[] r = jv.get(a);
				
				xSum += r[1]*massOfParts[b];
				ySum += r[2]*massOfParts[b];
				zSum += r[3]*massOfParts[b];
				time = r[0];
			}
			//store total momentum for each unit of time
			double[] input = new double[2];
			input[0] = time;
			input[1] = Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
			momentum.add(input);
		}
		
		return momentum;
	}
	
	//Get total displacement, then divide by two to get the center
	public double[] computeCenterPosition(ArrayList<double[]> joint) {
		int xSum = 0;
		int ySum = 0;
		int zSum = 0;
		
		for(int i=0; i<joint.size(); i++) {
			double[] point = joint.get(i);
			
			xSum += point[1];
			ySum += point[2];
			zSum += point[3];
		}
		
		double[] centerPoint = {-1, xSum/joint.size(), ySum/joint.size(), zSum/joint.size()};
		return centerPoint;
	}
	
	//Compute for the average velocity of leg movements
	public double getLegMotionVelocity() {
		int xSum = 0;
		int ySum = 0;
		int zSum = 0;
		
		for(int a=0; a<legJoints.length; a++) {
			ArrayList<double[]> jv = e.GetColumnsForJoint("Velocity", legJoints[a]);
			for(int b=0; b<jv.size(); b++) {
				double[] r = jv.get(b);
				
				xSum += r[1];
				ySum += r[2];
				zSum += r[3];
			}
		}
		
		return Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
	}
}
