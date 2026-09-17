# Technical flowchart: search, view and renew a permit

Audience: engineers implementing the React staff portal and Spring Boot API

```mermaid
flowchart TD
    A[React: permit register] --> B[GET /api/permits
    filters, page=0, size=10,
    sort=createdAt,desc]
    B --> C[PermitController]
    C --> D[PermitQueryService]
    D --> E[(PermitRepository)]
    E --> F{Matching rows?}
    F -- No --> G[Return 200 with empty page]
    F -- Yes --> H[Return 200 with page of permits]
    G --> I[React shows empty state]
    H --> J[React renders permit grid]
    J --> K[Officer selects permit]
    K --> L[GET /api/permits/{permitNumber}]
    L --> C
    C --> M[PermitViewService]
    M --> N[(PermitRepository + RenewalRepository
    + HistoryRepository)]
    N --> O{Permit exists?}
    O -- No --> P[Return 404]
    O -- Yes --> Q[Return read-only permit
    with renewal history]
    P --> R[React shows not-found message]
    Q --> S[React shows permit view]
    S --> T[Officer enters new end date]
    T --> U[POST /api/permits/{permitNumber}/renewal-quote
    {newEndDate}]
    U --> C
    C --> V[RenewalService: validate and calculate]
    V --> W[(Read permit and hall rate)]
    W --> X{Date after current end?}
    X -- No --> Y[Return 400 validation error]
    X -- Yes --> Z{Eligible to renew?}
    Z -- No --> ZA[Return 409 renewal unavailable]
    Z -- Yes --> ZB[Calculate fee:
    daily rate x min(added days, 30)]
    ZB --> ZC{Council Use?}
    ZC -- Yes --> ZD[Return quote with fee 0
    and no payment required]
    ZC -- No --> ZE[Return chargeable quote]
    Y --> ZF[React shows validation message]
    ZA --> ZG[React shows renewal unavailable message]
    ZD --> ZH[React shows quote and confirm action]
    ZE --> ZH
    ZH --> ZI{Officer confirms?}
    ZI -- No --> ZJ[Discard quote; no write]
    ZI -- Yes --> ZK[POST /api/permits/{permitNumber}/renewals
    {newEndDate, quoteId}]
    ZK --> C
    C --> ZL[RenewalService: revalidate quote]
    ZL --> ZM[Begin transaction]
    ZM --> ZN[(Write renewal record
    update permit
    write history)]
    ZN --> ZO{Fee greater than zero?}
    ZO -- Yes --> ZP[Set status AWAITING_PAYMENT]
    ZO -- No --> ZQ[Keep status ACTIVE]
    ZP --> ZR[Commit transaction]
    ZQ --> ZR
    ZR --> ZS[Return 200 updated permit]
    ZS --> ZT[React refreshes permit view]

    classDef client fill:#e8f0fe,stroke:#315b9d,color:#172b4d;
    classDef api fill:#f3e8ff,stroke:#7048a8,color:#32145c;
    classDef store fill:#e6f4ea,stroke:#27733f,color:#153b21;
    classDef decision fill:#fff4ce,stroke:#9a6b00,color:#3d2b00;
    classDef error fill:#fde8e8,stroke:#b42318,color:#5f1712;
    class A,I,J,K,S,T,ZF,ZG,ZH,ZJ,ZT client;
    class B,C,D,L,M,U,V,Q,G,H,P,R,ZA,ZB,ZD,ZE,ZK,ZL,ZM,ZP,ZQ,ZR,ZS api;
    class E,N,W,ZN store;
    class F,O,X,Z,ZC,ZI,ZO decision;
    class Y,ZA,P,R,ZF,ZG error;
```

## Technical decisions represented

- Search defaults to `createdAt DESC`, with a page size of 10.
- Search filters are optional and combined with AND semantics.
- The permit view reads the permit, renewal records, and history records without writing.
- Renewal quoting is separate from renewal confirmation so the officer sees the calculated fee before persistence.
- The confirmation endpoint revalidates the quote and eligibility inside the write operation.
- A renewal is eligible when the permit is `ACTIVE`, or `EXPIRED` with an end date no more than 90 days ago.
- The renewal fee is `dailyRate * min(addedDays, 30)`.
- Council Use renewals have a zero fee and remain `ACTIVE`.
- Chargeable renewals move to `AWAITING_PAYMENT`; payment processing is outside this service.
- A successful renewal transaction writes the renewal record, permit end date, and permit history together.
