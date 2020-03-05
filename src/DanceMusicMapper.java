import java.util.ArrayList;
import java.util.Arrays;

import jm.JMC;
import jm.music.tools.*;
import jm.music.data.*;
import jm.util.*;

public class DanceMusicMapper implements JMC{
	public static DanceMoveIdentifier dmi;
	public static DanceFeatureExtractor dfe;
	public static Excel ex;
	//Used to know how long the score should be, converting from beat time to real time and vice versa;
    public static double measuresPerSecond = 0;
    public static int numberOfMeasures = 0;
	
	//constants
	public static final double totalTime = 10; //in seconds
	public static final double timeInterval = 0.25; //Time interval of the recordings
	public static final int numIntervals = (int) (totalTime/timeInterval);
	public static final double timeFrame = 2; //in seconds
	public static final int numFrames = (int) (totalTime/timeFrame);
	public static final double beatsPerMeasure = 4; //Sticking to 4/4 time signature;
	public static final int[] pitches = { A0, AS0, B0, C1, CS1, D1, DS1, E1, F1, FS1, G1, GS1, A1, AS1, B1, C2, CS2, D2, DS2, E2, F2, FS2, G2, GS2, A2, AS2, B2, C3, CS3, D3, DS3, E3, F3, FS3, G3, GS3, A3, AS3, B3, C4, CS4, D4, DS4, E4, F4, FS4, G4, GS4, A4, AS4, B4, C5, CS5, D5, DS5, E5, F5, FS5, G5, GS5, A5, AS5, B5, C6, CS6, D6, DS6, E6, F6, FS6, G6, GS6, A6, AS6, B6, C7, CS7, D7, DS7, E7, F7, FS7, G7, GS7, A7, AS7, B7, C8 };
	public static final double[] rhythm = { SIXTEENTH_NOTE, EIGHTH_NOTE, QUARTER_NOTE, HALF_NOTE, WHOLE_NOTE};
	public static final int[] dynamic = { SILENT, PPP, PP, P, MP, MF, F, FF, FFF };
	//public static final String[] danceMoves = { "Head", "Hips", "Groove", "Step", "Hop", "Wave", "Throw" };
	
	public DanceMusicMapper() {
		dmi = new DanceMoveIdentifier();
		dfe = new DanceFeatureExtractor();
		ex = new Excel();
	}
	
