package ir.mhdolatabadi.atid.ui.texts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ir.mhdolatabadi.atid.R
import ir.mhdolatabadi.atid.data.ReligiousTexts
import ir.mhdolatabadi.atid.databinding.FragmentTextsBinding
import ir.mhdolatabadi.atid.databinding.ItemTextCardBinding

class TextsFragment : Fragment() {

    private var _binding: FragmentTextsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextsBinding.inflate(inflater, container, false)

        ReligiousTexts.all.forEach { text ->
            val item = ItemTextCardBinding.inflate(inflater, binding.containerTexts, true)
            item.textTitle.text = text.title
            item.textSubtitle.text = text.subtitle
            item.root.setOnClickListener {
                findNavController().navigate(
                    R.id.action_texts_to_detail,
                    bundleOf("textId" to text.id)
                )
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
