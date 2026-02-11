# Issue #9: Break Cyclic Dependencies in search.lucene Package

## Implementation Plan

### Problem

The `search.lucene` package has a tight cyclic dependency chain:

```
LuceneIndexManager → creates operations (passes `this`)
     ↑                         ↓
     └── operations hold a `LuceneIndexManager` field
         and call back into it (instance + static methods)
         
IndexUtil.getTerm() → calls LuceneIndexManager.getAnalyzer() (static)
```

**Cycle 1**: `LuceneIndexManager` ↔ `IndexOperation` (field + constructor)  
**Cycle 2**: `LuceneIndexManager` ↔ `SearchOperation` (cast from `IndexManager` to `LuceneIndexManager`)  
**Cycle 3**: `LuceneIndexManager` ↔ `IndexUtil` (static call to `getAnalyzer()`)

### Design Principles Applied

| Principle | Application |
|-----------|-------------|
| **GRASP Indirection** | New `IndexOperationContext` interface acts as intermediary between manager and operations |
| **GRASP Low Coupling** | Operations depend on a narrow 5-method interface instead of the full 486-line manager class |
| **GRASP Pure Fabrication** | `IndexOperationContext` is a design construct created solely to reduce coupling |
| **GRASP Information Expert** | `LuceneIndexManager` remains the expert that implements the context methods |
| **DIP (Dependency Inversion)** | Operations depend on abstraction (`IndexOperationContext`) not concretion (`LuceneIndexManager`) |
| **ISP (Interface Segregation)** | The interface exposes only the 5 methods operations actually need |
| **Protected Variations** | Operations are shielded from changes to `LuceneIndexManager` internals |

### New Interface: `IndexOperationContext`

```java
public interface IndexOperationContext {
    Analyzer getAnalyzer();
    Directory getIndexDirectory();
    ReadWriteLock getReadWriteLock();
    void resetSharedReader();
    IndexReader getSharedIndexReader();
}
```

These are the **only 5 methods** that operation classes call on the manager.

### Files Changed (11 files)

| # | File | Change Summary |
|---|------|---------------|
| 1 | **IndexOperationContext.java** (NEW) | New interface — 5 methods |
| 2 | **LuceneIndexManager.java** | `implements IndexOperationContext`, `getAnalyzer()` static→instance |
| 3 | **IndexOperation.java** | Field `LuceneIndexManager` → `IndexOperationContext`, constructor param updated |
| 4 | **ReadFromIndexOperation.java** | Constructor param `LuceneIndexManager` → `IndexOperationContext` |
| 5 | **WriteToIndexOperation.java** | Constructor param `LuceneIndexManager` → `IndexOperationContext` |
| 6 | **IndexUtil.java** | `getTerm()` takes `Analyzer` parameter, removes `LuceneIndexManager` dependency |
| 7 | **SearchOperation.java** | Constructor takes `IndexOperationContext`, removes cast, updates calls |
| 8 | **AddEntryOperation.java** | Constructor param `LuceneIndexManager` → `IndexOperationContext` |
| 9 | **ReIndexEntryOperation.java** | Constructor param `LuceneIndexManager` → `IndexOperationContext` |
| 10 | **RemoveEntryOperation.java** | Constructor param `LuceneIndexManager` → `IndexOperationContext` |
| 11 | **RebuildWebsiteIndexOperation.java** | Constructor param + `IndexUtil.getTerm()` calls updated |
| 12 | **RemoveWebsiteIndexOperation.java** | Constructor param + `IndexUtil.getTerm()` call updated |

### Files NOT Changed (verified safe)

- **FieldConstants.java** — Pure constants, zero dependencies
- **IndexManager.java** — Public API interface, unchanged
- **JPAWebloggerModule.java** — Guice binding `IndexManager.class → LuceneIndexManager.class`, unchanged
- **All test files** — Use only `IndexManager` interface, no concrete lucene classes

### Dependency Before vs After

**BEFORE:**
```
IndexOperation ──depends on──→ LuceneIndexManager (concrete class)
IndexUtil ──depends on──→ LuceneIndexManager (static call)
SearchOperation ──depends on──→ LuceneIndexManager (cast)
Read/WriteToIndexOperation ──depends on──→ LuceneIndexManager (constructor)
All concrete operations ──depends on──→ LuceneIndexManager (constructor)
```

**AFTER:**
```
IndexOperation ──depends on──→ IndexOperationContext (interface)
IndexUtil ──depends on──→ nothing (Analyzer passed as parameter)
SearchOperation ──depends on──→ IndexOperationContext (interface)
Read/WriteToIndexOperation ──depends on──→ IndexOperationContext (interface)
All concrete operations ──depends on──→ IndexOperationContext (interface)
LuceneIndexManager ──implements──→ IndexOperationContext (interface)
```

### Step-by-Step Implementation Order

1. Create `IndexOperationContext.java` (no compile dependency on anything else)
2. Update `LuceneIndexManager.java` (implements new interface, getAnalyzer static→instance)
3. Update `IndexOperation.java` (base class field + constructor + beginWriting)
4. Update `ReadFromIndexOperation.java` (constructor param)
5. Update `WriteToIndexOperation.java` (constructor param)
6. Update `IndexUtil.java` (add Analyzer parameter, remove LuceneIndexManager import)
7. Update `SearchOperation.java` (constructor, remove cast, update getAnalyzer + getTerm calls)
8. Update `AddEntryOperation.java` (constructor param)
9. Update `ReIndexEntryOperation.java` (constructor param)
10. Update `RemoveEntryOperation.java` (constructor param)
11. Update `RebuildWebsiteIndexOperation.java` (constructor param + getTerm calls)
12. Update `RemoveWebsiteIndexOperation.java` (constructor param + getTerm call)

### Verification

```bash
mvn compile -pl app          # Must pass with zero errors
mvn test -pl app             # Must pass — no functionality change
```

### Key Design Decisions

1. **Interface over abstract class** — `IndexOperationContext` defines a capability contract, not shared state
2. **Analyzer as method parameter in IndexUtil** — `IndexUtil` is a stateless utility with static methods; parameter injection preserves its Pure Fabrication nature
3. **Static→instance for getAnalyzer()** — Required because interface methods cannot be static; private static helpers remain unchanged
4. **No changes to external API** — `IndexManager` interface and Guice binding are untouched; zero impact outside the package
