package com.emq.camera

import android.net.Uri
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    var localPicturePath = ""
    var outsidePicturePath: Uri? = null
}