package com.redbear.chat;

public class dataProcessor {
	private int inputData[][];
	int NUMBER_ELECTRODES = 9; 
	int BUFFER_SIZE = 10; 
	
    public dataProcessor(int electrodesNumber, int bufferSize) {    
    	NUMBER_ELECTRODES =  electrodesNumber; 
    	BUFFER_SIZE = bufferSize; 
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
	    	}  //end for
    	}//end for 
    }//end add
    
    
	public int [] getAverage() { 
		int average[]; 
		int sum =0; 
		average = new int[NUMBER_ELECTRODES] ;
		
		for (int i=0; i<NUMBER_ELECTRODES; i++) { 	
	    	for (int g =0; g<BUFFER_SIZE; g++) { 
	    		sum  = inputData[i][g] +sum; 
	    	} //end for	
	    	average[i] = sum/BUFFER_SIZE;
	    	sum = 0; 
		}//end for
    	
		return average;
	}
    
	public int averageAllChannels() { 
		int [] averageAll = getAverage(); 
		int sum =0 ; 
		for (int i = 0; i<NUMBER_ELECTRODES; i++) { 
			sum = averageAll[i] + sum; 
		}
		return sum/NUMBER_ELECTRODES; 	
		
	}
	
	public int activationDetected() {
		int activated = 0; 
		if (averageAllChannels() >150) 
			activated = 1; 
		return activated; 
	}
	
	public int [][] getBuffer() { 
		return inputData;
		
	}
}//end class
