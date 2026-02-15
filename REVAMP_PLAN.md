# FossWallet Full Revamp Plan

**Version:** 1.0  
**Date:** 2026-02-15  
**Target Completion:** Q2 2026 (8-12 weeks)  
**Prepared by:** AI Architecture Review (Claude Opus)

---

## Executive Summary

This document outlines a comprehensive revamp plan for FossWallet based on in-depth analysis of the codebase, architecture, security posture, and code quality. The revamp focuses on:

1. **Security Hardening** - Address 8 CRITICAL and 4 HIGH severity vulnerabilities
2. **Test Coverage** - Increase from ~5% to 60%+ with comprehensive test suite
3. **Code Quality** - Implement automated quality gates and static analysis
4. **Performance** - Optimize database queries, image loading, and UI rendering
5. **Documentation** - Create comprehensive developer and user documentation
6. **CI/CD** - Establish robust continuous integration and deployment pipeline

**Estimated Effort:** 320-400 developer hours  
**Team Size:** 1-2 developers  
**Timeline:** 8-12 weeks

---

## Table of Contents

1. [Project Goals](#project-goals)
2. [Current State Assessment](#current-state-assessment)
3. [Revamp Phases](#revamp-phases)
4. [Detailed Work Breakdown](#detailed-work-breakdown)
5. [Success Criteria](#success-criteria)
6. [Risk Assessment](#risk-assessment)
7. [Resource Requirements](#resource-requirements)
8. [Timeline](#timeline)

---

## Project Goals

### Primary Goals

1. **Security First**
   - Eliminate all CRITICAL vulnerabilities
   - Achieve OWASP Mobile Top 10 compliance
   - Pass security audit with no high-severity findings

2. **Quality & Reliability**
   - 60%+ test coverage with unit, integration, and UI tests
   - Zero P0/P1 bugs in production
   - <1% crash rate

3. **Performance**
   - App startup < 1 second
   - Pass import < 500ms average
   - Smooth 60fps UI rendering

4. **Maintainability**
   - Comprehensive documentation
   - Automated quality checks
   - Clear contribution guidelines

### Secondary Goals

1. **Feature Parity:** Maintain all existing features
2. **User Experience:** No breaking changes to UI/UX
3. **Backward Compatibility:** Support existing pass databases
4. **Open Source Health:** Active community engagement

---

## Current State Assessment

### Strengths ✅

- ✅ **Modern Architecture:** MVVM + Repository, Jetpack Compose, Hilt
- ✅ **Clean Code:** Well-organized, follows Kotlin conventions
- ✅ **Up-to-date Dependencies:** All major libraries current (2026-02)
- ✅ **Good Database Design:** Proper migrations, normalized schema
- ✅ **Active Development:** Regular commits, responsive to issues

### Critical Gaps ❌

- ❌ **Security:** 8 CRITICAL vulnerabilities (see SECURITY_ANALYSIS.md)
- ❌ **Testing:** Only 4 test files, ~5% coverage
- ❌ **Documentation:** Minimal code comments, no architecture docs
- ❌ **CI/CD:** Basic GitHub Actions, no security/quality gates
- ❌ **Error Handling:** Many edge cases not handled

### Technical Debt 📊

| Category | Severity | Items | Estimated Hours |
|----------|----------|-------|-----------------|
| Security | CRITICAL | 8 | 120 |
| Testing | HIGH | 1 | 80 |
| Documentation | MEDIUM | 1 | 40 |
| Code Quality | MEDIUM | 1 | 40 |
| Performance | MEDIUM | 1 | 40 |
| **Total** | | **12** | **320** |

---

## Revamp Phases

### Phase 1: Security Hardening (Weeks 1-3)
**Goal:** Eliminate all CRITICAL and HIGH security vulnerabilities  
**Duration:** 3 weeks  
**Effort:** 120 hours

### Phase 2: Test Infrastructure (Weeks 3-5)
**Goal:** Build comprehensive test suite with 60%+ coverage  
**Duration:** 2 weeks  
**Effort:** 80 hours

### Phase 3: Code Quality & CI/CD (Weeks 5-7)
**Goal:** Implement automated quality gates and security scanning  
**Duration:** 2 weeks  
**Effort:** 40 hours

### Phase 4: Performance Optimization (Weeks 7-9)
**Goal:** Optimize critical paths, improve app responsiveness  
**Duration:** 2 weeks  
**Effort:** 40 hours

### Phase 5: Documentation & Polish (Weeks 9-10)
**Goal:** Complete documentation, final testing, release prep  
**Duration:** 2 weeks  
**Effort:** 40 hours

### Phase 6: Beta Testing & Release (Weeks 11-12)
**Goal:** Public beta, bug fixes, stable release  
**Duration:** 2 weeks  
**Effort:** Variable (bug fixes)

---

## Detailed Work Breakdown

## Phase 1: Security Hardening (120 hours)

### 1.1 PKPass Signature Verification (40 hours)
**Priority:** CRITICAL  
**Issue:** Passes loaded without cryptographic verification

**Tasks:**
- [ ] Research Apple PKPass signature format (PKCS#7)
- [ ] Add BouncyCastle library for crypto operations
- [ ] Implement signature extraction from .pkpass ZIP
- [ ] Implement PKCS#7 signature verification
- [ ] Validate certificate chain against Apple root certs
- [ ] Add user warning for unsigned/invalid passes
- [ ] Unit tests for signature validation
- [ ] Integration tests for various pass formats
- [ ] Performance testing (ensure <100ms verification)
- [ ] Documentation: Security section update

**Files to Modify:**
- `app/build.gradle.kts` - Add BouncyCastle dependency
- `app/src/main/java/nz/eloque/foss_wallet/persistence/loader/PassLoader.kt`
- Create: `app/src/main/java/nz/eloque/foss_wallet/security/SignatureVerifier.kt`
- Create: `app/src/test/java/nz/eloque/foss_wallet/security/SignatureVerifierTest.kt`

**Acceptance Criteria:**
- ✅ All signed passes verified before loading
- ✅ Invalid/unsigned passes rejected with clear error
- ✅ Option to allow unsigned passes for testing
- ✅ No performance regression (<100ms per pass)

---

### 1.2 ZIP Path Traversal Fix (8 hours)
**Priority:** CRITICAL  
**Issue:** Malicious ZIP entries can write outside app directory

**Tasks:**
- [ ] Implement `validateZipEntryPath()` function
- [ ] Sanitize all ZIP entry names
- [ ] Reject entries with `..`, absolute paths
- [ ] Unit tests with malicious ZIP samples
- [ ] Security test with path traversal payloads

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/persistence/loader/PassLoader.kt`

**Code Change:**
```kotlin
private fun validateZipEntryPath(entryName: String): Boolean {
    if (entryName.contains("..")) return false
    if (entryName.startsWith("/")) return false
    val canonical = File(entryName).canonicalPath
    return canonical == entryName
}
```

**Acceptance Criteria:**
- ✅ Path traversal attacks blocked
- ✅ Valid passes still load correctly
- ✅ Security tests pass

---

### 1.3 Database Encryption (24 hours)
**Priority:** CRITICAL  
**Issue:** Sensitive data stored in plain SQLite

**Tasks:**
- [ ] Add SQLCipher dependency
- [ ] Implement encryption key generation/storage (Android Keystore)
- [ ] Migrate database to encrypted version
- [ ] Test data migration from plain to encrypted
- [ ] Performance testing (ensure minimal overhead)
- [ ] Backup/restore with encryption
- [ ] Documentation: Encryption design doc

**Files to Modify:**
- `app/build.gradle.kts` - Add SQLCipher dependency
- `app/src/main/java/nz/eloque/foss_wallet/persistence/WalletDb.kt`
- Create: `app/src/main/java/nz/eloque/foss_wallet/security/DatabaseEncryption.kt`
- Create: Database migration for encrypted version

**Acceptance Criteria:**
- ✅ Database encrypted at rest
- ✅ Existing users migrated seamlessly
- ✅ No data loss during migration
- ✅ Performance within 5% of unencrypted

---

### 1.4 EncryptedSharedPreferences (8 hours)
**Priority:** CRITICAL  
**Issue:** Settings stored in plain text

**Tasks:**
- [ ] Replace SharedPreferences with EncryptedSharedPreferences
- [ ] Migrate existing settings
- [ ] Test on Android 6+ devices
- [ ] Backward compatibility testing

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/persistence/SettingsStore.kt`

**Acceptance Criteria:**
- ✅ Settings encrypted on disk
- ✅ Existing users migrated
- ✅ No breaking changes

---

### 1.5 Certificate Pinning (16 hours)
**Priority:** CRITICAL  
**Issue:** No protection against MITM attacks

**Tasks:**
- [ ] Research pass vendor certificate patterns
- [ ] Implement certificate pinning for common vendors
- [ ] Handle certificate rotation gracefully
- [ ] Fallback strategy for unknown vendors
- [ ] Network security config XML
- [ ] Testing with proxy (Charles, mitmproxy)

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/api/PassbookApi.kt`
- Create: `app/src/main/res/xml/network_security_config.xml`

**Acceptance Criteria:**
- ✅ Apple Wallet URLs pinned
- ✅ MITM attacks blocked in testing
- ✅ Graceful handling of cert rotation

---

### 1.6 Android Manifest Security (8 hours)
**Priority:** CRITICAL  
**Issue:** Backup enabled, exported components

**Tasks:**
- [ ] Set `android:allowBackup="false"`
- [ ] Implement selective backup rules if needed
- [ ] Remove `application/octet-stream` MIME handler
- [ ] Review all exported components
- [ ] Add signature-level permissions where needed

**Files to Modify:**
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`

**Acceptance Criteria:**
- ✅ ADB backup disabled or restricted
- ✅ Intent filters narrowed to specific types
- ✅ Exported components justified

---

### 1.7 WebView Security (8 hours)
**Priority:** HIGH  
**Issue:** JavaScript enabled without sandboxing

**Tasks:**
- [ ] Evaluate if WebView is necessary
- [ ] If yes: Disable JavaScript or implement CSP
- [ ] Disable file access, geolocation
- [ ] Implement URL whitelist
- [ ] Test with malicious pass URLs

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/ui/screens/webview/WebviewView.kt`

**Options:**
1. **Remove WebView** (preferred)
2. **Disable JavaScript**
3. **Implement strict CSP**

**Acceptance Criteria:**
- ✅ XSS attacks mitigated
- ✅ No file system access from WebView
- ✅ Legitimate pass URLs still work

---

### 1.8 Zxing Library Update (8 hours)
**Priority:** MEDIUM  
**Issue:** Using version from 2018

**Tasks:**
- [ ] Investigate issue #184 (reason for pinning)
- [ ] Test latest Zxing version
- [ ] If issue persists, document CVE review
- [ ] Consider alternative: ML Kit Barcode Scanning

**Files to Modify:**
- `gradle/libs.versions.toml`

**Acceptance Criteria:**
- ✅ Updated to latest Zxing OR documented security review
- ✅ No regression in barcode functionality

---

## Phase 2: Test Infrastructure (80 hours)

### 2.1 Unit Test Suite (40 hours)
**Goal:** 60% unit test coverage

**Areas to Test:**

#### A. Parser Tests (12 hours)
- [ ] **PassParser:** All PKPass JSON formats
- [ ] **FieldParser:** All field types
- [ ] **ColorDeriving:** Color extraction edge cases
- [ ] **HexParser:** Invalid hex codes

**Files to Create:**
- `PassParserTest.kt` (expand existing)
- `FieldParserTest.kt`
- `ColorDerivingTest.kt`
- `HexParserTest.kt`

#### B. ViewModel Tests (12 hours)
- [ ] **PassViewModel:** Filtering, sorting, state management
- [ ] **CreateViewModel:** Pass creation, validation
- [ ] **SettingsViewModel:** Settings persistence

**Files to Create:**
- `PassViewModelTest.kt`
- `CreateViewModelTest.kt`
- `SettingsViewModelTest.kt`

#### C. Repository Tests (12 hours)
- [ ] **PassRepository:** CRUD operations, filtering
- [ ] **TagRepository:** Tag management
- [ ] **PassLocalizationRepository:** Localization logic

**Files to Create:**
- `PassRepositoryTest.kt`
- `TagRepositoryTest.kt`

#### D. Utility Tests (4 hours)
- [ ] **ExtensionFunctions:** Kotlin extensions
- [ ] **Hash:** Hash generation
- [ ] **Validators:** Input validation

---

### 2.2 Integration Tests (20 hours)
**Goal:** Test component interactions

#### A. Database Tests (12 hours)
- [ ] Migration tests (v4 → v22)
- [ ] Complex queries
- [ ] Transaction handling
- [ ] Foreign key constraints

**Files to Create:**
- `WalletDbTest.kt`
- `MigrationTest.kt`

#### B. Pass Loading Tests (8 hours)
- [ ] Import various .pkpass formats
- [ ] Error handling (corrupt files)
- [ ] Performance testing (large passes)

**Files to Create:**
- `PassLoaderIntegrationTest.kt`

---

### 2.3 UI Tests (20 hours)
**Goal:** Test critical user workflows

#### A. Compose UI Tests (16 hours)
- [ ] Wallet list rendering
- [ ] Pass detail view
- [ ] Pass creation flow
- [ ] Settings screen
- [ ] Search/filter functionality

**Files to Create:**
- `WalletScreenTest.kt`
- `PassScreenTest.kt`
- `CreateScreenTest.kt`

#### B. Navigation Tests (4 hours)
- [ ] Screen transitions
- [ ] Deep links
- [ ] Back navigation

**Files to Create:**
- `NavigationTest.kt`

---

## Phase 3: Code Quality & CI/CD (40 hours)

### 3.1 Static Analysis Setup (16 hours)

**Tools to Integrate:**

#### A. Ktlint (4 hours)
```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}
```

**Tasks:**
- [ ] Add ktlint plugin
- [ ] Configure code style
- [ ] Fix existing violations
- [ ] Add pre-commit hook

#### B. Detekt (4 hours)
```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}
```

**Tasks:**
- [ ] Add detekt plugin
- [ ] Configure rules
- [ ] Fix P0/P1 issues
- [ ] Generate baseline for existing code

#### C. Dependency Vulnerability Scanning (4 hours)
```kotlin
plugins {
    id("org.owasp.dependencycheck") version "9.0.9"
}
```

**Tasks:**
- [ ] Add OWASP dependency check
- [ ] Configure CVE threshold
- [ ] Document any accepted risks

#### D. Code Coverage (4 hours)
```kotlin
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}
```

**Tasks:**
- [ ] Add Kover for coverage
- [ ] Set coverage thresholds
- [ ] Generate coverage reports

---

### 3.2 CI/CD Pipeline Enhancement (24 hours)

**GitHub Actions Workflows:**

#### A. Pull Request Checks (8 hours)
```yaml
# .github/workflows/pr-checks.yml
name: PR Checks
on: [pull_request]
jobs:
  test:
    - Lint (ktlint)
    - Static analysis (detekt)
    - Unit tests
    - Integration tests
    - Code coverage (min 60%)
    - Security scan (CodeQL)
    - Build APK
```

**Tasks:**
- [ ] Create PR checks workflow
- [ ] Require all checks to pass
- [ ] Add status badges to README

#### B. Security Scanning (8 hours)
```yaml
# .github/workflows/security.yml
name: Security Scan
on: [push, pull_request]
jobs:
  codeql:
    - CodeQL analysis (Java/Kotlin)
  dependency-check:
    - OWASP dependency check
  secrets:
    - Scan for hardcoded secrets
```

**Tasks:**
- [ ] Enable CodeQL
- [ ] Configure dependency scanning
- [ ] Add secret scanning

#### C. Release Automation (8 hours)
```yaml
# .github/workflows/release.yml
name: Release
on:
  push:
    tags: ['v*']
jobs:
  build:
    - Build signed APK
    - Build AAB for Play Store
    - Run full test suite
    - Generate changelog
    - Create GitHub release
    - Upload artifacts
```

**Tasks:**
- [ ] Automate release builds
- [ ] Sign APKs in CI
- [ ] Generate changelogs
- [ ] Deploy to F-Droid

---

## Phase 4: Performance Optimization (40 hours)

### 4.1 Database Optimization (16 hours)

**Tasks:**
- [ ] Analyze slow queries with Room's query profiler
- [ ] Add database indices for common queries
- [ ] Optimize pass filtering query
- [ ] Implement pagination for large lists
- [ ] Add query result caching

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/persistence/pass/PassDao.kt`
- `app/src/main/java/nz/eloque/foss_wallet/persistence/WalletDb.kt`

**Queries to Optimize:**
```kotlin
// Before
@Query("SELECT * FROM passes WHERE description LIKE '%' || :query || '%'")

// After (with FTS)
@Query("SELECT * FROM passes_fts WHERE passes_fts MATCH :query")
```

**Acceptance Criteria:**
- ✅ Search query < 50ms for 1000 passes
- ✅ List load < 100ms
- ✅ No UI jank during scrolling

---

### 4.2 Image Loading Optimization (12 hours)

**Tasks:**
- [ ] Implement image caching strategy
- [ ] Lazy load images in LazyColumn
- [ ] Optimize image sizes (downscale)
- [ ] Use Coil's placeholder/error handling
- [ ] Profile memory usage

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/ui/card/PassCard.kt`

**Code Improvements:**
```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(pass.iconUri)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .size(Size(100, 100)) // Constrain size
        .build(),
    contentDescription = "Pass icon"
)
```

**Acceptance Criteria:**
- ✅ Image load < 100ms from cache
- ✅ Memory usage < 50MB for 100 passes
- ✅ Smooth 60fps scrolling

---

### 4.3 App Startup Optimization (12 hours)

**Tasks:**
- [ ] Profile app startup with Android Profiler
- [ ] Defer non-critical initialization
- [ ] Use App Startup library for dependencies
- [ ] Lazy initialize Hilt components
- [ ] Optimize Room database opening

**Files to Modify:**
- `app/src/main/java/nz/eloque/foss_wallet/app/WalletApplication.kt`
- `app/src/main/java/nz/eloque/foss_wallet/app/AppModule.kt`

**Improvements:**
```kotlin
// Defer WorkManager initialization
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }
}
```

**Acceptance Criteria:**
- ✅ Cold start < 1 second
- ✅ Warm start < 500ms
- ✅ Time to first frame < 800ms

---

## Phase 5: Documentation & Polish (40 hours)

### 5.1 Code Documentation (16 hours)

**Tasks:**
- [ ] Add KDoc to all public APIs
- [ ] Document complex algorithms
- [ ] Add inline comments for non-obvious code
- [ ] Generate Dokka documentation

**Files to Document (Priority):**
- [ ] `PassParser.kt`
- [ ] `PassLoader.kt`
- [ ] `PassViewModel.kt`
- [ ] `PassRepository.kt`
- [ ] `WalletDb.kt`

**Example:**
```kotlin
/**
 * Parses a PKPass JSON file into a [Pass] domain model.
 *
 * This function validates the JSON structure, extracts all fields,
 * derives colors, and handles localization.
 *
 * @param json The raw JSON string from pass.json
 * @param id Unique identifier for this pass
 * @param bitmaps Images extracted from the .pkpass bundle
 * @param addedAt Timestamp when pass was imported
 * @return Parsed [Pass] object
 * @throws JSONException if JSON is invalid
 * @throws IllegalArgumentException if required fields are missing
 */
fun parse(json: String, id: String, bitmaps: PassBitmaps, addedAt: Long): Pass
```

---

### 5.2 User Documentation (8 hours)

**Tasks:**
- [ ] Expand README.md with:
  - [ ] Feature overview with screenshots
  - [ ] Installation instructions
  - [ ] User guide (import, view, update passes)
  - [ ] FAQ section
  - [ ] Troubleshooting guide
- [ ] Create SECURITY.md for responsible disclosure
- [ ] Update PRIVACY.md with encryption details

**Files to Create/Update:**
- `README.md` (expand)
- `SECURITY.md` (create)
- `PRIVACY.md` (update)
- `docs/USER_GUIDE.md` (create)
- `docs/FAQ.md` (create)

---

### 5.3 Developer Documentation (16 hours)

**Tasks:**
- [ ] Create CONTRIBUTING.md
- [ ] Document build process
- [ ] Architecture decision records (ADRs)
- [ ] Code style guide
- [ ] Testing guide
- [ ] Release process documentation

**Files to Create:**
- `CONTRIBUTING.md`
- `docs/BUILD.md`
- `docs/ARCHITECTURE.md` (based on analysis)
- `docs/TESTING.md`
- `docs/RELEASE.md`
- `docs/adr/` - Architecture Decision Records
  - `001-use-jetpack-compose.md`
  - `002-database-encryption.md`
  - `003-certificate-pinning.md`

**ADR Template:**
```markdown
# ADR-002: Database Encryption

## Status
Accepted

## Context
Passes contain sensitive data (auth tokens, barcodes, PII).
Rooted devices can access unencrypted SQLite databases.

## Decision
Use SQLCipher for database encryption at rest.
Encryption key stored in Android Keystore.

## Consequences
Positive:
- Data protected on rooted devices
- Compliance with security best practices

Negative:
- ~5% performance overhead
- Complexity in key management
- Migration effort for existing users

## Alternatives Considered
- Room's EncryptedEntity (not available yet)
- Manual field-level encryption (too complex)
```

---

## Phase 6: Beta Testing & Release (Variable hours)

### 6.1 Beta Testing (Weeks 11-12)

**Tasks:**
- [ ] Create beta release channel (Google Play Beta, F-Droid Beta)
- [ ] Recruit beta testers (GitHub, Reddit, XDA)
- [ ] Monitor crash reports
- [ ] Collect feedback
- [ ] Triage and fix P0/P1 bugs
- [ ] Performance testing on various devices

**Metrics to Track:**
- Crash-free users (target: >99%)
- ANR rate (target: <0.1%)
- App startup time across devices
- User feedback ratings

---

### 6.2 Release Preparation (Week 12)

**Tasks:**
- [ ] Final security audit
- [ ] Performance audit
- [ ] Accessibility audit
- [ ] Update changelogs
- [ ] Update app store listings
- [ ] Prepare release notes
- [ ] Tag release version
- [ ] Build signed release APKs/AABs

---

## Success Criteria

### Must-Have (P0)

- [x] ✅ All CRITICAL security vulnerabilities fixed
- [ ] ✅ 60%+ test coverage
- [ ] ✅ All tests passing
- [ ] ✅ No P0 bugs
- [ ] ✅ Documentation complete
- [ ] ✅ CI/CD pipeline functional
- [ ] ✅ Performance targets met

### Should-Have (P1)

- [ ] ✅ All HIGH security issues fixed
- [ ] ✅ 70%+ test coverage
- [ ] ✅ CodeQL scan passing
- [ ] ✅ No P1 bugs
- [ ] ✅ Accessibility compliance
- [ ] ✅ Beta testing complete

### Nice-to-Have (P2)

- [ ] ✅ 80%+ test coverage
- [ ] ✅ All MEDIUM security issues addressed
- [ ] ✅ Performance exceeds targets
- [ ] ✅ Multi-language docs

---

## Risk Assessment

### High Risks

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **Database migration failure** | Medium | High | Thorough testing, backup/restore functionality |
| **Performance regression** | Medium | High | Continuous profiling, benchmark tests |
| **Breaking changes** | Low | High | Comprehensive testing, staged rollout |

### Medium Risks

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **Schedule overrun** | Medium | Medium | Prioritize P0 items, parallel workstreams |
| **Dependency conflicts** | Low | Medium | Lock versions, test early |
| **Key management issues** | Low | Medium | Android Keystore best practices |

### Low Risks

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **User resistance** | Low | Low | Clear communication, opt-in beta |
| **CI/CD issues** | Low | Low | Test pipelines early |

---

## Resource Requirements

### Developer Skills Required

- **Android Development:** Expert level (Kotlin, Compose, MVVM)
- **Security:** Intermediate (crypto, secure coding)
- **Testing:** Intermediate (JUnit, Espresso, Mockito)
- **CI/CD:** Intermediate (GitHub Actions)
- **Performance:** Intermediate (Android Profiler)

### Tools & Services

- **Development:** Android Studio, Git
- **Testing:** Firebase Test Lab (optional)
- **CI/CD:** GitHub Actions (free for open source)
- **Security:** CodeQL (free for open source)
- **Monitoring:** Google Play Console, F-Droid metrics

### Estimated Costs

- **Developer Time:** 320-400 hours @ volunteer or TBD rate
- **Infrastructure:** $0 (all tools free for open source)
- **Testing Devices:** Borrow/virtual devices (free)
- **Total:** ~$0 for FOSS project

---

## Timeline

### Gantt Chart (Simplified)

```
Week 1-3:  █████████████████ Security Hardening
Week 3-5:          ██████████ Test Infrastructure
Week 5-7:                  ██████ Code Quality & CI/CD
Week 7-9:                      ██████ Performance
Week 9-10:                          ████ Documentation
Week 11-12:                              ████ Beta & Release
```

### Milestones

- **Week 3:** Security fixes complete, security audit passed
- **Week 5:** Test coverage >60%, all tests passing
- **Week 7:** CI/CD pipeline live, quality gates enforced
- **Week 9:** Performance targets met, docs complete
- **Week 10:** Beta release candidate ready
- **Week 12:** Stable release published

---

## Post-Revamp Maintenance

### Ongoing Activities

1. **Weekly:**
   - Monitor crash reports
   - Review new issues
   - Dependency security alerts

2. **Monthly:**
   - Dependency updates
   - Performance review
   - Security scan review

3. **Quarterly:**
   - Major version planning
   - Architecture review
   - User feedback analysis

4. **Annually:**
   - External security audit
   - Technology stack review
   - Roadmap update

---

## Appendix A: Dependency List

### New Dependencies to Add

```kotlin
// Security
implementation("org.bouncycastle:bcpkix-jdk18on:1.78")  // PKPass signatures
implementation("net.zetetic:android-database-sqlcipher:4.5.4")  // DB encryption
implementation("androidx.security:security-crypto:1.1.0-alpha06")  // Encrypted prefs

// Testing
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
testImplementation("app.cash.turbine:turbine:1.0.0")  // Flow testing
testImplementation("io.mockk:mockk:1.13.9")  // Kotlin-friendly mocking
androidTestImplementation("androidx.test:core-ktx:1.5.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")

// Code Quality
plugin("org.jlleitschuh.gradle.ktlint:12.1.0")
plugin("io.gitlab.arturbosch.detekt:1.23.5")
plugin("org.owasp:dependency-check-gradle:9.0.9")
plugin("org.jetbrains.kotlinx.kover:0.7.5")  // Coverage
```

---

## Appendix B: Testing Strategy

### Test Pyramid

```
         ╱╲
        ╱  ╲      E2E Tests (5%)
       ╱────╲     - Critical user flows
      ╱      ╲    - Real device testing
     ╱────────╲   
    ╱          ╲  Integration Tests (20%)
   ╱────────────╲ - Component interactions
  ╱──────────────╲
 ╱                ╲ Unit Tests (75%)
╱──────────────────╲ - Business logic
                      - ViewModels
                      - Repositories
                      - Parsers
```

### Coverage Targets by Module

| Module | Target Coverage |
|--------|----------------|
| Parsing | 90% |
| ViewModels | 80% |
| Repositories | 80% |
| Database | 70% (via integration tests) |
| UI Components | 50% |
| Utilities | 90% |

---

## Appendix C: Security Checklist

### Pre-Release Security Audit

- [ ] All CRITICAL vulnerabilities fixed
- [ ] All HIGH vulnerabilities fixed
- [ ] CodeQL scan passing (no high/critical alerts)
- [ ] OWASP dependency check passing
- [ ] Manual penetration testing completed
- [ ] Security.md published
- [ ] Responsible disclosure process documented
- [ ] No hardcoded secrets in code
- [ ] ProGuard rules reviewed
- [ ] Network security config verified
- [ ] Android manifest reviewed
- [ ] File permissions reviewed
- [ ] Certificate pinning tested

---

## Conclusion

This comprehensive revamp plan addresses all critical gaps in FossWallet while maintaining its strengths. By following this phased approach, we can:

1. **Secure the app** - Eliminate vulnerabilities, protect user data
2. **Improve quality** - Establish testing and CI/CD practices
3. **Enhance performance** - Optimize critical paths
4. **Enable contribution** - Document architecture and processes

**Commitment:** This revamp is feasible within 8-12 weeks with 1-2 dedicated developers. The plan prioritizes security and quality while maintaining backward compatibility and user trust.

**Next Steps:**
1. Review and approve this plan
2. Set up project tracking (GitHub Projects)
3. Begin Phase 1: Security Hardening
4. Regular progress updates (weekly)

---

*This revamp plan is a living document. Update as needed based on progress and new learnings.*

**Last Updated:** 2026-02-15  
**Version:** 1.0
