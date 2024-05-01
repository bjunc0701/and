package com.example.myapplication

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var editTextBusStopName: EditText
    private lateinit var buttonSearch: Button
    private lateinit var textViewResult: TextView
    private lateinit var listViewBusStops: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextBusStopName = findViewById(R.id.editTextBusStopName)
        buttonSearch = findViewById(R.id.buttonSearch)
        textViewResult = findViewById(R.id.textViewResult)
        listViewBusStops = findViewById(R.id.listViewBusStops)

        buttonSearch.setOnClickListener {
            val busStopName = editTextBusStopName.text.toString()
            if (busStopName.isNotEmpty()) {
                FetchBusStopListTask().execute(busStopName)
            } else {
                Toast.makeText(this, "정류장 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        listViewBusStops.setOnItemClickListener { parent, view, position, id ->
            val selectedBusStop = parent.getItemAtPosition(position) as String
            val intent = Intent(this@MainActivity, BusStopInfoActivity::class.java)
            intent.putExtra("selectedBusStop", selectedBusStop)
            startActivity(intent)
        }
    }

    private inner class FetchBusStopListTask : AsyncTask<String, Void, List<String>>() {
        override fun doInBackground(vararg params: String?): List<String> {
            val busStopName = params[0]

            val url = URL("http://192.168.1.2:12300/search_bus_stop") // 서버 주소 입력
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            try {
                val outputStream = connection.outputStream
                val body = JSONObject().apply {
                    put("bus_stop_name", busStopName)
                }.toString().toByteArray(Charsets.UTF_8)
                outputStream.write(body)
                outputStream.close()

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonArray = JSONArray(response.toString())
                val busStopList = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val busStopName = jsonObject.getString("nodenm")
                    if (!busStopList.contains(busStopName)) {
                        busStopList.add(busStopName)
                    }
                }
                return busStopList
            } catch (e: Exception) {
                Log.e("MainActivity", "Error", e)
                return emptyList()
            } finally {
                connection.disconnect()
            }
        }

        override fun onPostExecute(result: List<String>?) {
            super.onPostExecute(result)
            if (result.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "정류장 목록을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, result)
                listViewBusStops.adapter = adapter
            }
        }
    }
}