	public void MapDanceToMusic() {
		//PROCEDURE
		//Segment dance move into intervals
		//Collect all the features
		//Use features to map the piano and drum instruments
		//	Piano - motion span, weight component, motion density, directional change
		//  Drums - motion key frame
		//	Tempo - average velocity
		//	Dynamics - average acceleration
		//Either play or export as midi file
		
		//-------------------------------------------------------- COLLECT FEATURES -------------------------------------------------------------
		double AveVel = dfe.getAveVel(0, totalTime);
		System.out.println("AveVel: "+AveVel);
		
		double[] AveAccel = new double[numFrames];
		for(int i=0; i<numFrames; i++) {
			AveAccel[i] = dfe.getAveAccel(timeFrame*i, timeFrame*(i+1));
		}
		
//		double[] DirecChange = new double[numIntervals];
//		for(int i=0; i<numIntervals; i++) {
//			DirecChange[i] = dfe.getDirectChange(timeInterval*i, timeInterval*(i+1));
//		}
		
		double[] MotionSpan = new double[numFrames];
		for(int i=0; i<numFrames; i++) {
			MotionSpan[i] = dfe.getMotionSpan(timeFrame*i, timeFrame*(i+1));
		}
		
		double[] MotionDensity = new double[numFrames];
		for(int i=0; i<numFrames; i++) {
			MotionDensity[i] = dfe.getMotionDensity(timeFrame*i, timeFrame*(i+1));
		}
		
		ArrayList<Double> DirectionChange = dfe.getDirectChange();
		System.out.println("Direction Change");
		for(int i=0; i<DirectionChange.size(); i++) {
			System.out.println(DirectionChange.get(i));
		}
		
		ArrayList<double[]> WeightComponent = dfe.getWeightComponent();			
		ArrayList<Double> MotionKeyFrame = dfe.getMotionKeyFrame();
		
		//Create music
		Score music = new Score();
		music.setTimeSignature(4, 4); // limit to 4/4 time signature
		Part piano = new Part("Melody", SYNTH_BASS);
		Part drums = new Part("Drum Kit", 0, 9);
		
		//-------------------------------------------------------- SET TEMPO -------------------------------------------------------------
        //Velocity --> Determines Beat / Tempo 
      	//Slowest is around 0.5, Fastest is around 2.5
        //Make tempo range from 60-160 bpm
        double newTempo = (AveVel/2.5)*160;
        if(newTempo < 60) newTempo = 60;
        if(newTempo > 160) newTempo = 160;
        System.out.println("Tempo: "+newTempo);
        
        //set values of constants based on tempo;
        measuresPerSecond = newTempo/240;
		numberOfMeasures = (int)(Math.ceil(measuresPerSecond*totalTime));
		
		//Initialize Instruments to start in interval
        Phrase phrP = new Phrase(0.0);
		Phrase phrBD = new Phrase(0.0);
        Phrase phrSD = new Phrase(0.0);
        //Set Tempo
        phrP.setTempo(newTempo);
        phrBD.setTempo(newTempo);
        phrSD.setTempo(newTempo);
		
		//-------------------------------------------------------- PIANO -------------------------------------------------------------
        
        //Generate note rhythm
        //Motion Density, Directional Change --> Rhythm
        
        //Directional Change determines when notes should be played(times with high directional change)
        
        //Motion Density determines what note should be considered for the melody
        ArrayList<double[]> noteList = new ArrayList<double[]>();
        for(int i=0; i<numFrames; i++) {
        	//Set comparative threshold to be 0.004 for now
        	if(MotionDensity[i]<0.004) {
        		//Use longer sounding notes
        		noteList.add(Arrays.copyOfRange(rhythm, 0, 3));
        	}
        	else {
        		//Use shorter sounding notes
        		noteList.add(Arrays.copyOfRange(rhythm, 2, 5));
        	}
        }
        
        //Pitch --> Emotion
      	//Joy = high motion span, effort weight: high, shape component: rise/up
      	//		= fast rhythm, high tempo, high pitch, high contour type
      	//Sadness = Arms to upper body, Shape component: sink, low speed, High amounts of bending and stretching
      	//		= whole and half notes, low tempo
      	//      = slow rhythm, low tempo, Low pitch, low contour type
		
		//Check which emotion it is closer to and generate notes to match that
        double beatsPerSecond = measuresPerSecond*4;
        double beatsPerInterval = beatsPerSecond*timeFrame;
        
        for(int i=0; i<numFrames; i++) {
        	//Set motion span threshold to 370 for now
        	if(MotionSpan[i]>370) {
        		//Set weight component threshold to 20 for now
        		if(WeightComponent.get(i)[1]>20) {
        			Mod.append(phrP, generateHappyMusic(noteList.get(i), beatsPerInterval));
        		}
        		else {
        			Mod.append(phrP, generateSadMusic(noteList.get(i), beatsPerInterval));
        		}
        	}
        	else {
        		Mod.append(phrP, generateSadMusic(noteList.get(i), beatsPerInterval));
        	}
        }
        
		for(int i=0; i<rhythm.length; i++) {
			phrP.add(new Note(C4, rhythm[i]));
		}
		piano.addPhrase(phrP);
		music.addPart(piano);
        
        //-------------------------------------------------------- DRUMS -------------------------------------------------------------
        //Constant Snare (?) for now
        for(int i=0;i<2*numberOfMeasures;i++){
            Note rest = new Note(REST, QUARTER_NOTE); //rest
            rest.setDuration(QUARTER_NOTE);
            phrSD.addNote(rest);
            Note note = new Note(38, QUARTER_NOTE);
            note.setDuration(QUARTER_NOTE);
            phrSD.addNote(note);
        }
        
        //Motion Key Frame --> Determines Rhythm of Beat
        //What if you try sorting the values increasing and get the lowest values
        //Assume that the motion key frames are arranged in ascending order
        while(MotionKeyFrame.size()>0) {
        	//Get the timeInBeats when the motion key frame happened;
        	double bassTime = convertRealTimeToBeats(MotionKeyFrame.get(0));
        	MotionKeyFrame.remove(0);
        	
        	//Add rests for every interval while the key frame isn't there yet
        	for(double i=phrBD.getEndTime(); i<bassTime; i+=SIXTEENTH_NOTE) {
        		Note rest = new Note(REST, SIXTEENTH_NOTE);
        		rest.setDuration(SIXTEENTH_NOTE);
        		phrBD.addNote(rest);
        	}
        	
        	//add bass for key frame
        	Note note = new Note(36, QUARTER_NOTE);
        	note.setDuration(QUARTER_NOTE);
        	phrBD.addNote(note);
        }
               
        //Add drums to array
        drums.addPhrase(phrSD);
        drums.addPhrase(phrBD);
        music.addPart(drums);
		Play.midi(music);
	}
	
