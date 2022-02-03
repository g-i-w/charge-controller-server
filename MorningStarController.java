import java.util.*;
import paddle.*;

public class MorningStarController extends ServerState {

	private String address;
	private int port;
	private int id;
	private Map<Integer,Integer> registers;
	
	public MorningStarController ( String address, int port, int id ) {
		this.address = address;
		this.port = port;
		this.id = id;
		registers = new HashMap<>();
	}

	public void read ( int function, int register, int quantity ) {
		Bytes byteSpace = new Bytes( 4 );
		byteSpace.writeIntBE( register, 0, 2 );
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
			this.getClass().getName()+":"+id+"@"+address+":"+port,
			httpReqStr.getBytes(),
			new byte[1024],
			register,
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
			for (int i=3; i<csvTextArray.length; i+=2) {
				int upper = Integer.parseInt(csvTextArray[i]);
				int upperShifted = upper << 8;
				int lower = Integer.parseInt(csvTextArray[i+1]);
				int combinedValue = upperShifted + lower;
				//System.out.println("upper: "+upper+", upperShifted: "+upperShifted+", lower: "+lower+", combinedValue: "+combinedValue);
				registers.put( currentRegister, combinedValue );
				currentRegister++;
			}
		}
		System.out.println( "registers: "+registers.toString() );
	}
	
	public static void main ( String[] args ) throws Exception {
		MorningStarController msc = new MorningStarController( args[0], 80, 1 );
		msc.read(
			Integer.parseInt( args[1] ),
			Integer.parseInt( args[2], 16 ),
			Integer.parseInt( args[3] )
		);
		Thread.sleep(2000);
		msc.read(
			Integer.parseInt( args[1] ),
			Integer.parseInt( args[2], 16 ),
			Integer.parseInt( args[3] )
		);
		Thread.sleep(5000);		
	}
}

