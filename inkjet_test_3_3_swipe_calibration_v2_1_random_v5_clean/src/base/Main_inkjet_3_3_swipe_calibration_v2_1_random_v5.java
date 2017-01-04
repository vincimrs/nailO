/*
 * inkjet nail sensors 3X3 matrix 
 * 1. calibration, 2. swipe detection, 3. rainbow application
 * cindykao@mit.edu
 * Aug 2014
 */

package base;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import processing.core.PApplet;
import weka.core.Instances;
import base.application.CalibrationApplication;
import base.application.ExamApplication;
import base.application.RainbowApplication;
import base.application.RainbowQuizApplication;
import base.coor.transform.CoorTransform;
import base.processing.Mask;
import base.visual.DrawArrow;
import base.visual.Visual;
//means we are importing everything from the processing core
//import weka.classifiers.functions.supportVector.Kernel;
//import weka.classifiers.functions.supportVector.Kernel; 

public class Main_inkjet_3_3_swipe_calibration_v2_1_random_v5 extends PApplet {
	
	// class for drawing
	private DrawArrow drawArrow = new DrawArrow(this);
	private Visual visualAid = new Visual(this);
	
	// applications
	private CalibrationApplication cali_app;
	private RainbowApplication rainbow_app;
	private RainbowQuizApplication rainbow_quiz_app;
	private ExamApplication exam_app;
	
	// calibration_data_collect() constants
	private final int STATE_NUM = 5;
	private final int PRINT_NUM = 15;
	private boolean baselineFlag = false;
	
	static int numberOfFeatures = 15;
	
	/*check sesnor state flag*/
	private boolean checkSensorFlag = false;
	
	/*calibration flag*/
	private boolean calibrationDoneFlag = false;
	private boolean calibrationFlag = false;
	private int calibration_count = 0;
	
	/*run application flag*/
	private boolean swipeFlag = false;
	
	
	/*random application*/
	private int random_app_count = 0;
	
	//swipe detection
	private final boolean swipe_detection_phase = true;
	
	//
	
	
	// swipedetection() constants
	private final int COUNT_CONTINUOUS_THRES = 2;
	
	
	private final float SWIPE_DIFF_THRES = (float) 0.4;
	
	// exam applictaion Flag
	private boolean examFlag = false;
	
	
	
	/* queue */
	// FloatList is a class extending ArrayList to create array
	// http://stackoverflow.com/questions/8559092/create-an-array-of-arraylists
	// remember to initialize each object in queue in setup()
	

