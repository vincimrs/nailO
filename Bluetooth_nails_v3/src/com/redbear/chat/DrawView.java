package com.redbear.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View {
    Paint paint = new Paint();
    int [] color = {0,0,0,0,0,0,0,0,0}; 
    String gesture = "none"; 

    public DrawView(Context context) {
        super(context);            
    }
    
    public void changeColor(int []newColor) { 
    	color[0] = newColor[0]; 
    	color[1] = newColor[5]; 
    	color[2] = newColor[8]; 
    	color[3] = newColor[1]; 
    	color[4] = newColor[3]; 
    	color[5] = newColor[7]; 
    	color[6] = newColor[2]; 
    	color[7] = newColor[4]; 
    	color[8] = newColor[6]; 
    	
    }
    
    public void displayGesture(String input) { 
    	gesture = input;
    }

    
    @Override
    public void onDraw(Canvas canvas) {

        paint.setColor(Color.rgb(color[0],0,0));
        canvas.drawRect(30, 30, 300, 300, paint );
        paint.setColor(Color.rgb(color[1],0,0));
        canvas.drawRect(340, 30, 300+300, 300, paint );
        paint.setColor(Color.rgb(color[2],0,0));
        canvas.drawRect(340+300, 30, 300+600, 300, paint );

        
        //electrode 4 and 5 switch places 
        paint.setColor(Color.rgb(color[3],0,0));
        canvas.drawRect(30, 330, 300, 300+300, paint );
        
        paint.setColor(Color.rgb(color[4],0,0));
        canvas.drawRect(340, 330, 300+300, 300+300, paint );
        
        paint.setColor(Color.rgb(color[5],0,0));
        canvas.drawRect(340+310, 330, 300+300+300, 300+300, paint );
        
        
        paint.setColor(Color.rgb(color[6],0,0));
        canvas.drawRect(30, 330+300, 300, 300+300+300, paint );
        
        paint.setColor(Color.rgb(color[7],0,0));
        canvas.drawRect(340, 330+300, 300+300, 300+300+300, paint );
        
        paint.setColor(Color.rgb(color[8],0,0));
        canvas.drawRect(340+310, 330+300, 300+300+300, 300+300+300, paint );
        
       // paint.setTextSize(70); 
       // canvas.drawText("Baseline", 50, 1434, paint);
        
        paint.setTextSize(120); 
        canvas.drawText(gesture, 50, 1234, paint);
        

    }

}