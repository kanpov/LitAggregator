package io.github.kanpov.litaggregator.desktop.platform

import io.github.bonigarcia.wdm.WebDriverManager
import io.github.kanpov.litaggregator.engine.util.BrowserElement
import io.github.kanpov.litaggregator.engine.util.BrowserEmulator
import io.github.kanpov.litaggregator.engine.util.io.BasicCookie
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import java.time.Instant

object DesktopBrowserEmulator : BrowserEmulator() {
    private lateinit var driver: WebDriver

    override val loadedUrl: String
        get() = driver.currentUrl
    override val cookies: Set<BasicCookie>
        get() = buildSet {
            for (seleniumCookie in driver.manage().cookies) {
                val expiry: Instant? = try {
                    seleniumCookie.expiry.toInstant()
                } catch (_: Exception) {
                    null
                }

                this += BasicCookie(
                    name = seleniumCookie.name,
                    value = seleniumCookie.value,
                    expiry = expiry
                )
            }
        }

    override fun launch(headless: Boolean) {
        driver = setupDriver()
    }

    override fun shutdown() {
        driver.quit()
    }

    override fun loadUrl(url: String) {
        driver.get(url)
    }

    override fun findElementOrNull(xpath: String): BrowserElement? {
        return try {
            DesktopBrowserElement(driver.findElement(By.xpath(xpath)))
        } catch (_: Exception) {
            null
        }
    }

    private fun setupDriver(): WebDriver {
        // Only Chrome, Firefox and MS Edge are currently supported when added to PATH (e.g. most Linux and Windows installs)
        val browsers = mapOf<WebDriverManager, () -> WebDriver>(
            WebDriverManager.chromedriver() to { ChromeDriver(ChromeOptions()) },
            WebDriverManager.edgedriver() to { EdgeDriver(EdgeOptions()) },
            WebDriverManager.firefoxdriver() to { FirefoxDriver(FirefoxOptions()) }
        )

        for ((manager, driverFactory) in browsers) {
            if (!manager.browserPath.isPresent) continue

            manager.setup()
            return driverFactory.invoke()
        }

        throw IllegalArgumentException("No browser driver is available")
    }
}

class DesktopBrowserElement(private val element: WebElement) : BrowserElement {
    override val visibleToUser: Boolean
        get() = element.isDisplayed && element.isEnabled

    override fun click() {
        element.click()
    }

    override fun inputText(value: String) {
        element.sendKeys(value)
    }
}