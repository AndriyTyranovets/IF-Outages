package com.github.andriytyranovets.ifoutages.viewmodels

import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.andriytyranovets.ifoutages.IFOutagesApplication
import com.github.andriytyranovets.ifoutages.config.RetrofitServiceInstance
import com.github.andriytyranovets.ifoutages.datastore.DataStoreRepository
import com.github.andriytyranovets.ifoutages.models.OutageDuration
import com.github.andriytyranovets.ifoutages.models.OutageHour
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MainViewModel(private val dataRepository: DataStoreRepository) : ViewModel() {

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as IFOutagesApplication)
                MainViewModel(app.dataStoreRepository)
            }
        }
    }
    private val apiService = RetrofitServiceInstance.outagesApi

    val accountNumber: StateFlow<String?> = dataRepository.accountNumber
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = null
        )

    val lastUpdate: StateFlow<LocalDateTime?> = dataRepository.lastUpdate
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = null
        )

    private val _outages = MutableLiveData<Map<String, List<OutageDuration>>>()
    val outages: LiveData<Map<String, List<OutageDuration>>> = _outages;

    private val _queue = MutableLiveData<String>()
    val queue: LiveData<String> = _queue;

    suspend fun updateAccountNumber(accountNumber: String) {
        if(isValidNumber(accountNumber)) {
            viewModelScope.launch {
                dataRepository.saveAccountNumber(accountNumber)
            }.invokeOnCompletion {
                this.fetchOutages(accountNumber)
            }
        }
    }

    fun fetchOutages(accountNumber: String) {
        viewModelScope.launch {
//            if(lastUpdate.lastOrNull()?.isBefore(LocalDateTime.now().minusMinutes(15)) == true) {
                val res = apiService.getOutages(accountNumber)
                Log.i("Main View Model", "${lastUpdate.value}")
                if (res.graphs.today != null || res.graphs.tomorrow != null) {
                    _outages.value = mapOf(
                        "Today" to (res.graphs.today?.hoursList
                            ?.filter { h -> h.electricity > 0 }
                            ?.fold(mutableListOf()) { acc, h -> folder(acc, h, LocalDate.now()) }
                            ?: emptyList()),
                        "Tomorrow" to (res.graphs.tomorrow?.hoursList
                            ?.filter { h -> h.electricity > 0 }
                            ?.fold(mutableListOf()) { acc, h ->
                                folder(
                                    acc,
                                    h,
                                    LocalDate.now().plusDays(1)
                                )
                            }
                            ?: emptyList())
                    )
                }
                _queue.value = "${res.current.queue}.${res.current.subqueue}"
//            }
        }.invokeOnCompletion { ex ->
            if(ex != null) {
                if(isValidNumber(accountNumber)) {
                    // reschedule
                }
            } else {
                viewModelScope.launch { dataRepository.saveLastUpdate(LocalDateTime.now()) }
            }
        }
    }

    private fun isValidNumber(accountNumber: String) =
        accountNumber.isNotEmpty() && accountNumber.isDigitsOnly() && accountNumber.length == 8

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