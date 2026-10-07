# Security Specification — CivilBIM Tutor Cloud Sync

## 1. Data Invariants
1. **User Ownership Isolation**: Every document under `/users/{userId}`, `/users/{userId}/bookmarked_commands/{commandId}`, and `/users/{userId}/interview_attempts/{attemptId}` is strictly owned by `userId == request.auth.uid`. Cross-user reads, lists, creates, updates, and deletes are forbidden.
2. **Schema & Key Allowlisting**: Every write (`create` and `update`) must pass standalone `isValid[Entity](incoming())` validation, enforce `hasAll` and `hasOnly` on `create`, and enforce `affectedKeys().hasOnly(...)` on `update`.
3. **Immutable Identity & Creation Timestamps**: `userId`, `id`, and `createdAt` cannot be modified after creation (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Temporal Integrity**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` values (`<= request.time`) populated via `FieldValue.serverTimestamp()`.
5. **String & Numeric Bounds**: Every string property enforces strict `.size()` bounds and `score` enforces integer range `0..10`. Document IDs must match `isValidId()`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Profile Read**: `GET /users/alice_123` with `auth = null` -> REJECT.
2. **Cross-User Profile Read**: `GET /users/bob_456` with `auth.uid = "alice_123"` -> REJECT.
3. **Identity Spoofing on Create**: `CREATE /users/alice_123` where `userId: "bob_456"` -> REJECT.
4. **Shadow Field Injection on Profile**: `CREATE /users/alice_123` with extra field `isAdmin: true` -> REJECT (`hasOnly` guard).
5. **Oversized DisplayName Resource Exhaustion**: `CREATE /users/alice_123` with 500-char `displayName` -> REJECT (`.size() <= 100`).
6. **Future Timestamp Spoofing**: `CREATE /users/alice_123` with `createdAt` in the future -> REJECT (`<= request.time`).
7. **Immutable Field Mutation**: `UPDATE /users/alice_123` mutating `createdAt` or `userId` -> REJECT.
8. **Cross-User Bookmark Access**: `LIST /users/bob_456/bookmarked_commands` as `alice_123` -> REJECT.
9. **Bookmark Shadow Update**: `UPDATE /users/alice_123/bookmarked_commands/cmd_1` adding `isVerified: true` -> REJECT.
10. **Invalid Bookmark Command Type**: `UPDATE /users/alice_123/bookmarked_commands/cmd_1` setting `command: 12345` -> REJECT.
11. **Out-of-Range Interview Score**: `CREATE /users/alice_123/interview_attempts/att_1` with `score: 15` -> REJECT (`0 <= score <= 10`).
12. **Cross-User Interview Attempt Delete**: `DELETE /users/bob_456/interview_attempts/att_1` as `alice_123` -> REJECT.
