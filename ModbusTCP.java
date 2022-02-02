import paddle.*;

public class ModbusTCP {

	private ServerState state;

	// TCP settings
	private String address;
	private int por;
	
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
	
	
	private Bytes modbusBytes ( int transactionIdentifier, int functionCode, int address, boolean write, int data ) {
		// determine length of PDU
		int pduLength = 3; // functionCode (1 byte) + address (2 bytes)
		if (write) pduLength = 5; // functionCode (1 byte) + address (2 bytes) + new_data (2 bytes)
		// populate header fields
		Bytes bytes = new Bytes( 6 + unitIdentifierLength + pduLength );
		bytes.writeIntBE( transactionIdentifier, 0, 2 ); //					transactionIdentifier:	|**|__|__|_|_|__|..:
		bytes.writeIntBE( protocolIdentifier, 2, 2 ); //					protocolIdentifier:	|__|**|__|_|_|__|..:
		bytes.writeIntBE( ( unitIdentifierLength + pduBytes.length ), 4, 2 ); //		length:			|__|__|**|_|_|__|..:
		bytes.writeIntBE( unitIdentifier, 6, 1 ); //						unitIdentifier:		|__|__|__|*|_|__|..:
		bytes.writeIntBE( functionCode, ( 6 + unitIdentifierLength ), 1 ); //			functionCode:		|__|__|__|_|*|__|..:
		bytes.writeIntBE( address, ( 6 + unitIdentifierLength + 1 ) , 2 ); //			address:		|__|__|__|_|_|**|..:
		if (write) bytes.writeIntBE( address, ( 6 + unitIdentifierLength + 1 + 2 ) , 2 ); //	new_data (write):	|__|__|__|_|_|__|**:
		return bytes
	}
	// Note: the "function code" is really more like a memory space or page identifier.
	// Essentially MODBUS [traditionally] has several "memory spaces", some of which are read-only
	
	public byte[] read ( int transactionIdentifier int functionCode, int address ) {
		Bytes outbound = modbusBytes( transactionIdentifier, functionCode, address, false, 0 );
		return
			( new OutboundTCP(state, address, port, outbound.bytes()) )
			.receive()
			.data();		
	}
	
	public byte[] write ( int functionCode, int address, int data ) {
		Bytes outbound = modbusBytes( transactionIdentifier, functionCode, address, true, data );
		return
			( new OutboundTCP(state, address, port, outbound.bytes()) )
			.receive()
			.data();		
	}
	
	
	public int port () { return port; }
	
	public void port ( int p ) { port = p; }
	
	
	public int unitIdentifier () { return unitIdentifier; }
	
	public void unitIdentifier ( int u ) { unitIdentifier = u; }
}
