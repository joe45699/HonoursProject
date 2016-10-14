import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class BusTutorialDoubleLL {
	public static void main(String[] args) throws Exception {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection database = DriverManager.getConnection("jdbc:sqlite:");
			Statement stat = database.createStatement();			
			stat.executeUpdate("restore from OCBus.db");
			
			ResultSet r = stat.executeQuery("select sid from stop_times join stops on stop_times.stop_id=stops.stop_id where trip_id like \"%39413992%\";");
		
			SQLDoubleLL<Stop> BusTripStops = new SQLDoubleLL<Stop>(Stop.class, database,"stops", "sid");
			while(r.next()){
				BusTripStops.offer(r.getInt(1));
			}
			
			while(!BusTripStops.isEmpty()){
				System.out.println("SOUTH KEYS 2A is "+(BusTripStops.indexOf(3162))+" stops away");
				r =stat.executeQuery("Select stop_name from stops where sid ="+BusTripStops.poll()+";");				
				System.out.println("The current stop is: "+r.getString(1));
				System.out.println();
			}
			
			
			BusTripStops.clear();
			
			r = stat.executeQuery("select sid from stop_times join stops on stop_times.stop_id=stops.stop_id where trip_id like \"%39414414%\" order by stop_times.stop_sequence desc;");
			while(r.next()){
				BusTripStops.push(r.getInt(1));
			}
			
			while(!BusTripStops.isEmpty()){
				
				r =stat.executeQuery("Select stop_name from stops where sid ="+BusTripStops.pop()+";");
				System.out.println("The current stop is: "+r.getString(1));
			}
			
			//stat.executeUpdate("backup to bus.db");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}


