package ir.mhdolatabadi.atid.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ir.mhdolatabadi.atid.data.QadaPrayer
import ir.mhdolatabadi.atid.data.QadaRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QadaRepository(application)

    private val _prayerCounts = MutableLiveData<Map<QadaPrayer, Int>>()
    val prayerCounts: LiveData<Map<QadaPrayer, Int>> = _prayerCounts

    private val _fastCount = MutableLiveData<Int>()
    val fastCount: LiveData<Int> = _fastCount

    init {
        refresh()
    }

    fun incrementPrayer(prayer: QadaPrayer) {
        repository.incrementPrayer(prayer)
        refresh()
    }

    fun decrementPrayer(prayer: QadaPrayer) {
        repository.decrementPrayer(prayer)
        refresh()
    }

    fun incrementFast() {
        repository.incrementFast()
        refresh()
    }

    fun decrementFast() {
        repository.decrementFast()
        refresh()
    }

    private fun refresh() {
        _prayerCounts.value = QadaPrayer.values().associateWith { repository.getPrayerCount(it) }
        _fastCount.value = repository.getFastCount()
    }
}
