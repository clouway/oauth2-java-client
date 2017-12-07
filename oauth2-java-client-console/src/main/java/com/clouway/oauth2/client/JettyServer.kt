package com.clouway.oauth2.client

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * That server provides endpoint which listen for authorization response.
 *
 * @author Ianislav Nachev <qnislav.nachev@clouway.com>
 */
internal class JettyServer(port: Int) {

    private val server: Server = Server(port)
    val callbackUri = "http://localhost:$port/oauth2callback"

    fun startServer() {
        val servletContextHandler = ServletContextHandler(ServletContextHandler.SESSIONS)
        servletContextHandler.contextPath = "/"

        servletContextHandler.addEventListener(object : ServletContextListener {
            override fun contextInitialized(servletContextEvent: ServletContextEvent) {
                val servletContext = servletContextEvent.servletContext
                servletContext.addServlet("pageHandlerServlet", OAuth2CallbackServlet()).addMapping("/oauth2callback")
            }

            override fun contextDestroyed(servletContextEvent: ServletContextEvent) {}
        })

        server.handler = servletContextHandler

        try {
            server.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Server is up.")
    }

    fun stopServer() {
        server.stop()
        println("Server was shutted down")
    }
}

internal class OAuth2CallbackServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val authCode = req.getParameter("code")
        resp.writer.write("Authorization code: " + authCode)
    }
}