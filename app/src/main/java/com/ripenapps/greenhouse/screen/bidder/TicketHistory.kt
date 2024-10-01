package com.ripenapps.greenhouse.screen.bidder

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.abstracts.BaseActivity
import com.ripenapps.greenhouse.adapter.bidder.RecyclerTicketAdapter
import com.ripenapps.greenhouse.databinding.ActivityTicketHistoryBinding
import com.ripenapps.greenhouse.datamodels.biddermodel.ChatDataModel
import com.ripenapps.greenhouse.datamodels.biddermodel.TicketModel
import com.ripenapps.greenhouse.fragment.AccountBlocked
import com.ripenapps.greenhouse.model.bidder.response.GetHelpSupportResponseModel
import com.ripenapps.greenhouse.screen.SignIn
import com.ripenapps.greenhouse.utills.AESHelper
import com.ripenapps.greenhouse.utills.CommonUtils
import com.ripenapps.greenhouse.utills.CommonUtils.formatDate
import com.ripenapps.greenhouse.utills.Companion
import com.ripenapps.greenhouse.utills.Constants
import com.ripenapps.greenhouse.utills.Constants.TOKEN
import com.ripenapps.greenhouse.utills.Preferences
import com.ripenapps.greenhouse.utills.Status
import com.ripenapps.greenhouse.utills.setVisibility
import com.ripenapps.greenhouse.view_models.GetOrderSupportViewModel

class TicketHistory : BaseActivity() {
    private lateinit var binding: ActivityTicketHistoryBinding

    private var getOrderSupportViewModel: GetOrderSupportViewModel? = null
    private var accountBlocked: AccountBlocked? = null
    private var recyclerAdapter: RecyclerTicketAdapter? = null
    private var isPaging: Boolean = false
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.changeStatusBarColor(this@TicketHistory, R.color.status_bar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ticket_history)

        if (Preferences.getStringPreference(this@TicketHistory, "language").equals("ar")) {
            binding.backButton.rotation = 180f
        }

