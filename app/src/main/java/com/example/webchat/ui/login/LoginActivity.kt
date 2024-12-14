package com.example.webchat.ui.login

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.webchat.databinding.ActivityLoginBinding
import android.content.Intent
import android.os.Looper
import com.example.webchat.MainActivity


import kotlinx.coroutines.*

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject



import com.example.webchat.R

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
private lateinit var binding: ActivityLoginBinding
var UserAuthToken = "";
    fun GenerateRequest(params: Map<String, String>,RequestUrl: String) : HttpURLConnection
    {
        val urlConnection = URL(RequestUrl).openConnection() as HttpURLConnection

        urlConnection.requestMethod = "POST"
        urlConnection.doOutput = true
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("accept", "application/json")
        return  urlConnection
    }
    fun CreateUser(params: Map<String, String>,LogRes:LoginResult)
    {

        val urlConnection = GenerateRequest(params,"http://${getString(R.string.host)}:3000/user/create")
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
                    response = if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader(InputStreamReader(urlConnection.inputStream)).use { reader ->
                            reader.readText()
                        }

                    } else {
                        "Error: $responseCode"
                    }

                    if(responseCode == 201)
                    {
                            updateUiWithUser(LogRes.success,params["username"]?:"Defaul user")
                    }
                    else{
                        showLoginFailed(R.string.usere_create_error)
                    }
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
    fun AuthUser(params: Map<String, String>,LogRes:LoginResult) {

        val urlConnection = GenerateRequest(params,"http://${getString(R.string.host)}:3000/user/login")
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

                        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        val Token = JSONObject(JSONObject(response).getString("content")).getString("Token")
                        val UserUUID = JSONObject(JSONObject(response).getString("content")).getString("UserGuid")
                        editor.putString("user_token", Token)
                        editor.putString("user_uuid", UserUUID)
                        editor.putString("user_name", params["username"])
                        editor.apply()

                        try{
                        updateUiWithUser(LogRes.success, params["username"]?:"Defaul user")}
                        catch (ex: Exception) {
                            println(ex.message)
                        }
                    }
                    else{
                        //showLoginFailed(R.string.error_invalid_credentials)
                        CreateUser(params,LogRes)
                    }
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityLoginBinding.inflate(layoutInflater)
     setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
               password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            AuthUser(mapOf("username" to username.text.toString(), "password" to password.text.toString()),loginResult)


        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView?, username:String) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val welcome = getString(R.string.welcome)
                Toast.makeText(
                    applicationContext,
                    "$welcome $username",
                    Toast.LENGTH_LONG
                ).show()
                val i = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(i)
            }
        }
        catch (ex: Exception) {
            println(ex.message)
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}