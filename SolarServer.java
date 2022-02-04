import paddle.*;
import canoedb.*;

public class SolarServer extends ServerState {

	private TriStarMPPT ts;
	private ServerHTTP http;

	public void respond ( InboundHTTP session ) {
		session.response().setBody(
			"<h1>Battery Voltage: "+ts.battery_voltage()+"V</h1>\n"+
			"<h1>Battery Current: "+ts.battery_current()+"A</h1>\n"+
			"<h1>Output Power: "+ts.output_power()/1000+"kW</h1>\n"+
			"<h1>Array Voltage: "+ts.array_voltage()+"V</h1>\n"+
			"<h1>Array Current: "+ts.array_current()+"A</h1>\n"
		);
		printConnection( session );
	}

	public SolarServer ( int serverPort, String controllerAddress ) {
		ts = new TriStarMPPT( controllerAddress );
		//new Database( args[0] );
		http = new ServerHTTP( this, serverPort, this.getClass().getName()+":"+serverPort );
		
		while (true) {
			try {
				ts.read( 3, 0, 91 );
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main (String[] args) {
		new SolarServer( Integer.parseInt(args[0]), args[1] );
	}

}
