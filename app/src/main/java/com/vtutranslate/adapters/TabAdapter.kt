package com.vtutranslate.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vtutranslate.ui.LogsFragment
import com.vtutranslate.ui.SettingsFragment
import com.vtutranslate.ui.TranslateFragment

class TabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TranslateFragment()
            1 -> SettingsFragment()
            2 -> LogsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
} 