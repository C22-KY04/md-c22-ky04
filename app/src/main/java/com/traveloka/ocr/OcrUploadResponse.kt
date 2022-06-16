package com.traveloka.ocr

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class OcrUploadResponse(

	@field:SerializedName("data")
	val data: Data,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

@Parcelize
data class Data(

	@field:SerializedName("id_number")
	val idNumber: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("occupation")
	val occupation: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("expiry_date")
	val expiryDate: String? = null,

	@field:SerializedName("religion")
	val religion: String? = null,

	@field:SerializedName("marital_status")
	val maritalStatus: String? = null,

	@field:SerializedName("province")
	val province: String? = null,

	@field:SerializedName("nationality")
	val nationality: String? = null,

	@field:SerializedName("attachment")
	val attachment: String? = null,

	@field:SerializedName("subdistrict")
	val subdistrict: String? = null,

	@field:SerializedName("district")
	val district: String? = null,

	@field:SerializedName("blood_type")
	val bloodType: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("place_date_of_birth")
	val placeDateOfBirth: String,

	@field:SerializedName("neighborhood")
	val neighborhood: String? = null,

	@field:SerializedName("village")
	val village: String? = null
) : Parcelable
