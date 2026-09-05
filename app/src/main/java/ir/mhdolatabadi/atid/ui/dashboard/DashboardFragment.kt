package ir.mhdolatabadi.atid.ui.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.databinding.FragmentDashboardBinding
import ir.mhdolatabadi.atid.util.PersianDateUtils

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val weekdayLabels = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        weekdayLabels.forEach { label ->
            binding.containerWeekdayHeader.addView(createWeekdayLabel(label))
        }

        binding.buttonPrevMonth.setOnClickListener { dashboardViewModel.goToPreviousMonth() }
        binding.buttonNextMonth.setOnClickListener { dashboardViewModel.goToNextMonth() }

        dashboardViewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.textToday.text = state.todayLabel
            binding.textMonthYear.text = state.monthLabel
            renderDays(state.days, state.todayDay)
        }

        return binding.root
    }

    private fun renderDays(days: List<Int?>, todayDay: Int?) {
        binding.containerDays.removeAllViews()
        days.chunked(WEEK_LENGTH).forEach { week ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            week.forEach { day ->
                row.addView(createDayCell(day, day != null && day == todayDay))
            }
            repeat(WEEK_LENGTH - week.size) {
                row.addView(createDayCell(null, false))
            }
            binding.containerDays.addView(row)
        }
    }

    private fun createWeekdayLabel(text: String): TextView = TextView(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        gravity = Gravity.CENTER
        setTypeface(typeface, Typeface.BOLD)
        setText(text)
    }

    private fun createDayCell(day: Int?, isToday: Boolean): TextView = TextView(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        gravity = Gravity.CENTER
        setPadding(0, 24, 0, 24)
        textSize = 15f
        text = day?.let { PersianDateUtils.toPersianDigits(it) }.orEmpty()
        if (isToday) {
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.purple_500))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val WEEK_LENGTH = 7
    }
}
