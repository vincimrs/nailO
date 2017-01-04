package base.application;

import processing.core.PApplet;
import base.Swipe;
import base.processing.DataProcessor;
import base.visual.DrawText;

public class RainbowApplication {
	private final int[] red   = {255, 255, 255, 128,   0,   0,   0,   0,   0,  76};
	private final int[] green = {  0, 128, 255, 255, 255, 255, 255, 128,   0,   0};
	private final int[] blue  = {  0,   0,   0,   0,   0, 128, 255, 255, 255, 153};
	
	private PApplet applet;
	private DrawText draw_text;
	
	private DataProcessor processor;
	
	private int swipe_index = 0;
	
	public RainbowApplication(PApplet _applet, double _T, int _buff, String _filename, int _stateFlag, int _electrodeFlag) {
		applet = _applet;
		draw_text = new DrawText(applet);
		processor = new DataProcessor(_T, _buff, _filename, _stateFlag, _electrodeFlag);
		
	}
	
	public void on_draw(int[] data_raw) {
		int swipe_dir = processor.raw_data_processing(data_raw);
		if (swipe_dir == Swipe.RIGHT) // -->
			swipe_index++;
		if (swipe_dir == Swipe.LEFT)
			swipe_index--;
		
		if (swipe_index < 0)
			swipe_index = 0;
		if (swipe_index > 9)
			swipe_index = 9;			

		// print gravity
		draw_text.draw(0, 0, 0, 900, 300, 15, "Centroid: x: " + processor.get_x_center() + " y: " + processor.get_y_center());
		
		applet.fill(red[swipe_index], green[swipe_index], blue[swipe_index]);
		applet.rect(100, 370, 800, 380);
		
		draw_text.draw(0, 0, 0, 500, 600, 50, "" + (swipe_index+1));
	}
}
