import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class BusTutorialBSTree {
	public static void main(String[] args) throws Exception {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection database = DriverManager.getConnection("jdbc:sqlite:");

			Statement stat = database.createStatement();
			
			stat.executeUpdate("restore from OCBus.db");
			ResultSet r = stat.executeQuery("select stop_code from stops;");
			
			
			SQLBST<Stop> BusStops = new SQLBST<Stop>(Stop.class, database,"stops", "stop_code");
			while(r.next()){
				BusStops.insert(r.getInt(1));
			}
			
			System.out.println("Tree Search 8791: "+BusStops.treeSearch(27454));
			
			System.out.println("Tree height: "+BusStops.height(BusStops.root));
			
			System.out.println("Tree minimum: "+BusStops.minimum(BusStops.root));
			System.out.println("Tree maximum: "+BusStops.maximum(BusStops.root));
			
			BusStops.delete(4627);
			System.out.println(BusStops.listPrint("stop_name"));
			
			
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


