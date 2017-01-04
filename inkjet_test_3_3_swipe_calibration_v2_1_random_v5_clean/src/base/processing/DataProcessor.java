package base.processing;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import base.FloatList;
import base.Swipe;
import base.coor.transform.CoorTransform;

/*
 * The goal of DataProcessor is to process raw data and decide whether there is a swipe event "from now on".
 * We make an abstraction that DataProcessor takes one "vector", or an array of int each time. This lets DataProcessor
 * be able to do real-time detection, or off-line detection.
 * 
 */
public class DataProcessor {
	private final int SMOOTH_DATA_ARRAY_TOTAL_INDEX = 11; //data_mask (9 values),  center_y, center_x
	private final int SMOOTHING_QUEUE_SIZE = 5; 
	private final int START_COUNT_THRES = 55;
	private final int LONG_PRESS_DETECTION_THRESHOLD = 1; // threshold for detecting if long press
	
	private final int X_CENTER_INDEX = 9;
	private final int Y_CENTER_INDEX = 10;
	
	private double T;
	private int buff;
	private String filename;
	int stateFlag;
	int electrodeFlag;
	
	private FloatList[] smoothing_queue = new FloatList[SMOOTH_DATA_ARRAY_TOTAL_INDEX];
	private float [] smooth_data = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
	private double x_center;
	private double y_center;
	private float x_avg_front;
	private float y_avg_front;
	private float x_avg_mid;
	private float y_avg_mid;
	private float x_avg_end;
	private float y_avg_end;
	
	private int swipe_buffer_count = 0; // counting buffer
	private float[][] swipe_buffer = new float[80][13];
	
	private int direction_vote_buf_index = 0; //swipe_buffer index
	private int direction_vote_buf[] = new int[10000]; // swipe buffer
	
	private int[] current_raw;
	
	public DataProcessor(double _T, int _buff, String _filename, int _stateFlag, int _electrodeFlag) {
		// initializing queue
		for (int i=0; i<smoothing_queue.length; i++){
			smoothing_queue[i] = new FloatList();
		}
		
		filename = _filename;
		reset_with_parameters(_T, _buff);
		stateFlag = _stateFlag;
		electrodeFlag = _electrodeFlag;
	}
	
	// ---- reset -----------------------------------------------------------------------------------
	// need to be able to reset client. Since DataProcessor is stateful class, when processing a
	// record, it keeps in internal arrays. Reset() can clean the internal buffers.
	public void reset() {
		reset_with_parameters(T, buff);  // pass the values we have now to avoid duplicate work.
	}
	
	public void reset_with_parameters(double _T, int _buff) {
		T = _T;
		buff = _buff;
		swipe_buffer_count = 0; // for swipeDetection() reset
		
		log_state(filename, "T: " + T, ",BUF: " + buff); // log state once
	}
	
	// ---- process ---------------------------------------------------------------------------------
	// This is the bulk of the meat in DataProcessor. 
	
