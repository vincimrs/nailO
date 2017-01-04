package base.application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import processing.core.PApplet;
import base.Swipe;
import base.logger.GroundTruthLogger;
import base.processing.DataProcessor;
import base.visual.DrawArrow;


public class CalibrationApplication {
	private final int BASIC_TIME_SLOT = 30;
	private final int NUM_OF_SWIPE_RECORDED = 5; // how many swipes to record for calibration data collection
	private final int SWIPE_INCREMENT_BASIC_SLOT_NUM = 3; // num of basic_time_slots to record a swipe
	private final double T_START =  0.06;
	private final double T_END = 0.21;
	private final double T_INCREMENT = 0.01;
	private final int BUF_START = 4;
	private final int BUF_END = 10;
	
	private final boolean calibration_phase = false;  // copied from Main, don't know why it's keep false
	
	private PApplet applet;
	private DrawArrow draw_arrow;
	
	private String filename_parameter_log;
	
	private int counter = 0;  // since itself will play the whole script, so it needs to keep an internal counter
	
	private DataProcessor processor;
	private GroundTruthLogger ground_logger;
	
	public CalibrationApplication(PApplet _applet, String _filename_calibration, String _filename_calibration_log, String _filename_parameter_log, int _stateFlag, int _electrodeFlag) {
		applet = _applet;
		draw_arrow = new DrawArrow(applet);
		filename_parameter_log = _filename_parameter_log;
		processor = new DataProcessor(T_START, BUF_START, _filename_calibration_log, _stateFlag, _electrodeFlag);
		ground_logger = new GroundTruthLogger(_filename_calibration);
	}
	
	public double[] on_draw(int[] data_raw){
		// this function collects data from the user to train optimal parameters

		double[] return_matrix = null; // set it to null

		// open logger before logging
		if (counter == 0) {
			ground_logger.write_begin();
		}
				
		// print summary of tasks
		int summary_show_time = 7;

		if (counter < BASIC_TIME_SLOT * summary_show_time) {
			applet.fill(0, 0, 0);
			applet.textSize(30);
			applet.text("[Summary of tasks]", 400, 100);
			applet.textSize(20);
			applet.text("(1) swipe right " + NUM_OF_SWIPE_RECORDED + " times", 400, 130);
			draw_arrow.drawRight(650, 130, 20);
			applet.text("(2) swipe left " + NUM_OF_SWIPE_RECORDED + " times", 400, 160);
			draw_arrow.drawLeft(650, 160, 20);
			applet.text("(3) swipe top " + NUM_OF_SWIPE_RECORDED + " times", 400, 190);
			draw_arrow.drawTop(650, 190, 20);
			applet.text("(4) swipe down " + NUM_OF_SWIPE_RECORDED + " times", 400, 340);
			draw_arrow.drawBottom(650, 340, 20);
			applet.text("(5) long press " + NUM_OF_SWIPE_RECORDED + " times", 400, 490);
			draw_arrow.drawPress(650, 490, 20);
		}

		// print "countdown"
		if ((counter >= BASIC_TIME_SLOT * summary_show_time)
				&& (counter < BASIC_TIME_SLOT
						* (summary_show_time + 1))) {
			applet.fill(255, 0, 0);
			applet.textSize(40);
			applet.text("Countdown...", 400, 260);
		}

		// print count down 5, 4, 3, 2, 1, 0
		int countdown_start_time = summary_show_time + 1;
		int countdown_end_time = countdown_start_time + 6;
		for (int i = countdown_start_time; i < countdown_end_time; i++) {
			if ((counter >= BASIC_TIME_SLOT * i)
					&& (counter < BASIC_TIME_SLOT * (i + 1))) {
				applet.fill(255, 0, 0);
				applet.textSize(40);
				applet.text("" + (countdown_end_time - i - 1), 400, 260);
			}
		}
		
		// print arrows and long press
		int begin_index = countdown_end_time;
		String[] msg_direction = { "right", "left", "up", "down" };
		int start_index = 0;
		int end_index = 0;

		for (int j = 0; j < 5; j++) { // this is for direction control
			for (int i = 0; i < NUM_OF_SWIPE_RECORDED; i++) {
				start_index = (begin_index + j * NUM_OF_SWIPE_RECORDED * SWIPE_INCREMENT_BASIC_SLOT_NUM
						+ i * SWIPE_INCREMENT_BASIC_SLOT_NUM)
						* BASIC_TIME_SLOT;
				end_index = (begin_index + j * NUM_OF_SWIPE_RECORDED * SWIPE_INCREMENT_BASIC_SLOT_NUM
						+ i * SWIPE_INCREMENT_BASIC_SLOT_NUM + 2)
						* BASIC_TIME_SLOT;
				if (counter >= start_index && counter < end_index) {
					applet.textSize(40);
					if (j != 4)
						applet.text("swipe " + msg_direction[j] + " " + (i + 1), 400, 200);
					else
						applet.text("long press " + " " + (i + 1), 400, 200);

					draw_arrow.drawByIndex(j, 400, 300, 80);
					ground_logger.log(data_raw, j);
				}
			}
		}

		// bulk of the meat! find the parameters
		if (counter == end_index) {
			ground_logger.write_end();
			
			applet.textSize(40);
			applet.text("calculating optimal parameter", 400, 260);
			return_matrix = calibration_find_parameter();

			applet.text("=== max_T: " + return_matrix[0] + " max_buf: "
					+ return_matrix[1] + " ===", 400, 300);
			applet.textSize(40);
			applet.text("Finished Calibration! T: " + return_matrix[0] + " BUF: " + (int)return_matrix[1], 400, 200);
			  
		}

		// ending message
		// TODO: FIX. threading problem, only appears for one second
		if (counter > end_index) {
			applet.text("Calibration Finished", 400, 300);
		}

		counter += 1;
		//System.out.println("calibration_count: " + counter);

		return return_matrix;

	}

