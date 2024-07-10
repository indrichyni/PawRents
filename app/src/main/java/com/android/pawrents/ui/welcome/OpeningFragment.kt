package com.android.pawrents.ui.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.pawrents.R
import com.android.pawrents.databinding.FragmentOpeningBinding

class OpeningFragment(private val callback : () -> Unit, private val toSignUpCallback: () -> Unit) : Fragment() {

    private var _binding: FragmentOpeningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOpeningBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSkip.setOnClickListener {
            callback.invoke()
        }

        binding.btnSignUp.setOnClickListener {
            toSignUpCallback.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}