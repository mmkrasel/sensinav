package edu.ewubd.cse.sensinav.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MapDB extends SQLiteOpenHelper {

	public MapDB(Context context) {
		super(context, "MapDB.db", null, 2);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("DB@OnCreate");
		String sql = "CREATE TABLE nodes_info(id INTEGER PRIMARY KEY, building TEXT, label TEXT, x REAL, y REAL, z REAL)";
		db.execSQL(sql);
		sql = "CREATE TABLE edges_info(node1 INTEGER, node2 INTEGER, distance REAL, PRIMARY KEY(node1,node2))";
		db.execSQL(sql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("Write code to modify database schema here");
		// db.execSQL("ALTER table my_table  ......");
		db.execSQL("DROP TABLE nodes_info");
		db.execSQL("DROP TABLE edges_info");
		System.out.println("DB@OnCreate");
		String sql = "CREATE TABLE nodes_info(id INTEGER PRIMARY KEY, building TEXT, label TEXT, x REAL, y REAL, z REAL)";
		db.execSQL(sql);
		sql = "CREATE TABLE edges_info(node1 INTEGER, node2 INTEGER, distance REAL, PRIMARY KEY(node1,node2))";
		db.execSQL(sql);
	}
	public void truncate(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM nodes_info");
		db.execSQL("DELETE FROM edges_info");
		db.close();
	}
	public void insertNode(int id, String building, String label, double x, double y, double z) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cols = new ContentValues();
		cols.put("id", id);
		cols.put("building", building);
		cols.put("label", label);
		cols.put("x", x);
		cols.put("y", y);
		cols.put("z", z);
		db.insert("nodes_info", null ,  cols);
		db.close();
	}
	public void updateNode(int id, String building, String label, double x, double y, double z) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cols = new ContentValues();
		cols.put("id", id);
		cols.put("building", building);
		cols.put("label", label);
		cols.put("x", x);
		cols.put("y", y);
		cols.put("z", z);
  		db.update("nodes_info", cols, "id=?", new String[ ] {""+id} );
		db.close();
	}
	public void deleteNode(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
  		db.delete("nodes_info", "id=?", new String[ ] {""+id} );
		db.close();
	}
	public void insertEdge(int node1, int node2, double distance) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cols = new ContentValues();
		cols.put("node1", node1);
		cols.put("node2", node2);
		cols.put("distance", distance);
		db.insert("edges_info", null ,  cols);
		db.close();
	}
	public void updateEdge(int node1, int node2, double distance) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cols = new ContentValues();
		cols.put("node1", node1);
		cols.put("node2", node2);
		cols.put("distance", distance);
		db.update("edges_info", cols, "node1=?,node2=?", new String[] {""+node1,""+node2} );
		db.close();
	}
	public void deleteEdge(int node1, int node2, double distance) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("edges_info", "node1=?,node2=?", new String[] {""+node1,""+node2} );
		db.close();
	}
	public Cursor getAllNodes() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res=null;
		try {
			res = db.rawQuery("SELECT * FROM nodes_info", null);
		} catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}
	public Cursor getAllEdges() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res=null;
		try {
			res = db.rawQuery("SELECT * FROM edges_info", null);
		} catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}
}