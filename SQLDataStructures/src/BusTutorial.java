import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class BusTutorial {
	public static void main(String[] args) throws Exception {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection database = DriverManager.getConnection("jdbc:sqlite:");
			Statement stat = database.createStatement();			
			stat.executeUpdate("restore from OCBus.db");
			ResultSet r = stat.executeQuery("select rID from routes;");		
			SQLArrayList<Route> routes = new SQLArrayList<Route>(Route.class, database,"routes", "rID");
			LinkedList<Integer> routeNumbers = new LinkedList<Integer>();
			while(r.next()){
				routeNumbers.add(r.getInt(1));
			}
			routes.addAll(routeNumbers);
			stat.execute("INSERT INTO ROUTES VALUES(154, '301-224',301,'null','null',3,'null');");
			r= stat.executeQuery("SELECT rID from ROUTES WHERE route_short_name = 301");
			routes.add(r.getInt(1));
			routes.remove(153);
			routes.add(124, 154);			
			System.out.println(routes.listPrint("route_id, route_short_name"));
			int q = routes.get(47);
			r = stat.executeQuery("Select * from routes where rID = "+q+";");
			Route nintyfive = new Route(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)});
			System.out.println("The route nintyfive "+nintyfive);
			stat.executeUpdate("backup to bus.db");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
