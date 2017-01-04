package base.processing;

public class Mask {
	// make the mask as private, guess no one need to know the detail
	private static int[][] mask = {
		{1, 2, 1},
		{2, 6, 2},
		{1, 2, 1},
	};
	
	public static double[][] mask_data(double[][] data){
		int mask_aggregate;
		int mask_total;
		double data_mask[][] = new double[3][3];
		
		// the center now
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				mask_aggregate = 0;
				mask_total = 0;
				
				// mask orientation
				for(int i_mask=-1; i_mask<=1; i_mask++){
					for(int j_mask=-1; j_mask<=1; j_mask++){
						if( ((i+i_mask) >=0) && ((i+i_mask) <=2) && ((j+j_mask) >= 0) && ((j+j_mask) <= 2) ){
							mask_aggregate += data[i+i_mask][j+j_mask] * mask[i_mask+1][j_mask+1];
							mask_total += mask[i_mask+1][j_mask+1];
							data_mask[i][j] = (float)mask_aggregate / mask_total; 
						}     
					}	
				}//end mask orientation		
				
			}
		}
		return data_mask;
	}
	
	public static double get_centroid_x(double[][] data) {
		double sum = 0.0;
		double weight = 0.0;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				double t = data[i][j] + 1.0;  // to avoid divide by 0
				sum += t * i;
				weight += t;
			}
		return sum / weight;
	}
	
	public static double get_centroid_y(double[][] data) {
		double sum = 0.0;
		double weight = 0.0;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				double t = data[i][j] + 1.0;  // to avoid divide by 0
				sum += t * j;
				weight += t;
			}
		return sum / weight;
	}
}
