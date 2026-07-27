# ReconX Architecture Decision Records

ADRs in this directory use the Michael Nygard structure and record one
decision per file. The decision is kept even if it is later superseded; a new
ADR should link back to the old one instead of rewriting history.

## AI drafting prompt

The following prompt template was used to produce an initial draft. Each draft
was then checked against the implemented schema and edited by the team.

```text
Act as an architecture reviewer and draft a concise ADR in Michael Nygard
format: title, status, context, decision, and consequences.

System: ReconX, a trade-reconciliation platform using PostgreSQL 16,
Spring Boot 3, Kafka, and React.
Workload: about 50,000 trades per day, retained for five years, with most
analyst and reconciliation queries restricted to a date range.
Decision: <describe one decision only>
Alternatives evaluated: <name at least two>
Forces and constraints: <list operational and technical constraints>

Use Markdown, set the status to Accepted, identify both benefits and costs,
and keep the response below 300 words. Do not invent capabilities that are
not present in the supplied context.
```

The concrete values supplied for each decision are recorded in
[`prompts/day1-prompts.md`](prompts/day1-prompts.md).
