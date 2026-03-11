# Project 1 - Bonus: Benchmarking of Refactored Code Across LLMs and Settings
**(CS6.401 – Software Engineering)**

**TEAM 33**
**Members**
- Aryan Mishra, 2024121001
- Aditya Nair, 2023111029
- Varun Patil, 2025204013
- Hardik Chadha, 2023111031
- Vijay Aravynthan, 2023111007

## Bonus Task: Benchmarking

We compared two LLMs (Claude 3.5 Sonnet and Gemini 1.5 Pro) in both **Non-Agentic** (single-shot prompt with file context) and **Agentic** (autonomous tool use) settings. The goal was to benchmark their ability to identify and refactor the **Hub-like God Object** smell in `WeblogEntry.java` (Issue #4), which we manually refactored by extracting classes like `WeblogEntryPresenter`, `TagManifest`, and `PluginConfiguration`.

### Setup
- **File Addressed:** `WeblogEntry.java`
- **Models Used:** Claude 3.5 Sonnet (Thinking mode), Gemini 3 Pro (Preview).
- **Settings:**
    - **Non-Agentic:** Provided `WeblogEntry.java` and `designCodeSmells.csv` in the prompt. Asked for a plan and refactor.
    - **Agentic:** Allowed the models to explore the codebase using tools to address the smell in `WeblogEntry.java`.

### 1. Smell Identification & Focus

| Setting | Model | Identified "Hub-Like" / God Object? | Decided to Fix it? | Focus Area |
| :--- | :--- | :--- | :--- | :--- |
| **Manual** | **Human Team** | **Yes** | **Yes** | **Architectural Decoupling:** Extracted `WeblogEntryPresenter`, `TagManifest`, `PluginConfiguration`. Removed `WebloggerFactory` dependency. |
| Non-Agentic | Claude Sonnet | Yes (Explicitly noted) | **No** | **Internal Encapsulation:** Noted Hub-like/Cyclic smells but stated they require changes across many files. Focused on `serialVersionUID`, unmodifiable collections, and a *private inner class* `EntryTagCollection`. |
| Non-Agentic | Gemini 3 Pro | Yes (Implicitly "High Complexity") | **No** | **Local Logic Simplification:** Focused on simplifying conditional logic in `displayContent`, `render`, and extracting private helpers. Kept the `WebloggerFactory` dependency but wrapped it in a helper. |
| Agentic | Claude Sonnet | Yes | **Partial** | **Analysis Stage Only:** The agent spent significant time analyzing the repo (reading 18+ files). It identified the smell but stopped short of a full architectural refactor in the provided output, leaning towards internal cleanup. |
| Agentic | Gemini 3 Pro | Yes | **Partial** | **Refactor Attempts:** Attempted to address "Deficient Encapsulation" primarily. Like its non-agentic counterpart, it focused on localized fixes rather than extracting new public classes. |

**Observation:**
In the **Non-Agentic** setting, both models correctly identified the high complexity or specific smells provided in the CSV. However, they both **refused or failed to address the Hub-like God Object smell**.
- **Claude Sonnet** explicitly stated that Hub-like and Cyclic smells are "architectural and require changes across many files" and thus left them "structurally intact," focusing instead on what could be done within the single file (Encapsulation).
- **Gemini 3 Pro** recognized the "High Complexity/Responsibility" but focused on method-level refactoring (extracting private helpers) rather than extracting class-level responsibilities.

In the **Agentic** setting, despite having the *ability* to create new files and explore dependencies, the models still trended towards conservative, single-file improvements (Deficient Encapsulation) rather than the radical surgery we performed manually (extracting `WeblogEntryPresenter`, etc.). This suggests that without explicit instructions to "extract classes," LLMs prioritize "do no harm" and local coherence over architectural restructuring.

### 2. Quality of Refactoring & Compliability

| Metric | Manual Refactoring | Non-Agentic LLMs | Agentic LLMs |
| :--- | :--- | :--- | :--- |
| **Architectural Change** | **High:** Created 3 new classes, removed Service Locator pattern. | **None:** All changes remained within `WeblogEntry.java`. | **Low/None:** Mostly focused on internal fields and helper methods. |
| **De-coupling** | **High:** `WeblogEntry` no longer depends on `WebloggerFactory` or rendering logic. | **Low:** logic extracted to private helpers still keeps the dependencies inside the class. | **Low:** Dependencies remained largely strictly coupled. |
| **Compliability** | **High:** Validated with tests. | **High:** The code provided was syntactically correct and defensive (e.g., using `Collections.unmodifiableSet`). | **Variable:** Agentic text outputs were valid, but determining if their multi-step plan would compile requires execution. Analysis looked sound. |

### Conclusion

1.  **Identification vs. Action:** LLMs are excellent at *identifying* the same smells we found (referencing the CSV and code complexity). However, they differ drastically in their *strategy* for fixing them.
    -   **Humans** typically prioritize **Extract Class** to solve God Objects.
    -   **LLMs** (especially in single-file contexts) prioritize **Extract Method** and **Encapsulation** logic to solve the same problem.
2.  **Context Limitation:** The Non-Agentic LLMs correctly deduced that they could not fix "Hub-like Modularization" because they couldn't see or edit the other files involved. This demonstrates a strong "awareness of limitations" in Claude Sonnet.
3.  **Agentic Behavior:** Even with agentic capabilities, the models did not spontaneously decide to create new files (`WeblogEntryPresenter.java`, etc.) to solve the God Object smell. They treated the task as "fix this file" rather than "fix this system design," likely due to the prompt focusing on providing "WeblogEntry.java" as the target.

**Verdict:** For architectural refactoring (refactoring *across* files), **Manual Human Engineering** (or highly specific prompt engineering directing the AI to extract classes) is still superior to autonomous AI refactoring. AI is best suited for **local** refactoring (cleaning up methods, encapsulation, and loops) within the identified God Object.
