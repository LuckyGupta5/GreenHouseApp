package com.ripenapps.greenhouse.datamodels.biddermodel

data class FaqQuestionModel(
    var question: String? = null,
    var answer: String? = null,
    var isItemVisible: Boolean = false
)