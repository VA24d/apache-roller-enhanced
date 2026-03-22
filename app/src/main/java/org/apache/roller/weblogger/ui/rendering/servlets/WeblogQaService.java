package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;

/**
 * Entry point for weblog QA answers.
 */
public class WeblogQaService {

    private final WeblogQaEntryRepository entryRepository;
    private final Map<String, WeblogQaAnswerStrategy> strategies = new LinkedHashMap<>();
    private final AutoStrategyResolver autoStrategyResolver;

    public WeblogQaService() {
        this(new BusinessLayerWeblogQaEntryRepository(),
                List.of(
                        new RetrievalAugmentedWeblogQaStrategy(),
                        new LongContextWeblogQaStrategy()),
                new GeminiQaAutoStrategySelector());
    }

    WeblogQaService(WeblogQaEntryRepository entryRepository, List<WeblogQaAnswerStrategy> strategies,
            AutoStrategyResolver autoStrategyResolver) {
        this.entryRepository = entryRepository;
        this.autoStrategyResolver = autoStrategyResolver;
        for (WeblogQaAnswerStrategy strategy : strategies) {
            this.strategies.put(strategy.getName(), strategy);
        }
    }

    public WeblogQaAnswer answerQuestion(String weblogHandle, String question, String strategyName)
            throws WebloggerException {
        if (StringUtils.isBlank(weblogHandle)) {
            throw new IllegalArgumentException("weblogHandle is required");
        }
        if (StringUtils.isBlank(question)) {
            throw new IllegalArgumentException("question is required");
        }

        ResolvedStrategy resolvedStrategy = resolveStrategy(question.trim(), strategyName);
        List<WeblogQaEntryDocument> documents = entryRepository.getPublishedEntries(weblogHandle);
        WeblogQaAnswer baseAnswer = resolvedStrategy.strategy.answer(question.trim(), documents);
        return new WeblogQaAnswer(
                baseAnswer.getStrategy(),
                baseAnswer.getQuestion(),
                baseAnswer.getAnswer(),
                baseAnswer.getSources(),
                baseAnswer.getEntryCount(),
                baseAnswer.getSupportingPassageCount(),
                baseAnswer.isTruncatedContext(),
                resolvedStrategy.reason != null ? resolvedStrategy.reason : baseAnswer.getStrategyReason());
    }

    private ResolvedStrategy resolveStrategy(String question, String requestedStrategy) {
        String normalized = StringUtils.defaultIfBlank(requestedStrategy, "auto").toLowerCase(Locale.ROOT);
        if (!"auto".equals(normalized)) {
            WeblogQaAnswerStrategy strategy = strategies.getOrDefault(normalized, strategies.get("rag"));
            return new ResolvedStrategy(strategy, null);
        }

        StrategyDecision decision = autoStrategyResolver.select(question);
        WeblogQaAnswerStrategy strategy = strategies.getOrDefault(decision.strategyName, strategies.get("rag"));
        return new ResolvedStrategy(strategy, decision.reason);
    }

    interface AutoStrategyResolver {
        StrategyDecision select(String question);
    }

    static final class StrategyDecision {
        private final String strategyName;
        private final String reason;

        StrategyDecision(String strategyName, String reason) {
            this.strategyName = strategyName;
            this.reason = reason;
        }
    }

    private static final class ResolvedStrategy {
        private final WeblogQaAnswerStrategy strategy;
        private final String reason;

        private ResolvedStrategy(WeblogQaAnswerStrategy strategy, String reason) {
            this.strategy = strategy;
            this.reason = reason;
        }
    }
}
