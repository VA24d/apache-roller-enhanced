package org.apache.roller.weblogger.ui.rendering.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;

/**
 * Servlet endpoint for the weblog QA chatbot.
 */
public class WeblogQAServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(WeblogQAServlet.class);
    private static final Gson gson = new Gson();

    private final WeblogQaService qaService;

    public WeblogQAServlet() {
        this(new WeblogQaService());
    }

    WeblogQAServlet(WeblogQaService qaService) {
        this.qaService = qaService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            WeblogQaRequestPayload payload = gson.fromJson(
                    new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8),
                    WeblogQaRequestPayload.class);

            if (payload == null || StringUtils.isBlank(payload.weblogHandle) || StringUtils.isBlank(payload.question)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Missing required fields: weblogHandle, question\"}");
                return;
            }

            try {
                WeblogQaAnswer answer = qaService.answerQuestion(
                        payload.weblogHandle, payload.question, payload.strategy);
                out.print(gson.toJson(answer));
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (WebloggerException e) {
                log.error("Weblog QA failed", e);
                if (e.getMessage() != null && e.getMessage().contains("Weblog not found")) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (Exception e) {
                log.error("Weblog QA failed", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private String escapeJson(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static final class WeblogQaRequestPayload {
        private String weblogHandle;
        private String question;
        private String strategy;
    }
}
