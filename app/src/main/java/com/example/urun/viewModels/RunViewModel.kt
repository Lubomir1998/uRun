package com.example.urun.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urun.data.Run
import com.example.urun.repository.Repository
import kotlinx.coroutines.launch

class RunViewModel
@ViewModelInject constructor(private val repository: Repository) : ViewModel() {


    fun insertRun(run: Run){
        viewModelScope.launch {
            repository.insert(run)
        }
    }

    fun deleteRun(run: Run){
        viewModelScope.launch {
            repository.delete(run)
        }
    }


}