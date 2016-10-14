
public class Stop_Time {
	//The columns from the table become the data for this class
	//trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
	String trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type;
	public Stop_Time(String []r){
		if(r.length ==7){
			trip_id = r[0];
			arrival_time=r[1];
			departure_time = r[2];
			stop_id = r[3];
			stop_sequence = r[4];
			pickup_type = r[5];
			drop_off_type =r[6];
		}
	}
}
