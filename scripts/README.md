# Scripts

Automation scripts for the KTAR project.

## 📁 Available Scripts

### 🔄 `sync_sonar_issues.sh`

Synchronizes SonarCloud issues to GitHub Issues automatically.

**Usage:**
```bash
# Basic usage
export SONAR_ORG="felipemacedo1"
export SONAR_PROJECT="felipemacedo1_ktar"
export SONAR_TOKEN="your-sonar-token"
./sync_sonar_issues.sh

# Dry run (preview only)
./sync_sonar_issues.sh --dry-run

# Filter by severity
./sync_sonar_issues.sh --severities=BLOCKER,CRITICAL

# Filter by type
./sync_sonar_issues.sh --types=BUG,VULNERABILITY

# Combined filters
./sync_sonar_issues.sh --severities=BLOCKER,CRITICAL --types=BUG
```

**Required environment variables:**
- `SONAR_ORG` - SonarCloud organization key
- `SONAR_PROJECT` - SonarCloud project key
- `SONAR_TOKEN` - SonarCloud API token
- `GH_TOKEN` - GitHub token (optional if using `gh auth login`)

**See:** [docs/SONARCLOUD_SYNC.md](../docs/SONARCLOUD_SYNC.md) for full documentation.

---

### 🔧 `setup_sonar_sync.sh`

One-time setup script for SonarCloud sync automation.

**What it does:**
- ✅ Validates GitHub CLI installation and authentication
- ✅ Creates required GitHub labels
- ✅ Verifies script permissions
- ✅ Provides guidance for secrets configuration

**Usage:**
```bash
# Run once to setup everything
./setup_sonar_sync.sh
```

**Prerequisites:**
- GitHub CLI (`gh`) installed and authenticated
- Repository access permissions

---

## 🔐 Security Notes

- **Never commit tokens** to the repository
- Store tokens in GitHub Secrets for CI/CD
- Use environment variables for local execution
- Tokens should have appropriate scopes:
  - SonarCloud: Read-only access to issues
  - GitHub: `repo` and `write:issues` permissions

---

## 🤝 Contributing

When adding new scripts:

1. Make them executable: `chmod +x script_name.sh`
2. Add shebang: `#!/bin/bash`
3. Include usage documentation in header comments
4. Add entry to this README
5. Validate syntax: `bash -n script_name.sh`
6. Test thoroughly before committing

---

## 📚 Related Documentation

- [SonarCloud Sync Guide](../docs/SONARCLOUD_SYNC.md)
- [CI/CD Documentation](../docs/CI_CD_FIX_REPORT.md)
- [Contributing Guidelines](../README.md#-contribuindo)
