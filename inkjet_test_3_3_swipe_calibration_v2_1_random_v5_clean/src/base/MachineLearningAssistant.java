package base;

import java.io.File;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class MachineLearningAssistant {
	
	
	public void trainClass(int classToTrain, int pointsForTraining, int[] data_raw, Instances trainingData, int numberOfFeatures, String filenameTraining) { 

	    try{ 
	    	
	    	for (int i=0; i<pointsForTraining; i++) {
	    		data_raw[15] = classToTrain; 
	    	//	saveData();
	    		trainingData = MachineLearningAssistant.addInstances(data_raw,trainingData, numberOfFeatures); 
	    		try{
	    		    Thread.sleep(100);
	    		}catch(Exception e)
	    		{
	    		   System.out.println("Exception caught");
	    		}

		  //trainingData = addInstances(data,trainingData);
	    	}
		  System.out.println(trainingData);
	    }catch(Exception e) {System.out.println(e);} 
	        
	    saveToFile(trainingData, filenameTraining); 
	}//end train class
 	
	
	public static Instances CreateInstance() throws Exception{
	   // Declare two numeric attributes
	   Attribute Attribute1 = new Attribute("sensor1");
	   Attribute Attribute2 = new Attribute("sensor2");
	   Attribute Attribute3 = new Attribute("sensor3"); 
	   Attribute Attribute4 = new Attribute("sensor4"); 
	   Attribute Attribute5 = new Attribute("sensor5"); 
	   Attribute Attribute6 = new Attribute("sensor6"); 
	   Attribute Attribute7 = new Attribute("sensor7"); 
	   Attribute Attribute8 = new Attribute("sensor8"); 
	   Attribute Attribute9 = new Attribute("sensor9");
	   Attribute Attribute10 = new Attribute("sensor10"); 
	   Attribute Attribute11 = new Attribute("sensor11"); 
	   Attribute Attribute12 = new Attribute("sensor12"); 
	   Attribute Attribute13 = new Attribute("sensor13"); 
	   Attribute Attribute14 = new Attribute("sensor14"); 
	   Attribute Attribute15 = new Attribute("sensor15"); 
	   // Declare a nominal attribute along with its value
	   
	   // Declare the class attribute along with its values
	   FastVector fvClassVal = new FastVector(7);
	   fvClassVal.addElement("0.0");
	   fvClassVal.addElement("1.0");
	   //fvClassVal.addElement("fingers_extended");
	   fvClassVal.addElement("2.0"); 
	   fvClassVal.addElement("3.0"); 
	   fvClassVal.addElement("4.0");
	   fvClassVal.addElement("5.0");
	   fvClassVal.addElement("6.0");
	   //fvClassVal.addElement("thumb");
	   Attribute ClassAttribute = new Attribute("classes", fvClassVal);
	    
	   
	   // Declare the feature vector
	   FastVector fvWekaAttributes = new FastVector(15);
	   fvWekaAttributes.addElement(Attribute1);    
	   fvWekaAttributes.addElement(Attribute2);    
	   fvWekaAttributes.addElement(Attribute3);    
	   fvWekaAttributes.addElement(Attribute4); 
	   fvWekaAttributes.addElement(Attribute5); 
	   fvWekaAttributes.addElement(Attribute6); 
	   fvWekaAttributes.addElement(Attribute7); 
	   fvWekaAttributes.addElement(Attribute8); 
	   fvWekaAttributes.addElement(Attribute9); 
	   fvWekaAttributes.addElement(Attribute10); 
	   fvWekaAttributes.addElement(Attribute11); 
	   fvWekaAttributes.addElement(Attribute12); 
	   fvWekaAttributes.addElement(Attribute13); 
	   fvWekaAttributes.addElement(Attribute14); 
	   fvWekaAttributes.addElement(Attribute15);  
	   fvWekaAttributes.addElement(ClassAttribute);
	               
	   
	   Instances createdInstances = new Instances("Rel", fvWekaAttributes, 17);    
		
	   //Set Class index
	   createdInstances.setClassIndex(15);
		return createdInstances;
	}//end createInstances
	
	public static double classifyInstance(Classifier model, int [] inputData, int numberOfFeatures) throws Exception{ 
		double ClassLabel = 0; 
		double[] confidence; 
		
		Instances newDataInstance = CreateInstance(); 
		newDataInstance = addInstances(inputData, newDataInstance, numberOfFeatures ); 
		//newDataInstance.setValue((Attribute)fvWekaAttributes.elementAt(4), "?");
			
	  ClassLabel = model.classifyInstance(newDataInstance.instance(0)); 	
	  
	  //confidence = model.distributionForInstances(newDataInstance.instance(0)); 
	
	  
		return ClassLabel;
	}//end classifyInstance
	
	public static Instances addInstances(int[] input, Instances inputInstances, int size) throws Exception { 
		
		Instances createdInstances = inputInstances; 
		
	   double[] instanceValue2 = new double[createdInstances.numAttributes()];
	
	  // instanceValue2[0] = createdInstances.attribute(0).addStringValue("1");
	   
	  
	   for (int i=0; i<size+1; i++) { 
	   	instanceValue2[i] = input[i]; 	
	   }      
	   
	   createdInstances.add(new DenseInstance(1.0, instanceValue2));	
		return createdInstances;
		
	}//end addInstances
	 
	public static Classifier trainClassifer(Instances inputInstances) throws Exception{ 
	  // Classifier cModel = (Classifier)new NaiveBayes();   
		 String[] options = new String[1]; 
		// options[0] = "-C 3.0"; // confidenceFactor = 1.0, minNumObject = 5 
	
	 // Classifier cModel = (Classifier)new SMO();
	  //Classifier cModel = new SMO();
	  weka.classifiers.functions.SMO scheme = new weka.classifiers.functions.SMO();
	  
	  scheme.setOptions(weka.core.Utils.splitOptions("-C 3.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"")); 
	  //((SMO) cModel).setOptions(options);
	//   Classifier cModel = (Classifier)new IBk();  //nearest neighbor
	   scheme.buildClassifier(inputInstances);
	  
		return scheme; 
	}//end trainClassifier
	
	
	
	public  void saveToFile(Instances inputData, String whereToSave) { 
		System.out.println("saveToFile called");
	
		try { 
		ArffSaver saver = new ArffSaver(); 
		saver.setInstances(inputData);
		saver.setFile(new File(whereToSave));
		//saver.setDestination(arg0);
		saver.writeBatch(); 
		} catch(IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
			
	}//end save to file 
	
	
	public double [] getConfidence(Classifier model, int [] inputData, int numberOfFeatures) throws Exception { 
		
		double [] confidence; 
		Instances newDataInstance = CreateInstance(); 
		newDataInstance = addInstances(inputData, newDataInstance, numberOfFeatures ); 
		
	    confidence = model.distributionForInstance(newDataInstance.instance(0)); 
	       
	    /*
	       for (int i =0; i<confidence.length; i++) {
	    	   System.out.print(Double.toString(confidence[i]) + ',');
	       }
	       System.out.println(' ');
	     */ 
	       
	     return confidence;
	}//end getConfidence
	
	
	
}
