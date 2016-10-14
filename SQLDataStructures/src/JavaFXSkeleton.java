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

public class JavaFXSkeleton extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Group root = new Group();
			Connection database;
			Statement stat;
			Statement stat2;
			Class.forName("org.sqlite.JDBC");
			database = DriverManager.getConnection("jdbc:sqlite:");
			stat = database.createStatement();
			stat2 = database.createStatement();		
			stat.executeUpdate("restore from bus.db");
			Scene scene = new Scene(root, 1000, 1000, Color.BLACK);			
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
}

