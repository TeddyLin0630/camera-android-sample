package com.emq.camera.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.emq.camera.R
import com.emq.camera.databinding.FragmentGalleryBinding
import com.emq.camera.utils.padWithDisplayCutout
import java.io.File
import java.util.*

val EXTENSION_WHITELIST = arrayOf("JPG")

class GalleryFragment internal constructor() : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>

    inner class MediaPagerAdapter(fm: Fragment) : FragmentStateAdapter(fm) {
        override fun getItemCount(): Int = mediaList.size
        override fun createFragment(position: Int): Fragment =
            PhotoFragment.create(mediaList[position])
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootDirectory = File(args.rootDirectory)
        val files = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.uppercase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList()
        mediaList = files?.let { mutableListOf(it.first()) } ?: mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Checking media files list
        if (mediaList.isEmpty()) {
            binding.btnYes.isEnabled = false
        }

        binding.photoViewPager.apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(this@GalleryFragment)
        }

        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            binding.cutoutSafeArea.padWithDisplayCutout()
        }

        // Handle back button press
        binding.btnBack.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }
        binding.btnYes.setOnClickListener {
            if (!mediaList.first().absolutePath.isNullOrEmpty()) {
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(GalleryFragmentDirections.actionGalleryToPermission(mediaList.first().absolutePath))
            }
        }
    }
}
