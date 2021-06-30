package com.emq.camera.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.emq.camera.MainActivity
import com.emq.camera.R
import com.emq.camera.databinding.FragmentCameraSelectionBinding
import com.emq.camera.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val PERMISSIONS_REQUEST_CODE = 10
val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class CameraSelectionFragment : Fragment() {
    private var _binding: FragmentCameraSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: CameraSelectionFragmentArgs by navArgs()
    private var imageUri: Uri? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                handleCapturePicture()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions(requireContext())) {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOpenCamera.setOnClickListener {
            launchTakePhoto()
        }

        binding.btnOpenCameraLocal.setOnClickListener {
            navigateToCamera()
        }

        args.picturePath?.let {
            Glide.with(binding.imgCamera)
                .load(it)
                .fitCenter()
                .into(binding.imgCamera)
        }
    }

    private fun launchTakePhoto() {
        lifecycleScope.launch {
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                MainActivity.createFile(
                    MainActivity.getOutputDirectory(requireContext()),
                    MainActivity.FILENAME,
                    MainActivity.PHOTO_EXTENSION
                )
            )
            getContent.launch(imageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                // Take the user to the success fragment when permission is granted
                Toast.makeText(context, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
                activity?.finish()
            }
        }
    }

    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                CameraSelectionFragmentDirections.actionCameraSelectionToCamera()
            )
        }
    }

    private fun handleCapturePicture() {
        lifecycleScope.launch {
            var rotatedBitmap: Bitmap?
            withContext(Dispatchers.IO) {
                rotatedBitmap =
                    imageUri?.let { uri ->
                        BitmapUtils.handleSamplingAndRotationBitmap(requireContext(), uri)
                    }
            }
            withContext(Dispatchers.Main) {
                Glide.with(binding.imgCamera)
                    .load(rotatedBitmap)
                    .fitCenter()
                    .into(binding.imgCamera)
            }
        }
    }

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
