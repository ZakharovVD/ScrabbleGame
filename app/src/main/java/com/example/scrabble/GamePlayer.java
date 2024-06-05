package com.example.scrabble;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GamePlayer implements View.OnClickListener {

    private boolean isComputer;
    private Set<GameLetter> hand;
    private int score;
    private int cellSize;
    private int margin;
    private LinearLayout handLayout;
    private Context context;
    private ImageView clickedImage;
    private Map<ImageView, GameLetter> imageMap;
    private Set<ImageView> removedImages;
    private boolean changeMode;
    private Set<ImageView> changedImage;

    public GamePlayer(LinearLayout handLayout, Context context, int width) {

        System.out.println("constructor");

        isComputer = false;
        this.handLayout = handLayout;
        this.context = context;
        changeMode = false;
        cellSize = ((width) / 9);
        margin = (cellSize / 7);
        hand = new HashSet<>();
        score = 0;
        imageMap = new HashMap<>();
        removedImages = new HashSet<>();
        changedImage = new HashSet<>();
    }

    public GamePlayer() {

        System.out.println("constructorEmpty");

        isComputer = true;
        hand = new HashSet<>();
        score = 0;
        imageMap = new HashMap<>();
        removedImages = new HashSet<>();
        changedImage = new HashSet<>();
        changeMode = false;
    }

    private void setClickedImage(ImageView view) {

        System.out.println("setClicked");

        clickedImage = view;
    }

    private void resetClickedImage() {

        System.out.println("reset");

        clickedImage = null;
    }

    @Override
    public void onClick(View view) {

        System.out.print("onClick");
        System.out.println(changeMode);

        if (!changeMode) {
            if (clickedImage != null) {
                clickedImage.animate().scaleX(1f).scaleY(1f).setDuration(500);
            }

            if (clickedImage == null || !clickedImage.equals(view)) {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500);
                setClickedImage((ImageView) view);
            } else {
                resetClickedImage();
            }
        }
        else {
            // Already have clicked image
            if (changedImage.contains((ImageView) view)) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(500);
                changedImage.remove((ImageView) view);
            }
            else {
                view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500);
                changedImage.add((ImageView) view);
            }
        }
    }

    public GameLetter getClickedLetter() {

        System.out.println("getClicked");

        if (clickedImage == null) {
            return null;
        }
        return imageMap.get(clickedImage);
    }

    public void removeClickedImageFromHand() {

        System.out.println("removeClickedFromHand");

        clickedImage.animate().scaleX(1f).scaleY(1f).setDuration(0);
        handLayout.removeView(clickedImage);
        hand.remove(imageMap.get(clickedImage));
        removedImages.add(clickedImage);
        resetClickedImage();
    }

    public void backLettersFromField() {

        System.out.println("backFromField");

        if (removedImages == null) {
            return;
        }
        for (ImageView item : removedImages) {
            handLayout.addView(item);
        }
        removedImages.clear();
    }

    public void setChangeMode() {


        changeMode = !changeMode;
        System.out.print("setMode ");
        System.out.println(changeMode);
    }

    public boolean isChangeMode() {

        System.out.println("checkMode");

        return changeMode;
    }

    public Set<GameLetter> removeChangedImages() {

        System.out.println("removeChangedImages");

        Set<GameLetter> backLetter = new HashSet<>();
        for (ImageView item : changedImage) {
            handLayout.removeView(item);
            hand.remove(imageMap.get(item));
            backLetter.add(imageMap.get(item));
        }
        changedImage.clear();
        return backLetter;
    }

    public int getNumChange() {

        System.out.println("getNum");

        return changedImage.size();
    }


    public void drawHand() {

        System.out.println("drawHand");

        handLayout.removeAllViews();
        for (GameLetter item : hand) {
            ImageView imageLetter = new ImageView(context);
            imageMap.put(imageLetter, item);
            imageLetter.setImageResource(item.getImageId());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cellSize, cellSize);
            layoutParams.setMargins(margin, 2 * margin, margin, 2 * margin);
            imageLetter.setLayoutParams(layoutParams);
            imageLetter.setOnClickListener(this);
            handLayout.addView(imageLetter);
        }
    }

    public Integer getScore() {

        System.out.println("getScore");

        return score;
    }

    public void setScore(int score) {

        System.out.println("setScore");

        this.score = score;
    }

    public int getHandSize() {

        System.out.println("getHandSize");

        return hand.size();
    }


    public void addLetter(GameLetter lt) {

        System.out.println("addLetter");

        hand.add(lt);
    }

    public void addPoints(int p) {

        System.out.println("addPoints");

        score += p;
    }

    public Set<GameLetter> getHand() {

        System.out.println("getHand");

        return hand;
    }

    public void setHand(Set<Character> set) {

        System.out.println("setHand");

        hand.clear();
        for (Character c : set) {
            hand.add(new GameLetter(c));
        }
    }
}
