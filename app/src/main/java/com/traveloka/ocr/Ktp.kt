package com.traveloka.ocr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ktp(
    var nik: String? = null,
    var name: String? = null
) : Parcelable
