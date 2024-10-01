package com.ripenapps.greenhouse.model.seller.response

data class WalletHistoryResponseModel(
    val `data`: Data? = null, val message: String? = null, val status: Int? = null
) {
    data class Data(
        val availableWalletAmount: Double? = null,
        val freezedWalletAmount: Double? = null,
        val totalCount: Double? = null,
        val walletHistory: List<WalletHistory>? = null,
        val walletTotalAmount: Double? = null
    ) {
        data class WalletHistory(
            val __v: Int,
            val _id: String,
            val amount: Double,
            val createdAt: String,
            val transactionId: String,
            val transactionSource: String,
            val transactionType: String,
            val updatedAt: String,
            val userId: String
        )
    }
}