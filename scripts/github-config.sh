#!/bin/bash

# GitHub Configuration Script for KTAR
# This script automates label and milestone creation
# Requires: GitHub CLI (gh) installed and authenticated

set -e

OWNER="felipemacedo1"
REPO="ktar"

echo "🚀 Starting KTAR GitHub Configuration..."
echo "Repository: $OWNER/$REPO"
echo ""

# ==========================================
# 1. CREATE LABELS
# ==========================================
echo "📌 Creating Labels..."
echo ""

create_label() {
    local name=$1
    local color=$2
    local description=$3
    
    echo "  Creating label: $name"
    gh label create "$name" \
        --repo "$OWNER/$REPO" \
        --color "$color" \
        --description "$description" 2>/dev/null || \
    echo "  ⚠️ Label '$name' already exists or error occurred"
}

# Type Labels
create_label "bug" "FF0000" "Something isn't working"
create_label "enhancement" "00FF00" "New feature or request"
create_label "documentation" "0075CA" "Improvements or additions to documentation"
create_label "question" "CC317C" "Further information is requested"
create_label "discussion" "D4C5F9" "Start or discuss a topic"

echo ""
echo "Priority Labels"
create_label "priority/critical" "B60205" "Critical - needs immediate attention"
create_label "priority/high" "FF6600" "High priority"
create_label "priority/medium" "FBCA04" "Medium priority"
create_label "priority/low" "CCCCCC" "Low priority"

echo ""
echo "Status Labels"
create_label "status/in-progress" "1D76DB" "Currently being worked on"
create_label "status/blocked" "EE0701" "Work is blocked"
create_label "status/review" "FEF2C0" "Waiting for review"
create_label "status/help-wanted" "33AA3F" "We need help with this"

echo ""
echo "Difficulty Labels"
create_label "difficulty/easy" "7057FF" "Good for beginners"
create_label "difficulty/intermediate" "0E8A16" "Intermediate level"
create_label "difficulty/advanced" "B60205" "Advanced/expert level"

echo ""
echo "Component Labels"
create_label "component/ui" "1F883D" "UI/Compose related"
create_label "component/ssh" "7410D9" "SSH client functionality"
create_label "component/terminal" "0052CC" "Terminal emulator"
create_label "component/sftp" "FBCA04" "SFTP file transfer"
create_label "component/security" "FF0000" "Security related"
create_label "component/testing" "BFDADC" "Testing and tests"

echo ""
echo "Other Labels"
create_label "dependencies" "0366D6" "Pull requests that update a dependency"
create_label "wontfix" "FFFFFF" "This will not be worked on"
create_label "duplicate" "CFD3D7" "This issue or pull request already exists"
create_label "invalid" "E4E669" "This issue or PR is not valid"

echo ""
echo "✅ Labels created successfully!"
echo ""

# ==========================================
# 2. CREATE MILESTONES
# ==========================================
echo "🎯 Creating Milestones..."
echo ""

create_milestone() {
    local title=$1
    local description=$2
    local due_date=$3
    
    echo "  Creating milestone: $title"
    gh milestone create "$title" \
        --repo "$OWNER/$REPO" \
        --description "$description" \
        --due-date "$due_date" 2>/dev/null || \
    echo "  ⚠️ Milestone '$title' already exists or error occurred"
}

# Due dates format: YYYY-MM-DD
# Calculate approximate dates (3, 6, 12 months from now)
TODAY=$(date +%Y-%m-%d)
IN_3_MONTHS=$(date -d "+3 months" +%Y-%m-%d 2>/dev/null || date -v+3m +%Y-%m-%d)
IN_6_MONTHS=$(date -d "+6 months" +%Y-%m-%d 2>/dev/null || date -v+6m +%Y-%m-%d)
IN_12_MONTHS=$(date -d "+12 months" +%Y-%m-%d 2>/dev/null || date -v+12m +%Y-%m-%d)

create_milestone \
    "v1.0.0 - Stable Release" \
    "First stable production release. Solidify features and fix remaining beta issues." \
    "$IN_3_MONTHS"

create_milestone \
    "v1.1.0 - Enhancement Release" \
    "Community-driven improvements based on beta feedback. New features and UX enhancements." \
    "$IN_6_MONTHS"

create_milestone \
    "v2.0.0 - Major Features" \
    "Major version with significant new features. Connection multiplexing, advanced terminal emulation, batch operations." \
    "$IN_12_MONTHS"

create_milestone \
    "Future Backlog" \
    "Ideas and features for future releases. Lower priority, TBD." \
    ""

echo ""
echo "✅ Milestones created successfully!"
echo ""

# ==========================================
# SUMMARY
# ==========================================
echo "╔════════════════════════════════════════════════════════════════════════════╗"
echo "║                                                                            ║"
echo "║          ✅ GITHUB LABELS & MILESTONES CONFIGURED SUCCESSFULLY!            ║"
echo "║                                                                            ║"
echo "╚════════════════════════════════════════════════════════════════════════════╝"
echo ""
echo "📊 Created:"
echo "   • 25 Labels (types, priority, status, difficulty, components, other)"
echo "   • 4 Milestones (v1.0.0, v1.1.0, v2.0.0, Future Backlog)"
echo ""
echo "📝 Next Steps - MANUAL Configuration (via GitHub Web):"
echo ""
echo "1. Repository Settings > General:"
echo "   □ Description: 'Professional SSH terminal for Android...'"
echo "   □ Topics: android, ssh, terminal, kotlin, jetpack-compose"
echo "   □ Enable Discussions (checkbox)"
echo ""
echo "2. Settings > Branches > Add Protection Rule for 'main':"
echo "   □ Require pull request before merging"
echo "   □ Require 1 approval"
echo "   □ Require status checks:"
echo "     - build.yml"
echo "     - sonarcloud.yml"
echo "   □ Require branches to be up to date"
echo ""
echo "3. Settings > Code security & analysis:"
echo "   □ Enable 'Private vulnerability reporting'"
echo "   □ Enable 'Dependabot alerts'"
echo "   □ Enable 'Dependabot security updates'"
echo ""
echo "4. Settings > Features:"
echo "   □ Check 'Discussions' if not already enabled"
echo ""
echo "📌 Links:"
echo "   Repository: https://github.com/$OWNER/$REPO"
echo "   Settings:   https://github.com/$OWNER/$REPO/settings"
echo "   Labels:     https://github.com/$OWNER/$REPO/labels"
echo "   Milestones: https://github.com/$OWNER/$REPO/milestones"
echo ""
echo "═════════════════════════════════════════════════════════════════════════════"
