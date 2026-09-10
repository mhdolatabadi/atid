package ir.mhdolatabadi.atid.ui.notifications

import android.Manifest
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.databinding.FragmentNotificationsBinding
import ir.mhdolatabadi.atid.databinding.ItemPrayerTimeRowBinding
import ir.mhdolatabadi.atid.util.PersianDateUtils
import ir.mhdolatabadi.atid.util.PrayerTimes

private data class PrayerRow(
    val binding: ItemPrayerTimeRowBinding,
    val defaultLabelColor: Int,
    val defaultTimeColor: Int
)

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var rows: Map<String, PrayerRow>

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val rowBindings = linkedMapOf(
            "fajr" to binding.rowFajr,
            "sunrise" to binding.rowSunrise,
            "dhuhr" to binding.rowDhuhr,
            "asr" to binding.rowAsr,
            "sunset" to binding.rowSunset,
            "maghrib" to binding.rowMaghrib,
            "isha" to binding.rowIsha
        )
        val labels = mapOf(
            "fajr" to getString(R.string.label_fajr),
            "sunrise" to getString(R.string.label_sunrise),
            "dhuhr" to getString(R.string.label_dhuhr),
            "asr" to getString(R.string.label_asr),
            "sunset" to getString(R.string.label_sunset),
            "maghrib" to getString(R.string.label_maghrib),
            "isha" to getString(R.string.label_isha)
        )
        rows = rowBindings.mapValues { (key, rowBinding) ->
            rowBinding.textLabel.text = labels.getValue(key)
            PrayerRow(
                binding = rowBinding,
                defaultLabelColor = rowBinding.textLabel.currentTextColor,
                defaultTimeColor = rowBinding.textTime.currentTextColor
            )
        }

        binding.buttonUseLocation.setOnClickListener {
            requestLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            bindTimes(state.times)
            binding.textLocation.text = state.locationLabel
            highlightNextPrayer(state.nextPrayerKey)
        }

        return binding.root
    }

    private fun bindTimes(times: PrayerTimes) {
        rows.getValue("fajr").binding.textTime.text = toPersianTime(times.fajr)
        rows.getValue("sunrise").binding.textTime.text = toPersianTime(times.sunrise)
        rows.getValue("dhuhr").binding.textTime.text = toPersianTime(times.dhuhr)
        rows.getValue("asr").binding.textTime.text = toPersianTime(times.asr)
        rows.getValue("sunset").binding.textTime.text = toPersianTime(times.sunset)
        rows.getValue("maghrib").binding.textTime.text = toPersianTime(times.maghrib)
        rows.getValue("isha").binding.textTime.text = toPersianTime(times.isha)
    }

    private fun toPersianTime(hhmm: String): String =
        hhmm.split(":").joinToString(":") { PersianDateUtils.toPersianDigits(it.toInt()) }

    private fun highlightNextPrayer(nextKey: String) {
        val highlightColor = ContextCompat.getColor(requireContext(), R.color.brand_primary)
        rows.forEach { (key, row) ->
            val isNext = key == nextKey
            val typeface = if (isNext) Typeface.BOLD else Typeface.NORMAL
            row.binding.textLabel.setTypeface(null, typeface)
            row.binding.textTime.setTypeface(null, typeface)
            row.binding.textLabel.setTextColor(if (isNext) highlightColor else row.defaultLabelColor)
            row.binding.textTime.setTextColor(if (isNext) highlightColor else row.defaultTimeColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
