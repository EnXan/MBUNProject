package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projektmbun.R

class InitialFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        findNavController().navigate(R.id.action_initialFragment_to_menuFragment)
        return inflater.inflate(R.layout.fragment_initial, container, false)
    }
}
