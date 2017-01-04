package base.visual;
import processing.core.PApplet;

// Drawing arrows 
public class DrawArrow {
	
		private PApplet applet;

		public DrawArrow(PApplet _applet) {
			applet = _applet;
		}
		
		public void drawRight(int x, int y, int size){
			applet.textSize(size);
			applet.fill(0, 0, 0);
			applet.text("----->", x, y);
		}
		
		public void drawLeft(int x, int y, int size){
			applet.textSize(size);
			applet.fill(0, 0, 0);
			applet.text("<-----", x, y);
			
		}
		
		public void drawTop(int x, int y, int size){
			applet.textSize(size);
			applet.fill(0, 0, 0);
			applet.text("^", x, y);
			applet.text("|", x, y+size);
			applet.text("|", x, y+size*2);
			applet.text("|", x, y+size*3);
			
		}
		
		public void drawBottom(int x, int y, int size){
			applet.textSize(size);
			applet.fill(0, 0, 0);
			applet.text("|", x, y);
			applet.text("|", x, y+size);
			applet.text("|", x, y+size*2);
			applet.text("v", x, y+size*3);
		
		}
		
		
		public void drawPress(int x, int y, int size){
			applet.textSize(size);
			applet.fill(0, 0, 0);
			applet.text("-", x, y);
		
		}
		
		public void drawByIndex(int index, int x, int y, int size){

			if(index == 0)	drawRight(x, y, size);
			if(index == 1)	drawLeft(x, y, size);
			if(index == 2)	drawTop(x, y, size);
			if(index == 3)	drawBottom(x, y, size);
			if(index == 4)	drawPress(x, y, size);
			
		}
		
}
