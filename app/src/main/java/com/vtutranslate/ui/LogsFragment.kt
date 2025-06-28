package com.vtutranslate.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.vtutranslate.R
import com.vtutranslate.VTUTranslateApp

class LogsFragment : Fragment() {
    
    private lateinit var tvLogs: TextView
    private val logManager = VTUTranslateApp.instance.logManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tvLogs = view.findViewById(R.id.tvLogs)
        val btnClearLogs = view.findViewById<MaterialButton>(R.id.btnClearLogs)
        val btnCopyLogs = view.findViewById<MaterialButton>(R.id.btnCopyLogs)
        
        btnClearLogs.setOnClickListener {
            clearLogs()
        }
        
        btnCopyLogs.setOnClickListener {
            copyLogs()
        }
        
        observeLogs()
    }
    
    private fun observeLogs() {
        logManager.logs.observe(viewLifecycleOwner) { logs ->
            tvLogs.text = logs
        }
    }
    
    private fun clearLogs() {
        logManager.clear()
        logManager.log("Logs cleared")
        Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
    }
    
    private fun copyLogs() {
        val logs = logManager.getLogs()
        
        if (logs.isNotEmpty()) {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("VTU Translate Logs", logs)
            clipboard.setPrimaryClip(clip)
            
            logManager.log("Logs copied to clipboard")
            Toast.makeText(context, getString(R.string.success_logs_copied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh logs when fragment becomes visible
        tvLogs.text = logManager.getLogs()
    }
} 