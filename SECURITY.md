# Security Policy

## Reporting a Vulnerability

**Do not open a public issue for security vulnerabilities.**

If you discover a security vulnerability in KTAR, please report it responsibly via:

### Private Vulnerability Reporting (Recommended)

GitHub provides a private vulnerability reporting feature:

1. Go to the [Security tab](https://github.com/felipemacedo1/ktar/security)
2. Click "Report a vulnerability"
3. Provide detailed information about the vulnerability
4. Submit the report

### Email

If the above option is not available, email the maintainer at: **[your-email@example.com]**

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if available)

## Vulnerability Disclosure Timeline

We follow a 90-day responsible disclosure timeline:

1. **Day 0**: Vulnerability reported
2. **Day 1-7**: Initial assessment and acknowledgment
3. **Day 8-89**: Development of patch and release preparation
4. **Day 90**: Public disclosure (if not already fixed)

## Security Considerations

### SSH Connections

- KTAR uses **SSHJ 0.38.0** for SSH client implementation
- All connections use **TLS/SSH protocol security standards**
- Host key verification prevents MITM attacks
- Support for RSA and ED25519 key algorithms

### Credential Storage

- **Private keys**: Encrypted with Android Keystore (AES-GCM)
- **Passwords**: Never stored in plaintext
- **DataStore**: Uses encrypted preferences
- **Memory**: Sensitive data cleared after use

### Permissions

KTAR requests only necessary permissions:
- `INTERNET`: SSH connections
- `READ_EXTERNAL_STORAGE`: File operations (scoped storage compliant)

### Supported Versions

| Version | Supported          |
|---------|-------------------|
| Latest  | ✅ Yes             |
| Previous| ✅ 3 months        |
| Older   | ❌ Not supported   |

## Security Best Practices for Users

1. **Keep KTAR updated**: Always use the latest version
2. **Verify host keys**: Accept host keys only from trusted servers
3. **Use strong credentials**: Prefer ED25519 keys over passwords
4. **Secure your device**: Use device-level encryption
5. **Review permissions**: Only grant necessary permissions

## Dependency Security

KTAR uses **Snyk** and **OWASP Dependency-Check** to identify vulnerable dependencies.

Current dependencies are checked regularly for known vulnerabilities.

## Compliance

- ✅ OWASP Top 10 secure coding practices
- ✅ Android Security & Privacy Year Class guidelines
- ✅ Secure data storage patterns
- ✅ Safe SSH implementation

## Questions?

For security-related questions (not vulnerabilities), feel free to open a discussion or issue.

---

**Thank you for helping keep KTAR secure! 🔒**
