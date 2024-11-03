package com.example.tubitak

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tubitak.databinding.FragmentOneBinding


class OneFragment : Fragment() {
    private lateinit var binding: FragmentOneBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.camera.setOnClickListener() {
            val action = OneFragmentDirections.actionOneFragmentToCameraFragment()
            findNavController().navigate(action)
        }
        binding.stack.setOnClickListener() {
            val action = OneFragmentDirections.actionOneFragmentToStackFragment()
            findNavController().navigate(action)
        }
    }
}