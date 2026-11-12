# Contributing to FaultCall

Thank you for your interest in contributing to **FaultCall**!

## Code of Conduct
Please be polite, respectful, and constructive in all discussions, issues, and pull requests.

## How to Contribute
1. **Fork** the repository and create your branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Make your changes**:
   - Keep shared game rules inside `:shared`.
   - Keep phone-specific UI and Android audio services inside `:mobile`.
   - Keep watch-specific Wear Compose controls inside `:wear`.
3. **Run all tests**:
   ```bash
   ./gradlew :shared:test
   ```
4. **Build APKs**:
   ```bash
   ./gradlew assembleDebug
   ```
5. **Open a Pull Request** describing your changes, motivation, and test verification.

## Architecture Guidelines
- **Zero audio focus leaks**: Never request audio focus directly inside voice providers; audio focus must always be centrally managed in `AnnouncementManager`.
- **Pure domain logic in `:shared`**: Do not introduce Android framework dependencies (`android.*`) into the `:shared` module.
