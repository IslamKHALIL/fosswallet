# Branch Protection Quick Start Guide

## ⚡ Fast Setup (5 Minutes)

This guide gets branch protection working quickly. For detailed explanations, see [BRANCH_PROTECTION.md](BRANCH_PROTECTION.md).

### Step 1: Open Branch Protection Settings

1. Go to: `https://github.com/IslamKHALIL/fosswallet/settings/branches`
2. Click **"Add branch protection rule"**

### Step 2: Configure Basic Settings

**Branch name pattern:** `main`

### Step 3: Enable These Checkboxes

✅ **Require a pull request before merging**
   - Set approvals to: **1**
   - ✅ Dismiss stale pull request approvals when new commits are pushed

✅ **Require status checks to pass before merging**
   - ✅ Require branches to be up to date before merging
   - **Add these status checks** (search for each):
     - `Gradle Wrapper Validation`
     - `Lint Check`  
     - `Unit Tests`
     - `Integration Tests`
     - `Build Verification`
     - `PR Validation Complete`

✅ **Require conversation resolution before merging**

✅ **Do not allow bypassing the above settings**

### Step 4: Save

Click **"Create"** at the bottom

### Step 5: Verify

Run this command:
```bash
./scripts/verify-branch-protection.sh
```

## ✅ Done!

Your main branch is now protected. All changes must:
- Go through pull requests
- Pass all tests
- Get at least 1 approval
- Resolve all comments

## 🚀 Next Steps

Read the full guide: [BRANCH_PROTECTION.md](BRANCH_PROTECTION.md)

## ⚠️ Important Note

If you don't see the status checks in the list, you need to:
1. Create a test PR first
2. Let the workflows run
3. Then the check names will appear in the list
4. Edit the branch protection rule to add them
