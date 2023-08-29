import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timeline {

	private List<Double> data;
	private List<ZonedDateTime> timestamps;
	private int length;
	private String dateFormat; // example: "MM/dd/uu HH:mm:ss"
	
	public Timeline ( int length, String dateFormat ) {
		this.length = length;
		this.dateFormat = dateFormat;
		data = new ArrayList<>();
		timestamps = new ArrayList<>();
	}
	
	public void sample ( double sample ) {
		data.add( sample );
		timestamps.add(
			ZonedDateTime
			.now( ZoneId.systemDefault() )
		);
		if (data.size() > length) {
			data.remove(0);
			timestamps.remove(0);
		}
	}
	
	public List<Double> data () {
		return data;
	}

	public List<ZonedDateTime> time () {
		return timestamps;
	}
	
	public String dataCSV () {
		StringBuilder str = new StringBuilder();
		if (data.size()>0) str.append( data.get(0) );
		for (int i=1; i<data.size(); i++) {
			str.append( "," ).append( data.get(i) );
		}
		return str.toString();
	}

	public String timeCSV () {
		String str = "";
		for (int i=0; i<timestamps.size(); i++) {
			String timestamp = timestamps.get(i).format( DateTimeFormatter.ofPattern( dateFormat ) );
			str += ( i<timestamps.size()-1 ? "'"+timestamp+"'," : "'"+timestamp+"'" );
		}
		return str;
	}
}
