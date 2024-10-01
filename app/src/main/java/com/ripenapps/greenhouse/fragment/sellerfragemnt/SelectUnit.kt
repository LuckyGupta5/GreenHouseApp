package com.ripenapps.greenhouse.fragment.sellerfragemnt

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ripenapps.greenhouse.R
import com.ripenapps.greenhouse.adapter.seller.SellerRecyclerSelectCategoryAdapter
import com.ripenapps.greenhouse.databinding.FragmentSelectUnitBinding
import com.ripenapps.greenhouse.datamodels.sellermodel.SelectCategoryModel
import com.ripenapps.greenhouse.enums.UnitEnum
import com.ripenapps.greenhouse.utills.Preferences
import java.util.Locale

class SelectUnit(
    private val dismissCallBack: (String) -> (Unit),
    private val selectedUnit: String? = null
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSelectUnitBinding
    private val arrSelectUnit: MutableList<SelectCategoryModel> = mutableListOf()
    private var recyclerUnit: SellerRecyclerSelectCategoryAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_unit, container, false)
        if (Preferences.getStringPreference(requireContext(), "language") == "ar") {
            setLocale("ar")
        } else
            setLocale("en")
        arrSelectUnit.add(
            SelectCategoryModel(
                getString(R.string.kilogram),
                isSelect = selectedUnit?.lowercase() == UnitEnum.KILO_GRAM.value
            )
        )
        arrSelectUnit.add(
            SelectCategoryModel(
                getString(R.string.gram),
                isSelect = selectedUnit?.lowercase() == UnitEnum.GRAM.value
            )
        )
        arrSelectUnit.add(
            SelectCategoryModel(
                getString(R.string.tonne),
                isSelect = selectedUnit?.lowercase() == UnitEnum.TONNE.value
            )
        )
        recyclerUnit = SellerRecyclerSelectCategoryAdapter(arrSelectUnit, ::itemClickAction)
        binding.selectUnitRecycler.adapter = recyclerUnit
        return binding.root
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        // Example log to see locale change
        Log.d("Locale", "Locale changed to $languageCode")
    }

    private fun itemClickAction(position: Int) {
        dismissCallBack.invoke(arrSelectUnit.getOrNull(position)?.text ?: "")
        dismiss()/*     Toast.makeText(mcontext, "Hello $position", Toast.LENGTH_SHORT).show()*/
    }
}