	public double[] calibration_find_parameter() {
		//int data_calibrate_index = 0;
		int[] fake_zero_data = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		int correct_count;
		int total_count;
		int all_count;
		double accuracy;

		double max_accuracy = 0;
		double max_T = 0;
		int max_buf = 0;

		double T;
		int buf;
		
		// 1. read from file
		int[] groundtruth = ground_logger.read_groundtruth();
		int[][] data_calibrate = ground_logger.read_raw_data();
		

		// 2. run loop to find optimal T and BUF
		int total_count_sum = 0;
		int total_num = 0;
		double avg_total_count;
		double accuracy_buffer[][] = new double[1000][6];
		int accuracy_index = 0;
		int t_index = 1;
		int buf_index = 2;
		int correct_c_index = 3;
		int total_c_index = 4;

		for (T = T_START; T <= T_END; T += T_INCREMENT) { // finding T
			for (buf = BUF_START; buf <= BUF_END; buf++) { // finding BUF
				processor.reset_with_parameters(T, buf);
				
				correct_count = 0;
				total_count = 0;
				all_count = 0;
				double score = 0; // score of this pair of parameter

				for (int k = 0; k < 5; k++) { // 5 kinds of swipe
					for (int l = 0; l < NUM_OF_SWIPE_RECORDED; l++) {
						int s = (k * (NUM_OF_SWIPE_RECORDED) + l)
								* ((SWIPE_INCREMENT_BASIC_SLOT_NUM - 1) * BASIC_TIME_SLOT);
						int e = (k * (NUM_OF_SWIPE_RECORDED) + l + 1)
								* ((SWIPE_INCREMENT_BASIC_SLOT_NUM - 1) * BASIC_TIME_SLOT);
						int nr_output = 0;
						int nr_correct = 0;
						for (int m = s; m <= e; m++) {
							int raw_result;
							int correct_answer;
							
							if (m < e) {
								raw_result = processor.raw_data_processing(data_calibrate[m]);
								correct_answer = groundtruth[m];
							}
							else {
								raw_result = processor.force_output_direction_and_empty_buffer();
								correct_answer = groundtruth[m-1];
							}
							
							if (raw_result != Swipe.NO) {
								nr_output++;
								if (raw_result == correct_answer)
									nr_correct++;
							}
						}

						if (nr_output == 0)
							nr_output = 1;
						score += (double) nr_correct / nr_output;

						System.out.println("----, score," + score + " ,T, "
										+ T + ",buf," + buf + ",s," + s
										+ ",e," + e);

						log_state(filename_parameter_log, "----, score,"
								+ score + ",nr_correct," + nr_correct
								+ ",nr_output," + nr_output + " ,T, " + T
								+ ",buf," + buf + ",s," + s + ",e," + e,
								null);

					}
				}

				// store in accuracy buffer
				accuracy_buffer[total_num][accuracy_index] = score;
				accuracy_buffer[total_num][t_index] = T;
				accuracy_buffer[total_num][buf_index] = buf;

				total_num += 1; // to calculate avg_total_count

				

				System.out.println("score," + score + " ,T, " + T + ",buf,"
						+ buf);

				log_state(filename_parameter_log, "score," + score
						+ " ,T, " + T + ",buf," + buf, null);
			}
		}

		// calculate optimal parameters
		max_accuracy = 0;
		max_T = 0;
		max_buf = 0;

		for (int i = 0; i < total_num; i++) {
			// TODO: when accuracy are the same, how do I choose???
			// mechanism

			if (accuracy_buffer[i][accuracy_index] >= max_accuracy) {
				max_accuracy = accuracy_buffer[i][accuracy_index];
				max_T = accuracy_buffer[i][t_index];
				max_buf = (int) accuracy_buffer[i][buf_index];
			}

			log_state(filename_parameter_log, "-- SCORE,"
					+ accuracy_buffer[i][accuracy_index] + ",T,"
					+ accuracy_buffer[i][t_index] + ",buf,"
					+ accuracy_buffer[i][buf_index], null);
		}

		// log state
		log_state(filename_parameter_log, "MAX SCORE," + max_accuracy
				+ " ,max_T, " + max_T + ",max_buf," + max_buf, null);
		System.out.println("MAX SCORE " + max_accuracy + " max_T: " + max_T
				+ " max_buf:" + max_buf + " ===");

		double[] return_matrix = new double[2];
		return_matrix[0] = max_T;
		return_matrix[1] = (int) (max_buf + 0.5);

		return return_matrix;

		
	}
	
	
	// ---- data logger ---------------------------------------------------------------------------
	
	public void log_state(String filename, String id, String descrip) { 
	    try
	     { 
	         FileWriter fw = new FileWriter(filename,true); //the true will append the new data
	    	 fw.write("" + id + "\t" + descrip);//appends the string to the file
	         fw.write("\n");
	         fw.close();
	     }
	     catch(IOException ioe)
	     {
	         System.err.println("IOException: " + ioe.getMessage());
	     }

	}//end saveState
}
