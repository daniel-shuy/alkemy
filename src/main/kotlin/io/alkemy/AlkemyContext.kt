package io.alkemy

import io.alkemy.assertions.shouldBeDisabled
import io.alkemy.assertions.shouldBeEnabled
import io.alkemy.assertions.shouldBeHidden
import io.alkemy.assertions.shouldBeVisible
import io.alkemy.assertions.shouldHaveClass
import io.alkemy.assertions.shouldHaveText
import io.alkemy.config.AlkemyConfig
import io.alkemy.extensions.clearText
import io.alkemy.extensions.find
import io.alkemy.extensions.typeIn
import io.alkemy.extensions.value
import io.alkemy.pom.Page
import io.alkemy.reports.AlkemyReport
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import kotlin.reflect.full.primaryConstructor

class AlkemyContext(
    val config: AlkemyConfig,
    val webDriverProvider: () -> WebDriver
) {

    companion object {
        fun withConfig(config: AlkemyConfig): AlkemyContext {
            val driver = config.newWebDriver()
            return AlkemyContext(config) { driver }
        }
    }

    val webDriver: WebDriver get() = webDriverProvider()
    val report: AlkemyReport = AlkemyReport(this)

    fun get(relativeUrl: String): WebDriver {
        webDriver.get(config.baseUrl + relativeUrl)
        return webDriver
    }

    inline fun <reified P : Page> goTo(): P {
        val page = P::class.primaryConstructor!!.call(this)
        get(page.relativeUrl)
        return page
    }

    val testSelector: String = config.testSelectorAttribute

    fun byTestSelector(value: String): WebElement {
        return webDriver.find("[$testSelector=$value]")
    }

    fun String.click(): String {
        webDriver.find(this).click()
        return this
    }

    fun String.clearText(): String {
        webDriver.find(this).clearText()
        return this
    }

    infix fun String.find(cssSelector: String): String {
        webDriver.find(this).find(cssSelector)
        return this
    }

    infix fun String.type(value: String): String {
        webDriver.typeIn(this, value)
        return this
    }

    infix fun String.shouldHaveText(text: String): String {
        webDriver.shouldHaveText(this, text)
        return this
    }

    infix fun String.shouldHaveClass(cssClass: String): String {
        webDriver.find(this).shouldHaveClass(cssClass)
        return this
    }

    fun String.shouldBeEnabled(): String {
        webDriver.find(this).shouldBeEnabled()
        return this
    }

    infix fun String.shouldBeEnabled(maxWaitSeconds: Int): String {
        webDriver.find(this).shouldBeEnabled(maxWaitSeconds)
        return this
    }

    fun String.shouldBeDisabled(): String {
        webDriver.find(this).shouldBeDisabled()
        return this
    }

    infix fun String.shouldBeDisabled(maxWaitSeconds: Int): String {
        webDriver.find(this).shouldBeDisabled(maxWaitSeconds)
        return this
    }

    fun String.shouldBeVisible(): String {
        webDriver.find(this).shouldBeVisible()
        return this
    }

    fun String.shouldBeHidden(): String {
        webDriver.find(this).shouldBeHidden()
        return this
    }

    val String.inputValue: String
        get() {
            return webDriver.find(this).value
        }

    val String.text: String
        get() {
            return webDriver.find(this).text
        }
}

fun AlkemyContext.use(block: AlkemyContext.() -> Unit) {
    try {
        this.block()
    } finally {
        this.webDriver.close()
    }
}