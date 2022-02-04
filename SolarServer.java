import paddle.*;

public class SolarServer extends MorningStarController {

	public void respond ( InboundHTTP session ) {
		read( 3, 0, 30 );
		session.response().setBody(
			"<h1>Battery Voltage: "+scaledValue( 3, 0x0018, 0x0000, 0x0001 )+"</h1>\n"+
			"<h1>Battery Current: "+scaledValue( 3, 0x001C, 0x0002, 0x0003 )+"</h1>\n"+
			"<h1>Array Voltage: "+scaledValue( 3, 0x001B, 0x0000, 0x0001 )+"</h1>\n"+
			"<h1>Array Current: "+scaledValue( 3, 0x001D, 0x0002, 0x0003 )+"</h1>\n"
		);
		printConnection( session );
	}

	public SolarServer ( String controllerAddress, int controllerPort, int controllerId, int serverPort ) {
		super(controllerAddress, controllerPort, controllerId);
		new ServerHTTP( this, serverPort, this.getClass().getName()+":"+serverPort );
	}
	
	public static void main (String[] args) {
		new SolarServer( args[0], 80, 1, 8080 );
	}

}
