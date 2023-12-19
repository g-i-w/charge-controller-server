package bluegill;

public class InterpolatedFunction {

	private double[] x;
	private double[] y;

	public InterpolatedFunction ( double[] x, double[] y ) {
		this.x = x;
		this.y = y;
	}
	
	public static double interpolate( double xInput, double x1, double x2, double y1, double y2 ) {
		// linear: y = ax + b
		double a = (y2-y1)/(x2-x1);
		//System.out.println( "slope: "+a );
		double b = y1;
		//System.out.println( "const: "+b );
		return a*(xInput-x1) + b;
	}
	
	public static double interpolateFromTables( double xInput, double[] xTable, double[] yTable ) {
		for (int i=0; i<xTable.length; i++) {
			// tables are organized from greatest to least
			if (xInput <= xTable[i] && xInput >= xTable[i+1]) {
				return interpolate( xInput, xTable[i+1], xTable[i], yTable[i+1], yTable[i] );
			}
		}
		if (xInput > xTable[0]) {
			return yTable[0];
		} else {
			return yTable[yTable.length-1];
		}
	}
		

}
