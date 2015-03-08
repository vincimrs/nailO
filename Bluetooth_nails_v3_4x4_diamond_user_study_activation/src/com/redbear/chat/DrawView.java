package com.redbear.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

public class DrawView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 
    
    int [] color = {0,0,0,0,0,0,0,0,0}; 
    String gesture = "none"; 
    float xpos = 10;
    float ypos = 10; 
    int PERSISTANCE = 30; 
    int activationState = 0; 
    dataProcessor previousDrawings = new dataProcessor(2,PERSISTANCE);
  
    
    public DrawView(Context context) {
        super(context);            
    }
    
    public void changeColor(int []newColor) { 
    	color[0] = newColor[3]; 
    	color[1] = newColor[2]; 
    	color[2] = newColor[1]; 
    	color[3] = newColor[0]; 
    	color[4] = newColor[4]; 
    	color[5] = newColor[5]; 
    	color[6] = newColor[6]; 
    	color[7] = newColor[7]; 
    	color[8] = newColor[8]; 
    	
    }
    
    public void activationFlag(int activation) { 
    	activationState = activation;
    }
    
    public void displayGesture(String input) { 
    	gesture = input;
    }

    
    @Override
    public void onDraw(Canvas canvas) {
 
    	  Path path = new Path();
    	  
    	  paint.setStyle(Paint.Style.FILL);
        // x-axis
        for (int i = 0; i<4; i++) { 
            paint.setColor(Color.rgb(color[i],0,0));
            canvas.drawRect(120 + 230*i, 20, 120 + 230+230*i, 100, paint );
        }

        //y-axis
        for (int i = 0; i<4; i++) { 
            paint.setColor(Color.rgb(color[i+4],0,0));
            canvas.drawRect(20, 120+210*i, 100, 120+210+210*i, paint );
        }
    	
    	paint.setColor(Color.rgb(0,0,0));
        
       float   sumColor = color[0] + color[1] + color[2] + color[3]; 
       
       if (sumColor != 0 ) { 
         xpos = (1 * (color[0]/ sumColor) + 2* (color[1] / sumColor) + 3*(color[2] / sumColor) + 4*(color[3]/ sumColor))/10;
        paint.setTextSize(120); 
        
        //Log.d("data", Integer.toString(sumColor)); 
       }    	
       
       sumColor = color[4] + color[5] + color[6] + color[7]; 
       if (sumColor >10) { 
            ypos = (1 * (color[4]/ sumColor) + 2* (color[5] / sumColor) + 3*(color[6] / sumColor) + 4*(color[7]/ sumColor))/10;
           paint.setTextSize(120); 
           
           //Log.d("data", Integer.toString(sumColor)); 
          }  
      xpos = xpos*3000 - 200; 
      ypos = ypos*3000 - 200;
       
       //canvas.drawText(Float.toString(xpos), 50, 1234, paint);
       //canvas.drawText(Float.toString(ypos), 50, 1434, paint);
       canvas.drawCircle(xpos, ypos, 20, paint);
       
       if(activationState > 20) { 
    	   canvas.drawText("Activated!", 50, 1034, paint);
       }
       else canvas.drawText(Integer.toString(activationState), 50,1034,paint);
       
       paint.setColor(Color.DKGRAY);
       canvas.drawRect(20, 1034, activationState*50, 1134, paint );
       
       
       int [] temp = {(int) xpos, (int) ypos};
       previousDrawings.add(temp);
       
/*       
       paint = new Paint(Paint.ANTI_ALIAS_FLAG);
       paint.setStyle(Paint.Style.STROKE);
       paint.setStrokeWidth(2);
       paint.setColor(Color.BLACK);*/
       
   	   paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeWidth(4);
       
       path.moveTo(xpos, ypos);
       for (int i =0; i<PERSISTANCE; i++) { 
    	//   canvas.drawCircle(previousDrawings.getBuffer()[0][i], previousDrawings.getBuffer()[1][i], 20, paint);
    	   path.lineTo(previousDrawings.getBuffer()[0][i], previousDrawings.getBuffer()[1][i]);
    	  // canvas.drawPath(path, paint);
       }
       canvas.drawPath(path, paint);
       
 
       
    }//end onDraw

}