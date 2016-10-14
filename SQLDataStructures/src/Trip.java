
public class Trip {
	//The columns from the table become the data for this class
	//route_id, service_id, trip_id, trip_headsign, direction_id, block_id
	 String route_id, service_id, trip_id, trip_headsign, direction_id, block_id;
	public Trip(String []r){
		if(r.length ==6){
			route_id=r[0];
			service_id =r[1];
			trip_id =r[2];
			trip_headsign =r[3];
			direction_id =r[4];
			block_id =r[5];
		}
	}
}
