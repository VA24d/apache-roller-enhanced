package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;

/**
 * Repository abstraction for entry retrieval used by the QA service.
 */
public interface WeblogQaEntryRepository {

    List<WeblogQaEntryDocument> getPublishedEntries(String weblogHandle) throws WebloggerException;
}
