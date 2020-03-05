import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DanceFeatureExtractor {
	//0,0 x and y is located in the middle at the bottom
	/*
	 * Features:
	 * Average Velocity - vector summation of average velocities of each joint
	 * Average Acceleration - vector summation of the acceleration of each joint
	 * Direction Change - vector summation of angles between two movements
	 * Motion Span
	 * Motion Density - how much movement there is for each joint
	 * 
	 * Laban Movement Components Features:
	 * Motion Key Frame (Shape Component) - When the motions stop moving
	 * Weight Component
	 * 
	 */
	public static double aveVel; 
	public static double direcChange; //
	public static ArrayList<Double> motionKeyFrame; //
	public static double aveAccel; //
	public static double[] motionDensity; //
	public static Excel e;
	
	//CONSTANTS
	public static final double timeFrame = 2; //Time frame where we check for a local minimum in getMotionKeyFrame
	public static final double timeInterval = 0.25; //Time interval of the recordings
	//Joints
	public static final String[] joints = {"Hip", "Spine", "ShoulderCenter", "Head", "ShoulderLeft", "ElbowLeft", "WristLeft", "HandLeft", "ShoulderRight", "ElbowRight", "WristRight", "HandRight", "HipLeft", "KneeLeft", "AnkleLeft", "FootLeft", "HipRight", "KneeRight", "AnkleRight", "FootRight"};
	public static final String[] armJoints = Arrays.copyOfRange(joints, 4, 12);
	public static final String[] legJoints = Arrays.copyOfRange(joints, 12, 20);
	public static final String[] coreJoints = Arrays.copyOfRange(joints, 0, 4);
	//Used For computations	
	public static final double massOfSpecimen = 72.5; //in kg
	public static final double[] massOfParts = {massOfSpecimen*0.1117, massOfSpecimen*0.1633, massOfSpecimen*0.1596,  massOfSpecimen*0.0694, massOfSpecimen*0.0271, massOfSpecimen*0.0162, massOfSpecimen*0.00305, massOfSpecimen*0.00305, massOfSpecimen*0.0271, massOfSpecimen*0.0162, massOfSpecimen*0.00305, massOfSpecimen*0.00305, massOfSpecimen*0.0708, massOfSpecimen*0.0708, massOfSpecimen*0.0433, massOfSpecimen*0.0129, massOfSpecimen*0.0708, massOfSpecimen*0.0708, massOfSpecimen*0.0433, massOfSpecimen*0.0129};
	public static final double mkfPercentThreshold = 0.3;
	
	
	public DanceFeatureExtractor() {
		e = new Excel();
	}
	
	//FOR CORE ONLY
	//just get vector sum of all velocities for joints
	//return magnitude of resulting sum of each component
	public double getAveVel(double startTime, double endTime) {
		double xSum = 0;
		double ySum = 0;
		double zSum = 0;
		
		ArrayList<ArrayList<double[]>> jv = new ArrayList<ArrayList<double[]>>();
		for(int a=0; a<coreJoints.length; a++) {
			jv.add(e.GetColumnsForJointByTime("Velocity", coreJoints[a], startTime, endTime));
		}
		
		for(int a=0; a<jv.size(); a++) {
			ArrayList<double[]> j = jv.get(a);
			for(int b=0; b<j.size(); b++) {
				double[] r = j.get(b);
					
				xSum += r[1];
				ySum += r[2];
				zSum += r[3];
			}
		}
		
		//return magnitude of the sum of the vectors
		return Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
	}
	
	//FOR ARMS ONLY
	//add average of all accelerations for all joints
	public double getAveAccel(double startTime, double endTime) {
		int xSum = 0;
		int ySum = 0;
		int zSum = 0;
		
		for(int a=0; a<armJoints.length; a++) {
			ArrayList<double[]> jv = e.GetColumnsForJointByTime("Acceleration", armJoints[a], startTime, endTime);
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
	
	//FOR ALL
	//Use formula (dot product) to compute for the angle and add them all up (up until the time)
	public ArrayList<Double> getDirectChange() {
		//Build the Direction Change Array
		ArrayList<ArrayList<double[]>> jv = new ArrayList<ArrayList<double[]>>();
		for(int a=0; a<joints.length; a++) {
			jv.add(e.GetColumnsForJoint("Position", joints[a]));
		}
		
		//direcChange is the sum of the angles
		ArrayList<double[]> direcChange = new ArrayList<double[]>();
		
		double time = 0;
		double angle = 0;
		for(int a=0; a<jv.get(0).size()-1; a++) {
			for(int b=0; b<jv.size(); b++) {
				ArrayList<double[]> j = jv.get(b);
				//Get the angle between the current time and time after for each joint
				double[] r1 = j.get(a);
				double[] r2 = j.get(a+1);
				
				time = r1[0];
				double dotProduct = (r1[1]*r2[1])+(r1[2]*r2[2])+(r1[3]*r2[3]);
				double magnitude1 = Math.sqrt(Math.pow(r1[1], 2)+Math.pow(r1[2], 2)+Math.pow(r1[3], 2));
				double magnitude2 = Math.sqrt(Math.pow(r2[1], 2)+Math.pow(r2[2], 2)+Math.pow(r2[3], 2));
				//formula for angle between two vectors (returns in radians)
				angle += Math.acos(dotProduct/(magnitude1*magnitude2));
			}
			double[] input = new double[2];
			input[0] = time;
			input[1] = angle;
			direcChange.add(input);
		}
		
		//Compute for Direc Change times
		//Sort the array by momentum magnitude (bubble sort)
		for(int a=0; a<direcChange.size()-1; a++) {
			for(int b=0; b<direcChange.size()-1-a; b++) {
				if(direcChange.get(b)[1]<direcChange.get(b+1)[1]) {
					double[] temp = direcChange.get(b);
					direcChange.set(b, direcChange.get(b+1));
					direcChange.set(b+1, temp);
				}
			}
		}
		
		for(int i=0; i<direcChange.size(); i++) {
			System.out.println("Direction "+direcChange.get(i)[0]+" : "+direcChange.get(i)[1]);
		}
		
		//Key frame times are those with a momentum a certain threshold below the max
		//Max-((Max - Min)*PresetPercentage)
		double threshold = direcChange.get(0)[1]-((direcChange.get(0)[1]-direcChange.get(direcChange.size()-1)[1])*mkfPercentThreshold);
		ArrayList<Double> times = new ArrayList<Double>();
		for(int a=0; a<direcChange.size(); a++) {
			if(direcChange.get(a)[1]<threshold) {
				break;
			}
			times.add(direcChange.get(a)[0]);
		}
		
		//Sort by ascending order of time
		Collections.sort(times);
		return times;
	}
	
//	public double getDirectChange(double startTime, double endTime) {
//		ArrayList<ArrayList<double[]>> jv = new ArrayList<ArrayList<double[]>>();
//		for(int a=0; a<joints.length; a++) {
//			jv.add(e.GetColumnsForJointByTime("Position", joints[a], startTime, endTime));
//		}
//		
//		//direcChange is the sum of the angles
//		double direcChange = 0;
//		for(int a=0; a<jv.size(); a++) {
//			ArrayList<double[]> j = jv.get(a);
//			for(int b=0; b<j.size()-1; b++) {
//				//Get the angle between the current time and time after for each joint
//				double[] r1 = j.get(b);
//				double[] r2 = j.get(b+1);
//				
//				//formula for angle between two vectors (returns in radians)
//				double angle = Math.acos(((r1[1]*r2[1])+(r1[2]*r2[2])+(r1[3]*r2[3]))/(Math.sqrt(Math.pow(r1[1], 2)+Math.pow(r1[2], 2)+Math.pow(r1[3], 2))*Math.sqrt(Math.pow(r2[1], 2)+Math.pow(r2[2], 2)+Math.pow(r2[3], 2))));
//				direcChange += angle;
//			}
//		}
//		
//		return direcChange;
//	}
	
	//FOR ALL
	//Compute for the center point of the movement of each joint and then compute how far it is from it on average
	public double getMotionSpan(double startTime, double endTime) {
		ArrayList<ArrayList<double[]>> jv = new ArrayList<ArrayList<double[]>>();
		for(int i=0; i<joints.length; i++) {
			jv.add(e.GetColumnsForJointByTime("Position", joints[i], startTime, endTime));
		}
		
		//Compute for center and get resulting vector sum
		ArrayList<double[]> center = new ArrayList<double[]>();
		ArrayList<double[]> resultant = new ArrayList<double[]>();
		
		for(int a=0; a<joints.length; a++) {
			ArrayList<double[]> j = jv.get(a);
			double[] centerPoint = new double[4];
			double[] vectorSum = new double[4];
			for(int b=0; b<j.size(); b++) {
				double[] point = j.get(b);
				
				//get center position of each joint
				centerPoint[1] += point[1];
				centerPoint[2] += point[2];
				centerPoint[3] += point[3];
				
				//compute resulting vector of parts
				vectorSum[1] += point[1];
				vectorSum[2] += point[2];
				vectorSum[3] += point[3];
			}
			centerPoint[1] = centerPoint[1]/joints.length;
			centerPoint[2] = centerPoint[2]/joints.length;
			centerPoint[3] = centerPoint[3]/joints.length;
			center.add(centerPoint);
			resultant.add(vectorSum);
		}
		
		//compute for motion span of each joint
		double totalSpan = 0;
		for(int a=0; a<joints.length; a++) {
			double[] r = resultant.get(a);
			double[] centerPoint = center.get(a);
			totalSpan += Math.sqrt(Math.pow(centerPoint[1]-r[1], 2)+Math.pow(centerPoint[2]-r[2], 2)+Math.pow(centerPoint[3]-r[3], 2));
		}
		
		return totalSpan;
	}
	
	//FOR ALL
	//Motion velocity all over motion span
	//Center position = average of position until that time
	public double getMotionDensity(double startTime, double endTime) {
		double totalVelocity = getAveVel(startTime, endTime);
		double totalSpan = getMotionSpan(startTime, endTime);
		//motion density = motion velocity / motion span
		return totalVelocity/totalSpan;
	}
	
	//FOR ALL
	//Computes for the momentum (which is basically Laban Weight component) arranged by the time
	public ArrayList<double[]> getWeightComponent() {
		ArrayList<ArrayList<double[]>> jv = new ArrayList<ArrayList<double[]>>();
		for(int a=0; a<joints.length; a++) {
			jv.add(e.GetColumnsForJoint("Velocity", joints[a]));
		}
		
		//Get vector sum of the velocity
		ArrayList<double[]> momentum = new ArrayList<double[]>();
		double xSum = 0;
		double ySum = 0;
		double zSum = 0;
		double time = 0;
		for(int a=0; a<jv.get(0).size(); a++) {
			for(int b=0; b<jv.size(); b++) {
				ArrayList<double[]> j = jv.get(b);
				double[] r = j.get(a);
				
				xSum += r[1]*massOfParts[b];
				ySum += r[2]*massOfParts[b];
				zSum += r[3]*massOfParts[b];
				time = r[0];
			}
			double input[] = new double[2];
			input[0] = time;
			input[1] = Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
			momentum.add(input);
		}
		
		return momentum;
	}
	
	//FOR ALL
	//compute for the Laban Weight component, get the local minimums of the graph
	//local minimum of velocity graph
	public ArrayList<Double> getMotionKeyFrame() {
		ArrayList<double[]> momentum = getWeightComponent();
		
		//Sort the array by momentum magnitude (bubble sort)
		for(int a=0; a<momentum.size()-1; a++) {
			for(int b=0; b<momentum.size()-1-a; b++) {
				if(momentum.get(b)[1]<momentum.get(b+1)[1]) {
					double[] temp = momentum.get(b);
					momentum.set(b, momentum.get(b+1));
					momentum.set(b+1, temp);
				}
			}
		}
		
		//Key frame times are those with a momentum a certain threshold below the max
		//Max-((Max - Min)*PresetPercentage)
		double threshold = momentum.get(0)[1]-((momentum.get(0)[1]-momentum.get(momentum.size()-1)[1])*mkfPercentThreshold);
		ArrayList<Double> keyFrameTimes = new ArrayList<Double>();
		for(int a=0; a<momentum.size(); a++) {
			if(momentum.get(a)[1]<threshold) {
				break;
			}
			keyFrameTimes.add(momentum.get(a)[0]);
		}
		
		//Sort by ascending order of time
		Collections.sort(keyFrameTimes);
		return keyFrameTimes;
	}
	
	
	
//	//Get total displacement, then divide by two to get the center
//	public double[] computeCenterPosition(ArrayList<double[]> joint) {
//		double xSum = 0;
//		double ySum = 0;
//		double zSum = 0;
//		
//		for(int i=0; i<joint.size(); i++) {
//			double[] point = joint.get(i);
//			
//			xSum += point[1];
//			ySum += point[2];
//			zSum += point[3];
//		}
//		
//		double[] centerPoint = {-1, xSum/joint.size(), ySum/joint.size(), zSum/joint.size()};
//		return centerPoint;
//	}
//	
//	//Compute for the average velocity of leg movements
//	public double getLegMotionVelocity() {
//		double xSum = 0;
//		double ySum = 0;
//		double zSum = 0;
//		
//		for(int a=0; a<legJoints.length; a++) {
//			ArrayList<double[]> jv = e.GetColumnsForJoint("Velocity", legJoints[a]);
//			for(int b=0; b<jv.size(); b++) {
//				double[] r = jv.get(b);
//				
//				xSum += r[1];
//				ySum += r[2];
//				zSum += r[3];
//			}
//		}
//		
//		return Math.sqrt(Math.pow(xSum, 2)+Math.pow(ySum, 2)+Math.pow(zSum, 2));
//	}
}
