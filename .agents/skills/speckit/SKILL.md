---
name: speckit
description: GitHub Spec Kit (Spec-Driven Development) workflow for AI coding agents. Enforces structured, gated phases: Constitution, Specify, Clarify, Plan, Tasks, and Implement.
---

# SpecKit — Spec-Driven Development (GitHub Spec Kit)

SpecKit enforces structured, gated **Spec-Driven Development (SDD)** with AI coding agents to eliminate "vibe coding" drift, ensure architectural consistency, and guarantee verifiable task execution.

---

## 🧭 The SpecKit Gated Workflow

```
CONSTITUTION ──→ SPECIFY ──→ CLARIFY ──→ PLAN ──→ TASKS ──→ IMPLEMENT
     │              │           │          │        │           │
     ▼              ▼           ▼          ▼        ▼           ▼
  Project        Define      Resolve    Technical Dependency-  Execute &
 Guidelines    What & Why  Ambiguities  Strategy   Ordered      Verify
```

---

## 🛠️ SpecKit Commands & Phases

### 1. `/speckit.constitution`
**Objective:** Define and enforce the project's non-negotiable architectural principles, tech stack, and constraints.
* Focus:
  - Architecture standards (Clean Architecture, MVVM)
  - Security rules & secret protection
  - Testing requirements (100% pass unit tests before commit)

### 2. `/speckit.specify`
**Objective:** Capture user requirements, feature scope, and acceptance criteria.
* Focus:
  - User Stories & Business Rules (BR)
  - Functional / Non-functional specs
  - Definition of Done (DoD)

### 3. `/speckit.clarify`
**Objective:** Surface hidden assumptions and resolve ambiguity before technical planning.
* Focus:
  - Surface assumptions immediately
  - Clarify edge cases and data models

### 4. `/speckit.plan`
**Objective:** Produce a comprehensive technical strategy document.
* Focus:
  - Module boundaries & Data contracts
  - File changes breakdown (New / Modify / Delete)
  - Verification & Test plan

### 5. `/speckit.tasks`
**Objective:** Convert the plan into an actionable, dependency-ordered task list.
* Focus:
  - Granular, independent task items
  - Verification gate per task

### 6. `/speckit.implement`
**Objective:** Execute tasks sequentially with human review and automated test gates between steps.
* Focus:
  - Incremental execution
  - Automated verification (`testDebugUnitTest`)
  - Log synchronization (`HANDOVER_LOG.md`, `CHANGELOG.md`)
