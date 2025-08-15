# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a visual workflow orchestration system combining a Vue 3 + Vue Flow frontend with a Spring Boot + Graph Core backend. The system enables drag-and-drop workflow design, execution, and monitoring with MySQL persistence.

## Common Development Commands

### Backend (Spring Boot)
```bash
# Build the backend (requires Java 17+)
mvn clean package -DskipTests

# Run the backend server (port 8080)
mvn spring-boot:run
# OR
java -jar target/workflow-visual-example-1.0.0.jar

# Run tests (if available)
mvn test
```

### Frontend (Vue 3 + TypeScript)
```bash
# Navigate to frontend directory
cd workflow-frontend

# Install dependencies
npm install

# Start development server (port 5173)
npm run dev

# Build for production
npm run build

# Type checking
vue-tsc -b

# Preview production build
npm run preview
```

### System Scripts
```bash
# Start entire system (backend + frontend)
./start.sh

# Stop all services
./stop.sh

# Test and diagnose system
./test-system.sh
```

## Architecture & Key Components

### Backend Architecture
The backend follows a standard Spring Boot layered architecture:

- **Controller Layer** (`controller/WorkflowController.java`): REST API endpoints for workflow and execution management
- **Service Layer**: 
  - `WorkflowService`: Manages workflow CRUD operations
  - `ExecutionService`: Handles workflow execution using Graph Core engine, includes node action creation and state management
- **Repository Layer**: JPA repositories for database operations
- **Entity Layer**: JPA entities for workflows and executions with JSON column support
- **Configuration**: CORS configuration in `WebConfig` for frontend communication

### Frontend Architecture
Vue 3 application with Vue Flow for visual workflow editing:

- **Main Component** (`App.vue`): Core workflow editor with drag-and-drop, node management, and execution monitoring
- **Node System**: Custom nodes (Input, Process, Delay, Output) with configurable properties
- **State Management**: Local component state for workflows, executions, and UI controls
- **API Integration**: Axios-based service calls to backend REST endpoints

### Database Schema
MySQL database (`home`) with automatic schema creation:
- `workflows`: Stores workflow definitions with JSON graph data
- `executions`: Tracks workflow execution history with inputs/outputs

### Key Dependencies
- **Graph Core**: Local JAR dependency at `../spring-ai-alibaba-graph-core/target/`
- **Database**: MySQL 8.0 with root/root credentials on localhost:3306
- **Frontend Libraries**: Vue Flow, Element Plus, Axios, Dagre (for layout)

## Important Configuration

### Database Connection
- URL: `jdbc:mysql://localhost:3306/home?createDatabaseIfNotExist=true`
- Username: `root`
- Password: `root`
- Auto-creates database and tables on startup

### API Endpoints
- Base URL: `http://localhost:8080/api`
- Key endpoints: `/workflows`, `/workflows/{id}/execute`, `/workflows/executions/{id}`

### CORS Configuration
Configured to allow requests from `http://localhost:5173` (frontend dev server)

## Development Notes

- The system uses a local Graph Core library as a system dependency - ensure it's built before running
- Frontend uses Vue Flow for the visual workflow editor with custom node components
- Workflow definitions are stored as JSON in the database
- Execution service creates node actions dynamically based on node types
- Delay nodes simulate async operations with 1-second delays
- Frontend polls for execution status updates (consider WebSocket for real-time updates)


# Development Guidelines

## Philosophy

### Core Beliefs

- **Incremental progress over big bangs** - Small changes that compile and pass tests
- **Learning from existing code** - Study and plan before implementing
- **Pragmatic over dogmatic** - Adapt to project reality
- **Clear intent over clever code** - Be boring and obvious

### Simplicity Means

- Single responsibility per function/class
- Avoid premature abstractions
- No clever tricks - choose the boring solution
- If you need to explain it, it's too complex

## Process

### 1. Planning & Staging

Break complex work into 3-5 stages. Document in `IMPLEMENTATION_PLAN.md`:

```markdown
## Stage N: [Name]
**Goal**: [Specific deliverable]
**Success Criteria**: [Testable outcomes]
**Tests**: [Specific test cases]
**Status**: [Not Started|In Progress|Complete]
```
- Update status as you progress
- Remove file when all stages are done

### 2. Implementation Flow

1. **Understand** - Study existing patterns in codebase
2. **Test** - Write test first (red)
3. **Implement** - Minimal code to pass (green)
4. **Refactor** - Clean up with tests passing
5. **Commit** - With clear message linking to plan

### 3. When Stuck (After 3 Attempts)

**CRITICAL**: Maximum 3 attempts per issue, then STOP.

1. **Document what failed**:
  - What you tried
  - Specific error messages
  - Why you think it failed

2. **Research alternatives**:
  - Find 2-3 similar implementations
  - Note different approaches used

3. **Question fundamentals**:
  - Is this the right abstraction level?
  - Can this be split into smaller problems?
  - Is there a simpler approach entirely?

4. **Try different angle**:
  - Different library/framework feature?
  - Different architectural pattern?
  - Remove abstraction instead of adding?

## Technical Standards

### Architecture Principles

- **Composition over inheritance** - Use dependency injection
- **Interfaces over singletons** - Enable testing and flexibility
- **Explicit over implicit** - Clear data flow and dependencies
- **Test-driven when possible** - Never disable tests, fix them

### Code Quality

- **Every commit must**:
  - Compile successfully
  - Pass all existing tests
  - Include tests for new functionality
  - Follow project formatting/linting

- **Before committing**:
  - Run formatters/linters
  - Self-review changes
  - Ensure commit message explains "why"

### Error Handling

- Fail fast with descriptive messages
- Include context for debugging
- Handle errors at appropriate level
- Never silently swallow exceptions

## Decision Framework

When multiple valid approaches exist, choose based on:

1. **Testability** - Can I easily test this?
2. **Readability** - Will someone understand this in 6 months?
3. **Consistency** - Does this match project patterns?
4. **Simplicity** - Is this the simplest solution that works?
5. **Reversibility** - How hard to change later?

## Project Integration

### Learning the Codebase

- Find 3 similar features/components
- Identify common patterns and conventions
- Use same libraries/utilities when possible
- Follow existing test patterns

### Tooling

- Use project's existing build system
- Use project's test framework
- Use project's formatter/linter settings
- Don't introduce new tools without strong justification

## Quality Gates

### Definition of Done

- [ ] Tests written and passing
- [ ] Code follows project conventions
- [ ] No linter/formatter warnings
- [ ] Commit messages are clear
- [ ] Implementation matches plan
- [ ] No TODOs without issue numbers

### Test Guidelines

- Test behavior, not implementation
- One assertion per test when possible
- Clear test names describing scenario
- Use existing test utilities/helpers
- Tests should be deterministic

## Important Reminders

**NEVER**:
- Use `--no-verify` to bypass commit hooks
- Disable tests instead of fixing them
- Commit code that doesn't compile
- Make assumptions - verify with existing code

**ALWAYS**:
- Commit working code incrementally
- Update plan documentation as you go
- Learn from existing implementations
- Stop after 3 failed attempts and reassess
