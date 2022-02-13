import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Timeline {

	private List<Double> data;
	private List<String> timestamps;
	private int length;
	
	public Timeline ( int length ) {
		this.length = length;
		data = new ArrayList<>();
		timestamps = new ArrayList<>();
	}
	
	public void sample ( double sample ) {
		data.add( sample );
		timestamps.add(
			ZonedDateTime
			.now( ZoneId.systemDefault() )
			.format( DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ) )
		);
		if (data.size() > length) {
			data.remove(0);
			timestamps.remove(0);
		}
	}
	
	public List<Double> data () {
		return data;
	}

	public List<String> time () {
		return timestamps;
	}
	
	public String dataCSV () {
		String str = "";
		for (int i=0; i<data.size(); i++) {
			str += ( i<data.size()-1 ? data.get(i)+"," : data.get(i)+"" );
		}
		return str;
	}

	public String timeCSV () {
		String str = "";
		for (int i=0; i<timestamps.size(); i++) {
			str += ( i<timestamps.size()-1 ? "'"+timestamps.get(i)+"'," : "'"+timestamps.get(i)+"'" );
		}
		return str;
	}
}
