public class LeadAcidDeepCycle12V {

	private static double chargeCutoff = 1.0; // overly simplistic divide between charging and discharging states


	// based on https://www.batteriesinaflash.com/deep-cycle-battery-faq

	private static double[] voltageDischarging = new double[]{
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
	};
	private static double[] voltageChargingNeg40C = new double[]{
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
	};
	private static double[] voltageCharging50C = new double[]{
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
	};
	private static double[] percent = new double[]{
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
	};
	
	// used for testing
	public static void printTable ( double[] table ) {
		for (int i=0; i<table.length; i++) {
			System.out.println( table[i] );
		}
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
	
	public static double[] createVoltageTable( double batteryTemp, double chargingCurrent ) {
		if (chargingCurrent < chargeCutoff) {
			return voltageDischarging;
		} else {
			double[] voltageAtTemp = new double[voltageDischarging.length];
			for (int i=0; i<voltageDischarging.length; i++) {
				voltageAtTemp[i] = interpolate( batteryTemp, -40.0, 50.0, voltageChargingNeg40C[i], voltageCharging50C[i] );
			}
			return voltageAtTemp;
		}
	}
	
	public static double chargePercent ( double batteryVoltage, double batteryTemp, double chargingCurrent ) {
		return interpolateFromTables( batteryVoltage, createVoltageTable( batteryTemp, chargingCurrent ), percent );
	}
	
	
	public static void main (String[] args) {
		System.out.println( "testing interpolate( )" );
		System.out.println( LeadAcidDeepCycle12V.interpolate( 1.4, 1, 2, 1, 3 ) );
		System.out.println( LeadAcidDeepCycle12V.interpolate( 1.4, 2, 1, 3, 1 ) );
		System.out.println( "testing interpolateFromTables( )" );
		System.out.println( LeadAcidDeepCycle12V.interpolateFromTables( 12.1, voltageDischarging, percent ) );
		System.out.println( LeadAcidDeepCycle12V.interpolateFromTables( 12.6, voltageDischarging, percent ) );
		System.out.println( "testing createVoltageTable( )" );
		LeadAcidDeepCycle12V.printTable( LeadAcidDeepCycle12V.createVoltageTable( 45.0, 0.5 ) );
		LeadAcidDeepCycle12V.printTable( LeadAcidDeepCycle12V.createVoltageTable( 45.0, 1.5 ) );
		LeadAcidDeepCycle12V.printTable( LeadAcidDeepCycle12V.createVoltageTable( 20.0, 1.5 ) );
		System.out.println( "testing chargePercent( )" );
		System.out.println( LeadAcidDeepCycle12V.chargePercent( 12.1, 45.0, 0.5 ) );
		System.out.println( LeadAcidDeepCycle12V.chargePercent( 12.1, 45.0, 1.5 ) );
		System.out.println( LeadAcidDeepCycle12V.chargePercent( 12.6, 20.0, 1.5 ) );
	}

}
