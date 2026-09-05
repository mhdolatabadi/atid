package ir.mhdolatabadi.atid.ui.notifications

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mhdolatabadi.atid.databinding.FragmentNotificationsBinding
import ir.mhdolatabadi.atid.util.PrayerTimes

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationsViewModel

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
        }

        return binding.root
    }

    private fun bindTimes(times: PrayerTimes) {
        binding.textFajr.text = times.fajr
        binding.textSunrise.text = times.sunrise
        binding.textDhuhr.text = times.dhuhr
        binding.textAsr.text = times.asr
        binding.textSunset.text = times.sunset
        binding.textMaghrib.text = times.maghrib
        binding.textIsha.text = times.isha
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
