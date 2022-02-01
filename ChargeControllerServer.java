import canoedb.*;
import paddle.*;

public class ChargeControllerServer extends ServerState {

	private String databaseDirectory;
	private String chargeControllerAddress;
	
	private byte[] modbusRead ( byte[] outboundBytes ) {
		return (new OutboundTCP(
			this,
			chargeControllerAddress,
			502,
			outboundBytes
		).receive().data();
	}
	
	public ChargeControllerServer ( String databaseDirectory, String chargeControllerAddress ) {
		this.databaseDirectory = databaseDirectory;
		this.chargeControllerAddress = chargeControllerAddress;
	}

	public void respond ( InboundHTTP session ) {
		byte[] scaleFactorBytes = modbusRead( byte[]{ 0x00, 0x00, 0x00, 0x00 }] );
		byte[] batteryVoltageBytes = modbusRead( byte[]{ 0x00, 0x00, 0x00, 0x00 }] );		
		session.response().setBody(
			"<h1>Battery: </h1>\n<br>\n"+
			"path: "+session.request().path()+"\n<br>\n"+
			"body: "+session.request().body()+"\n<br>"
		);
		printConnection( session );
	}
	
	public static main (String[] args) {
		//Database database = new Database( databaseDirectory );
		ServerHTTP webUI = new ServerHTTP( this, 8080, "Charge Controller Server - Web UI" );
		
		//while(1) {
		//	new OutboundTCP(             );
		//
	}
}
