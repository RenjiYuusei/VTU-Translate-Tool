package com.vtu.translate.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.vtu.translate.ui.theme.XmlAttribute
import com.vtu.translate.ui.theme.XmlAttributeValue
import com.vtu.translate.ui.theme.XmlTag
import com.vtu.translate.ui.theme.XmlText

@Composable
fun XmlHighlighter(xmlContent: String, modifier: Modifier = Modifier) {
    val annotatedString = buildAnnotatedString {
        val tagRegex = Regex("<(/)?([a-zA-Z0-9_:]+)([^>]*)>")
        val attributeRegex = Regex("([a-zA-Z0-9_:]+)="([^"]*)"")

        var lastIndex = 0
        tagRegex.findAll(xmlContent).forEach { tagMatch ->
            // Append text before the tag
            if (tagMatch.range.first > lastIndex) {
                withStyle(style = SpanStyle(color = XmlText)) {
                    append(xmlContent.substring(lastIndex, tagMatch.range.first))
                }
            }

            // Append the tag itself
            withStyle(style = SpanStyle(color = XmlTag)) {
                append(tagMatch.value.substring(0, 1)) // <
                append(tagMatch.value.substring(1, tagMatch.value.length - 1).split(" ")[0]) // tag name
            }

            // Append attributes
            val attributes = tagMatch.groups[3]?.value ?: ""
            attributeRegex.findAll(attributes).forEach { attrMatch ->
                withStyle(style = SpanStyle(color = XmlAttribute)) {
                    append(" ")
                    append(attrMatch.groups[1]?.value) // attribute name
                }
                withStyle(style = SpanStyle(color = XmlTag)) {
                    append("=")
                }
                withStyle(style = SpanStyle(color = XmlAttributeValue)) {
                    append(""")
                    append(attrMatch.groups[2]?.value) // attribute value
                    append(""")
                }
            }

            withStyle(style = SpanStyle(color = XmlTag)) {
                append(tagMatch.value.substring(tagMatch.value.length - 1)) // >
            }

            lastIndex = tagMatch.range.last + 1
        }

        // Append any remaining text after the last tag
        if (lastIndex < xmlContent.length) {
            withStyle(style = SpanStyle(color = XmlText)) {
                append(xmlContent.substring(lastIndex))
            }
        }
    }
    Text(text = annotatedString, modifier = modifier)
}
