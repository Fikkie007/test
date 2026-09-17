# Business flowchart: search, view and renew a permit

Audience: Riverside Council Permits Officers

```mermaid
flowchart TD
    A([Start]) --> B[Open the permit register]
    B --> C{Need to narrow the list?}

    C -- No --> D[Show all permits]
    C -- Yes --> E[Enter any combination of search details:
    permit number, holder, hall, purpose,
    status, or date range]
    E --> F[Search the register]
    F --> G{Any permits match?}
    G -- No --> H[Show an empty result message]
    G -- Yes --> I[Show matching permits,
    newest permits first]
    D --> I
    H --> J{Try another search?}
    J -- Yes --> E
    J -- No --> Z([Finish])

    I --> K[Select a permit]
    K --> L[View the permit details
    and any renewal history]
    L --> M{Return to the results?}
    M -- Yes --> I
    M -- No --> N{Start a renewal?}
    N -- No --> Z
    N -- Yes --> O[Enter a new end date]
    O --> P{Is the new date after
    the current end date?}
    P -- No --> Q[Show a message:
    the new date must be later]
    Q --> O
    P -- Yes --> R{Can this permit be renewed?}
    R -- No --> S[Show a message explaining
    why renewal is not available]
    S --> Z
    R -- Yes --> T[Calculate the renewal fee]
    T --> U[Show the fee for confirmation]
    U --> V{Confirm the renewal?}
    V -- No --> Z
    V -- Yes --> W[Save the renewal details]
    W --> X[Update the permit's end date]
    X --> Y{Is the fee greater than zero?}
    Y -- Yes --> AA[Set the permit to awaiting payment]
    Y -- No --> AB[Keep the permit active
    with no payment required]
    AA --> AC[Show the updated permit
    and renewal history]
    AB --> AC
    AC --> Z

    classDef action fill:#e8f0fe,stroke:#315b9d,color:#172b4d;
    classDef decision fill:#fff4ce,stroke:#9a6b00,color:#3d2b00;
    classDef outcome fill:#e6f4ea,stroke:#27733f,color:#153b21;
    class A,Z outcome;
    class B,D,E,F,H,I,K,L,O,Q,S,T,U,W,X,AA,AB,AC action;
    class C,G,J,M,N,P,R,V,Y decision;
```

## Business rules shown in the flow

- All search details are optional.
- Holder name searches can use part of the name and ignore capitalisation.
- Results show the permit number, holder, hall, purpose, status, dates, and fee.
- A renewal date must be later than the current end date.
- Only an active permit, or an expired permit within 90 days of its end date, can be renewed.
- The renewal fee uses the hall's daily rate and is capped at 30 days.
- Council Use renewals have no fee and do not require payment.
- A chargeable renewal changes the permit to awaiting payment after confirmation.
- The renewal is not saved until the officer confirms the displayed fee.
