# futawa 二輪 — Maturity

**Stage: R0** (scaffold) — ADR-2605261330. Small-displacement motorcycle manufacturing
(≤250cc / ≤15kW / ≤200kg). ABS-mandatory, build-time anti-surveillance, right-to-repair
forward-publishing. Lifecycle surveillance/repair loop closed with hodoki (EOL).

| Dimension | State |
|---|---|
| Lexicons | ✅ 8 canonical Datomic-native definitions in `data/lex/*.edn`; imported JSON wire schemas are preserved in `wire/lex/` and converted to `data/lex/*.wire.edn` |
| Cells | 🟡 9 R0 declarations in `data/cells/`; social state-machine CLJC in `src/futawa/cells/` |
| Manifest | ✅ canonical `manifest.edn`; JSON-LD mirror in `wire/manifest.jsonld` |
| Tests | ✅ 38 tests / 178 assertions via `clojure -M -m futawa.test-runner` |
| Methods | 🟡 agent, social, and MCP ingestion CLJC; offline build engine remains R1 |
| Policy audit | ✅ `bb scripts/audit.clj` parses EDN, enforces the wire boundary, and rejects deprecated Python/Go/TinyGo/shell artifacts |

## Substrate-native status

EDN is the authoritative representation. Canonical actor data lives under `data/`, while
external JSON and JSON-LD formats are isolated under `wire/`. Runtime and tests are Clojure/CLJC.
The former Python and deployment-shell implementations were pruned, not ported. No Go/TinyGo
implementation was present in the migrated source.

The constitutional suite pins manifest G1–G14, anti-surveillance, ABS, KPI, right-to-repair,
and lifecycle invariants against the canonical EDN data.

## R0 → R1 gate

ADR-2605261330 Council Lv6+ ratify + motorcycle-engineering SME + benchtop assembly PoC;
cell execution stays R0-gated until then.
