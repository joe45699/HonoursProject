import java.util.ArrayList;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.shape.Line;

public class BusTutorialGraphB extends Application {
	double minLong, minLat, maxLong, maxLat, maxWeight, minWeight;
	@Override
	public void start(Stage primaryStage) {
		try {
			Group root = new Group();

			Connection database;
			Statement stat;
			Statement stat2;
			Class.forName("org.sqlite.JDBC");
			//memory database
			database = DriverManager.getConnection("jdbc:sqlite:");

			stat = database.createStatement();
			stat2 = database.createStatement();
		
			stat.executeUpdate("restore from bus.db");
			Scene scene = new Scene(root, 1000, 1000, Color.BLACK);
			SQLGraph<Stop> MyMap = new SQLGraph<Stop>(Stop.class,database, "stops", "sid", 0);
			ResultSet r = stat.executeQuery("select min(stop_lat), max(stop_lat), min(stop_lon), max(stop_lon) from stops;");
			minLong = r.getDouble(3);
			minLat =r.getDouble(1);
			maxLong = r.getDouble(4);
			maxLat = r.getDouble(2);
				
			
			Group path = new Group();
			ResultSet f = stat.executeQuery("select max(weight), min(weight) from "+MyMap.getName()+"Edges;");
			maxWeight = f.getDouble(1);
			minWeight = f.getDouble(2);
			f = stat.executeQuery("select * from "+MyMap.getName()+"Edges;");			
			while(f.next()){				
				
				r = stat2.executeQuery("select stop_lat,stop_lon from stops where sid = "+f.getInt(2)+";");
				int []xy1 = mapPoint(r.getDouble(1), r.getDouble(2));
				r = stat2.executeQuery("select stop_lat,stop_lon from stops where sid = "+f.getInt(3)+";");
				int []xy2 = mapPoint(r.getDouble(1), r.getDouble(2));
				Line l = new Line(xy1[0], xy1[1], xy2[0], xy2[1]);
				l.setStroke(Color.TURQUOISE);
				path.getChildren().add(l);
				
			}
			r = stat.executeQuery("select stop_lat,stop_lon from stops;");			
			Group sDots = new Group();
			int circleSize = 1;
			while(r.next()){
				Circle c = new Circle(circleSize, Color.RED);
				int [] pos = mapPoint(r.getDouble(1), r.getDouble(2));
				c.setLayoutX(pos[0]);
				c.setLayoutY(pos[1]);
				
				sDots.getChildren().add(c);
			}
			
			ArrayList<Integer> stopList  =MyMap.adjacent(457);
			for(int i = 0; i< 7; i++){
				ArrayList<Integer> nList = new ArrayList<Integer>();
				for(int k :stopList){
					nList.addAll(MyMap.adjacent(k));
				}
				nList.removeAll(stopList);
				stopList.addAll(nList);				
			}
			
			Group cDots = new Group();
			for(int k :stopList){
				r = stat.executeQuery("select stop_lat,stop_lon from stops where sid = "+k+";");
				Circle c = new Circle(circleSize, Color.YELLOW);
				int [] pos = mapPoint(r.getDouble(1), r.getDouble(2));
				c.setLayoutX(pos[0]);
				c.setLayoutY(pos[1]);
				cDots.getChildren().add(c);
			}
			
			r = stat.executeQuery("select stop_lat,stop_lon from stops where sid = 457;");
			Circle c = new Circle(circleSize*3, Color.PINK);
			int [] pos = mapPoint(r.getDouble(1), r.getDouble(2));
			c.setLayoutX(pos[0]);
			c.setLayoutY(pos[1]);
			cDots.getChildren().add(c);	
			root.getChildren().add(path);
			root.getChildren().add(sDots);
			root.getChildren().add(cDots);
			stat.executeUpdate("backup to bus.db");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	private int[] mapPoint(double lat, double lon){

		int Y = (int)(((-1)*Math.floor(((lat-minLat)/(maxLat-minLat))*1000))+1000);
		int X = (int)( Math.floor(((lon-minLong)/(maxLong-minLong))*1000));
		int[] xy = new int[]{X, Y};
		return xy;
	}
}

