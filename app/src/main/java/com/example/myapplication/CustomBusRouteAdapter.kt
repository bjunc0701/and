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
import kotlin.random.Random

class CustomBusRouteAdapter(context: Context, busRoutes: List<Any>) :
    ArrayAdapter<Any>(context, 0, busRoutes) {

    // 직행 버스 노선을 나타내는 데이터 클래스
    data class DirectBusRoute(val routeInfo: String, val totalTime: String, val routeType: String?)

    // 환승 버스 노선을 나타내는 데이터 클래스
    data class TransferBusRoute(val stationInfo: String, val totalTime: String, val startRouteType: String?, val endRouteType: String?)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_bus_route, parent, false)
        }
        val routeInfoTextView = view!!.findViewById<TextView>(R.id.text_view_route_info)
        val totalTimeTextView = view.findViewById<TextView>(R.id.text_view_total_time)
        val statusTextView = view.findViewById<TextView>(R.id.text_view_status) // 상태를 표시할 TextView
        val busRoute = getItem(position)

        // 상태를 랜덤으로 선택
        val statuses = listOf("쾌적", "보통", "혼잡", "매우혼잡")
        val sortedStatuses = statuses.sorted() // 혼잡도가 낮은 순으로 정렬
        val randomStatus = sortedStatuses[Random.nextInt(sortedStatuses.size)]

        // 상태에 따른 글자 색상 설정
        val statusColor = when (randomStatus) {
            "쾌적" -> Color.parseColor("#87CEEB") // 하늘색
            "보통" -> Color.parseColor("#ADFF2F") // 연두색
            "혼잡" -> Color.parseColor("#FFA500") // 주황색
            "매우혼잡" -> Color.RED // 빨간색
            else -> Color.BLACK
        }

        statusTextView.text = randomStatus
        statusTextView.setTextColor(statusColor)

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

                // 출발 버스와 환승 버스의 색상을 절반씩 나타내기 위한 GradientDrawable 생성
                val startDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(startColor, startColor))
                startDrawable.cornerRadius = 80f

                val endDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(endColor, endColor))
                endDrawable.cornerRadius = 80f

                val layerDrawable = LayerDrawable(arrayOf(startDrawable, endDrawable))
                val halfWidth = view.resources.displayMetrics.widthPixels / 2
                layerDrawable.setLayerInset(1, halfWidth, 0, 0, 0)

                totalTimeTextView.background = layerDrawable
            }
        }
        return view
    }
}
