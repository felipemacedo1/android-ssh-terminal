#!/bin/bash

# Setup script for SonarCloud → GitHub Issues synchronization
# Run this once to configure labels and test the setup

set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  🔧 SonarCloud Sync Setup"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if gh is installed
if ! command -v gh &> /dev/null; then
    echo "❌ GitHub CLI (gh) is not installed."
    echo "   Install it from: https://cli.github.com/"
    exit 1
fi

# Check authentication
echo "🔐 Checking GitHub authentication..."
if ! gh auth status &> /dev/null; then
    echo "❌ Not authenticated with GitHub CLI"
    echo "   Run: gh auth login"
    exit 1
fi
echo "✓ Authenticated"
echo ""

# Create labels
echo "🏷️  Creating GitHub labels..."
gh label create "sonarcloud" --color "0052CC" --description "Issues from SonarCloud" --force 2>/dev/null || true
gh label create "code-quality" --color "FFA500" --description "Code quality issues" --force 2>/dev/null || true
gh label create "priority:high" --color "DC143C" --description "High priority" --force 2>/dev/null || true
gh label create "priority:medium" --color "FFA500" --description "Medium priority" --force 2>/dev/null || true
gh label create "priority:low" --color "FFFF00" --description "Low priority" --force 2>/dev/null || true
gh label create "security-hotspot" --color "FF6B6B" --description "Security hotspot review needed" --force 2>/dev/null || true
echo "✓ Labels created"
echo ""

# Check for secrets
echo "🔑 Checking required secrets..."
echo ""
echo "Please ensure these secrets are set in GitHub:"
echo "  • SONAR_TOKEN      - SonarCloud API token"
echo "  • SONAR_ORG        - SonarCloud organization (optional, defaults to felipemacedo1)"
echo "  • SONAR_PROJECT    - SonarCloud project key (optional, defaults to felipemacedo1_ktar)"
echo ""
echo "Set them at:"
echo "  https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/settings/secrets/actions"
echo ""

# Test script execution
echo "🧪 Testing sync script..."
if [ ! -x "./scripts/sync_sonar_issues.sh" ]; then
    echo "❌ Script is not executable"
    chmod +x ./scripts/sync_sonar_issues.sh
    echo "✓ Made script executable"
fi

echo "✓ Script is ready"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  ✅ Setup Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Next steps:"
echo "  1. Add SONAR_TOKEN to GitHub secrets"
echo "  2. Run workflow manually to test: gh workflow run sonar-sync.yml"
echo "  3. Check the Actions tab for results"
echo ""
echo "For more information, see: docs/SONARCLOUD_SYNC.md"
echo ""
