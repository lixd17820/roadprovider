package com.android.provider.roadcode;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class RoadcodeProvider extends ContentProvider {

    public static final String TAG = "RoadcodeProvider";

    public RoadcodeDatabaseHelper dbAdapter;

    public static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Roadcode.AUTHORITY, "roaditem", Roadcode.ROAD_ITEM);
        uriMatcher.addURI(Roadcode.AUTHORITY, "roadsegitem",
                Roadcode.ROADSEG_ITEM);
        uriMatcher.addURI(Roadcode.AUTHORITY, "xzqh", Roadcode.XZQH);
        uriMatcher.addURI(Roadcode.AUTHORITY, "queryLxxx", Roadcode.QUERYLXXX);
        uriMatcher.addURI(Roadcode.AUTHORITY, "query_cross", Roadcode.QUERY_CROSS);
        uriMatcher.addURI(Roadcode.AUTHORITY, "update_cross", Roadcode.UPDATE_CROSS);
        uriMatcher.addURI(Roadcode.AUTHORITY, "delete_cross", Roadcode.DELETE_CROSS);
        uriMatcher.addURI(Roadcode.AUTHORITY, "insert_cross", Roadcode.INSERT_CROSS);
    }

    public boolean onCreate() {
        int currentCode = 0;
        try {
            PackageManager pm = getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo("com.android.provider.roadcode",
                    0);
            currentCode = pi.versionCode;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        dbAdapter = RoadcodeDatabaseHelper.getDBAdapterInstance(getContext(),
                currentCode);
        try {
            dbAdapter.createDataBase();
        } catch (IOException e) {
            Log.i("*** select ", e.getMessage());
        }
        dbAdapter.openDataBase();
        return true;
    }

    @Override
    public int delete(Uri uri, String where, String[] arg) {
        int c = 0;
        switch (uriMatcher.match(uri)) {
            case Roadcode.DELETE_CROSS:
                c = dbAdapter.deleteRecordInDB(Roadcode.JtbzCross.TABLE_NAME, where, arg);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return c;
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {
        long rowId = 0;
        switch (uriMatcher.match(uri)) {
            case Roadcode.INSERT_CROSS:
                rowId = dbAdapter.insertRecordsInDB(Roadcode.JtbzCross.TABLE_NAME, cv);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Roadcode.CONTENT_URI,
                    rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c = null;

        switch (uriMatcher.match(uri)) {
            case Roadcode.ROAD_ITEM:
                c = dbAdapter
                        .selectRecordsFromDB(Roadcode.RoadItem.TABLE_NAME,
                                projection, selection, selectionArgs, null, null,
                                sortOrder);
                break;
            case Roadcode.ROADSEG_ITEM:
                c = dbAdapter
                        .selectRecordsFromDB(Roadcode.RoadsegItem.TABLE_NAME,
                                projection, selection, selectionArgs, null, null,
                                sortOrder);
                break;
            case Roadcode.XZQH:
                c = dbAdapter
                        .selectRecordsFromDB(Roadcode.Xzqh.TABLE_NAME, projection,
                                selection, selectionArgs, null, null, sortOrder);
                break;
            case Roadcode.QUERYLXXX:
                c = dbAdapter
                        .selectRecordsFromDB(Roadcode.ZapcLxxx.TABLE_NAME,
                                projection, selection, selectionArgs, null, null,
                                sortOrder);
                break;
            case Roadcode.QUERY_CROSS:
                c = dbAdapter
                        .selectRecordsFromDB(Roadcode.JtbzCross.TABLE_NAME,
                                projection, selection, selectionArgs, null, null,
                                sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues cv, String where, String[] arg) {
        int c = 0;
        switch (uriMatcher.match(uri)) {
            case Roadcode.UPDATE_CROSS:
                c = dbAdapter.updateRecordsInDB(Roadcode.JtbzCross.TABLE_NAME, cv, where, arg);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return c;
    }

}
