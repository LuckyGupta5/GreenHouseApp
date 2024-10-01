package com.ripenapps.greenhouse.retrofit

import com.ripenapps.greenhouse.model.CommonReqModel
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {
    @POST("user/auth/register")
    suspend fun registerApi(@Body request: MultipartBody): String

    @POST("user/auth/login")
    suspend fun login(@Body request: CommonReqModel): String

    @POST("user/auth/send/otp")
    suspend fun sendOtp(@Body request: CommonReqModel): String

    @POST("user/auth/verify/otp")
    suspend fun verifyOtp(@Body request: CommonReqModel): String

    @POST("user/auth/forgotPassword/sendOtp")
    suspend fun forgotPassword(@Body request: CommonReqModel): String

    @POST("user/auth/resetPassword")
    suspend fun resetPassword(@Body request: CommonReqModel): String

    @POST("user/auth/check-profile")
    suspend fun checkProfile(@Body request: CommonReqModel): String

    @POST("user/profile/address/add/edit")
    suspend fun profileAddressAddEdit(@Body request: CommonReqModel): String

    @POST("user/profile/changePassword")
    suspend fun changePassword(@Body request: CommonReqModel): String

    @GET("user/profile/address/lists")
    suspend fun addressList(@Query("reqData") request: String): String

    @GET("seller/getCategoriesWithSubcategories")
    suspend fun getCategory(@Query("reqData") request: String): String

    @POST("seller/product/add")
    suspend fun addProduct(@Body request: MultipartBody): String

    @GET("seller/product")
    suspend fun productList(@Query("reqData") request: String): String

    @GET("seller/product/details")
    suspend fun productDetails(@Query("reqData") request: String): String

    @POST("seller/product/edit")
    suspend fun productEdit(@Body request: CommonReqModel): String

    @DELETE("seller/product/delete")
    suspend fun productDelete(@Query("reqData") request: String): String

    @GET("bidder/product")
    suspend fun bidderProductList(@Query("reqData") request: String): String

    @DELETE("user/profile/address/delete")
    suspend fun profileDelete(@Query("reqData") request: String): String

    @GET("user/profile/address/lists")
    suspend fun profileAddressList(@Query("reqData") request: String): String

    @GET("bidder/product/details")
    suspend fun bidderProductDetail(@Query("reqData") request: String): String

    @GET("bidder/product/sellerProductLists")
    suspend fun bidderProductSameSeller(@Query("reqData") request: String): String

    @GET("bidder/product/getWishList")
    suspend fun bidderGetWishlist(@Query("reqData") request: String): String

    @POST("user/auth/logout")
    suspend fun logout(@Body request: CommonReqModel): String

    @GET("bidder/getFavoriteSeller")
    suspend fun bidderGetFavoriteSeller(@Query("reqData") request: String): String

    @GET("user/profile/getUserDetails")
    suspend fun getUserDetails(@Query("reqData") request: String): String

    @POST("user/profile/switch")
    suspend fun switchProfile(@Body request: MultipartBody): String

    @POST("bidder/product/addWishList")
    suspend fun addWishList(@Body request: CommonReqModel): String

    @POST("user/profile/edit")
    suspend fun editProfile(@Body request: CommonReqModel): String

    @GET("bidder/getSeller")
    suspend fun getSeller(@Query("reqData") request: String): String

    @GET("seller/bid/highest-list")
    suspend fun getHighestBid(@Query("reqData") request: String): String

    @POST("seller/bid/add")
    suspend fun getAddBid(@Body request: CommonReqModel): String

    @GET("seller/bid/highest-detail")
    suspend fun bidHighestDetail(@Query("reqData") request: String): String

    @GET("seller/bid/bidding-history")
    suspend fun bidHighestHistory(@Query("reqData") request: String): String

    @GET("bidder/mybid/getmyBids")
    suspend fun getMyBids(@Query("reqData") request: String): String

    @GET("bidder/mybid/pending-myBids")
    suspend fun getBidInProgress(@Query("reqData") request: String): String

    @GET("seller/bid/myBids")
    suspend fun getSellerMYBids(@Query("reqData") request: String): String

    @GET("seller/order/getMyOrders")
    suspend fun getSellerMyOrders(@Query("reqData") request: String): String

    @GET("seller/order/getOrder")
    suspend fun getSellerGetOrderDetails(@Query("reqData") request: String): String

    @GET("bidder/review/getReviews")
    suspend fun getReviews(@Query("reqData") request: String): String

    @POST("bidder/order/add")
    suspend fun getOrderPlace(@Body request: CommonReqModel): String

    @GET("bidder/order/getMyOrders")
    suspend fun bidderGetMyOrders(@Query("reqData") request: String): String

    @GET("bidder/order/getOrder")
    suspend fun bidderOrderDetail(@Query("reqData") request: String): String

    @GET("bidder/mybid/bidDetails")
    suspend fun bidderBidDetails(@Query("reqData") request: String): String

    @DELETE("user/profile/delete")
    suspend fun accountDelete(@Query("reqData") request: String): String

    @POST("seller/order/packed")
    suspend fun orderPacked(@Body request: CommonReqModel): String

    @GET("seller/bid/onging-featured-bids")
    suspend fun ongoingFeatureBidding(@Query("reqData") request: String): String

    @POST("bidder/order/cancelOrder")
    suspend fun cancelOrder(@Body request: CommonReqModel): String

    @POST("bidder/review/addUpdate")
    suspend fun addReview(@Body request: CommonReqModel): String

    @GET("bidder/getBestSellers")
    suspend fun getBestSeller(@Query("reqData") request: String): String

    @POST("seller/order/assign-second-highest-bidder")
    suspend fun getSecondHighestBidder(@Body request: CommonReqModel): String

    @GET("bidder/featured-product")
    suspend fun getFeaturedProduct(@Query("reqData") request: String): String

    @GET("user/support-chat-list")
    suspend fun getOrderSupport(@Query("reqData") request: String): String

    @POST("user/support-chat")
    suspend fun supportListing(@Body request: CommonReqModel): String

    @POST("user/image-upload")
    suspend fun imageUpload(@Body request: MultipartBody): String

    @GET("user/wallet-history")
    suspend fun getWalletHistory(@Query("reqData") request: String): String

    @POST("user/add-wallet-amount")
    suspend fun getWalletAdd(@Body request: CommonReqModel): String

    @POST("user/withdraw-amount")
    suspend fun getWalletWithdraw(@Body request: CommonReqModel): String

    @POST("seller/order/download-invoice")
    suspend fun getDownloadInvoice(@Body request: CommonReqModel): String

    @GET("seller/order/sold-products")
    suspend fun getSoldProduct(@Query("reqData") request: String): String

    @GET("seller/graph-data")
    suspend fun getHomeGraph(@Query("reqData") request: String): String

    @GET("bidder/home-product-list")
    suspend fun getSearchProductList(@Query("reqData") request: String): String

    @GET("user/auth/notification-list")
    suspend fun getNotificationList(@Query("reqData") request: String): String

    @GET("user/faq-list")
    suspend fun getFaqList(@Query("reqData") request: String): String

    @POST("user/create-payment")
    suspend fun createPayment(@Body request: CommonReqModel): String

    @POST("bidder/order/reject-order")
    suspend fun rejectOrder(@Body request: CommonReqModel): String
}