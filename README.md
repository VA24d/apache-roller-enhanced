# Apache Roller Enhanced 🚀

An enterprise-grade blogging platform extended with modern AI capabilities, automated pipelines, and advanced social features. This project was developed as part of the Software Engineering curriculum at IIIT Hyderabad.

## 🌟 Key Features

### 1. Social Engagement (User Highlights)
- **Starring System**: Users can "star" both weblogs and individual entries.
- **Dynamic Paging**: Starred content is displayed with efficient pagination and sorted by recency of posts.
- **Trending Metrics**: Real-time "Trending Blogs" and "Trending Posts" leaderboards, optimized using database-level aggregation (`GROUP BY + COUNT`) for maximum performance.

### 2. Transforming Feeds Pipeline
- **Automated Processing**: Every blog post passes through a modular processing pipeline upon saving.
- **Modular Steps**: 
    - **Profanity Filtering**: Automatic censorship of restricted content.
    - **AI Summarization**: Generates concise summaries using Gemini 2.0 Flash.
    - **Sentiment Analysis**: Detects the emotional tone of the content.
    - **Automatic Tagging**: Intelligent metadata generation.
- **Design Pattern**: Implemented using the **Chain of Responsibility** pattern for high extensibility.

### 3. Admin & Community Insights
- **Site Summary Dashboard**: A centralized hub for admins to monitor site-wide metrics (Users, Blogs, Activity). Supports **Minimalist** and **Full** views using the **Builder Pattern**.
- **Community Pulse**: Visualizes comment section activity using classical statistical measures and AI-driven conversation breakdowns.
- **Bug Reporting System**: Integrated issue tracking with automated email notifications (via MailerSend) and modular support for Slack/Teams integrations.

### 4. AI-Powered Enhancements
- **Web Translation**: Real-time translation supporting 5+ languages via Sarvam AI and Google Translate, featuring an intelligent **Differential Caching** layer.
- **Weblog Q&A Chatbot**: A RAG-based (Retrieval-Augmented Generation) assistant that answers natural language questions grounded in a blog's specific history.

---

## 🛠️ Technical Architecture

- **Core**: Java, JSP, Struts2, JPA (Hibernate/OpenJPA)
- **Database**: Apache Derby / PostgreSQL
- **AI/LLM**: Google Gemini API, Sarvam AI
- **Patterns**: Chain of Responsibility, Builder, Repository, Static Factory, Strategy.
- **DevOps**: Docker, Maven, GitHub Actions.

## 📂 Project Structure

- `app/`: Main web application source code.
- `docs/`: Technical reports, UML diagrams (PlantUML), and architecture documentation.
- `task3-pipeline/`: Automated LLM-based refactoring tools.
- `db-utils/`: Database migration and utility scripts.

---

## 🚀 Getting Started

1. **Build**: `mvn clean install -DskipTests`
2. **Run**: `mvn jetty:run -pl app`
3. **Access**: `http://localhost:8080/roller`

---

## 📝 Academic Context
Developed for **CS6.401: Software Engineering** at IIIT Hyderabad.
- **Project 1**: Focused on Reverse Engineering, Design Smell Analysis, and Manual/Automated Refactoring.
- **Project 2**: Focused on Feature Extension, Design Pattern Integration, and AI System Design.
