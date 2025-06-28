package com.vtutranslate.viewmodels

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.models.StringResource
import com.vtutranslate.utils.XmlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class TranslateViewModel : ViewModel() {

    private val apiClient = VTUTranslateApp.instance.apiClient
    private val logManager = VTUTranslateApp.instance.logManager
    
    // Original strings resources
    private val _stringResources = MutableLiveData<List<StringResource>>(emptyList())
    val stringResources: LiveData<List<StringResource>> = _stringResources
    
    // Translated strings resources
    private val _translatedResources = MutableLiveData<List<StringResource>>(emptyList())
    val translatedResources: LiveData<List<StringResource>> = _translatedResources
    
    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error state
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    // Current file
    private var currentFile: File? = null
    
    /**
     * Load strings from a strings.xml file
     */
    fun loadStringsFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                withContext(Dispatchers.IO) {
                    // Copy the file to app's cache directory to work with it
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val fileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "strings.xml"
                    val tempFile = File(context.cacheDir, fileName)
                    
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                    
                    inputStream?.close()
                    currentFile = tempFile
                    
                    // Parse the file
                    val resources = XmlUtils.parseStringsXml(tempFile)
                    
                    withContext(Dispatchers.Main) {
                        _stringResources.value = resources
                        logManager.log("Loaded ${resources.size} strings from $fileName")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error loading file: ${e.message}"
                logManager.log("Error loading file: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Translate the loaded strings
     */
    fun translateStrings() {
        val resources = _stringResources.value ?: return
        
        if (resources.isEmpty()) {
            _error.value = "No strings to translate"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val translatedResources = apiClient.translateStrings(resources)
                _translatedResources.value = translatedResources
            } catch (e: Exception) {
                _error.value = "Translation error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Save translated strings to a file
     */
    fun saveTranslatedFile(context: Context, outputUri: Uri) {
        val resources = _translatedResources.value ?: return
        
        if (resources.isEmpty()) {
            _error.value = "No translated strings to save"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                withContext(Dispatchers.IO) {
                    // Create temporary output file
                    val tempOutputFile = File(context.cacheDir, "temp_output.xml")
                    
                    // Write translated strings to temp file
                    if (currentFile != null && XmlUtils.writeTranslatedStringsXml(currentFile!!, tempOutputFile, resources)) {
                        // Copy temp file to the selected output URI
                        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                            tempOutputFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        withContext(Dispatchers.Main) {
                            logManager.log("Translated strings saved successfully")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _error.value = "Error saving translated strings"
                            logManager.log("Error saving translated strings")
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error saving file: ${e.message}"
                logManager.log("Error saving file: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 