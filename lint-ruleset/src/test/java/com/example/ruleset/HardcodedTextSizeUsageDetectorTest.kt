@file:Suppress("UnstableApiUsage")

package com.example.ruleset

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.jupiter.api.Test

internal class HardcodedTextSizeUsageDetectorTest {

    @Test
    fun `given resource is used in textSize when detector applies then HardcodedTextSizeUsageIssue is not reported`() {
        val layout = xml(
            "res/layout/activity_main.xml",
            """
                <TextView xmlns:android="http://schemas.android.com/apk/res/android"
                    android:textSize="@dimen/textSizeBig"/>
            """
        ).indented()

        lint()
            .files(layout)
            .issues(HardcodedTextSizeUsageIssue)
            .run()
            .expectClean()
    }

    @Test
    fun `given hardcoded SP value is used in textSize when detector applies then HardcodedTextSizeUsageIssue is reported`() {
        val layout = xml(
            "res/layout/activity_main.xml",
            """
                <TextView xmlns:android="http://schemas.android.com/apk/res/android"
                    android:textSize="18sp"/>
            """
        ).indented()

        lint()
            .files(layout)
            .issues(HardcodedTextSizeUsageIssue)
            .run()
            .expectWarningCount(1)
    }
}