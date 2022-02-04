import java.util.*;
import paddle.*;

public class MorningStarController extends ServerState {

	private String address;
	private int port;
	private int id;
	private String name;
	private Map<Integer,Map<Integer,Integer>> registers;
	
	// NOTE: "function" is a MODBUS term, and essentially is a just memory address space or page.
	// All "function" address spaces are readable but only certain are writable
	
	public MorningStarController ( String address, int port, int id ) {
		this.address = address;
		this.port = port;
		this.id = id;
		name = this.getClass().getName()+":"+id+"@"+address+":"+port;
		registers = new HashMap<>();
	}

	public void read ( int function, int memAddress, int quantity ) {
		Bytes byteSpace = new Bytes( 4 );
		byteSpace.writeIntBE( memAddress, 0, 2 );
		byteSpace.writeIntBE( quantity, 2, 2 );
		int AHI = (int)byteSpace.read(0);
		int ALO = (int)byteSpace.read(1);
		int RHI = (int)byteSpace.read(2);
		int RLO = (int)byteSpace.read(3);
		String httpReqStr = "GET /MBCSV.cgi?ID="+id+"&F="+function+"&AHI="+AHI+"&ALO="+ALO+"&RHI="+RHI+"&RLO="+RLO+" HTTP/1.1\r\n\r\n";
		//System.out.println( "httpReqStr: "+httpReqStr );
		OutboundTCP httpRequest = new OutboundTCP (
			this,
			address,
			port,
			name,
			httpReqStr.getBytes(),
			new byte[1024],
			memAddress,
			true
		);
	}
	
	public void respond ( OutboundTCP query ) {
		String httpResponse = query.text();
		query.end();
		int httpDoubleReturn = httpResponse.indexOf("\r\n\r\n");
		int currentRegister = query.connectionId(); // using the connection ID to tag the start register
		if (httpDoubleReturn != -1) {
			String csvText = httpResponse.substring( httpDoubleReturn+4, httpResponse.length() );
			//System.out.println("csvText: "+csvText);
			String[] csvTextArray = csvText.split(",");
			int function = Integer.parseInt(csvTextArray[1]);
			if (! registers.containsKey(function)) {
				registers.put( function, new HashMap<>() );
			}
			for (int i=3; i<csvTextArray.length; i+=2) {
				int upper = Integer.parseInt(csvTextArray[i]);
				int upperShifted = upper << 8;
				int lower = Integer.parseInt(csvTextArray[i+1]);
				int combinedValue = upperShifted + lower;
				//System.out.println("upper: "+upper+", upperShifted: "+upperShifted+", lower: "+lower+", combinedValue: "+combinedValue);
				registers.get(function).put( currentRegister, combinedValue );
				currentRegister++;
			}
		}
	}
	
	public String name () {
		return name;
	}
	
	public Map<Integer,Map<Integer,Integer>> registers () {
		return registers;
	}
	
	public int register ( int function, int memAddress ) {
		if ( !registers.containsKey(function) || !registers.get(function).containsKey(memAddress) ) {
			read( function, memAddress, 1 );
			try {
				Thread.sleep(100);
				return register( function, memAddress ); // recursive
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return registers.get(function).get(memAddress);
	}
	
	public String toString () {
		return name()+" : "+registers.toString();
	}
	
	public double scaledValue ( int function, int unscaledReg, int intScaleReg, int fracScaleReg ) {
		double unscaled = (double)register( function, unscaledReg );
		double intScalar = (double)register( function, intScaleReg );
		double fracScalar = (double)register( function, fracScaleReg );
		double scalar =  intScalar + fracScalar/65536; // first + second/2^16
		System.out.println( "intScalar: "+intScalar+", fracScalar: "+fracScalar+", scalar: "+scalar );
		return scalar*unscaled/32768;
	}
	
	public static void main ( String[] args ) throws Exception {
		MorningStarController msc = new MorningStarController( args[0], 80, 1 );
		System.out.println( msc );
		msc.read( 3, 0, 30 );
		Thread.sleep(1000);
		System.out.println( msc );
		System.out.println( "Battery Voltage: "+msc.scaledValue( 3, 0x0018, 0x0000, 0x0001 ) );
		System.out.println( "Battery Terminal Voltage: "+msc.scaledValue( 3, 0x0019, 0x0000, 0x0001 ) );
		System.out.println( "Battery Current: "+msc.scaledValue( 3, 0x001C, 0x0002, 0x0003 ) );
		System.out.println( "Array Voltage: "+msc.scaledValue( 3, 0x001B, 0x0000, 0x0001 ) );
		System.out.println( "Array Current: "+msc.scaledValue( 3, 0x001D, 0x0002, 0x0003 ) );
	}
}
