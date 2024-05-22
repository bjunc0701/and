package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R

class CustomBusRouteAdapter(context: Context, busRoutes: List<BusRoute>) :
    ArrayAdapter<CustomBusRouteAdapter.BusRoute>(context, 0, busRoutes) {

    data class BusRoute(val routeInfo: String, val totalTime: String, val routeType: String)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_bus_route, parent, false)
        }
        val routeInfoTextView = view!!.findViewById<TextView>(R.id.text_view_route_info)
        val totalTimeTextView = view.findViewById<TextView>(R.id.text_view_total_time)
        val busRoute: BusRoute? = getItem(position)

        if (busRoute != null) {
            routeInfoTextView.text = busRoute.routeInfo
            totalTimeTextView.text = busRoute.totalTime
            totalTimeTextView.setTextColor(Color.WHITE) // 텍스트 색상 흰색으로 설정
            if (busRoute.routeType == "간선버스") {
                totalTimeTextView.setBackgroundColor(Color.BLUE)
            } else {
                totalTimeTextView.setBackgroundColor(Color.RED)
            }
        }
        return view
    }
}
