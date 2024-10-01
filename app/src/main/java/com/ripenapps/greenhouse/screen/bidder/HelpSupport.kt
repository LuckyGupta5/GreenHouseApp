package com.ripenapps.greenhouse.screen.bidder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.databinding.ActivityHelpSupportBinding
import com.ripenapps.greenhouse.datamodels.FilterModel
import com.ripenapps.greenhouse.datamodels.biddermodel.ChatDataModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.fragment.bidderfragment.HelpTopicSheet
import com.ripenapps.greenhouse.model.bidder.response.GetRoomIdResponse
import com.ripenapps.greenhouse.model.change_password.ChangePasswordResponseModel
import com.ripenapps.greenhouse.model.help.SupportChatViewModel
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.generateRandomNumber
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.ROOM_DATA
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject


class HelpSupport : BaseActivity() {
    private lateinit var helpSupportBinding: ActivityHelpSupportBinding
    private var selectTopicSheet: HelpTopicSheet? = null
    private var filterModel: FilterModel? = null
    private var ticketId: Int? = null
    private var supportChatViewModel: SupportChatViewModel? = null
    var accountBlocked: AccountBlocked? = null
    private var loginData: LoginResponseModel.LoginData? = null
    var roomId: Long = 0
    private var socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.changeStatusBarColor(this@HelpSupport, R.color.status_bar)
        super.onCreate(savedInstanceState)
        helpSupportBinding = DataBindingUtil.setContentView(this, R.layout.activity_help_support)

