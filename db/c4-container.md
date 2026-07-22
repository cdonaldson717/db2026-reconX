# ReconX – C4 Level 2 (Container)

```mermaid
C4Container
title ReconX - Container Diagram

Person(trader, "Trader", "Uses ReconX to monitor and investigate reconciliations.")

System_Ext(oms, "Order Management System", "Publishes trade events.")
System_Ext(sso, "Enterprise SSO", "Authenticates users via OIDC.")

System_Boundary(reconxBoundary, "ReconX") {

    Container(spa, "React SPA", "React", "Browser-based user interface")

    Container(api, "API Service", "Spring Boot / REST", "Handles client requests, authentication, and business APIs")

    Container(engine, "Reconciliation Engine", "Java Service", "Processes trade events and performs reconciliation")

    ContainerDb(postgres, "PostgreSQL", "PostgreSQL", "Stores users, trades, reconciliation results, and audit data")

    ContainerQueue(kafka, "Kafka", "Apache Kafka", "Streams trade events between services")

    Container(prometheus, "Prometheus", "Prometheus", "Collects application metrics")

    Container(grafana, "Grafana", "Grafana", "Visualizes operational dashboards")
}

Rel(trader, spa, "Uses application", "HTTPS")

Rel(spa, api, "Requests data and submits actions", "HTTPS / JSON")
Rel(api, postgres, "Reads and writes application data", "JDBC")
Rel(api, kafka, "Publishes trade events", "Kafka")
Rel(engine, kafka, "Consumes trade events", "Kafka")
Rel(engine, postgres, "Stores reconciliation results", "JDBC")

Rel(oms, kafka, "Publishes trade events", "Kafka")
Rel(api, sso, "Authenticates users", "OIDC / HTTPS")

Rel(api, prometheus, "Exposes metrics", "HTTP /metrics")
Rel(engine, prometheus, "Exposes metrics", "HTTP /metrics")
Rel(grafana, prometheus, "Queries metrics for dashboards", "PromQL / HTTP")
```