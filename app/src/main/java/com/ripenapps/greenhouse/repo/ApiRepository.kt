package com.ripenapps.greenhouse.repo

import com.ripenapps.greenhouse.abstracts.BaseRepository
import com.ripenapps.greenhouse.model.CommonReqModel
import com.ripenapps.greenhouse.retrofit.RetrofitBuilder
import okhttp3.MultipartBody

class ApiRepository : BaseRepository() {
    private val service = RetrofitBuilder.apiService

    suspend fun registerApi(request: MultipartBody) = service.registerApi(request)

    // make function in api service class to define request and response like login function
    suspend fun logInApi(request: CommonReqModel) = service.login(request)

    suspend fun sendOtpApi(request: CommonReqModel) = service.sendOtp(request)

    suspend fun verifyOtpApi(request: CommonReqModel) = service.verifyOtp(request)

    suspend fun forgotPasswordApi(request: CommonReqModel) = service.forgotPassword(request)

    suspend fun resetPasswordApi(request: CommonReqModel) = service.resetPassword(request)

    suspend fun checkProfileApi(request: CommonReqModel) = service.checkProfile(request)

    suspend fun profileAddressAddEditApi(request: CommonReqModel) =
        service.profileAddressAddEdit(request)

    suspend fun changePasswordApi(request: CommonReqModel) = service.changePassword(request)

    suspend fun categoryApi(request: String) = service.getCategory(request)

    suspend fun addProduct(request: MultipartBody) = service.addProduct(request)

    suspend fun productListApi(request: String) = service.productList(request)

    suspend fun productDetailsApi(request: String) = service.productDetails(request)
    suspend fun productDeleteApi(request: String) = service.productDelete(request)

    suspend fun productEditApi(request: CommonReqModel) = service.productEdit(request)

    suspend fun addressListApi(request: String) = service.addressList(request)

    suspend fun bidderProductDetailApi(request: String) = service.bidderProductDetail(request)

    suspend fun bidderProductListApi(request: String) = service.bidderProductList(request)

    suspend fun profileDeleteApi(request: String) = service.profileDelete(request)

    suspend fun profileAddressListApi(request: String) = service.profileAddressList(request)

    suspend fun bidderProductSameSellerApi(request: String) =
        service.bidderProductSameSeller(request)

    suspend fun bidderGetWishlistApi(request: String) = service.bidderGetWishlist(request)

    suspend fun logoutApi(request: CommonReqModel) = service.logout(request)

    suspend fun bidderGetFavoriteSellerApi(request: String) =
        service.bidderGetFavoriteSeller(request)

    suspend fun getUserDetailsApi(request: String) = service.getUserDetails(request)

    suspend fun switchProfileApi(request: MultipartBody) = service.switchProfile(request)

    suspend fun addWishList(request: CommonReqModel) = service.addWishList(request)

    suspend fun editProfileApi(request: CommonReqModel) = service.editProfile(request)

    suspend fun getSellerApi(request: String) = service.getSeller(request)

    suspend fun getHighestBidApi(request: String) = service.getHighestBid(request)

    suspend fun getAddBidApi(request: CommonReqModel) = service.getAddBid(request)

    suspend fun getBidHighestDetailApi(request: String) = service.bidHighestDetail(request)

    suspend fun getBidHighestHistoryAPi(request: String) = service.bidHighestHistory(request)

    suspend fun getMyBidsApi(request: String) = service.getMyBids(request)

    suspend fun getBidInProgressApi(request: String) = service.getBidInProgress(request)

    suspend fun getSellerMyBidsApi(request: String) = service.getSellerMYBids(request)

    suspend fun getSellerMyOrdersApi(request: String) = service.getSellerMyOrders(request)

    suspend fun getSellerMyOrdersDetailsApi(request: String) =
        service.getSellerGetOrderDetails(request)

    suspend fun getReviewsApi(request: String) = service.getReviews(request)

    suspend fun getOrderPlaceApi(request: CommonReqModel) = service.getOrderPlace(request)

    suspend fun bidderGetMyOrders(request: String) = service.bidderGetMyOrders(request)

    suspend fun bidderOrderDetailsApi(request: String) = service.bidderOrderDetail(request)

    suspend fun bidderBidDetailsApi(request: String) = service.bidderBidDetails(request)

    suspend fun accountDeleteApi(request: String) = service.accountDelete(request)

    suspend fun getOrderPackedApi(request: CommonReqModel) = service.orderPacked(request)

    suspend fun getOnGoingFeatureBidding(request: String) = service.ongoingFeatureBidding(request)

    suspend fun cancelOrderApi(request: CommonReqModel) = service.cancelOrder(request)

    suspend fun addReviewApi(request: CommonReqModel) = service.addReview(request)

    suspend fun getBestSellerApi(request: String) = service.getBestSeller(request)

    suspend fun getSecondHighestBidderApi(request: CommonReqModel) =
        service.getSecondHighestBidder(request)

    suspend fun getFeaturedProductApi(request: String) = service.getFeaturedProduct(request)

    suspend fun getWalletHistory(request: String) = service.getWalletHistory(request)

    suspend fun getWalletAdd(request: CommonReqModel) = service.getWalletAdd(request)

    suspend fun getWalletWithdraw(request: CommonReqModel) = service.getWalletWithdraw(request)

    suspend fun getOrderSupportApi(request: String) = service.getOrderSupport(request)

    suspend fun uploadImageApi(request: MultipartBody) = service.imageUpload(request)

    suspend fun getDownloadInvoice(request: CommonReqModel) = service.getDownloadInvoice(request)

    suspend fun getSoldProduct(request: String) = service.getSoldProduct(request)

    suspend fun supportListingApi(request: CommonReqModel) = service.supportListing(request)

    suspend fun getHomeGraphApi(request: String) = service.getHomeGraph(request)

    suspend fun getSearchProductApi(request: String) = service.getSearchProductList(request)

    suspend fun getNotificationListApi(request: String) = service.getNotificationList(request)

    suspend fun getFaqListApi(request: String) = service.getFaqList(request)

    suspend fun createPaymentApi(request: CommonReqModel) = service.createPayment(request)

    suspend fun rejectOrderApi(request: CommonReqModel) = service.rejectOrder(request)
}
