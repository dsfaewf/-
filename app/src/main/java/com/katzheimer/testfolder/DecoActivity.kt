package com.katzheimer.testfolder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class DecoActivity : AppCompatActivity(), ShopItemsAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopItemsAdapter
    private lateinit var buyItemList: MutableList<ShopItem>
    private lateinit var coinText: TextView
    private lateinit var frame: FrameLayout
    private lateinit var roomBtn: TextView
    private lateinit var shopBtn: TextView
    private lateinit var saveBtn: TextView
    private lateinit var clickedItem: ShopItem
    private lateinit var newcatGif: GifImageView
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // FirebaseAuth 객체 초기화

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deco)

        coinText = findViewById(R.id.coin_text)
        val catGif = findViewById<GifImageView>(R.id.cat_gif)
        newcatGif = findViewById(R.id.newcat_gif)
        frame = findViewById(R.id.deco_frame)
        roomBtn = findViewById(R.id.room_btn_deco)
        shopBtn = findViewById(R.id.shop_btn_deco)
        saveBtn = findViewById(R.id.save_btn_deco)

        recyclerView = findViewById(R.id.buy_items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        buyItemList = mutableListOf()
        adapter = ShopItemsAdapter(buyItemList, this, this)
        recyclerView.adapter = adapter

        // Initialize if it's not initialized
        if (!SingletonKotlin.isInitialized()) {
           SingletonKotlin.initialize(auth, database)
        }

        // 현재 장착중인 배경으로 배경 설정
        SingletonKotlin.loadUserBackground(frame)

        // 사용자 코인 불러오기
        SingletonKotlin.loadUserCoins(coinText)

        // 기본 배경을 품목에 무조건 추가하도록 설정
        buyItemList.add(ShopItem(R.drawable.room3, "Default Room", 0))

        // 기본 고양이 친구 숨기기 품목 추가
        buyItemList.add(ShopItem(R.drawable.normal_cat, "No Cat Friend", 0))

        // 사용자가 구매한 아이템 불러오기
        SingletonKotlin.loadPurchasedItems(buyItemList, adapter)

        // 유저가 구입한 새로운 고양이 친구 불러오기
        SingletonKotlin.loadPurchasedCatFriends(buyItemList, adapter)

        roomBtn.setOnClickListener {
            val intent = Intent(this, CatRoomActivity::class.java)
            startActivity(intent)
            finish()
        }
        shopBtn.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
            finish()
        }
        saveBtn.setOnClickListener {
            // 선택된 배경 또는 고양이 친구를 db에 저장하도록 싱글톤 구현
            if (clickedItem.name.startsWith("Cat Friend")) {
                SingletonKotlin.saveUserCatFriend(clickedItem.name)
            } else if (clickedItem.name == "No Cat Friend") {
                SingletonKotlin.saveUserCatFriend("No Cat Friend")
            } else {
                SingletonKotlin.saveUserBackground(clickedItem.name)
            }
            showConfirmationDialog() // 팝업 띄우고 확인 누르면 룸 이동
        }

        // 새로운 고양이 친구를 로드하여 표시
        SingletonKotlin.loadUserCatFriend(newcatGif)
    }


    override fun onItemClick(position: Int) {
        clickedItem = buyItemList[position]
        if (clickedItem.name.startsWith("Cat Friend")) {
            newcatGif.setImageResource(clickedItem.imageResource)
            (newcatGif.drawable as? GifDrawable)?.apply {
                loopCount = 0 // 무한 반복
                start()
            }
            newcatGif.visibility = View.VISIBLE
        } else if (clickedItem.name == "No Cat Friend") {
            newcatGif.visibility = View.GONE
        } else {
            frame.setBackgroundResource(clickedItem.imageResource)
        }
    }

    private fun showConfirmationDialog() {
        val dialog = ConfirmDialog(
            confirmDialogInterface = object : DialogCustomInterface {
                override fun onClickYesButton(id: Int) {
                    val intent = Intent(this@DecoActivity, CatRoomActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            },
            title = "Save Confirmation",
            content = "Do you want to save and move to the room?",
            buttonText = "Yes",
            id = 1 // or any other identifier you need
        )

        dialog.show(supportFragmentManager, "ConfirmDialog")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, CatRoomActivity::class.java)
        startActivity(intent)
        finish()
    }
}
