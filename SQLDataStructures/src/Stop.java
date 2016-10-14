
public class Stop {
	//The columns from the table become the data for this class
	//stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type
	//The columns stop_lat and stop_lon contain the geographical data for the stop, this will be converted to double from strings
	String stop_id,stop_code,stop_name,stop_desc, zone_id,stop_url,location_type;
	double stop_lat,stop_lon;
	public Stop(String[] r){
		if(r.length==9){
			stop_id = r[0];
			stop_code=r[1];
			stop_name=r[2];
			stop_desc=r[3];
			stop_lat=Double.valueOf(r[4]);
			stop_lon=Double.valueOf(r[5]);
			zone_id=r[6];
			stop_url=r[7];
			location_type=r[8];
		}
	}

}
