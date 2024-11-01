package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.projektmbun.R

class RecipesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Layout für dieses Fragment festlegen
        return inflater.inflate(R.layout.fragment_receipts, container, false)
    }
}