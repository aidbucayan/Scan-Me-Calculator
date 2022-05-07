package com.adrian.bucayan.scanmecalculator.presentation

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.adrian.bucayan.scanmecalculator.BuildConfig
import com.adrian.bucayan.scanmecalculator.R
import com.adrian.bucayan.scanmecalculator.databinding.FragmentMainBinding
import com.adrian.bucayan.scanmecalculator.presentation.util.PermissionUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    val PHOTO_PICKER_REQUEST_CODE = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.BtnInput.setOnClickListener {
            requestPermission()
        }
    }

    private fun requestPermission() {
        if(BuildConfig.INPUT.equals("camera", true)) {
            if (!PermissionUtility.hasPermissionsCamera(requireContext())) {
                requestPermissions(PermissionUtility.PERMISSIONS_REQUIRED_CAMERA, PermissionUtility.PERMISSIONS_REQUEST_CODE)
            } else {
                navigateToCamera()
            }
        } else {
            if (!PermissionUtility.hasPermissionsFile(requireContext())) {
                requestPermissions(PermissionUtility.PERMISSIONS_REQUIRED_FILE, PermissionUtility.PERMISSIONS_REQUEST_CODE)
            } else {
                pickFromGallery()
            }
        }
    }

    private fun pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Launching the Intent
        startActivityForResult(intent, PHOTO_PICKER_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtility.PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                // Take the user to the success fragment when permission is granted
                Toast.makeText(context, "Permission request granted", Toast.LENGTH_LONG).show()

                if(BuildConfig.INPUT.equals("camera", true)) {
                    navigateToCamera()
                } else {
                    Timber.d("File system")
                }

            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                MainFragmentDirections.actionPermissionsToCamera())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            PHOTO_PICKER_REQUEST_CODE -> {
                //data.getData returns the content URI for the selected Image
                val selectedImage: Uri? = data?.data
                Timber.d("selectedImage = %s", selectedImage)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}