package com.github.andriytyranovets.ifoutages.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.andriytyranovets.ifoutages.config.RetrofitServiceInstance
import com.github.andriytyranovets.ifoutages.models.OutageDuration
import com.github.andriytyranovets.ifoutages.models.OutageHour
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainViewModel : ViewModel() {
    private val apiService = RetrofitServiceInstance.outagesApi

    private val _outages = MutableLiveData<Map<String, List<OutageDuration>>>()
    val outages: LiveData<Map<String, List<OutageDuration>>> = _outages;

    private val _queue = MutableLiveData<String>()
    val queue: LiveData<String> = _queue;

    fun getOutages(accountNumber: String) {
        viewModelScope.launch {
            try {
                val res = apiService.getOutages(accountNumber)
                Log.i("Main View Model", "${res}")
                if(res.graphs.today != null || res.graphs.tomorrow != null) {
                    _outages.value = mapOf(
                        "Today" to (res.graphs.today?.hoursList
                            ?.filter { h -> h.electricity > 0 }
                            ?.fold(mutableListOf()) { acc, h -> folder(acc, h, LocalDate.now()) }
                            ?: emptyList()),
                        "Tomorrow" to (res.graphs.tomorrow?.hoursList
                            ?.filter { h -> h.electricity > 0 }
                            ?.fold(mutableListOf()) { acc, h -> folder(acc, h, LocalDate.now().plusDays(1)) }
                            ?: emptyList())
                    )
                }
                _queue.value = "${res.current.queue}.${res.current.subqueue}"
            } catch (ex: Exception) {
                Log.e("Main Activity", "Outage error:")
                Log.e("Main Activity", ex.message.toString())
                Log.e("Main Activity", ex.toString())
            }
        }
    }

    private fun folder(accumulator: MutableList<OutageDuration>, outageHour: OutageHour, day: LocalDate): MutableList<OutageDuration> {
        val begin = LocalDateTime.of(day, LocalTime.of(outageHour.hour.toInt() - 1, 0))
        if(accumulator.isNotEmpty() && accumulator.last().end.hour == begin.hour) {
            val dur = accumulator.removeLast()
            accumulator.add(dur.withEnd(begin.plusHours(1)))
        } else {
            accumulator.add(OutageDuration(begin, begin.plusHours(1)))
        }

        return accumulator
    }
}