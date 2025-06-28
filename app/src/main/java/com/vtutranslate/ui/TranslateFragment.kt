package com.vtutranslate.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtutranslate.R
import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.adapters.StringResourceAdapter
import com.vtutranslate.data.SettingsManager
import com.vtutranslate.viewmodels.TranslateViewModel
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.RecyclerView
import com.vtutranslate.utils.XmlUtils
import java.io.File

class TranslateFragment : Fragment() {
    
    private lateinit var viewModel: TranslateViewModel
    private lateinit var originalAdapter: StringResourceAdapter
    private lateinit var translatedAdapter: StringResourceAdapter
    
    private var selectedFile: Uri? = null
    private var selectedOutputFile: Uri? = null
    
    private val logManager = VTUTranslateApp.instance.logManager
    private val settingsManager = SettingsManager()
    
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFile = uri
                viewModel.loadStringsFile(requireContext(), uri)
                logManager.log("File selected: $uri")
            }
        }
    }
    
    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedOutputFile = uri
                viewModel.saveTranslatedFile(requireContext(), uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[TranslateViewModel::class.java]
        
        setupRecyclerViews(view)
        setupButtons(view)
        observeViewModel()
    }
    
    private fun setupRecyclerViews(view: View) {
        val rvOriginalStrings = view.findViewById<RecyclerView>(R.id.rvOriginalStrings)
        val rvTranslatedStrings = view.findViewById<RecyclerView>(R.id.rvTranslatedStrings)
        
        originalAdapter = StringResourceAdapter()
        translatedAdapter = StringResourceAdapter()
        
        rvOriginalStrings.layoutManager = LinearLayoutManager(requireContext())
        rvOriginalStrings.adapter = originalAdapter
        
        rvTranslatedStrings.layoutManager = LinearLayoutManager(requireContext())
        rvTranslatedStrings.adapter = translatedAdapter
    }
    
    private fun setupButtons(view: View) {
        val btnLoadFile = view.findViewById<MaterialButton>(R.id.btnLoadFile)
        val btnTranslate = view.findViewById<MaterialButton>(R.id.btnTranslate)
        val btnSaveFile = view.findViewById<MaterialButton>(R.id.btnSaveFile)
        
        btnLoadFile.setOnClickListener {
            openFileChooser()
        }
        
        btnTranslate.setOnClickListener {
            if (viewModel.stringResources.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_no_file_selected), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val apiKey = settingsManager.getApiKey()
            if (apiKey.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.error_invalid_api_key), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.translateStrings()
        }
        
        btnSaveFile.setOnClickListener {
            if (viewModel.translatedResources.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_translation_failed), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            saveFileChooser()
        }
    }
    
    private fun observeViewModel() {
        viewModel.stringResources.observe(viewLifecycleOwner) { resources ->
            originalAdapter.submitList(resources)
        }
        
        viewModel.translatedResources.observe(viewLifecycleOwner) { resources ->
            translatedAdapter.submitList(resources)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { _ ->
            // Handle loading state if needed
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"
        }
        openFileLauncher.launch(intent)
    }
    
    private fun saveFileChooser() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"
            putExtra(Intent.EXTRA_TITLE, "strings-vi.xml")
        }
        saveFileLauncher.launch(intent)
    }
} 