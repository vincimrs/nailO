package base.visual;

import processing.core.PApplet;
import base.coor.transform.CoorTransform;


/*
 * move this two visualization methods in this class.
 */
public class Visual {
	private PApplet applet;

	public Visual(PApplet _applet) {
		applet = _applet;
	}
	
	public void matrix_viz(double[][] data, int x, int y, int square_size, int electrodeFlag){
		int center_color;

		// 3X3 square visualization
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				
				center_color = 255 - ((int) data[i][j]);
				
				if (center_color < 250  && center_color > 20){
					center_color = center_color - 20;
				}
				
				System.out.println("center_color: " + center_color + " data[" + i + "][" + j +"] = " + data[i][j]);
				
				applet.fill(255, center_color, center_color);
				applet.rect(x + i * (square_size + 5), y + j
						* (square_size + 5), square_size, square_size);
				// text
				applet.fill(0, 0, 0);
				applet.textSize(10);
				int text_x_coordinate = x + 10;
				int text_y_coordinate = y + 15;
				applet.text("R" + CoorTransform.map[i][j], text_x_coordinate
						+ i * (square_size + 5), text_y_coordinate + j
						* (square_size + 5));
				applet.text("" + data[i][j], text_x_coordinate + i
						* (square_size + 5), (text_y_coordinate + 10) + j
						* (square_size + 5));

				if (electrodeFlag == 0) {
					applet.fill(0, 0, 255);
					applet.text("Logging R" + CoorTransform.map[i][j],
							text_x_coordinate + i * (square_size + 5),
							(text_y_coordinate + 30) + j * (square_size + 5));
				}
			}
		}

	}// end matrix_viz
	
	
	public void histogram_viz(int[] data, int x, int y_base, int size) {

		// histograms
		for (int i = 0; i <= 14; i++) {
			applet.fill(255, 0, 0);
			applet.rect(x + ((size + 5) * i), y_base - data[i], size, data[i]);
			applet.fill(0, 0, 0);
			applet.textSize(15);
			applet.text("R" + i, 100 + ((size + 5) * i) + 5, y_base + 20);
			applet.textSize(15);
			applet.text(data[i], 100 + ((size + 5) * i) + 5, y_base + 40);
		}

	}// end historam_viz
}
