package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var busNumberInput: EditText
    private lateinit var searchButton: Button
    private lateinit var resultText: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        busNumberInput = findViewById(R.id.busNumberInput)
        searchButton = findViewById(R.id.searchButton)
        resultText = findViewById(R.id.resultText)

        searchButton.setOnClickListener {
            val busNumber = busNumberInput.text.toString()
            if (busNumber.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = getBusRouteInfo(busNumber)
                    withContext(Dispatchers.Main) {
                        showRoutes(response)
                    }
                }
            }
        }

        resultText.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = resultText.getItemAtPosition(position) as String
            CoroutineScope(Dispatchers.IO).launch {
                val response = getSelectedRouteInfo(selectedItem)
                withContext(Dispatchers.Main) {
                    val nodenms = response.getJSONArray("nodenms")
                    val nodenmsList = mutableListOf<String>()
                    for (i in 0 until nodenms.length()) {
                        nodenmsList.add(nodenms.getString(i))
                    }
                    showSelectedRoute(nodenmsList)
                }
            }
        }
    }

    private fun getBusRouteInfo(busNumber: String): JSONObject {
        val url = URL("http://192.168.55.195:12300/bus_route_info")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val jsonInputString = "{\"bus_no\": \"$busNumber\"}"

        connection.outputStream.use { outputStream ->
            outputStream.write(jsonInputString.toByteArray(Charsets.UTF_8))
        }

        // Check if the response code is 200 (OK)
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).use { br ->
                var responseLine: String?
                while (br.readLine().also { responseLine = it } != null) {
                    response.append(responseLine!!.trim())
                }
            }
            return JSONObject(response.toString())
        } else {
            // Handle error cases
            return JSONObject().put("error", connection.responseCode)
        }
    }

    private fun getSelectedRouteInfo(routeId: String): JSONObject {
        val url = URL("http://192.168.55.195:12300/select_route")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val jsonInputString = "{\"route_id\": \"$routeId\",\"route_id\": \"$routeId\"}"

        connection.outputStream.use { outputStream ->
            outputStream.write(jsonInputString.toByteArray(Charsets.UTF_8))
        }

        // Check if the response code is 200 (OK)
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).use { br ->
                var responseLine: String?
                while (br.readLine().also { responseLine = it } != null) {
                    response.append(responseLine!!.trim())
                }
            }
            return JSONObject(response.toString())
        } else {
            // Handle error cases
            return JSONObject().put("error", connection.responseCode)
        }
    }

    private fun showRoutes(response: JSONObject) {
        val jsonArray = response.optJSONArray("routes")
        val routes = mutableListOf<String>()
        jsonArray?.let {
            for (i in 0 until it.length()) {
                val item = it.getJSONObject(i)
                routes.add(item.getString("routeid"))
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, routes)
        resultText.adapter = adapter
    }

    private fun showSelectedRoute(nodenmsList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nodenmsList)
        resultText.adapter = adapter
    }
}
