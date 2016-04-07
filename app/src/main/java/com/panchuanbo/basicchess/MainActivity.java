// Chuanbo Pan <chuanbo@stanford.edu>
// BasicChess - Allows two people to play a game of chess!
// Doesn't have any AI implementation, check/checkmate detection
// an En Passant (hence the "Simple").
// Has all the other basic features of the game of chess.

package com.panchuanbo.basicchess;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final int WHITE_COLOR = Color.rgb(255, 255, 255);
    private final int GRAY_COLOR = Color.rgb(128, 128, 128);
    private final int BLUE_COLOR = Color.rgb(100, 149, 237);
    private final int UP_PAWN_HOME = 6;
    private final int DOWN_PAWN_HOME = 1;

    private GridLayout gridLayout;

    private Point start = null, end = null;

    private Boolean drewInitialBoard = false;
    private String currentTurn = "W";

    private final String[][] initialLayout = {
            {"Brk", "Bkn", "Bbi", "Bqn", "Bki", "Bbi", "Bkn", "Brk"},
            {"Bp-d", "Bp-d", "Bp-d", "Bp-d", "Bp-d", "Bp-d", "Bp-d", "Bp-d"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"Wp-u", "Wp-u", "Wp-u", "Wp-u", "Wp-u", "Wp-u", "Wp-u", "Wp-u"},
            {"Wrk", "Wkn", "Wbi", "Wqn", "Wki", "Wbi", "Wkn", "Wrk"},
    };

    String[][] currentLayout;

    private Map<String, Integer> pieceImage = new HashMap<>();

    private ImageButton[][] imageButtonLayout = new ImageButton[8][8];

    private LinearLayout pawnPromotionView;
    private Boolean pawnPromotion = false;
    private String promotionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMap();

        gridLayout = (GridLayout)findViewById(R.id.board);
        pawnPromotionView = (LinearLayout)findViewById(R.id.pawnPromotionView);
    }

    private void setupMap() {
        pieceImage.put("Wbi", R.drawable.bishop_w);
        pieceImage.put("Bbi", R.drawable.bishop_b);

        pieceImage.put("Wki", R.drawable.king_w);
        pieceImage.put("Bki", R.drawable.king_b);

        pieceImage.put("Wkn", R.drawable.knight_w);
        pieceImage.put("Bkn", R.drawable.knight_b);

        pieceImage.put("Wp-u", R.drawable.pawn_w);
        pieceImage.put("Bp-u", R.drawable.pawn_b);
        pieceImage.put("Wp-d", R.drawable.pawn_w);
        pieceImage.put("Bp-d", R.drawable.pawn_b);

        pieceImage.put("Wqn", R.drawable.queen_w);
        pieceImage.put("Bqn", R.drawable.queen_b);

        pieceImage.put("Wrk", R.drawable.rook_w);
        pieceImage.put("Brk", R.drawable.rook_b);
    }

    private void beginPawnPromotion() {
        Button promotionButton = ((Button) findViewById(R.id.promotePawnButton));
        promotionButton.setTag(new Point(end.x, end.y));
        promotionType = currentTurn;
        pawnPromotionView.setVisibility(View.VISIBLE);
        pawnPromotion = true;
    }

    private Boolean validMovePawnUp() {
        if (end.x - start.x > 0 || end.x - start.x < -2) return false;
        if (end.x - start.x == -2 && start.x != UP_PAWN_HOME) return false;
        if (Math.abs(end.y - start.y) > 1 ||
                (Math.abs(end.y - start.y) == 1 && (end.x - start.x == 0 || Math.abs(end.x - start.x) > 1))) return false;
        if (end.y - start.y == 0 && !currentLayout[end.x][end.y].isEmpty()) return false;
        if (Math.abs(end.y - start.y) == 1 && currentLayout[end.x][end.y].isEmpty()) return false;
        if (Math.abs(end.x - start.x) == 2 && !currentLayout[end.x + 1][end.y].isEmpty()) return false;
        if (end.x == 0) beginPawnPromotion();
        return true;
    }

    private Boolean validMovePawnDown() {
        if (end.x - start.x < 0 || end.x - start.x > 2) return false;
        if (end.x - start.x == 2 && start.x != DOWN_PAWN_HOME) return false;
        if (Math.abs(end.y - start.y) > 1 ||
                (Math.abs(end.y - start.y) == 1 && (end.x - start.x == 0 || Math.abs(end.x - start.x) > 1))) return false;
        if (end.y - start.y == 0 && !currentLayout[end.x][end.y].isEmpty()) return false;
        if (Math.abs(end.y - start.y) == 1 && currentLayout[end.x][end.y].isEmpty()) return false;
        if (Math.abs(end.x - start.x) == 2 && !currentLayout[end.x - 1][end.y].isEmpty()) return false;
        if (end.x == 7) beginPawnPromotion();
        return true;
    }

    private Boolean validMoveKing() {
        if (Math.abs(end.x - start.x) > 1 || Math.abs(end.y - start.y) > 1) return false;
        return true;
    }

    private Boolean validMoveKnight() {
        if (Math.abs(end.x - start.x) == 2 && Math.abs(end.y - start.y) == 1) return true;
        if (Math.abs(end.x - start.x) == 1 && Math.abs(end.y - start.y) == 2) return true;
        return false;
    }

    private Boolean validMoveRook() {
        if (Math.abs(end.x - start.x) != 0 && Math.abs(end.y - start.y) != 0) return false;
        int minPos = ((end.x - start.x == 0) ? Math.min(start.y, end.y) : Math.min(start.x, end.x)) + 1;
        int maxPos = ((end.x - start.x == 0) ? Math.max(start.y, end.y) : Math.max(start.x, end.x)) - 1;
        for (; minPos <= maxPos; minPos++) {
            if (end.x - start.x == 0 && !currentLayout[end.x][minPos].isEmpty()) return false;
            if (end.y - start.y == 0 && !currentLayout[minPos][end.y].isEmpty()) return false;
        }
        return true;
    }

    private Boolean validMoveBishop() {
        if (Math.abs(end.x - start.x) != Math.abs(end.y - start.y)) return false;
        int offsetX = (end.x - start.x < 0) ? -1 : 1;
        int offsetY = (end.y - start.y < 0) ? -1 : 1;
        for (int x = start.x + offsetX, y = start.y + offsetY; x != end.x && y != end.y; x += offsetX, y += offsetY) {
            if (!currentLayout[x][y].isEmpty()) return false;
        }
        return true;
    }

    private Boolean validMoveQueen() {
        return validMoveBishop() || validMoveRook();
    }

    private Boolean moveIsValid() {
        String startPiece = currentLayout[start.x][start.y];
        startPiece = startPiece.substring(1);
        switch (startPiece) {
            case "bi": //bishop
                return validMoveBishop();
            case "ki": //king
                return validMoveKing();
            case "kn": //knight
                return validMoveKnight();
            case "p-u": //pawn-up
                return validMovePawnUp();
            case "p-d": //pawn-down
                return validMovePawnDown();
            case "qn": //queen
                return validMoveQueen();
            case "rk": //rook
                return validMoveRook();
        }
        return false;
    }

    private View.OnClickListener createOnClickListinerForCell() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pawnPromotion) {
                    ImageButton imgButton = ((ImageButton) v);
                    if (start == null) {
                        start = (Point) imgButton.getTag();
                        if (currentLayout[start.x][start.y].isEmpty()) {
                            start = null;
                        } else {
                            if (currentLayout[start.x][start.y].startsWith(currentTurn)) {
                                imgButton.setBackgroundColor(BLUE_COLOR);
                            } else {
                                Toast.makeText(MainActivity.this, "Not your turn.", Toast.LENGTH_SHORT).show();
                                start = null;
                            }
                        }
                    } else {
                        end = (Point) imgButton.getTag();
                        if (!moveIsValid() || (!currentLayout[end.x][end.y].isEmpty() &&
                                currentLayout[end.x][end.y].charAt(0) == currentLayout[start.x][start.y].charAt(0))) {
                            imageButtonLayout[start.x][start.y].setBackgroundColor((start.x + start.y) % 2 == 1 ? GRAY_COLOR : WHITE_COLOR);
                            Toast.makeText(MainActivity.this, "Invalid Move", Toast.LENGTH_SHORT).show();
                            start = end = null;
                            return;
                        }
                        imageButtonLayout[start.x][start.y].setImageResource(android.R.color.transparent);
                        imageButtonLayout[start.x][start.y].setBackgroundColor((start.x + start.y) % 2 == 1 ? GRAY_COLOR : WHITE_COLOR);
                        imageButtonLayout[end.x][end.y].setImageResource((Integer) pieceImage.get(currentLayout[start.x][start.y]));
                        currentLayout[end.x][end.y] = currentLayout[start.x][start.y];
                        currentLayout[start.x][start.y] = "";
                        start = end = null;
                        currentTurn = (currentTurn == "W") ? "B" : "W";
                    }
                }
            }
        };
    }

    public void drawBoard(View view) {
        int gridLayoutWidth = gridLayout.getWidth();
        int gridLayoutHeight = gridLayout.getHeight();

        if (!drewInitialBoard) {
            ((Button)view).setText("Reset Board");
            TextView welcomeText = (TextView)findViewById(R.id.welcomeView);
            TextView subWelcomeText = (TextView)findViewById(R.id.subTextView);
            if (welcomeText != null) ((LinearLayout)welcomeText.getParent()).removeView(welcomeText);
            if (subWelcomeText != null) ((LinearLayout)subWelcomeText.getParent()).removeView(subWelcomeText);
        }
        currentTurn = "W";

        currentLayout = new String[initialLayout.length][];
        for(int i = 0; i < initialLayout.length; i++) {
            currentLayout[i] = initialLayout[i].clone();
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(!drewInitialBoard) imageButtonLayout[i][j] = new ImageButton(this);
                ImageButton square = imageButtonLayout[i][j];
                if (!drewInitialBoard) {
                    square.setLayoutParams(new LinearLayout.LayoutParams(gridLayoutWidth / 8, gridLayoutWidth / 8));
                    square.setTag(new Point(i, j));
                    square.setOnClickListener(createOnClickListinerForCell());
                    square.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    gridLayout.addView(square);
                }
                String key = currentLayout[i][j];
                if (!key.isEmpty()) square.setImageResource((Integer)pieceImage.get(currentLayout[i][j]));
                else square.setImageResource(android.R.color.transparent);
                if ((i + j) % 2 == 1) square.setBackgroundColor(GRAY_COLOR);
                else square.setBackgroundColor(WHITE_COLOR);
            }
        }
        drewInitialBoard = true;
    }

    public void finishPawnPromotion(View view) {
        RadioGroup group = (RadioGroup)findViewById(R.id.promotionGroup);
        if (group != null) {
            int radioButtonID = group.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton)group.findViewById(radioButtonID);
            String tag = (String)radioButton.getTag();
            Point loc = (Point)((Button)view).getTag();
            currentLayout[loc.x][loc.y] = promotionType + tag;
            Log.d("Simplicity: ", currentLayout[loc.x][loc.y]);
            imageButtonLayout[loc.x][loc.y].setImageResource((Integer) pieceImage.get(currentLayout[loc.x][loc.y]));
            pawnPromotionView.setVisibility(View.INVISIBLE);
            pawnPromotion = false;
        }
    }
}