	public double convertRealTimeToBeats(double timeInSecs) {
		return timeInSecs*measuresPerSecond*beatsPerMeasure;
	}
	
	public Phrase generateHappyMusic(double[] rhythm, double numBeats) {
		//High pitch range: upper half of pitches
		//High contour type: big jumps in rhythm
		Phrase p = new Phrase(0.0);
		
		while(p.getBeatLength() < numBeats-1) {
			double rhy = rhythm[(int)(Math.random()*3)];
			int pitch = pitches[(int)((Math.random()*(pitches.length/2)+(pitches.length/2)))];
			Note n = new Note(pitch, rhy);
			p.add(n);
		}
		//Add rest in the end to make it sakto
		Note rest = new Note(REST, (numBeats-p.getBeatLength()));
		p.add(rest);
		
		return p;
	}
	
	public Phrase generateSadMusic(double[] rhythm, double numBeats) {
		//Low pitch range: lower half of pitches
		//Low contour type: small jumps in rhythm
		Phrase p = new Phrase(0.0);
		
		while(p.getBeatLength() < numBeats-4) {
			double rhy = rhythm[(int)(Math.random()*3)];
			int pitch = pitches[(int)((Math.random()*(pitches.length/2)))];
			Note n = new Note(pitch, rhy);
			p.add(n);
		}
		//Add rest in the end to make it sakto
		Note rest = new Note(REST, (numBeats-p.getBeatLength()));
		p.add(rest);
		
		return p;
	}
	
}

//Option 1
//Create similarity matrix for music (preset)
//Create similarity matrix for motion (code)
//Compare similarity matrix of music and motion to see the current match compatibility
//Want to minimize the difference between both similarity matrices
//Check the correlation of the motion and music features
//Use regression function to verify the movement of the features

