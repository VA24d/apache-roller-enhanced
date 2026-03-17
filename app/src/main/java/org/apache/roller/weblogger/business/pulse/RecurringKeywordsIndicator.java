/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Indicator 3: Recurring Keywords / Top Concerns.
 * Extracts the top 5 most frequent meaningful words from all comments
 * using word frequency analysis with stop-word filtering.
 */
public class RecurringKeywordsIndicator implements DiscussionIndicator {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to",
            "for", "of", "with", "by", "from", "is", "it", "this", "that",
            "was", "are", "were", "be", "been", "being", "have", "has", "had",
            "do", "does", "did", "will", "would", "could", "should", "may",
            "might", "shall", "can", "need", "dare", "not", "so", "no",
            "nor", "as", "if", "then", "than", "too", "very", "just",
            "about", "above", "after", "again", "all", "also", "am", "any",
            "because", "before", "between", "both", "each", "few", "get",
            "got", "here", "her", "him", "his", "how", "its", "into",
            "like", "more", "most", "much", "my", "new", "now", "only",
            "other", "our", "out", "over", "own", "same", "she", "some",
            "still", "such", "take", "their", "them", "these", "they",
            "those", "through", "under", "up", "us", "way", "we", "well",
            "what", "when", "where", "which", "while", "who", "whom", "why",
            "you", "your", "one", "two", "even", "back", "make", "many",
            "there", "think", "know", "said", "says", "say", "going",
            "really", "right", "don", "doesn", "didn", "won", "isn",
            "http", "https", "www", "com"
    ));

    private static final int MAX_KEYWORDS = 5;

    @Override
    public String getName() { return "recurringKeywords"; }

    @Override
    public String getLabel() { return "Recurring Keywords / Top Concerns"; }

    @Override
    public Map<String, Object> compute(CommentData data) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> wordFreq = new HashMap<>();

        for (WeblogEntryComment comment : data.getComments()) {
            String content = comment.getContent();
            if (content == null) continue;

            // Strip HTML tags
            String text = content.replaceAll("<[^>]+>", " ")
                                 .replaceAll("&\\w+;", " ");

            // Tokenize and count
            String[] words = text.toLowerCase().split("[^a-zA-Z0-9]+");
            for (String word : words) {
                if (word.length() >= 3 && !STOP_WORDS.contains(word)) {
                    wordFreq.merge(word, 1, Integer::sum);
                }
            }
        }

        // Sort by frequency descending, take top N
        List<Map.Entry<String, Integer>> sorted = wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_KEYWORDS)
                .collect(Collectors.toList());

        List<Map<String, Object>> keywords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            Map<String, Object> kw = new LinkedHashMap<>();
            kw.put("word", entry.getKey());
            kw.put("count", entry.getValue());
            keywords.add(kw);
        }

        result.put("keywords", keywords);
        result.put("totalUniqueWords", wordFreq.size());

        return result;
    }
}