	public int raw_data_processing(int[] raw) {
		// the bulk of the meat
		int swipe_direction = Swipe.NO;

		// log para only if main swipe detection
		//log_state(filename, "T: " + T, ",BUF: " + buff);
		
		// transistion data_raw[] to data[][]
		double[][] data = CoorTransform.coordinate_transistion_2d(raw);

		// mask data
		double[][] data_mask = Mask.mask_data(data);

		x_center = Mask.get_centroid_x(data_mask);
		y_center = Mask.get_centroid_y(data_mask);
		
		/* smoothing and putting into queue */
		// put current data[] into queue
		// center_y, center_x
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int index = (i * 3) + j;
				smoothing_queue[index].add((float) data_mask[i][j]);
			}
		}
		// put center_x, center_y in queue
		smoothing_queue[9].add((float)x_center);
		smoothing_queue[10].add((float)y_center);

		if (smoothing_queue[0].size() == SMOOTHING_QUEUE_SIZE) { // start removing after there are five values

			for (int i = 0; i < SMOOTH_DATA_ARRAY_TOTAL_INDEX; i++) { // store data[] and center_x, center_y
				smooth_data[i] = 0; // initialize
				for (int j = 0; j < SMOOTHING_QUEUE_SIZE; j++) { // get 5 values in queue
					smooth_data[i] += smoothing_queue[i].get(j);
				}
				smooth_data[i] /= SMOOTHING_QUEUE_SIZE;
				// System.out.print(smooth_data[i] + " ");
			}

			// Bulk of the meat! detect swipes!
			swipe_direction = swipe_detection(smooth_data);

			// pop one out of queue
			for (int i = 0; i < SMOOTH_DATA_ARRAY_TOTAL_INDEX; i++) {
				smoothing_queue[i].remove(0);
			}
		}
		
		current_raw = raw;

		// logging data
		log_data();
		
		return swipe_direction;

	}

	public int force_output_direction_and_empty_buffer() {
		int swipe_direction = Swipe.NO;
		if ((swipe_buffer_count > 0) && (direction_vote_buf_index > 0)) { 
			// was previously touched  && there were enough values to put in swipe_buf_index
			
			/* swipe detection */
			// calculate the number of each swipe direction in swipe_buf
			swipe_direction = get_result_from_direction_vote_buf();
		}

		// reset
		direction_vote_buf_index = 0;
		/*-- end --*/

		swipe_buffer_count = 0;
		return swipe_direction;
	}
	

	private int swipe_detection(float[] smooth_data) {

		boolean start_flag = false;
		
		int swipe_direction = Swipe.NO;

		for (int i = 0; i < 9; i++) { // check that it is pressed
			if (smooth_data[i] >= START_COUNT_THRES) {
				start_flag = true;
				break;
			}
		}

		if (start_flag) { // a value is pressed

			// put it in buffer
			for (int i = 0; i <= SMOOTH_DATA_ARRAY_TOTAL_INDEX; i++) {
				swipe_buffer[swipe_buffer_count][i] = smooth_data[i];
			}

			swipe_buffer_count += 1;

			// yay! we've got enough values. this is a swipe or a long press
			if (swipe_buffer_count == (buff + 1)) {

				// swipe analysis
				swipe_analyzer();

				// move buffer forward
				for (int i = 0; i < buff; i++) {
					for (int j = 0; j <= SMOOTH_DATA_ARRAY_TOTAL_INDEX; j++) {
						swipe_buffer[i][j] = swipe_buffer[i + 1][j];
					}
				}

				swipe_buffer_count -= 1;

			}

		} else { // not pressed

			/*---- calculate the major swipe direction logged in swipe_buf to find the direction of this swiping window ----*/
			swipe_direction = force_output_direction_and_empty_buffer();

		}

		// swipe_direction
		return swipe_direction;

	}

	private int get_result_from_direction_vote_buf() {
		int[] index_count = { 0, 0, 0, 0, 0 };

		for (int i = 0; i < direction_vote_buf_index; i++) {
			int t = direction_vote_buf[i];
			index_count[t] += 1;
		}
		
		int max = 0;
		int re = Swipe.NO;
		for (int i = 0; i < 4; i++) { // check only the swipes.. find
			// the max swipe
			if (index_count[i] > max) {
				max = index_count[i];
				re = i;
			}
		}

		if ((max == 0) && (index_count[Swipe.PRESS] > LONG_PRESS_DETECTION_THRESHOLD)) {
			// no swipe index has any values && PRESS values
			// >LONG_PRESS_DETECTION_THRESHOLD... this is a long swipe
			// TODO: check the threshold... make it tighter
			
			re = Swipe.PRESS;
		}
		
		// log this data
		String buf_print = ""; // debugging
		for (int i = 0; i < direction_vote_buf_index; i++) { // debugging
											// function
			buf_print += ("" + direction_vote_buf[i] + ",");
		}
		log_state(filename, Swipe.get_text(re),
				",max:" + max + ",swipe_buf_index: " + direction_vote_buf_index + "," + buf_print);
		
		return re;
	}

	
	// The main algorithm to detect direction of swipe in current sliding window. All the information is kept
	// in the class, thus no input parameters. Also made it as private.
	private void swipe_analyzer() {
		// / first 1/3 of window
		int one_third = buff / 3;
		
		// detect swipes
		x_avg_front = get_average_from_subarray(0, one_third, X_CENTER_INDEX);
		y_avg_front = get_average_from_subarray(0, one_third, Y_CENTER_INDEX); 
		x_avg_mid   = get_average_from_subarray(one_third, buff - one_third, X_CENTER_INDEX);
		y_avg_mid   = get_average_from_subarray(one_third, buff - one_third, Y_CENTER_INDEX);
		x_avg_end   = get_average_from_subarray(buff - one_third, buff, X_CENTER_INDEX);
		y_avg_end   = get_average_from_subarray(buff - one_third, buff, Y_CENTER_INDEX);

		int x_avg_front_coordinate = round_to_coordinate(x_avg_front);
		int y_avg_front_coordinate = round_to_coordinate(y_avg_front);
		int x_avg_mid_coordinate   = round_to_coordinate(x_avg_mid);
		int y_avg_mid_coordinate   = round_to_coordinate(y_avg_mid);
		int x_avg_end_coordinate   = round_to_coordinate(x_avg_end);
		int y_avg_end_coordinate   = round_to_coordinate(y_avg_end);

		float x_avg_abs = Math.abs(x_avg_front - x_avg_end);
		float y_avg_abs = Math.abs(y_avg_front - y_avg_end);

		// see if it is swipe or long press
		// TODO: can fix confition_long_press latter two conditions a bit more

		boolean condition_long_press = (x_avg_front_coordinate == x_avg_end_coordinate)
				&& (x_avg_front_coordinate == x_avg_mid_coordinate)
				&& (y_avg_front_coordinate == y_avg_end_coordinate)
				&& (y_avg_front_coordinate == y_avg_mid_coordinate)
				&& (x_avg_abs < T) && (y_avg_abs < T);
		boolean condition_swipe = ((x_avg_front_coordinate != x_avg_end_coordinate)
				|| (y_avg_front_coordinate != y_avg_end_coordinate)
				|| (x_avg_abs >= T) || (y_avg_abs >= T));

		/*--------------------------------------------*/
		if (condition_swipe) { // this is a swipe
			// TODO: UI
			/*
			textSize(15);
			fill(0, 0, 0);
			text("*** from (" + x_avg_front + "," + y_avg_front + ") to ("
					+ x_avg_end + "," + y_avg_end + ") ***", 400, 50);
*/
			float x_abs_diff = Math.abs(x_avg_front - x_avg_end);
			float y_abs_diff = Math.abs(y_avg_front - y_avg_end);

			if (x_abs_diff >= y_abs_diff) { // x direction change larger than y
											// direction change

				// text("x change larger than y", 400, 100 );
				// System.out.println(" x change larger than y");
				System.out.println(x_avg_front + " " + x_avg_end + " "
						+ (x_avg_front < x_avg_end));

				if (x_avg_front < x_avg_end) {
					// TODO: UI
					//text(" x_avg < x_avg_end", 400, 120);
					System.out.println(" x_avg < x_avg_end");

					/*--put things in swipe_buffer*/
					direction_vote_buf[direction_vote_buf_index] = Swipe.RIGHT;
					direction_vote_buf_index += 1;

				} else if (x_avg_front > x_avg_end) {
					// TODO: UI
					//text(" x_avg > x_avg_end", 400, 120);
					System.out.println(" x_avg > x_avg_end");

					/*--put things in swipe_buffer*/
					direction_vote_buf[direction_vote_buf_index] = Swipe.LEFT;
					direction_vote_buf_index += 1;
				}

			} else { // y direction change is larger

				// text(" y change larger than x",400, 100 );
				// System.out.println(" y change larger than x");

				if (y_avg_front > y_avg_end) {

					/*--put things in swipe_buffer*/
					direction_vote_buf[direction_vote_buf_index] = Swipe.TOP;
					direction_vote_buf_index += 1;

				} else if (y_avg_front < y_avg_end) {
					// System.out.println("y_avg < y_avg_end");

					/*--put things in swipe_buffer*/
					direction_vote_buf[direction_vote_buf_index] = Swipe.DOWN;
					direction_vote_buf_index += 1;

				}
			}

		} else if (condition_long_press) { // this is long press

			/* -- put things in swipe_buffer */
			direction_vote_buf[direction_vote_buf_index] = Swipe.PRESS;
			direction_vote_buf_index += 1;

		} else {
			// touched, yet not swipe and not long touch. ignore, don't care.
		}

	} // end swipeAnalyzer
	
	
	// ---- getter -----------------------------------------------------------------------------------
	public double get_x_center() {
		return x_center;
	}
	
	public double get_y_center() {
		return y_center;
	}
	// ---- internal functions -----------------------------------------------------------------------
	private float get_average_from_subarray(int s, int e, int attr_ind) {
		float re = 0.0f;
		for (int i = s; i < e; i++)
			re += swipe_buffer[i][attr_ind];
		return re / (e - s);
	}
	
	private int round_to_coordinate(float avg){
		// if avg < 0.5, return 0.5;  if 0.5 < avg < 1.5, return 1; if avg > 1.5, return 1.5
		return (int)(avg + 0.5f);
	}
	
	
	
	// ---- data logging ----------------------------------------------------------------------------
	private void log_state(String filename, String id, String descrip) { 
		try {
			FileWriter fw = new FileWriter(filename, true); // the true will
															// append the new
															// data
			fw.write("" + id + "\t" + descrip);// appends the string to the file
			fw.write("\n");
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}//end saveState
	
	public void log_data() { 
		// TODO: messy. should try to make everything local variable..

		Date date = new Date();
		try {
			FileWriter fw = new FileWriter(filename, true); // the true will append the new data
			long timestamp = System.currentTimeMillis();
			fw.write("" + timestamp);
			for (int i = 0; i < 9; i++)
				fw.write("," + current_raw[i]);
			fw.write("," + smooth_data[X_CENTER_INDEX]);
			fw.write("," + smooth_data[Y_CENTER_INDEX]);
			fw.write("," + x_avg_front);
			fw.write("," + y_avg_front);
			fw.write("," + x_avg_end);
			fw.write("," + y_avg_end);
			fw.write("," + swipe_buffer_count);
			fw.write("\n");
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}

	}// end saveData
}
