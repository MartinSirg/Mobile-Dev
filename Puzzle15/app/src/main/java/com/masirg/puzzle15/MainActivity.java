package com.masirg.puzzle15;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    static private final String TAG = MainActivity.class.getSimpleName();
    private PuzzleController mController;
    private Map<Integer, Integer> drawables;
    private View[][] views;
    private boolean showingConfirm = false;
    private ScheduledExecutorService mScheduledExecutorService;

    final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
    private TextView timerTextView;
    private TextView currentMovesTextView;
    private TextView bestMovesTextView;
    private TextView isSolvedTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_container);
        mController = PuzzleController.getInstance();
        timerTextView = findViewById(R.id.textViewTimer);
        currentMovesTextView = findViewById(R.id.textViewMoves);
        bestMovesTextView = findViewById(R.id.textViewBestMoves);
        isSolvedTextView = findViewById(R.id.textViewSolved);
        initData();
        updateTiles();
        updateStats();
    }

    private void updateStats() {
        updateTimer();
        updateMoves();
        updateBestMoves();
        updateIsSolved();
    }

    private void updateIsSolved() {
        if (mController.isSolved) isSolvedTextView.setVisibility(View.VISIBLE);
        else isSolvedTextView.setVisibility(View.INVISIBLE);
    }


    public void tileClick(View view) throws Exception {
//        if(!mController.isStarted){
//            Snackbar.make(view, "Press randomize to start", Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show();
//        }
        // Works out row and col index of the view clicked
        int row = -1, col = -1;
        for (int i = 0; i < views.length; i++) {
            for (int j = 0; j < views[i].length; j++) {
                if (view == views[i][j]) {
                    row = i;
                    col = j;
                    break;
                }
            }
        }
        if (row == -1) throw new Exception("View is not added to views list");

        int outcome  = mController.tileClicked(row, col);
        switch (outcome) {
            case C.GAME_ALREADY_SOLVED:
                showPopupWindowNotify(view, getString(R.string.errorAlreadySolved));
                break;
            case C.SOLUTION_TOGGLED:
                showPopupWindowNotify(view, getString(R.string.errorSolutionToggled));
                break;
            case C.NOT_STARTED:
                showPopupWindowNotify(view, getString(R.string.errorNotStarted));
                break;
            case C.CLICKED_ON_EMPTY_TILE:
                showPopupWindowNotify(view, getString(R.string.errorClickedEmptyTile));
                break;
            case C.CLICKED_OFF_BOTH_VALID_AXES:
                showPopupWindowNotify(view, getString(R.string.errorClickedOffAxes));
                break;
            case C.GAME_SOLVED:
                updateBestMoves();
                updateIsSolved(); //intentional no break
            case C.TILES_MOVED:
                updateTiles();
                updateMoves();
                break;
        }
    }

    private void updateMoves() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(String.format("Moves: %d", mController.currentMoves));
        sb.setSpan(bold, 0,6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        currentMovesTextView.setText(sb);
    }

    private void updateBestMoves(){
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.clear();
        sb.append(String.format("Best moves: %d", mController.bestMoves));
        sb.setSpan(bold, 0, 11, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        bestMovesTextView.setText(sb);

        if (!mController.isStarted){
            if(mScheduledExecutorService != null) mScheduledExecutorService.shutdown();
        }
    }

    public void showPopupWindowConfirm(View view) {
        showingConfirm = true;
        // inflate the layout of the popup window

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_confirm, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Button yesButton = popupWindow.getContentView().findViewById(R.id.confirmButtonYes);
        Button noButton = popupWindow.getContentView().findViewById(R.id.confirmButtonNo);

        yesButton.setOnClickListener((v) -> {
            popupWindow.dismiss();
            randomizeTiles();
            showingConfirm = false;
        });

        noButton.setOnClickListener((v) -> {
            popupWindow.dismiss();
            showingConfirm = false;
        });
    }

    public void showPopupWindowNotify(View view, String message) {

        // inflate the layout of the popup window

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_error, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        ((TextView) popupWindow.getContentView().findViewById(R.id.textViewPopUpTextField)).setText(message);


        // dismiss the popup window when touched
        popupView.setOnClickListener((v) -> popupWindow.dismiss());
    }

    public void randomizeButtonClick(View view){
        if (mController.solutionToggled) showPopupWindowNotify(view, getString(R.string.errorSolutionToggled));
        else if (mController.isStarted) showPopupWindowConfirm(view);
        else randomizeTiles();
    }

    private void randomizeTiles(){
        mController.randomize();
        updateTiles();
        updateMoves();
        startTimer();
        updateIsSolved();
    }

    public void solutionButtonClick(View view){
        if (mController.isSolved) showPopupWindowNotify(view, getString(R.string.errorAlreadySolved));
        else if (!mController.isStarted) showPopupWindowNotify(view, getString(R.string.errorNotStarted));
        else{
            mController.toggleSolution();
            updateTiles();
        }
    }

    private void updateTiles() {
        Log.d(TAG, "Updating all tiles");
        //Tile's current position is held in mController.tiles
        //A tile's corresponding drawable is held in drawables hashmap


        for (int i = 0; i < mController.tiles.length; i++) {
            for (int j = 0; j < mController.tiles[i].length; j++) {
                ImageView view = (ImageView) views[i][j];
                int tileValue = mController.tiles[i][j];

                view.setImageResource(drawables.get(tileValue));
                ;
                view.setBackground(ContextCompat.getDrawable(getApplicationContext(), tileValue == 16 ? R.color.colorEmptyTile : R.color.filledTile));
            }
        }
    }

    private void startTimer(){
        if (mScheduledExecutorService != null)mScheduledExecutorService.shutdown();
        mScheduledExecutorService = Executors.newScheduledThreadPool(5);
        mScheduledExecutorService.scheduleAtFixedRate(() ->{
            mController.currentTimer++;
            updateTimer();
        },0,1, TimeUnit.SECONDS);
    }

    private void updateTimer(){
        runOnUiThread(() ->{
            try{
                SpannableStringBuilder sb = new SpannableStringBuilder();
                sb.append(String.format("Timer:  %02d:%02d", mController.currentTimer / 60, mController.currentTimer % 60));
                sb.setSpan(bold, 0, 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                timerTextView.setText(sb);
            } catch (Exception e){
                Log.d(TAG, "EXCEPTION: " + e.getMessage());
            }
        });
    }

    // ============================== LIFECYCLE EVENTS ===============================

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        updateMoves();
        updateBestMoves();
        if (mController.isStarted) startTimer();
        updateTimer();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if(mScheduledExecutorService != null) mScheduledExecutorService.shutdown();
        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void initData() {
        drawables = new HashMap<>();
        drawables.put(1,R.drawable.sadaharu1);
        drawables.put(2,R.drawable.sadaharu2);
        drawables.put(3,R.drawable.sadaharu3);
        drawables.put(4,R.drawable.sadaharu4);
        drawables.put(5,R.drawable.sadaharu5);
        drawables.put(6,R.drawable.sadaharu6);
        drawables.put(7,R.drawable.sadaharu7);
        drawables.put(8,R.drawable.sadaharu8);
        drawables.put(9,R.drawable.sadaharu9);
        drawables.put(10,R.drawable.sadaharu10);
        drawables.put(11,R.drawable.sadaharu11);
        drawables.put(12,R.drawable.sadaharu12);
        drawables.put(13,R.drawable.sadaharu13);
        drawables.put(14,R.drawable.sadaharu14);
        drawables.put(15,R.drawable.sadaharu15);
        drawables.put(16,R.color.colorEmptyTile);

        views = new View[][]{
                {findViewById(R.id.board1_1), findViewById(R.id.board1_2), findViewById(R.id.board1_3), findViewById(R.id.board1_4)},
                {findViewById(R.id.board2_1), findViewById(R.id.board2_2), findViewById(R.id.board2_3), findViewById(R.id.board2_4)},
                {findViewById(R.id.board3_1), findViewById(R.id.board3_2), findViewById(R.id.board3_3), findViewById(R.id.board3_4)},
                {findViewById(R.id.board4_1), findViewById(R.id.board4_2), findViewById(R.id.board4_3), findViewById(R.id.board4_4)},
        };






    }
}
