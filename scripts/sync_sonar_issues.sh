#!/bin/bash

################################################################################
# SonarCloud to GitHub Issues Synchronization Script
# 
# This script fetches issues from SonarCloud API and creates corresponding
# GitHub Issues automatically, avoiding duplicates and applying proper labels.
#
# Usage:
#   ./sync_sonar_issues.sh [OPTIONS]
#
# Options:
#   --severities=BLOCKER,CRITICAL   Filter by severity levels
#   --types=BUG,VULNERABILITY       Filter by issue types
#   --dry-run                       Show what would be created without creating
#
# Required environment variables:
#   SONAR_ORG       SonarCloud organization key
#   SONAR_PROJECT   SonarCloud project key
#   SONAR_TOKEN     SonarCloud API token
#   GH_TOKEN        GitHub token (or use gh auth)
################################################################################

set -euo pipefail

# Colors for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Configuration
readonly SONAR_API_URL="https://sonarcloud.io/api"
readonly ISSUES_PER_PAGE=500

# Default filters
SEVERITIES="${SEVERITIES:-BLOCKER,CRITICAL,MAJOR}"
TYPES="${TYPES:-BUG,VULNERABILITY,CODE_SMELL,SECURITY_HOTSPOT}"
DRY_RUN=false

# Counters
TOTAL_SONAR_ISSUES=0
CREATED_ISSUES=0
SKIPPED_ISSUES=0
ERRORS=0

################################################################################
# Functions
################################################################################

log_info() {
    echo -e "${BLUE}ℹ${NC} $*"
}

log_success() {
    echo -e "${GREEN}✓${NC} $*"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $*"
}

log_error() {
    echo -e "${RED}✗${NC} $*" >&2
}

check_dependencies() {
    local missing_deps=()
    
    for cmd in curl jq gh; do
        if ! command -v "$cmd" &> /dev/null; then
            missing_deps+=("$cmd")
        fi
    done
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        log_error "Missing required dependencies: ${missing_deps[*]}"
        log_info "Install them with:"
        log_info "  sudo apt-get install curl jq"
        log_info "  curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg"
        log_info "  echo \"deb [arch=\$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main\" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null"
        log_info "  sudo apt-get update && sudo apt-get install gh"
        exit 1
    fi
}

check_env_vars() {
    local missing_vars=()
    
    for var in SONAR_ORG SONAR_PROJECT SONAR_TOKEN; do
        if [ -z "${!var:-}" ]; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        log_error "Missing required environment variables: ${missing_vars[*]}"
        log_info "Set them with:"
        log_info "  export SONAR_ORG='your-org'"
        log_info "  export SONAR_PROJECT='your-project'"
        log_info "  export SONAR_TOKEN='your-token'"
        exit 1
    fi
}

parse_args() {
    for arg in "$@"; do
        case $arg in
            --severities=*)
                SEVERITIES="${arg#*=}"
                ;;
            --types=*)
                TYPES="${arg#*=}"
                ;;
            --dry-run)
                DRY_RUN=true
                ;;
            --help)
                head -n 25 "$0" | grep "^#" | sed 's/^# *//'
                exit 0
                ;;
            *)
                log_error "Unknown argument: $arg"
                log_info "Use --help for usage information"
                exit 1
                ;;
        esac
    done
}

fetch_sonar_issues() {
    log_info "Fetching issues from SonarCloud..."
    log_info "Organization: $SONAR_ORG"
    log_info "Project: $SONAR_PROJECT"
    log_info "Filters - Severities: $SEVERITIES, Types: $TYPES"
    
    local response
    response=$(curl -s -u "${SONAR_TOKEN}:" \
        "${SONAR_API_URL}/issues/search?componentKeys=${SONAR_ORG}_${SONAR_PROJECT}&severities=${SEVERITIES}&types=${TYPES}&ps=${ISSUES_PER_PAGE}&resolved=false" \
        2>&1)
    
    if [ $? -ne 0 ]; then
        log_error "Failed to fetch issues from SonarCloud"
        log_error "Response: $response"
        exit 1
    fi
    
    # Check if response is valid JSON
    if ! echo "$response" | jq empty 2>/dev/null; then
        log_error "Invalid JSON response from SonarCloud API"
        log_error "Response: $response"
        exit 1
    fi
    
    echo "$response"
}

issue_exists_in_github() {
    local title="$1"
    
    # Search for existing issues with the same title and sonarcloud label
    local existing
    existing=$(gh issue list --label "sonarcloud" --state open --search "$title" --json title,number --jq ".[] | select(.title == \"$title\") | .number" 2>/dev/null || echo "")
    
    if [ -n "$existing" ]; then
        return 0  # exists
    else
        return 1  # doesn't exist
    fi
}

get_severity_emoji() {
    case "$1" in
        BLOCKER)    echo "🚨" ;;
        CRITICAL)   echo "🔴" ;;
        MAJOR)      echo "🟠" ;;
        MINOR)      echo "🟡" ;;
        INFO)       echo "ℹ️" ;;
        *)          echo "📌" ;;
    esac
}

get_type_emoji() {
    case "$1" in
        BUG)                echo "🐛" ;;
        VULNERABILITY)      echo "🔓" ;;
        CODE_SMELL)         echo "👃" ;;
        SECURITY_HOTSPOT)   echo "🔥" ;;
        *)                  echo "📝" ;;
    esac
}

