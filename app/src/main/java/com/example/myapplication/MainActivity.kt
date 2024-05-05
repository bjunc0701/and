package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var editTextBusNumber: EditText
    private lateinit var buttonGetRoute: Button
    private lateinit var textViewRouteInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextBusNumber = findViewById(R.id.editTextBusNumber)
        buttonGetRoute = findViewById(R.id.buttonGetRoute)
        textViewRouteInfo = findViewById(R.id.textViewRouteInfo)

        buttonGetRoute.setOnClickListener {
            val busNumber = editTextBusNumber.text.toString()
            if (busNumber.isNotEmpty()) {
                getBusRoute(busNumber)
            }
        }
    }

    private fun getBusRoute(busNumber: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val baseUrl = "http://192.168.55.195:12300/get_bus_route"
            val encodedBusNumber = URLEncoder.encode(busNumber, "UTF-8")
            val url = URL("$baseUrl?bus_no=$encodedBusNumber")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    bufferedReader.close()
                    inputStream.close()

                    withContext(Dispatchers.Main) {
                        textViewRouteInfo.text = response.toString()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        textViewRouteInfo.text = "Error: ${connection.responseMessage}"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    textViewRouteInfo.text = "Error: ${e.message}"
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}
