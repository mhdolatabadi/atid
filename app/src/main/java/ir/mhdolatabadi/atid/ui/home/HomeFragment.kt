package ir.mhdolatabadi.atid.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.data.QadaPrayer
import ir.mhdolatabadi.atid.databinding.FragmentHomeBinding
import ir.mhdolatabadi.atid.databinding.ItemQadaCounterBinding
import ir.mhdolatabadi.atid.util.PersianDateUtils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private val prayerRows = mutableMapOf<QadaPrayer, ItemQadaCounterBinding>()
    private lateinit var fastRow: ItemQadaCounterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val prayerLabelRes = mapOf(
            QadaPrayer.FAJR to R.string.qada_fajr,
            QadaPrayer.ZOHR to R.string.qada_zohr,
            QadaPrayer.ASR to R.string.qada_asr,
            QadaPrayer.MAGHRIB to R.string.qada_maghrib,
            QadaPrayer.ISHA to R.string.qada_isha
        )

        QadaPrayer.values().forEach { prayer ->
            val row = ItemQadaCounterBinding.inflate(inflater, binding.containerPrayers, true)
            row.textLabel.text = getString(prayerLabelRes.getValue(prayer))
            row.buttonIncrement.setOnClickListener { homeViewModel.incrementPrayer(prayer) }
            row.buttonDecrement.setOnClickListener { homeViewModel.decrementPrayer(prayer) }
            prayerRows[prayer] = row
        }

        fastRow = ItemQadaCounterBinding.inflate(inflater, binding.containerFast, true)
        fastRow.textLabel.text = getString(R.string.qada_fast_row)
        fastRow.buttonIncrement.setOnClickListener { homeViewModel.incrementFast() }
        fastRow.buttonDecrement.setOnClickListener { homeViewModel.decrementFast() }

        homeViewModel.prayerCounts.observe(viewLifecycleOwner) { counts ->
            counts.forEach { (prayer, count) ->
                prayerRows[prayer]?.textCount?.text = PersianDateUtils.toPersianDigits(count)
            }
        }
        homeViewModel.fastCount.observe(viewLifecycleOwner) { count ->
            fastRow.textCount.text = PersianDateUtils.toPersianDigits(count)
        }
        homeViewModel.totalOutstanding.observe(viewLifecycleOwner) { total ->
            binding.textTotal.text = PersianDateUtils.toPersianDigits(total)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prayerRows.clear()
        _binding = null
    }
}
