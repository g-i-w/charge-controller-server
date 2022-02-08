import java.io.*;
import java.nio.file.*;
import paddle.*;
import canoedb.*;

public class SolarServer extends ServerState {

	private TemplateFile html;
	private String plotly;
	private TriStarMPPT ts;
	private ServerHTTP http;
	private LeadAcidDeepCycle12V bat;
	
	private double c_to_f ( double c ) { return 9.0/5.0*c + 32.0; }
	
	private String dec ( double value, int places ) { return String.format("%."+places+"f",value); }

	public void respond ( InboundHTTP session ) {
		printConnection( session );
		if (session.path().indexOf("/plotly") {
			session.response().setBody( plotly );
		} else {
			double batteryCharge = bat.chargePercent( ts.battery_voltage()/4, ts.battery_temp(), ts.battery_current() )*100;
			html.replace( new String[]{
				"battery_charge", dec( batteryCharge, 0 ),
				"battery_charge_color", (batteryCharge >= 60 ? "green" : (batteryCharge >= 30 ? "yellow" : "red" )),
				"battery_voltage", dec( ts.battery_voltage(), 1 ),
				"battery_current", dec( ts.battery_current(), 1 ),
				"battery_temp", dec( c_to_f(ts.battery_temp()), 1 ),
				"output_power", dec( ts.output_power(), 0 ),
				"output_power_unit", "W",
				"array_voltage", dec( ts.array_voltage(), 1 ),
				"array_current", dec( ts.array_current(), 1 )
			});
			session.response().setBody( html.toString() );
		}
	}

	public SolarServer ( int serverPort, String controllerAddress, String workingDirectory ) {
		html = new TemplateFile( workingDirectory+"/html/solar-server.html", "////" );
		plotly = Files.readString( workingDirectory+"/html/plotly-2.8.3.min.js" );
		ts = new TriStarMPPT( controllerAddress );
		//new Database( args[0] );
		http = new ServerHTTP( this, serverPort, this.getClass().getName()+":"+serverPort );
		bat = new LeadAcidDeepCycle12V();
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
		new SolarServer( Integer.parseInt(args[0]), args[1], args[2] );
	}

}
