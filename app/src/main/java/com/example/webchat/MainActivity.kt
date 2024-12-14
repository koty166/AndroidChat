package com.example.webchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.webchat.ui.login.LoginResult
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.tubesock.WebSocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


data class Chat(
    val id: String,
    val name: String,
)

class ChatAdapter(context: Context, private val people: List<Chat>) : ArrayAdapter<Chat>(context, 0, people) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val chat = getItem(position)


        val listItem = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)


        val personNameTextView = listItem.findViewById<TextView>(R.id.chat_name)
        personNameTextView.text = chat?.name

        return listItem
    }
}

class MainActivity : AppCompatActivity() {

    fun GenerateRequest(RequestUrl: String, Type:String) : HttpURLConnection
    {
        val urlConnection = URL(RequestUrl).openConnection() as HttpURLConnection

        urlConnection.requestMethod = "GET"
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Authorization", "Bearer $UserTokenttoken")
        return  urlConnection
    }


    fun EnterRoom(RoomUID:String)
    {
        val urlConnection = GenerateRequest("http://${getString(R.string.host)}:3000/user/enterChatroom/$RoomUID/$UserUID","GET")
        var response = ""

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            try {
                val responseCode = urlConnection.responseCode
                response = BufferedReader(InputStreamReader(urlConnection.inputStream)).use { reader ->
                    reader.lineSequence().joinToString("\n")
                }

                if(responseCode == 200)
                {
                }
                else{
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка получения списка комнат",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }


    var UserTokenttoken = ""
    var UserUID = ""

//    private lateinit var chatList: RecyclerView
//    private lateinit var createChatButton: Button
    private val chats = mutableListOf<Chat>()

    private lateinit var chatListView: ListView
    private lateinit var btnNewChat: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrf = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        UserTokenttoken = sharedPrf.getString("user_token", null)?:"NULL"
        UserUID = sharedPrf.getString("user_uuid", null)?:"NULL"

        chatListView = findViewById(R.id.chatList)
        btnNewChat = findViewById(R.id.createChatButton)

        val adapter = ChatAdapter(this, chats)
        //val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chats)
        chatListView.adapter = adapter

        chatListView.setOnItemClickListener { _, _, position, _ ->
            try {
                EnterRoom(chats[position].id ?: "NULL")
                val i = Intent(this@MainActivity, ChatActivity::class.java)
                startActivity(i)
            }
         catch (ex: Exception) {
            println(ex.message)
        }
        }

        btnNewChat.setOnClickListener {
            createNewChat()
            adapter.notifyDataSetChanged()
        }



//        chatList = findViewById(R.id.chatList)
//        createChatButton = findViewById(R.id.createChatButton)
//
//        chatList.layoutManager = LinearLayoutManager(this)
//        val adapter = ChatAdapter(chats) { chat ->
//            openChat(chat)
//        }
//        chatList.adapter = adapter
//
//        createChatButton.setOnClickListener {
//            createNewChat()
//        }
//
        loadChatData()
        adapter.notifyDataSetChanged()
    }

    private fun loadChatData() {

        val urlConnection = GenerateRequest("http://${getString(R.string.host)}:3000/chatroom/get","GET")
        var response = ""

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            try {
                val responseCode = urlConnection.responseCode
                response = BufferedReader(InputStreamReader(urlConnection.inputStream)).use { reader ->
                    reader.lineSequence().joinToString("\n")
                }

                if(responseCode == 200)
                {
                    val jsonArray = JSONArray(JSONObject(response).getString("content"))
                    for (i in 0 until jsonArray.length()) {
                        val jsobj = jsonArray.getJSONObject(i)
                        chats.add(Chat(jsobj.getString("ChatroomId"),jsobj.getString("Name")))
                    }
                }
                else{
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка получения списка комнат",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }

    private fun openChat(chat: Chat) {

    }

    private fun createNewChat() {
        val newChat = Chat("1", "Новый чат")
        chats.add(newChat)
        Toast.makeText(this, "Создан новый чат: ${newChat.name}", Toast.LENGTH_SHORT).show()
    }

}