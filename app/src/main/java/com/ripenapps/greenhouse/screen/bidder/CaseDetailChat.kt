package com.ripenapps.greenhouse.screen.bidder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.app.csc_mobile.utils.ProcessDialog
import com.google.android.gms.common.util.CollectionUtils
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.ChatAdapter
import com.ripenapps.greenhouse.databinding.ActivityCaseDetailChatBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.ChatDataModel
import com.ripenapps.greenhouse.datamodels.biddermodel.ChatModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.chat.response.UploadImageResponseModel
import com.ripenapps.greenhouse.model.chat.viewModel.UploadImageViewModel
import com.ripenapps.greenhouse.model.register_user.response.LoginResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.changeStatusBarColor
import com.ripenapps.greenhouse.utills.CommonUtils.isKeyboardOpen
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants.ADMIN_ID
import com.ripenapps.greenhouse.utills.Constants.ROOM_DATA
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.utills.showToast
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject

class CaseDetailChat : BaseActivity() {
    private lateinit var binding: ActivityCaseDetailChatBinding
    private var loginData: LoginResponseModel.LoginData? = null
    private var mChatAdapter: ChatAdapter? = null
    private var socket: Socket? = null
    private var roomId: Long? = null
    private var imagePath: MutableList<String> = mutableListOf()
    private var uploadImageViewModel: UploadImageViewModel? = null
    private var listOfUri = mutableListOf<String>()
    private var accountBlocked: AccountBlocked? = null
    private var ticketId: String? = null
    private var roomModel: ChatDataModel? = null
    private var keyBoardOpen = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        changeStatusBarColor(this@CaseDetailChat, R.color.status_bar)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CaseDetailChat, R.layout.activity_case_detail_chat)
        loginData = Gson().fromJson(
            Preferences.getStringPreference(this@CaseDetailChat, "user_details"),
            LoginResponseModel.LoginData::class.java
        )

        if (intent.getStringExtra("fromHistory") == "1") {
            binding.sendingMessageLayer.setVisibility(intent.getBooleanExtra("status", false))
            binding.messageAdmin.setVisibility(!intent.getBooleanExtra("status", false))
        }

        if (intent.hasExtra(ROOM_DATA)) {
            roomModel = Gson().fromJson(intent.getStringExtra(ROOM_DATA), ChatDataModel::class.java)
            Log.d("roomModel", "onCreate: ${Gson().toJson(roomModel)}")
        } else {
            showToast(getString(R.string.something_went_wrong))
            finish()
        }
        ticketId = roomModel?.ticketId
        roomId = roomModel?.roomId
        binding.ticketId.text = getString(R.string.ticket) + roomModel?.ticketId

        initListeners()
        initViewModel()
        initSocket()
        getChatList()
        initReceiveMessage()

        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!keyBoardOpen && isKeyboardOpen(rootView)) {
                scrollDown()
                keyBoardOpen = true
            } else {
                keyBoardOpen = isKeyboardOpen(rootView)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initSocket() {
        val socketUrl = "http://13.235.137.221:6002?userId=${loginData?.userDetails?.id}"
        socket = IO.socket(socketUrl)
        Log.d("TAG", "initSocket: $socketUrl")
        socket?.connect()
        Log.d("TAG", "initSocket: ${socket?.isActive}")
    }

    private fun initViewModel() {
        uploadImageViewModel = ViewModelProvider(this)[UploadImageViewModel::class.java]
        binding.lifecycleOwner = this
        Log.e("TAG", "initViewModel: " + Gson().toJson(uploadImageViewModel?.request))
        uploadImageViewModel?.uploadImageData()?.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = Gson().fromJson(
                        AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                        UploadImageResponseModel::class.java
                    )
                    when (data.status) {
                        200 -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "uploadImageResponse" + Gson().toJson(data))
                            data.data?.forEach { imageUrl ->
                                sendMessage(2, "", listOf(imageUrl))
                            }
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
                            Preferences.removePreference(this@CaseDetailChat, "token")
                            Preferences.removePreference(this@CaseDetailChat, "user_details")
                            Preferences.removePreference(this@CaseDetailChat, "isVerified")
                            val signin = Intent(this@CaseDetailChat, SignIn::class.java)
                            startActivity(signin)
                            finishAffinity()
                        }

                        else -> {
                            ProcessDialog.dismissDialog()
                            Log.d("TAG", "onResponseCallBack" + Gson().toJson(data))
                            Toast.makeText(
                                this@CaseDetailChat, "${data.message}", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.startDialog(this@CaseDetailChat)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog()
                    Toast.makeText(this@CaseDetailChat, it.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "onErrorCallBack: ${it.message}")
                }
            }
        }
    }

    private fun getChatList() {
        if (socket == null) {
            initSocket()
        }
        val dataChat = JSONObject()
        dataChat.put("userId", loginData?.userDetails?.id)
        dataChat.put("roomId", roomId)
        Log.d("TAG", "getChatListTicketId: $ticketId")
        dataChat.put("ticketId", ticketId)
        dataChat.put("senderType", "user")
        Log.d("TAG", "requestChatList: ${Gson().toJson(dataChat)}")
        socket?.emit("chatMessage", dataChat)
        socket?.on("receiveMessages") { args: Array<Any?> ->
            runOnUiThread {
                val chatMessages = Gson().fromJson(
                    JSONArray().put(args[0] as JSONObject?).get(0).toString(), ChatModel::class.java
                )
                Log.d(
                    "getChatList", "getChatList: ${
                        Gson().toJson(
                            JSONArray().put(args[0] as JSONObject?).get(0).toString()
                        )
                    }"
                )
                if (mChatAdapter == null) {
                    if (!chatMessages.result.isNullOrEmpty()) {
                        initAdapter()
                        mChatAdapter?.updateChatList(chatMessages.result as MutableList<ChatModel.Result>)
                        scrollDown()
                    } else {
                        sendMessage(1, roomModel?.question ?: "Rishabh")
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        mChatAdapter = ChatAdapter()
        binding.chatRecycler.adapter = mChatAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun sendMessage(messageType: Int, message: String, images: List<String?> = listOf()) {
        Log.d("TAG", "sendMessage:]122122121")
        val dataSend = JSONObject()
        dataSend.put("roomId", roomId)
        dataSend.put("senderType", "user")
        dataSend.put("supportType", roomModel?.supportType)
        dataSend.put("userId", loginData?.userDetails?.id)
        dataSend.put("receiverId", ADMIN_ID)
        dataSend.put("ticketId", ticketId)
        dataSend.put("messageType", messageType)
        if (messageType == 2) {
            dataSend.put("images", JSONArray(images))
        }
        dataSend.put("message", message)
        socket?.emit("sendMessage", dataSend)
        Log.d("TAG", "sendMessage: ${Gson().toJson(dataSend)}")
        binding.messageMe.setText("")
    }

    private fun initReceiveMessage() {
        socket?.on("receiveMessage") { args: Array<Any?> ->
            Log.d(
                "TAG", "initReceiveMessage: ${
                    Gson().toJson(
                        (JSONArray().put(args[0] as JSONObject?).get(0))
                    )
                }"
            )
            runOnUiThread {
                val chatModel = Gson().fromJson(
                    (JSONArray().put(args[0] as JSONObject?).get(0)).toString(),
                    ChatModel::class.java
                )

                Log.d(
                    "TAG", "initReceiveMessage: ${
                        Gson().toJson(
                            (JSONArray().put(args[0] as JSONObject?).get(0))
                        )
                    }"
                )

                if (chatModel.status == 400) {
                    binding.sendingMessageLayer.setVisibility(false)
                    binding.messageAdmin.setVisibility(true)

                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("status", "update")
                    })
                    return@runOnUiThread
                }else {
                    binding.sendingMessageLayer.setVisibility(true)
                    binding.messageAdmin.setVisibility(false)

                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("status", "update")
                    })

                    if (mChatAdapter == null) {
                        initAdapter()
                        val messageList = (chatModel.result as MutableList<ChatModel.Result>).also {
                            it.getOrNull(0)?.createdAt = chatModel.createdAtTime
                        }
                        mChatAdapter?.updateChatList(messageList)
                        scrollDown()

                    } else {
                        val result: ChatModel.Result = ChatModel.Result(
                            message = chatModel.result?.getOrNull(0)?.message,
                            senderType = chatModel.result?.getOrNull(0)?.senderType,
                            images = chatModel.result?.getOrNull(0)?.images,
                            createdAt = chatModel.createdAtTime
                        )
                        mChatAdapter?.apply {
                            messages.add(result)
                            this.notifyItemInserted(this.messages.size - 1)
                        }
                        scrollDown()
                    }
                }
            }
        }
    }

    private fun scrollDown() {
        binding.chatRecycler.scrollToPosition((mChatAdapter?.itemCount ?: 0) - 1)
    }

    private fun initListeners() {
        binding.apply {
            sendMessage.setOnClickListener {
                if (binding.messageMe.text.toString().isEmpty()) return@setOnClickListener
                sendMessage(1, binding.messageMe.text.toString().trim())
            }
            backButton.setOnClickListener {
                finish()
            }
            storageIcon.setOnClickListener {
                listOfUri.clear()
                imagePath.clear()
                selectImage()
            }
        }
    }

    private fun selectImage() {
        val permission: Array<String?> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (CommonUtils.requestPermissions(this, 100, permission)) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForImageGallery.launch("image/*")
        } else {
            CommonUtils.showSettingDialog(this@CaseDetailChat)
        }
    }

    private val startForImageGallery =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!CollectionUtils.isEmpty(uris)) {
                Log.d("url", "imageUris : $uris")
                for (imageUri in uris.take(10)) {
                    val filePath = CommonUtils.getRealPathFromURI(this@CaseDetailChat, imageUri)
                    if (listOfUri.size < 10) {
                        imagePath.add(filePath)
                        listOfUri.add(filePath)
                    }
                }

                if (listOfUri.size >= 10) {
                    binding.storageIcon.isEnabled = false
                } else {
                    uploadImageViewModel?.request?.token = Preferences.getStringPreference(
                        this@CaseDetailChat, TOKEN
                    )
                    uploadImageViewModel?.uploadImageRequest(imagePath)
                    binding.storageIcon.isEnabled = true
                }
            }
        }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.hasExtra(ROOM_DATA) == true) {
            roomModel = Gson().fromJson(intent.getStringExtra(ROOM_DATA), ChatDataModel::class.java)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        offlineEvent()
    }

    private fun offlineEvent() {
        val data = JSONObject()
        socket?.emit("offline", data)
    }
}
