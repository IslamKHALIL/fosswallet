# CI/CD Pipeline Documentation

This document describes the CI/CD pipeline for this forked repository. This pipeline ensures code quality and prevents broken code from reaching the main branch.

## Pipeline Overview

This repository uses two main workflows to ensure code quality:

### 1. CI Pipeline (`ci.yml`)
**Triggers:** Runs on all branches when code is pushed or PRs are created

**Purpose:** Provides immediate feedback on code quality for all branches

**Jobs:**
- **Gradle Wrapper Validation:** Ensures Gradle wrapper is valid and secure
- **Lint:** Runs Android Lint to catch code quality issues
- **Unit Tests:** Runs all unit tests (`testDebugUnitTest`)
- **Instrumented Tests:** Runs integration tests on Android emulators (API 28 & 34)
- **Build:** Builds the debug APK to ensure the app compiles

**Artifacts:** Test reports, lint results, and APK files are uploaded as artifacts

### 2. PR Validation to Main (`pr-validation.yml`)
**Triggers:** Runs when a PR is opened/updated targeting the `main` branch

**Purpose:** Gate-keeper for the main branch - ensures only tested code is merged

**Jobs:** Same as CI Pipeline, but specifically for PRs to main

**Requirement:** All checks must pass before merging to main

## Workflow for Contributing

### Working on Feature Branches

1. **Create a feature branch** from main:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** and commit regularly:
   ```bash
   git add .
   git commit -m "Your commit message"
   ```

3. **Push your branch** to trigger CI:
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Monitor CI results** in GitHub Actions tab
   - All tests should pass
   - Review any lint warnings or errors
   - Fix issues and push again if needed

### Merging to Main Branch

1. **Create a Pull Request** to main:
   - Go to GitHub and create a PR from your feature branch to `main`
   - Add a descriptive title and description

2. **Wait for PR validation** to complete:
   - All tests must pass (Unit, Integration, E2E)
   - Lint checks must pass
   - Build must succeed

3. **Review the PR** yourself:
   - Check the changes one more time
   - Ensure all tests passed
   - Verify artifacts if needed

4. **Merge the PR** when all checks pass:
   - Use "Squash and merge" or "Merge commit" as preferred
   - Delete the feature branch after merging

### Creating PR to Upstream Repository

Once your main branch is stable and tested:

1. **Ensure main branch is clean:**
   - All CI checks passing
   - No broken tests
   - All features working as expected

2. **Review your changes:**
   ```bash
   git log origin/main..main
   ```

3. **Create PR to upstream:**
   - Go to the original repository (SeineEloquenz/fosswallet)
   - Click "New Pull Request"
   - Select "compare across forks"
   - Set base repository: `SeineEloquenz/fosswallet` base: `main`
   - Set head repository: `IslamKHALIL/fosswallet` compare: `main`
   - Add detailed description of your changes
   - Submit the PR

## CI/CD Pipeline Details

### Test Types

1. **Unit Tests (`testDebugUnitTest`)**
   - Fast, isolated tests
   - Test individual components
   - Run without Android emulator
   - Location: `app/src/test/`

2. **Instrumented Tests (`connectedDebugAndroidTest`)**
   - Integration tests
   - Run on Android emulator
   - Test UI and Android-specific functionality
   - Location: `app/src/androidTest/`
   - Tested on API levels: 28 (min) and 34 (recent)

3. **Lint Checks (`lintDebug`)**
   - Static code analysis
   - Catches common issues and potential bugs
   - Enforces code quality standards

### Running Tests Locally

Before pushing, you can run tests locally:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Build debug APK
./gradlew assembleDebug

# Run instrumented tests (requires emulator or device)
./gradlew connectedDebugAndroidTest
```

### Understanding CI Status

- ✅ **Green checkmark:** All tests passed, safe to proceed
- ❌ **Red X:** Tests failed, review the logs and fix issues
- 🟡 **Yellow dot:** Tests are running, please wait
- ⚪ **Gray circle:** Tests haven't started yet

### Troubleshooting

**CI fails with build errors:**
- Pull latest changes: `git pull origin main`
- Clean build: `./gradlew clean`
- Sync Gradle files in Android Studio

**Tests fail locally but pass in CI (or vice versa):**
- Ensure you're using Java 17
- Check Gradle version matches CI
- Verify emulator API level if running instrumented tests

**Lint errors:**
- Review lint report in artifacts
- Fix or suppress with proper justification
- Run `./gradlew lintDebug` locally first

## Best Practices

1. **Always run tests locally** before pushing
2. **Keep commits atomic** - one logical change per commit
3. **Write meaningful commit messages**
4. **Don't merge to main** until all CI checks pass
5. **Review CI artifacts** if tests fail
6. **Keep your fork synced** with upstream regularly
7. **Test thoroughly** on your main branch before creating upstream PR

## Branch Protection (Recommended)

To enforce CI checks, consider enabling branch protection for `main`:

1. Go to repository Settings → Branches
2. Add branch protection rule for `main`
3. Enable:
   - ✅ Require a pull request before merging
   - ✅ Require status checks to pass before merging
   - ✅ Require branches to be up to date before merging
   - Select required checks: PR Validation Complete

This ensures code can only be merged to main after passing all tests.

## Pipeline Maintenance

The CI/CD pipelines are defined in:
- `.github/workflows/ci.yml` - Main CI pipeline
- `.github/workflows/pr-validation.yml` - PR validation for main branch

Update these files if you need to:
- Add new test types
- Change test configurations
- Modify build steps
- Adjust emulator settings

---

**Note:** This CI/CD pipeline is specific to this forked repository and is not intended to be merged back to the upstream repository (SeineEloquenz/fosswallet). It serves as quality assurance for your main branch before creating PRs to the upstream.
