import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLArrayList<T> {
	int numberOfElements;
	int tableNumber;
	Connection theDatabase;
	String tableName;
	String colName;

	Statement s;
	Class<T> c;


	public SQLArrayList(Class<T> aClass, Connection aDatabase, String _tableName, String _colName)throws SQLException{
		numberOfElements = 0;
		c = aClass;
		theDatabase = aDatabase;
		tableName = _tableName;
		colName = _colName;
		s = theDatabase.createStatement();
		s.execute("CREATE TABLE IF NOT EXISTS ARRAYLISTS(lID INTEGER PRIMARY KEY AUTOINCREMENT, ArrayListName TEXT NOT NULL);");
		s.execute("INSERT INTO ARRAYLISTS (ArrayListName) VALUES ('"+c.getName()+"');");
		ResultSet a= s.executeQuery("SELECT COUNT (*) from ARRAYLISTS;");
		tableNumber = a.getRow();
		s.execute("CREATE TABLE IF NOT EXISTS ArrayList"+c.getName()+""+tableNumber+" (eID INTEGER NOT NULL, fID INTEGER, "
				+ "PRIMARY KEY(eID), FOREIGN KEY(fID) REFERENCES "+tableName+"("+colName+"));");
		s.execute("Create Index AL"+c.getName()+""+tableNumber+" on  ArrayList"+c.getName()+""+tableNumber+"(fid);");
	}


	public int get(int index){
		try {
			ResultSet a = s.executeQuery("SELECT fID FROM ArrayList"+c.getName()+""+tableNumber+" WHERE eID = "+index+";");
			int q = a.getInt(1);
			return q;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return -1;
	}


	public int size() {
		return numberOfElements;
	}


	public int set(int i, int e){
		if(i < 0 || i> numberOfElements-1) throw new IndexOutOfBoundsException();
		try {
			ResultSet a = s.executeQuery("SELECT fID FROM ArrayList"+c.getName()+""+tableNumber+" WHERE eID = "+i+";");
			int res = a.getInt(1);
			s.execute("UPDATE ArrayList"+c.getName()+""+tableNumber+" SET fID = "+e+" WHERE eID = "+i+";");
			return res;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return -1;		
	}

	public void add(int i, int e){
		try {
			for(int j = numberOfElements; j>i; j--){
				s.addBatch("UPDATE ArrayList"+c.getName()+""+tableNumber+" SET eID = "+(j)+" WHERE eID = "+(j-1)+";");
			}
			s.addBatch("INSERT INTO ArrayList"+c.getName()+""+tableNumber+" values ("+i+", "+e+");");
			s.executeBatch();
			numberOfElements++;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void add(int e){
		try {
			s.execute("INSERT INTO ArrayList"+c.getName()+""+tableNumber+" values ("+numberOfElements+", "+e+");");
			numberOfElements++;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

	public void addAll(int index, Collection<Integer> e){
		if(index<0||index>numberOfElements) throw new IndexOutOfBoundsException();
		try {
			for(int j = numberOfElements+e.size(); j>index; j--){
				s.addBatch("UPDATE ArrayList"+c.getName()+""+tableNumber+" SET eID = "+(j)+" WHERE eID = "+(j-e.size())+";");
			}
			for(Integer eh :e){
				s.addBatch("INSERT INTO ArrayList"+c.getName()+""+tableNumber+" values ("+index+", "+eh.intValue()+");");
				index++;
			}
			s.executeBatch();
			numberOfElements+=e.size();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void addAll(Collection<Integer> e){
		try {
			for(Integer eh :e){
				s.addBatch("INSERT INTO ArrayList"+c.getName()+""+tableNumber+" values ("+numberOfElements+", "+eh.intValue()+");");
				numberOfElements++;
			}
			s.executeBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void clear() throws SQLException{
		s.execute("DELETE FROM ArrayList"+c.getName()+""+tableNumber+";");
	}

	public int indexOf(int e) throws SQLException{
		ResultSet a = s.executeQuery("SELECT eID FROM ArrayList"+c.getName()+""+tableNumber+" WHERE fID = "+e+" ORDER BY eID ASC;");
		int res = a.getInt(1);
		return res;	
	}

	public int lastIndexOf(int e) throws SQLException{
		ResultSet a = s.executeQuery("SELECT eID FROM ArrayList"+c.getName()+""+tableNumber+" WHERE fID = "+e+" ORDER BY eID DESC;");
		int res = a.getInt(1);
		return res;	
	}

	public int remove(int index) throws SQLException{
		if(index<0||index>numberOfElements-1)throw new IndexOutOfBoundsException();
		ResultSet a = s.executeQuery("SELECT fID FROM ArrayList"+c.getName()+""+tableNumber+" WHERE eID = "+index+";");
		int res = a.getInt(1);
		s.execute("DELETE FROM ArrayList"+c.getName()+""+tableNumber+" WHERE eID = "+index+";");
		for(int i = index; i<numberOfElements; i++){
			s.addBatch("UPDATE ArrayList"+c.getName()+""+tableNumber+" SET eID = "+(i)+" WHERE eID = "+(i+1)+";");
		}
		s.executeBatch();
		numberOfElements--;
		return res;
	}

	public void removeRange(int a , int b) throws SQLException{
		if(a>b||a<0||b>numberOfElements)throw new IndexOutOfBoundsException();
		s.addBatch("DELETE FROM ArrayList"+c.getName()+""+tableNumber+" WHERE eID BETWEEN "+a+" AND "+b+";");
		for(int i = b; i<numberOfElements; i++){
			s.addBatch("UPDATE ArrayList"+c.getName()+""+tableNumber+" SET eID = "+(i-(b-a)-1)+" WHERE eID = "+(i)+";");
		}
		s.executeBatch();
	}

	public List<Integer> subList(int fromIndex, int toIndex) throws SQLException{
		if(fromIndex>toIndex||fromIndex<0||toIndex>numberOfElements)throw new IndexOutOfBoundsException();
		ResultSet a = s.executeQuery("SELECT fID FROM ArrayList"+c.getName()+""+tableNumber+" WHERE eID BETWEEN "+fromIndex+" AND "+toIndex+";");
		List<Integer> q = new ArrayList<Integer>();
		while(!a.isAfterLast())
		{
			a.next();
			q.add(a.getInt(1));
		}
		return q;
	}

	String listPrint(String label) throws SQLException{
		String trt="";
		ResultSet r = s.executeQuery("select eid, fid, "+label+" from ArrayList"+c.getName()+""+tableNumber+" join "+tableName+" on fid="+colName+";");
		while(r.next()){
			trt+= r.getString(1)+" | ";
			trt+= r.getString(2)+" | ";
			trt+= r.getString(3);
			trt+="\n";
		}
		return trt;

	}

}
