package com.vtutranslate.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.vtutranslate.R
import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.data.SettingsManager
import com.vtutranslate.models.ModelType
import com.vtutranslate.models.TranslationModel

class SettingsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private val logManager = VTUTranslateApp.instance.logManager

    private lateinit var etApiKey: TextInputEditText
    private lateinit var cbHideApiKey: CheckBox
    private lateinit var rgGeminiModels: RadioGroup
    private lateinit var rgDeepseekModels: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager()

        initializeViews(view)
        loadSettings()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        etApiKey = view.findViewById(R.id.etApiKey)
        cbHideApiKey = view.findViewById(R.id.cbHideApiKey)
        rgGeminiModels = view.findViewById(R.id.rgGeminiModels)
        rgDeepseekModels = view.findViewById(R.id.rgDeepseekModels)
    }

    private fun loadSettings() {
        // Load API key
        etApiKey.setText(settingsManager.getApiKey())

        // Load selected model
        val currentModel = settingsManager.getSelectedModel()

        // Check appropriate radio button based on model type and ID
        when (currentModel.type) {
            ModelType.GEMINI -> {
                when (currentModel) {
                    TranslationModel.GEMMA_27B -> view?.findViewById<RadioButton>(R.id.rbGemini27b)?.isChecked = true
                    TranslationModel.GEMINI_2_FLASH -> view?.findViewById<RadioButton>(R.id.rbGemini2Flash)?.isChecked = true
                    else -> {}
                }
                // Clear selection in the other group
                rgDeepseekModels.clearCheck()
            }
            ModelType.DEEPSEEK -> {
                when (currentModel) {
                    TranslationModel.DEEPSEEK_R1 -> view?.findViewById<RadioButton>(R.id.rbDeepseekR1)?.isChecked = true
                    TranslationModel.DEEPSEEK_V3 -> view?.findViewById<RadioButton>(R.id.rbDeepseekV3)?.isChecked = true
                    else -> {}
                }
                // Clear selection in the other group
                rgGeminiModels.clearCheck()
            }
        }
    }

    private fun setupListeners() {
        // API key visibility toggle
        cbHideApiKey.setOnCheckedChangeListener { _, isChecked ->
            etApiKey.inputType = if (isChecked) {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                android.text.InputType.TYPE_CLASS_TEXT
            }
            etApiKey.setSelection(etApiKey.text?.length ?: 0) // Set cursor to end
        }

        // Get API key button
        view?.findViewById<MaterialButton>(R.id.btnGetApiKey)?.setOnClickListener {
            openApiKeyWebsite()
        }

        // Radio group listeners
        rgGeminiModels.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                // When a Gemini model is selected, clear DeepSeek selection
                rgDeepseekModels.clearCheck()
            }
        }

        rgDeepseekModels.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                // When a DeepSeek model is selected, clear Gemini selection
                rgGeminiModels.clearCheck()
            }
        }

        // Save settings button
        view?.findViewById<MaterialButton>(R.id.btnSaveSettings)?.setOnClickListener {
            saveSettings()
        }
    }

    private fun openApiKeyWebsite() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
        startActivity(browserIntent)
        logManager.log("Opening OpenRouter API key website")
    }

    private fun saveSettings() {
        // Save API key
        val apiKey = etApiKey.text.toString()
        if (apiKey.isBlank()) {
            Toast.makeText(context, getString(R.string.error_invalid_api_key), Toast.LENGTH_SHORT).show()
            return
        }
        settingsManager.saveApiKey(apiKey)
        
        // Save selected model
        val selectedModel = getSelectedModel()
        if (selectedModel != null) {
            settingsManager.saveSelectedModel(selectedModel)
            logManager.log("Selected model: ${selectedModel.displayName}")
        } else {
            // No model selected, show error
            Toast.makeText(context, "Please select a model", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, getString(R.string.success_settings_saved), Toast.LENGTH_SHORT).show()
        logManager.log("Settings saved")
    }
    
    private fun getSelectedModel(): TranslationModel? {
        val geminiCheckedId = rgGeminiModels.checkedRadioButtonId
        val deepseekCheckedId = rgDeepseekModels.checkedRadioButtonId
        
        return when {
            geminiCheckedId == R.id.rbGemini27b -> TranslationModel.GEMMA_27B
            geminiCheckedId == R.id.rbGemini2Flash -> TranslationModel.GEMINI_2_FLASH
            deepseekCheckedId == R.id.rbDeepseekR1 -> TranslationModel.DEEPSEEK_R1
            deepseekCheckedId == R.id.rbDeepseekV3 -> TranslationModel.DEEPSEEK_V3
            else -> null
        }
    }
} 