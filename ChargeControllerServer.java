import canoedb.*;
import paddle.*;

public class ChargeControllerServer ( String databaseDirectory ) extends ServerState {

	public void respond ( InboundHTTP session ) {
		count++;
		session.response().setBody(
			"<h1>HTTP works!</h1>\n<br>\n"+
			"path: "+session.request().path()+"\n<br>\n"+
			"body: "+session.request().body()+"\n<br>"
		);
		printConnection( session );
	}
	
	public static main (String[] args) {
		Database chargeControllerData = new Database( databaseDirectory );
		ServerHTTP webUI = new ServerHTTP( this, 8080, "Charge Controller Server - Web UI" );
		
		
		new OutboundTCP();
		
	}
}
