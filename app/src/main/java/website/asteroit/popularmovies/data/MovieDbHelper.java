package website.asteroit.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Wahyu on 26/07/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 2;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieContract.MovieEntry.TABLE_NAME + "(" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_POSTER + " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_BACKDROP + " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_DATE + " DATE NOT NULL," +
                MovieContract.MovieEntry.COLUMN_RUNTIME + " INTEGER NOT NULL," +
                MovieContract.MovieEntry.COLUMN_RATING + " DOUBLE NOT NULL," +
                MovieContract.MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL);";

        final String SQL_CREATE_VIDEO_TABLE = "CREATE TABLE " +
                MovieContract.VideoEntry.TABLE_NAME + "(" +
                MovieContract.VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.VideoEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                MovieContract.VideoEntry.COLUMN_VIDEO_NAME + " TEXT NOT NULL," +
                MovieContract.VideoEntry.COLUMN_VIDEO_URL + " TEXT NOT NULL," +
                MovieContract.VideoEntry.COLUMN_VIDEO_TYPE + " TEXT NOT NULL);";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " +
                MovieContract.ReviewEntry.TABLE_NAME + "(" +
                MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + MovieContract.MovieEntry.TABLE_NAME + "'");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.VideoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + MovieContract.VideoEntry.TABLE_NAME + "'");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + MovieContract.ReviewEntry.TABLE_NAME + "'");

        onCreate(sqLiteDatabase);
    }
}
