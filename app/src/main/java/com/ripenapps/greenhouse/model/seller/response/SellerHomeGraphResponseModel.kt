package com.ripenapps.greenhouse.model.seller.response

data class SellerHomeGraphResponseModel(
    val `data`: Data?=null,
    val message: String,
    val status: Int
) {
    data class Data(
        val newData: NewData
    ) {
        data class NewData(
            val revenue: List<Revenue>,
            val totalPercentageOfCategories: List<TotalPercentageOfCategory>
        ) {
            data class Revenue(
                val categories: List<Category>,
                val month: String
            ) {
                data class Category(
                    var categoryId: String?=null,
                    val totalAmount: Float,
                    val totalOrders: Float
                )
            }

            data class TotalPercentageOfCategory(
                val categoryId: String,
                val totalPercentage: Double
            )
        }
    }
}