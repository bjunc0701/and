package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
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
    }

    private fun getBusRouteInfo(routeno: String): JSONObject {
        val url = URL("http://192.168.1.2:12300/bus_route_info")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val jsonInputString = "{\"bus_no\": \"$routeno\"}"

        connection.outputStream.use { outputStream ->
            outputStream.write(jsonInputString.toByteArray(Charsets.UTF_8))
        }

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
            return JSONObject().put("error", connection.responseCode)
        }
    }

    private fun showRoutes(response: JSONObject) {
        val jsonArray = response.optJSONArray("routes")
        val routes = mutableListOf<String>()
        val routeIds = mutableListOf<String>()
        jsonArray?.let {
            for (i in 0 until it.length()) {
                val item = it.getJSONObject(i)
                val routeNo = item.getString("routeno")
                val routeId = item.getString("routeid")
                routes.add(routeNo)
                routeIds.add(routeId)
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, routes)
        resultText.adapter = adapter

        // 리스트뷰에서 아이템을 클릭했을 때 이벤트 처리
        resultText.setOnItemClickListener { _, _, position, _ ->
            val selectedRouteId = routeIds[position] // 선택된 노선에 해당하는 ID 가져오기
            val selectedRouteNo = routes[position] // 선택된 노선 번호
            CoroutineScope(Dispatchers.IO).launch {
                val response = getBusStopInfo(selectedRouteId) // 노선 ID로 정류소 정보 가져오기
                val jsonArray = response.optJSONArray("nodenm")
                val busStops = mutableListOf<String>()
                jsonArray?.let {
                    for (i in 0 until it.length()) {
                        val stop = it.getString(i)
                        busStops.add(stop)
                    }
                }
                withContext(Dispatchers.Main) {
                    showSelectedRoute(selectedRouteNo, busStops)
                }
            }
        }
    }

    private fun getBusStopInfo(routeId: String): JSONObject {
        val url = URL("http://192.168.1.2:12300/bus_stop_info")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val jsonInputString = "{\"route_id\": \"$routeId\"}"

        connection.outputStream.use { outputStream ->
            outputStream.write(jsonInputString.toByteArray(Charsets.UTF_8))
        }

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
            return JSONObject().put("error", connection.responseCode)
        }
    }

    private fun showSelectedRoute(routeNo: String, busStops: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, busStops)
        resultText.adapter = adapter
    }
}
