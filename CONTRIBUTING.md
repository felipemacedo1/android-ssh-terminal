# Contributing to KTAR

Thank you for your interest in contributing to KTAR! We welcome contributions from the community and appreciate your help in making this project better.

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## How to Contribute

### Reporting Bugs

Before creating a bug report, please check the issue list to avoid duplicates.

When filing a bug report, include:
- **Description**: Clear description of what the bug is
- **Steps to Reproduce**: Step-by-step instructions to reproduce the issue
- **Expected Behavior**: What you expected to happen
- **Actual Behavior**: What actually happened
- **Screenshots**: If applicable
- **Environment**: Android version, device model, KTAR version
- **Logs**: Relevant app logs (enable via Settings)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:
- Use a clear and descriptive title
- Provide a step-by-step description of the suggested enhancement
- Provide specific examples to demonstrate the steps
- Explain why this enhancement would be useful
- List other SSH clients where this feature exists, if applicable

### Pull Requests

- Fill out the required PR template
- Follow the [code style guide](#code-style)
- Reference related issues in your PR description
- Include tests for new functionality
- Update documentation as needed
- Ensure CI/CD passes (build, tests, linting)

## Development Setup

### Prerequisites

- Java 17 or higher
- Android SDK 26+ (API 26+)
- Android Gradle Plugin compatible version
- Git

### Local Setup

1. **Fork the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/ktar.git
   cd ktar
   ```

2. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on emulator or device**
   ```bash
   ./gradlew installDebug
   ```

5. **Push and create a PR**
   ```bash
   git push origin feature/your-feature-name
   ```

## Code Style

We follow Kotlin conventions and the [Android Architecture Components guidelines](https://developer.android.com/jetpack).

### Key Points

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Add comments only for complex logic
- Use Kotlin idioms (prefer `val` over `var`, use scoping functions appropriately)

### Running Linting

```bash
./gradlew lint
./gradlew ktlint
```

## Testing

- Write unit tests for business logic
- Write instrumentation tests for UI components
- Run tests before submitting PR:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Commit Conventions

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): short description

Optional longer description explaining the change.

Fixes #123
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`

**Examples**:
```
feat(terminal): add command history support
fix(auth): prevent key file descriptor leak
docs(readme): update installation instructions
```

## Security

If you discover a security vulnerability, please see [SECURITY.md](SECURITY.md).

## License

By contributing to KTAR, you agree that your contributions will be licensed under the MIT License.

## Questions?

Feel free to open an issue with the `question` label or start a discussion.

---

**Thank you for contributing to KTAR! 🚀**
