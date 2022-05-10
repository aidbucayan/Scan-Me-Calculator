package com.adrian.bucayan.scanmecalculator.presentation

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {

    init {

    }

    private val capturedImage: MutableLiveData<String> = MutableLiveData<String>()

    fun setImageUri(uri: String?) {
        capturedImage.value = uri!!
    }

    fun getImageUri(): MutableLiveData<String> {
        return capturedImage
    }



}