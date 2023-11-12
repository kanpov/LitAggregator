package io.github.kanpov.litaggregator.desktop.platform

import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleClientSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.ServerSocket
import java.net.URI
import java.util.Scanner

class DesktopGoogleAuthorizer(session: GoogleClientSession = GoogleClientSession()) : GoogleAuthorizer(session) {
    override var redirectUri: String = "http://127.0.0.1:|port|"

    override fun authorizeImpl(oauthUrl: String) {
        val server = ServerSocket(0)
        val port = server.localPort

        redirectUri = redirectUri.replace("|port|", port.toString())
        val oauthUri = URI.create(oauthUrl.replace("|port|", port.toString()))
        DesktopEnginePlatform.openBrowser(oauthUri)

        val client = server.accept()
        val scanner = Scanner(client.getInputStream())

        var rawData: String? = null
        while (scanner.hasNextLine()) {
            rawData = scanner.nextLine()
            break
        }

        val code = rawData!!.split(" ")[1].split("=")[1].split("&")[0]

        runBlocking {
            obtainTokens(code)
        }

        server.close()
    }
}
