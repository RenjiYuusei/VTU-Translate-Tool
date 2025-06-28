package com.vtutranslate.utils

import com.vtutranslate.VTUTranslateApp
import com.vtutranslate.models.StringResource
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.io.StringWriter

object XmlUtils {
    
    private val logManager = VTUTranslateApp.instance.logManager
    
    /**
     * Parse strings.xml file and extract string resources
     */
    fun parseStringsXml(file: File): List<StringResource> {
        val resources = mutableListOf<StringResource>()
        
        try {
            val inputStream = FileInputStream(file)
            val reader = InputStreamReader(inputStream)
            
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(reader)
            
            var eventType = parser.eventType
            var currentName: String? = null
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "string") {
                            currentName = parser.getAttributeValue(null, "name")
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (currentName != null) {
                            val value = parser.text
                            resources.add(StringResource(currentName, value))
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "string") {
                            currentName = null
                        }
                    }
                }
                eventType = parser.next()
            }
            
            inputStream.close()
            logManager.log("Parsed ${resources.size} strings from ${file.name}")
            
        } catch (e: Exception) {
            logManager.log("Error parsing XML: ${e.message}")
            throw e
        }
        
        return resources
    }
    
    /**
     * Write translated strings to a new XML file
     */
    fun writeTranslatedStringsXml(
        outputFile: File,
        translatedStrings: List<StringResource>
    ): Boolean {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val serializer = factory.newSerializer()
            
            val writer = StringWriter()
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.text("\n")
            serializer.startTag("", "resources")
            serializer.text("\n")
            
            // Add each translated string
            for (string in translatedStrings) {
                serializer.text("    ")
                serializer.startTag("", "string")
                serializer.attribute("", "name", string.name)
                serializer.text(string.translatedValue)
                serializer.endTag("", "string")
                serializer.text("\n")
            }
            
            serializer.endTag("", "resources")
            serializer.endDocument()
            
            // Write to file
            val outputStream = FileOutputStream(outputFile)
            outputStream.write(writer.toString().toByteArray())
            outputStream.close()
            
            logManager.log("Saved ${translatedStrings.size} translated strings to ${outputFile.name}")
            true
            
        } catch (e: Exception) {
            logManager.log("Error writing XML: ${e.message}")
            false
        }
    }
} 