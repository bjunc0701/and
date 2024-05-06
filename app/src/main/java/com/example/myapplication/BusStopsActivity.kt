package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class BusStopsActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_stops)

        listView = findViewById(R.id.busStopsListView)

        val stopsInfo = JSONArray(intent.getStringExtra("stopsInfo"))
        val stopNames = mutableListOf<String>()

        for (i in 0 until stopsInfo.length()) {
            val stop = stopsInfo.getJSONObject(i)
            val stopName = stop.getString("nodenm")
            stopNames.add(stopName)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stopNames)
        listView.adapter = adapter
    }
}
