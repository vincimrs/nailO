package base.application;

import processing.core.PApplet;
import base.Swipe;
import base.coor.transform.CoorTransform;
import base.logger.CustomizedLogger;
import base.processing.DataProcessor;
import base.processing.Mask;
import base.visual.DrawArrow;
import base.visual.DrawText;
import base.visual.Visual;

public class RainbowQuizApplication {
	private final int STATE_BEGINNING = 0;
	private final int STATE_QUESTION = 1;
	private final int STATE_RESULT = 2;
	
	private final int[] red   = {255, 255, 255, 128,   0,   0,   0,   0,   0,  76};
	private final int[] green = {  0, 128, 255, 255, 255, 255, 255, 128,   0,   0};
	private final int[] blue  = {  0,   0,   0,   0,   0, 128, 255, 255, 255, 153};
	
	private final int[] question_award_dir   = {Swipe.RIGHT, Swipe.LEFT,  Swipe.TOP,  Swipe.DOWN};
	private final int[] question_penalty_dir = {Swipe.LEFT,  Swipe.RIGHT, Swipe.DOWN, Swipe.TOP};
	
	private PApplet applet;
	private DrawText draw_text;
	private Visual visualAid;
	
	private DataProcessor processor;
	private CustomizedLogger result_logger;
	
	private int state;
	private int counter;
	private int question_ind;  // which question, corresponding to swipe direction
	private int question_progress;  // corresponding to rainbow color
	private long question_start_time;
	private long question_interval;
	
	
	private DrawArrow draw_arrow;
	int rainbow_app_buf[] = new int[50000]; // buffer to store results and calculate 
	int rainbow_app_buf_index = 0;
	int[] buf_result_index = new int[5];
	double accuracy;
	double[] confusion_matrix = new double[5];
	int meaningful_buf_index_num;
	
	
	public RainbowQuizApplication(PApplet _applet, double _T, int _buff, String _filename, String _filename_result, int _stateFlag, int _electrodeFlag) {
		applet = _applet;
		draw_text = new DrawText(applet);
		processor = new DataProcessor(_T, _buff, _filename, _stateFlag, _electrodeFlag);
		result_logger =  new CustomizedLogger(_filename_result);
		draw_arrow = new DrawArrow(applet);
		
		visualAid = new Visual(applet); // visualAid
		
		state = STATE_BEGINNING;
		counter = 0;
	}
	
	public void on_draw(int[] data_raw) {
		int[] tc;  // tmp color, since use it a lot, thus declare here.
		
		int swipe_dir = processor.raw_data_processing(data_raw);
		
		System.out.println("in RainbowQuizApp"); //debug
		
		double[][] data = CoorTransform.coordinate_transistion_2d(data_raw); // display visualAid
		double[][] data_mask = Mask.mask_data(data); //
		visualAid.matrix_viz(data, 830, 300, 30, 1);
		visualAid.matrix_viz(data_mask, 980, 300, 30, 1);
		
		
		if (state == STATE_BEGINNING) {
			final String title = "This is a Rainbow Quiz!";
			for (int i = 0; i < title.length(); i++) {
				if (10 <= i && i < 17)
					draw_text.draw(red[i-10], green[i-10], blue[i-10], 200 + 20 * i, 200, 40, title.substring(i, i+1));
				else
					draw_text.draw(0, 0, 0, 200 + 20 * i, 200, 40, title.substring(i, i+1));
			}
			draw_text.draw(64, 64, 64, 300, 300, 18, "In this task, you need to do:");
			draw_text.draw(64, 64, 64, 300, 330, 18, "Swipe right and repeat 10 times");
			draw_text.draw(64, 64, 64, 300, 360, 18, "Swipe left and repeat 10 times");
			draw_text.draw(64, 64, 64, 300, 390, 18, "Swipe top and repeat 10 times");
			draw_text.draw(64, 64, 64, 300, 420, 18, "Swipe down and repeat 10 times");
			draw_text.draw(0, 0, 0, 200, 500, 30, "Easy?! How fast you can finish it?");
			double ratio = Math.abs(Math.sin((double)counter / 25.5));
			tc = color_gred(255, 255, 255, 255, 0, 0, ratio);
			draw_text.draw(tc[0], tc[1], tc[2], 250, 650, 20, "(Long press to start)");
			draw_text.draw(0, 0, 0, 650, 670, 12, "-> It will start as soon as\nyou release your finger!!");
			
			if (swipe_dir == Swipe.PRESS) {
				state = STATE_QUESTION;
				counter = 0;
				question_ind = 0;
				question_progress = 0;
				question_start_time = System.currentTimeMillis();
				
				// start logging result
				result_logger.write_begin(); 
			}
		}
		else if (state == STATE_QUESTION) {
			
			draw_text.draw(0, 0, 0, 100, 100, 40, "Instruction: swipe " + Swipe.get_text(question_ind));
			int[][] rects = get_rect_params(counter, question_ind);
			int[] color_bar = color_gred(red[question_progress], green[question_progress], blue[question_progress], 255, 255, 255, 0.5);
			for (int i = 0; i < 3; i++) 
				if (rects[i] != null) {
					if (i % 2 == 0)
						applet.fill(red[question_progress], green[question_progress], blue[question_progress]);
					else
						applet.fill(color_bar[0], color_bar[1], color_bar[2]);
					applet.rect(rects[i][0], rects[i][1], rects[i][2], rects[i][3]);
				}
			
			long time_elapsed = System.currentTimeMillis() - question_start_time;
			draw_text.draw(0, 0, 0, 300, 650, 20, ms_to_watch_time(time_elapsed));
			
			// feedback
			if(swipe_dir != Swipe.NO){
				applet.textSize(30);
				applet.text("detected direction", 650, 200);
				draw_arrow.drawByIndex(swipe_dir, 650, 230, 30);				
			}
			
			if (swipe_dir == question_award_dir[question_ind])
				question_progress++;
			/*if (swipe_dir == question_penalty_dir[question_ind])
				question_progress--;  */
			if (question_progress < 0)
				question_progress = 0;
			
			result_logger.writeln("gnd_truth,"  + question_award_dir[question_ind] + ",result," + swipe_dir + 
					", question_progress," + question_progress); // log result
			
			rainbow_app_buf[rainbow_app_buf_index] = swipe_dir; // store in buf
			rainbow_app_buf_index += 1;
			
			if (question_progress == 10) { // end of a swipe session

				buf_result_index = swipe_count(rainbow_app_buf, rainbow_app_buf_index); //calculate results
				meaningful_buf_index_num = 0;
				for(int i = 0; i < 5; i ++)	meaningful_buf_index_num += buf_result_index[i];
				for(int i = 0; i < 5; i ++)	confusion_matrix[i] = (double)buf_result_index[i] / meaningful_buf_index_num;
				accuracy = (double) question_award_dir[question_ind] / meaningful_buf_index_num;
				
				result_logger.writeln("swipe " +question_award_dir[question_ind] + "ended, time_elapsed," + time_elapsed + 
						"," + ms_to_watch_time(time_elapsed)); // logging data
				result_logger.writeln("result_index, [0]," + buf_result_index[0] + "," + confusion_matrix[0] + 
						",[1]," + buf_result_index[1]+ ", " + confusion_matrix[1] + 
						",[2]," + buf_result_index[2]+ ","+ confusion_matrix[2] + 
						",[3]," + buf_result_index[3] + ","+ confusion_matrix[3] + 
						",[4]," + buf_result_index[4] + ","+ confusion_matrix[4] + ", accuracy," + accuracy);
				
				rainbow_app_buf_index = 0; //reset
				
				question_ind++; // onto next question
				question_progress = 0;
				if (question_ind == 4) {
					question_interval = time_elapsed;
					state = STATE_RESULT;
				}
				
			}
			
		}
		else if (state == STATE_RESULT) {
			draw_text.draw(0, 0, 0, 100, 100, 30, "Amazing!! You finished the task in:");
			draw_text.draw(0, 0, 0, 200, 180, 80, ms_to_watch_time(question_interval));
			
			// log result
			result_logger.writeln("total time_elapsed," + question_interval + "," + ms_to_watch_time(question_interval));
			result_logger.write_end();
		}
		
		counter++;
	}
	
