package com.ripenapps.greenhouse.screen.bidder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerFaqQuestionAdapter
import com.ripenapps.greenhouse.databinding.ActivityOrderDetailHelpBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.ChatDataModel
import com.ripenapps.greenhouse.datamodels.biddermodel.FaqQuestionModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.GetOrderSupportResponseModel
import com.ripenapps.greenhouse.model.bidder.response.GetRoomIdResponse
import com.ripenapps.greenhouse.model.change_password.ChangePasswordResponseModel
import com.ripenapps.greenhouse.model.help.SupportChatViewModel
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.formatDate
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.ROOM_DATA
import com.ripenapps.greenhouse.utills.Constants.TICKET_ID
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.GetOrderSupportViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject

class OrderDetailHelp : BaseActivity() {
    private lateinit var binding: ActivityOrderDetailHelpBinding
    private var getOrderSupportViewModel: GetOrderSupportViewModel? = null
    private var accountBlocked: AccountBlocked? = null
    private var roomId: Long? = null
    private var ticketId: String? = null
    private var recyclerAdapter: RecyclerFaqQuestionAdapter? = null
    private var arrQuestion: ArrayList<FaqQuestionModel>? = arrayListOf()
    private var socket: Socket? = null
    private var loginData: LoginResponseModel.LoginData? = null
    private var supportChatViewModel: SupportChatViewModel? = null
    private lateinit var getContent: ActivityResultLauncher<Intent>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(this@OrderDetailHelp, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_detail_help)
        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@OrderDetailHelp, "user_details"),
            LoginResponseModel.LoginData::class.java
        )
        ticketId = intent.getStringExtra(TICKET_ID)
        binding.orderId.text = getString(R.string.order_id) + intent.getStringExtra(TICKET_ID)
        Log.d("TAG", "TICKET_NUMBER: ${intent.getStringExtra(TICKET_ID)}")

        initListeners()
        initActiveConversationModel()
        orderHelpApi()
        initStartActivityInstance()
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        orderHelpApi()
                    }
                }
            }
    }

    private fun helpApi(model: FaqQuestionModel) {
        supportChatViewModel(model)
        supportChatViewModel?.request?.token =
            Preferences.getStringPreference(this@OrderDetailHelp, TOKEN)
        supportChatViewModel?.request?.supportType = "orderSupport"
        supportChatViewModel?.request?.ticketId = intent.getStringExtra(TICKET_ID)
        supportChatViewModel?.request?.title = model.question
        supportChatViewModel?.request?.userId = loginData?.userDetails?.id
        supportChatViewModel?.request?.roomId = roomId
        supportChatViewModel?.request?.message = model.answer
        supportChatViewModel?.getSupportChatRequest()
    }

    private fun generateRoomId(model: FaqQuestionModel) {
        if (!isFinishing && socket == null) {
            val socketUrl = "http://13.235.137.221:6002?userId=${loginData?.userDetails?.id}"
            socket = IO.socket(socketUrl)
            Log.d("TAG", "initSocket: $socketUrl")
            socket?.connect()
        }
        val dataRoom = JSONObject()
        dataRoom.put("receiverId", Constants.ADMIN_ID)
        dataRoom.put("userId", loginData?.userDetails?.id)
        dataRoom.put("senderType", "user")
        dataRoom.put("ticketId", intent.getStringExtra(TICKET_ID)?.toInt())
        dataRoom.put("receiverType", "admin")
        Log.d("TAG", "DATA_ROOM_CHAT: ${Gson().toJson(dataRoom)}")
        socket?.emit("initiateChat", dataRoom)
        // get roomId
        socket?.on("getRoomId") { args: Array<Any?> ->
            if (isFinishing) return@on
            val roomResponse = Gson().fromJson(
                JSONArray().put(args[0] as JSONObject?).get(0).toString(),
                GetRoomIdResponse::class.java
            )
            roomId = roomResponse.result?.roomId ?: 0 //1718715165865
            Log.d("TAG", "ROOM_ID: $roomId")
            runOnUiThread {
                helpApi(model)
            }
        }
    }

    private fun orderHelpApi() {
        getOrderSupportViewModel?.request?.token =
            Preferences.getStringPreference(this@OrderDetailHelp, TOKEN)
        getOrderSupportViewModel?.request?.supportType = "orderSupport"
        getOrderSupportViewModel?.request?.ticketId = intent.getStringExtra(TICKET_ID)
        getOrderSupportViewModel?.getOrderSupportRequest()
    }

    private fun supportChatViewModel(model: FaqQuestionModel) {
        supportChatViewModel =
            ViewModelProvider(this@OrderDetailHelp)[SupportChatViewModel::class.java]
        binding.lifecycleOwner = this@OrderDetailHelp
        supportChatViewModel?.getSupportChatData()?.observe(this@OrderDetailHelp) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(false)
                        binding.dataLayer.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            ChangePasswordResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Toast.makeText(
                                    this@OrderDetailHelp, data.message, Toast.LENGTH_SHORT
                                ).show()
                                val toCaseDetailChat =
                                    Intent(this@OrderDetailHelp, CaseDetailChat::class.java)
                                val dataModel = ChatDataModel(
                                    question = model.question,
                                    roomId = roomId,
                                    ticketId = intent.getStringExtra(TICKET_ID),
                                    supportType = "orderSupport"
                                )
                                toCaseDetailChat.putExtra(ROOM_DATA, Gson().toJson(dataModel))
                                getContent.launch(toCaseDetailChat)
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@OrderDetailHelp, "token")
                                Preferences.removePreference(this@OrderDetailHelp, "user_details")
                                Preferences.removePreference(this@OrderDetailHelp, "isVerified")
                                val signin = Intent(this@OrderDetailHelp, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(true)
                        binding.dataLayer.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(true)
                        binding.dataLayer.setVisibility(false)
                        Toast.makeText(this@OrderDetailHelp, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(true)
                binding.dataLayer.setVisibility(false)
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initActiveConversationModel() {
        getOrderSupportViewModel =
            ViewModelProvider(this@OrderDetailHelp)[GetOrderSupportViewModel::class.java]
        binding.lifecycleOwner = this@OrderDetailHelp
        getOrderSupportViewModel?.getOrderSupportData()?.observe(this@OrderDetailHelp) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(false)
                        binding.dataLayer.setVisibility(true)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetOrderSupportResponseModel::class.java
                        )
                        when (data.status) {
                            200 -> {
                                Log.d(
                                    "TAG",
                                    "GetOrderSupportResponseModel: ${Gson().toJson(data.data)}"
                                )
                                if (data.data?.list == null) {
                                    binding.apply {
                                        titleActiveConversations.setVisibility(false)
                                        orderDetailLayer.setVisibility(false)
                                    }
                                } else {
                                    binding.apply {
                                        titleActiveConversations.setVisibility(true)
                                        orderDetailLayer.setVisibility(true)
                                        setActiveConversationUi(data)
                                    }
                                }

                                initQuestion(data.data?.list != null)
                            }

                            402 -> {
                                if (accountBlocked?.isAdded != true) {
                                    accountBlocked = AccountBlocked(data.message.toString())
                                    accountBlocked?.show(
                                        supportFragmentManager, accountBlocked?.tag
                                    )
                                }
                            }

                            403, 401 -> {
                                Preferences.removePreference(this@OrderDetailHelp, TOKEN)
                                Preferences.removePreference(this@OrderDetailHelp, "user_details")
                                Preferences.removePreference(this@OrderDetailHelp, "isVerified")
                                val signin = Intent(this@OrderDetailHelp, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(false)
                                binding.dataLayer.setVisibility(true)
                                Toast.makeText(
                                    this@OrderDetailHelp, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(true)
                        binding.dataLayer.setVisibility(false)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.orderHelpShimmer.myProfileMainShimmer.setVisibility(true)
                        binding.dataLayer.setVisibility(false)
                        Toast.makeText(this@OrderDetailHelp, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setActiveConversationUi(data: GetOrderSupportResponseModel) {
        binding.apply {
            roomId = data.data?.list?.roomId
            ticketId = data.data?.list?.ticketId
            orderId.text =
                getString(R.string.order_id) + " " + data.data?.list?.ticketId
            titleItemMissing.text = data.data?.list?.title
            dayAndTime.text =
                formatDate(data.data?.list?.createdAt.toString())

            if (data.data?.list?.status == true) {
                orderStatus.text = getString(R.string.open)
                orderStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_i_yellow, 0, 0, 0
                )
                orderStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.open_ticket_color
                    )
                )
            } else {
                orderStatus.text = getString(R.string.solved)
                orderStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_i_green, 0, 0, 0
                )
                orderStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.greenhousetheme
                    )
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initQuestion(status: Boolean) {
        arrQuestion?.add(
            FaqQuestionModel(
                getString(R.string.question1), getString(R.string.answer1)
            )
        )

        arrQuestion?.add(
            FaqQuestionModel(
                getString(R.string.question2), getString(R.string.answer1)
            )
        )

        arrQuestion?.add(
            FaqQuestionModel(
                getString(R.string.question3), getString(R.string.answer1)
            )
        )

        arrQuestion?.add(
            FaqQuestionModel(
                getString(R.string.question4), getString(R.string.answer1)
            )
        )
        recyclerAdapter = arrQuestion?.let { it1 ->
            RecyclerFaqQuestionAdapter(
                "orderHelp", it1, ::handler, status
            )
        }
        binding.faqQuestionRecycler.adapter = recyclerAdapter
        recyclerAdapter?.notifyDataSetChanged()
    }

    private fun handler(model: FaqQuestionModel) {
        generateRoomId(model)
    }

    private fun initListeners() {
        binding.apply {
            backButton.setOnClickListener {
                finish()
            }
            orderDetailLayer.setOnClickListener {
                val toChat = Intent(this@OrderDetailHelp, CaseDetailChat::class.java)
                val dataModel = ChatDataModel(
                    roomId = roomId,
                    ticketId = intent.getStringExtra(TICKET_ID),
                    supportType = "orderSupport"
                )
                toChat.putExtra(ROOM_DATA, Gson().toJson(dataModel))
                startActivity(toChat)
            }
        }
    }
}