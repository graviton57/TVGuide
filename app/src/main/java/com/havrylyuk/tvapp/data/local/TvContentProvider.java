/*
 * Copyright (c)  2017. Igor Gavriluyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.havrylyuk.tvapp.data.local;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.havrylyuk.tvapp.data.local.TvContract.CategoryEntry;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;
import com.havrylyuk.tvapp.data.local.TvContract.ProgramEntry;

/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvContentProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private TvDBHelper openHelper;

    static final int CATEGORY = 100;
    static final int CATEGORY_WITH_ID = 101;
    static final int CHANNEL = 103;
    static final int CHANNEL_WITH_ID = 104;
    static final int PROGRAM = 105;
    static final int PROGRAM_WITH_ID = 106;

    private static final SQLiteQueryBuilder sCategoryQueryBuilder;
    private static final SQLiteQueryBuilder sChannelQueryBuilder;
    private static final SQLiteQueryBuilder sProgramQueryBuilder;

    static {
        sCategoryQueryBuilder = new SQLiteQueryBuilder();
        sCategoryQueryBuilder.setTables(CategoryEntry.TABLE_NAME);

        sChannelQueryBuilder = new SQLiteQueryBuilder();
        sChannelQueryBuilder.setTables(ChannelEntry.TABLE_NAME + " INNER JOIN " + CategoryEntry.TABLE_NAME +
                " ON " + ChannelEntry.TABLE_NAME + "." + ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID +
                " = " + CategoryEntry.TABLE_NAME + "." + CategoryEntry.COLUMN_CATEGORY_ID);

        sProgramQueryBuilder = new SQLiteQueryBuilder();
        sProgramQueryBuilder.setTables(ProgramEntry.TABLE_NAME);
    }


    private static final String categoryByIdSelection =
            CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID + " = ? ";

    private static final String channelByIdSelection =
            ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID + " = ? ";

    private static final String programByIdSelection =
            ProgramEntry.TABLE_NAME + "." + ProgramEntry._ID + " = ? ";

    private Cursor getCategoryById(Uri uri, String[] projection, String sortOrder) {
        String selectionCategoryId = String.valueOf(CategoryEntry.getIdFromUri(uri));
        String selection = categoryByIdSelection;
        String[] selectionArgs = new String[]{selectionCategoryId};
        return sCategoryQueryBuilder.query(openHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getChannelById(Uri uri, String[] projection, String sortOrder) {
        String selectionChannelId = String.valueOf(CategoryEntry.getIdFromUri(uri));
        String selection = channelByIdSelection;
        String[] selectionArgs = new String[]{selectionChannelId};
        return sChannelQueryBuilder.query(openHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getProgramById(Uri uri, String[] projection, String sortOrder) {
        String selectionProgramId = String.valueOf(CategoryEntry.getIdFromUri(uri));
        String selection = programByIdSelection;
        String[] selectionArgs = new String[]{selectionProgramId};
        return sProgramQueryBuilder.query(openHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TvContract.CONTENT_AUTHORITY;
        // For each desc of URI you want to add, create a corresponding code.
        matcher.addURI(authority, TvContract.PATH_CATEGORY, CATEGORY);
        matcher.addURI(authority, TvContract.PATH_CATEGORY +"/#", CATEGORY_WITH_ID);
        matcher.addURI(authority, TvContract.PATH_CHANEL, CHANNEL);
        matcher.addURI(authority, TvContract.PATH_CHANEL +"/#", CHANNEL_WITH_ID);
        matcher.addURI(authority, TvContract.PATH_PROGRAM, PROGRAM);
        matcher.addURI(authority, TvContract.PATH_PROGRAM +"/#", PROGRAM_WITH_ID);
        return matcher;
    }

    public TvContentProvider() {

    }

    @Override
    public boolean onCreate() {
        openHelper = new TvDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            case CATEGORY_WITH_ID:
                return CategoryEntry.CONTENT_ITEM_TYPE;
            case CHANNEL:
                return ChannelEntry.CONTENT_TYPE;
            case CHANNEL_WITH_ID:
                return ChannelEntry.CONTENT_ITEM_TYPE;
            case PROGRAM:
                return ProgramEntry.CONTENT_TYPE;
            case PROGRAM_WITH_ID:
                return ProgramEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case CATEGORY:
                rowsDeleted = db.delete(
                        CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHANNEL:
                rowsDeleted = db.delete(
                        ChannelEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROGRAM:
                rowsDeleted = db.delete(
                        ProgramEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case CATEGORY: {
                long _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                else  throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CHANNEL: {
                long _id = db.insert(ChannelEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ChannelEntry.buildChanelUri(_id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PROGRAM: {
                long _id = db.insert(ProgramEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ProgramEntry.buildProgramUri(_id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "category"
            case CATEGORY: {
                retCursor = openHelper.getReadableDatabase().query(
                        sCategoryQueryBuilder.getTables(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "category/*"
            case CATEGORY_WITH_ID: {
                retCursor = getCategoryById(uri, projection, sortOrder);
                break;
            }
            // "channel"
            case CHANNEL: {
                retCursor = openHelper.getReadableDatabase().query(
                        sChannelQueryBuilder.getTables(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "channel/*"
            case CHANNEL_WITH_ID: {
                retCursor = getChannelById(uri, projection, sortOrder);
                break;
            }
            // "program"
            case PROGRAM: {
                retCursor = openHelper.getReadableDatabase().query(
                        ProgramEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "program/*"
            case PROGRAM_WITH_ID: {
                retCursor = getProgramById(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case CATEGORY:
                rowsUpdated = db.update(CategoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CHANNEL:
                rowsUpdated = db.update(ChannelEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case PROGRAM:
                rowsUpdated = db.update(ProgramEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case CATEGORY:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CategoryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case CHANNEL:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ChannelEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case PROGRAM:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ProgramEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
