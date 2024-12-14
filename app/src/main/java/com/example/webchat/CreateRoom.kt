package com.example.webchat
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class CreateRoomActivity : AppCompatActivity() {

    private lateinit var editTextRoomName: EditText
    private lateinit var buttonCreateRoom: Button

    fun GenerateRequest(RequestUrl: String, Type:String) : HttpURLConnection
    {
        val sharedPrf = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val UserTokenttoken = sharedPrf.getString("user_token", null)?:"NULL"


        val urlConnection = URL(RequestUrl).openConnection() as HttpURLConnection

        urlConnection.requestMethod = "POST"
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Authorization", "Bearer $UserTokenttoken")
        return  urlConnection
    }


    fun CreateRoom(params: Map<String, String>)
    {
        val urlConnection = GenerateRequest("http://${getString(R.string.host)}:3000/chatroom/create","POST")
        var response = ""
        val postData = "{" + params.map {
            "\"${URLEncoder.encode(it.key, "UTF-8")}\":\"${URLEncoder.encode(it.value, "UTF-8")}\""
        }.joinToString(",") + "}"

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            try {
                OutputStreamWriter(urlConnection.outputStream).use { writer ->
                    writer.write(postData)
                    writer.flush()
                    val responseCode = urlConnection.responseCode
                    response =
                        BufferedReader(InputStreamReader(urlConnection.inputStream)).use { reader ->
                            reader.readText()
                        }

                    if(responseCode == 201 || responseCode == 200)
                    {
                        finish()
                    }
                    else{

                    }
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_room)

        editTextRoomName = findViewById(R.id.editTextRoomName)
        buttonCreateRoom = findViewById(R.id.buttonCreateRoom)

        buttonCreateRoom.setOnClickListener {
            createRoom()
        }
    }

    private fun createRoom() {
        val roomName = editTextRoomName.text.toString().trim()

        if (roomName.isEmpty()) {
            Toast.makeText(this, "Введите имя комнаты", Toast.LENGTH_SHORT).show()
        } else {


            CreateRoom(mapOf("name" to roomName))



            editTextRoomName.text.clear()
        }
    }
}