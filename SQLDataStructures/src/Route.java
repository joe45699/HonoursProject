
public class Route {
	String route_id,route_short_name,route_long_name,route_desc,route_type,route_url;
	public Route(String [] r){
		if (r.length==6){
			route_id = r[0];
			route_short_name = r[1];
			route_long_name = r[2];
			route_desc=r[3];
			route_type=r[4];
			route_url=r[5];
		}
	}
	public String toString(){
		return ""+route_id+" "+route_short_name+" "+route_long_name+" "+route_desc+" "+route_type+" "+route_url;
	}
}
