package ir.mhdolatabadi.atid.ui.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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

        weekdayLabels.forEachIndexed { index, label ->
            binding.containerWeekdayHeader.addView(createWeekdayLabel(label, isFriday = index == FRIDAY_INDEX))
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
            week.forEachIndexed { index, day ->
                row.addView(createDayCell(day, day != null && day == todayDay, isFriday = index == FRIDAY_INDEX))
            }
            repeat(WEEK_LENGTH - week.size) {
                row.addView(createDayCell(null, false, isFriday = false))
            }
            binding.containerDays.addView(row)
        }
    }

    private fun createWeekdayLabel(text: String, isFriday: Boolean): TextView = TextView(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        gravity = Gravity.CENTER
        setTypeface(typeface, Typeface.BOLD)
        textSize = 13f
        setText(text)
        if (isFriday) {
            setTextColor(ContextCompat.getColor(context, R.color.friday_text))
        }
    }

    private fun createDayCell(day: Int?, isToday: Boolean, isFriday: Boolean): View {
        val frame = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(44), 1f)
        }
        val label = TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(dpToPx(34), dpToPx(34), Gravity.CENTER)
            gravity = Gravity.CENTER
            textSize = 14f
            text = day?.let { PersianDateUtils.toPersianDigits(it) }.orEmpty()
            when {
                isToday -> {
                    setBackgroundResource(R.drawable.bg_today_circle)
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                    setTypeface(typeface, Typeface.BOLD)
                }
                isFriday && day != null -> {
                    setTextColor(ContextCompat.getColor(context, R.color.friday_text))
                }
            }
        }
        frame.addView(label)
        return frame
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val WEEK_LENGTH = 7
        private const val FRIDAY_INDEX = 6
    }
}