	private int[] data_raw = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}; 
	private double[][] data = new double[3][3]; // 2-Dimension data
	private double[][] data_mask = new double[3][3];
	 
	String programState = "Start"; 
	String trainingState = "Press 1 to train closed hand"; 
	
	Instances trainingData; 
	
	votingClassifier votedClassifier = new votingClassifier(10,7);
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	
	private String[] stateDescrip = {"----","Hand on table", "Hand mid-air", "Hand flexing"}; 
	
	// swipe application index, and rgb values
	
	
	
	// log state
	/*boolean stateFlag1 = true;
  	boolean stateFlag2 = false;
  	boolean stateFlag3 = false;
	
  	// log which electrode
	boolean electrodeFlag0 = true;
	boolean  electrodeFlag4 = false;
	boolean  electrodeFlag8 = false;
	boolean  electrodeFlag14 = false;
	*/
	
	// flags for data logging 
	private int stateFlag = 0;
	private int electrodeFlag = -1;
	
	Date dateToday = new Date();
	private final String dataPath = "/Users/vincimrs/Documents/data/inkjet_nail/";
	private final String filename = dataPath + dateFormat.format(dateToday) + "_testData.csv"; //logging data
	private final String filename_parameter_log = dataPath + dateFormat.format(dateToday) + "_parameter_log.csv";
	private final String filename_calibration_log = dataPath + dateFormat.format(dateToday) + "_calibration_log.csv"; //calibration file
	private final String filename_calibration = dataPath + dateFormat.format(dateToday) + "_calibration.csv"; //calibration file
	//private final String filename_random_app = dataPath + dateFormat.format(dateToday) + "_random_app.csv"; //random app file
	private final String filename_exam_log = dataPath + dateFormat.format(dateToday) + "_exam_log.csv";
	private final String filename_exam_gnd = dataPath + dateFormat.format(dateToday) + "_exam_gnd.csv";
	private final String filename_exam_result = dataPath + dateFormat.format(dateToday) + "_exam_result.csv";
	private final String filename_rainbow_quiz_log = dataPath + dateFormat.format(dateToday) + "_rainbow_quiz_log.csv";
	private final String filename_rainbow_quiz_result = dataPath + dateFormat.format(dateToday) + "_rainbow_quiz_result.csv";
	
	Serial myPort;        // The serial port 

	// for calibration parameter in main
	private double[] calibration_para = new double[2];  
	
	// for random_application 
	int seq_select;
	
	
	public void setup() {
		frameRate(30);
		size(1440,800); 
		background(255);
		for(int i=0; i< Serial.list().length; i++){
			System.out.println(i+": " + Serial.list()[i]);
		}
		myPort = new Serial(this, Serial.list()[6], 9600); 
		programState = " ";
		write_serial();
		
		// TODO: the following two try-catch blocks have no function...
		// Filewriter for logging data
		try {
			FileWriter fw = new FileWriter(filename,true);
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
		
		// Filewriter for logging calibration data
		try {
			FileWriter fw_calibration = new FileWriter(filename_calibration,true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		  
		// random generater for random_application
		Random rand = new Random();
		seq_select = rand.nextInt(3); //find randomized sequence to run
		
		// prepare application
		cali_app = new CalibrationApplication(this, filename_calibration, filename_calibration_log, filename_parameter_log, stateFlag, electrodeFlag);
		
	}//end setup
	
	// ---- Processing UI ---------------------------------------------------------------
	public void draw() {
	  
	  smooth();
	  background(255);
	  
	  // print text for set baseline
	  if(baselineFlag == true){
		  fill(0, 0, 0);
		  textSize(15);
		  text("baseline set for 0x25", 900, 20);
	  }
	   
	  
	  if( (checkSensorFlag == true) && (calibrationFlag == false) && (swipeFlag == false)){
	  // 0. hardware check: test sensor 
		  
		  // data transition
		  data = CoorTransform.coordinate_transistion_2d(data_raw); // transition data_raw[] to data[][]
		  data_mask = Mask.mask_data(data);  // mask data
		  
		  // data visualization
		  visualAid.matrix_viz(data, 100, 5, 50, electrodeFlag);
		  visualAid.matrix_viz(data_mask, 300, 5, 50, electrodeFlag);
		  visualAid.histogram_viz(data_raw, 100, 650, 50);
	  
	  }else if( (calibrationFlag == true) && (swipeFlag == false) && (calibrationDoneFlag == false) ){
	  // 1. calibration phase
		  
		  double[] t = cali_app.on_draw(data_raw);
		  
		  if (t != null){
			  calibration_para = t;
			  
			  System.out.println("calibration_para[0]: " + calibration_para[0] + " calibration_para[1]: " + (int)calibration_para[1]);
			  
			  calibrationDoneFlag = true; // make sure it only goes in calibration once
			  
			  double T = calibration_para[0];
			  int buf = (int)calibration_para[1];
			  
			  rainbow_app = new RainbowApplication(this, T, buf, filename, stateFlag, electrodeFlag);
			  exam_app = new ExamApplication(this, T, buf, filename_exam_gnd, filename_exam_log, filename_exam_result, stateFlag, electrodeFlag);
			  rainbow_quiz_app =  new RainbowQuizApplication(this, T, buf, filename_rainbow_quiz_log, filename_rainbow_quiz_result,stateFlag, electrodeFlag);
		  }
		  
	  }else if((swipeFlag == true) && (calibrationDoneFlag == true)){ //run application after it is calibrated
	  // 2. swipe detection phase
		  
		  textSize(20);
		  fill(0, 0, 0);
		  text("T: " + calibration_para[0] + " buf: " + (int)calibration_para[1], 900, 40 );	
		  
		  rainbow_quiz_app.on_draw(data_raw);
		  
		  //exam_app.on_draw(data_raw);
		  
		  // visualization for swipe app
		  /* 	*/
		  
		  
	  }
	  /*else if ((swipeFlag == true) && (calibrationDoneFlag == true) && (examFlag == true)){
		  exam_app.on_draw(data_raw);
	  }*/
   
	}//end draw


	public int detect_press() { 
		int max = 0;
		int max_index = -1;
		for(int i =0; i<=14; i++){
			if (data_raw[i] > max){
				max = data_raw[i];
				max_index = i;
			}
		}
		
		if(max > 50)
			return max_index;
		else
			return -1;

	}//end 
	
	
	public void keyPressed()
	{
		 if (key == 'r'){ // set baseline	  
			 write_serial();
			 baselineFlag = true;
		 }else if (key == 'c'){
			 calibrationFlag = true;
		 }
		 else if (key == 's'){
			 swipeFlag = true;
			 
			 System.out.println("============in Swipe Flag ");
		 }
		 else if(key == 'a'){
			 checkSensorFlag = true;
		 }
		 else if(key == 'e'){
			 examFlag = true; 
			 
			 System.out.println("============ in exam flag  ");
		 }
	
	}//end keyPressed


	// ---- Serial communication --------------------------------------------------------
	public void write_serial(){
			 // set baseline
			 myPort.write(49);
			 myPort.write(" ");		 
	}
	
	 
	 public void serialEvent (Serial myPort) {
		 // get the ASCII string:
		 String inString = myPort.readStringUntil('\n');	 
		 if (inString != null){
		 
			 //System.out.println(inString);
		 
		 }
		 //Check if the string is good
		 //I use the 'S' character at the beginning
		 //Otherwise, I get a null point, and computer may crash (MAC 10.6)
		 if (inString != null && inString.charAt(0) == 'S') {
		   // trim off any whitespace:
		   inString = trim(inString);
		   String[] list = split(inString, ',');
	
		   //System.out.println(inString);
		   
		   if(list.length == 18 ) { 	
			     for (int i =0; i<list.length-1; i++ ) { 
			    	    //TODO: list[i+1] crashes here sometimes...
			    	 // TODO: VERY IMPORTANT, I CHANGE THE . TO A PERIOD. DON'T KNOW IT WILL CAUSE A PROBLEM OR NOT
			    	 if( (list[i+1] != "\"Hello capsSense_driver_v3") && (list[i+1] != ",Hello capsSense_driver_v3")){
			    	    	data_raw[i] = Integer.parseInt(list[i+1]);
			     	}
			     }
		   }//end	   
		 }//end if String is correct
		     
	 }//end serial Event
	 
	// ---- Data logging ----------------------------------------------------------------
	

	
	
	
	
/*
    	TODO: something I need to take care
	if (swipe_detection_phase_flag) { // only log for the
										// application, not for
										// calibration phase
		if (swipe_direction != Swipe.NO)
			log_state(filename, arrows[swipe_direction], ",max:"
					+ max + ",swipe_buf_index: "
					+ direction_vote_buf_index + "," + buf_print);
		else
			log_state(filename, "NO_SWIPE", ",max:" + max
					+ ",swipe_buf_index: "
					+ direction_vote_buf_index + "," + buf_print);
	} else {
		if (swipe_direction != -1)
			log_state(filename_calibration_log,
					arrows[swipe_direction], ",max:" + max
							+ ",swipe_buf_index: "
							+ direction_vote_buf_index + ","
							+ buf_print);
		else
			log_state(filename_calibration_log, "NO_SWIPE", ",max:"
					+ max + ",swipe_buf_index: "
					+ direction_vote_buf_index + "," + buf_print);
	}
	*/
	
	// ---- Random application ---------------------------------------------------------
	
	  
	
	
	
	
	
	
	
}//end class
	














