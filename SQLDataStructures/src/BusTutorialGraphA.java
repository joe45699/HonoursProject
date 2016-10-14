import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class BusTutorialGraphA {
	public static void main(String[] args) throws Exception {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection database = DriverManager.getConnection("jdbc:sqlite:");
			Statement stat = database.createStatement();			
			stat.executeUpdate("restore from OCBus.db");
			
			LinkedList<Integer> stopList = new LinkedList<Integer>();
			ResultSet r =  stat.executeQuery("Select sid from stops;");
			SQLGraph<Stop> MyMap = new SQLGraph<Stop>(Stop.class,database, "stops", "sid");
			while(r.next()){
				 stopList.add(r.getInt(1));
			}
			MyMap.addAllVertext(stopList);
			
			ResultSet g = stat.executeQuery("select stop_times.trip_id, stop_times.stop_sequence, stops.sid from stop_times "
					+ "join stops on stop_times.stop_id = stops.stop_id "
					+ "order by stop_times.trip_id asc, stop_times.stop_sequence asc;");
			
			int last = Integer.MAX_VALUE;
			LinkedList<Integer> stopTimes = new LinkedList<Integer>();
			LinkedList<Integer> stopTimes2 = new LinkedList<Integer>();
			while(g.next()){
				if(last > g.getInt(2)){
					if(!stopTimes.isEmpty()){
					stopTimes.removeLast();
					}
					stopTimes.add(g.getInt(3));
					
				}
				else{
					stopTimes.add(g.getInt(3));
					stopTimes2.add(g.getInt(3));
				}
				last = g.getInt(2);
			}
			stopTimes.removeLast();
			MyMap.addAllEdge(stopTimes, stopTimes2, true);
			
			stat.executeUpdate("backup to bus.db");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}


