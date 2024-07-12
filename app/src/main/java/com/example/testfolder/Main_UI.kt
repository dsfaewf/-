package com.example.testfolder

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Main_UI : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var mediaPlayer: MediaPlayer   //효과음 재생용 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        mediaPlayer = MediaPlayer.create(this, R.raw.paper_flip)

        checkFirstLogin()

        val imageButton1 = findViewById<View>(R.id.my_button4) as Button
        val diaryButton = findViewById<View>(R.id.my_button2) as Button
        val catRoomButton = findViewById<View>(R.id.my_button3) as Button
        val gameButton = findViewById<Button>(R.id.btn_game)

        imageButton1.setOnClickListener {
            val intent = Intent(applicationContext, Setting_UI::class.java)
            startActivity(intent)
        }

        diaryButton.setOnClickListener {
            // 효과음 재생 테스트
            mediaPlayer.start()

            // Diary_write_UI 액티비티로 이동
            val intent = Intent(applicationContext, Diary_write_UI::class.java)
            startActivity(intent)
        }

        gameButton.setOnClickListener {
            val intent = Intent(this, gamelistActivity::class.java)
            startActivity(intent)
        }

        catRoomButton.setOnClickListener {
            val intent = Intent(this, CatRoomActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun checkFirstLogin() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)
            userRef.child("surveyCompleted").get().addOnSuccessListener { dataSnapshot ->
                val surveyCompleted = dataSnapshot.getValue(Boolean::class.java) ?: false
                if (!surveyCompleted) {
                    showSurveyDialog()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to get data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSurveyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Request for a survey")
            .setMessage("Oh? It's your first time using our app. Please take a brief survey!")
            .setPositiveButton("Go to Survey") { dialog, which ->
                val intent = Intent(this, SurveyActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Do it Later", null)
            .show()
    }
}
