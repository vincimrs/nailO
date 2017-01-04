package base.logger;

import java.io.FileWriter;
import java.io.IOException;

public class CustomizedLogger {
	private static final int STATUS_STAND_BY = 0;
	private static final int STATUS_WRITE = 2;
	
	private int status = STATUS_STAND_BY;
	
	private String filename;
	private FileWriter fw = null;
	
	public CustomizedLogger(String _filename) {
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
	
	public void write(String msg) {
		if (status != STATUS_WRITE)
			throw new RuntimeException("Attemp to write data while not in write mode");
		
		try {
			fw.write(msg);// appends the string  to the file
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	public void writeln(String msg) {
		write(msg + '\n');
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
	
	private static final String[] STATUS_MSG = {"StandBy", "Read", "write"};
	private String get_mode_msg(int mode) {
		return STATUS_MSG[mode];
	}
}
