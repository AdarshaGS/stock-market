# Constraints

- The system must remain read-only in Phase 1
- No financial decisions or recommendations are made
- All calculations must be explainable and auditable
- External data sources must be reliable and documented
- The system should work for a solo developer initially
- Infrastructure cost should remain minimal in early phases

## AI & Automation Safety Boundaries
- **Air Gap Enforcement**: AI-driven services (e.g., `PortfolioInsightService`) MUST NOT depend on or invoke any `Write` or `Mutation` services. They must remain strictly "Read-Process-Output".
- **Deterministic Input**: AI context must be built purely from `DTOs` provided by `ReadPlatformService` layers.
- **Auditability**: All calls to AI components must be logged via `RequestAuditService`, capturing the exact input prompt and raw output.