	private int[] color_gred(int src_r, int src_g, int src_b, int dst_r, int dst_g, int dst_b, double ratio) {
		// wanna return r, g, b at the same time, thus return an 3-element array as r, g, b value
		int[] re = {
				gred(src_r, dst_r, ratio),
				gred(src_g, dst_g, ratio),
				gred(src_b, dst_b, ratio),
		};
		return re;
	}
	
	
	private int[][] get_rect_params(int _counter, int dir) {
		_counter %= 45;
		if (_counter > 30) {
			int[][] re = { {200, 200, 400, 400}, null, null };
			return re;
		}
		else {
			int bar_lx = gred(-40, 400, (double)_counter / 30.0);
			int bar_rx = bar_lx + 40;
			int[][] rect_coor = {
				{0,      0, bar_lx, 400},
				{bar_lx, 0, bar_rx, 400},
				{bar_rx, 0, 400,    400},
			};
			for (int i = 0; i < 3; i++)
				if (rect_coor[i][0] >= rect_coor[i][2])
					rect_coor[i] = null;
				
			final int[] rotate_times = {0, 2, 3, 1};
			for (int i = 0; i < rotate_times[dir]; i++)
				rect_coor = rotate(rect_coor);
			return fit_rect_param(rect_coor);
		}
	}
	
	private int gred(int a, int b, double ratio) {
		return (int)(a + (b-a) * ratio);
	}
	
	private int[][] rotate(int[][] rects) {
		// (0, 0) -----------> (400, 0)
		//   ^                    |
		//   |                    |
		//   |                    v
		// (0, 400) <--------- (400, 400)
		for (int[] rect : rects) 
			if (rect != null) {
				for (int i = 0; i < 4; i += 2) {
					int x = rect[i];
					int y = rect[i+1];
					rect[i]   = 400 - y; 
					rect[i+1] = x;
				}
			}
		return rects;
	}
	
	private int[][] fit_rect_param(int[][] rects) {
		// change (x1, y1, x2, y2) into (x, y, w, h) format
		// also change origin from (0, 0) to (200, 200)
		for (int[] rect : rects) 
			if (rect != null) {
				rect[2] -= rect[0];
				rect[3] -= rect[1];
				rect[0] += 200;
				rect[1] += 200;
			}
		return rects;
	}
	
	private String ms_to_watch_time(long delta) {
		int ms = (int)delta % 1000;
		delta /= 1000;
		int s = (int)delta % 60;
		delta /= 60;
		int m = (int)delta;
		return String.format("%d:%02d.%03d", m, s, ms);
	}
	
	public int[] swipe_count(int a[], int a_length) {
		int[] index_count = { 0, 0, 0, 0, 0 };

		for (int i = 0; i < a_length; i++) {
			if(a[i] != -1) // so accumulated index_count will not be equal to a_length!
				index_count[a[i]] += 1;
		}

		return index_count;
	}
}