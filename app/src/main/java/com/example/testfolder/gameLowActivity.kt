package com.example.testfolder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser

class gameLowActivity : BaseActivity() {
    private lateinit var progressBar: ProgressBar
    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var coinText: TextView
    private lateinit var currentUser: FirebaseUser
    private lateinit var quizList: List<SingletonKotlin.QuizItem>
    private lateinit var selectedQuizzes: List<SingletonKotlin.QuizItem>
    private var currentRound = 0
    private var correctAnswers = 0
    private var startTime: Long = 0
    private var totalTime = 0L
    private val roundTime = 60 * 1000 // 60초
    private var progressBarThread: Thread? = null

    private lateinit var roundImageView: ImageView
    private lateinit var numberImageView: ImageView
    private lateinit var finishedTextView: TextView

    private lateinit var loadingLayout: View
    private lateinit var loadingImage: ImageView
    private lateinit var loadingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_low)
        progressBar = findViewById(R.id.progressBar1)
        val questionTextView = findViewById<TextView>(R.id.qeustionbox) // 질문텍스트
        val obutton = findViewById<Button>(R.id.o_btn)  // O버튼
        val xbutton = findViewById<Button>(R.id.x_btn) // X버튼
        coinText = findViewById(R.id.coin_text)

        roundImageView = findViewById(R.id.roundImageView)
        numberImageView = findViewById(R.id.numberImageView)
        finishedTextView = findViewById(R.id.finishedTextView)

        loadingLayout = findViewById(R.id.loading_layout)
        loadingImage = findViewById(R.id.loading_image)
        loadingText = findViewById(R.id.loading_text)

        // 로딩 이미지 회전 애니메이션 적용
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        loadingImage.startAnimation(rotateAnimation)
        startLoadingTextAnimation()

        try {
            currentUser = SingletonKotlin.getCurrentUser() ?: throw IllegalStateException("User authentication required.")
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "SingletonKotlin is not initialized.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 사용자 코인 불러오기
        try {
            SingletonKotlin.loadUserCoins(coinText)
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "SingletonKotlin is not initialized.", Toast.LENGTH_SHORT).show()
            finish()
        }

        val excludeIds = setOf(R.id.o_btn, R.id.x_btn)
        applyFontSize(excludeIds)

        // 3초 동안 로딩 화면 표시 후 게임 시작
        handler.postDelayed({
            loadingLayout.visibility = View.GONE
            // 랜덤으로 5개의 OX 퀴즈 데이터 불러오기
            SingletonKotlin.loadOXQuizData { quizData ->
                quizList = quizData
                if (quizList.size >= 5) {
                    selectedQuizzes = quizList.shuffled().take(5)
                    startRound(questionTextView)
                } else {
                    questionTextView.text = "Not enough quizzes available."
                }
            }
        }, 3000)

        obutton.setOnClickListener {
            handleAnswer("O", questionTextView)
        }

        xbutton.setOnClickListener {
            handleAnswer("X", questionTextView)
        }
    }

    private fun startRound(questionTextView: TextView) {
        startTime = System.currentTimeMillis()
        progressStatus = 0
        progressBar.progress = progressStatus
        updateRoundImages()
        displayQuestion(questionTextView)
        startProgressBar(questionTextView)
    }

    private fun displayQuestion(questionTextView: TextView) {
        if (currentRound < selectedQuizzes.size) {
            val quizItem = selectedQuizzes[currentRound]
            questionTextView.text = "Date: ${quizItem.date}\n\n${quizItem.question}" //4지선다랑 형식 통일화
        } else {
            val totalGameTime = totalTime / 1000 // 초 단위로 변환
            SingletonKotlin.saveGameResult("OX", correctAnswers, totalGameTime) // 게임 유형 추가
            questionTextView.text = "Quiz completed! Correct answers: $correctAnswers, Time taken: ${totalGameTime} seconds\nReturning to game selection screen in 5 seconds..."
            handler.postDelayed({
                finish() // 이전 화면으로 돌아가기
            }, 5000) // 나가기 전에 5초 딜레이
        }
    }

    private fun handleAnswer(userAnswer: String, questionTextView: TextView) {
        if (currentRound < selectedQuizzes.size) {
            val quizItem = selectedQuizzes[currentRound]
            if (userAnswer == quizItem.answer) {
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                correctAnswers++
                SingletonKotlin.updateUserCoins(5, coinText)
            } else {
                Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show()
            }
            currentRound++
            totalTime += System.currentTimeMillis() - startTime
            startRound(questionTextView)
        }
    }

    private fun startProgressBar(questionTextView: TextView) {
        progressStatus = 0
        progressBar.progress = progressStatus
        progressBarThread?.interrupt()
        progressBarThread = Thread {
            val startRoundTime = System.currentTimeMillis()
            while (progressStatus < 300 && (System.currentTimeMillis() - startRoundTime) < roundTime) {  // 0.2 * 300 = 60초
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                }
                try {
                    Thread.sleep(200)   // 0.2초 대기
                } catch (e: InterruptedException) {
                    return@Thread
                }
            }
            handler.post {
                if (currentRound < selectedQuizzes.size) {
                    questionTextView.text = "Time's up!"
                    totalTime += roundTime
                    currentRound++
                    startRound(questionTextView)
                }
            }
        }.apply { start() }
    }

    private fun updateRoundImages() {
        val roundDrawable = R.drawable.round // round.png 이미지 리소스
        val numberDrawable = when (currentRound + 1) {
            1 -> R.drawable.number1
            2 -> R.drawable.number2
            3 -> R.drawable.number3
            4 -> R.drawable.number4
            5 -> R.drawable.number5
            else -> null
        }
        roundImageView.setImageResource(roundDrawable)
        if (numberDrawable != null) {
            numberImageView.visibility = View.VISIBLE
            finishedTextView.visibility = View.GONE
            numberImageView.setImageResource(numberDrawable)
        } else {
            numberImageView.visibility = View.GONE
            finishedTextView.visibility = View.VISIBLE
        }
    }

    private fun startLoadingTextAnimation() { //...을 로딩과 함께 애니메이션으로 움직이도록
        var dotCount = 0
        handler.post(object : Runnable {
            override fun run() {
                dotCount++
                if (dotCount > 3) {
                    dotCount = 0
                }
                val dots = ".".repeat(dotCount)
                loadingText.text = "Pulling out the diary$dots"
                handler.postDelayed(this, 500) // 500ms마다 업데이트
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        progressBarThread?.interrupt()
    }
}
