package com.example.scrabble;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    // mode = 1 - new game, 2 players
    // = 2 - new game with a computer
    // = 3 - continue a previous game
    private int mode;
    private boolean isComputer;
    private boolean finished;

    private TextView[] scores;
    // Layout for letters in the hand
    private LinearLayout linearLayout;
    private GameField field;
    private GameBag bag;
    private GamePlayer[] players;

    // Alert Dialogs with information whose turn is being made
    private AlertDialog[] dialogs;
    // Alert Dialog if words are unconnected
    private AlertDialog connectDialog;
    private AlertDialog.Builder dictDialog;

    // Mark whose turn is being made;
    // 0 - 1st player,
    // 1 - 2nd player
    private int turn;
    // Number of letters in the player's hand
    private final int handSize = 7;

    // Information about a previous game
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Game initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getSupportActionBar().hide();
        Bundle args = getIntent().getExtras();
        mode = (Integer) args.get(getString(R.string.mode));
        isComputer = mode == 2;
        finished = false;

        // View initialization
        scores = new TextView[2];
        scores[0] = findViewById(R.id.score1);
        scores[1] = findViewById(R.id.score2);
        linearLayout = findViewById(R.id.handLayout);

        GridLayout gridLayout = findViewById(R.id.gridField);
        Point size = new Point();
        // Getting width of the screen
        getWindowManager().getDefaultDisplay().getSize(size);
        field = new GameField(this, gridLayout, this, size.x);
        bag = new GameBag();
        players = new GamePlayer[2];
        players[0] = new GamePlayer(linearLayout, this, size.x);
        if (!isComputer) { // Creating a 2nd player
            players[1] = new GamePlayer(linearLayout, this, size.x);
            dialogs = new AlertDialog[2];
            // Setting text for dialogs
            createAlertDialog(0);
            createAlertDialog(1);
        } else { // Creating a computer player
            players[1] = new GamePlayer();
            TextView tv = findViewById(R.id.viewName2);
            tv.setText(R.string.computerName);
        }
        createConnectDialog();

        turn = 0;
        if (mode < 3) { // Starting a new game. Loading user dictionary
            preferences = getPreferences(MODE_PRIVATE);
            Set<String> dict = new HashSet<>();
            int n = preferences.getInt(getString(R.string.myDictSize), 0);
            for (int i = 0; i < n; ++i) {
                dict.add(preferences.getString("myDict" + i, ""));
            }
            field.setMyDict(dict);
            showHand();
        }
        else { // Continuing a previous game
            load();
        }
    }


    private void createAlertDialog(int t) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialogTitle);
        String format = getResources().getString(R.string.dialogMessage);
        String message = "";
        if (t == 0) {
            message = String.format(format, getResources().getString(R.string.name1));
        }
        if (t == 1) {
            message = String.format(format, getResources().getString(R.string.name2));
        }

        builder.setMessage(message);

        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            showHand();
        });
        dialogs[t] = builder.create();
    }

    // Creating Alert Dialog in case words are unconnected
    private void createConnectDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialogTitle);
        builder.setMessage(R.string.connectionMessage);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        connectDialog = builder.create();
    }

    // Filling player's hand and showing it
    private void showHand() {

        while (players[turn].getHandSize() < handSize) {
            GameLetter l = bag.getLetter();
            if (l == null) { // Bag is empty
                if (players[turn].getHandSize() == 0) { // Game over
                    if (players[0].getScore() > players[1].getScore()) { // 1st player won
                        createFinishDialog(0);
                        return;
                    }
                    if (players[0].getScore() < players[1].getScore()) { // 2nd player won
                        createFinishDialog(1);
                        return;
                    }
                    // Draw
                    createFinishDialog(2);
                    return;
                }
                break;
            }
            players[turn].addLetter(l);
        }

        players[turn].drawHand();
    }

    private void createFinishDialog(int t) {

        finished = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.finish);
        String format = getResources().getString(R.string.win);
        String message = "";
        if (t == 0) {
            message = String.format(format, getResources().getString(R.string.name1));
        }
        if (t == 1) {
            message = String.format(format, getResources().getString(R.string.name2));
        }
        if (t == 2) {
            message = getString(R.string.noWin);
        }

        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> exit());
        AlertDialog d = builder.create();
        d.show();
    }

    // Changing the turn and showing information about it
    private void showDialog() {
        if (turn == 0) {
            turn = 1;
        }
        else {
            turn = 0;
        }
        linearLayout.removeAllViews();
        dialogs[turn].show();
    }


    // Show dialog if words are unconnected
    private void showConnectDialog() {
        connectDialog.show();
    }

    // Word is not in dictionary dialog
    private void showDictDialog(String word) {
        dictDialog = new AlertDialog.Builder(this);
        dictDialog.setTitle(R.string.dialogTitle);
        String format = getResources().getString(R.string.dictMessage);
        String message = String.format(format, word);
        dictDialog.setMessage(message);
        dictDialog.setCancelable(true);
        dictDialog.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        dictDialog.setNeutralButton(R.string.addInDict, (dialogInterface, i) -> {
            field.addInDict();
            dialogInterface.dismiss();
            checkWords();
        });
        AlertDialog dialog = dictDialog.create();
        dialog.show();
    }
    // Remove letters from this turn
    private void reset() {
        field.resetLetters();
        players[turn].backLettersFromField();
    }
    //Check words in dictionary
    private void checkWords() {
        if (!field.checkConnects()) {
            showConnectDialog();
            return;
        }
        String w = field.checkWordsInDict();
        if (w != null) {
            showDictDialog(w);
            return;
        }
        field.updateField();
        players[turn].addPoints(field.getPoints());
        scores[turn].setText(String.format(Locale.getDefault(), "%d", players[turn].getScore()));

        if (!isComputer) {
            showDialog();
        }
        else {
            computer();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.reset):
                if (!players[turn].isChangeMode()) {
                    reset();
                }
                break;
            case (R.id.pass):
                if (!players[turn].isChangeMode()) {
                    if (turn == 0)
                        turn = 1;
                    else turn = 0;
                    createFinishDialog(turn);
                }
                break;
            case (R.id.ok):
                if (!players[turn].isChangeMode()) {
                    checkWords();
                }
                break;
            case (R.id.change):
                if (players[turn].isChangeMode()) {
                    int num = players[turn].getNumChange();
                    if (num > 0) {
                        bag.backLetter(players[turn].removeChangedImages());
                        players[turn].setChangeMode();
                        if (!isComputer) {
                            showDialog();
                        } else {
                            computer();
                        }
                    }
                    else {
                        players[turn].setChangeMode();
                    }
                }
                else {
                    reset();
                    players[turn].setChangeMode();
                }
                break;
            default:
                if (players[turn].getClickedLetter() != null && field.cellEmpty((Button) view)) {
                    GameLetter clickedLetter = players[turn].getClickedLetter();
                    if (clickedLetter.getLetter() == '*') {
                        changeStar(view);
                    }
                    else {
                        addLetter(clickedLetter, view);
                    }
                }
                break;
        }
    }
    // Add letter to the field
    private void addLetter(GameLetter letter, View view) {
        view.setBackgroundResource(letter.getImageId());
        field.addLetter((Button) view, letter);
        players[turn].removeClickedImageFromHand();
    }
    // Change * on the field to a letter
    private void changeStar(final View view) {
        LayoutInflater li = LayoutInflater.from(this);
        final View dialog = li.inflate(R.layout.change_star, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialog);
        final EditText input = dialog.findViewById(R.id.editStar);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            String s = input.getText().toString();
            addLetter(new GameLetter(s.charAt(0)), view);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog d = builder.create();
        d.show();
    }

    private void computer() {
        while (players[1].getHandSize() < handSize) {
            players[1].addLetter(bag.getLetter());
        }
        field.computerMode(players[1].getHand());
        players[1].addPoints(field.getPoints());
        scores[1].setText(String.format(Locale.getDefault(), "%d", players[1].getScore()));

        showHand();
    }


    // Exit to main activity
    private void exit() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    //Auto save
    private void save() {
        // discard changes if any
        reset();
        // Straightforward save of all settings to editor
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("finished", finished);
        Set<String> myDict = field.getMyDict();
        editor.putInt(getString(R.string.myDictSize), myDict.size());
        int idx = 0;
        for (String item : myDict) {
            editor.putString("myDict" + idx, item);
            ++idx;
        }
        if (finished) {
            return;
        }
        editor.putInt(getString(R.string.turn), turn);
        editor.putBoolean(getString(R.string.isComputer), isComputer);
        Map<GameLetter, Integer> bagMap = bag.getBag();
        for(Map.Entry<GameLetter, Integer> item : bagMap.entrySet()) {
            editor.putInt("" + item.getKey().getLetter(), item.getValue());
        }
        int sz = field.getFieldSize();
        for (int i = 0; i < sz; ++i) {
            for (int j = 0; j < sz; ++j) {
                int n = i * sz + j;
                editor.putString(getString(R.string.fieldLetter) + n,
                        "" + field.getFieldLetter(i, j));
            }
        }
        Set<String> dict = field.getDict();
        editor.putInt(getString(R.string.dictSize), dict.size());
        int i = 0;
        for (String item : dict) {
            editor.putString(getString(R.string.dict) + i, item);
            ++i;
        }
        Set<GameLetter> set1 = players[0].getHand();
        Set<GameLetter> set2 = players[1].getHand();
        editor.putInt("player1Num", players[0].getHandSize());
        editor.putInt("player2Num", players[1].getHandSize());
        i = 0;
        for (GameLetter item : set1) {
            editor.putString("player1" + i, item.getLetter() + "");
            ++i;
        }
        i = 0;
        for (GameLetter item : set2) {
            editor.putString("player2" + i, item.getLetter() + "");
            ++i;
        }
        editor.putInt("score1", players[0].getScore());
        editor.putInt("score2", players[1].getScore());
        editor.apply();
    }

    // Loading information about a previous game
    private void load() {

        preferences = getPreferences(MODE_PRIVATE);
        turn = preferences.getInt(getString(R.string.turn), 0);
        finished = preferences.getBoolean("finished", false);



        if (!finished) {
            field.clearDict();
        }
        int myDictSize = preferences.getInt(getString(R.string.myDictSize), 0);
        Set<String> myDict = new HashSet<>();
        for (int i = 0; i < myDictSize; ++i) {
            myDict.add(preferences.getString("myDict" + i, ""));
        }
        field.setMyDict(myDict);
        if (finished) {
            return;
        }

        isComputer = preferences.getBoolean(getString(R.string.isComputer), false);
        if (isComputer) {
            players[1] = new GamePlayer();
            TextView tv = findViewById(R.id.viewName2);
            tv.setText(R.string.computerName);
        }
        Map<GameLetter, Integer> bagMap = bag.getBag();
        for (int i = 0; i < 32; ++i) {
            char c = (char)('a' + i);
            int n = preferences.getInt("" + c, 0);
            if (n > 0) {
                bagMap.put(new GameLetter(c), n);
            }
            else {
                bagMap.remove(new GameLetter(c));
            }
        }
        int n = preferences.getInt("*", 0);
        if (n > 0) {
            bagMap.put(new GameLetter('*'), n);
        }
        else {
            bagMap.remove(new GameLetter('*'));
        }
        int sz = field.getFieldSize();
        for (int i = 0; i < sz; ++i) {
            for (int j = 0; j < sz; ++j) {
                int m = i * sz + j;
                field.setFieldLetter(i, j, (preferences.getString(
                        getString(R.string.fieldLetter) + m, "-")).charAt(0));
            }
        }
        n = preferences.getInt(getString(R.string.dictSize), 0);
        Set<String> dict = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            dict.add(preferences.getString(getString(R.string.dict) + i, ""));
        }
        field.setDict(dict);
        Set<Character> set1 = new HashSet<>();
        Set<Character> set2 = new HashSet<>();
        int handSize1 = preferences.getInt("player1Num", 0);
        int handSize2 = preferences.getInt("player2Num", 0);
        for (int i = 0; i < handSize1; ++i) {
            set1.add(preferences.getString("player1" + i, "-").charAt(0));
        }
        for (int i = 0; i < handSize2; ++i) {
            set2.add(preferences.getString("player2" + i, "-").charAt(0));
        }
        players[0].setHand(set1);
        players[1].setHand(set2);
        players[0].setScore(preferences.getInt("score1", 0));
        players[1].setScore(preferences.getInt("score2", 0));
        scores[0].setText(String.format(Locale.getDefault(), "%d", players[0].getScore()));
        scores[1].setText(String.format(Locale.getDefault(), "%d", players[1].getScore()));
        if (turn == 0) {
            turn = 1;
        }
        else {
            turn = 0;
        }
        showDialog();
    }

    @Override
    protected void onPause() {
        save();
        super.onPause();
    }
}