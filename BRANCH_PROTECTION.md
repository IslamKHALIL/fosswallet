# Branch Protection Setup Guide

This guide explains how to enable and configure branch protection for the `main` branch to ensure all code is merged through approved pull requests.

## 🎯 Purpose

Branch protection ensures:
- All code changes go through pull requests
- All CI tests must pass before merging
- Code is reviewed before merging
- Direct pushes to main are prevented
- Force pushes are disabled

## 🚀 Quick Setup (Manual - Recommended)

### Step 1: Navigate to Branch Protection Settings

1. Go to your repository on GitHub: `https://github.com/IslamKHALIL/fosswallet`
2. Click on **Settings** (tab at the top)
3. Click on **Branches** (in the left sidebar under "Code and automation")
4. Click **Add branch protection rule** (or **Add rule**)

### Step 2: Configure the Rule

#### Basic Settings

1. **Branch name pattern**: Enter `main`
   - This applies the rule to the main branch

#### Required Settings (Must Enable)

✅ **Require a pull request before merging**
   - Check this box
   - Set **Required approving reviews**: `1`
   - ✅ Check **Dismiss stale pull request approvals when new commits are pushed**
   - ✅ Check **Require review from Code Owners** (optional, if you have CODEOWNERS file)

✅ **Require status checks to pass before merging**
   - Check this box
   - ✅ Check **Require branches to be up to date before merging**
   - Search and select the following status checks:
     - `Gradle Wrapper Validation`
     - `Lint Check`
     - `Unit Tests`
     - `Integration Tests`
     - `Build Verification`
     - `PR Validation Complete`
   
   **Note**: These status checks will only appear in the list after they have run at least once. You may need to create a test PR first to populate this list.

✅ **Require conversation resolution before merging**
   - Check this box (ensures all PR comments are resolved)

✅ **Do not allow bypassing the above settings**
   - Check this box (enforces rules for admins too)
   - This ensures even repository administrators must follow the rules

#### Additional Recommended Settings

✅ **Require linear history** (optional but recommended)
   - Prevents merge commits, enforces rebase or squash merging

❌ **Allow force pushes** - Keep DISABLED
   - Prevents rewriting history on main branch

❌ **Allow deletions** - Keep DISABLED
   - Prevents accidental deletion of main branch

### Step 3: Save the Rule

1. Scroll to the bottom
2. Click **Create** (or **Save changes**)
3. ✅ Your main branch is now protected!

## 🤖 Automated Setup (Alternative Method)

### Option 1: Using GitHub API with Personal Access Token

1. **Create a Personal Access Token**:
   - Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Click **Generate new token (classic)**
   - Select scopes: `repo` (full control)
   - Generate and copy the token

2. **Add Token as Repository Secret**:
   - Go to your repository → Settings → Secrets and variables → Actions
   - Click **New repository secret**
   - Name: `BRANCH_PROTECTION_TOKEN`
   - Value: Paste your token
   - Click **Add secret**

3. **Run the Configuration Workflow**:
   - Go to Actions tab
   - Select "Configure Branch Protection" workflow
   - Click "Run workflow"
   - Select branch: `main`
   - Click "Run workflow"

### Option 2: Using GitHub CLI

```bash
# Install GitHub CLI if not already installed
# Visit: https://cli.github.com/

# Authenticate
gh auth login

# Configure branch protection
gh api repos/IslamKHALIL/fosswallet/branches/main/protection \
  --method PUT \
  -H "Accept: application/vnd.github.v3+json" \
  -f required_status_checks[strict]=true \
  -f 'required_status_checks[contexts][]=Gradle Wrapper Validation' \
  -f 'required_status_checks[contexts][]=Lint Check' \
  -f 'required_status_checks[contexts][]=Unit Tests' \
  -f 'required_status_checks[contexts][]=Integration Tests' \
  -f 'required_status_checks[contexts][]=Build Verification' \
  -f 'required_status_checks[contexts][]=PR Validation Complete' \
  -f enforce_admins=true \
  -f required_pull_request_reviews[dismiss_stale_reviews]=true \
  -f required_pull_request_reviews[required_approving_review_count]=1 \
  -f required_linear_history=false \
  -f allow_force_pushes=false \
  -f allow_deletions=false \
  -f required_conversation_resolution=true
```

## ✅ Verification

### Check if Branch Protection is Enabled

**Method 1: GitHub UI**
1. Go to Settings → Branches
2. You should see "main" listed under "Branch protection rules"
3. Click "Edit" to review the settings

