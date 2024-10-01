package com.ripenapps.greenhouse.utills

fun <T> List<T>?.isValidList(position: Int = -1): Boolean {
    this ?: return false
    return position in this.indices
}
