package base.logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GroundTruthLogger {
	private static final int STATUS_STAND_BY = 0;
	private static final int STATUS_READ = 1;
	private static final int STATUS_WRITE = 2;
	
	private int status = STATUS_STAND_BY;
	
	private String filename;
	private FileWriter fw = null;
	
	public GroundTruthLogger(String _filename) {
		filename = _filename;
	}
	
	public void write_begin() {
		write_begin(true);
	}
	
	public void write_begin(boolean append_flag) {
		if (status != STATUS_STAND_BY)
			throw new RuntimeException("Attemp to open and write a file in non-standby mode");
		
		status = STATUS_WRITE;
		try {
			fw = new FileWriter(filename, append_flag); // the true will append the new data
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	public void log(int[] data, int groundtruth_index) {
		if (status != STATUS_WRITE)
			throw new RuntimeException("Attemp to write data while not in write mode");
		
		try {
			fw.write("" + groundtruth_index);// appends the string  to the file
			for (int i = 0; i < 9; i++) {
				fw.write("," + data[i]);
			}
			fw.write("\n");
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	public void write_end() {
		if (status != STATUS_WRITE)
			throw new RuntimeException("Attemp to commit write_end command data while the file is not in write mode");
		
		try {
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		status = STATUS_STAND_BY;
	}
	
	// since asinine java doesn't allow returning two arrays at the same time, need to ask logger twice anyway
	// to get groundtruth and rawdata... this is inefficient way to do that, which is read the file twice.
	public int[] read_groundtruth() {
		if (status != STATUS_STAND_BY)
			throw new RuntimeException("Attemp to read file in non-standby mode");
		
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			String fileString;
			while ((fileString = br.readLine()) != null) {
				// System.out.println("from file: " + fileString);
	
				// trim off any whitespace:
				String[] list = fileString.trim().split(",");
				
				// System.out.println("from file list: " + fileString);
	
				if (list.length == 10) {
					tmp.add(Integer.parseInt(list[0]));
				}// end
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int[] re = new int[ tmp.size() ];
		for (int i = 0; i < tmp.size(); i++)
			re[i] = tmp.get(i);
		return re;
	}
	
	public int[][] read_raw_data() {
		if (status != STATUS_STAND_BY)
			throw new RuntimeException("Attemp to read file in non-standby mode");
		
		ArrayList< int[] > tmp = new ArrayList< int[] >();
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			String fileString;
			while ((fileString = br.readLine()) != null) {
				// System.out.println("from file: " + fileString);
	
				// trim off any whitespace:
				String[] list = fileString.trim().split(",");
				
				// System.out.println("from file list: " + fileString);
	
				if (list.length == 10) {
					int t[] = new int[9];
					for (int i = 0; i < 9; i++)
						t[i] = Integer.parseInt(list[i + 1]);
					tmp.add(t);
				}// end
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tmp.toArray(new int[ tmp.size() ][] );
	}
	
	private static final String[] STATUS_MSG = {"StandBy", "Read", "write"};
	private String get_mode_msg(int mode) {
		return STATUS_MSG[mode];
	}
}
