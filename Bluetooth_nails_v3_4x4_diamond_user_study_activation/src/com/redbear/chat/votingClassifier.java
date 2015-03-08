package com.redbear.chat;

public class votingClassifier {
	private int size; 
	private double samples []; 
	private int counter []; 
	private int numberOfLabels; 
	
	public votingClassifier(int size, int numberOfLabels) { 
		this.size = size; 
		this.numberOfLabels = numberOfLabels ; 
		samples = new double[size];
		counter = new int[numberOfLabels];
		for (int i = 0; i < size; i++) samples[i] = 0d;
		for (int i = 0; i < numberOfLabels; i++) counter[i] = 0;
		
	}//end votingClassifier 
	
	public void add(double inputLabel) { 
		double tempArray []; 
		tempArray = new double[size]; 
		
		for (int i =0; i<numberOfLabels; i++) counter[i] = 0;
		
		for(int i=1; i<size; i++) { 
			tempArray[i] = samples[i-1]; 			
		}//end for 
		tempArray[0] = inputLabel; 
		
		samples = tempArray; 
		
		
		for(int m = 0; m<numberOfLabels; m++){
		
			for (int g = 0; g<size; g++ ){ 
				if (samples[g]==m) { 
					counter[m]++; 
				}//end if 
			}//end for
		}//end for 
		

		
		
	}//end add
	
	public int getVote() { 	
		int maxIndex = 0;
		for (int i = 1; i < counter.length; i++){
		   int newnumber = counter[i];
		   if ((newnumber > counter[maxIndex])){
		   maxIndex = i;
		  }
		} 
		return maxIndex; 		
	}//end getVote
	
	public void displayBuffer() { 
		for (int i = 0; i < size; i++) {
			System.out.print(samples[i] + " , "); 
		}//end for
	}
	
	public int getSum(int whichClass) {
		int sum = 0; 
		for (int i=0; i<size; i++) { 
			if (samples[i] == whichClass){ 
				sum = sum + 1; 
			}//end if 
			else 
				if (sum>0) {
					sum = sum -1;
				}
		}//end for 
		return sum;
	}
		

	
	

}
