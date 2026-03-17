/*
 * Licensed under the Apache License, Version 2.0.
 * See the NOTICE file for additional copyright information.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.Collections;
import java.util.List;

/**
 * Immutable result of a single dashboard metric computation.
 */
public class MetricResult {

    private final String name;
    private final String label;
    private final String value;
    private final List<String> details;

    public MetricResult(String name, String label, String value) {
        this(name, label, value, Collections.emptyList());
    }

    public MetricResult(String name, String label, String value,
                        List<String> details) {
        this.name = name;
        this.label = label;
        this.value = value;
        this.details = details != null
                ? Collections.unmodifiableList(details) : Collections.emptyList();
    }

    public String getName() { return name; }
    public String getLabel() { return label; }
    public String getValue() { return value; }
    public List<String> getDetails() { return details; }
}
