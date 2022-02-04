import paddle.*;

public class TriStarMPPT extends MorningStarController {

	public TriStarMPPT ( String address ) {
		this( address, 80, 1 );
	}

	public TriStarMPPT ( String address, int port, int id ) {
		super( address, port, id );
	}
	
	public double scaledVoltage 	( double voltage ) { return voltage * scalar( 3, 0x0000, 0x0001 ) / 32768; }
	public double scaledCurrent 	( double current ) { return current * scalar( 3, 0x0002, 0x0003 ) / 32768; }
	public double scaledPower 	( double power   ) { return power * scalar( 3, 0x0000, 0x0001 ) * scalar( 3, 0x0002, 0x0003 ) / 131072; }
	
	public double battery_voltage	() { return scaledVoltage( register( 3, 0x0018 ) ); }
	public double battery_current	() { return scaledCurrent( register( 3, 0x001C ) ); }
	public double battery_temp	() { return                register( 3, 0x0025 )  ; }
	public double array_voltage	() { return scaledVoltage( register( 3, 0x001B ) ); }
	public double array_current	() { return scaledCurrent( register( 3, 0x001D ) ); }
	public double output_power	() { return scaledPower  ( register( 3, 0x003A ) ); }
		
	
	public static void main (String[] args) throws Exception {
		TriStarMPPT ts = new TriStarMPPT( args[0] );
		ts.read( 3, 0, 91 );
		Thread.sleep(2000);
		System.out.println( ts );
		System.out.println( "battery voltage: "+ts.battery_voltage() );
		System.out.println( "battery current: "+ts.battery_current() );
		System.out.println( "battery temp: "+ts.battery_temp() );
		System.out.println( "battery charge: "+LeadAcidDeepCycle12V.chargePercent( ts.battery_voltage()/4, ts.battery_temp(), ts.battery_current() )*100 );
		System.out.println( "output power: "+ts.output_power() );
	}

}
