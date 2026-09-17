# Riverside Council permit build test

This repository contains the requirements analysis and design artifacts for the staff-facing permit register slice.

## Folder structure

```text
BRIEF.md
requirements/
  stories.md
  clarifications.md
docs/
  business-flowchart.md
  technical-flowchart.md
  technical-document.md
diagrams/
  business-flowchart.json
  business-flowchart.html
  technical-flowchart.json
  technical-flowchart.html
  evidence/
    browser validation receipts and screenshots
```

## Artifact chain

```text
requirements/stories.md
requirements/clarifications.md
        |
        v
docs/business-flowchart.md
        |
        v
docs/technical-flowchart.md
        |
        v
docs/technical-document.md
```

The JSON and HTML files in `diagrams/` are the editable and rendered versions of the two flowcharts. The `evidence/` folder contains the automated browser validation output.

## Remaining submission work

- `ASSUMPTIONS.md`
- Functional document
- React and Spring Boot implementation
- Agent pipeline and RC-4 output
- Validation check for invented or dropped requirements
- `DECISIONS.md`
- AI conversation export
