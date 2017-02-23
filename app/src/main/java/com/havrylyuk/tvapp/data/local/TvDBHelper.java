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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.havrylyuk.tvapp.data.local.TvContract.CategoryEntry;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;
import com.havrylyuk.tvapp.data.local.TvContract.ProgramEntry;

/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "tv.db";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    private Context context;

    public TvDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoryEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                CategoryEntry.COLUMN_CATEGORY_TITLE + " TEXT NOT NULL, " +
                CategoryEntry.COLUMN_CATEGORY_PICTURE + " TEXT NOT NULL, " +
                "UNIQUE (" + CategoryEntry.COLUMN_CATEGORY_ID + ") ON CONFLICT REPLACE );";

        String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + ChannelEntry.TABLE_NAME + " (" +
                ChannelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ChannelEntry.COLUMN_CHANNEL_ID + " INTEGER NOT NULL, " +
                ChannelEntry.COLUMN_CHANNEL_NAME + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_CHANNEL_URL + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_CHANNEL_PICTURE + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID + " INTEGER NOT NULL, " +
                ChannelEntry.COLUMN_CHANNEL_FAVORITE + " INTEGER NOT NULL DEFAULT 0 , " +
                "UNIQUE (" + ChannelEntry.COLUMN_CHANNEL_ID + ") ON CONFLICT IGNORE, " + //save favorites
                "FOREIGN KEY (" + ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + "(" + CategoryEntry.COLUMN_CATEGORY_ID + ") );";

        String SQL_CREATE_PROGRAM_TABLE = "CREATE TABLE " + ProgramEntry.TABLE_NAME + " (" +
                ProgramEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProgramEntry.COLUMN_PROGRAM_CHANEL_ID + " INTEGER NOT NULL, " +
                ProgramEntry.COLUMN_PROGRAM_DATE + " TEXT NOT NULL, " +
                ProgramEntry.COLUMN_PROGRAM_TIME + " TEXT NOT NULL, " +
                ProgramEntry.COLUMN_PROGRAM_TITLE + " TEXT NOT NULL, " +
                ProgramEntry.COLUMN_PROGRAM_DESCRIPTION + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + ProgramEntry.COLUMN_PROGRAM_CHANEL_ID + ") REFERENCES " +
                ChannelEntry.TABLE_NAME + "(" + ChannelEntry.COLUMN_CHANNEL_ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CHANNEL_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROGRAM_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChannelEntry.TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProgramEntry.TABLE_NAME);
            onCreate(sqLiteDatabase);

    }


}
