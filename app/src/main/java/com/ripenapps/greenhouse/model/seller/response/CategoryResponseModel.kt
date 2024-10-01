package com.ripenapps.greenhouse.model.seller.response

data class CategoryResponseModel(
    val `data`: CategoryListModel?=null,
    val message: String?=null,
    val status: Int?=null
) {
    data class CategoryListModel(
        val categoriesWithSubcategories: List<CategoriesWithSubcategory>?=null
    ) {
        data class CategoriesWithSubcategory(
            val __v: Int,
            val _id: String,
            val arName: String,
            val createdAt: String,
            val enName: String,
            val image: String,
            val status: Boolean,
            val subCategories: List<SubCategory>?=null,
            val updatedAt: String
        ) {
            data class SubCategory(
                val __v: Int,
                val _id: String,
                val arName: String,
                val categoryId: String,
                val createdAt: String,
                val enName: String,
                val status: Boolean,
                val updatedAt: String
            )
        }
    }
}