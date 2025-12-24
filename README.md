# PI-System (Portfolio Intelligence System)

PI-System is a **personal finance intelligence platform** designed to help individuals understand, track, and improve their financial health across **investments, savings, loans, insurance, and net worth**.

The goal of PI-System is **not just tracking**, but **decision-making** — turning raw financial data into **clear, actionable insights**.

---

## 🚀 Vision

Most finance apps show numbers.

**PI-System explains what those numbers mean and what to do next.**

It acts as a **personal financial assistant** that:
- Aggregates financial data
- Analyzes risk and diversification
- Highlights problems early
- Suggests corrective actions
- Evolves with the user over time

---

## 🧩 Core Principles

- **Explainability over hype** – every score and insight is transparent  
- **Rule-based first** – deterministic, testable logic before AI  
- **User trust first** – no misleading “all good” signals  
- **Action-oriented** – every insight points to a decision  
- **Modular architecture** – easy to extend, isolate, and scale  

---

## 🏗️ Current Architecture

PI-System is designed as a **modular backend system**, with scope to expand into microservices.

### Core Services
- **PI-System Core** – Financial engine & APIs
- **Automation Test Suite** – API automation & regression testing
- **LLM PI-System (Planned)** – Future intelligence layer

---

## 📦 Implemented Modules (Current Status)

### ✅ User & Authentication
- User registration & login
- Password hashing (BCrypt)
- JWT-based authentication
- Refresh token & logout (blacklisting)
- Forgot password flow

---

### ✅ Portfolio Module (MVP Complete)
- Stock holdings management
- Portfolio valuation
- Sector allocation analysis
- Market-cap distribution
- Diversification scoring
- Explainable scoring model
- Priority-based risk insights
- Next Best Action generation
- Data freshness tracking

---

### ✅ Net Worth Engine
- Aggregates:
  - Investments
  - Savings
  - Loans
  - Insurance cover
- Computes:
  - Total assets
  - Total liabilities
  - Net worth
- Integrated with portfolio intelligence

---

### ✅ External Services Layer
- Centralized third-party API configuration
- Dynamic credentials support
- Request/response auditing
- Failure-tolerant design
- Fallback handling

---

### ✅ Savings Accounts (Manual + Foundation)
- Savings balance tracking
- FD / RD basic support
- Ready for Account Aggregator ingestion

---

### ✅ Loans (In Progress)
- Outstanding loan tracking
- Integrated into net worth
- Risk context planned (liquidity vs debt)

---

### ✅ Lending Money Tracker
- Track money lent to friends/family
- Due date reminders (in-app)
- Outstanding balance calculation
- Integrated into net worth

---

### ✅ Observability
- Health status endpoint
- Request audit logs
- Third-party request/response logs

---

## 🧠 Portfolio Intelligence Engine (Key Feature)

The Portfolio Engine produces **decision-quality insights**, not just metrics.

### Example Capabilities
- Detects single-stock over-concentration
- Flags sector imbalance
- Highlights small-cap risk
- Identifies sharp drawdowns
- Generates prioritized risk insights
- Produces a single **Next Best Action**

### Example Output (Simplified)
```json
{
  "score": 30,
  "assessment": "POORLY_DIVERSIFIED",
  "nextBestAction": {
    "title": "Reduce risk from over-concentrated stocks",
    "urgency": "HIGH"
  }
}
