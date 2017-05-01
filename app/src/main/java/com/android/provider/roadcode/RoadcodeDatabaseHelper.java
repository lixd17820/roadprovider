package com.android.provider.roadcode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RoadcodeDatabaseHelper extends SQLiteOpenHelper {

	private static String TAG = "RoadcodeDatabaseHelper";

	private static String DB_PATH = "";
	private SQLiteDatabase myDataBase;

	private final Context myContext;
	private int version;

	private static RoadcodeDatabaseHelper mDBConnection;

	public RoadcodeDatabaseHelper(Context context, int version) {
		super(context, Roadcode.DATABASE_NAME, null, version);
		this.myContext = context;
		this.version = version;
		DB_PATH = "/data/data/"
				+ context.getApplicationContext().getPackageName()
				+ "/databases/";
		File f = new File(DB_PATH);
		if (!f.exists())
			f.mkdirs();
	}

	/**
	 * 单例方法
	 * 
	 * @param context
	 * @return DBAdapter
	 */
	public static synchronized RoadcodeDatabaseHelper getDBAdapterInstance(
			Context context, int version) {
		if (mDBConnection == null) {
			mDBConnection = new RoadcodeDatabaseHelper(context, version);
		}
		return mDBConnection;
	}

	/**
	 * 验证数据库是否存在或版本号,拷贝文件
	 * 
	 * @throws IOException
	 */
	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (dbExist) {
			// do nothing - database already exist
		} else {
			try {
				copyDataBase();
				Log.e(TAG, "version is updated");
				String myPath = DB_PATH + Roadcode.DATABASE_NAME;
				SQLiteDatabase db = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READWRITE);
				Log.e(TAG, "version " + version);
				if (db != null && db.getVersion() < version) {
					db.setVersion(version);
					Log.e(TAG, "db.setVersion(" + version + ")");
					db.close();
				}
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	/**
	 * 验证文件是否存在,并且版本和目前版本的关系,如果不存在或版本低于目前版本则返回真
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		boolean result = false;
		int oldversion = 0;
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + Roadcode.DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			oldversion = checkDB.getVersion();
			Log.e(TAG, "old database's version is " + oldversion);
			checkDB.close();
			if (oldversion >= version) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {
		AssetManager am = myContext.getAssets();
		byte[] b = new byte[1024];
		try {
			OutputStream os = new FileOutputStream(new File(DB_PATH
					+ Roadcode.DATABASE_NAME));
			String[] files = am.list("");
			Arrays.sort(files);
			for (int j = 0; j < files.length; j++) {
				if (files[j].startsWith(Roadcode.DATABASE_NAME)) {
					InputStream in = am.open(files[j]);
					int len = -1;
					while ((len = in.read(b)) > -1) {
						os.write(b, 0, len);
					}
					in.close();
				}
			}
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the database
	 * 
	 * @throws SQLException
	 */
	public void openDataBase() throws SQLException {
		String myPath = DB_PATH + Roadcode.DATABASE_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	/**
	 * Close the database if exist
	 */
	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log
				.i("onUpgrade", "db old " + oldVersion + " newVersion "
						+ newVersion);
	}

	// ----------------------- CRUD Functions ------------------------------

	/**
	 * This function used to select the records from DB.
	 * 
	 * @param tableName
	 * @param tableColumns
	 * @param whereClase
	 * @param whereArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return A Cursor object, which is positioned before the first entry.
	 */
	public Cursor selectRecordsFromDB(String tableName, String[] tableColumns,
			String whereClase, String whereArgs[], String groupBy,
			String having, String orderBy) {
		return myDataBase.query(tableName, tableColumns, whereClase, whereArgs,
				groupBy, having, orderBy);
	}

	/**
	 * This function used to update the Record in DB.
	 * 
	 * @param tableName
	 * @param initialValues
	 * @param whereClause
	 * @param whereArgs
	 * @return 0 in case of failure otherwise return no of row(s) are updated
	 */
	public int updateRecordsInDB(String tableName, ContentValues initialValues,
			String whereClause, String whereArgs[]) {
		return myDataBase.update(tableName, initialValues, whereClause,
				whereArgs);
	}

	/**
	 * 加入或更新记录
	 * 
	 * @param tableName
	 * @param initialValues
	 * @return
	 */
	public long replaceRecordsInDB(String tableName, ContentValues initialValues) {
		return myDataBase.replace(tableName, null, initialValues);
	}

	/**
	 * This function used to delete the Record in DB.
	 * 
	 * @param tableName
	 * @param whereClause
	 * @param whereArgs
	 * @return 0 in case of failure otherwise return no of row(s) are deleted.
	 */
	public int deleteRecordInDB(String tableName, String whereClause,
			String[] whereArgs) {
		return myDataBase.delete(tableName, whereClause, whereArgs);
	}

	// --------------------- Select Raw Query Functions ---------------------

	/**
	 * apply raw Query
	 * 
	 * @param query
	 * @param selectionArgs
	 * @return Cursor
	 */
	public Cursor selectRecordsFromDB(String query, String[] selectionArgs) {
		return myDataBase.rawQuery(query, selectionArgs);
	}

	// -------------------------------------------------------------------------

	/**
	 * 加入数据
	 * 
	 * 
	 */
	public long insertRecordsInDB(String tableName, ContentValues values) {
		return myDataBase.insert(tableName, null, values);
	}

}
