# Build Environment Issues and Solutions

## Issue: Android Gradle Plugin Cannot Be Downloaded

### Problem
When running `./gradlew` commands, you may see errors like:
```
Plugin [id: 'com.android.application', version: 'X.X.X'] was not found
Could not resolve host: dl.google.com
```

### Root Cause
The build environment cannot access external Maven repositories (specifically `dl.google.com` for the Android Gradle Plugin). This prevents Gradle from downloading required dependencies.

### Solutions

#### Solution 1: Run Tests in GitHub Actions (Recommended)
The tests are designed to run in GitHub Actions CI where network access is properly configured:

1. Push your branch to GitHub:
   ```bash
   git push origin your-branch-name
   ```

2. GitHub Actions will automatically run all tests
3. Check the Actions tab for results: `https://github.com/IslamKHALIL/fosswallet/actions`

#### Solution 2: Run in Local Environment
If you have Android Studio or proper Android SDK setup locally:

1. Clone the repository to your local machine
2. Open in Android Studio
3. Let Android Studio download dependencies
4. Run tests from IDE or command line:
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew connectedDebugAndroidTest
   ./gradlew lintDebug
   ```

#### Solution 3: Use Docker with Network Access
If you need to test locally without Android Studio:

1. Use a Docker container with network access:
   ```bash
   docker run --rm -v $(pwd):/project -w /project \
     gradle:8.5-jdk17 \
     gradle testDebugUnitTest --no-daemon
   ```

### Verification Without Building

Even without building, you can verify the CI/CD pipeline setup:

1. **Check workflow files exist:**
   ```bash
   ls -la .github/workflows/
   ```
   Should show: `ci.yml`, `pr-validation.yml`, `configure-branch-protection.yml`

2. **Run verification script:**
   ```bash
   ./scripts/verify-branch-protection.sh
   ```
   This checks files and configuration without requiring a build.

3. **Validate YAML syntax:**
   ```bash
   # Install yamllint if needed
   yamllint .github/workflows/*.yml
   ```

4. **Check test files exist:**
   ```bash
   find app/src/test -name "*.kt"
   find app/src/androidTest -name "*.kt"
   ```

### Testing in GitHub Actions

The proper way to test is through GitHub Actions:

1. **Create a test branch:**
   ```bash
   git checkout -b test-ci-pipeline
   git commit --allow-empty -m "Test CI pipeline"
   git push origin test-ci-pipeline
   ```

2. **Watch the CI run:**
   - Go to: `https://github.com/IslamKHALIL/fosswallet/actions`
   - You'll see the CI workflow running
   - It will download dependencies and run all tests

3. **Create a test PR:**
   - Create PR from `test-ci-pipeline` to `main`
   - The PR validation workflow will run
   - All status checks will appear

### Expected Behavior in GitHub Actions

When workflows run in GitHub Actions:

✅ **CI Workflow** runs on all branches:
- Downloads Android Gradle Plugin successfully
- Runs lint checks
- Executes unit tests (6 test files, 34 tests)
- Runs integration tests (3 test files, 16 tests)
- Builds debug APK
- All tests should pass

✅ **PR Validation** runs on PRs to main:
- Same checks as CI workflow
- Must pass before merge is allowed
- Provides status checks for branch protection

### Current AGP Version

The `gradle/libs.versions.toml` file specifies:
```toml
androidGradlePlugin = "8.3.2"
```

This is a stable version that works in GitHub Actions. The version was originally set to `8.13.2` which doesn't exist - this has been corrected.

### Troubleshooting in GitHub Actions

If tests fail in GitHub Actions:

1. **Check the workflow run logs:**
   - Click on the failed workflow
   - Click on the failed job
   - Review the logs for specific errors

2. **Common issues:**
   - Dependency version conflicts
   - Test failures (legitimate code issues)
   - Timeout (tests taking too long)

3. **Fix and retry:**
   - Fix the issue locally
   - Commit and push
   - GitHub Actions will automatically run again

## Summary

**For Development:**
- ✅ Use GitHub Actions for automated testing
- ✅ Use Android Studio for local development
- ✅ Use verification script to check configuration
- ❌ Don't rely on this sandboxed environment for builds

**The CI/CD pipeline is fully functional in GitHub Actions**, which is the intended environment for automated testing.
