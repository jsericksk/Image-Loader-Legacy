package com.kproject.imageloader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kproject.imageloader.models.Bookmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BookmarksDB extends SQLiteOpenHelper {
    private static final String DB_NAME = "bookmarks.db";
    private static final String TABLE = "bookmarks";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String URL = "url";
    private static final String DB_PATH = "/data/data/com.kproject.imageloader/databases/bookmarks.db";
    private static final String[] COLUMNS = {ID, TITLE, URL};

    public BookmarksDB(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE =
                "CREATE TABLE " + TABLE + " ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "title TEXT, "
                        + "url TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        this.onCreate(db);
    }

    public void addBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, bookmark.getTitle());
        contentValues.put(URL, bookmark.getUrl());
        db.insert(TABLE, null, contentValues);
        db.close();
    }

    public Bookmark getBookmark(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE, COLUMNS, " id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor == null) {
            cursor.close();
            return null;
        } else {
            cursor.moveToFirst();
            Bookmark bookmark = bookmarkToCursor(cursor);
            cursor.close();
            return bookmark;
        }
    }

    private Bookmark bookmarkToCursor(Cursor cursor) {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(Integer.parseInt(cursor.getString(0)));
        bookmark.setTitle(cursor.getString(1));
        bookmark.setUrl(cursor.getString(2));
        return bookmark;
    }

    public List<Bookmark> getAllBookmarks() {
        List<Bookmark> bookmarkList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Bookmark bookmark = bookmarkToCursor(cursor);
                bookmarkList.add(bookmark);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookmarkList;
    }

    public int updateBookmart(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, bookmark.getTitle());
        contentValues.put(URL, bookmark.getUrl());
        int update = db.update(TABLE, contentValues, ID + " = ?", new String[]{String.valueOf(bookmark.getId())});
        db.close();
        return update;
    }

    public int deleteBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(TABLE, ID + " = ?", new String[]{String.valueOf(bookmark.getId())});
        db.close();
        return delete;
    }

    public void importDatabase(String fileDBPath) {
        byte[] bytes = new byte[1024];
        int size;
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(fileDBPath);
            output = new FileOutputStream(DB_PATH);
            while ((size = input.read(bytes)) != -1) {
                output.write(bytes, 0, size);
            }
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void exportDatabase(String exportOutput) {
        byte[] bytes = new byte[1024];
        int size;
        InputStream input = null;
        OutputStream output = null;
        try {
            if (!new File(exportOutput).exists()) {
                new File(exportOutput).createNewFile();
            }
            input = new FileInputStream(DB_PATH);
            output = new FileOutputStream(exportOutput);
            while ((size = input.read(bytes)) != -1) {
                output.write(bytes, 0, size);
            }
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }

        }
    }

}
