package com.example.tubitak

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tubitak.databinding.FragmentStackBinding


class StackFragment : Fragment() {
    private lateinit var binding: FragmentStackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStackBinding.inflate(inflater, container, false)
        return binding.root
       }

}