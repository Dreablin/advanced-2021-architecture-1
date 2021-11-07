@file:Suppress("UnstableApiUsage")

package com.example.ruleset

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

internal class ExampleIssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues = listOf(HardcodedTextSizeUsageIssue)
}