package base.application;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import processing.core.PApplet;
import base.Swipe;
import base.coor.transform.CoorTransform;
import base.logger.CustomizedLogger;
import base.logger.GroundTruthLogger;
import base.processing.DataProcessor;
import base.processing.Mask;
import base.visual.DrawArrow;
import base.visual.Visual;

public class ExamApplication {
	private final int BASIC_TIME_SLOT = 30;
	private final int RAND_SWIPE_NUM = 5 * 14; // total number of detected swipes
	private final int RAND_QUESTION_TIME_SLOT = 2;
	private final int RAND_RESULT_TIME_SLOT = 2;
	private final int RAND_QUESTION_FRAME = RAND_QUESTION_TIME_SLOT * BASIC_TIME_SLOT;
	private final int RAND_RESULT_FRAME = RAND_RESULT_TIME_SLOT * BASIC_TIME_SLOT;
	private final int RAND_ROUND_FRAME = RAND_QUESTION_FRAME + RAND_RESULT_FRAME;
	private final int RAND_APPLICATION_TOTAL_FRAME = RAND_ROUND_FRAME * RAND_SWIPE_NUM;
	
	private final String[] msg_direction = { "swipe right", "swipe left", "swipe up", "swipe down", "long press" };
	private final int[] fake_zero_data = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int[] selected_swipe_seq = {0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1,1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
	
	private PApplet applet;
	private DrawArrow draw_arrow;
	private Visual visualAid;
	
	//private String filename_exam;
	private int counter = 0;  // since itself will play the whole script, so it needs to keep an internal counter
	
	private DataProcessor processor;
	private GroundTruthLogger ground_logger;
	private CustomizedLogger result_logger;
	
	private int nr_correct = 0;
	
	private int nr_guess_wrong;
	private int nr_guess_right;
	
	private boolean correctFlag; // correctFlag
	private boolean wrongFlag; // wrongFlag 
	private int max_error; // for wrongFlag
	private int max_error_index; // for wrongFlag 
	int[] error_index = new int[5];
	int rand_application_buffer[] = new int[RAND_QUESTION_FRAME + 1];
	String rand_application_buffer_string;
	
	
	public ExamApplication(PApplet _applet, double _T, int _buff, String _filename_exam_gnd, String _filename_exam_log, String _filename_exam_result, int _stateFlag, int _electrodeFlag) {
		applet = _applet;
		draw_arrow = new DrawArrow(applet);
		//filename_exam = _filename_exam;
		processor = new DataProcessor(_T, _buff, _filename_exam_log, _stateFlag, _electrodeFlag);
		//selected_swipe_seq = generate_random_swipe_sequence(); // TODO: change to sequential ?
		
		ground_logger = new GroundTruthLogger(_filename_exam_gnd);
		result_logger =  new CustomizedLogger(_filename_exam_result);
		
		visualAid = new Visual(applet); // visualAid
		
	}
	
	public void on_draw(int[] data_raw){
	
		double[][] data = CoorTransform.coordinate_transistion_2d(data_raw); // transistion data_raw[] to data[][]
		double[][] data_mask = Mask.mask_data(data); // mask data
		visualAid.matrix_viz(data, 500, 300, 30, 1);
		visualAid.matrix_viz(data_mask, 680, 300, 30, 1);
		
		if (counter == 0) {
			ground_logger.write_begin();
			result_logger.write_begin();
		}
		
		// script happened in order...
		if (counter < RAND_APPLICATION_TOTAL_FRAME) {
			int ques_ind = counter / RAND_ROUND_FRAME; // question index
			int offset = counter % RAND_ROUND_FRAME;
			int question = selected_swipe_seq[ques_ind]; 
			
			//System.out.println("counter:" + counter + "  ind:" + ques_ind + "  offset:" + offset); //debug 

			if (offset < RAND_QUESTION_FRAME) {   // meaning that it's still in question phase
				if (offset == 0) { // off to a brand new start, yeah 
					processor.reset();
					nr_guess_wrong = 0;
					nr_guess_right = 0;
					
					// reseting everything
					correctFlag = false; // calculate if this swipe is correct
					wrongFlag = false; 
					max_error = 0;
					max_error_index = -1;
					for(int i=0; i <= RAND_QUESTION_FRAME; i++)	rand_application_buffer[i] = 0;
					rand_application_buffer_string = "";
					
				}
				
				ground_logger.log(data_raw, question);
				
				applet.textSize(40);
				applet.text("Test on: " + msg_direction[question], 400, 100);
				draw_arrow.drawByIndex(question, 400, 200, 80);
				int process_result = processor.raw_data_processing(data_raw);
				if (process_result != Swipe.NO) {
					if (process_result == question)
						nr_guess_right++;
					else
						nr_guess_wrong++;
                }
				
				rand_application_buffer[offset] = process_result;// store in buffer, will store Swipe.NO as well. 			
				
				// debug msg
				/*String debug_msg = "debug: ";
				for (int i = 0; i < 9; i++)
				debug_msg += data_raw[i] + ",";
				debug_msg += " direction=" + process_result;
				System.out.println(debug_msg);
				*/
            }
			else { //  offset == RAND_QUESTION_FRAME, end of a question phase. judge swipe direction is correct or not
				if (offset == RAND_QUESTION_FRAME) { // if it is on the ending frame
					max_error = 0;
					max_error_index = -1;
					
					System.out.println("[[[[[[[[ offset == RAND_QUESTION_FRAME ]]]]]]]");
					
					int process_result = processor.force_output_direction_and_empty_buffer();
					if (process_result != Swipe.NO) {
						if (process_result == question)
							nr_guess_right++;
						else
							nr_guess_wrong++;
	                }	
					rand_application_buffer[offset] = process_result;// store in buffer	 			
					
					error_index = swipe_count(rand_application_buffer, RAND_QUESTION_FRAME + 1); // get count of the buffers
					
					
					// generate rand_buffer_string
					rand_application_buffer_string = "rand_buf_string";
					for(int i=0; i<RAND_QUESTION_FRAME; i++){
						String tmp = "," + "" + rand_application_buffer[i];
						rand_application_buffer_string += tmp;
					}
					
					boolean judge_correct_condition = nr_guess_right > 0 && nr_guess_wrong == 0;
					if (judge_correct_condition) { // corrected detected!
					
						nr_correct++;
						System.out.println("got correct");
						correctFlag = true;
					
					}else{ // find the wrong detected direction
						
						for(int i = 0; i < 5; i ++){
							if(i != question){ //find the largest error
								if (error_index[i] > max_error) {
									max_error = error_index[i];
									max_error_index = i;
								}	
							}		
						}
						
						wrongFlag = true;
					}
				} // end of (offset == RAND_QUESTION_FRAME) frame detections 
				
				// print results
				if (correctFlag == true){ // this swipe is correctly detected 
					// print original question
					//applet.text("Test on: " + msg_direction[question], 400, 100);
					//draw_arrow.drawByIndex(question, 400, 200, 80);
					
					// log result
					result_logger.writeln(ques_ind + ",O, gnd_truth," + question + ",result,"  + question + ",counter," + counter +",offset," + offset + ",max_error_index," + max_error_index + ",max_error," + max_error + ",R," + error_index[0] + ",L," + error_index[1] + ",T," + error_index[2] + ",D," + error_index[3] + ",P," + error_index[4] + "," + rand_application_buffer_string);
					
					// print result
					//applet.fill(0, 255, 0);
					applet.textSize(40);
					//applet.text("Correct Detected direction: " + msg_direction[question], 400, 500);
					applet.text("Detected direction:", 400, 500);
					draw_arrow.drawByIndex(question, 400, 550, 30);
					
					//debug
					System.out.println("Correct Detected direction: " + msg_direction[question]);
					System.out.println("counter:" + counter +" offset:" + offset + " max_error_index" + max_error_index + " max_error" + max_error + " R" + error_index[0] + " L" + error_index[1] + " T" + error_index[2] + " D" + error_index[3]);
				
				}else if(wrongFlag == true){ // oops, wrong detection
					
					// print original question
					//applet.text("Test on: " + msg_direction[question], 400, 100);
					//draw_arrow.drawByIndex(question, 400, 200, 80);
					
					if(max_error_index != -1){  // happens when nr_guess_right == 0 && nr_guess_wrong == 0 (?)
						
						// log result
						result_logger.writeln(ques_ind + ",X, gnd_truth," + question + ",result,"  + max_error_index + ",counter," + counter +",offset," + offset + ",max_error_index," + max_error_index + ",max_error," + max_error + ",R," + error_index[0] + ",L," + error_index[1] + ",T," + error_index[2] + ",D," + error_index[3] + ",P," + error_index[4] + "," + rand_application_buffer_string);
						
						// print result
						//applet.fill(255, 0, 0);
						applet.textSize(40);
						//applet.text("Wrong Detected direction: " + msg_direction[max_error_index] + " Question:" + msg_direction[question], 400, 500);
						applet.text("Detected direction:", 400, 500);
						draw_arrow.drawByIndex(max_error_index, 400, 530, 30);
						
						applet.text("R" + error_index[0] + " L" + error_index[1] + " T" + error_index[2] + " D" + error_index[3], 400, 600);

						//debug
						System.out.println("Wrong Detected direction: " + msg_direction[max_error_index] + " GndTruth:" + msg_direction[question]);
						System.out.println("counter:" + counter +" offset:" + offset + " max_error_index:" + max_error_index + "R" + error_index[0] + " L" + error_index[1] + " T" + error_index[2] + " D" + error_index[3]);
					
					}else{ // show output for no swipe 						
						
						// log result
						result_logger.writeln(ques_ind + ",X, gnd_truth," + question + ",result, -1" + ",counter," + counter +",offset," + offset + ",max_error_index," + max_error_index + ",max_error," + max_error + ",R," + error_index[0] + ",L," + error_index[1] + ",T," + error_index[2] + ",D," + error_index[3] +  ",P," + error_index[4] + "," + rand_application_buffer_string);

						// UI
						applet.fill(255, 0, 0);
						applet.textSize(40);
						applet.text("no swipe", 400, 500);
						
						//debug
						System.out.println("Wrong Detected direction: no swipe" + " GndTruth:" + msg_direction[question]);
						System.out.println("counter:" + counter +" offset:" + offset + " max_error_index:" + max_error_index + "R" + error_index[0] + " L" + error_index[1] + " T" + error_index[2] + " D" + error_index[3]);
					}
				
				}else{ // errrr
	
					// log result
					result_logger.writeln(ques_ind + ",X, gnd_truth," + msg_direction[question] + ",result, ?"  + ",counter," + counter +",offset," + offset + ",max_error_index," + max_error_index + ",max_error," + max_error + ",R," + error_index[0] + ",L," + error_index[1] + ",T," + error_index[2] + ",D," + error_index[3] + ",P," + error_index[4] +"," + rand_application_buffer_string);
					
					//debug
					System.out.println("Orphan Detected direction: " + msg_direction[question] + " GndTruth:" + msg_direction[question]);
					System.out.println("counter:" + counter +" offset:" + offset + " max_error_index:" + max_error_index + "R" + error_index[0] + " L" + error_index[1] + " T" + error_index[2] + " D" + error_index[3]);
					
				}
			}
		}
		else {  // random_app_count==RAND_APPLICATION_TOTAL_FRAME should be the frame to judge overall result
			
			// TODO: UI
			double accuracy = (double) nr_correct / RAND_SWIPE_NUM;
			applet.textSize(40);
			applet.text("Awesome! Test completed! " + RAND_SWIPE_NUM, 400, 500);
			//applet.text("Accuracy: " + accuracy + " nr_correct: " + nr_correct + " total_num: " + RAND_SWIPE_NUM, 400, 500);
			
			// log result
			result_logger.writeln("Accuracy: " + accuracy + " nr_correct: " + nr_correct + " total_num: " + RAND_SWIPE_NUM);	
			
			if (counter == RAND_APPLICATION_TOTAL_FRAME){
				ground_logger.write_end();
				result_logger.write_end();
			}
		}
		
		counter++;
	}
	
	public int[] swipe_count(int a[], int a_length) {
		int[] index_count = { 0, 0, 0, 0, 0 };

		for (int i = 0; i < a_length; i++) {
			if(a[i] != -1) // so accumulated index_count will not be equal to a_length!
				index_count[a[i]] += 1;
		}

		return index_count;
	}
	
	private int[] generate_random_swipe_sequence() {
		Random random = new Random();
		int[] re = new int[RAND_SWIPE_NUM];
		for (int i = 0; i < RAND_SWIPE_NUM; i++)
			re[i] = i * 5 / RAND_SWIPE_NUM;
		for (int i = RAND_SWIPE_NUM - 1; i > 0; i--) {
			int a = random.nextInt(i);
			int t = re[a];
			re[a] = re[i];
			re[i] = t;
		}
		return re;
	}
}

