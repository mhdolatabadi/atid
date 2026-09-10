package ir.mhdolatabadi.atid.ui.texts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.mhdolatabadi.atid.data.ReligiousTexts
import ir.mhdolatabadi.atid.databinding.FragmentTextDetailBinding

class TextDetailFragment : Fragment() {

    private var _binding: FragmentTextDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextDetailBinding.inflate(inflater, container, false)

        val textId = requireArguments().getString("textId")
        val text = ReligiousTexts.byId(requireNotNull(textId))

        binding.textTitle.text = text.title
        binding.textSubtitle.text = text.subtitle
        binding.textNote.text = text.note
        binding.textBody.text = text.body

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
