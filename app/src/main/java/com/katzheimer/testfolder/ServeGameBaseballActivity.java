package com.katzheimer.testfolder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ServeGameBaseballActivity extends AppCompatActivity {
    private EditText requestText;
    private TextView responseText;
    private TextView resultText;
    private TextView lifeCountText;
    private TextView coinText;
    private LinearLayout lifeContainer;
    private FirebaseUser currentUser;

    private Integer[] comNumber = new Integer[3];
    private Set<Integer> userNumbers = new HashSet<>(); // Set for checking duplicate user input numbers

    private int lifeCount = 10;
    private int strike = 0;
    private int ball = 0;

    private static final int COIN_REWARD = 10;
    private static final int MAX_CLEARS_PER_DAY = 3;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth auth = FirebaseAuth.getInstance(); // FirebaseAuth 객체 초기화

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serve_game_baseball);

        // Initialize if it's not initialized
        if (!SingletonJava.isInitialized()) {
            SingletonJava.initialize(auth, databaseReference);
        }

        currentUser = SingletonJava.getInstance().getCurrentUser();
        coinText = findViewById(R.id.coin_text);

        try {
            requestText = findViewById(R.id.request_text);
            responseText = findViewById(R.id.response_text);
            resultText = findViewById(R.id.result_text);
            lifeContainer = findViewById(R.id.life_container);

            Button startBtn = findViewById(R.id.start_btn);
            Button answerBtn = findViewById(R.id.answer_btn);
            Button resetBtn = findViewById(R.id.reset_btn);

            viewMode("end");

            startBtn.setOnClickListener(view -> {
                randomNumber();
                toastMessage("Game started");
                viewMode("start");
                initializeLifeImages();
            });

            answerBtn.setOnClickListener(view -> numberCheck());

            resetBtn.setOnClickListener(view -> {
                toastMessage("Reset");
                viewMode("end");
                reset();
            });

        } catch (Exception e) {
            Log.e("ServeGameBaseballActivity", "Error during onCreate", e);
        }

        // Load user coins and daily reset check
        SingletonJava.getInstance().checkAndResetDailyClears(currentUser, coinText, this);
        // Load user coins
        SingletonJava.getInstance().loadUserCoins(coinText);
    }

    private void randomNumber() {
        try {
            Set<Integer> set = new HashSet<>();
            Random random = new Random();

            while (set.size() < 3) {
                int randomValue = random.nextInt(9) + 1; // Range changed to 1~9
                set.add(randomValue);
            }

            comNumber = set.toArray(new Integer[0]);

        } catch (Exception e) {
            Log.e("ServeGameBaseballActivity", "Error during randomNumber", e);
        }
    }

    private void numberCheck() {
        try {
            String inputNumber = requestText.getText().toString();

            if (inputNumber.length() == 3 && !hasDuplicate(inputNumber)) {
                userNumbers.clear(); // Clear user input numbers

                userNumbers.add(Integer.parseInt(inputNumber.substring(0, 1)));
                userNumbers.add(Integer.parseInt(inputNumber.substring(1, 2)));
                userNumbers.add(Integer.parseInt(inputNumber.substring(2, 3)));

                strike = 0;
                ball = 0;

                for (int num = 0; num < 3; num++) {
                    if (comNumber[num].equals(Integer.parseInt(inputNumber.substring(num, num + 1)))) {
                        strike++;
                    } else if (userNumbers.contains(comNumber[num])) {
                        ball++;
                    }
                }

                lifeCount--; // Decrease life count
                decreaseLife();

                // onCreate 메서드 내부에서 responseText 참조를 수정
                responseText = findViewById(R.id.response_text);

// numberCheck 메서드에서 responseText를 사용하는 부분 수정
                if (strike == 3) {
                    toastMessage("Success");
                    // responseText를 사용하지 않으므로 지움
                    // responseText.setText("Answer: " + comNumber[0] + ", " + comNumber[1] + ", " + comNumber[2]);
                    // Reward coins
                    SingletonJava.getInstance().checkAndRewardCoins(currentUser, MAX_CLEARS_PER_DAY, COIN_REWARD, coinText, this);
                    // Navigate to game list after game ends
                    navigateToGameList();
                } else if (lifeCount <= 0) {
                    toastMessage("Failure");
                    // responseText를 사용하지 않으므로 지움
                    // responseText.setText("Answer: " + comNumber[0] + ", " + comNumber[1] + ", " + comNumber[2]);
                    responseText.setTextSize(32); // Strike, Ball 텍스트 크기 설정
                    // Navigate to game list after game ends
                    navigateToGameList();
                } else {
                    responseText.setText("Strike: " + strike + ", Ball: " + ball);
                    responseText.setTextSize(32); // Strike, Ball 텍스트 크기 설정
                    showResult(inputNumber);
                }



                lifeCountText.setText("Life: " + lifeCount);
                requestText.setText("");
                responseText.setTextSize(32);
                strike = 0;
                ball = 0;
            } else if (inputNumber.length() != 3) {
                toastMessage("Please enter 3 numbers.");
            } else {
                toastMessage("You cannot enter duplicate numbers.");
            }
        } catch (Exception e) {
            Log.e("ServeGameBaseballActivity", "Error during numberCheck", e);
        }
    }

    private boolean hasDuplicate(String inputNumber) {
        Set<Character> charSet = new HashSet<>();
        for (char c : inputNumber.toCharArray()) {
            if (!charSet.add(c)) {
                return true;
            }
        }
        return false;
    }

    private void showResult(String inputNumber) {
        try {
            String result = "Strike: " + strike + ", Ball: " + ball;
            resultText.append(inputNumber + " : " + result + "\n");
            resultText.setTextSize(32); // Strike, Ball 텍스트 크기 설정
        } catch (Exception e) {
            Log.e("ServeGameBaseballActivity", "Error during showResult", e);
        }
    }

    private void reset() {
        try {
            lifeCount = 10;
            lifeCountText.setText("Life: " + lifeCount);
            responseText.setText("");
            resultText.setText("");
            lifeContainer.removeAllViews();
            initializeLifeImages();
        } catch (Exception e) {
            Log.e("ServeGameBaseballActivity", "Error during reset", e);
        }
    }

    private void viewMode(String mode) {
        try {
            Button startBtn = findViewById(R.id.start_btn);
            Button answerBtn = findViewById(R.id.answer_btn);
            Button resetBtn = findViewById(R.id.reset_btn);

            if (mode.equals("start")) {
                startBtn.setEnabled(false);
                answerBtn.setEnabled(true);
                resetBtn.setEnabled(true);
                requestText.setEnabled(true);

                startBtn.setBackgroundColor(getColor(android.R.color.darker_gray));
                answerBtn.setBackgroundColor(getColor(android.R.color.holo_green_light));
                resetBtn.setBackgroundColor(getColor(android.R.color.holo_green_light));
            } else if (mode.equals("end")) {
                startBtn.setEnabled(true);
                answerBtn.setEnabled(false);
                resetBtn.setEnabled(true);
                requestText.setEnabled(false);

                startBtn.setBackgroundColor(getColor(android.R.color.holo_green_light));
                answerBtn.setBackgroundColor(getColor(android.R.color.holo_green_light));
                resetBtn.setBackgroundColor(getColor(android.R.color.holo_green_light));
            }
        } catch (Exception e) {
            Log.e("ServeGameBaseballActivity", "Error during viewMode", e);
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToGameList() {
        Intent intent = new Intent(this, GamelistActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToGameList(); // 이전 화면으로 돌아갈 때 게임 목록으로 이동
    }

    private void initializeLifeImages() {
        for (int i = 1; i <= lifeCount; i++) {
            ImageView lifeImage = new ImageView(this);
            lifeImage.setImageResource(R.drawable.fish); // 목숨 이미지 리소스 설정
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 150);
            params.setMargins(0, 2, 0, 2); // 이미지 간격 조절
            lifeImage.setLayoutParams(params);
            lifeContainer.addView(lifeImage);
        }
    }

    private void decreaseLife() {
        if (lifeCount > 0) {
            lifeContainer.removeViewAt(lifeCount - 1); // 마지막 이미지 제거
        }
    }
}
