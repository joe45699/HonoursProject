import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SQLLinkedList<T> {
	int numberOfElements;
	int elementCounter;
	int tableNumber;
	String colName;
	String tableName;
	Connection theDatabase;
	Statement s;
	Class<T> c;
	int head;
	int tail;
	public SQLLinkedList(Class<T> aClass, Connection aDatabase, String _tableName, String _colName) throws SQLException{
		numberOfElements = 0;
		c = aClass;
		theDatabase = aDatabase;
		colName = _colName;
		tableName = _tableName;
		tail = 0;
		head =0;
		elementCounter = 0;
		s = theDatabase.createStatement();
		s.execute("CREATE TABLE IF NOT EXISTS LINKEDLISTS(lID INTEGER PRIMARY KEY AUTOINCREMENT, LinkedListName TEXT NOT NULL);");
		s.execute("INSERT INTO LINKEDLISTS (LinkedListName) VALUES ('"+c.getName()+"');");
		ResultSet a= s.executeQuery("SELECT COUNT (*) from LINKEDLISTS;");
		tableNumber = a.getRow();
		s.execute("CREATE TABLE IF NOT EXISTS LinkedList"+c.getName()+""+tableNumber+" (eID INTEGER NOT NULL, fID INTEGER, "
				+ "nID INTEGER, FOREIGN KEY(fID) REFERENCES "+tableName+"("+colName+"));");
	}
	
	boolean add(int e)throws SQLException{
		if(numberOfElements == 0){
			s.execute("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", null);");			
			head = elementCounter;
			tail = elementCounter;
			elementCounter++;
			numberOfElements++;
			return true;
		}
		s.addBatch("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", null);");
		s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" SET nID = "+elementCounter+" WHERE eID = "+tail+";");
		s.executeBatch();
		tail = elementCounter;
		elementCounter++;
		numberOfElements++;
		return true;
	}
	
	void add(int i, int e) throws SQLException{
		if(i<0||i>numberOfElements-1) throw new IndexOutOfBoundsException();
		if(i==0){ 
			addFirst(e);
		}
		else if(i==(numberOfElements-1)) {
			add(e);
		}
		else{
			int j=0;
			int curr =head;
			s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID = "+elementCounter+" WHERE eID="+tail+";");
			ResultSet r;
			while(j!=(i-1)){
				r = s.executeQuery("SELECT nID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
				curr = r.getInt(1);
				j++;
			}
			r = s.executeQuery("SELECT nID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			j= r.getInt(1);
			if(curr==tail){
				s.addBatch("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", null);");
				s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID ="+elementCounter+" WHERE eID = "+curr+";");	
				tail = elementCounter;
			}else{
			s.addBatch("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", "+j+");");
			s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID ="+elementCounter+" WHERE eID = "+curr+";");
			} 
			s.executeBatch();
			elementCounter++;
			numberOfElements++;
		}
	}
	
	boolean addAll(Collection<Integer> e) throws SQLException{
		if(e.isEmpty()){
			return false;
		}
		Iterator<Integer> k=e.iterator();		
		if(numberOfElements == 0){
			add(k.next());
			numberOfElements-=1;		
		}
		s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID = "+elementCounter+" WHERE eID="+tail+";");
		int next;
		while(k.hasNext()){
			next = k.next();
			s.addBatch("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+next+", "+(++elementCounter)+");");
		}
		s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID = null WHERE nID="+(elementCounter)+";");
		s.executeBatch();
		tail=elementCounter-1;
		numberOfElements+=e.size();
		return true;		
	}
	
	boolean addAll(int i, Collection<Integer> e) throws SQLException{
		if(i<0||i>numberOfElements) throw new IndexOutOfBoundsException();		
		if(e.isEmpty()){
			return false;
		}
		if(i==numberOfElements){
			return addAll(e);
		}
		Iterator<Integer> k=e.iterator();
		if(elementCounter==0){
			add(k.next());
			numberOfElements-=1;
		}		
		int curr = findI(i);
		
		if(i==0){
			head = elementCounter;
		}
		int next;		
		while(k.hasNext()){
			next = k.next();
			s.addBatch("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+next+", "+(++elementCounter)+");");
		}
		s.addBatch("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID = "+curr+" WHERE nID="+(elementCounter)+";");
		s.executeBatch();
		numberOfElements+=e.size();
		return true;		
	}
	
	void addFirst(int e) throws SQLException{
		if(numberOfElements==0) {
			add(e);
		}
		s.execute("INSERT INTO LinkedList"+c.getName()+""+tableNumber+" VALUES ("+elementCounter+", "+e+", "+head+");");
		head = elementCounter;
		elementCounter++;
		numberOfElements++;
	}
	
	void addLast(int e) throws SQLException{
		add(e);
	}
	
	void clear() throws SQLException{
		s.execute("DELETE * FROM LinkedList"+c.getName()+""+tableNumber+";");
	}
	
	boolean contains(int o) throws SQLException{
		ResultSet r= s.executeQuery("SELECT * from LinkedList"+c.getName()+""+tableNumber+" WHERE fID = "+o+";");
		if(!r.next()){
			return false;
		}
		return true;
	}
	
	int get(int i) throws SQLException{
		if(i<0||i>numberOfElements) throw new IndexOutOfBoundsException();
		int j=0;
		ResultSet r;
		int curr =head;
		while(j!=i){
			r = s.executeQuery("SELECT nID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			curr = r.getInt(1);
			j++;
		}
		r = s.executeQuery("SELECT fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
		return r.getInt(1);
	}
	
	int getFirst() throws SQLException{
		ResultSet r = s.executeQuery("SELECT fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+head+";");
		return r.getInt(1);
	}
	
	int getLast() throws SQLException{
		ResultSet r = s.executeQuery("SELECT fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+tail+";");
		return r.getInt(1);
	}
	
	int indexOf(int o) throws SQLException{
		int j=-1;
		ResultSet r;
		int curr =head;
		do{
			j++;
			r = s.executeQuery("SELECT nID, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			
			if(o==r.getInt(2)){
				return j;
			}
			curr = r.getInt(1);			
		}
		while(curr!=tail);
		return -1;
	}
	
	int lastIndexOf(int o) throws SQLException{
		int j=-1;
		ResultSet r;
		int loc = -1;
		int curr =head;
		do{
			r = s.executeQuery("SELECT nID, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(o==r.getInt(2)){
				loc = j+1;
			}
			curr = r.getInt(1);
			j++;
		}
		while(curr!=tail);		
		return loc;
	}
	
	int remove() throws SQLException{
		if(numberOfElements ==0)throw new NoSuchElementException();
		ResultSet r = s.executeQuery("SELECT nID, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+head+";");
		int b = head;
		head = r.getInt(1);
		numberOfElements--;
		int a =r.getInt(2); 
		s.execute("DELETE from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+b+";");	
		return a;
	}
	
	int remove(int i) throws SQLException{
		if(i<0||i>numberOfElements) throw new IndexOutOfBoundsException();
		int j=0;
		ResultSet r;
		int curr =head;
		int p = -1;
		while(j!=(i-1)){
			r = s.executeQuery("SELECT nID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			p = curr;
			curr = r.getInt(1);
			j++;
		}
		r =s.executeQuery("SELECT nid, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");	
		int o = r.getInt(2);
		s.execute("UPDATE LinkedList"+c.getName()+""+tableNumber+" SET nID ="+r.getInt(1)+" WHERE eID ="+p+";");
		s.execute("DELETE from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
		numberOfElements--;
		return o;					
	}
	
	boolean removeObject(int o) throws SQLException
	{
		ResultSet r;
		int curr =head;
		do{
			r = s.executeQuery("SELECT nID, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(o==r.getInt(2)){
				s.execute("DELETE from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
				s.execute("UPDATE LinkedList"+c.getName()+""+tableNumber+" SET nID ="+r.getInt(1)+" WHERE nID ="+curr+";");
				numberOfElements--;
				return true;
			}
			curr = r.getInt(1);		
		}
		while(curr!=tail);
		return false;
	}
	
	int removeFirst() throws SQLException{
		return remove();
	}
	
	int removeLast() throws SQLException{
		if(numberOfElements ==0)throw new NoSuchElementException();
		ResultSet r = s.executeQuery("SELECT fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+tail+";");
		int a = r.getInt(1);
		s.execute("DELETE from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+tail+";");
		r= s.executeQuery("SELECT eID from LinkedList"+c.getName()+""+tableNumber+" WHERE nID = "+tail+";");
		tail = r.getInt(1);
		s.execute("UPDATE LinkedList"+c.getName()+""+tableNumber+" set nID = null where eID ="+tail+";");
		numberOfElements--;
		return a;
	}

	int set(int i, int e) throws SQLException{
		if(i<0||i>numberOfElements) throw new IndexOutOfBoundsException();
		int j=-1;
		ResultSet r;
		int curr =head;
		while(j!=(i-1)){
			r = s.executeQuery("SELECT nID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			curr = r.getInt(1);
			j++;
		}
		r = s.executeQuery("select fid from LinkedList"+c.getName()+""+tableNumber+" where eID = "+curr+";");
		int o = r.getInt(1);
		s.execute("UPDATE LinkedList"+c.getName()+""+tableNumber+" SET fID = "+e+" where eID = "+curr+";");
		return o;
	}
	
	int size(){
		return numberOfElements;
	}
	
	boolean isEmpty(){
		if(numberOfElements!=0){
			return false;
		}
		return true;
	}
	
	boolean removeAll(Collection<Integer> e) throws SQLException{
		Iterator<Integer> k = e.iterator();
		while(k.hasNext()){
			int a = k.next();
			if(contains(a)){
				remove(a);
			}
		}
		return true;
	}
	
	boolean retainAll(Collection<Integer> e) throws SQLException{
		int curr = head;
		ResultSet r;
		do{
			r = s.executeQuery("SELECT nID, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(!e.contains(r.getInt(2))){
				remove(curr);
			}
			curr = r.getInt(1);
		}while(curr!=tail);
		return true;
	}
	
	boolean containsAll(Collection<Integer> e) throws SQLException{
		int curr = head;
		ResultSet r;
		do{
			r = s.executeQuery("SELECT nID, fID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			if(!e.contains(r.getInt(2))){
				return false;
			}
			curr = r.getInt(1);
		}while(curr!=tail);
		return true;
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
			r = s.executeQuery("SELECT nID from LinkedList"+c.getName()+""+tableNumber+" WHERE eID = "+curr+";");
			curr = r.getInt(1);			
		}
		return curr;
	}
	
	String listPrint(String label) throws SQLException{
		String trt="";
		ResultSet r = s.executeQuery("select eid, nid, fid, "+label+" from LinkedList"+c.getName()+""+tableNumber+" join "+tableName+" on fid="+colName);
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
