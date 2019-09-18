package website.asteroit.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Wahyu on 26/07/2017.
 */

public class MovieProvider extends ContentProvider {

    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int MOVIE_COUNT = 110;

    public static final int VIDEOS = 200;
    public static final int VIDEO_WITH_ID = 201;

    public static final int REVIEWS = 300;
    public static final int REVIEW_WITH_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.MovieEntry.TABLE_NAME, MOVIES);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.MovieEntry.TABLE_NAME + "/#", MOVIE_WITH_ID);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,
                MovieContract.MovieEntry.TABLE_NAME + "/" + MovieContract.MovieEntry.PATH_COUNT + "/#", MOVIE_COUNT);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.VideoEntry.TABLE_NAME, VIDEOS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.VideoEntry.TABLE_NAME + "/#", VIDEO_WITH_ID);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.ReviewEntry.TABLE_NAME, REVIEWS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.ReviewEntry.TABLE_NAME + "/#", REVIEW_WITH_ID);

        return uriMatcher;
    }

    private MovieDbHelper mMovieDbHelper;

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case MOVIES:
                retCursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_WITH_ID:
                selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                retCursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_COUNT:
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                retCursor = db.rawQuery("SELECT COUNT(*) FROM " + MovieContract.MovieEntry.TABLE_NAME +
                        " WHERE " + MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?", selectionArgs);
                break;
            case VIDEO_WITH_ID:
                selection = MovieContract.VideoEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                retCursor = db.query(MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case REVIEW_WITH_ID:
                selection = MovieContract.ReviewEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                retCursor = db.query(MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES:
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = MovieContract.MovieEntry.buildMovieIdUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case VIDEOS:
                return bulkInsertData(uri, values, MovieContract.VideoEntry.TABLE_NAME, MovieContract.VideoEntry.COLUMN_VIDEO_NAME);
            case REVIEWS:
                return bulkInsertData(uri, values, MovieContract.ReviewEntry.TABLE_NAME, MovieContract.ReviewEntry.COLUMN_AUTHOR);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    public int bulkInsertData(@NonNull Uri uri, @NonNull ContentValues[] values, String tableName, String uniqueColumn) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        db.beginTransaction();

        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {

                if (value == null) {
                    throw new IllegalArgumentException("Cannot have null content values");
                }

                long _id = -1;

                try {
                    _id = db.insertOrThrow(tableName, null, value);
                } catch (SQLiteConstraintException e) {
                    Log.w(LOG_TAG, "Attempting to insert " +
                            value.getAsString(
                                    uniqueColumn)
                            + " but value is already in database.");
                }

                if (_id != -1) {
                    rowsInserted++;
                }
            }

            if (rowsInserted > 0) {
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }

        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case MOVIES:
                rowsDeleted = deleteData(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_WITH_ID:
                selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                rowsDeleted = deleteData(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEOS:
                rowsDeleted = deleteData(MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEO_WITH_ID:
                selection = MovieContract.VideoEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                rowsDeleted = deleteData(MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = deleteData(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW_WITH_ID:
                selection = MovieContract.ReviewEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                rowsDeleted = deleteData(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    public int deleteData(String tableName, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(tableName, selection, selectionArgs);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + tableName + "'");

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

}
