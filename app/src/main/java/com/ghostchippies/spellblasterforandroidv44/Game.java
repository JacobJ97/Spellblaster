package com.ghostchippies.spellblasterforandroidv44;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Game extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener,
        SensorEventListener, SubmitDetailsDialog.NoticeDialogListener {

    private final int EASY = 1;
    private final int MODERATE = 2;
    private int HARD = 3;

    private View decorView;
    private ActionBar actionBar;
    private GestureDetectorCompat gestureDetector;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private LinearLayout linearLayout;
    private TextView instructionLabel, quizNumber, score, shuffledLetters, descriptionLabel,
            resultsLabel, wordInputLabel, scoreLabel, audioSource1, audioSource2, audioSource3;
    private EditText wordInput;
    private Button hintButton, submitButton, gameToHomeScreen, gameToHighScores;
    private String[] arrayOfWords = new String[30];
    private List<String> listOfWords;
    private String tweetUrl;
    private GameSound gameSound;
    private AddScore addScore;
    private MediaPlayer mediaPlayer;

    private boolean twitterIntegration;
    private int numberOfQuestions, questionCount, difficulty;
    private int totalPoints = 0, scorePoints, losePoints;
    private int bigBomb, bombPlaced, firecracker, sadTrombone, sizzleOut;
    private long lastUpdate;
    private float last_x, last_y, last_z;
    private String wordToShuffle;
    private String playerInitials;

    private static final int NO_CONTROLS =
            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    private static final int FULLSCREEN =
            View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ghostchippies.spellblaster.R.layout.activity_game);

        /*grabs sharedpreferences from preferencescreen */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        twitterIntegration = sharedPreferences.getBoolean("twitterIntegration", false);
        playerInitials = sharedPreferences.getString("nameInitials", "AAA");

        /* Int values are sent as String */
        String difficultyString = sharedPreferences.getString("difficultyValue", "2");
        String numberOfQuestionsString = sharedPreferences.getString("numberOfQuestions", "10");

        /* Conversion to Integer */
        difficulty = Integer.parseInt(difficultyString);
        numberOfQuestions = Integer.parseInt(numberOfQuestionsString);

        /* Initialise gestures */
        actionBar = getSupportActionBar();
        gestureDetector = new GestureDetectorCompat(this, new GestureHandler());
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(FULLSCREEN);
        decorView.setOnSystemUiVisibilityChangeListener(this);

        /* Set up and add sound effects */
        gameSound = new GameSound(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bigBomb = gameSound.addSound(com.ghostchippies.spellblaster.R.raw.big_bomb);
            bombPlaced = gameSound.addSound(com.ghostchippies.spellblaster.R.raw.bomb_has_been_placed);
            firecracker = gameSound.addSound(com.ghostchippies.spellblaster.R.raw.firecracker);
            sadTrombone = gameSound.addSound(com.ghostchippies.spellblaster.R.raw.sad_trombone);
            sizzleOut = gameSound.addSound(com.ghostchippies.spellblaster.R.raw.sizzle_out);
        }
        else {
            bigBomb = gameSound.addSoundKitKat(com.ghostchippies.spellblaster.R.raw.big_bomb);
            bombPlaced = gameSound.addSoundKitKat(com.ghostchippies.spellblaster.R.raw.bomb_has_been_placed);
            firecracker = gameSound.addSoundKitKat(com.ghostchippies.spellblaster.R.raw.firecracker);
            sadTrombone = gameSound.addSoundKitKat(com.ghostchippies.spellblaster.R.raw.sad_trombone);
            sizzleOut = gameSound.addSoundKitKat(com.ghostchippies.spellblaster.R.raw.sizzle_out);
        }

        /* Set up music */
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(this, com.ghostchippies.spellblaster.R.raw.killers);
        mediaPlayer.setVolume(0.5f, 0.5f);

        addScore = new AddScore(this);

        /* finds accelerometer */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        /* initialises widgets and stuff */
        instructionLabel = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.game_instructions);
        quizNumber = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.quiz_number);
        shuffledLetters = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.shuffled_letters);
        descriptionLabel = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.game_word_description);
        hintButton = (Button) findViewById(com.ghostchippies.spellblaster.R.id.hint_button);
        resultsLabel = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.text_result);
        wordInputLabel = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.game_guess_word_label);
        wordInput = (EditText) findViewById(com.ghostchippies.spellblaster.R.id.game_guess_word);
        submitButton = (Button) findViewById(com.ghostchippies.spellblaster.R.id.submit_button);
        score = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.score);
        linearLayout = (LinearLayout) findViewById(com.ghostchippies.spellblaster.R.id.score_quiz_number_layout);
        scoreLabel = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.score_label);
        gameToHomeScreen = (Button) findViewById(com.ghostchippies.spellblaster.R.id.to_home_button);
        gameToHighScores = (Button) findViewById(com.ghostchippies.spellblaster.R.id.to_high_scores);
        audioSource1 = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.sources_audio_1);
        audioSource2 = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.sources_audio_2);
        audioSource3 = (TextView) findViewById(com.ghostchippies.spellblaster.R.id.sources_audio_3);

        /* Grabs array of words based on difficulty set by player */
        if (difficulty == EASY) {
            arrayOfWords = getResources().getStringArray(com.ghostchippies.spellblaster.R.array.easy_words);
            scorePoints = 2;
            losePoints = -1;
        } else if (difficulty == MODERATE) {
            arrayOfWords = getResources().getStringArray(com.ghostchippies.spellblaster.R.array.moderate_words);
            scorePoints = 3;
            losePoints = -2;
        } else if (difficulty == HARD) {
            arrayOfWords = getResources().getStringArray(com.ghostchippies.spellblaster.R.array.hard_words);
            scorePoints = 4;
            losePoints = -4;
        }

        /* Shuffles word position */
        listOfWords = Arrays.asList(arrayOfWords);
        Collections.shuffle(listOfWords);
        questionCount = 1;

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void startQuestion() {
        score.setText("" + "Score: " + totalPoints);
        quizNumber.setText(questionCount + "/" + numberOfQuestions);
        Splitter splitter = new Splitter(listOfWords.get(questionCount - 1));
        wordToShuffle = splitter.getWord();
        Shuffler shuffler = new Shuffler(wordToShuffle);
        String shuffledWord = shuffler.getShuffledWord();
        shuffledLetters.setText(shuffledWord);
        String shuffledWordDescription = splitter.getDescription();
        descriptionLabel.setText(shuffledWordDescription);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String retrievedString = wordInput.getText().toString();
                wordInput.setText("");
                retrievedString = retrievedString.toLowerCase();
                if (Objects.equals(retrievedString, "")) {
                    Toast.makeText(Game.this, "Textbox must not be empty.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (retrievedString.matches(".*\\d+.*")) {
                    Toast.makeText(Game.this, "Textbox must not contain a number.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Objects.equals(retrievedString, wordToShuffle)) {
                    resultsLabel.setText("That is incorrect! Lose " + losePoints + " points.");
                    questionCount++;
                    totalPoints += losePoints;
                    if (questionCount > numberOfQuestions) {
                        finishGame();
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            gameSound.play(sizzleOut);
                        }
                        else {
                            gameSound.playKitKat(sizzleOut);
                        }
                        startQuestion();
                    }
                }
                if (Objects.equals(retrievedString, wordToShuffle)) {
                    //Toast.makeText(Game.this, "That is correct! Gain " + scorePoints + "points for " +
                                    //"correctly spelling '" + wordToShuffle + ".'", Toast.LENGTH_SHORT).show();
                    resultsLabel.setText("That is correct! Gain " + scorePoints + " points for " +
                            "correctly spelling '" + wordToShuffle + ".'");
                    questionCount++;
                    totalPoints += scorePoints;

                    if (questionCount > numberOfQuestions) {
                        finishGame();
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            gameSound.play(firecracker);
                        }
                        else {
                            gameSound.playKitKat(firecracker);
                        }
                        startQuestion();
                    }
                }
            }
        });

        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(Game.this, "The word was " + wordToShuffle, Toast.LENGTH_SHORT).show();
                resultsLabel.setText("You have used a hint to reveal the spelling of the word '" +
                        wordToShuffle + ".' Lose " + (losePoints - 1) + " points.");
                totalPoints += losePoints - 1;
                questionCount++;
                if (questionCount > numberOfQuestions) {
                    finishGame();
                } else {
                    startQuestion();
                }
            }
        });
    }

    public void finishGame() {
        if (totalPoints > 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                gameSound.play(bigBomb);
            }
            else {
                gameSound.playKitKat(bigBomb);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                gameSound.play(sadTrombone);
            }
            else {
                gameSound.playKitKat(sadTrombone);
            }
        }

        linearLayout.setVisibility(View.GONE);
        quizNumber.setVisibility(View.GONE);
        score.setVisibility(View.GONE);
        shuffledLetters.setVisibility(View.GONE);
        descriptionLabel.setVisibility(View.GONE);
        hintButton.setVisibility(View.GONE);
        resultsLabel.setVisibility(View.GONE);
        wordInputLabel.setVisibility(View.GONE);
        wordInput.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        scoreLabel.setVisibility(View.VISIBLE);
        gameToHomeScreen.setVisibility(View.VISIBLE);
        gameToHighScores.setVisibility(View.VISIBLE);
        audioSource1.setVisibility(View.VISIBLE);
        audioSource2.setVisibility(View.VISIBLE);
        audioSource3.setVisibility(View.VISIBLE);

        if (difficulty == EASY) {
            addScore.initialiseDatabase("Easy");
        }

        if (difficulty == MODERATE) {
            addScore.initialiseDatabase("Moderate");
        }

        if (difficulty == HARD) {
            addScore.initialiseDatabase("Hard");
        }
        addScore.addScoreToDB(playerInitials, totalPoints);

        if (twitterIntegration) {
            DialogFragment newFragment = new SubmitDetailsDialog();
            newFragment.show(getFragmentManager(), "points");
        }

        scoreLabel.setText("You have scored a total of " + totalPoints + " point(s), congrats! You "
                + " can go back to the main menu, or see how your score compares to others.");
    }

    private void toggleControls() {
        int flags = decorView.getSystemUiVisibility();
        flags ^= NO_CONTROLS;
        decorView.setSystemUiVisibility(flags);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if (actionBar == null) return;
        switch (visibility) {
            case NO_CONTROLS:
                actionBar.hide();
                break;
            default:
                actionBar.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.ghostchippies.spellblaster.R.menu.activity_game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case com.ghostchippies.spellblaster.R.id.settings_actionbar_id: {
                goToSettings(item);
                break;
            }
            case com.ghostchippies.spellblaster.R.id.share_actionbar_id: {
                socialShare(item);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void goToSettings(MenuItem item) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (instructionLabel.getVisibility() == View.VISIBLE) {
                long currentTime = System.currentTimeMillis();

                if ((currentTime - lastUpdate) > 100) {
                    long diffTime = (currentTime - lastUpdate);
                    lastUpdate = currentTime;

                    float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                    float SHAKE_LIMIT = 2000;
                    if (speed > SHAKE_LIMIT) {
                        instructionLabel.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        quizNumber.setVisibility(View.VISIBLE);
                        score.setVisibility(View.VISIBLE);
                        shuffledLetters.setVisibility(View.VISIBLE);
                        descriptionLabel.setVisibility(View.VISIBLE);
                        hintButton.setVisibility(View.VISIBLE);
                        resultsLabel.setVisibility(View.VISIBLE);
                        wordInputLabel.setVisibility(View.VISIBLE);
                        wordInput.setVisibility(View.VISIBLE);
                        submitButton.setVisibility(View.VISIBLE);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            gameSound.play(bombPlaced);
                        }
                        else {
                            gameSound.playKitKat(bombPlaced);
                        }
                        mediaPlayer.start();

                        startQuestion();
                    }

                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //unnecessary
    }

    public void socialShare(MenuItem item) {
        tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s",
                encode("Playing my new favourite game, Spell Blaster! http://play.google.com" +
                        "/store/apps/details?id=com.ghostchippies.spellblaster"));
        shareToTwitter(tweetUrl);
    }

    public void shareToTwitter(String tweetUrl) {
        mediaPlayer.stop();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                intent.setPackage(info.activityInfo.packageName);
            }
        }

        startActivity(intent);
    }

    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        mediaPlayer.stop();
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mediaPlayer.start();
    }

    public void gameToHighScores(View view) {
        Intent intent = new Intent(this, HighScores.class);
        mediaPlayer.stop();
        startActivity(intent);
    }

    public void gameToHomeScreen(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        mediaPlayer.stop();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s",
                encode("Scored " + totalPoints + " points while playing Spell Blaster! http://play" +
                        ".google.com/store/apps/details?id=com.ghostchippies.spellblaster"));
        shareToTwitter(tweetUrl);
    }

    public void stopMusic(MenuItem item) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private class GestureHandler extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            toggleControls();
        }
    }


}