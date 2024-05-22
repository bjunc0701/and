package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R

/**
 * Custom adapter for displaying bus routes.
 *
 * @property context The context.
 * @property busRoutes The list of bus routes.
 */
class CustomBusRouteAdapter(context: Context, busRoutes: List<Any>) :
    ArrayAdapter<Any>(context, 0, busRoutes) {

    // 직행 버스 노선을 나타내는 데이터 클래스
    data class DirectBusRoute(val routeInfo: String, val totalTime: String, val routeType: String?)

    // 환승 버스 노선을 나타내는 데이터 클래스
    data class TransferBusRoute(val stationInfo: String, val totalTime: String, val startRouteType: String?, val endRouteType: String?)

    /**
     * Gets the view for a specific item in the list.
     *
     * @param position The position of the item in the list.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent view.
     * @return The view corresponding to the data at the specified position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_bus_route, parent, false)
        }
        val routeInfoTextView = view!!.findViewById<TextView>(R.id.text_view_route_info)
        val totalTimeTextView = view.findViewById<TextView>(R.id.text_view_total_time)
        val busRoute = getItem(position)

        if (busRoute != null) {
            // 직행 버스 노선인 경우
            if (busRoute is DirectBusRoute) {
                routeInfoTextView.text = busRoute.routeInfo
                totalTimeTextView.text = busRoute.totalTime
                totalTimeTextView.setTextColor(Color.WHITE)
                val routeType = busRoute.routeType
                when (routeType) {
                    "간선버스" -> totalTimeTextView.setBackgroundResource(R.drawable.blue_background)
                    "급행버스" -> totalTimeTextView.setBackgroundResource(R.drawable.red_background)
                    "지선버스" -> totalTimeTextView.setBackgroundResource(R.drawable.green_background)
                    else -> totalTimeTextView.setBackgroundResource(R.drawable.orange_background)
                }
            }
            // 환승 버스 노선인 경우
            else if (busRoute is TransferBusRoute) {
                routeInfoTextView.text = busRoute.stationInfo
                totalTimeTextView.text = busRoute.totalTime
                totalTimeTextView.setTextColor(Color.WHITE)
                val startRouteType = busRoute.startRouteType
                val endRouteType = busRoute.endRouteType

                val startColor = when (startRouteType) {
                    "간선버스" -> Color.BLUE
                    "급행버스" -> Color.RED
                    "지선버스" -> Color.GREEN
                    else -> Color.YELLOW
                }

                val endColor = when (endRouteType) {
                    "간선버스" -> Color.BLUE
                    "급행버스" -> Color.RED
                    "지선버스" -> Color.GREEN
                    else -> Color.YELLOW
                }

                // 출발 버스와 환승 버스의 색상을 절반씩 나타내기
                val startDrawable = GradientDrawable()
                startDrawable.shape = GradientDrawable.RECTANGLE
                startDrawable.cornerRadius = 80f
                startDrawable.setColor(startColor)

                val endDrawable = GradientDrawable()
                endDrawable.shape = GradientDrawable.RECTANGLE
                endDrawable.cornerRadius = 80f
                endDrawable.setColor(endColor)

                val layerDrawable = LayerDrawable(arrayOf(startDrawable, endDrawable))
                val halfWidth = view.resources.displayMetrics.widthPixels / 2
                layerDrawable.setLayerInset(1, halfWidth, 0, 0, 0)

                totalTimeTextView.background = layerDrawable
            }
        }
        return view
    }
}
