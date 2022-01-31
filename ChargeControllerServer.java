import canoedb.*;
import paddle.*;

public class ChargeControllerServer extends ServerState {

	private String databaseDirectory;
	
	public ChargeControllerServer ( String databaseDirectory ) {
		this.databaseDirectory = databaseDirectory;
	}

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
		Database database = new Database( databaseDirectory );
		ServerHTTP webUI = new ServerHTTP( this, 8080, "Charge Controller Server - Web UI" );
		
		while(1) {
			new OutboundTCP(             );
		
	}
}
