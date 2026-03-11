# Code Analysis Tool Results Summary

**Date:** February 3, 2026  
**Project:** Apache Roller 6.1.5

## Executed Analysis Tools

### 1. Checkstyle ✓
- **Configuration:** Sun Checks (sun_checks.xml)
- **Total Violations:** 28,023
- **Output Location:** `analysis/checkstyle/checkstyle-result.xml`

### 2. PMD ✓
- **Version:** 7.17.0
- **Total Violations:** 276
- **Output Location:** `analysis/pmd.xml`

### 3. CK Metrics (Chidamber & Kemerer) ✓
- **Source:** Designite Type Metrics
- **Classes Analyzed:** 517
- **Output Location:** `analysis/designite/ck_metrics.csv`

### 4. Designite (Already Run) ✓
- **Design Smells:** 491 instances
- **Implementation Smells:** 949 instances
- **Output Location:** `analysis/designite/`

---

## Key Metrics Summary

| Tool | Metric | Value |
|------|--------|-------|
| Checkstyle | Total Violations | 28,023 |
| PMD | Total Violations | 276 |
| Designite | Design Smells | 491 |
| Designite | Implementation Smells | 949 |
| CK Metrics | Classes Analyzed | 517 |

---

## Top 10 Most Complex Classes (by WMC)

| Rank | Class Name | WMC | DIT | LCOM | FANIN | FANOUT |
|------|------------|-----|-----|------|-------|--------|
| 1 | JPAWeblogEntryManagerImpl | 165 | 1 | 0.04 | 3 | 17 |
| 2 | WeblogEntry | 134 | 0 | 0.13 | 54 | 20 |
| 3 | Weblog | 127 | 0 | 0.14 | 113 | 20 |
| 4 | Utilities | 110 | 0 | 0.65 | 46 | 3 |
| 5 | JPAWeblogManagerImpl | 90 | 1 | 0.07 | 1 | 20 |
| 6 | JPAMediaFileManagerImpl | 88 | 1 | 0.07 | 1 | 15 |
| 7 | DatabaseInstaller | 86 | 0 | 0.0 | 1 | 3 |
| 8 | URLModel | 71 | 1 | 0.11 | 0 | 8 |
| 9 | JPAUserManagerImpl | 69 | 1 | 0.06 | 1 | 8 |
| 10 | PageServlet | 69 | 0 | 0.0 | 0 | 21 |

**WMC** = Weighted Methods per Class (higher = more complex)  
**DIT** = Depth of Inheritance Tree  
**LCOM** = Lack of Cohesion of Methods (higher = less cohesive)  
**FANIN** = Number of classes depending on this class  
**FANOUT** = Number of classes this class depends on

---

## Files Available for Analysis

```
/Users/va/SE assignment 1/analysis/
├── checkstyle/
│   ├── checkstyle-result.xml (29,166 lines)
│   └── checkstyle-google.xml
├── designite/
│   ├── designCodeSmells.csv (491 smells)
│   ├── implementationCodeSmells.csv (949 smells)
│   ├── methodMetrics.csv (5,074 methods)
│   ├── typeMetrics.csv (517 classes)
│   └── ck_metrics.csv (CK metrics extract)
└── pmd.xml (276 violations)
```

All analysis tools have been successfully executed and results are available in the `/analysis` directory.
