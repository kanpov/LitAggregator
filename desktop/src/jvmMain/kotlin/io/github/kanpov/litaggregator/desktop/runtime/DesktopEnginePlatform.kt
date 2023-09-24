package io.github.kanpov.litaggregator.desktop.runtime

import io.github.bonigarcia.wdm.WebDriverManager
import io.github.kanpov.litaggregator.engine.util.BasicCookie
import io.github.kanpov.litaggregator.engine.EnginePlatform
import kotlinx.coroutines.delay
import org.apache.commons.lang3.SystemUtils
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import java.io.File

object DesktopEnginePlatform : EnginePlatform {
    override val name: String
        get() = "Desktop, OS: ${SystemUtils.OS_NAME} ${SystemUtils.OS_VERSION}, architecture: ${SystemUtils.OS_ARCH}"
    override val googleClientId: String = "627773039515-rd5jl5lfgk0it63j9a09aqpd2og2vi2o.apps.googleusercontent.com"
    override val googleClientSecret: String = "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"
    override val googleAuthorizerFactory = ::DesktopGoogleAuthorizer

    override fun getCachePath(relativePath: String): String {
        val dir = File("${SystemUtils.USER_HOME}/.litaggregator/cache/")
        dir.mkdirs()
        return dir.absolutePath + "/" + relativePath
    }

    override fun getPersistentPath(relativePath: String): String {
        val dir = File("${SystemUtils.USER_HOME}/.litaggregator/")
        dir.mkdirs()
        return dir.absolutePath + "/" + relativePath
    }

    override suspend fun useBrowserEmulator(
        loginUrl: String,
        usernameInputXpath: String,
        usernameValue: String,
        passwordInputXpath: String,
        passwordValue: String,
        buttonXpath: String,
        delayAfterPageLoad: Long,
        delayAfterClick: Long
    ): Set<BasicCookie> {
        val driver = setupDriver()

        driver.get(loginUrl)
        delay(delayAfterPageLoad)

        driver.findElement(By.xpath(usernameInputXpath)).sendKeys(usernameValue)
        driver.findElement(By.xpath(passwordInputXpath)).sendKeys(passwordValue)
        driver.findElement(By.xpath(buttonXpath)).click()
        delay(delayAfterClick)

        val cookies = mutableSetOf<BasicCookie>()

        for (cookie in driver.manage().cookies) {
            cookies += BasicCookie(cookie.name, cookie.value, cookie.expiry?.toInstant())
        }

        driver.quit()
        return cookies
    }

    private fun setupDriver(): WebDriver {
        val browsers = mapOf<WebDriverManager, () -> WebDriver>(
            WebDriverManager.chromedriver() to { ChromeDriver(ChromeOptions()) },
            WebDriverManager.edgedriver() to { EdgeDriver(EdgeOptions()) as WebDriver },
            WebDriverManager.firefoxdriver() to { FirefoxDriver(FirefoxOptions()) }
        )

        for ((manager, driverFactory) in browsers) {
            if (!manager.browserPath.isPresent) continue

            manager.setup()
            return driverFactory.invoke()
        }

        throw IllegalArgumentException()
    }
}
