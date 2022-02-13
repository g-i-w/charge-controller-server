import java.io.*;
import java.nio.file.*;
import paddle.*;
import canoedb.*;

public class SolarServer extends ServerState {

	private TemplateFile html;
	private String plotly;
	private TriStarMPPT ts;
	private ServerHTTP http;
	private Battery bat;
	private Timeline voltHist;
	private Timeline chargeHist;
	private Timeline powerHist;
	private Timeline powerMaxHist;
	
	private double c_to_f ( double c ) { return 9.0/5.0*c + 32.0; }
	
	private String dec ( double value, int places ) { return String.format("%."+places+"f",value); }

	public void respond ( InboundHTTP session ) {
		System.out.println( "path requested: "+session.request().path() );
		printConnection( session );
		if (session.path("/plotly.js")) {
			session.response().setMIME( "application/javascript" );
			session.response().setBody( plotly );
		} else if (session.path("/")) {
			double chargePercent = bat.charge()*100;
			double hoursRemaining = bat.chargeTimeIntercept(0.15)/3600.0;
			html.replace( new String[]{
				"battery_charge", dec( chargePercent, 0 ),
				"battery_charge_color", (chargePercent >= 60 ? "green" : (chargePercent >= 30 ? "yellow" : "red" )),
				"battery_life", ( hoursRemaining > 0 && hoursRemaining < 48  ? dec( hoursRemaining, 1 ) : "-" ),
				"battery_life_unit", "Hours",
				"battery_voltage", dec( ts.battery_voltage(), 1 ),
				"battery_current", dec( ts.battery_current(), 1 ),
				"battery_temp", dec( c_to_f(ts.battery_temp()), 1 ),
				"battery_voltage_data", voltHist.dataCSV(),
				"battery_voltage_time", voltHist.timeCSV(),
				"battery_charge_data", chargeHist.dataCSV(),
				"battery_charge_time", chargeHist.timeCSV(),
				"output_power", dec( ts.output_power(), 0 ),
				"output_power_unit", "W",
				"array_voltage", dec( ts.array_voltage(), 1 ),
				"array_current", dec( ts.array_current(), 1 ),
				"array_power_percent_used", dec(ts.input_power()/ts.input_power_max()*100, 0),
				"array_power_used_time", powerHist.timeCSV(),
				"array_power_used_data", powerHist.dataCSV(),
				"array_power_max_time", powerMaxHist.timeCSV(),
				"array_power_max_data", powerMaxHist.dataCSV()
			});
			session.response().setBody( html.toString() );
		}
	}

	public SolarServer ( int serverPort, String controllerAddress, String workingDirectory ) throws Exception {
		html 		= new TemplateFile( workingDirectory+"/html/solar-server.html", "////" );
		plotly 		= Files.readString( Paths.get(workingDirectory+"/html/plotly-2.8.3.min.js") );
		//System.out.println( plotly );
		ts 		= new TriStarMPPT( controllerAddress );
		//new Database( args[0] );
		http 		= new ServerHTTP( this, serverPort, this.getClass().getName()+":"+serverPort );
		bat 		= new Battery	( 5, 5*60/5 ); // 5sec, 5min
		chargeHist 	= new Timeline	( 5*60/5 ); // 5 min
		voltHist 	= new Timeline	( 5*60/5 ); // 5 min
		powerHist 	= new Timeline	( 24*60*60/5 ); // 24 hours
		powerMaxHist 	= new Timeline	( 24*60*60/5 ); // 24 hours
		while (true) {
			try {
				ts.read( 3, 0, 91 );
				Thread.sleep(5000);
				bat.sample( ts.battery_voltage()/4, ts.battery_temp() );
				voltHist.sample( bat.voltageNormalized() );
				chargeHist.sample( bat.charge() );
				powerHist.sample( ts.input_power() );
				powerMaxHist.sample( ts.input_power_max() );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main (String[] args) throws Exception {
		new SolarServer( Integer.parseInt(args[0]), args[1], args[2] );
	}

}
