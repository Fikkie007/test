# Part 4 Agent Pipeline

This is a small, dependency-free requirements-to-artifacts pipeline for RC-4 withdrawal. It is
deliberately evidence-first rather than pretending to make an unauditable model call without
credentials.

## Stages

1. Intake checks that the raw stories and clarifications contain the expected source evidence.
2. Decision records preserve the later clarification that withdrawal is allowed only before the start
   date.
3. Artifact generation writes business, technical, functional, and technical documents plus a code
   policy and traceability manifest.
4. The validation gate checks every requirement ID and evidence token, the approved endpoint, and a
   small allowlist-based set of invented fields.

The pipeline stops rather than deciding when its required source evidence is missing. A future LLM
adapter can replace the renderer, but it must emit the same traceability manifest and pass the same
gate.

## Run

From the repository root:

```bash
python part4/pipeline.py --self-test
python part4/pipeline.py
```

The second command generates and validates `part4/generated/rc4/`.

The self-test proves both failure modes required by the brief:

- removing the 500-character evidence is reported as a dropped requirement;
- adding an `actor` field is reported as an unsupported invention.
