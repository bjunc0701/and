package com.example.myapplication

//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//import java.net.URL
//
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val editTextRouteNo = findViewById<EditText>(R.id.editTextRouteNo)
//        val buttonGetRouteId = findViewById<Button>(R.id.buttonGetRouteId)
//        val textViewRouteId = findViewById<TextView>(R.id.textViewRouteId)
//
//        buttonGetRouteId.setOnClickListener {
//            val routeNo = editTextRouteNo.text.toString()
//
//            GlobalScope.launch(Dispatchers.IO) {
//                val routeId = getRouteId(routeNo)
//                launch(Dispatchers.Main) {
//                    textViewRouteId.text = "Route ID: $routeId"
//                }
//            }
//        }
//    }
//
//    private fun getRouteId(routeNo: String): String {
//        val url = "http://192.168.55.195:12300/bus_routes"
//        val response = URL(url).readText()
//        val jsonResponse = JSONObject(response)
//        val busRoutes = jsonResponse.getJSONArray("bus_routes")
//
//        for (i in 0 until busRoutes.length()) {
//            val route = busRoutes.getJSONObject(i)
//            if (route.getString("route_no") == routeNo) {
//                return route.getString("route_id")
//            }
//        }
//
//        return "Route ID not found"
//    }
//}

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val busNumberInput = findViewById<EditText>(R.id.busNumberInput)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        searchButton.setOnClickListener {
            val busNumber = busNumberInput.text.toString()
            if (busNumber.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = getBusRouteInfo(busNumber)
                    CoroutineScope(Dispatchers.Main).launch {
                        resultText.text = response
                    }
                }
            }
        }
    }

    private fun getBusRouteInfo(busNumber: String): String {
        val url = URL("http://192.168.55.195:12300/bus_route_info")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val jsonInputString = "{\"bus_no\": \"$busNumber\"}"

        connection.outputStream.use { outputStream ->
            outputStream.write(jsonInputString.toByteArray())
        }

        val response = StringBuilder()
        BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).use { br ->
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
        }
        return response.toString()
    }
}

