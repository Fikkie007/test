# Riverside Council permit build test

This repository contains the requirements analysis and design artifacts for the staff-facing permit register slice.

## Folder structure

```text
BRIEF.md
AGENTS.md
ASSUMPTIONS.md
requirements/
  stories.md
  clarifications.md
docs/
  business-flowchart.md
  technical-flowchart.md
  functional-document.md
  technical-document.md
diagrams/
  business-flowchart.json
  business-flowchart.html
  technical-flowchart.json
  technical-flowchart.html
  evidence/
    browser validation receipts and screenshots
backend/
  Spring Boot REST API
frontend/
  React staff portal
requirements-pipeline/
  requirements-to-artifacts pipeline and RC-4 generated output
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
docs/functional-document.md
         |
         v
docs/technical-document.md
```

Part 2 implementation:

```text
frontend/  <->  backend/
```

The JSON and HTML files in `diagrams/` are the editable and rendered versions of the two flowcharts. The `evidence/` folder contains the automated browser validation output.

## Run Part 2

Start the API from `backend/`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-26.0.2'
.\mvnw.cmd spring-boot:run
```

Start the portal from `frontend/` in a second terminal:

```powershell
npm install
npm run dev
```

Open `http://localhost:5173`.
