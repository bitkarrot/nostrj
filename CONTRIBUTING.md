# Contributing to NostrJ

Thank you for your interest in contributing to NostrJ! This document provides guidelines and instructions for contributing.

## Development Setup

### Prerequisites

- Java 17 or later
- Git

### Clone and Build

```bash
git clone https://github.com/yourusername/nostrj.git
cd nostrj
mvn clean install
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn -pl nostrj-core test
mvn -pl nostrj-client test
mvn -pl nostrj-server test
```

## Project Structure

```
nostrj/
├── nostrj-core/          # Core Nostr functionality
├── nostrj-client/        # Client library for relay connections
├── nostrj-server/        # Server components for building relays
├── nostrj-relay-app/     # Complete relay application
├── pom.xml               # Root build configuration
└── (module pom.xml)      # Module configurations
```

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and concise
- Write tests for new functionality

## Making Changes

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Write clean, well-documented code
   - Add tests for new functionality
   - Update documentation as needed

4. **Test your changes**
   ```bash
   mvn test
   mvn clean install
   ```

5. **Commit your changes**
   ```bash
   git add .
   git commit -m "Add feature: description of your changes"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Create a Pull Request**
   - Provide a clear description of the changes
   - Reference any related issues
   - Ensure all tests pass

## Areas for Contribution

### High Priority

- Additional NIP implementations
- Performance optimizations
- Documentation improvements
- Example applications
- Integration tests

### Module-Specific Contributions

#### nostrj-core
- Additional cryptographic operations
- Event validation improvements
- Support for more event kinds

#### nostrj-client
- Connection pooling
- Automatic reconnection
- Rate limiting
- Caching strategies

#### nostrj-server
- Alternative database backends (PostgreSQL, MongoDB)
- Event indexing optimizations
- Relay metrics and monitoring
- Advanced filtering capabilities

#### nostrj-relay-app
- Admin dashboard
- Configuration UI
- Relay statistics
- Moderation tools

## Testing Guidelines

- Write unit tests for all new functionality
- Ensure tests are deterministic and isolated
- Use meaningful test names that describe the scenario
- Test both success and failure cases

Example test structure:
```java
@Test
void testFeatureName_whenCondition_thenExpectedBehavior() {
    // Arrange
    // Act
    // Assert
}
```

## Documentation

- Update README.md for major features
- Add examples to EXAMPLES.md
- Include JavaDoc for public APIs
- Update CHANGELOG.md (if exists)

## Reporting Issues

When reporting issues, please include:

- NostrJ version
- Java version
- Operating system
- Steps to reproduce
- Expected behavior
- Actual behavior
- Error messages or stack traces

## Questions?

Feel free to open an issue for questions or discussions about:
- Feature requests
- Design decisions
- Implementation approaches
- Best practices

## License

By contributing to NostrJ, you agree that your contributions will be licensed under the Apache License 2.0.
