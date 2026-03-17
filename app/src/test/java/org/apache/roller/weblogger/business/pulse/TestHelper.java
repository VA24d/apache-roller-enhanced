package org.apache.roller.weblogger.business.pulse;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Test utility to create WeblogEntryComment instances for unit tests
 * without needing the full Roller infrastructure.
 */
public class TestHelper {

    public static WeblogEntryComment comment(String name, String content, Timestamp postTime) {
        WeblogEntryComment c = new WeblogEntryComment();
        c.setName(name);
        c.setContent(content);
        c.setPostTime(postTime);
        c.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        return c;
    }

    public static WeblogEntryComment comment(String name, String content) {
        return comment(name, content, new Timestamp(System.currentTimeMillis()));
    }

    public static List<WeblogEntryComment> generateComments(int count, long startMs, long endMs) {
        List<WeblogEntryComment> comments = new ArrayList<>();
        long step = (endMs - startMs) / Math.max(count, 1);
        for (int i = 0; i < count; i++) {
            comments.add(comment(
                    "User" + (i % 5),
                    "Comment number " + i + " about topic" + (i % 3),
                    new Timestamp(startMs + i * step)
            ));
        }
        return comments;
    }
}
