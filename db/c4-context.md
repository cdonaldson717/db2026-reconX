# ReconX – C4 Level 1 (System Context)

```mermaid
C4Context
title ReconX - System Context

Person(trader, "Trader", "Views positions, reconciliations, and exceptions")
Person(operations, "Operations Analyst", "Investigates and resolves reconciliation breaks")
Person(admin, "Platform Administrator", "Configures, monitors, and supports the platform")
Person(auditor, "Auditor", "Reviews reconciliation results and audit history")

System(reconx, "ReconX", "Reconciliation platform for comparing, monitoring, and reporting financial data")

System_Ext(oms, "OMS", "Order Management System")
System_Ext(sftp, "SFTP Server", "Secure file exchange")
System_Ext(bloomberg, "Bloomberg", "Market and reference data")
System_Ext(email, "Email Service", "Notification delivery")
System_Ext(sso, "Enterprise SSO", "Identity provider")
System_Ext(grafana, "Grafana", "Monitoring and dashboards")

Rel(trader, reconx, "Uses reconciliation dashboards and workflows", "HTTPS")
Rel(operations, reconx, "Investigates and resolves reconciliation exceptions", "HTTPS")
Rel(admin, reconx, "Configures and administers the platform", "HTTPS")
Rel(auditor, reconx, "Reviews audit history and reconciliation reports", "HTTPS")

Rel(reconx, oms, "Imports orders and execution data", "HTTPS / API")
Rel(reconx, sftp, "Imports and exports reconciliation files", "SFTP")
Rel(reconx, bloomberg, "Retrieves market and reference data", "HTTPS / API")
Rel(reconx, email, "Sends alerts and scheduled reports", "SMTP")
Rel(reconx, sso, "Authenticates users", "OIDC")
Rel(reconx, grafana, "Publishes metrics for operational monitoring", "Prometheus / HTTP")
```