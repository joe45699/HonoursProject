import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class BusTutorialLinkedList {
	public static void main(String[] args) throws Exception {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection database = DriverManager.getConnection("jdbc:sqlite:");
			Statement stat = database.createStatement();			
			stat.executeUpdate("restore from OCBus.db");
			
			ResultSet r = stat.executeQuery("select sid from stop_times join stops on stop_times.stop_id=stops.stop_id where trip_id like \"%39373164%\";");				
			SQLLinkedList<Stop> BusTripStops = new SQLLinkedList<Stop>(Stop.class, database,"stops", "sid");
			LinkedList<Integer> stopTimes = new LinkedList<Integer>();
			while(r.next()){
				stopTimes.add(r.getInt(1));
			}
			BusTripStops.addAll(stopTimes);
			
			System.out.println(BusTripStops.listPrint("stop_name"));
			BusTripStops.set(38, 581);
			System.out.println("BANK / SOMERSET is stop "+BusTripStops.indexOf(766));
			System.out.println("BANK / SOMERSET is "+(BusTripStops.indexOf(766)-BusTripStops.indexOf(585))+" stops from RIDEAU / FRIEL");
			for(int i = 0; i < 70; i++){
			BusTripStops.removeFirst();
			}
			System.out.println(BusTripStops.listPrint("stop_name"));
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


