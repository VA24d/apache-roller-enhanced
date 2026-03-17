package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BreakdownStrategySelectorTest {

    private BreakdownStrategySelector selector;
    private boolean llmConfigured;

    @BeforeEach
    void setUp() {
        selector = new BreakdownStrategySelector();
        // Detect actual config state so tests pass regardless of apiKey setting
        String apiKey = WebloggerConfig.getProperty("pulse.llm.apiKey", "");
        llmConfigured = apiKey != null && !apiKey.trim().isEmpty();
    }

    @Test
    void testSmallCommentSetAlwaysUsesTfIdf() {
        // Regardless of LLM config, small sets always use TF-IDF
        BreakdownStrategy strategy = selector.selectStrategy(3);
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testZeroCommentSetUsesTfIdf() {
        BreakdownStrategy strategy = selector.selectStrategy(0);
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testMediumCommentSetSelection() {
        BreakdownStrategy strategy = selector.selectStrategy(15);
        if (llmConfigured) {
            assertEquals("hybrid-llm", strategy.getName());
        } else {
            assertEquals("tfidf", strategy.getName());
        }
    }

    @Test
    void testLargeCommentSetSelection() {
        BreakdownStrategy strategy = selector.selectStrategy(100);
        if (llmConfigured) {
            assertEquals("hybrid-llm", strategy.getName());
        } else {
            assertEquals("tfidf", strategy.getName());
        }
    }

    @Test
    void testGetStrategyByNameTfIdf() {
        BreakdownStrategy strategy = selector.getStrategyByName("tfidf");
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testGetStrategyByNameHybrid() {
        BreakdownStrategy strategy = selector.getStrategyByName("hybrid-llm");
        assertEquals("hybrid-llm", strategy.getName());
    }

    @Test
    void testGetStrategyByNameUnknownDefaultsTfIdf() {
        BreakdownStrategy strategy = selector.getStrategyByName("unknown");
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testAvailableStrategiesMatchConfig() {
        BreakdownStrategy[] strategies = selector.getAvailableStrategies();
        if (llmConfigured) {
            assertEquals(2, strategies.length);
        } else {
            assertEquals(1, strategies.length);
            assertEquals("tfidf", strategies[0].getName());
        }
    }
}
