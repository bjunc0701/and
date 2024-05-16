package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var editTextStartStation: EditText
    private lateinit var editTextEndStation: EditText
    private lateinit var listViewStartResults: ListView
    private lateinit var listViewEndResults: ListView
    private lateinit var listViewBusRoutes: ListView
    private lateinit var adapterStart: ArrayAdapter<String>
    private lateinit var adapterEnd: ArrayAdapter<String>
    private lateinit var adapterBusRoutes: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextStartStation = findViewById(R.id.editText_departure)
        editTextEndStation = findViewById(R.id.editText_destination)
        listViewStartResults = findViewById(R.id.listView_departure_results)
        listViewEndResults = findViewById(R.id.listView_destination_results)
        listViewBusRoutes = findViewById(R.id.listView_bus_routes)
        val buttonSearch: Button = findViewById(R.id.button_search)

        adapterStart = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        adapterEnd = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        adapterBusRoutes = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listViewStartResults.adapter = adapterStart
        listViewEndResults.adapter = adapterEnd
        listViewBusRoutes.adapter = adapterBusRoutes

        editTextStartStation.addTextChangedListener {
            val startNode = editTextStartStation.text.toString()
            GetStartStationInfoTask().execute(startNode)
        }

        editTextEndStation.addTextChangedListener {
            val endNode = editTextEndStation.text.toString()
            GetEndStationInfoTask().execute(endNode)
        }

        buttonSearch.setOnClickListener {
            val startNode = editTextStartStation.text.toString()
            val endNode = editTextEndStation.text.toString()
            GetBusRouteInfoTask().execute(startNode, endNode)
        }

        listViewStartResults.setOnItemClickListener { _, _, position, _ ->
            val selectedStation = adapterStart.getItem(position)
            editTextStartStation.setText(selectedStation)
            listViewStartResults.visibility = ListView.GONE
        }

        listViewEndResults.setOnItemClickListener { _, _, position, _ ->
            val selectedStation = adapterEnd.getItem(position)
            editTextEndStation.setText(selectedStation)
            listViewEndResults.visibility = ListView.GONE
        }
    }

    private inner class GetStartStationInfoTask : AsyncTask<String, Void, List<String>?>() {

        override fun doInBackground(vararg params: String?): List<String>? {
            val startNode = params[0] ?: return null
            val url = URL("http://192.168.1.2:12300/start_station_info")

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val postData = "{\"start_station\":\"$startNode\"}"
                val postDataBytes = postData.toByteArray(Charsets.UTF_8)

                val outputStream = connection.outputStream
                outputStream.write(postDataBytes)
                outputStream.flush()
                outputStream.close()

                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                bufferedReader.close()
                inputStream.close()
                connection.disconnect()

                val jsonArray = JSONArray(response.toString())
                val stations = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val stationName = jsonArray.getJSONObject(i).getString("station_name")
                    stations.add(stationName)
                }
                return stations
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: List<String>?) {
            super.onPostExecute(result)
            adapterStart.clear()
            if (result != null) {
                adapterStart.addAll(result)
                if (result.size > 1) {
                    listViewStartResults.visibility = ListView.VISIBLE
                } else {
                    listViewStartResults.visibility = ListView.GONE
                }
            }
        }
    }

    private inner class GetEndStationInfoTask : AsyncTask<String, Void, List<String>?>() {

        override fun doInBackground(vararg params: String?): List<String>? {
            val endNode = params[0] ?: return null
            val url = URL("http://192.168.1.2:12300/end_station_info")

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val postData = "{\"end_station\":\"$endNode\"}"
                val postDataBytes = postData.toByteArray(Charsets.UTF_8)

                val outputStream = connection.outputStream
                outputStream.write(postDataBytes)
                outputStream.flush()
                outputStream.close()

                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                bufferedReader.close()
                inputStream.close()
                connection.disconnect()

                val jsonArray = JSONArray(response.toString())
                val stations = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val stationName = jsonArray.getJSONObject(i).getString("station_name")
                    stations.add(stationName)
                }
                return stations
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: List<String>?) {
            super.onPostExecute(result)
            adapterEnd.clear()
            if (result != null) {
                adapterEnd.addAll(result)
                if (result.size > 1) {
                    listViewEndResults.visibility = ListView.VISIBLE
                } else {
                    listViewEndResults.visibility = ListView.GONE
                }
            }
        }
    }

    private inner class GetBusRouteInfoTask : AsyncTask<String, Void, JSONObject?>() {

        override fun doInBackground(vararg params: String?): JSONObject? {
            val startNode = params[0] ?: return null
            val endNode = params[1] ?: return null
            val url = URL("http://192.168.1.2:12300/route_info")

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val postData = "{\"start_station\":\"$startNode\", \"end_station\":\"$endNode\"}"
                val postDataBytes = postData.toByteArray(Charsets.UTF_8)

                val outputStream = connection.outputStream
                outputStream.write(postDataBytes)
                outputStream.flush()
                outputStream.close()

                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                bufferedReader.close()
                inputStream.close()
                connection.disconnect()

                return JSONObject(response.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: JSONObject?) {
            super.onPostExecute(result)
            if (result != null) {
                handleBusRouteInfoResponse(result)
            }
        }

        private fun handleBusRouteInfoResponse(jsonResponse: JSONObject) {
            val directRoutes = jsonResponse.optJSONArray("direct_routes")
            val intermediateStations = jsonResponse.optJSONObject("intermediate_stations")

            val busRoutesArray = ArrayList<String>()

            // Process direct routes
            if (directRoutes != null) {
                for (i in 0 until directRoutes.length()) {
                    val route = directRoutes.getJSONObject(i)
                    val busNumber = route.getString("bus_number")
                    val totalDistance = route.getDouble("total_distance")
                    val totalTime = route.getString("total_time")

                    val routeInfo = "버스 $busNumber\n" +
                            "총 거리: $totalDistance km\n소요예정시간: $totalTime"
                    busRoutesArray.add(routeInfo)
                }
            }

            // Process intermediate stations
            if (intermediateStations != null) {
                intermediateStations.keys().forEach { startBus ->
                    val routes = intermediateStations.getJSONArray(startBus)
                    for (i in 0 until routes.length()) {
                        val station = routes.getJSONObject(i)
                        val stationName = station.getString("station")
                        val endBus = station.getString("end_bus")
                        val totalDistance = station.getDouble("total_distance")
                        val totalTime = station.getString("total_time")

                        val stationInfo = "환승정류장: $stationName\n" +
                                "출발 버스: $startBus\n환승 버스: $endBus\n" +
                                "총 거리: $totalDistance km\n소요예정시간: $totalTime"

                        busRoutesArray.add(stationInfo)
                    }
                }
            }

            // Update UI with bus routes
            runOnUiThread {
                adapterBusRoutes.clear()
                adapterBusRoutes.addAll(busRoutesArray)
                adapterBusRoutes.notifyDataSetChanged()
                listViewBusRoutes.visibility = ListView.VISIBLE
            }
        }
    }
}
