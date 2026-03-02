package com.example.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calendar.RoomDatabase.RegionDao
import com.example.calendar.RoomDatabase.RegionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RegionViewModel(private val dao: RegionDao) : ViewModel() {

    val regions: Flow<List<RegionEntity>> = dao.getAllRegions()

    fun removeRegion(code: String) {
        viewModelScope.launch {
            dao.deleteRegion(code)
        }
    }
}


class RegionViewModelFactory(
    private val dao: RegionDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegionViewModel(dao) as T
    }
}

