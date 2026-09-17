from __future__ import annotations

import argparse
import json
import tempfile
from dataclasses import asdict, dataclass
from pathlib import Path


@dataclass(frozen=True)
class Requirement:
    id: str
    source: str
    statement: str
    evidence: tuple[str, ...]


REQUIREMENTS = (
    Requirement(
        "RC-4.1",
        "stories.md:90",
        "An officer can withdraw a permit from the permit view.",
        ("withdraw a permit", "POST /api/permits/{permitNumber}/withdrawal"),
    ),
    Requirement(
        "RC-4.2",
        "stories.md:91",
        "A withdrawal reason is required and is at most 500 characters.",
        ("reason", "500 characters"),
    ),
    Requirement(
        "RC-4.3",
        "stories.md:92",
        "Withdrawal changes status to WITHDRAWN and records the reason in history.",
        ("WITHDRAWN", "history", "history event"),
    ),
    Requirement(
        "RC-4.4",
        "stories.md:93",
        "Withdrawal does not refund anything.",
        ("does not refund",),
    ),
    Requirement(
        "RC-4.C1",
        "clarifications.md:95-98",
        "A permit may be withdrawn only before its start date; after start it is cancellation.",
        ("startDate > today", "cancellation"),
    ),
)

FORBIDDEN_INVENTIONS = ("actor", "refundAmount", "paymentId", "approvalCode", "withdrawalDate")


def intake(root: Path) -> dict:
    stories = (root / "requirements" / "stories.md").read_text(encoding="utf-8")
    clarifications = (root / "requirements" / "clarifications.md").read_text(encoding="utf-8")
    required_sources = {
        "stories": ("withdraw a permit", "withdrawal reason", "WITHDRAWN", "does not refund"),
        "clarifications": ("Only permits that haven't started yet", "cancellation"),
    }
    missing = [
        f"{source}:{marker}"
        for source, markers in required_sources.items()
        for marker in markers
        if marker not in (stories if source == "stories" else clarifications)
    ]
    if missing:
        raise ValueError(f"Source intake failed; missing evidence: {', '.join(missing)}")
    return {"requirements": [asdict(requirement) for requirement in REQUIREMENTS]}


def generate(root: Path, output: Path) -> None:
    manifest = intake(root)
    output.mkdir(parents=True, exist_ok=True)
    files = {
        "business-flowchart.md": BUSINESS_FLOWCHART,
        "technical-flowchart.md": TECHNICAL_FLOWCHART,
        "functional-document.md": FUNCTIONAL_DOCUMENT,
        "technical-document.md": TECHNICAL_DOCUMENT,
        "WithdrawalPolicy.java": WITHDRAWAL_POLICY,
    }
    for name, content in files.items():
        (output / name).write_text(content.strip() + "\n", encoding="utf-8")
    (output / "traceability.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")


def validate(output: Path) -> list[str]:
    manifest = json.loads((output / "traceability.json").read_text(encoding="utf-8"))
    files = [path for path in output.iterdir() if path.is_file()]
    artifacts = [path for path in files if path.name != "traceability.json"]
    text = "\n".join(path.read_text(encoding="utf-8") for path in artifacts)
    code = "\n".join(path.read_text(encoding="utf-8") for path in files if path.suffix == ".java")
    errors = []
    requirement_ids = {item["id"] for item in manifest.get("requirements", [])}
    expected_ids = {requirement.id for requirement in REQUIREMENTS}
    if requirement_ids != expected_ids:
        errors.append(f"traceability IDs differ: expected {sorted(expected_ids)}, got {sorted(requirement_ids)}")
    for requirement in REQUIREMENTS:
        entry = next((item for item in manifest["requirements"] if item["id"] == requirement.id), None)
        if not entry or not entry.get("source") or not entry.get("statement"):
            errors.append(f"{requirement.id} has no source or statement")
        for evidence in requirement.evidence:
            if evidence not in text:
                errors.append(f"{requirement.id} dropped evidence: {evidence}")
    for invention in FORBIDDEN_INVENTIONS:
        if invention in code:
            errors.append(f"unsupported invention detected: {invention}")
    if "POST /api/permits/{permitNumber}/withdrawal" not in text:
        errors.append("withdrawal endpoint is missing")
    return errors


