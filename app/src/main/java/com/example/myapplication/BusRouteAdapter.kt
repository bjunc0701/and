// CustomBusRouteAdapter.kt
package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

data class BusRoute(val routeInfo: String, val totalTime: String)

class CustomBusRouteAdapter(context: Context, private val busRoutes: List<BusRoute>) :
    ArrayAdapter<BusRoute>(context, 0, busRoutes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.list_item_bus_route, parent, false
        )

        val routeInfoTextView: TextView = view.findViewById(R.id.text_view_route_info)
        val totalTimeTextView: TextView = view.findViewById(R.id.text_view_total_time)

        val busRoute = getItem(position)
        routeInfoTextView.text = busRoute?.routeInfo
        totalTimeTextView.text = busRoute?.totalTime

        return view
    }
}
