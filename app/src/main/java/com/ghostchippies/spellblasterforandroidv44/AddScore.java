package com.ghostchippies.spellblasterforandroidv44;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

class AddScore {

    private String tableName;
    private SQLiteDatabase database;
    private Context context;

    AddScore(Context context) {
        this.context = context;
    }

    void initialiseDatabase(String difficulty) {
        if (Objects.equals(difficulty, "Easy")) {
            tableName = "easy";
        }
        if (Objects.equals(difficulty, "Moderate")) {
            tableName = "moderate";
        }
        if (Objects.equals(difficulty, "Hard")) {
            tableName = "hard";
        }

        try {
            database = context.openOrCreateDatabase("scoresSpellblaster", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (score_id INTEGER " +
                    "PRIMARY KEY AUTOINCREMENT NOT NULL, person_initials VARCHAR(3), score INTEGER);");

            Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
            int numOfValues = cursor.getCount();
            cursor.close();

            if (numOfValues == 0) {
                for (int x = 1; x <= 10; ++x) {
                    addScoreToDB("AAA", 0);
                }
            }
        } catch (SQLException e) {
            Log.e("Something went wrong.", e.toString());
        }
    }

    void addScoreToDB(String name, int score) {
        String sqlQuery = "INSERT INTO " + tableName + " (score_id, person_initials, score)" + " VALUES " +
                "(NULL, ?, ?);";
        Object[] bindArgs = new Object[] {name, score};

        try {
            database.execSQL(sqlQuery, bindArgs);
        } catch (SQLException e) {
            Log.e("Score cannot be added.", e.toString());
        }
    }
}

