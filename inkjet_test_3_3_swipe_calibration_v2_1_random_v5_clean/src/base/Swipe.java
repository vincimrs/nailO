package base;

// Since these constants are so universal, let's make such kind of class.
public class Swipe {
	public static final int NO = -1;
	public static final int RIGHT = 0;
	public static final int LEFT = 1;
	public static final int TOP = 2;
	public static final int DOWN = 3;
	public static final int PRESS = 4;
	
	public static String get_text(int dir) {
		if (dir == NO)      return "x";
		if (dir == RIGHT)	return "---->";	
		if (dir == LEFT)    return "<----";
		if (dir == TOP)     return "^";
		if (dir == DOWN)    return "V";
		if (dir == PRESS)   return "-";
		return "?";
	}
}
