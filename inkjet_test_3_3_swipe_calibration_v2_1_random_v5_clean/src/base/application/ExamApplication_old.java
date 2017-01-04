package base.application;

import java.util.Random;

import processing.core.PApplet;
import base.Swipe;
import base.logger.GroundTruthLogger;
import base.processing.DataProcessor;
import base.visual.DrawArrow;

public class ExamApplication_old {
	private final int BASIC_TIME_SLOT = 30;
	private final int RAND_SWIPE_NUM = 60;
	private final int RAND_QUESTION_TIME_SLOT = 2;
	private final int RAND_RESULT_TIME_SLOT = 2;
	private final int RAND_QUESTION_FRAME = RAND_QUESTION_TIME_SLOT * BASIC_TIME_SLOT;
	private final int RAND_RESULT_FRAME = RAND_RESULT_TIME_SLOT * BASIC_TIME_SLOT;
	private final int RAND_ROUND_FRAME = RAND_QUESTION_FRAME + RAND_RESULT_FRAME;
	private final int RAND_APPLICATION_TOTAL_FRAME = RAND_ROUND_FRAME * RAND_SWIPE_NUM;
	
	private final String[] msg_direction = { "swipe right", "swipe left", "swipe up", "swipe down", "long press" };
	private int[] selected_swipe_seq;
	
	private PApplet applet;
	private DrawArrow draw_arrow;
	
	//private String filename_exam;
	private int counter = 0;  // since itself will play the whole script, so it needs to keep an internal counter
	
	private DataProcessor processor;
	private GroundTruthLogger ground_logger;
	
	private int nr_correct = 0;
	
	private int nr_guess_wrong;
	private int nr_guess_right;
	
	public ExamApplication_old(PApplet _applet, double _T, int _buff, String _filename_exam, String _filename_exam_log, int _stateFlag, int _electrodeFlag) {
		applet = _applet;
		draw_arrow = new DrawArrow(applet);
		//filename_exam = _filename_exam;
		processor = new DataProcessor(_T, _buff, _filename_exam_log, _stateFlag, _electrodeFlag);
		ground_logger = new GroundTruthLogger(_filename_exam);
		selected_swipe_seq = generate_random_swipe_sequence();
	}
	
	public void on_draw(int[] data_raw){
		if (counter == 0)
			ground_logger.write_begin();
			
		// script happened in order...
		if (counter < RAND_APPLICATION_TOTAL_FRAME) {
			int ques_ind = counter / RAND_ROUND_FRAME;
			int offset = counter % RAND_ROUND_FRAME;
			int question = selected_swipe_seq[ques_ind];
			
			System.out.println("in on_draw(), counter: " + counter  + " ind: " + ques_ind +  " offset: " + offset);
			
			if (offset < RAND_QUESTION_FRAME) {   // meaning that it's still in question phase
				if (offset == 0) {
					processor.reset();
					nr_guess_wrong = 0;
					nr_guess_right = 0;
				}
				
				ground_logger.log(data_raw, question);
				
				applet.text("Test on: " + msg_direction[question], 400, 200);
				draw_arrow.drawByIndex(question, 400, 300, 80);
				
				int process_result = processor.raw_data_processing(data_raw);
				if(process_result != Swipe.NO){
					if (process_result == question){
						nr_guess_right++;

					}else{
						nr_guess_wrong++;
						
					}
				}
				
				System.out.println( "((((((( offset < RAND_QUESTION_FRAME )))))))");
			}
			else {    // maybe offset == RAND_QUESTION_FRAME should judge the swipe direction is correct or not?
				// TODO: UI
				if (offset == RAND_QUESTION_FRAME) {
					System.out.println("judge time: right = " + nr_guess_right + " wrong = " + nr_guess_wrong );
					//if (nr_guess_right > 0 && nr_guess_wrong == 0){
					if (nr_guess_right > 0){	
						nr_correct++;
						
						
						// TODO: print results
						applet.fill(255, 0, 0);
						applet.text("Detected direction: " + msg_direction[question], 600, 600);
						draw_arrow.drawByIndex(question, 600, 700, 60);
						System.out.println("[[[[[[[  offset == RAND_QUESTION_FRAME ]]]]]]]] && nr_guess_right > 0");
					}
				}
				
				
			}
		}
		else {  // random_app_count==RAND_APPLICATION_TOTAL_FRAME should be the frame to judge overall result
			ground_logger.write_end();
			
			// TODO: UI
		}
		
		counter++;
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
