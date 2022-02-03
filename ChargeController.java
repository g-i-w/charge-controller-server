import paddle.*;

public class ChargeController extends ServerState {

	private String address;
	private int port;
	private int id;
	
	public ChargeController ( String address ) {
		this( address, 80, 1 );
	}
	
	public ChargeController ( String address, int port, int id ) {
		this.address = address;
		this.port = port;
		this.id = id;
	}
	
	public String readCSV ( int function, int register, int quantity ) {
		Bytes byteSpace = new Bytes( 4 );
		byteSpace.writeIntBE( register, 0, 2 );
		byteSpace.writeIntBE( quantity, 2, 2 );
		int AHI = (int)byteSpace.read(0);
		int ALO = (int)byteSpace.read(1);
		int RHI = (int)byteSpace.read(2);
		int RLO = (int)byteSpace.read(3);
		String httpRequest = "GET /MBCSV.cgi?ID="+id+"&F="+function+"&AHI="+AHI+"&ALO="+ALO+"&RHI="+RHI+"&RLO="+RLO+" HTTP/1.1\r\n\r\n";
		System.out.println( "HTTP Request: "+httpRequest );
		return (new OutboundTCP(
			this,
			address,
			port,
			"HTTP Request",
			httpRequest.getBytes(),
			new byte[1024],
			-1,
			true
		))
		//.timeout(500)
		//.capture()
		.receive()
		.text();
	}
	
	public static void main ( String[] args ) {
		ChargeController cc = new ChargeController( args[0] );
		System.out.println(
			cc.readCSV(
				Integer.parseInt( args[1], 16 ),
				Integer.parseInt( args[2], 16 ),
				Integer.parseInt( args[3] )
			)
		);
	}

}
