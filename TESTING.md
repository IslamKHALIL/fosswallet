# Testing & CI/CD Documentation

This document provides an overview of the testing infrastructure and CI/CD pipelines for this forked repository.

## 🎯 Purpose

This testing and CI/CD infrastructure is designed specifically for **this forked repository** to:
- Maintain code quality in the main branch
- Catch bugs early through automated testing
- Ensure all changes are tested before merging
- Provide confidence when creating PRs to the upstream repository

**Note:** These workflows and additional tests are for fork maintenance only and are not intended to be merged back to the upstream repository (SeineEloquenz/fosswallet).

## 📊 Test Coverage

### Unit Tests (34 tests)
Located in `app/src/test/`:
- **HashTest** (7 tests) - SHA-256 hashing utilities
- **BarCodeTest** (27 tests) - Barcode format detection, encoding, and validation
- **PassTypeTest** - Pass type identification and comparison
- **JsonLoaderTest** - JSON loading and parsing
- **PassParserTest** - Pass parsing from JSON

### Integration Tests (16 tests)
Located in `app/src/androidTest/`:
- **WalletDbIntegrationTest** (15 tests) - Database operations
  - Pass CRUD operations
  - Tag management
  - Pass-Tag relationships
  - PassGroup associations
  - Data flow and reactivity
- **ExampleInstrumentedTest** - Basic instrumentation test

### E2E Tests (12 tests)
Located in `app/src/androidTest/ui/`:
- **MainFlowE2ETest** (12 tests) - End-to-end user flows
  - App launch and initialization
  - Navigation and back navigation
  - UI responsiveness
  - Configuration changes (rotation)
  - Memory management
  - Interactive elements

**Total: 62 automated tests**

## 🔄 CI/CD Pipelines

### 1. CI Pipeline (`ci.yml`)
Runs on all branches when code is pushed or PRs are created.

**Jobs:**
- ✅ Gradle Wrapper Validation
- ✅ Lint (Android Lint)
- ✅ Unit Tests
- ✅ Integration Tests (on Android emulators API 28 & 34)
- ✅ Build Debug APK

**Triggers:**
- Push to any branch
- Pull request to any branch
- Manual workflow dispatch

### 2. PR Validation (`pr-validation.yml`)
Runs specifically when creating PRs to the `main` branch.

**Purpose:** Gate-keeper for the main branch - ensures only tested code is merged.

**Same jobs as CI Pipeline, but required to pass before merge.**

## 🚀 Workflow Guide

### For Development

1. **Create a feature branch:**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make changes and commit:**
   ```bash
   git add .
   git commit -m "Add my feature"
   ```

3. **Push and trigger CI:**
   ```bash
   git push origin feature/my-feature
   ```

4. **Monitor CI results** in GitHub Actions tab
   - All tests must pass
   - Fix any issues and push again

### For Merging to Main

1. **Create a Pull Request** targeting `main`
2. **Wait for PR validation** to complete
   - All tests must pass
   - Lint checks must pass
   - Build must succeed
3. **Review changes** yourself
4. **Merge** when all checks are green ✅

### For Contributing Upstream

1. **Ensure your main branch is stable:**
   - All CI checks passing
   - No broken tests
   - Features working as expected

2. **Create PR to upstream:**
   - Navigate to SeineEloquenz/fosswallet
   - Create PR from your fork's main to upstream main
   - Add detailed description
   - Submit

## 🧪 Running Tests Locally

### Prerequisites
- Java 17
- Android SDK
- Android emulator (for integration tests)

### Commands

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific unit test class
./gradlew testDebugUnitTest --tests "nz.eloque.foss_wallet.utils.HashTest"

# Run lint
./gradlew lintDebug

# Run integration tests (requires emulator or device)
./gradlew connectedDebugAndroidTest

# Build debug APK
./gradlew assembleDebug

# Clean build
./gradlew clean
```

### Test Reports
After running tests, find reports at:
- Unit tests: `app/build/reports/tests/testDebugUnitTest/index.html`
- Lint: `app/build/reports/lint-results-debug.html`
- Integration tests: `app/build/reports/androidTests/connected/index.html`

## 📋 CI Status Indicators

- ✅ **Green** - All tests passed, safe to proceed
- ❌ **Red** - Tests failed, review logs and fix issues
- 🟡 **Yellow** - Tests are running, please wait
- ⚪ **Gray** - Tests haven't started yet

## 🔧 Troubleshooting

### Build fails locally
```bash
./gradlew clean
git pull origin main
./gradlew assembleDebug
```

### Tests fail locally but pass in CI
- Verify Java version: `java -version` (should be 17)
- Check Android SDK is updated
- Clear Gradle cache: `./gradlew clean --no-daemon`

### Lint errors
```bash
./gradlew lintDebug
# Check report: app/build/reports/lint-results-debug.html
```

### Integration tests fail
- Ensure emulator is running
- Check emulator API level matches test requirements (28 or 34)
- Verify sufficient system resources

## 📖 Additional Resources

- **[CI_CD_PIPELINE.md](CI_CD_PIPELINE.md)** - Detailed CI/CD documentation
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Quick reference guide
- **[GitHub Actions](../../actions)** - View workflow runs

## 🔒 Branch Protection (Optional)

To enforce CI checks on the `main` branch:

1. Go to **Settings → Branches**
2. Add branch protection rule for `main`
3. Enable:
   - ✅ Require a pull request before merging
   - ✅ Require status checks to pass before merging
   - ✅ Require branches to be up to date before merging
4. Select required status checks:
   - PR Validation Complete
   - Unit Tests
   - Integration Tests
   - Lint
   - Build

This ensures code can only be merged after passing all tests.

## 🎯 Test Coverage Goals

Current coverage: **62 automated tests**

Areas well covered:
- ✅ Utility functions (Hash, BarCode)
- ✅ Database operations
- ✅ Basic UI flows

Future expansion opportunities:
- Additional model tests
- API client tests
- More comprehensive UI tests
- Performance tests

## 🤝 Maintenance

### Updating Tests
When adding new features:
1. Add corresponding unit tests
2. Add integration tests if touching database
3. Add E2E tests if changing UI flows
4. Ensure all tests pass before creating PR

### Updating CI/CD
Workflow files located in `.github/workflows/`:
- `ci.yml` - Main CI pipeline
- `pr-validation.yml` - PR validation for main branch
- Other existing workflows (unchanged)

## ℹ️ Notes

- CI/CD runs on GitHub Actions infrastructure
- Integration tests use Android emulators (API 28 & 34)
- Emulator tests may take 15-30 minutes
- All artifacts (APKs, reports) are uploaded for review
- Workflows use Gradle caching for faster builds

---

For questions or issues with the CI/CD pipeline, please refer to the workflow run logs in the GitHub Actions tab.
