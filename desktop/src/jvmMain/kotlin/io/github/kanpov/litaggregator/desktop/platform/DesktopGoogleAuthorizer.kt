package io.github.kanpov.litaggregator.desktop.platform

import io.github.kanpov.litaggregator.engine.authorizer.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.authorizer.GoogleClientSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.URI
import java.util.*

class DesktopGoogleAuthorizer(session: GoogleClientSession = GoogleClientSession()) : GoogleAuthorizer(session) {
    override var redirectUri: String = "http://127.0.0.1:|port|"

    override suspend fun authorizeImpl(oauthUrl: String) {
        val server = withContext(Dispatchers.IO) {
            ServerSocket(0)
        }
        val port = server.localPort

        redirectUri = redirectUri.replace("|port|", port.toString())
        val oauthUri = URI.create(oauthUrl.replace("|port|", port.toString()))
        DesktopEnginePlatform.openBrowser(oauthUri)

        val client = withContext(Dispatchers.IO) { server.accept() }
        val scanner = Scanner(withContext(Dispatchers.IO) { client.getInputStream() })

        var rawData: String? = null
        while (scanner.hasNextLine()) {
            rawData = scanner.nextLine()
            break
        }

        val code = rawData!!.split(" ")[1].split("=")[1].split("&")[0]

        obtainTokens(code)

        withContext(Dispatchers.IO) {
            server.close()
        }
    }
}
