package com.adrian.bucayan.scanmecalculator.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.adrian.bucayan.scanmecalculator.BuildConfig
import com.adrian.bucayan.scanmecalculator.databinding.FragmentMainBinding
import com.adrian.bucayan.scanmecalculator.presentation.util.Constants
import com.adrian.bucayan.scanmecalculator.presentation.util.PermissionUtility
import com.adrian.bucayan.scanmecalculator.presentation.util.cropper.CropImageContract
import com.adrian.bucayan.scanmecalculator.presentation.util.cropper.CropImageView
import com.adrian.bucayan.scanmecalculator.presentation.util.cropper.options
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val PHOTO_PICKER_REQUEST_CODE = 20
    private val CAMERA_PICKER_REQUEST_CODE = 30
    var image_uri: Uri? = null

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

        setFragmentResultListener(Constants.CAMERA_URI) {
                requestKey, bundle ->
            Timber.e("requestKey === %s", requestKey)
            Timber.e("bundle === %s", bundle.getString(Constants.BUNDLE_URI))

            val uriData : Uri = Uri.parse(bundle.getString(Constants.BUNDLE_URI))

            startCrop(uriData)

        }

    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
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
        Timber.d("navigateToCamera")
         lifecycleScope.launchWhenStarted {
            findNavController().apply {
                this.navigate(MainFragmentDirections.actionPermissionsToCamera())
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            PHOTO_PICKER_REQUEST_CODE -> {
                //data.getData returns the content URI for the selected Image
                val selectedImage: Uri? = data?.data
                Timber.e("selectedImage = %s", selectedImage)
                clearInputAndResult()
                if (selectedImage != null) {
                    startCrop(selectedImage)
                }
                //runTextRecognition(selectedImage)
            }
            CAMERA_PICKER_REQUEST_CODE -> {
                clearInputAndResult()
                runTextRecognition(image_uri)
            }
        }
    }

    private fun clearInputAndResult() {
        binding.TvInput.text = "input : "
        binding.TvResult.text = "result :"
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            runTextRecognition(uriContent)
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    private fun startCrop(uriData: Uri) {
        // start cropping activity for pre-acquired image saved on the device and customize settings
        cropImage.launch(
            options(uri = uriData) {
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
            }
        )
    }


    @SuppressLint("SetTextI18n")
    private fun runTextRecognition(selectedImage: Uri?) {
        val recognizer = TextRecognizer.Builder(requireActivity().applicationContext).build()
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
        if (!recognizer.isOperational) {
            Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show()
        } else {
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val items = recognizer.detect(frame)
            val sb = StringBuilder()
            for (i in 0 until items.size()) {
                val myItem = items.valueAt(i)
                sb.append(myItem.value)
            }
            binding.TvInput.text = "input : " + sb.toString()
            computeForResult(sb.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun computeForResult(text: String) {
        var newText : String = text
        var splitStrings = text.split(" ")
        if (splitStrings.size < 2) {
            newText = newText.replace("+", " + " )
            newText = newText.replace("-", " - " )
            newText = newText.replace("/", " / " )
            newText = newText.replace("*", " * " )
            newText = newText.replace("x", " * " ) //if x
        }

        splitStrings = newText.split(" ") // split again
        if(splitStrings.size > 2) {
            var result: Int = 0
            val left = splitStrings[0].toInt()
            val op = splitStrings[1]
            val right = splitStrings[2].toInt()
            when(op) {
                "+" -> {
                    result = left + right
                }
                "-" -> {
                    result = left - right
                }
                "/" -> {
                    result = left  / right
                }
                "*" -> {
                    result = left  * right
                }
            }
            binding.TvResult.text = "result = $result"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}