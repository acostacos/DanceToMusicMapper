import java.util.ArrayList;

public class DanceMusicMapper {
	public static DanceMoveIdentifier dmi;
	public static DanceFeatureExtractor dfe;
	public static Excel ex;
	
	//constants
	public static final double timeInterval = 4; //in seconds
	public static final double totalTime = 4; //in seconds
	public static final int numIntervals = (int) (totalTime/timeInterval);
	public static final String[] danceMoves = { "Head", "Hips", "Groove", "Step", "Hop", "Wave", "Throw" };
	public static final double[] musicFeatAveVel = { 80, 82, 84, 86, 88, 90, 92, 94, 96, 98, 100, 102, 104, 106, 108, 110, 112, 114, 116, 118, 120 };
	
	public DanceMusicMapper() {
		dmi = new DanceMoveIdentifier();
		dfe = new DanceFeatureExtractor();
		ex = new Excel();
	}
	
	public double[] MapDanceToMusic() {
		//Procedure for each feature
		//Segment dance move into 4 second intervals
		//Compute for similarity matrix for dance
		//Compute for possible values with similarity matrix similar to dance
		//Out of the combinations remaining, want to maximize correlation coefficient and correlation coefficient of difference
		//Return list of mp3 files that go well together
		
		//FOR AVERAGE VELOCITY
		//Initialize starting array
		double startTime = 0;
		double endTime = timeInterval;
		double[] aveVel = new double[numIntervals];
		for(int i=0; i<numIntervals; i++) { 
			aveVel[i] = dfe.getAveVel(startTime, endTime);
			startTime += timeInterval;
			endTime += timeInterval;
		}
		
		//Compute for similarity matrix for dance
		double[][] danceSM = new double[numIntervals][numIntervals];
		for(int i=0; i<numIntervals; i++) {
			for(int j=i; j<numIntervals; j++)
			danceSM[i][j] = aveVel[i]-aveVel[j];
			
		}
		
		//Compute for possible values with similarity matrix similar to dance
		ArrayList<double[]> possiblePatterns = new ArrayList<double[]>();
		double[] ps;
		for(int i=0; i<musicFeatAveVel.length; i++) {
			ps = new double[numIntervals];
			ps[0] = musicFeatAveVel[i];
			for(int j=1; j<numIntervals; j++) {
				double idealValue = ps[j-1]+danceSM[j][0];
				//Check if value is valid in array
				if(!(idealValue > musicFeatAveVel[0] && idealValue < musicFeatAveVel[musicFeatAveVel.length-1])) {
					break;
				}
			}
			if(ps.length==numIntervals) {
				possiblePatterns.add(ps);
			}
		}
		
		double cc = -1;
		double[] bestMatch = null;
		for(int i=0; i<possiblePatterns.size(); i++) {
			if(cc<computeCorrelCoeffs(aveVel, possiblePatterns.get(i))) {
				cc=computeCorrelCoeffs(aveVel, possiblePatterns.get(i));
				bestMatch = possiblePatterns.get(i);
			}
		}
		
		return bestMatch;
	}
	
	public static String[] IdentifyMove() {
		//Check appropriate move
		if(dmi.IsHead()) {
			
		}
		
		if(dmi.IsHips()){
			
		}
		
		if(dmi.IsGroove()) {
			//Groove is linked with average velocity move so we check for that
			
		}
		
		//Check if step
		if(dmi.IsStepOrHop()==1) {
			
		}
		
		//Check if hop
		if(dmi.IsStepOrHop()==2) {
			
		}
		
		//Check if wave
		if(dmi.IsWaveOrThrow()==1) {
			
		}
		
		//Check if throw
		if(dmi.IsWaveOrThrow()==2) {
			
		}
		
		return new String[1];
	}
	
	public static boolean valueInFeature(double[] list, double d) {
		for(int i=0; i<list.length; i++) {
			if(list[i]==d) return true;
		}
		return false;
	}
	
	public static double computeCorrelCoeffs(double[] feature1, double[] feature2) {
		//Assumed feature1 and feature2 have the same length
		int n = feature1.length;
		int xy = 0;
		int x = 0;
		int y = 0;
		int x2 = 0;
		int y2 = 0;
		for(int i=0; i<feature1.length; i++) {
			xy += feature1[i]*feature2[i];
			x += feature1[i];
			y += feature2[i];
			x2 += feature1[i]*feature1[i];
			y2 += feature2[i]*feature2[i];
		}
		
		return (n*xy-x*y)/Math.sqrt((n*x2-x*x)*(n*y2-y*y));
	}

	
	//Option 1
	//Create similarity matrix for music (preset)
	//Create similarity matrix for motion (code)
	//Compare similarity matrix of music and motion to see the current match compatibility
	//Want to minimize the difference between both similarity matrices
	//Check the correlation of the motion and music features
	//Use regression function to verify the movement of the features
	
	//Option 2
	//Check similarity of rhythm components
	//Connectivity analysis to see if they transition well (matches those that transition well together)
	//Connect the music based on the intensity
}