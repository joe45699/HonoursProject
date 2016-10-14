import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SQLDoubleLL <T> {
	int numberOfElements;
	int elementCounter;
	int tableNumber;
	String tableName;
	String colName;
	Connection theDatabase;
	Statement s;
	Class<T> c;
	int head;
	int tail;
	public SQLDoubleLL(Class<T> aClass, Connection aDatabase, String _tableName, String _colName) throws SQLException{
		numberOfElements = 0;
		elementCounter = 0;
		colName = _colName;
		tableName = _tableName;
		c = aClass;
		theDatabase = aDatabase;
		tail = 0;
		head =0;
		s = theDatabase.createStatement();
		s.execute("CREATE TABLE IF NOT EXISTS DOUBLELL(lID INTEGER PRIMARY KEY AUTOINCREMENT, DoubleLLName TEXT NOT NULL);");
		s.execute("INSERT INTO DOUBLELL (DoubleLLName) VALUES ('"+c.getName()+"');");
		ResultSet a= s.executeQuery("SELECT COUNT (*) from DOUBLELL;");
		tableNumber = a.getRow();
		s.execute("CREATE TABLE IF NOT EXISTS DoubleLL"+c.getName()+""+tableNumber+" (eID INTEGER NOT NULL, fID INTEGER, "
				+ "nID INTEGER, pID INTEGER, FOREIGN KEY(fID) REFERENCES "+tableName+"("+colName+"));");
	}

	boolean add(int e) throws SQLException{
		if(numberOfElements == 0){
			s.execute("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", null, null);");			
			head = elementCounter;
			tail = elementCounter;
			elementCounter++;
			numberOfElements++;
			return true;
		}	
		s.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", null, "+tail+");");
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET nID = "+elementCounter+" WHERE eID = "+tail+";");
		s.executeBatch();
		tail = elementCounter;
		elementCounter++;
		numberOfElements++;
		return true;
	}

	void add(int i, int e) throws SQLException{
		if(i<0||i>numberOfElements-1)throw new IndexOutOfBoundsException();
		if(i==0){
			addFirst(e);
		}
		else if(i==numberOfElements-1){
			add(e);
		}
		else{
			int eid = findI(i);
			ResultSet r = s.executeQuery("select pID from DoubleLL"+c.getName()+""+tableNumber+" where eID = "+eid+";");
			Statement s2 = theDatabase.createStatement(); 
			s2.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" values ("+elementCounter+", "+e+", "+eid+", "+r.getInt(1)+");");
			s2.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set nID = "+elementCounter+" where eID ="+r.getInt(1)+";");
			s2.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set pID = "+elementCounter+" where eID ="+eid+";");
			s2.executeBatch();
			numberOfElements++;
			elementCounter++;
		}
	}

	boolean addAll(Collection<Integer> e) throws SQLException{
		if(e.isEmpty()){return false;}
		if(numberOfElements == 0){
			head = elementCounter;
			Iterator<Integer> k = e.iterator();
			s.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+k.next()+", "+(++elementCounter)+", null);");		
			while(k.hasNext()){
				s.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+k.next()+", "+(++elementCounter)+", "+(elementCounter-2)+");");
			}
			s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET nID = null WHERE eID = "+(elementCounter-1)+";");
			s.executeBatch();
			tail = elementCounter-1;
			numberOfElements+=e.size();
			return true;
		}
		Iterator<Integer> k = e.iterator();
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET nID = "+elementCounter+" WHERE eID = "+tail+";");
		while(k.hasNext()){
			s.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+k.next()+", "+(elementCounter+1)+", "+(elementCounter-1)+");");
			elementCounter++;
		}
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET nID = null WHERE eID = "+(elementCounter-1)+";");
		s.executeBatch();
		tail = elementCounter-1;
		numberOfElements+=e.size();
		return true;
	}

	boolean addAll(int i, Collection<Integer> e) throws SQLException{
		if(i<0||i>numberOfElements)throw new IndexOutOfBoundsException();
		if(e.isEmpty()){
			return false;
		}
		if(i==numberOfElements-1){
			return addAll(e);
		}
		int eid = findI(i);	
		ResultSet r = s.executeQuery("select pID, nid from DoubleLL"+c.getName()+""+tableNumber+" where eID = "+eid+";");
		Iterator<Integer> k = e.iterator();
		int p = r.getInt(1);
		do{
			s.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" values ("+elementCounter+", "+k.next()+", "+(elementCounter+1)+", "+p+");");
			p = elementCounter;
			elementCounter++;
		}while(k.hasNext());
		if(i==0){
			s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET pID = "+(elementCounter-1)+" where eID = "+head+";");
			head = elementCounter-e.size();
			s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET pID = null where eID = "+head+";");			
		}else{
			s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET pID = "+(elementCounter-1)+" where eID = "+eid+";");
			s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET nID = "+(elementCounter-e.size())+" where nID = "+eid+";");
		}
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET nID = "+eid+" where eID = "+(elementCounter-1)+";");	
		s.executeBatch();
		numberOfElements+=e.size();
		return true;
	}

	void addFirst(int e) throws SQLException{
		if(numberOfElements == 0){
			add(e);
		}
		s.addBatch("INSERT INTO DoubleLL"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", "+head+", null);");
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET pID = "+elementCounter+" where eID="+head+";");
		s.executeBatch();
		head = elementCounter;
		elementCounter++;
		numberOfElements++;
	}

	void addLast(int e) throws SQLException{
		add(e);
	}

	void clear() throws SQLException{
		s.execute("DELETE FROM DoubleLL"+c.getName()+""+tableNumber+";");
		numberOfElements = 0;
		elementCounter = 0;		
	}

	boolean contains(int o) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from DoubleLL"+c.getName()+""+tableNumber+" WHERE fID = "+o+";");
		if(!r.next()){
			return false;
		}
		return true;
	}

	int get(int i) throws SQLException{
		if (i<0||i>numberOfElements)throw new IndexOutOfBoundsException();
		int eid = findI(i);
		ResultSet r = s.executeQuery("select fID from DoubleLL"+c.getName()+""+tableNumber+" where eID = "+eid+";");
		return r.getInt(1);
	}

	int getFirst() throws SQLException{
		ResultSet r = s.executeQuery("select fid from DoubleLL"+c.getName()+""+tableNumber+" where eID = "+head+";");
		return r.getInt(1);
	}

	int getLast() throws SQLException{
		ResultSet r = s.executeQuery("select fid from DoubleLL"+c.getName()+""+tableNumber+" where eID = "+tail+";");
		return r.getInt(1);
	}

	int indexOf(int o) throws SQLException{
		int j=0;
		ResultSet r;
		int curr =head;
		do{
			r = s.executeQuery("SELECT nID, fID from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(r.next()){
				if(o==r.getInt(2)){
					return j;
				}
				curr = r.getInt(1);
				j++;
			}
			else{
				return -1;
			}
		}while(curr!=tail);
		return -1;
	}

	int lastIndexOf(int o) throws SQLException{
		int j=numberOfElements-1;
		ResultSet r;
		int curr =tail;
		do{
			r = s.executeQuery("SELECT pID, fID from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(r.next()){
				if(o==r.getInt(2)){
					return j;
				}
				curr = r.getInt(1);
				j--;
			}
			else{
				return -1;
			}
		}while(curr!=head);
		return -1;
	}

	boolean offer(int e) throws SQLException{
		addLast(e);
		return true;
	}

	boolean offerFirst(int e) throws SQLException{
		addFirst(e);
		return true;
	}

	boolean offerLast(int e) throws SQLException{
		addLast(e);
		return true;
	}

	int peek() throws SQLException{
		return getFirst();
	}

	int peekFirst() throws SQLException{
		return getFirst();
	}

	int peekLast() throws SQLException{
		return getLast();
	}

	int poll() throws SQLException{
		return removeFirst();
	}

	int pollFirst() throws SQLException{
		return removeFirst();
	}

	int pollLast() throws SQLException{
		return removeLast();
	}

	int pop() throws SQLException{
		return removeLast();
	}

	void push(int e) throws SQLException{
		add(e);
	}

	int remove() throws SQLException{
		if(numberOfElements ==0)throw new NoSuchElementException();
		ResultSet r = s.executeQuery("SELECT pID, fID from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+tail+";");
		int a = r.getInt(1);
		int b = r.getInt(2);
		s.execute("DELETE from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+tail+";");
		s.execute("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set nID = null where eID = "+a+";");
		tail = a;
		numberOfElements--;
		return b;
	}

	int remove(int i) throws SQLException{
		if(numberOfElements ==0)throw new NoSuchElementException();
		if(i<0||i>numberOfElements-1)throw new IndexOutOfBoundsException();
		int eid = findI(i);		
		ResultSet r = s.executeQuery("SELECT nID, pID, fID from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+eid+";");
		int a = r.getInt(1);
		int b = r.getInt(2);
		int d = r.getInt(3);		
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set nID = "+a+" where nID = "+eid+";");
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set pID = "+b+" where pID = "+eid+";");
		s.addBatch("DELETE from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+eid+";");
		s.executeBatch();
		numberOfElements--;
		return d;
	}

	boolean removeObject(int o) throws SQLException
	{
		ResultSet r;
		int curr =head;
		do{
			r = s.executeQuery("SELECT nID, pID, fID  from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(o==r.getInt(3)){
				s.execute("DELETE from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
				s.execute("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set nID = "+r.getInt(1)+" where nID = "+curr+";");
				s.execute("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set pID = "+r.getInt(2)+" where pID = "+curr+";");
				numberOfElements--;
				return true;
			}
			curr = r.getInt(1);

		}while(curr!=tail);
		return false;
	}

	int removeFirst() throws SQLException{
		if(numberOfElements ==0)throw new NoSuchElementException();
		ResultSet r = s.executeQuery("SELECT nID, fID from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+head+";");
		int a = r.getInt(1);
		int b = r.getInt(2);
		s.addBatch("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set pID = null where eID = "+a+";");
		s.addBatch("DELETE from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+head+";");

		s.executeBatch();
		head = a;
		numberOfElements--;
		return b;

	}
	
	int removeLast() throws SQLException{
		return remove();
	}
	
	boolean removeFirstOccurrence(int o) throws SQLException{
		return removeObject(o);

	}

	boolean removeLastOccurrence(int o) throws SQLException{
		ResultSet r;
		int curr =tail;
		do{
			r = s.executeQuery("SELECT nID, pID, fID  from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(o==r.getInt(3)){
				s.execute("DELETE from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
				s.execute("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set nID = "+r.getInt(1)+" where nID = "+curr+";");
				s.execute("UPDATE DoubleLL"+c.getName()+""+tableNumber+" set pID = "+r.getInt(2)+" where pID = "+curr+";");
				numberOfElements--;
				return true;
			}
			curr = r.getInt(1);
		}while(curr!=head);
		return false;
	}

	int set(int i, int e) throws SQLException{
		int eid = findI(i);
		ResultSet r = s.executeQuery("select fid from DoubleLL"+c.getName()+""+tableNumber+" where eID = "+eid+";");
		int o = r.getInt(1);
		s.execute("UPDATE DoubleLL"+c.getName()+""+tableNumber+" SET fID = "+e+" where eID = "+eid+";");
		return o;
	}

	int size(){
		return numberOfElements;
	}

	boolean isEmpty(){
		return (numberOfElements==0);
	}

	private int findI(int i) throws SQLException{
		if(i<0||i>numberOfElements)throw new IndexOutOfBoundsException();
		if(i==0){
			return head;
		}
		int j;
		int curr =head;
		ResultSet r;
		for(j=0; j<i; j++){
			r = s.executeQuery("SELECT nID from DoubleLL"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			curr = r.getInt(1);			
		}
		return curr;
	}

	String listPrint(String label) throws SQLException{
		String trt="";
		ResultSet r = s.executeQuery("select eid, pid, nid, fid, "+label+" from DoubleLL"+c.getName()+""+tableNumber+" join "+tableName+" on fid="+colName);
		while(r.next()){
			trt+= r.getString(1)+" | ";
			trt+= r.getString(2)+" | ";
			trt+= r.getString(3)+" | ";
			trt+= r.getString(4)+" | ";
			trt+= r.getString(5);
			trt+="\n";
		}
		return trt;

	}

}



