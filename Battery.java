import bluegill.*;

public class Battery {

	private FIRFilter voltageFilter;
	private double currentVoltage;
	private double previousVoltage;
	
	private FIRFilter chargeFilter;
	private double currentCharge;
	private double previousCharge;
	
	long currentTime;
	long previousTime;

	private double[] voltageDischarging;
	private double[] voltageChargingNeg40C;
	private double[] voltageCharging50C;
	private double[] percent;
	
	public Battery ( int samplePeriod, int cutoffFreqPeriod ) {
		this(
			samplePeriod,
			cutoffFreqPeriod,
			// lead acid deep cycle
			new double[]{
				12.70,
				12.50,
				12.42,
				12.32,
				12.20,
				12.06,
				11.90,
				11.75,
				11.58,
				11.31,
				10.50
			},
			new double[]{
				16.44,
				15.87,
				15.41,
				14.94,
				14.44,
				13.93,
				13.40,
				12.87,
				12.33,
				11.68,
				10.50
			},
			new double[]{
				13.80,
				13.49,
				13.30,
				13.09,
				12.86,
				12.61,
				12.34,
				12.08,
				11.80,
				11.42,
				10.50
			},
			new double[]{
				1.0,
				0.9,
				0.8,
				0.7,
				0.6,
				0.5,
				0.4,
				0.3,
				0.2,
				0.1,
				0.0
			}
		);
	}
	
	public Battery ( int samplePeriod, int cutoffFreqPeriod, double[] voltageDischarging, double[] voltageChargingNeg40C, double[] voltageCharging50C, double[] percent ) {
		this.voltageDischarging = voltageDischarging;
		this.voltageChargingNeg40C = voltageChargingNeg40C;
		this.voltageCharging50C = voltageCharging50C;
		this.percent = percent;
		currentVoltage = 0.0;
		previousVoltage = 0.0;
		voltageFilter = new FIRFilter(cutoffFreqPeriod/(2*samplePeriod));
		chargeFilter = new FIRFilter(cutoffFreqPeriod/(2*samplePeriod));
	}
	
	public void sample ( double v, double temp ) {
		previousTime = currentTime;
		currentTime = System.currentTimeMillis();
		previousVoltage = currentVoltage;
		currentVoltage = voltageFilter.sample( v );
		previousCharge = currentCharge;
		currentCharge = chargeFilter.sample( chargePercent( v, temp ) );
	}
	
	public double voltage () {
		return currentVoltage;
	}
	
	public double voltageNormalized () {
		return currentVoltage/voltageDischarging[0];
	}
	
	public double voltageSlope () {
		return (currentVoltage-previousVoltage)/((double)(currentTime-previousTime)/1000.0);
	}
	
	public double charge () {
		return currentCharge;
	}
	
	public double chargeSlope () {
		return (currentVoltage-previousVoltage)/((double)(currentTime-previousTime)/1000.0);
	}
	
	public double chargeTimeIntercept ( double chargeFinal ) {
		return (chargeFinal-currentCharge)*(currentTime-previousTime)/(currentCharge-previousCharge)+currentTime;
	}
	
	// used for testing
	public static void printTable ( double[] table ) {
		for (int i=0; i<table.length; i++) {
			System.out.println( table[i] );
		}
	}
	
	public double interpolate( double xInput, double x1, double x2, double y1, double y2 ) {
		// linear: y = ax + b
		double a = (y2-y1)/(x2-x1);
		//System.out.println( "slope: "+a );
		double b = y1;
		//System.out.println( "const: "+b );
		return a*(xInput-x1) + b;
	}
	
	public double interpolateFromTables( double xInput, double[] xTable, double[] yTable ) {
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
	
	public double[] createVoltageTable( double batteryTemp ) {
		if (voltageSlope() <= 0) {
			return voltageDischarging;
		} else {
			double[] voltageAtTemp = new double[voltageDischarging.length];
			for (int i=0; i<voltageDischarging.length; i++) {
				voltageAtTemp[i] = interpolate( batteryTemp, -40.0, 50.0, voltageChargingNeg40C[i], voltageCharging50C[i] );
			}
			return voltageAtTemp;
		}
	}
	
	public double chargePercent ( double batteryVoltage, double batteryTemp ) {
		return interpolateFromTables( batteryVoltage, createVoltageTable( batteryTemp ), percent );
	}
	
}
