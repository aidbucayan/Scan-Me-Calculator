package com.adrian.bucayan.scanmecalculator.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtility {

    const val PERMISSIONS_REQUEST_CODE = 10
    val PERMISSIONS_REQUIRED_CAMERA = arrayOf(Manifest.permission.CAMERA)
    val PERMISSIONS_REQUIRED_FILE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    fun hasPermissionsCamera(context: Context) = PERMISSIONS_REQUIRED_CAMERA.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPermissionsFile(context: Context) = PERMISSIONS_REQUIRED_FILE.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

}