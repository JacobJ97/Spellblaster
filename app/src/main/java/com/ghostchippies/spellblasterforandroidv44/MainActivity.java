package com.ghostchippies.spellblasterforandroidv44;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ghostchippies.spellblaster.R.layout.activity_main);
    }

    public void toGame(View view) {
        Intent intent = new Intent(this, Game.class);
        startActivity(intent);
    }

    public void toSettings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void toHighScores(View view) {
        Intent intent = new Intent(this, HighScores.class);
        startActivity(intent);
    }
}
