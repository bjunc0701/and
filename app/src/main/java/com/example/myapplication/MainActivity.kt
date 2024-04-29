package com.example.myapplication

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var editTextBusStopName: EditText
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextBusStopName = findViewById(R.id.editTextBusStopName)
        textViewResult = findViewById(R.id.textViewResult)
    }

    fun onSearchButtonClick(view: View) {
        val busStopName = editTextBusStopName.text.toString()
        if (busStopName.isNotEmpty()) {
            FetchBusRouteInfoTask().execute(busStopName)
        }
    }

    private inner class FetchBusRouteInfoTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            val busStopName = params[0]
            val url = URL("http://192.168.1.2:80/get_bus_info")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            try {
                val outputStream = connection.outputStream
                val body = JSONObject().apply {
                    put("bus_stop_name", busStopName)
                }.toString().toByteArray(charset("UTF-8"))
                outputStream.write(body)
                outputStream.close()

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection.disconnect()
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.isNullOrEmpty()) {
                textViewResult.text = "데이터를 가져오는 데 실패했습니다."
            } else {
                parseAndDisplayResult(result)
            }
        }

        private fun parseAndDisplayResult(result: String) {
            try {
                val jsonArray = JSONArray(result)
                if (jsonArray.length() > 0) {
                    val resultBuilder = StringBuilder()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        resultBuilder.append("노선번호: ${jsonObject.getString("노선번호")}\n")
                        resultBuilder.append("남은 정류장 수: ${jsonObject.getString("남은정류장수")}\n")
                        resultBuilder.append("도착 예정 시간: ${jsonObject.getString("도착예정시간")}\n")
                        resultBuilder.append("혼잡도: ${jsonObject.getString("혼잡도")}\n\n")
                    }
                    textViewResult.text = resultBuilder.toString()
                } else {
                    textViewResult.text = "해당 정류장에 대한 버스 노선 정보가 없습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                textViewResult.text = "데이터를 파싱하는 동안 오류가 발생했습니다."
            }
        }
    }
}