def self_test(root: Path) -> None:
    with tempfile.TemporaryDirectory() as directory:
        output = Path(directory) / "rc4"
        generate(root, output)
        assert not validate(output), "valid output should pass"

        for artifact in output.iterdir():
            if artifact.name != "traceability.json":
                artifact.write_text(artifact.read_text(encoding="utf-8").replace("500 characters", "a reason"), encoding="utf-8")
        assert any("500 characters" in error for error in validate(output)), "dropped requirement was not detected"

        generate(root, output)
        code = output / "WithdrawalPolicy.java"
        code.write_text(code.read_text(encoding="utf-8") + "\nString actor;\n", encoding="utf-8")
        assert any("actor" in error for error in validate(output)), "invented field was not detected"


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate and validate the RC-4 withdrawal slice.")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--output", type=Path, default=Path(__file__).parent / "generated" / "rc4")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test(args.root)
        print("self-test passed: valid, dropped, and invented cases checked")
        return
    generate(args.root, args.output)
    errors = validate(args.output)
    if errors:
        print("VALIDATION FAILED")
        print("\n".join(f"- {error}" for error in errors))
        raise SystemExit(1)
    print(f"generated and validated {args.output}")


BUSINESS_FLOWCHART = """
# RC-4 Withdrawal: Officer Flow

```text
Open a permit
      |
Choose "Withdraw permit"
      |
Is the event still in the future?
   /       \
 no         yes
 |           |
Explain      Enter a reason
withdrawal   (required, up to 500 characters)
is not       |
available    Confirm withdrawal
                 |
          Permit is withdrawn
          and the reason is recorded
```

Withdrawal releases the permit from this workflow. It does not issue a refund. If the event has
already started, the officer must use the separate cancellation process.
"""


TECHNICAL_FLOWCHART = """
# RC-4 Technical Flow

```text
React permit view
  -> POST /api/permits/{permitNumber}/withdrawal { reason }
  -> WithdrawalController
  -> WithdrawalService
       -> load permit
       -> validate reason and permit.startDate > today
       -> create WITHDRAWN permit state
       -> write withdrawal history
       -> commit both writes
  -> 200 updated permit view
```

Failure paths:

- `404` when the permit does not exist.
- `400` when the reason is blank or longer than 500 characters.
- `409` when the permit has already started.
- No payment or refund operation is called.
"""


FUNCTIONAL_DOCUMENT = """
# Functional Document: RC-4 Withdrawal

## Purpose

Permits Officers can withdraw a permit when the holder asks them to, provided the permitted event has
not started. The withdrawal reason is retained in the permit history. This workflow does not refund
money.

## Officer journey

1. Open a permit from the register.
2. Choose **Withdraw permit**.
3. Enter a mandatory reason of no more than 500 characters.
4. If the event has started, the system rejects the action and directs the officer to cancellation.
5. Review and confirm the withdrawal.
6. The permit shows as withdrawn and the reason appears in its history.

## Acceptance criteria coverage

| ID | Behavior | Evidence |
|---|---|---|
| RC-4.1 | Withdrawal starts from the permit view. | `POST /api/permits/{permitNumber}/withdrawal` |
| RC-4.2 | Reason is required and capped at 500 characters. | `WithdrawalRequest.reason` |
| RC-4.3 | Status becomes `WITHDRAWN`; reason is recorded in history. | `WithdrawalPolicy`, withdrawal history event |
| RC-4.4 | No refund is initiated. | No payment/refund endpoint or field |
| RC-4.C1 | Only permits not yet started can be withdrawn. | `startDate > today`; otherwise cancellation |
"""


TECHNICAL_DOCUMENT = """
# Technical Document: RC-4 Withdrawal

## Endpoint

`POST /api/permits/{permitNumber}/withdrawal`

Request:

```json
{ "reason": "Holder cancelled the booking before the event started." }
```

The server loads the current permit. It ignores any client-supplied status, history, date, or refund
value. It validates that the reason is not blank and has at most 500 characters, then requires
`permit.startDate > today`. A started permit returns `409` because it follows the separate cancellation
process.

The write transaction changes the permit status to `WITHDRAWN` and adds one history event with event
type `WITHDRAWN`, a description containing the supplied reason, and the event timestamp. Both writes
must commit together. The workflow has no payment or refund integration.

## Responses

- `200 OK`: updated permit view.
- `400 Bad Request`: missing, blank, or over-limit reason.
- `404 Not Found`: permit does not exist.
- `409 Conflict`: permit start date is today or earlier.

## Explicit non-goals

There is no refund field, payment call, actor field, cancellation implementation, or new permit-number
generator in this slice.
"""


WITHDRAWAL_POLICY = """
package generated.rc4;

import java.time.LocalDate;

/** Pure RC-4 validation; repository writes belong in the application service transaction. */
public final class WithdrawalPolicy {
    private WithdrawalPolicy() { }

    public static void validate(String reason, LocalDate startDate, LocalDate today) {
        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new IllegalArgumentException("Withdrawal reason is required and must be 500 characters or fewer.");
        }
        if (!startDate.isAfter(today)) {
            throw new IllegalStateException("A permit that has started must use cancellation instead.");
        }
    }
}
"""


if __name__ == "__main__":
    main()
