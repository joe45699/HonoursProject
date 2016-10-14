import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLBST <T>{
	int numberOfElements;
	int tableNumber;
	Connection theDatabase;
	String tableName;
	String colName;
	Statement s;
	Class<T> c;
	int root =0;
	public SQLBST(Class<T> aClass, Connection aDatabase, String _tableName, String _colName)throws SQLException{
		numberOfElements = 0;
		c = aClass;
		theDatabase = aDatabase;
		tableName = _tableName;
		colName = _colName;
		s = theDatabase.createStatement();
		s.execute("CREATE TABLE IF NOT EXISTS BST(lID INTEGER PRIMARY KEY AUTOINCREMENT, BSTName TEXT NOT NULL);");
		s.execute("INSERT INTO BST (BSTName) VALUES ('"+c.getName()+"');");
		ResultSet a= s.executeQuery("SELECT COUNT (*) from BST;");
		tableNumber = a.getRow();
		s.execute("CREATE TABLE IF NOT EXISTS BST"+c.getName()+""+tableNumber+" (fID INTEGER,parent INTEGER, "
				+ "lChild INTEGER, rChild INTEGER, FOREIGN KEY(fID) REFERENCES "+tableName+"("+colName+"));");
	}
	
	ArrayList<Integer> treeWalk() throws SQLException{
		ArrayList<Integer> trt = new ArrayList<Integer>();
		int u =root; int prev =0; int next;
		while(u!=0){
			ResultSet r = s.executeQuery("Select parent, lChild, rChild, fid from BST"+c.getName()+""+tableNumber+" where fid ="+u+";");
			int parent = r.getInt(1);
			int lChild = r.getInt(2);
			int rChild = r.getInt(3);
			int fid = r.getInt(4);
			if (prev == parent) {
				if (lChild != 0) next = lChild;
				else if (rChild != 0) next = rChild;
				else next = parent;
			} 
			else if (prev == lChild) {
				if (rChild != 0) next = rChild;
				else next = parent;
			} 
			else {
				next = parent;
				trt.add(fid);
			}
			prev = u;
			u = next;
		}
		return trt;

	}
	
	int treeSearch(int k) throws SQLException{
		int cNode = root;
		ResultSet r = s.executeQuery("Select fid from BST"+c.getName()+""+tableNumber+" where fid = "+root+";");
		int fid = r.getInt(1);
		while(cNode!=0 && fid !=k){
			if(fid < k){
				r= s.executeQuery("Select lChild, fid from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
				cNode = r.getInt(1);
				fid = r.getInt(2);
			}
			else{
				r= s.executeQuery("Select rChild, fid from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
				cNode = r.getInt(1);
				fid = r.getInt(2);
			}
		}
		return fid;
	}
	
	int minimum(int q) throws SQLException{
		int cNode =q;
		ResultSet r = s.executeQuery("Select lChild from BST"+c.getName()+""+tableNumber+" where fid ="+cNode+";");
		int lChild = r.getInt(1);
		while(lChild !=0){
			cNode = lChild;
			r= s.executeQuery("Select lChild from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
			lChild = r.getInt(1);
		}
		return cNode;
	}
	
	int maximum(int q) throws SQLException{
		int cNode =q;
		ResultSet r = s.executeQuery("Select rChild from BST"+c.getName()+""+tableNumber+" where fid ="+cNode+";");
		int rChild = r.getInt(1);
		while(rChild !=0){
			cNode = rChild;
			r= s.executeQuery("Select rChild from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
			rChild = r.getInt(1);
		}
		return cNode;
	}
	
	int size() throws SQLException{
		ResultSet r = s.executeQuery("SELECT COUNT (*) from BST"+c.getName()+""+tableNumber+";");
		return r.getInt(1);
	}
	
	int height(int q) throws SQLException{
		if (q==0){
			return -1;
		}
		ResultSet r = s.executeQuery("SELECT lChild, rChild from BST"+c.getName()+""+tableNumber+" where fid = "+q+";");
		int l = r.getInt(1);
		int w = r.getInt(2);
		return 1+ java.lang.Math.max(height(l),height(w));
	}
	
	void insert(int q) throws SQLException{
		int pNode=0;
		int cNode=root;
		ResultSet r;
		if(root==0){
			root = q;
			s.execute("Insert into BST"+c.getName()+""+tableNumber+" values ("+q+", 0, 0, 0);");

		}
		else{
			while (cNode!=0){
				pNode = cNode;
				if(q==cNode){
					return;
				}
				else if(q<cNode){
					r = s.executeQuery("Select lChild from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
					cNode = r.getInt(1);
				}
				else{
					r = s.executeQuery("Select rChild from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
					cNode = r.getInt(1);
				}				
			}
			if(q<pNode){
				s.addBatch("Insert into BST"+c.getName()+""+tableNumber+" values ("+q+", "+pNode+", 0, 0);");
				s.addBatch("UPDATE BST"+c.getName()+""+tableNumber+" set lChild = "+q+" where fid = "+pNode+";");
				s.executeBatch();
			}
			else{
				s.addBatch("Insert into BST"+c.getName()+""+tableNumber+" values ("+q+", "+pNode+", 0, 0);");
				s.addBatch("UPDATE BST"+c.getName()+""+tableNumber+" set rChild = "+q+" where fid = "+pNode+";");
				s.executeBatch();
			}
		}
		numberOfElements++;
	}


	void transplant(int q, int w) throws SQLException{
		int cNode = w;
		if(root == q){	
			root = w;
		}
		else{
			ResultSet r = s.executeQuery("select fid from BST"+c.getName()+""+tableNumber+" where lChild = "+q+";");
			if(r.next()){
				cNode = r.getInt(1);
				s.addBatch("UPDATE BST"+c.getName()+""+tableNumber+" set lChild = "+w+" where lChild = "+q+";");
			}
			else{
				r = s.executeQuery("select fid from BST"+c.getName()+""+tableNumber+" where rChild = "+q+";");
				cNode = r.getInt(1);
				s.addBatch("UPDATE BST"+c.getName()+""+tableNumber+" set rChild = "+w+" where rChild = "+q+";");
			}
			if(w!=0){
				s.addBatch("UPDATE BST"+c.getName()+""+tableNumber+" set parent = "+cNode+" where fid = "+w+";");
			}
			s.executeBatch();
		}

	}
	
	void delete(int q) throws SQLException{
		ResultSet r = s.executeQuery("select lChild, rChild from BST"+c.getName()+""+tableNumber+" where fid = "+q+";"); 
		int lChild=  r.getInt(1);
		int rChild = r.getInt(2);
		if(lChild == 0){
			transplant(q, rChild);
		}
		else if(rChild == 0){
			transplant(q, lChild);
		}
		else{
			int cNode = minimum(rChild);
			r = s.executeQuery("select parent, rChild from BST"+c.getName()+""+tableNumber+" where fid = "+cNode+";");
			if(r.getInt(1)==q){
				int f =  r.getInt(2);
				transplant(cNode, f);
				s.execute("UPDATE BST"+c.getName()+""+tableNumber+" set rChild = "+f+" where fid = "+cNode+";");
				s.execute("UPDATE BST"+c.getName()+""+tableNumber+" set parent = "+cNode+" where fid = "+f+";");
			}
			transplant(q, cNode);
			s.execute("UPDATE BST"+c.getName()+""+tableNumber+" set lChild = "+lChild+" where fid = "+cNode+";");
			s.execute("UPDATE BST"+c.getName()+""+tableNumber+" set parent = "+cNode+" where fid = "+lChild+";");
		}
		s.execute("DELETE from BST"+c.getName()+""+tableNumber+" where fid = "+q+";");
	}
	
	String listPrint(String label) throws SQLException{
		String trt="";
		ResultSet r = s.executeQuery("select fid, parent, lChild, rChild, "+label+" from BST"+c.getName()+""+tableNumber+" join "+tableName+" on fid="+colName);
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
	public String getName() {
		return "BST"+c.getName()+""+tableNumber;
	}
}
