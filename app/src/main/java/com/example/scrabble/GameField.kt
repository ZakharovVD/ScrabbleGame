package com.example.scrabble;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.core.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GameField {

    private final int fieldSize = 15;
    private final int cellSize;
    //Actual field. values:
    // 0 - empty cell
    // 1 - newly added letter
    // 2 - newly added letter but not counted in score
    // 3 - permanent correct letter or set by computer
    private final int[][] field = new int[fieldSize][fieldSize];
    // Field of GameLetter objects
    private final GameLetter[][] fieldLetter = new GameLetter[fieldSize][fieldSize];
    //Field to be drawn
    private int[][] fieldImage = new int[fieldSize][fieldSize];
    private GridLayout gridField;
    private Context context;
    private View.OnClickListener onClickListener;
    // Fast way of getting coordinates
    private Map<Button, Pair<Integer, Integer>> buttonCoordinates = new HashMap<>();
    private Set<String> dict;
    private List<String> curWords;
    private int points;
    private String addWord;
    private Set<String> removeWords;
    private Set<String> myDict;

    private void setImageId(int row, int col, int id) {
        fieldImage[row][col] = id;
        fieldImage[row][fieldSize - col - 1] = id;
        fieldImage[fieldSize - row - 1][col] = id;
        fieldImage[fieldSize - row - 1][fieldSize - col - 1] = id;
    }

    private void drawField() {
        for (int i = 0; i < fieldSize; ++i) {
            for (int j = 0; j < fieldSize; ++j) {
                GridLayout.Spec row = GridLayout.spec(i);
                GridLayout.Spec col = GridLayout.spec(j);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(row, col);
                layoutParams.width = cellSize;
                layoutParams.height = cellSize;
                Button buttonCell = new Button(context);
                buttonCell.setId(i * fieldSize + j);
                buttonCell.setBackgroundResource(fieldImage[i][j]);
                buttonCell.setLayoutParams(layoutParams);
                buttonCell.setOnClickListener(onClickListener);
                gridField.addView(buttonCell);
                buttonCoordinates.put(buttonCell, new Pair<>(i, j));
            }
        }
        String startWord = getStartWord();
        int n = startWord.length() / 2;
        int col = fieldSize / 2 - n;
        int row = fieldSize / 2;
        for (int i = 0; i < startWord.length(); ++i, ++col) {
            Button btn = gridField.findViewById(row * fieldSize + col);
            GameLetter letter = new GameLetter(startWord.charAt(i));
            btn.setBackgroundResource(letter.getImageId());
            field[row][col] = 3;
            fieldLetter[row][col] = letter;
        }

    }

    public GameField(Context context, GridLayout gridField, View.OnClickListener onClickListener,
                     int width) {

        curWords = new ArrayList<>();
        dict = new HashSet<>();
        myDict = new HashSet<>();
        removeWords = new HashSet<>();
        try {
            InputStream is = context.getAssets().open(context.getString(R.string.fileName));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String word;
            while ((word = reader.readLine()) != null) {
                if (word.length() <= 15) {
                    dict.add(word);
                }
            }
            is.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.gridField = gridField;
        this.context = context;
        this.onClickListener = onClickListener;
        cellSize = (int) (width * 1.0 / fieldSize + 0.5);
        for (int row = 0; row < fieldSize; ++row) {
            for (int col = 0; col < fieldSize; ++col) {
                fieldImage[row][col] = R.drawable.cell;
                fieldLetter[row][col] = new GameLetter();
                field[row][col] = 0;
            }
        }
        fieldImage[fieldSize / 2][fieldSize / 2] = R.drawable.start;
        setImageId(0, 0, R.drawable.w3);
        setImageId(0, fieldSize / 2, R.drawable.w3);
        setImageId(fieldSize / 2, 0, R.drawable.w3);
        for (int i = 1; i < 5; ++i) {
            setImageId(i, i, R.drawable.w2);
        }
        setImageId(0, 5, R.drawable.l3);
        setImageId(5, 0, R.drawable.l3);
        setImageId(5, 5, R.drawable.l3);
        setImageId(0, 3, R.drawable.l2);
        setImageId(3, 0, R.drawable.l2);
        setImageId(2, fieldSize / 2 - 1, R.drawable.l2);
        setImageId(3, fieldSize / 2, R.drawable.l2);
        setImageId(fieldSize / 2 - 1, 2, R.drawable.l2);
        setImageId(fieldSize / 2, 3, R.drawable.l2);
        setImageId(fieldSize / 2 - 1, fieldSize / 2 - 1, R.drawable.l2);
        drawField();
    }

    public boolean cellEmpty(Button btn) {
        Pair<Integer, Integer> crd = buttonCoordinates.get(btn);
        return field[crd.first][crd.second] == 0;
    }

    public void addLetter(Button btn, GameLetter letter) {
        Pair<Integer, Integer> crd = buttonCoordinates.get(btn);
        field[crd.first][crd.second] = 1;
        fieldLetter[crd.first][crd.second] = letter;
    }
    
    public void resetLetters() {
        for (Map.Entry<Button, Pair<Integer, Integer>> item : buttonCoordinates.entrySet()) {
            int i = item.getValue().first;
            int j = item.getValue().second;
            if (field[i][j] < 3) {
                item.getKey().setBackgroundResource(fieldImage[i][j]);
                field[i][j] = 0;
                fieldLetter[i][j] = new GameLetter();
            }
        }
    }

    private String  getStartWord() {
        int idx = new Random().nextInt(dict.size());
        int i = 0;
        String word = "";
        for (String item : dict) {
            if (i == idx) {
                word = item;
                dict.remove(item);
                break;
            }
            ++i;
        }
        return word;
    }
    //Multiply letter
    private int ltrPoints(int i, int j) {
        int p = fieldLetter[i][j].getPoints();
        if (fieldImage[i][j] == R.drawable.l2) {
            p *= 2;
        }
        if(fieldImage[i][j] == R.drawable.l3) {
            p *= 3;
        }
        return p;
    }
    //Multiply whole word
    private int kPoints(int i, int j) {
        int k = 1;
        if (fieldImage[i][j] == R.drawable.w2) {
            k *= 2;
        }
        if (fieldImage[i][j] == R.drawable.w3) {
            k *= 3;
        }
        return k;
    }
    // Check words correctness and updates score
    public String checkWordsInDict() {
        points = 0;
        curWords.clear();
        int wordPoints;
        int k;
        String w;
        boolean newWord;
        // Count columns down
        // Iterate over each row
        for (int i = 0; i < fieldSize; ++i) {
            w = "";
            newWord = false;
            wordPoints = 0;
            k = 1;
            // Iterate over each column
            for (int j = 0; j < fieldSize; ++j) {
                if (field[i][j] == 0) {
                    // Incorrect word or not a new word
                    if (w.length() <= 1 || !newWord) {
                        w = "";
                        wordPoints = 0;
                        newWord = false;
                        k = 1;
                        continue;
                    }
                    // Correct new word
                    if (dict.contains(w)) {
                        curWords.add(w);
                        points += wordPoints * k;
                    }
                    // Incorrect word. Show Dialog
                    else {
                        addWord = w;
                        return w;
                    }
                    w = "";
                    wordPoints = 0;
                    newWord = false;
                    k = 1;
                    continue;
                }
                // Base counting
                w += fieldLetter[i][j].getLetter();
                int p = ltrPoints(i, j);
                wordPoints += p;
                k *= kPoints(i, j);
                if (field[i][j] == 2) {
                    newWord = true;
                }
            }
            // Check after column iteration
            if (w.length() > 1 && newWord) {
                if (dict.contains(w)) {
                    curWords.add(w);
                    points += wordPoints * k;
                }
                else {
                    addWord = w;
                    return w;
                }
            }
        }
        // Iterate rows to right
        // Same logic but for rows
        for (int j = 0; j < fieldSize; ++j) {
            w = "";
            newWord = false;
            wordPoints = 0;
            k = 1;
            for (int i = 0; i < fieldSize; ++i) {
                if (field[i][j] == 0) {
                    if (w.length() <= 1 || !newWord) {
                        w = "";
                        wordPoints = 0;
                        newWord = false;
                        k = 1;
                        continue;
                    }
                    if (dict.contains(w)) {
                        curWords.add(w);
                        points += wordPoints * k;
                    }
                    else {
                        addWord = w;
                        return w;
                    }
                    w = "";
                    wordPoints = 0;
                    newWord = false;
                    k = 1;
                    continue;
                }
                w += fieldLetter[i][j].getLetter();
                int p = ltrPoints(i, j);
                wordPoints += p;
                k *= kPoints(i, j);
                if (field[i][j] == 2) {
                    newWord = true;
                }
            }
            if (w.length() > 1 && newWord) {
                if (dict.contains(w)) {
                    curWords.add(w);
                    points += wordPoints * k;
                }
                else {
                    addWord = w;
                    return w;
                }
            }
        }
        return null;
    }

    // Mark cells as newly approved
    // For those, who are set on this term, but not counted in score
    private void updateCell(int i, int j) {
        if (i - 1 >= 0 && field[i - 1][j] == 1) {
            field[i - 1][j] = 2;
            updateCell(i - 1, j);
        }
        if (i + 1 < fieldSize && field[i + 1][j] == 1) {
            field[i + 1][j] = 2;
            updateCell(i + 1, j);
        }
        if (j - 1 >= 0 && field[i][j - 1] == 1) {
            field[i][j - 1] = 2;
            updateCell(i, j - 1);
        }
        if (j + 1 < fieldSize && field[i][j + 1] == 1) {
            field[i][j + 1] = 2;
            updateCell(i, j + 1);
        }
    }

    public boolean checkConnects() {
        for (int i = 0; i < fieldSize; ++i) {
            for (int j = 0; j < fieldSize; ++j) {
                // Permanent letter
                if (field[i][j] == 3) {
                    updateCell(i, j);
                }
            }
        }
        for (int i = 0; i < fieldSize; ++i) {
            for (int j = 0; j < fieldSize; ++j) {
                // If left newly added
                if (field[i][j] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getPoints() {
        return points;
    }

    public void updateField() {
        for (String w : curWords) {
            dict.remove(w);
        }
        curWords.clear();
        for (int i = 0; i < fieldSize; ++i) {
            for (int j = 0; j < fieldSize; ++j) {
                if (field[i][j] == 2) {
                    field[i][j] = 3;
                }
            }
        }
    }

    public void addInDict() {
        dict.add(addWord);
        myDict.add(addWord);
    }

    public void computerMode(Set<GameLetter> letters) {
        for (GameLetter l : letters) {
            Log.d("comp", "" + l.getLetter());
        }
        points = 0;

        findInRow(letters);
        findInCol(letters);

        Log.d("comp", "fin");
    }

    private boolean findInRow(Set<GameLetter> letters) {
        boolean find = false;
        // Iterate over each row
        for (int i = 0; i < fieldSize; ++i) {
            // Iterate over each column in the row

            for (int j = 0; j < fieldSize; ++j) {
                // Check if the current cell is empty and the cell to the left is not

                if (field[i][j] == 0 && j - 1 >= 0 && field[i][j - 1] > 0) {
                    int j1 = j - 1;
                    String begin = "" + fieldLetter[i][j1].getLetter();
                    // Build the string to the left from the current position

                    while (j1 > 0 && field[i][j1 - 1] > 0) {
                        --j1;
                        begin = fieldLetter[i][j1].getLetter() + begin;
                    }
                    for (String w : dict) {
                        // Skip if the word doesn't fit in the row

                        if (j1 + w.length() > fieldSize || (j1 + w.length() != fieldSize
                                && field[i][j1 + w.length()] > 0)) {
                            continue;
                        }
                        Set<Character> copySet = new HashSet<>();
                        for (GameLetter gl : letters) {
                            copySet.add(gl.getLetter());
                        }
                        // Check if the beginning of the word matches the string built from the board

                        if (w.length() > j - j1 && w.substring(0, j - j1).equals(begin)) {
                            if (!checkMaskRow(w, copySet, i, j, j1)) {
                                continue;
                            }
                        } else {
                            continue;
                        }

                        boolean fit = checkConnectWordsRow(w, i, j1);

                        if (fit) {
                            Log.d("comp", w);
                            setComputerWordRow(letters, copySet, w, i, j1);
                            find = true;
                            break;
                        }
                    }
                }
                // Handle the scenario where the current cell is empty and either the left cell is empty
                // or out of bounds, and the right cell is not empty

                if (!find && field[i][j] == 0 && (j - 1 < 0 || field[i][j - 1] == 0)
                        && j + 1 <fieldSize && field[i][j + 1] > 0) {
                    // Try placing a word starting at different positions to the left of the current cell

                    for (int pref = 0; j - pref >= 0 && field[i][j - pref] == 0; pref++) {
                        for (String w : dict) {
                            Set<Character> copySet = new HashSet<>();
                            for (GameLetter gl : letters) {
                                copySet.add(gl.getLetter());
                            }

                            int j1 = j - pref;
                            if (j1 + w.length() > fieldSize || (j1 + w.length() != fieldSize
                                    && field[i][j1 + w.length()] > 0)) {
                                continue;
                            }
                            if (w.length() > pref + 1) {
                                if (!checkMaskRow(w, copySet, i, j1, j1)) {

                                    continue;
                                }
                            } else {
                                continue;
                            }

                            boolean fit = checkConnectWordsRow(w, i, j1);

                            if (fit) {
                                Log.d("comp", w);
                                setComputerWordRow(letters, copySet, w, i, j1);
                                find = true;
                                break;
                            }
                        }
                        if (find) {
                            break;
                        }
                    }
                }
            }
        }
        return find;
    }

    private boolean checkMaskRow(String w, Set<Character> copySet, int i, int j, int j1) {
        for (int s = j - j1; s < w.length(); ++s) {
            int idx = s + j1;
            // Check if there is a letter on hands
            if (field[i][idx] == 0 && !copySet.contains(w.charAt(s))) {
                return false;
            }
            // Check if the letter on field is a letter in word we are creating
            if (field[i][idx] > 0
                    && fieldLetter[i][idx].getLetter() != w.charAt(s)) {
                return false;
            }
            if (field[i][idx] == 0) {
                copySet.remove(w.charAt(s));
            }
        }
        return true;
    }
    // Check validity of additionally added words vertically
    private boolean checkConnectWordsRow(String w, int i, int j1) {
        removeWords.clear();
        for (int s = 0; s < w.length(); ++s) {
            if (field[i][j1 + s] == 0) {
                String w2 = "" + w.charAt(s);
                int i1 = i - 1;
                // Searching up
                while (i1 >= 0 && field[i1][j1 + s] > 0) {
                    w2 = fieldLetter[i1][j1 + s].getLetter() + w2;
                    --i1;
                }
                i1 = i + 1;
                // Searching down
                while (i1 < fieldSize && field[i1][j1 + s] > 0) {
                    w2 += fieldLetter[i1][j1 + s].getLetter();
                    ++i1;
                }
                if (w2.length() > 1) {
                    if (dict.contains(w2) && !removeWords.contains(w2)) {
                        removeWords.add(w2);
                    }
                    else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void setComputerWordRow(Set<GameLetter> letters, Set<Character> copySet, String w,
                                    int i, int j1) {
        int wordPoints = 0;
        int k = 1;
        for (int s = 0; s < w.length(); ++s) {
            if (field[i][j1 + s] == 0) {
                field[i][j1 + s] = 3;
                GameLetter ltr = new GameLetter(w.charAt(s));
                copySet.remove(w.charAt(s));
                fieldLetter[i][j1 + s] = ltr;
                Button btn = gridField.findViewById(i * fieldSize + j1 + s);
                btn.setBackgroundResource(ltr.getImageId());
            }
            wordPoints += ltrPoints(i, j1 + s);
            k *= kPoints(i, j1 + s);
        }
        points += wordPoints * k;
        letters.clear();
        for (Character c : copySet) {
            letters.add(new GameLetter(c));
        }
        dict.remove(w);
        for (String s : removeWords) {
            dict.remove(s);
        }
        removeWords.clear();
    }

    private boolean findInCol(Set<GameLetter> letters) {
        boolean find = false;
        // Iterate over each column
        for (int j = 0; j < fieldSize; ++j) {
            // Iterate over each row in the column
            for (int i = 0; i < fieldSize; ++i) {
                // Check if the current cell is empty and the cell above it is not
                if (field[i][j] == 0 && i - 1 >= 0 && field[i - 1][j] > 0) {
                    int i1 = i - 1;
                    String begin = "" + fieldLetter[i1][j].getLetter();
                    // Build the string upwards from the current position
                    while (i1 > 0 && field[i1 - 1][j] > 0) {
                        --i1;
                        begin = fieldLetter[i1][j].getLetter() + begin;
                    }
                    // Check each word in the dictionary
                    for (String w : dict) {
                        // Check if fits in column
                        if (i1 + w.length() > fieldSize || (i1 + w.length() != fieldSize
                                && field[i1 + w.length()][j] > 0)) {
                            continue;
                        }
                        Set<Character> copySet = new HashSet<>();
                        for (GameLetter gl : letters) {
                            copySet.add(gl.getLetter());
                        }
                        // Check if the beginning of the word matches the string built from the board
                        if (w.length() > i - i1 && w.substring(0, i - i1).equals(begin)) {
                            if (checkMaskCol(w, copySet, i, j, i1)) {
                                continue;
                            }
                        } else {
                            continue;
                        }

                        boolean fit = checkConnectWordsCol(w, i1, j);
                        // If the word fits, update the game board and return true
                        if (fit) {
                            Log.d("comp", w);
                            setComputerWordCol(letters, copySet, w, i1, j);
                            find = true;
                            break;
                        }
                    }
                }
                // Handle the scenario where the current cell is empty and either the above cell
                // is empty or out of bounds, and the cell below is not empty
                if (!find && field[i][j] == 0 && (i - 1 < 0 || field[i - 1][j] == 0)
                        && i + 1 < fieldSize && field[i + 1][j] > 0) {
                    // Try placing a word starting at different positions above the current cell
                    for (int pref = 0; i - pref >= 0 && field[i - pref][j] == 0; pref++) {
                        for (String w : dict) {
                            Set<Character> copySet = new HashSet<>();
                            for (GameLetter gl : letters) {
                                copySet.add(gl.getLetter());
                            }

                            int i1 = i - pref;
                            if (i1 + w.length() > fieldSize || (i1 + w.length() != fieldSize
                                    && field[i1 + w.length()][j] > 0)) {
                                continue;
                            }
                            if (w.length() > pref + 1) {
                                if (checkMaskCol(w, copySet, i1, j, i1)) {
                                    continue;
                                }
                            } else {
                                continue;
                            }

                            boolean fit = checkConnectWordsCol(w, i1, j);
                            // If the word fits, update the game board and return true
                            if (fit) {
                                Log.d("comp", w);
                                setComputerWordCol(letters, copySet, w, i1, j);
                                find = true;
                                break;
                            }
                        }
                        if (find) {
                            break;
                        }
                    }
                }
            }
        }
        return find;
    }

    private boolean checkMaskCol(String w, Set<Character> copySet, int i, int j, int i1) {
        for (int s = i - i1; s < w.length(); ++s) {
            int idx = s + i1;
            // Check if there is a letter on hands
            if (field[idx][j] == 0 && !copySet.contains(w.charAt(s))) {
                return true;
            }
            // Check if the letter on field is a letter in word we are creating
            if (field[idx][j] > 0
                    && fieldLetter[idx][j].getLetter() != w.charAt(s)) {
                return true;
            }
            if (field[idx][j] == 0) {
                copySet.remove(w.charAt(s));
            }
        }
        return false;
    }
    // Check validity of additionally added words horizontally
    private boolean checkConnectWordsCol(String w, int i1, int j) {
        removeWords.clear();
        for (int s = 0; s < w.length(); ++s) {
            if (field[i1 + s][j] == 0) {
                String w2 = "" + w.charAt(s);
                int j1 = j - 1;
                // Searching left
                while (j1 >= 0 && field[i1 + s][j1] > 0) {
                    w2 = fieldLetter[i1 + s][j1].getLetter() + w2;
                    --i1;
                }
                j1 = j + 1;
                // Searching right
                while (j1 < fieldSize && field[i1 + s][j1] > 0) {
                    w2 += fieldLetter[i1 + s][j1].getLetter();
                    ++i1;
                }
                if (w2.length() > 1) {
                    if (dict.contains(w2) && !removeWords.contains(w2)) {
                        removeWords.add(w2);
                    }
                    else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void setComputerWordCol(Set<GameLetter> letters, Set<Character> copySet, String w, int i1, int j) {
        int wordPoints = 0;
        int k = 1;
        for (int s = 0; s < w.length(); ++s) {
            if (field[i1 + s][j] == 0) {
                field[i1 + s][j] = 3;
                GameLetter ltr = new GameLetter(w.charAt(s));
                copySet.remove(w.charAt(s));
                fieldLetter[i1 + s][j] = ltr;
                Button btn = gridField.findViewById((i1 + s) * fieldSize + j);
                btn.setBackgroundResource(ltr.getImageId());
            }
            wordPoints += ltrPoints(i1 + s, j);
            k *= kPoints(i1 + s, j);
        }
        points += wordPoints * k;
        letters.clear();
        for (Character c : copySet) {
            letters.add(new GameLetter(c));
        }
        dict.remove(w);
        for (String s : removeWords) {
            dict.remove(s);
        }
        removeWords.clear();
    }

    public int getFieldSize() {
        return fieldSize;
    }

    public char getFieldLetter(int i, int j) {
        return fieldLetter[i][j].getLetter();
    }

    public void setFieldLetter(int i, int j, char c) {
        Button btn = gridField.findViewById(i * fieldSize + j);
        if (c == '-') {
            field[i][j] = 0;
            fieldLetter[i][j] = new GameLetter();
            btn.setBackgroundResource(fieldImage[i][j]);
        }
        else {
            field[i][j] = 3;
            fieldLetter[i][j] = new GameLetter(c);
            btn.setBackgroundResource(fieldLetter[i][j].getImageId());
        }
    }

    public Set<String> getDict() {
        return dict;
    }

    public void setDict(Set<String> s) {
        for(String item : s) {
            dict.add(item);
        }
    }


    public void clearDict() {
        dict.clear();
    }

    public Set<String> getMyDict() {
        return myDict;
    }


    public void setMyDict(Set<String> s) {
        myDict.clear();
        for (String item : s) {
            myDict.add(item);
            dict.add(item);
        }
    }

}
