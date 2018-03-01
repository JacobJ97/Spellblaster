package com.ghostchippies.spellblasterforandroidv44;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.ghostchippies.spellblaster.*;

import java.util.Objects;

public class HighScores extends AppCompatActivity {

    Spinner spinner;
    ListView listView;
    ArrayAdapter arrayAdapter;
    SQLiteDatabase database;
    String tableName, selectedDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ghostchippies.spellblaster.R.layout.activity_high_scores);

        spinner = (Spinner) findViewById(com.ghostchippies.spellblaster.R.id.difficulty_database_spinner);
        selectedDifficulty = spinner.getSelectedItem().toString();
        listView = (ListView) findViewById(com.ghostchippies.spellblaster.R.id.high_score_list);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficulty = spinner.getSelectedItem().toString();
                initialiseDatabase(selectedDifficulty);
                orderScores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //there will always be a value
            }
        });
    }

    public void initialiseDatabase(String selectedDifficulty) {
        if (Objects.equals(selectedDifficulty, "Easy")) {
            tableName = "easy";
        }
        if (Objects.equals(selectedDifficulty, "Moderate")) {
            tableName = "moderate";
        }
        if (Objects.equals(selectedDifficulty, "Hard")) {
            tableName = "hard";
        }

        try {
            database = this.openOrCreateDatabase("scoresSpellblaster", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (score_id INTEGER " +
                    "PRIMARY KEY AUTOINCREMENT NOT NULL, person_initials VARCHAR(3), score INTEGER);");

            Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
            int numOfValues = cursor.getCount();
            cursor.close();

            if (numOfValues == 0) {
                for (int x = 1; x <= 10; ++x) {
                    addScore("AAA", 0);
                }
            }
        } catch (SQLException e) {
            Log.e("Something went wrong.", e.toString());
        }
    }

    public void addScore(String name, int score) {
        String sqlQuery = "INSERT INTO " + tableName + " (score_id, person_initials, score)" + " VALUES " +
                "(NULL, ?, ?);";
        Object[] bindArgs = new Object[] {name, score};

        try {
            database.execSQL(sqlQuery, bindArgs);
        } catch (SQLException e) {
            Log.e("Score cannot be added.", e.toString());
        }
    }

    public Object[] findHighScores() {
        Cursor sqlCursor = database.rawQuery("SELECT * FROM " + tableName + " ORDER BY score DESC;",
                null);

        int highScoreNames = sqlCursor.getColumnIndex("person_initials");
        int highScore = sqlCursor.getColumnIndex("score");

        sqlCursor.moveToFirst();

        String[] arrayOfNames = new String[10];
        int[] arrayOfScores = new int[10];

        for (int x = 0; x <= 9; ++x) {
            String name = sqlCursor.getString(highScoreNames);
            arrayOfNames[x] = name;

            int score = sqlCursor.getInt(highScore);
            arrayOfScores[x] = score;

            sqlCursor.moveToNext();
        }

        sqlCursor.close();

        return new Object[]{arrayOfNames, arrayOfScores};
    }

    public void orderScores() {
        Object[] databaseArrays = findHighScores();

        Object nameArrayObject = databaseArrays[0];
        String[] nameArray = (String[]) nameArrayObject;

        Object scoreArrayObject = databaseArrays[1];
        int[] scoreArray = (int[]) scoreArrayObject;

        String[] highScoreList = new String[10];

        for (int x = 0; x <= 9; ++x) {
            String name = nameArray[x];
            int score = scoreArray[x];

            String highScoreSingle = name + ": " + score;
            highScoreList[x] = highScoreSingle;
            arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, highScoreList);
            listView.setAdapter(arrayAdapter);
        }
    }
}
