package base.visual;

import processing.core.PApplet;

public class DrawText {
	private PApplet applet;

	public DrawText(PApplet _applet) {
		applet = _applet;
	}
	
	public void draw(int r, int g, int b, int x, int y, int font_size, String str) {
		applet.textSize(font_size);
		applet.fill(r, g, b);
		applet.text(str, x, y);
	}
}
