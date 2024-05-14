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

    private lateinit var editTextDeparture: EditText
    private lateinit var editTextDestination: EditText
    private lateinit var listViewDepartureResults: ListView
    private lateinit var listViewDestinationResults: ListView
    private lateinit var listViewBusRoutes: ListView
    private lateinit var adapterDeparture: ArrayAdapter<String>
    private lateinit var adapterDestination: ArrayAdapter<String>
    private lateinit var adapterBusRoutes: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextDeparture = findViewById(R.id.editText_departure)
        editTextDestination = findViewById(R.id.editText_destination)
        listViewDepartureResults = findViewById(R.id.listView_departure_results)
        listViewDestinationResults = findViewById(R.id.listView_destination_results)
        listViewBusRoutes = findViewById(R.id.listView_bus_routes)
        val buttonSearch: Button = findViewById(R.id.button_search)

        adapterDeparture = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        adapterDestination = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        adapterBusRoutes = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listViewDepartureResults.adapter = adapterDeparture
        listViewDestinationResults.adapter = adapterDestination
        listViewBusRoutes.adapter = adapterBusRoutes

        editTextDeparture.addTextChangedListener {
            val departureNode = editTextDeparture.text.toString()
            GetDepartureStationInfoTask().execute(departureNode)
        }

        editTextDestination.addTextChangedListener {
            val destinationNode = editTextDestination.text.toString()
            GetDestinationStationInfoTask().execute(destinationNode)
        }

        buttonSearch.setOnClickListener {
            val departureNode = editTextDeparture.text.toString()
            val destinationNode = editTextDestination.text.toString()
            GetBusRouteInfoTask().execute(departureNode, destinationNode)
        }

        listViewDepartureResults.setOnItemClickListener { _, _, position, _ ->
            val selectedStation = adapterDeparture.getItem(position)
            editTextDeparture.setText(selectedStation)
            listViewDepartureResults.visibility = ListView.GONE
        }

        listViewDestinationResults.setOnItemClickListener { _, _, position, _ ->
            val selectedStation = adapterDestination.getItem(position)
            editTextDestination.setText(selectedStation)
            listViewDestinationResults.visibility = ListView.GONE
        }
    }

    private inner class GetDepartureStationInfoTask : AsyncTask<String, Void, List<String>?>() {

        override fun doInBackground(vararg params: String?): List<String>? {
            val departureNode = params[0] ?: return null
            val url = URL("http://192.168.1.2:12300/departure_station_info")

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val postData = "{\"departure_node\":\"$departureNode\"}"
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
            adapterDeparture.clear()
            if (result != null) {
                adapterDeparture.addAll(result)
                if (result.size > 1) {
                    listViewDepartureResults.visibility = ListView.VISIBLE
                } else {
                    listViewDepartureResults.visibility = ListView.GONE
                }
            }
        }
    }

    private inner class GetDestinationStationInfoTask : AsyncTask<String, Void, List<String>?>() {

        override fun doInBackground(vararg params: String?): List<String>? {
            val destinationNode = params[0] ?: return null
            val url = URL("http://192.168.1.2:12300/destination_station_info")

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val postData = "{\"destination_node\":\"$destinationNode\"}"
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
            adapterDestination.clear()
            if (result != null) {
                adapterDestination.addAll(result)
                if (result.size > 1) {
                    listViewDestinationResults.visibility = ListView.VISIBLE
                } else {
                    listViewDestinationResults.visibility = ListView.GONE
                }
            }
        }
    }

    private inner class GetBusRouteInfoTask : AsyncTask<String, Void, JSONObject?>() {

        override fun doInBackground(vararg params: String?): JSONObject? {
            val departureNode = params[0] ?: return null
            val destinationNode = params[1] ?: return null
            val url = URL("http://192.168.1.2:12300/bus_route_info")

            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val postData =
                    "{\"departure_node\":\"$departureNode\", \"destination_node\":\"$destinationNode\"}"
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
            val commonStations = jsonResponse.optJSONArray("common_stations")

            val busRoutesArray = ArrayList<String>()

            // Process direct routes
            if (directRoutes != null) {
                for (i in 0 until directRoutes.length()) {
                    val route = directRoutes.getJSONObject(i)
                    val busNumber = route.getString("bus_number")
//                    val departureStation = route.getString("departure_station")
//                    val destinationStation = route.getString("destination_station")
                    val totalDistance = route.getDouble("total_distance")
                    val totalTime = route.getString("total_time")

                    val routeInfo ="버스 $busNumber\n" +
                                "총 거리: $totalDistance km\n 소요에정시간: $totalTime"
                    busRoutesArray.add(routeInfo)
                }
            }

            // Process common stations
            if (commonStations != null) {
                for (i in 0 until commonStations.length()) {
                    val station = commonStations.getJSONObject(i)
                    val stationName = station.getString("station")
                    val departureBus = station.getString("departure_bus")
                    val destinationBus = station.getString("destination_bus")
                    val distanceDepartureToCommon =
                        station.getDouble("distance_departure_to_common")
                    val distanceCommonToDestination =
                        station.getDouble("distance_common_to_destination")
                    val totalDistance = station.getDouble("total_distance")
                    val totalTime = station.getString("total_time")

                    val stationInfo = "환승정류장: $stationName\n" +
                            "출발정류장 버스: $departureBus\n 환승버스:$destinationBus\n" +
//                            "Distance from Departure: $distanceDepartureToCommon km\n" +
//                            "Distance to Destination: $distanceCommonToDestination km\n" +
                            "총거리: $totalDistance km\n 소요예정시간:$totalTime"

                    busRoutesArray.add(stationInfo)
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

