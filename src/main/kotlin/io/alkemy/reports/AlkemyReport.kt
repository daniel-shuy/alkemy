package io.alkemy.reports

import com.aventstack.extentreports.MediaEntityBuilder
import io.alkemy.AlkemyContext
import io.alkemy.config.ReportConfig
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import java.io.File

class AlkemyReport(
    private val context: AlkemyContext
) : BeforeTestListener, AfterTestListener {

    fun screenshot(testCase: TestCase, description: String? = null, failure: Boolean = false) {
        val clsName = testCase.spec::class.simpleName!!
        val parentDir = File(ReportConfig.screenshotDir, clsName)
        parentDir.mkdirs()
        val fileName = testCase.name.testName.replace(" ", "-") + "-" + System.currentTimeMillis() + ".png"
        val pngFile = File(parentDir, fileName)

        val bytes = (context.webDriver as TakesScreenshot).getScreenshotAs(OutputType.BYTES)
        pngFile.writeBytes(bytes)

        val media = MediaEntityBuilder.createScreenCaptureFromPath(pngFile.absolutePath, if (failure) "Test failed" else null).build()
        val node = ReportContext.testNodes[testCase]!!
        if (failure) {
            node.fail(description, media)
        } else {
            node.info(description, media)
        }
    }

    fun screenshotFailure(testCase: TestCase, testResult: TestResult) {
        screenshot(testCase, testResult.errorOrNull?.message, true)
    }

    override suspend fun beforeAny(testCase: TestCase) {
        ReportContext.createTestCaseNode(testCase)
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        val extentNode = ReportContext.testNodes[testCase]!!

        if (result.isErrorOrFailure) {
            screenshotFailure(testCase, result)
        }

        if (result.isSuccess) {
            extentNode.pass("PASS")
        } else if (result.isErrorOrFailure) {
            val throwable = result.errorOrNull
            if (throwable != null) {
                extentNode.fail(throwable)
            } else {
                extentNode.fail("Unknown error")
            }
        } else if (result.isIgnored) {
            extentNode.skip(result.reasonOrNull ?: "IGNORED")
        }
    }
}

