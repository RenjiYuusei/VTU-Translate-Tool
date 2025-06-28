package com.vtutranslate.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.vtutranslate.R
import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.adapters.TabAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        // Set up toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Set up ViewPager with TabLayout
        setupViewPager()
    }
    
    private fun setupViewPager() {
        // Set up adapter
        val tabAdapter = TabAdapter(this)
        viewPager.adapter = tabAdapter
        
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.tab_translate)
                    tab.setIcon(R.drawable.ic_translate)
                }
                1 -> {
                    tab.text = getString(R.string.tab_settings)
                    tab.setIcon(R.drawable.ic_settings)
                }
                2 -> {
                    tab.text = getString(R.string.tab_logs)
                    tab.setIcon(R.drawable.ic_logs)
                }
            }
        }.attach()
        
        // Log startup information
        val logManager = VTUTranslateApp.instance.logManager
        logManager.log("Application started - VTU Translate Tool")
    }
} *