        if (Preferences.getStringPreference(this@HelpSupport, "language").equals("ar")) {
            helpSupportBinding.arrow.rotation = 180f
        }

        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@HelpSupport, "user_details"),
            LoginResponseModel.LoginData::class.java
        )

        helpSupportBinding.apply {
            remarkEditTextField.doAfterTextChanged {
                // Check if the remarkEditTextField is not empty and helpTopic is not null
                if (remarkEditTextField.text?.isNotEmpty() == true && filterModel?.helpTopic != null) {
                    btnSubmitHelp.isEnabled = true
                    btnSubmitHelp.setBackgroundColor(
                        ContextCompat.getColor(
                            this@HelpSupport, R.color.greenhousetheme
                        )
                    )
                } else {
                    btnSubmitHelp.isEnabled = false
                    btnSubmitHelp.setBackgroundColor(
                        ContextCompat.getColor(
                            this@HelpSupport, R.color.inactive_background
                        )
                    )
                }
            }
        }
        initViewModel()
        initListener()
    }

    private fun generateRoomId(ticket: Int?) {
        if (socket == null) {
            val socketUrl = "http://13.235.137.221:6002?userId=${loginData?.userDetails?.id}"
            socket = IO.socket(socketUrl)
            Log.d("TAG", "initSocket: $socketUrl")
            socket?.connect()
        }
        val dataRoom = JSONObject()
        dataRoom.put("receiverId", Constants.ADMIN_ID)
        dataRoom.put("userId", loginData?.userDetails?.id)
        dataRoom.put("senderType", "user")
        dataRoom.put("receiverType", "admin")
        dataRoom.put("ticketId", ticket)
        Log.d("TAG", "generateRoomId: ${Gson().toJson(dataRoom)}")
        socket?.emit("initiateChat", dataRoom)
        // get roomId
        socket?.on("getRoomId") { args: Array<Any?> ->
            val roomResponse = Gson().fromJson(
                JSONArray().put(args[0] as JSONObject?).get(0).toString(),
                GetRoomIdResponse::class.java
            )
            if (roomId == 0L) {
                roomId = roomResponse.result?.roomId ?: 0
                runOnUiThread {
                    helpApi()
                }
            }
            Log.d("TAG", "generateRoomId: $roomId")
        }
    }

    private fun helpApi() {
        supportChatViewModel?.request?.token =
            Preferences.getStringPreference(this@HelpSupport, TOKEN)
        supportChatViewModel?.request?.supportType = "helpSupport"
        supportChatViewModel?.request?.ticketId = ticketId.toString()
        supportChatViewModel?.request?.title = helpSupportBinding.selectTopic.text.toString().trim()
        supportChatViewModel?.request?.userId = loginData?.userDetails?.id
        supportChatViewModel?.request?.roomId = roomId
        supportChatViewModel?.request?.message =
            helpSupportBinding.selectTopic.text.toString().trim()
        supportChatViewModel?.getSupportChatRequest()
    }

    private fun initViewModel() {
        supportChatViewModel = ViewModelProvider(this@HelpSupport)[SupportChatViewModel::class.java]
        helpSupportBinding.lifecycleOwner = this@HelpSupport
        supportChatViewModel?.getSupportChatData()?.observe(this@HelpSupport) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        ProcessDialog.dismissDialog()
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            ChangePasswordResponseModel::class.java
                        )
                        Log.d("TAG", "ticketHistoryResponse: ${Gson().toJson(data.data)}")
                        when (data.status) {
                            200 -> {
                                Toast.makeText(
                                    this@HelpSupport, data.message, Toast.LENGTH_SHORT
                                ).show()

                                val toChat = Intent(this@HelpSupport, CaseDetailChat::class.java)

                                val dataModel = ChatDataModel(
                                    question = helpSupportBinding.selectTopic.text.toString(),
                                    roomId = roomId,
                                    ticketId = ticketId.toString(),
                                    supportType = "helpSupport"
                                )
                                toChat.apply {
                                    putExtra(ROOM_DATA, Gson().toJson(dataModel))
                                }
                                startActivity(toChat)
                                filterModel?.helpTopic = null
                                roomId = 0
                                helpSupportBinding.selectTopic.text =
                                    getString(R.string.select_topic)
                                helpSupportBinding.btnSubmitHelp.isEnabled = false
                                helpSupportBinding.btnSubmitHelp.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@HelpSupport, R.color.inactive_background
                                    )
                                )
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
                                Preferences.removePreference(this@HelpSupport, "token")
                                Preferences.removePreference(this@HelpSupport, "user_details")
                                Preferences.removePreference(this@HelpSupport, "isVerified")
                                Preferences.removePreference(this@HelpSupport, "roomId")
                                val signin = Intent(this@HelpSupport, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        ProcessDialog.startDialog(this)
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        ProcessDialog.dismissDialog()
                        Toast.makeText(this@HelpSupport, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                ProcessDialog.dismissDialog()
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun initListener() {

        helpSupportBinding.selectTopic.setOnClickListener {
            if (selectTopicSheet?.isAdded != true) {
                selectTopicSheet = HelpTopicSheet(filterModel, ::handelFilter)
                selectTopicSheet?.show(supportFragmentManager, selectTopicSheet?.tag)
            }
        }

        helpSupportBinding.closeHelpSupport.setOnClickListener {
            finish()
        }

        helpSupportBinding.btnSubmitHelp.setOnClickListener {
            helpSupportBinding.remarkEditTextField.text?.clear()
            ticketId = generateRandomNumber()
            Log.d("TAG", "TICKET_ID: $ticketId")
            generateRoomId(ticketId)
        }

        helpSupportBinding.mailBox.setOnClickListener {
            val to = "green.app.house@gmail.com" // Replace with the actual recipient email address
            val subject = "Green House Help & Support" // Replace with the desired subject
            val message = "Type your message here" // Replace with the actual message content

            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            email.putExtra(Intent.EXTRA_SUBJECT, subject)
            email.putExtra(Intent.EXTRA_TEXT, message)
            email.type = "message/rfc822"
            startActivity(Intent.createChooser(email, "Choose an Email client:"))
        }


        helpSupportBinding.callBox.setOnClickListener {
            val phoneNumber = "540555075"
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(dialIntent)
        }

        helpSupportBinding.btnViewTicketHistory.setOnClickListener {
            val toTicketHistory = Intent(this, TicketHistory::class.java)
            startActivity(toTicketHistory)
        }
    }

    private fun handelFilter(model: FilterModel? = null) {
        Log.d("TAG", "initListeners: ${Gson().toJson(filterModel)}")
        this@HelpSupport.filterModel = model
        setTopic()
    }

    private fun setTopic() {
        helpSupportBinding.selectTopic.text = filterModel?.helpTopic
    }
}