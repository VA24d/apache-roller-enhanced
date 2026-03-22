package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.List;

/**
 * Answering strategy for the weblog QA chatbot.
 */
public interface WeblogQaAnswerStrategy {

    String getName();

    WeblogQaAnswer answer(String question, List<WeblogQaEntryDocument> documents);
}
