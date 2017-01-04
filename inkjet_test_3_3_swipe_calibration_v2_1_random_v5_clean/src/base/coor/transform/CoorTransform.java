package base.coor.transform;

/*
 * declare everything as public since this class serve as a library. 
 * Two parts in this class: public field as map, and a function to re-map raw data
 * to the correct coordinate.
 */
public class CoorTransform {
	// (0, 0) is top left
	/*public static final int[][] map = { // v1 curved 
		{6, 8, 7},
		{5, 3, 4},
		{2, 0, 1}
	}; */
	
	public static final int[][] map = { // v2 straigh, upside down
		{2, 1, 0},
		{4, 3, 5},
		{6, 7, 8}
	};  
	
	/*public static final int[][] map = { // v1 curved, upside down
		{1, 0, 2},
		{4, 3, 5},
		{7, 8, 6}
	}; */
		
	public static double[][] coordinate_transistion_2d(int[] raw){
		// transistion from one dim to 2 dim 
		double[][] re = new double[3][3];
		
		for (int i=0; i<3; i++){
			for (int j=0; j<3; j++){
				re[i][j] = (double)raw[ map[i][j] ];
			}
		}		
		return re;
	}
	
	public static double[] coordinate_transistion_1d(int[] raw) {
		double[][] data2d = coordinate_transistion_2d(raw);
		double[] re = new double[9];
		for (int i = 0; i < 9; i++)
			re[i] = data2d[i / 3][i % 3];
		return re;
	}
}
