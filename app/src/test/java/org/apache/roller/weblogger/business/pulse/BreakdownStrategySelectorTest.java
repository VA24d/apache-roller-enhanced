package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BreakdownStrategySelectorTest {

    private BreakdownStrategySelector selector;

    @BeforeEach
    void setUp() {
        selector = new BreakdownStrategySelector();
    }

    @Test
    void testSmallCommentSetUsesTfIdf() {
        BreakdownStrategy strategy = selector.selectStrategy(3);
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testZeroCommentSetUsesTfIdf() {
        BreakdownStrategy strategy = selector.selectStrategy(0);
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testMediumCommentSetWithoutLlm() {
        // Without LLM configured, should use TF-IDF
        BreakdownStrategy strategy = selector.selectStrategy(15);
        assertEquals("tfidf", strategy.getName());
    }

    @Test
    void testLargeCommentSetWithoutLlm() {
        // Without LLM configured, should fall back to TF-IDF
        BreakdownStrategy strategy = selector.selectStrategy(100);
        assertEquals("tfidf", strategy.getName());
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
    void testAvailableStrategiesWithoutLlm() {
        BreakdownStrategy[] strategies = selector.getAvailableStrategies();
        // Without API key, only TF-IDF should be available
        assertEquals(1, strategies.length);
        assertEquals("tfidf", strategies[0].getName());
    }
}