**Method 2: GitHub API**
```bash
curl -H "Authorization: token YOUR_TOKEN" \
  https://api.github.com/repos/IslamKHALIL/fosswallet/branches/main/protection
```

**Method 3: Test It**
1. Try to push directly to main:
   ```bash
   git checkout main
   git commit --allow-empty -m "Test direct push"
   git push origin main
   ```
   ❌ This should be **rejected** if branch protection is working

2. Create a PR instead:
   ```bash
   git checkout -b test-branch-protection
   git commit --allow-empty -m "Test branch protection"
   git push origin test-branch-protection
   ```
   ✅ Then create a PR on GitHub - this should work

## 🔍 Required Status Checks Reference

The following status checks must pass before merging:

| Status Check Name | Source Workflow | Purpose |
|-------------------|----------------|---------|
| `Gradle Wrapper Validation` | pr-validation.yml | Validates Gradle wrapper security |
| `Lint Check` | pr-validation.yml | Ensures code quality standards |
| `Unit Tests` | pr-validation.yml | Runs all unit tests |
| `Integration Tests` | pr-validation.yml | Runs database and integration tests |
| `Build Verification` | pr-validation.yml | Ensures the app builds successfully |
| `PR Validation Complete` | pr-validation.yml | All checks passed summary |

## 🎯 What This Protects Against

✅ **Direct pushes to main** - All changes must go through PR  
✅ **Untested code** - All tests must pass  
✅ **Code quality issues** - Lint must pass  
✅ **Build failures** - Build must succeed  
✅ **Unreviewed code** - At least 1 approval required  
✅ **Force pushes** - History cannot be rewritten  
✅ **Branch deletion** - Main branch cannot be deleted  

## 🚫 What You Can No Longer Do

Once branch protection is enabled:

❌ Push directly to main branch  
❌ Merge PRs without passing tests  
❌ Merge PRs without approval  
❌ Force push to main  
❌ Delete main branch  

## 📝 Workflow After Enabling Protection

### For New Changes

1. **Create feature branch**:
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make changes and commit**:
   ```bash
   git add .
   git commit -m "Add my feature"
   ```

3. **Push to GitHub**:
   ```bash
   git push origin feature/my-feature
   ```

4. **Create Pull Request**:
   - Go to GitHub
   - Click "Compare & pull request"
   - Fill in description
   - Click "Create pull request"

5. **Wait for CI**:
   - All status checks must pass (green ✅)
   - Fix any failures and push again

6. **Request Review** (or self-approve if you're the owner):
   - Click "Request reviewers"
   - Or if you're the owner with sufficient permissions, approve yourself

7. **Merge**:
   - Once approved and all checks pass
   - Click "Merge pull request"
   - Choose merge strategy (squash, rebase, or merge commit)

### For Urgent Fixes

Even urgent fixes must go through the PR process. However, you can:
- Enable "Draft PRs" and mark as "Ready for review" quickly
- Use "Squash and merge" to keep history clean
- Request expedited review

**Note**: As the repository owner, if you have "Do not allow bypassing the above settings" unchecked, you can override protection rules in emergencies. However, this is NOT recommended.

## 🔧 Troubleshooting

### Issue: Status checks not appearing in the list

**Solution**: The status checks only appear after they've run at least once. 
1. Create a test PR first
2. Let the workflows run
3. Then the status check names will be available to select

### Issue: Can't merge even though all checks pass

**Possible causes**:
- Branch is not up to date with main
- Missing required review
- Some status checks haven't reported yet

**Solution**:
- Update your branch: `git pull origin main`
- Request a review from yourself or another collaborator
- Wait for all status checks to complete

### Issue: Accidentally locked yourself out

**Solution**:
- Go to Settings → Branches
- Edit the branch protection rule
- Temporarily disable "Enforce for administrators"
- Make your changes
- Re-enable the protection

## 📚 Additional Resources

- [GitHub Branch Protection Documentation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/defining-the-mergeability-of-pull-requests/about-protected-branches)
- [GitHub Status Checks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

## ⚡ Quick Command Reference

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes, commit, and push
git add .
git commit -m "Description of changes"
git push origin feature/my-feature

# Update branch with main
git checkout main
git pull origin main
git checkout feature/my-feature
git merge main

# Or rebase instead
git rebase main
```

---

## 🎉 Success!

Once branch protection is enabled, your main branch is protected and all changes must go through the proper review and testing process!

For questions or issues, refer to the [CI/CD Pipeline Documentation](CI_CD_PIPELINE.md) or [Testing Guide](TESTING.md).
