import paddle.*;

public class ModbusTCP {

	private ServerState state;

	// TCP settings
	private String address;
	private int port;
	
	// Modbus Application Protocol (MBAP) header values
	private int transactionIdentifier;
	private static final int protocolIdentifier = 0;
	// length (unitIdentifierLength + PDU_length) will be determined for each request
	private int unitIdentifier;
	private int unitIdentifierLength;
		
	// PDU format:
	// 1 byte: function code
	// 2 bytes: address or data
	// 2 bytes (only for write): data to be written

	public ModbusTCP ( ServerState state, String address ) {
		this( state, address, 502, 1 );
	}
	
	public ModbusTCP ( ServerState state, String address, int port, int unitIdentifier ) {
		// TCP
		this.state = state;
		this.address = address;
		this.port = port; // default is 502
		
		// Modbus
		this.unitIdentifier = unitIdentifier;
		this.unitIdentifierLength = 1; // default
	}
	
	
	private Bytes modbusBytes ( int transactionIdentifier, int functionCode, int memAddress, boolean write, int memData ) {
		// determine length of PDU
		int pduLength = 3; // functionCode (1 byte) + address (2 bytes)
		if (write) pduLength = 5; // functionCode (1 byte) + address (2 bytes) + new_data (2 bytes)
		// populate header fields
		Bytes bytes = new Bytes( 6 + unitIdentifierLength + pduLength );
		bytes.writeIntBE( transactionIdentifier, 0, 2 ); //					transactionIdentifier:	|**|__|__|_|_|__|..:
		bytes.writeIntBE( protocolIdentifier, 2, 2 ); //					protocolIdentifier:	|__|**|__|_|_|__|..:
		//bytes.writeIntBE( ( unitIdentifierLength + pduLength ), 4, 2 ); //			length:			|__|__|**|_|_|__|..:
		bytes.writeIntBE( ( pduLength ), 4, 2 ); //			length:			|__|__|**|_|_|__|..:
		bytes.writeIntBE( unitIdentifier, 6, 1 ); //						unitIdentifier:		|__|__|__|*|_|__|..:
		bytes.writeIntBE( functionCode, ( 6 + unitIdentifierLength ), 1 ); //			functionCode:		|__|__|__|_|*|__|..:
		bytes.writeIntBE( memAddress, ( 6 + unitIdentifierLength + 1 ) , 2 ); //		memAddress:		|__|__|__|_|_|**|..:
		if (write) bytes.writeIntBE( memData, ( 6 + unitIdentifierLength + 1 + 2 ) , 2 ); //	new_data (write):	|__|__|__|_|_|__|**:
		System.out.println( "ModbusTCP: sending bytes:" );
		System.out.println( bytes );
		return bytes;
	}
	// Note: the "function code" is really more like a memory space or page identifier.
	// Essentially MODBUS [traditionally] has several "memory spaces", some of which are read-only
	
	public byte[] read ( int transactionIdentifier, int functionCode, int memAddress ) {
		Bytes outbound = modbusBytes( transactionIdentifier, functionCode, memAddress, false, 0 );
		return
			( new OutboundTCP(state, address, port, outbound.bytes()) )
			.capture()
			.data();		
	}
	
	public byte[] readWait ( int transactionIdentifier, int functionCode, int memAddress, int chunks, int timeout ) {
		Bytes outbound = modbusBytes( transactionIdentifier, functionCode, memAddress, false, 0 );
		return
			( new OutboundTCP(state, address, port, outbound.bytes()) )
			.timeout(timeout)
			.receive(chunks)
			.data();		
	}
	
	public byte[] write ( int transactionIdentifier, int functionCode, int memAddress, int memData ) {
		Bytes outbound = modbusBytes( transactionIdentifier, functionCode, memAddress, true, memData );
		return
			( new OutboundTCP(state, address, port, outbound.bytes()) )
			.receive()
			.data();		
	}
	
	
	public int port () { return port; }
	
	public void port ( int p ) { port = p; }
	
	
	public int unitIdentifier () { return unitIdentifier; }
	
	public void unitIdentifier ( int u ) { unitIdentifier = u; }
	
	
	public static void main (String[] args) {
		ServerState state = new ServerStateTest();
		ModbusTCP mTCP = new ModbusTCP( state, args[0] );
		System.out.println(
			new Bytes( mTCP.read( Integer.parseInt(args[1],16), Integer.parseInt(args[2],16), Integer.parseInt(args[3],16) ) )
		);
	}
}

class ServerStateTest extends ServerState {
	public void respond ( OutboundTCP connection ) {
		System.out.println("Received:" + new Bytes( connection.data() ) );
	}
 }
