# Changelog

All notable changes to KTAR are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned

- Connection multiplexing
- Advanced terminal emulation
- Batch operations
- Terminal session recording

---

## [1.0.0-beta1] - 2025-10-29

### Added

- ✅ **Secure SSH connections** with SSHJ 0.38.0
- ✅ **Multiple authentication methods**: password and public key (RSA/ED25519)
- ✅ **Real terminal emulator** with PTY (pseudo-terminal) support
- ✅ **Terminal features**:
  - Persistent shell session
  - Command history
  - Output streaming and adaptive buffering
  - Support for long-running commands (tail -f, top -b, watch)
  - Environment variable persistence
- ✅ **SFTP file transfer** capabilities
- ✅ **Multiple simultaneous sessions**
- ✅ **Modern UI** built with Jetpack Compose and Material 3
- ✅ **Persistent host storage** with DataStore encryption
- ✅ **Credential encryption** via Android Keystore (AES-GCM)
- ✅ **Host key verification** with Trust On First Use (TOFU) pattern
- ✅ **Dark/Light theme support**
- ✅ **Professional CI/CD** with GitHub Actions
- ✅ **Code quality monitoring** with SonarCloud
- ✅ **Comprehensive documentation**:
  - Contributing guide
  - Development setup
  - Security policy
  - Architecture documentation

### Security

- AES-GCM encrypted storage for private keys
- No plaintext credential storage
- Proper SSH host key verification
- Secure memory handling for sensitive data

### Known Limitations (Beta)

- Terminal emulation does not support all ANSI escape sequences
- File transfer limited to SFTP protocol
- No connection profiles/templates
- Single-device support only (no cloud sync)

---

## Pre-release Versions

> Historical versions removed to establish clean versioning baseline.
> See git history for development progression: v1.0.0 → v1.2.0 → v1.3.0 → v1.4.1

---

## How to Contribute

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to submit bug reports, feature requests, and pull requests.

## Support

- 📖 [Documentation](docs/)
- 🐛 [Issue Tracker](https://github.com/felipemacedo1/ktar/issues)
- 💬 [Discussions](https://github.com/felipemacedo1/ktar/discussions)
- 🔐 [Security Issues](SECURITY.md)

---

**[Unreleased]**: Changes between current version and main branch
**[1.0.0-beta1]**: Initial beta release (2025-10-29)