//public double[] MapDanceToMusic() {
////Procedure for each feature
////Segment dance move into 4 second intervals
////Compute for similarity matrix for dance
////Compute for possible values with similarity matrix similar to dance
////Out of the combinations remaining, want to maximize correlation coefficient and correlation coefficient of difference
////Return list of mp3 files that go well together
//
////Initialize starting array that will hold all the features for a time period
//ArrayList<double[]> features = new ArrayList<double[]>();
//double startTime = 0;
//double endTime = timeFrame;
////Doubles will be stored in an array,
///*
// * Ruleset:
// * Total length: 11
// * 0 = Average Velocity
// * 1 = Direction Change
// * 2 = Average Acceleration
// * 3-10 = Motion Density (Only for Legs)
// */
//double[] timeFrame;
//for(int i=0; i<numIntervals; i++) { 
//	//add all values to ArrayList based on time division
//	timeFrame = new double[11];
//	timeFrame[0] = dfe.getAveVel(startTime, endTime);
//	timeFrame[1] = dfe.getDirectChange(startTime, endTime);
//	timeFrame[2] = dfe.getAveAccel(startTime, endTime);
//	double[] motionDensity = dfe.getMotionDensity(startTime, endTime);
//	for(int j=3; j<motionDensity.length+3; j++) {
//		timeFrame[j] = motionDensity[j-3];
//	}
//	
//	features.add(timeFrame);
//	startTime += timeFrame;
//	endTime += timeFrame;
//}
//
////Compute for similarity matrix for dance
//double[][] danceSM = new double[numIntervals][numIntervals];
////Store data to improve efficiency
//double[] mem = new double[numIntervals];
//for(int i=0; i<numIntervals; i++) {
//	//Get Linear Summation of Differences between the features
//	//DP to store value if it hasn't been computed for
//	if(mem[i]==0) {
//		for(int a=0; a<features.size(); a++) {
//			mem[i] += features.get(a)[i];
//		}
//	}
//	for(int j=i; j<numIntervals; j++) {
//		//DP to store value if it hasn't been computed for
//		if(mem[j]==0) {
//			for(int a=0; a<features.size(); a++) {
//				mem[j] += features.get(a)[j];
//			}
//		}
//		//Compute for difference
//		danceSM[i][j] = mem[i]-mem[j];
//	}
//}
//
////Testing purposes
//for(int a=0; a<numIntervals; a++) {
//	for(int b=0; b<numIntervals; b++) {
//		System.out.print(danceSM[a][b] + " ");
//	}
//	System.out.println("");
//}
//
//
////Compute for possible values with similarity matrix similar to dance
//ArrayList<double[]> possiblePatterns = new ArrayList<double[]>();
//double[] ps;
//for(int i=0; i<musicFeatAveVel.length; i++) {
//	ps = new double[numIntervals];
//	ps[0] = musicFeatAveVel[i];
//	for(int j=1; j<numIntervals; j++) {
//		double idealValue = ps[j-1]+danceSM[j][0];
//		//Check if value is valid in array
//		if(!(idealValue > musicFeatAveVel[0] && idealValue < musicFeatAveVel[musicFeatAveVel.length-1])) {
//			break;
//		}
//	}
//	if(ps.length==numIntervals) {
//		possiblePatterns.add(ps);
//	}
//}
//
////double cc = -1;
//double[] bestMatch = null;
////for(int i=0; i<possiblePatterns.size(); i++) {
////	if(cc<computeCorrelCoeffs(aveVel, possiblePatterns.get(i))) {
////		cc=computeCorrelCoeffs(aveVel, possiblePatterns.get(i));
////		bestMatch = possiblePatterns.get(i);
////	}
////}
//
//return bestMatch;
//}
//
//public static String[] IdentifyMove() {
//	//Check appropriate move
//	if(dmi.IsHead()) {
//		
//	}
//	
//	if(dmi.IsHips()){
//		
//	}
//	
//	if(dmi.IsGroove()) {
//		//Groove is linked with average velocity move so we check for that
//		
//	}
//	
//	//Check if step
//	if(dmi.IsStepOrHop()==1) {
//		
//	}
//	
//	//Check if hop
//	if(dmi.IsStepOrHop()==2) {
//		
//	}
//	
//	//Check if wave
//	if(dmi.IsWaveOrThrow()==1) {
//		
//	}
//	
//	//Check if throw
//	if(dmi.IsWaveOrThrow()==2) {
//		
//	}
//	
//	return new String[1];
//}
//
//public static boolean valueInFeature(double[] list, double d) {
//	for(int i=0; i<list.length; i++) {
//		if(list[i]==d) return true;
//	}
//	return false;
//}
//
//public static double computeCorrelCoeffs(double[] feature1, double[] feature2) {
//	//Assumed feature1 and feature2 have the same length
//	int n = feature1.length;
//	int xy = 0;
//	int x = 0;
//	int y = 0;
//	int x2 = 0;
//	int y2 = 0;
//	for(int i=0; i<feature1.length; i++) {
//		xy += feature1[i]*feature2[i];
//		x += feature1[i];
//		y += feature2[i];
//		x2 += feature1[i]*feature1[i];
//		y2 += feature2[i]*feature2[i];
//	}
//	
//	return (n*xy-x*y)/Math.sqrt((n*x2-x*x)*(n*y2-y*y));
//}