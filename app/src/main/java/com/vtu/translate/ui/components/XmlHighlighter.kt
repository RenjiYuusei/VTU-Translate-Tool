package com.vtu.translate.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.vtu.translate.ui.theme.XmlAttribute
import com.vtu.translate.ui.theme.XmlAttributeValue
import com.vtu.translate.ui.theme.XmlComment
import com.vtu.translate.ui.theme.XmlTag
import com.vtu.translate.ui.theme.XmlText

@Composable
fun XmlHighlighter(xmlContent: String, modifier: Modifier = Modifier) {
    val annotatedString = buildAnnotatedString {
        val tagRegex = Regex("<(/)?([a-zA-Z0-9_:]+)([^>]*)")
        val attributeRegex = Regex("([a-zA-Z0-9_:]+)=\"([^\"]*)\"")
        val commentRegex = Regex("""<!--[\s\S]*?-->""")

        var lastIndex = 0
        val matches = (tagRegex.findAll(xmlContent) + commentRegex.findAll(xmlContent)).sortedBy { it.range.first }

        matches.forEach { match ->
            // Append text before the current match
            if (match.range.first > lastIndex) {
                withStyle(style = SpanStyle(color = XmlText)) {
                    append(xmlContent.substring(lastIndex, match.range.first))
                }
            }

            if (match.value.startsWith("<!--")) {
                // It's a comment
                withStyle(style = SpanStyle(color = XmlComment)) {
                    append(match.value)
                }
            } else {
                // It's a tag
                val tagMatch = tagRegex.matchEntire(match.value)
                if (tagMatch != null) {
                    withStyle(style = SpanStyle(color = XmlTag)) {
                        append(tagMatch.value.substring(0, 1)) // < or </
                        append(tagMatch.groups[2]?.value) // tag name
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
                            append("\"")
                            append(attrMatch.groups[2]?.value) // attribute value
                            append("\"")
                        }
                    }

                    withStyle(style = SpanStyle(color = XmlTag)) {
                        append(tagMatch.value.substring(tagMatch.value.length - 1)) // >
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        // Append any remaining text after the last match
        if (lastIndex < xmlContent.length) {
            withStyle(style = SpanStyle(color = XmlText)) {
                append(xmlContent.substring(lastIndex))
            }
        }
    }
    Text(text = annotatedString, modifier = modifier)
}