        initListeners()
        initStartActivityInstance()
        initViewModel()
        initPagination()
        ticketHistoryApi()
    }

    private fun initStartActivityInstance() {
        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val status = result.data?.extras?.getString("status")
                    if (status == "update") {
                        getOrderSupportViewModel?.request?.page = "1"
                        getOrderSupportViewModel?.page = 1
                        isPaging = false
                        recyclerAdapter?.clearData()
                        ticketHistoryApi()
                    }
                }
            }
    }

    private fun ticketHistoryApi() {
        getOrderSupportViewModel?.request?.token =
            Preferences.getStringPreference(this@TicketHistory, TOKEN)
        getOrderSupportViewModel?.request?.supportType = "helpSupport"
        getOrderSupportViewModel?.getOrderSupportRequest()
    }

    private fun initViewModel() {
        getOrderSupportViewModel =
            ViewModelProvider(this@TicketHistory)[GetOrderSupportViewModel::class.java]
        binding.lifecycleOwner = this@TicketHistory
        getOrderSupportViewModel?.getOrderSupportData()?.observe(this@TicketHistory) {
            try {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.ticketShimmer.historyMainShimmer.setVisibility(false)
                        val data = Gson().fromJson(
                            AESHelper.decrypt(Companion.SECRET_KEY, it.data),
                            GetHelpSupportResponseModel::class.java
                        )
                        binding.idProgress.setVisibility(false)
                        when (data.status) {
                            200 -> {
                                getOrderSupportViewModel?.isLastPage =
                                    data.data?.dataList?.isEmpty() ?: true
                                if (getOrderSupportViewModel?.isLastPage == true) {
                                    if (isPaging) return@observe
                                }
                                getOrderSupportViewModel?.isDataLoading = false
                                binding.idRefreshLayout.isRefreshing = false
                                if (data.data?.dataList?.isEmpty() == true) {
                                    if (isPaging) return@observe
                                    binding.apply {
                                        placeHolder.setVisibility(true)
                                        headingPlaceHolder.setVisibility(true)
                                        ticketRecycler.setVisibility(false)
                                    }
                                    return@observe
                                } else {
                                    binding.apply {
                                        placeHolder.setVisibility(false)
                                        headingPlaceHolder.setVisibility(false)
                                        ticketRecycler.setVisibility(true)
                                    }
                                }
                                val arrTickets: ArrayList<TicketModel> = arrayListOf()
                                Log.d("TAG", "helpSupportResponse: ${Gson().toJson(data.data)}")
                                binding.apply {
                                    data.data?.dataList?.forEachIndexed { _, ticket ->
                                        arrTickets.add(
                                            TicketModel(
                                                id = ticket.ticketId,
                                                ticketId = getString(R.string.ticket_id) + ticket.ticketId,
                                                ticketQuestion = ticket.title,
                                                roomId = ticket.roomId,
                                                ticketDay = formatDate(ticket.createdAt.toString()),
                                                ticketStatus = ticket.status
                                            )
                                        )
                                    }
                                    if (recyclerAdapter == null || recyclerAdapter?.arrTickets?.isEmpty() == true || getOrderSupportViewModel?.page == 1) {
                                        recyclerAdapter = RecyclerTicketAdapter(::handler)
                                        binding.ticketRecycler.adapter = recyclerAdapter
                                        recyclerAdapter?.addItems(arrTickets)
                                        binding.ticketRecycler.setVisibility(true)
                                    } else {
                                        recyclerAdapter?.updateItemList(arrTickets)
                                    }
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
                                val signin = Intent(this@TicketHistory, SignIn::class.java)
                                startActivity(signin)
                                finishAffinity()
                            }

                            else -> {
                                binding.ticketShimmer.historyMainShimmer.setVisibility(false)
                                Toast.makeText(
                                    this@TicketHistory, data.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Status.LOADING -> {
                        if (!isPaging && !binding.idRefreshLayout.isRefreshing) {
                            binding.ticketShimmer.historyMainShimmer.setVisibility(true)
                            binding.ticketRecycler.setVisibility(false)
                            binding.placeHolder.setVisibility(false)
                            binding.headingPlaceHolder.setVisibility(false)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("TAG", "initViewModel: ${it.message}")
                        binding.ticketShimmer.historyMainShimmer.setVisibility(true)
                        binding.placeHolder.setVisibility(false)
                        binding.headingPlaceHolder.setVisibility(false)
                        Toast.makeText(this@TicketHistory, it.message, Toast.LENGTH_SHORT).show()
                        binding.idRefreshLayout.isRefreshing = false
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
            }
        }
    }

    private fun handler(ticketModel: TicketModel, position: Int) {
        val toCaseDetailChat = Intent(this@TicketHistory, CaseDetailChat::class.java)
        val dataModel = ChatDataModel(
            roomId = recyclerAdapter?.arrTickets?.getOrNull(position)?.roomId,
            ticketId = recyclerAdapter?.arrTickets?.getOrNull(position)?.id,
            supportType = "orderSupport"
        )
        toCaseDetailChat.putExtra(Constants.ROOM_DATA, Gson().toJson(dataModel))
        toCaseDetailChat.putExtra("fromHistory", "1")
        toCaseDetailChat.putExtra(
            "status", recyclerAdapter?.arrTickets?.getOrNull(position)?.ticketStatus
        )
        getContent.launch(toCaseDetailChat)
    }

    private fun allFilter() {
        binding.apply {
            AllLayout.background = (ContextCompat.getDrawable(
                this@TicketHistory, R.drawable.green_house_color_border
            ))
            allTicket.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.white))

            openLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.border_d4d4d4)

            openTicket.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.black))

            solvedLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.border_d4d4d4)

            solvedTickets.setTextColor(
                ContextCompat.getColor(
                    this@TicketHistory, R.color.black
                )
            )
        }
        getOrderSupportViewModel?.request?.statusType = null
        getOrderSupportViewModel?.request?.page = "1"
        getOrderSupportViewModel?.page = 1
        isPaging = false
        recyclerAdapter?.clearData()
        ticketHistoryApi()
    }

    private fun openFilter() {
        binding.apply {
            openLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.green_house_color_border)
            openTicket.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.white))

            AllLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.border_d4d4d4)
            allTicket.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.black))

            solvedLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.border_d4d4d4)
            solvedTickets.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.black))
        }
        getOrderSupportViewModel?.request?.statusType = "true"
        getOrderSupportViewModel?.request?.page = "1"
        getOrderSupportViewModel?.page = 1
        isPaging = false
        recyclerAdapter?.clearData()
        ticketHistoryApi()
    }

    private fun solvedFilter() {
        binding.apply {
            solvedLayout.background =
                (ContextCompat.getDrawable(this@TicketHistory, R.drawable.green_house_color_border))
            solvedTickets.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.white))
            openLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.border_d4d4d4)
            openTicket.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.black))
            AllLayout.background =
                ContextCompat.getDrawable(this@TicketHistory, R.drawable.border_d4d4d4)
            allTicket.setTextColor(ContextCompat.getColor(this@TicketHistory, R.color.black))
        }
        getOrderSupportViewModel?.request?.statusType = "false"
        getOrderSupportViewModel?.request?.page = "1"
        getOrderSupportViewModel?.page = 1
        isPaging = false
        recyclerAdapter?.clearData()
        ticketHistoryApi()
    }

    private fun initListeners() {
        binding.apply {

            idRefreshLayout.setOnRefreshListener {
                getOrderSupportViewModel?.page = 1
                getOrderSupportViewModel?.request?.page = "1"
                getOrderSupportViewModel?.request?.page = getOrderSupportViewModel?.page.toString()
                recyclerAdapter?.clearData()
                ticketHistoryApi()
            }

            AllLayout.setOnClickListener {
                allFilter()
            }
            openLayout.setOnClickListener {
                openFilter()
            }

            solvedLayout.setOnClickListener {
                solvedFilter()
            }

            backButton.setOnClickListener {
                finish()
            }
        }
    }


    private fun initPagination() {
        binding.ticketRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if the user has scrolled to the end of the list
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!getOrderSupportViewModel?.isLastPage!! && !getOrderSupportViewModel?.isDataLoading!! && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // Load more data when reaching the end of the list
                    getOrderSupportViewModel?.page = getOrderSupportViewModel?.page!! + 1
                    getOrderSupportViewModel?.isDataLoading = true
                    binding.idProgress.setVisibility(true)
                    isPaging = true // pagination working
                    getOrderSupportViewModel?.request?.apply {
                        this.page = getOrderSupportViewModel?.page?.toString()
                    }
                    ticketHistoryApi()
                }
            }
        })
    }
}