# Apache Roller Enhanced 🚀

An enterprise-grade blogging platform extended with modern AI capabilities, automated pipelines, and advanced social features. This project showcases software engineering best practices including architectural modeling, refactoring, and pattern-based extensions.

## 📁 Project Overview

This repository contains the work completed across two major project phases for the Software Engineering course at IIIT Hyderabad.

### 🔬 Project 1: Reverse Engineering & Refactoring
Focused on analyzing the legacy architecture of Apache Roller and improving its maintainability.
- **Architectural Modeling**: Reverse engineered the Weblog, User Management, and Search subsystems into comprehensive UML models.
- **Design Smell Analysis**: Identified 7+ critical design smells using SonarQube and Designite Java.
- **Quantitative Metrics**: Analyzed code quality using Chidamber and Kemerer (CK) metrics to guide refactoring decisions.
- **Refactoring**: Applied class-level refactorings to resolve architectural violations while preserving 100% test coverage.
- **AI Automation**: Implemented an agentic refactoring pipeline using LLM APIs to identify and suggest design improvements automatically.

### 🌟 Project 2: Advanced Feature Extensions
Focused on extending the platform with modular, AI-integrated features using Design Patterns.
- **Social Engagement**: Built a High-Performance Starring system with efficient SQL-level aggregation for trending content.
- **Processing Pipeline**: Implemented a **Chain of Responsibility** based pipeline for automated blog content filtering, summarization (Gemini), and tagging.
- **Admin Insights**: Developed a Site Summary Dashboard using the **Builder Pattern** for flexible reporting views.
- **AI Translation**: Integrated Sarvam AI and Google Translate with an intelligent **Differential Caching** layer to minimize API costs.
- **Community Pulse**: Created a dashboard for comment sentiment and theme breakdown using a hybrid approach of classical NLP and LLMs.
- **Weblog Q&A Chatbot**: Developed a RAG-based (Retrieval-Augmented Generation) assistant for natural language querying of blog archives.

---

## 🛠️ Technical Stack
- **Languages**: Java, JSP, JavaScript
- **Frameworks**: Struts 2, JPA (OpenJPA), Google Guice (DI)
- **AI Stack**: Gemini 2.0 Flash, Sarvam AI, Retrieval-Augmented Generation (RAG)
- **Patterns**: Chain of Responsibility, Builder, Repository, Static Factory, Strategy, Singleton.
- **Tools**: Docker, Maven, Lucene, MailerSend.

---

## 📂 Repository Structure
- `app/`: Core Java application source code.
- `docs/reports/`: Full PDF reports for [Project 1](docs/reports/project1_33.pdf) and [Project 2](docs/reports/project2_33.pdf).
- `docs/diagrams/`: UML diagrams and architectural visualizations.
- `docs/guides/`: Installation and configuration manuals.
- `db-utils/`: Database schema and migration scripts.

---

## 🚀 Getting Started

1. **Build**: `mvn clean install -DskipTests`
2. **Run**: `mvn jetty:run -pl app`
3. **Access**: `http://localhost:8080/roller`

---

## 📝 Academic Context
Developed for **CS6.401: Software Engineering** at IIIT Hyderabad.
**Team 33**: [Project 1 Documentation](docs/reports/project1_33.pdf) | [Project 2 Documentation](docs/reports/project2_33.pdf)
