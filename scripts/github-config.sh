#!/usr/bin/env bash
set -euo pipefail

# Usage:
# GITHUB_OWNER=felipemacedo1 GITHUB_REPO=ktar ./scripts/github-config.sh
# Requires: gh (GitHub CLI) v2+, jq
# Auth: gh auth login (or set GITHUB_TOKEN/PERSONAL_ACCESS_TOKEN with repo admin permissions)

OWNER="${GITHUB_OWNER:-}"
REPO="${GITHUB_REPO:-}"

if [[ -z "$OWNER" || -z "$REPO" ]]; then
  echo "Usage: GITHUB_OWNER=owner GITHUB_REPO=repo $0"
  exit 2
fi

FULL="${OWNER}/${REPO}"

DESCRIPTION="Professional SSH terminal for Android with PTY support, SFTP file transfer, and secure credential storage. Built with Kotlin & Jetpack Compose."
TOPICS=(android ssh terminal kotlin jetpack-compose open-source sftp pty)

echo "Updating repository description and homepage..."
if gh repo edit "$FULL" --description "$DESCRIPTION" 2>/dev/null; then
  echo "Description updated via gh." 
else
  echo "gh repo edit failed; falling back to API..."
  gh api -X PATCH "repos/$FULL" -f description="$DESCRIPTION" || echo "Failed to set description via API. Check permissions."
fi

echo "Setting topics..."
# Use API to set topics (stable across gh versions)
jq -n --argjson arr "$(printf '%s\n' "${TOPICS[@]}" | jq -R . | jq -s .)" '{names: $arr}' > /tmp/topics.json
# Build proper JSON for gh api
BODY=$(jq -nc --argjson names "$(printf '%s\n' "${TOPICS[@]}" | jq -R . | jq -s .)" '{names: $names}') || true
if gh api -X PUT "repos/$FULL/topics" -H "Accept: application/vnd.github.mercy-preview+json" -f body="$BODY" 2>/dev/null; then
  echo "Topics updated via API."
else
  echo "Could not set topics via gh/api. Please add manually in repository settings."
fi

echo "Enabling Discussions (if possible)..."
if gh repo edit "$FULL" --enable-discussions 2>/dev/null; then
  echo "Discussions enabled via gh repo edit."
else
  if gh api -X PATCH "repos/$FULL" -f has_discussions=true 2>/dev/null; then
    echo "Discussions enabled via API patch."
  else
    echo "Could not enable Discussions via API. Please enable via repository Settings -> Features -> Discussions."
  fi
fi

echo "Creating branch protection rule for 'main'..."
read -r -d '' PROTECTION_JSON <<'JSON' || true
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "build",
      "sonarcloud"
    ]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "required_approving_review_count": 1
  },
  "restrictions": null,
  "required_conversation_resolution": true
}
JSON

if gh api -X PUT "repos/$FULL/branches/main/protection" -H "Accept: application/vnd.github.luke-cage-preview+json" -f body="$PROTECTION_JSON" 2>/dev/null; then
  echo "Branch protection applied to 'main'."
else
  echo "Failed to apply branch protection. Ensure the branch exists and you have admin permissions."
fi

echo "Enabling Dependabot vulnerability alerts (if possible)..."
if gh api -X PUT "repos/$FULL/vulnerability-alerts" -H "Accept: application/vnd.github.dorian-preview+json" 2>/dev/null; then
  echo "Dependabot vulnerability alerts enabled."
else
  echo "Could not enable vulnerability alerts via API. This may require organization-level settings or admin permissions."
fi

echo "Attempting to enable secret scanning..."
if gh api -X PUT "repos/$FULL/secret-scanning" -H "Accept: application/vnd.github+json" 2>/dev/null; then
  echo "Secret scanning enabled."
else
  echo "Could not enable secret scanning via API. Please enable via Settings -> Security & analysis if necessary."
fi

echo "Note: For Dependabot automatic security updates, add a .github/dependabot.yml file to configure update schedule."

echo "Done. Review changes in repository settings and branch protection."
