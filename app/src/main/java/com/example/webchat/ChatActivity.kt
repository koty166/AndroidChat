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
    var UserTokenttoken = ""
    var UserUID = ""
    var ChatID = ""
    var UserName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.chat_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPrf = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        UserTokenttoken = sharedPrf.getString("user_token", null)?:"NULL"
        UserUID = sharedPrf.getString("user_uuid", null)?:"NULL"
        ChatID = sharedPrf.getString("chat_id", null)?:"NULL"
        UserName = sharedPrf.getString("user_name", null)?:"NULL"

        com.example.webchat.WebSocket.connectWebSocket(ChatID,UserUID,UserTokenttoken,this)

        var MyButton = findViewById<Button>(R.id.btn_send);
        MyButton?.setOnClickListener()
        {
            try {
                val message = findViewById<EditText>(R.id.message_input).text.toString()
                com.example.webchat.WebSocket.sendMessage(message)
                findViewById<TextView>(R.id.chat_messages).setText(findViewById<TextView>(R.id.chat_messages).text.toString() + "\n\r" + "${UserName}:\t" +message);
            }
            catch (Ex:Exception)
            {
                println("awd")
            }

        }

    }

    override fun onMessageReceived(message: String) {
        findViewById<TextView>(R.id.chat_messages).setText(findViewById<TextView>(R.id.chat_messages).text.toString() + "\n\r" +"Other:\t"+ message);
    }

    override fun onConnectionOpened() {

    }

    override fun onConnectionClosed() {

    }

    override fun onConnectionFailure(error: String) {

    }
}