sanitize_for_github() {
    # Escape characters that might break GitHub markdown
    echo "$1" | sed 's/`/\\`/g'
}

create_github_issue() {
    local issue_data="$1"
    
    local rule message component line severity type effort
    rule=$(echo "$issue_data" | jq -r '.rule // "N/A"')
    message=$(echo "$issue_data" | jq -r '.message // "No message"')
    component=$(echo "$issue_data" | jq -r '.component // "N/A"' | sed "s/${SONAR_ORG}_${SONAR_PROJECT}://")
    line=$(echo "$issue_data" | jq -r '.line // "N/A"')
    severity=$(echo "$issue_data" | jq -r '.severity // "UNKNOWN"')
    type=$(echo "$issue_data" | jq -r '.type // "UNKNOWN"')
    effort=$(echo "$issue_data" | jq -r '.effort // "N/A"')
    
    local severity_emoji=$(get_severity_emoji "$severity")
    local type_emoji=$(get_type_emoji "$type")
    
    # Create title
    local title="[SonarCloud] [$severity] $message"
    
    # Truncate title if too long (GitHub limit is 256 chars)
    if [ ${#title} -gt 200 ]; then
        title="${title:0:197}..."
    fi
    
    # Check if issue already exists
    if issue_exists_in_github "$title"; then
        log_warning "Issue already exists: $title"
        ((SKIPPED_ISSUES++))
        return 0
    fi
    
    # Build issue body
    local body
    body=$(cat <<EOF
## $severity_emoji $type_emoji SonarCloud Issue

**Severity:** \`$severity\`  
**Type:** \`$type\`  
**Rule:** \`$rule\`  
**Effort:** \`$effort\`

---

### 📄 Location
**File:** \`$component\`  
**Line:** \`$line\`

---

### 📋 Message
$(sanitize_for_github "$message")

---

### 🔗 Links
- [View in SonarCloud](https://sonarcloud.io/project/issues?open=$(echo "$issue_data" | jq -r '.key')&id=${SONAR_ORG}_${SONAR_PROJECT})
- [Rule Documentation](https://rules.sonarsource.com/java/RSPEC-${rule#*:})

---

> 🤖 This issue was automatically created by SonarCloud sync workflow.  
> 📅 Created: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
EOF
)
    
    # Determine labels
    local labels="sonarcloud"
    
    case "$type" in
        BUG)                labels="$labels,bug" ;;
        VULNERABILITY)      labels="$labels,security,vulnerability" ;;
        CODE_SMELL)         labels="$labels,code-quality" ;;
        SECURITY_HOTSPOT)   labels="$labels,security,security-hotspot" ;;
    esac
    
    case "$severity" in
        BLOCKER|CRITICAL)   labels="$labels,priority:high" ;;
        MAJOR)              labels="$labels,priority:medium" ;;
        MINOR|INFO)         labels="$labels,priority:low" ;;
    esac
    
    # Create the issue
    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY RUN] Would create issue: $title"
        log_info "[DRY RUN] Labels: $labels"
        ((CREATED_ISSUES++))
    else
        log_info "Creating issue: $title"
        
        if gh issue create \
            --title "$title" \
            --body "$body" \
            --label "$labels" > /dev/null 2>&1; then
            log_success "Created issue: $title"
            ((CREATED_ISSUES++))
        else
            log_error "Failed to create issue: $title"
            ((ERRORS++))
        fi
    fi
}

print_summary() {
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  📊 Synchronization Summary"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "  SonarCloud Issues Found:  $TOTAL_SONAR_ISSUES"
    echo "  GitHub Issues Created:    $CREATED_ISSUES"
    echo "  Skipped (duplicates):     $SKIPPED_ISSUES"
    echo "  Errors:                   $ERRORS"
    echo ""
    
    if [ "$DRY_RUN" = true ]; then
        echo "  ⚠️  DRY RUN MODE - No issues were actually created"
        echo ""
    fi
    
    if [ $ERRORS -eq 0 ]; then
        echo -e "  ${GREEN}✓ Synchronization completed successfully${NC}"
    else
        echo -e "  ${YELLOW}⚠ Synchronization completed with errors${NC}"
    fi
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
}

################################################################################
# Main execution
################################################################################

main() {
    log_info "🔄 Starting SonarCloud → GitHub Issues synchronization..."
    echo ""
    
    # Parse arguments
    parse_args "$@"
    
    # Check prerequisites
    check_dependencies
    check_env_vars
    
    # Fetch issues from SonarCloud
    local sonar_response
    sonar_response=$(fetch_sonar_issues)
    
    # Parse total count
    TOTAL_SONAR_ISSUES=$(echo "$sonar_response" | jq -r '.total // 0')
    
    log_success "Found $TOTAL_SONAR_ISSUES issues in SonarCloud"
    echo ""
    
    if [ "$TOTAL_SONAR_ISSUES" -eq 0 ]; then
        log_success "No issues to sync! 🎉"
        print_summary
        exit 0
    fi
    
    # Process each issue
    local issue_count=0
    while IFS= read -r issue; do
        ((issue_count++))
        log_info "Processing issue $issue_count/$TOTAL_SONAR_ISSUES..."
        create_github_issue "$issue"
    done < <(echo "$sonar_response" | jq -c '.issues[]')
    
    # Print summary
    print_summary
    
    # Exit with appropriate code
    if [ $ERRORS -gt 0 ]; then
        exit 1
    fi
}

# Run main function
main "$@"
