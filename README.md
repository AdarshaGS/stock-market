# 💰 MoneyPulse — Stock & Personal Finance Intelligence System

MoneyPulse is a Spring Boot–based financial intelligence platform designed to help users track their investments, analyze diversification, compute net worth, and make data-driven financial decisions.  

It integrates with external stock market APIs, performs sector-based diversification scoring, and is structured to support future modules like budgeting, account aggregators, alerts, and AI-driven insights.

---

## 🌟 Key Capabilities

✔ Stock price retrieval from external APIs  
✔ Portfolio tracking & profit/loss analysis  
✔ Sector-based diversification scoring  
✔ Token-based authentication using JWT  
✔ User password encryption using BCrypt  
✔ Caching logic for unavailable external providers  
✔ Expandable architecture for future finance modules  

---

## 🧭 Product Workflow Diagram

> The full system workflow diagram is available here:  
🔗 **https://www.mermaidchart.com/app/projects/ef11d05b-42d4-47ba-a46f-4b0a68ac58f3/diagrams/bf2087c9-56e3-4bd7-b604-812953cd9be5/version/v0.1/edit**

### Simplified System Flow (Mermaid)

```mermaid
flowchart TD

User[[User]] -->|Register/Login| Auth[JWT Auth Service]
Auth -->|Valid Credentials| JWT[Generate JWT Token]
JWT -->|Bearer Token| API[Protected API Layer]

API -->|Portfolio Requests| PortfolioService
API -->|Stock Lookup| StockService
API -->|Net Worth Requests| NetWorthService
API -->|User Asset Insert| AssetService
API -->|Future: Alerts & Analytics| InsightsService

StockService -->|Check DB| StockDB[(Stocks DB)]
StockDB -->|Found| ReturnStock
StockDB -->|Not Found| ThirdPartyCheck{External Stock API?}

ThirdPartyCheck -->|Yes| ExternalAPI[External Market API]
ExternalAPI --> Parse[Parse & Store]
Parse --> ReturnStock

ThirdPartyCheck -->|No| Cached[Use Last Known Price]

PortfolioService --> PortfolioDB[(Portfolio Holdings DB)]
PortfolioDB --> Calculate[Calculate Current Value]

Calculate --> Diversify[Sector Mapping & Score]

Diversify --> SectorDB[(Sectors DB)]
SectorDB --> Diversify

Diversify --> Insight[Generate Recommendations]
Insight --> ReturnSummary

NetWorthService --> UserAssets[(Assets DB)]
NetWorthService --> UserLiabilities[(Liabilities DB)]
UserAssets --> ComputeNW[Compute Net Worth]
UserLiabilities --> ComputeNW
ComputeNW --> ReturnNW[Return Net Worth Summary]