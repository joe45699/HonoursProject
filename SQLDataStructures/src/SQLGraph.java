import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class SQLGraph<T> {
	int numberOfVertices;
	int numberOfEdges;
	int vertexCounter;
	int edgeCounter;
	int tableNumber;
	String colName;
	String tableName;
	Connection theDatabase;
	Statement s;
	Class<T> c;

	public SQLGraph(Class<T> vClass, Connection aDatabase, String _tableName, String _colName) throws SQLException{
		numberOfVertices = 0;
		numberOfEdges = 0;
		vertexCounter = 0;
		edgeCounter = 0;
		c = vClass;
		theDatabase = aDatabase;
		colName = _colName;
		tableName = _tableName;
		s = theDatabase.createStatement();
		s.execute("CREATE TABLE IF NOT EXISTS GRAPHS(lID INTEGER PRIMARY KEY AUTOINCREMENT, GraphName TEXT NOT NULL);");
		s.execute("INSERT INTO GRAPHS (GraphName) VALUES ('"+c.getName()+"');");
		ResultSet a= s.executeQuery("SELECT COUNT (*) from GRAPHS;");
		tableNumber = a.getRow();
		s.execute("CREATE TABLE IF NOT EXISTS Graph"+c.getName()+""+tableNumber+"Vertices (vID INTEGER NOT NULL, fID INTEGER, "
				+ "FOREIGN KEY(fID) REFERENCES "+tableName+"("+colName+"));");
		s.execute("CREATE TABLE IF NOT EXISTS Graph"+c.getName()+""+tableNumber+"Edges (eID INTEGER NOT NULL, startID INTEGER, "
				+ "endID INTEGER, weight Integer, FOREIGN KEY(startID) REFERENCES Graph"+c.getName()+""+tableNumber+"Vertices(vID), "
				+ "FOREIGN KEY(endID) REFERENCES Graph"+c.getName()+""+tableNumber+"Vertices(vID));");
	}

	public SQLGraph(Class<T> vClass, Connection aDatabase, String _tableName, String _colName, int _tableNumber) throws SQLException{		
		c = vClass;
		theDatabase = aDatabase;
		colName = _colName;
		tableName = _tableName;
		s = theDatabase.createStatement();
		ResultSet r = s.executeQuery("select count(*), max(vid) from Graph"+c.getName()+""+tableNumber+"Vertices;");
		numberOfVertices = r.getInt(1);
		vertexCounter = (r.getInt(2))+1;
		r = s.executeQuery("select count(*), max(eid) from Graph"+c.getName()+""+tableNumber+"Edges;");
		numberOfEdges = r.getInt(1);		
		edgeCounter = (r.getInt(2))+1;
	}

	String getName(){
		return "Graph"+c.getName()+""+tableNumber;
	}

	boolean addVertex(int v) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Vertices WHERE fID = "+v+";");
		if(!r.next()){
			s.execute("INSERT INTO Graph"+c.getName()+""+tableNumber+"Vertices (vID, fID) values ("+(++vertexCounter)+", "+v+");");
			numberOfVertices++;
			return true;
		}
		return false;
	}

	boolean addAllVertext(Collection<Integer> v)throws SQLException{
		for(int q:v){
			ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Vertices WHERE fID = "+q+";");
			if(!r.next()){
				s.execute("INSERT INTO Graph"+c.getName()+""+tableNumber+"Vertices (vID, fID) values ("+(++vertexCounter)+", "+q+");");			
			}
		}
		numberOfVertices+=v.size();
		return true;
	}

	boolean addEdge(int a, int b, boolean Dubs) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+a+" AND endID = "+b+";");
		if(!r.next()){
			s.execute("INSERT INTO Graph"+c.getName()+""+tableNumber+"Edges (eID, startID, endID, weight) values ("+(++edgeCounter)+", "+a+", "+b+", 1);");
			numberOfEdges++;
			return true;
		}
		if(Dubs){
			s.execute("UPDATE Graph"+c.getName()+""+tableNumber+"Edges SET weight = "+(r.getInt(4)+1)+" WHERE startID = "+a+" AND endID = "+b+";");
		}
		return false;
	}

	boolean addEdge(int a, int b, int w, boolean Dubs) throws SQLException{	
		ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+a+" AND endID = "+b+";");
		if(!r.next()){
			s.execute("INSERT INTO Graph"+c.getName()+""+tableNumber+"Edges (eID, startID, endID, weight) values ("+(++edgeCounter)+", "+a+", "+b+", "+w+");");
			numberOfEdges++;
			return true;
		}
		if(Dubs){
			s.execute("UPDATE Graph"+c.getName()+""+tableNumber+"Edges SET weight = "+(r.getInt(4)+1)+" WHERE startID = "+a+" AND endID = "+b+";");
		}
		return false;
	}

	boolean addAllEdge(Collection<Integer>a, Collection<Integer>b, boolean Dubs) throws SQLException{
		if(a.size()!=b.size()){
			return false;
		}
		Iterator<Integer> A = a.iterator();
		Iterator<Integer> B = b.iterator();
		ResultSet r;
		LinkedList<String>sqlStrings = new LinkedList<String>();
		while(A.hasNext()){
			int q = A.next();
			int z = B.next();
			sqlStrings.add("Create temp table IF NOT EXISTS hgf (eID INTEGER NOT NULL, startID INTEGER, endID INTEGER, weight Integer);");
			r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+q+" AND endID = "+z+";");
			if(r.next()&&Dubs){
				sqlStrings.add("UPDATE Graph"+c.getName()+""+tableNumber+"Edges SET weight = "+(r.getInt(4)+1)+" WHERE startID = "+q+" AND endID = "+z+";");
			}else{

				sqlStrings.add("INSERT INTO hgf (eID, startID, endID, weight) values ("+(++edgeCounter)+", "+q+", "+z+", "+1+");");
				numberOfEdges++;
			}
		}
		for(String q :sqlStrings){
			s.addBatch(q);
		}
		s.executeBatch();
		if(Dubs){
			s.execute("Insert into Graph"+c.getName()+""+tableNumber+"Edges select eid, startid, endid, count(*) as weight from hgf group by startid, endid;");
		}
		else{
			s.execute("Insert into Graph"+c.getName()+""+tableNumber+"Edges select eid, startid, endid, weight as weight from hgf group by startid, endid;");
		}
		return true;
	}

	boolean addAllEdge(Collection<Integer>a, Collection<Integer>b, Collection<Integer>w) throws SQLException{
		if(a.size()==b.size()&&a.size()==w.size()){

			Iterator<Integer> A = a.iterator();
			Iterator<Integer> B = b.iterator();
			Iterator<Integer> W = w.iterator();
			ResultSet r;
			LinkedList<String> sqlStrings = new LinkedList<String>();
			sqlStrings.add("Create table IF NOT EXISTS ZX (eID INTEGER NOT NULL, startID INTEGER, endID INTEGER, weight Integer;");
			while(A.hasNext()){
				int z = A.next();
				int q = B.next();
				int x = W.next();
				r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+q+" AND endID = "+z+";");
				if(r.next()){
				}else{			
					sqlStrings.add("INSERT INTO ZX (eID, startID, endID, weight) values ("+(++edgeCounter)+", "+q+", "+z+", "+x+");");
					numberOfEdges++;
				}			
			}
			for(String q : sqlStrings){
				s.addBatch(q);
			}
			s.executeBatch();
			return true;
		}
		return false;
	}

	boolean removeVertex(int v) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Vertices WHERE fID = "+v+";");
		if(!r.next()){
			return false;
		}
		s.addBatch("DELETE FROM Graph"+c.getName()+""+tableNumber+"Edges Where startID = "+v+" OR endID = "+v+";");
		s.addBatch("DELETE FROM Graph"+c.getName()+""+tableNumber+"Vertices Where fID = "+v+";");
		s.executeBatch();
		r = s.executeQuery("Select count(*) from Graph"+c.getName()+""+tableNumber+"Edges;");
		numberOfEdges = r.getInt(1);
		numberOfVertices--;
		return true;		
	}

	boolean removeEdge(int a, int b) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+a+" AND endID = "+b+";");
		if(!r.next()){
			return false;
		}
		s.execute("DELETE FROM Graph"+c.getName()+""+tableNumber+"Edges where startID = "+a+" AND endID = "+b+";");
		numberOfEdges--;
		return true;
	}

	boolean adjacent(int a, int b) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+a+" AND endID = "+b+";");
		if(!r.next()){
			return false;
		}
		r= s.executeQuery("SELECT * from Graph"+c.getName()+""+tableNumber+"Edges WHERE startID = "+b+" AND endID = "+a+";");
		if(!r.next()){
			return false;
		}
		return true;
	}

	ArrayList<Integer> adjacent(int v) throws SQLException{
		ArrayList<Integer> n = new ArrayList<Integer>();
		ResultSet r = s.executeQuery("Select startID from Graph"+c.getName()+""+tableNumber+"Edges where endID = "+v+";");
		while(r.next()){
			n.add(r.getInt(1));
		}
		r = s.executeQuery("Select endID from Graph"+c.getName()+""+tableNumber+"Edges where startID = "+v+";");
		while(r.next()){
			n.add(r.getInt(1));
		}
		return n;
	}

	ArrayList<Integer> incedentEdges(int v) throws SQLException{
		ArrayList<Integer>n = new ArrayList<Integer>(); 
		ResultSet r = s.executeQuery("Select eID from Graph"+c.getName()+""+tableNumber+"Edges where startID ="+v+" OR endID = "+v+";");
		while(r.next()){
			n.add(r.getInt(1));
		}

		return n;
	}

	ArrayList<Integer> outEdges(int v) throws SQLException{
		ArrayList<Integer> n = new ArrayList<Integer>();
		ResultSet r = s.executeQuery("Select eID from Graph"+c.getName()+""+tableNumber+"Edges where startID = "+v+";");
		while(r.next()){
			n.add(r.getInt(1));
		}
		return n;
	}

	ArrayList<Integer> inEdges(int v) throws SQLException{	
		ArrayList<Integer> n = new ArrayList<Integer>();
		ResultSet r = s.executeQuery("Select eID from Graph"+c.getName()+""+tableNumber+"Edges where endID = "+v+";");
		while(r.next()){
			n.add(r.getInt(1));
		}
		return n;
	}

	String listPrint(String label) throws SQLException{
		String trt="";
		ResultSet r = s.executeQuery("select vid, fid, "+label+" from Graph"+c.getName()+""+tableNumber+"Vertices join "+tableName+" on fid="+colName+";");
		while(r.next()){
			trt+= r.getString(1)+" | ";
			trt+= r.getString(2)+" | ";
			trt+= r.getString(3);
			trt+="\n";
		}
		r = s.executeQuery("select eid, startid, endid, weight from Graph"+c.getName()+""+tableNumber+"Edges;");
		while(r.next()){
			trt+= r.getString(1)+" | ";
			trt+= r.getString(2)+" | ";
			trt+= r.getString(3)+" | ";
			trt+= r.getString(4);
			trt+="\n";
		}
		return trt;

	}
}
