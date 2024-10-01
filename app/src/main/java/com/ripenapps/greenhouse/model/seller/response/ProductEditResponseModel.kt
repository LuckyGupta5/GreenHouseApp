package com.ripenapps.greenhouse.model.seller.response

import com.google.gson.annotations.SerializedName
import com.ripenapps.greenhouse.model.product_details.Image
import com.ripenapps.greenhouse.model.user_details.Seller

data class ProductEditResponseModel(
    val `data`: Data? = null,
    val message: String? = null,
    val status: Int? = null
) {
    data class Data(
        val product: EditProduct? = null
    )

    data class EditProduct(
        @SerializedName("categoryId") val categoryId: String? = null,
        @SerializedName("countryCode") val countryCode: String? = null,
        @SerializedName("createdAt") val createdAt: String? = null,
        @SerializedName("description") val description: String? = null,
        @SerializedName("endDate") val endDate: String? = null,
        @SerializedName("endTime") val endTime: String? = null,
        @SerializedName("_id") val id: String? = null,
        @SerializedName("imageUrl") val imageUrl: List<String>? = null,
        @SerializedName("images") val images: List<Image>? = null,
        @SerializedName("isDeleted") val isDeleted: Boolean? = null,
        @SerializedName("mobile") val mobile: String? = null,
        @SerializedName("price") val price: Double? = null,
        @SerializedName("productLocation") val productLocation: String? = null,
        @SerializedName("quantity") val quantity: Int? = null,
        @SerializedName("sellerId") val sellerId: String? = null,
        @SerializedName("startDate") val startDate: String? = null,
        @SerializedName("startTime") val startTime: String? = null,
        @SerializedName("status") val status: Boolean? = null,
        @SerializedName("subCategoryId") val subCategoryId: String? = null,
        @SerializedName("unit") val unit: String? = null,
        @SerializedName("updatedAt") val updatedAt: String? = null,
        @SerializedName("__v") val v: Int? = null,
        @SerializedName("seller") val seller: Seller? = null
    )
}

