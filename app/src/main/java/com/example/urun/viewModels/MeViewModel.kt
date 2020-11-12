package com.example.urun.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MeViewModel: ViewModel() {

    val isEditSelectedLiveData = MutableLiveData<Boolean>()

    init {
        isEditSelectedLiveData.value = false
    }

    fun editButtonClicked(){
        isEditSelectedLiveData.value = !isEditSelectedLiveData.value!!
    }

}