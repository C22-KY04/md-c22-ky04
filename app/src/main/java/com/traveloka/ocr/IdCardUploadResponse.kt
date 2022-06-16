package com.traveloka.ocr

import com.google.gson.annotations.SerializedName

data class IdCardUploadResponse(

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)
