@file:Suppress("UnstableApiUsage")

package com.example.ruleset

import com.android.SdkConstants
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Attr
import java.util.regex.Pattern

internal val HardcodedTextSizeUsageIssue = Issue.create(
    id = "HardcodedTextSizeUsage",
    briefDescription = "Hardcoded text size values are forbidden.",
    explanation = "Hardcoded text size values are forbidden. Please, replace with a dimen resource.",
    category = Category.TYPOGRAPHY,
    severity = Severity.WARNING,
    implementation = Implementation(
        HardcodedTextSizeUsageDetector::class.java,
        Scope.RESOURCE_FILE_SCOPE,
    ),
    androidSpecific = true,
)

internal class HardcodedTextSizeUsageDetector : LayoutDetector() {

    private val hardcodedResourcePattern = Pattern.compile("^\\d+(sp|dp|px)\$")

    override fun getApplicableAttributes(): Collection<String> {
        return listOf(SdkConstants.ATTR_TEXT_SIZE)
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        if (attribute.namespaceURI != SdkConstants.ANDROID_URI) return

        if (hardcodedResourcePattern.matcher(attribute.value).matches()) {
            context.report(
                issue = HardcodedTextSizeUsageIssue,
                location = context.getValueLocation(attribute),
                message = "Using hardcoded text sizes is forbidden.",
            )
        }
    }
}