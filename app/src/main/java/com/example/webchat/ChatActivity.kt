package com.example.webchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

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



import okhttp3.OkHttpClient
import okhttp3.Request
//import okhttp3.WebSocket
import okhttp3.WebSocketListener
//import okhttp.Response



class ChatActivity : AppCompatActivity(), com.example.webchat.WebSocket.WebSocketCallback {
    fun PrintToMainView(Data:String)
    {

        CoroutineScope(Dispatchers.Main).launch {
            findViewById<TextView>(R.id.chat_messages).setText(findViewById<TextView>(R.id.chat_messages).text.toString() + "\n\r" + Data);
        }
    }
    fun ClearMainView()
    {
        CoroutineScope(Dispatchers.Main).launch {
            findViewById<TextView>(R.id.chat_messages).setText("");
        }
    }
    var IsRoomSeteltion = false
    var Rooms = mutableMapOf("0" to "0")
    fun GenerateRequest(RequestUrl: String, Type:String) : HttpURLConnection
    {
        val urlConnection = URL(RequestUrl).openConnection() as HttpURLConnection

        urlConnection.requestMethod = "GET"
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Authorization", "Bearer $UserTokenttoken")
        return  urlConnection
    }

    fun GetRooms()
    {

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
                    PrintToMainView("Выберите комнату для подключения:")
                    for (i in 0 until jsonArray.length()) {
                        PrintToMainView(jsonArray.getJSONObject(i).getString("Name"))
                        Rooms[jsonArray.getJSONObject(i).getString("Name")] = jsonArray.getJSONObject(i).getString("ChatroomId")
                    }
                    IsRoomSeteltion = true
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
                    ClearMainView()
                    PrintToMainView("Вы успешно вошли в комнату")

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
    fun WriteToRoom(message: String)
    {
        com.example.webchat.WebSocket.sendMessage(message)
    }

    var UserTokenttoken = ""
    var UserUID = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPrf = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        UserTokenttoken = sharedPrf.getString("user_token", null)?:"NULL"
        UserUID = sharedPrf.getString("user_uuid", null)?:"NULL"

        GetRooms()
//        var MyButton = findViewById<Button>(R.id.SendMessage);
//        MyButton?.setOnClickListener()
//        {
//            AnalyzeInput()
//            //val CurrentMessages = R.id.ChatView.get
//            var Data =  findViewById<TextView>(R.id.ChatView).text.toString() + "\n" + findViewById<TextInputEditText>(R.id.MessageInput).text.toString();
//            findViewById<TextView>(R.id.ChatView).setText(Data);
//            //findViewById<TextView>(R.id.ChatView).text = Data;
//        }

    }

    fun AnalyzeInput()
    {
        if(IsRoomSeteltion) {

           // val selectedRoom = findViewById<TextInputEditText>(R.id.MessageInput).text.toString()

//            if(Rooms.containsKey(selectedRoom))
//            {
//                PrintToMainView("Выбрана текущая комната $selectedRoom")
//                EnterRoom(Rooms[selectedRoom]?:"NULL")
//                com.example.webchat.WebSocket.connectWebSocket(Rooms[selectedRoom]?:"NULL",UserUID,UserTokenttoken,this)
//                IsRoomSeteltion = false
//            }
//            else
//                PrintToMainView("Не удалось найти комнаты с таким названием")
        }
        else{
           // WriteToRoom(findViewById<TextInputEditText>(R.id.MessageInput).text.toString())
        }
    }

    override fun onMessageReceived(message: String) {
        PrintToMainView(message)
    }

    override fun onConnectionOpened() {

    }

    override fun onConnectionClosed() {

    }

    override fun onConnectionFailure(error: String) {

    }
}