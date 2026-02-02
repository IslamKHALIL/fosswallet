# Quick CI/CD Reference

## For Contributors (Your Workflow)

### 1. Working on Features
```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "Add my feature"

# Push and check CI
git push origin feature/my-feature
# ✅ Check GitHub Actions - all tests must pass
```

### 2. Merging to Main
```bash
# Create PR to main on GitHub
# ✅ Wait for all PR validation checks to pass
# ✅ Review changes
# ✅ Merge when green
```

### 3. Contributing to Upstream
```bash
# After testing on your main branch:
# 1. Go to SeineEloquenz/fosswallet on GitHub
# 2. Create PR from your main to upstream main
# 3. Add detailed description
# 4. Submit PR
```

## CI Pipeline Checks

Every push and PR runs:
- ✅ **Lint** - Code quality checks
- ✅ **Unit Tests** - Fast isolated tests
- ✅ **Integration Tests** - UI and Android tests on emulators
- ✅ **Build** - APK compilation

## Local Testing

```bash
# Before pushing, run locally:
./gradlew lintDebug                  # Lint checks
./gradlew testDebugUnitTest          # Unit tests
./gradlew assembleDebug              # Build APK
./gradlew connectedDebugAndroidTest  # Integration tests (needs emulator)
```

## Status Indicators

- ✅ Green = All passed, safe to merge
- ❌ Red = Failed, needs fixes
- 🟡 Yellow = Running, wait...
- ⚪ Gray = Pending

## Quick Troubleshooting

**Build fails?**
```bash
./gradlew clean
git pull origin main
```

**Tests fail?**
- Check error logs in GitHub Actions
- Run the specific test locally
- Fix and push again

**Lint errors?**
```bash
./gradlew lintDebug
# Review report in app/build/reports/
```

---

📖 **Full Documentation:** See [CI_CD_PIPELINE.md](CI_CD_PIPELINE.md) for detailed information.
