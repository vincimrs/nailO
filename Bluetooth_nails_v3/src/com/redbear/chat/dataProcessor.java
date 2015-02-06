package com.redbear.chat;

public class dataProcessor {
	private int inputData[][];
	int NUMBER_ELECTRODES = 9; 
	int BUFFER_SIZE = 10; 
	
    public dataProcessor() {    
    	inputData = new int[NUMBER_ELECTRODES][BUFFER_SIZE];
        for (int i=0; i<NUMBER_ELECTRODES; i++) { 
        	for(int g = 0; g< BUFFER_SIZE; g++) { 
        		inputData[i][g] = 0; 
        	}
        }      
    }//end dataProcessor
    
    
    public 	void add(int input[]) { 	
    	int tempArray[][]; 
    	tempArray = new int[NUMBER_ELECTRODES][BUFFER_SIZE];
    	//sum = 0;
    	

    	
    	for (int i=0; i<NUMBER_ELECTRODES; i++) { 
	    	for(int g =1; g<BUFFER_SIZE; g++) { 
	    		tempArray[i][g] = inputData[i][g-1]; 
	    	}
	    	
	    	tempArray[i][0] = input[i];
	    	
	    	for (int g =0; g<BUFFER_SIZE; g++) { 
	    	inputData[i][g] = tempArray[i][g];
	    	}
	    	
    	}
    	
    	
    	/*
    	double tempArray []; 
    	tempArray = new double[size];
    	
    	for(int i =1; i<size; i++) { 
    		tempArray[i] = samples[i-1]; 
    	}
    	tempArray[0] = input; 
    	
    	samples = tempArray;
 
    	               
    	for(int i =0; i<size; i++) { 
    		sum = samples[i] + sum; 
    	}
        

       // System.out.println("average is: " + sum);
    */	   
    }
    
    
    
    
}//end class
