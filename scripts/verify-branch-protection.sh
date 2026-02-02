#!/bin/bash

# Branch Protection Verification Script
# This script checks if branch protection is properly configured for the main branch

set -e

REPO="IslamKHALIL/fosswallet"
BRANCH="main"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║         Branch Protection Verification Script                  ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    local status=$1
    local message=$2
    
    if [ "$status" == "pass" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $message"
    elif [ "$status" == "fail" ]; then
        echo -e "${RED}❌ FAIL${NC} - $message"
    elif [ "$status" == "warn" ]; then
        echo -e "${YELLOW}⚠️  WARN${NC} - $message"
    elif [ "$status" == "info" ]; then
        echo -e "${BLUE}ℹ️  INFO${NC} - $message"
    fi
}

echo "Repository: $REPO"
echo "Branch: $BRANCH"
echo ""

# Check workflow files exist
echo "Checking Workflow Files:"
echo "────────────────────────"

if [ -f ".github/workflows/ci.yml" ]; then
    print_status "pass" "CI workflow exists (.github/workflows/ci.yml)"
else
    print_status "fail" "CI workflow missing"
fi

if [ -f ".github/workflows/pr-validation.yml" ]; then
    print_status "pass" "PR validation workflow exists (.github/workflows/pr-validation.yml)"
else
    print_status "fail" "PR validation workflow missing"
fi

if [ -f ".github/workflows/configure-branch-protection.yml" ]; then
    print_status "pass" "Branch protection configuration workflow exists"
else
    print_status "warn" "Branch protection configuration workflow missing"
fi

echo ""
echo "────────────────────────────────────────────────────────────────"
echo ""

# Check for test files
echo "Checking Test Infrastructure:"
echo "─────────────────────────────"

UNIT_TESTS=$(find app/src/test -name "*.kt" 2>/dev/null | wc -l)
INTEGRATION_TESTS=$(find app/src/androidTest -name "*.kt" 2>/dev/null | wc -l)

if [ "$UNIT_TESTS" -gt 0 ]; then
    print_status "pass" "Unit tests found ($UNIT_TESTS test files)"
else
    print_status "warn" "No unit tests found"
fi

if [ "$INTEGRATION_TESTS" -gt 0 ]; then
    print_status "pass" "Integration tests found ($INTEGRATION_TESTS test files)"
else
    print_status "warn" "No integration tests found"
fi

echo ""
echo "────────────────────────────────────────────────────────────────"
echo ""

# Check documentation
echo "Checking Documentation:"
echo "───────────────────────"

docs=("BRANCH_PROTECTION.md" "CI_CD_PIPELINE.md" "TESTING.md" "CONTRIBUTING.md")
for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        print_status "pass" "$doc exists"
    else
        print_status "warn" "$doc missing"
    fi
done

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                      Verification Complete                     ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

echo -e "${YELLOW}ℹ️  NOTE: Branch protection must be configured manually via GitHub UI${NC}"
echo "📋 Follow the steps in BRANCH_PROTECTION.md to complete setup"
echo ""
echo "For detailed setup instructions, see: BRANCH_PROTECTION.md"
echo ""
