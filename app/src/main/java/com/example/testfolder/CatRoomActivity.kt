package com.example.testfolder

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class CatRoomActivity : AppCompatActivity() {

    private lateinit var coinText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_room)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        currentUser = auth.currentUser!!

        coinText = findViewById(R.id.coin_text)
        val catGif = findViewById<GifImageView>(R.id.cat_gif)
        val shopBtn = findViewById<TextView>(R.id.shop_btn)
        val decoBtn = findViewById<TextView>(R.id.deco_btn)
        val diaryBtn = findViewById<TextView>(R.id.diary_btn)
        val gameBtn = findViewById<TextView>(R.id.game_btn)

        // 사용자 코인 불러오기
        loadUserCoins()

        // GIF 반복 설정
        val gifDrawable = catGif.drawable as GifDrawable
        gifDrawable.loopCount = 0 // 무한 반복

        shopBtn.setOnClickListener {
            val intent = Intent(applicationContext, ShopActivity::class.java)
            startActivity(intent)
        }
        decoBtn.setOnClickListener {
            // Deco 버튼 클릭 시 동작
        }
        diaryBtn.setOnClickListener {
            val intent = Intent(applicationContext, Diary_write_UI::class.java)
            startActivity(intent)
        }
        gameBtn.setOnClickListener {
            val intent = Intent(applicationContext, gametestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserCoins() {
        val userId = currentUser.uid
        val userRef = database.child("users").child(userId)

        userRef.child("coins").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coins = snapshot.getValue(Long::class.java) ?: 0L
                coinText.text = coins.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CatRoomActivity, "데이터베이스 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
