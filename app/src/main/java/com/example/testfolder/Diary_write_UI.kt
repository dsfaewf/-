package com.example.testfolder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Diary_write_UI : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val saveButton = findViewById<Button>(R.id.my_button6)
        val diaryEditText = findViewById<EditText>(R.id.diary_edit_text)
        val characterCountTextView = findViewById<TextView>(R.id.character_count_text_view)
        val dateTextView = findViewById<TextView>(R.id.date_text_view)

        // Firebase 데이터베이스 루트 참조 가져오기
        databaseReference = FirebaseDatabase.getInstance().reference.child("diaries")

        // Save 버튼 클릭 시 날짜 표시
        saveButton.setOnClickListener {
            // 현재 날짜를 가져오기
            val currentDate = getCurrentDate()
            // 날짜를 TextView에 설정
            dateTextView.text = currentDate

            // 일기 내용을 Firebase 데이터베이스에 업로드
            val diaryContent = diaryEditText.text.toString()
            databaseReference.push().setValue(diaryContent)
        }

        // 텍스트 입력 수를 표시
        diaryEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val count = s?.length ?: 0
                characterCountTextView.text = "$count/1000"
            }
        })
    }

    // 현재 날짜를 가져오는 함수
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
