package com.traveloka.ocr

import com.google.gson.annotations.SerializedName

data class KtpResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class DataItem(

	@field:SerializedName("id_number")
	val idNumber: String,

	@field:SerializedName("address")
	val address: String,

	@field:SerializedName("occupation")
	val occupation: String,

	@field:SerializedName("gender")
	val gender: String,

	@field:SerializedName("expiry_date")
	val expiryDate: String,

	@field:SerializedName("religion")
	val religion: String,

	@field:SerializedName("uid")
	val uid: String,

	@field:SerializedName("marital_status")
	val maritalStatus: String,

	@field:SerializedName("province")
	val province: String,

	@field:SerializedName("nationality")
	val nationality: String,

	@field:SerializedName("attachment")
	val attachment: String,

	@field:SerializedName("subdistrict")
	val subdistrict: String,

	@field:SerializedName("district")
	val district: String,

	@field:SerializedName("blood_type")
	val bloodType: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("place_date_of_birth")
	val placeDateOfBirth: String,

	@field:SerializedName("neighborhood")
	val neighborhood: String,

	@field:SerializedName("village")
	val village: String
)
