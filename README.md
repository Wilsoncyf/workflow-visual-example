# Visual Workflow Orchestration System

A drag-and-drop visual workflow orchestration system built with Vue 3 + Vue Flow frontend and Spring Boot + Graph Core backend.

![Workflow Editor](images/Snipaste_2025-08-14_23-18-12.jpg)

## Features

- 🎨 **Visual Workflow Designer** - Intuitive drag-and-drop interface for creating workflows
- 🔧 **Multiple Node Types** - Input, Process, Delay, and Output nodes with configurable properties
- 💾 **Persistent Storage** - MySQL database for workflow definitions and execution history
- ⚡ **Real-time Execution** - Execute workflows and monitor their progress in real-time
- 📊 **Execution History** - Track all workflow executions with detailed status and results
- 🔄 **Auto-layout** - Automatic graph layout for better visualization

## Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.0** - Web framework
- **Spring Data JPA** - Database ORM
- **MySQL 8.0** - Data persistence
- **Graph Core** - Workflow execution engine
- **Maven** - Build tool

### Frontend
- **Vue 3** - UI framework
- **TypeScript** - Type safety
- **Vue Flow** - Flow diagram library
- **Element Plus** - UI component library
- **Axios** - HTTP client
- **Vite** - Build tool

## Prerequisites

- Java 17 or higher
- Node.js 16+ and npm
- MySQL 8.0
- Maven 3.6+

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/Wilsoncyf/workflow-visual-example.git
cd workflow-visual-example
```

### 2. Setup Database

Ensure MySQL is running on `localhost:3306` with:
- Username: `root`
- Password: `root`

The application will automatically create the database and tables.

### 3. Build and Run

#### Option 1: Using Start Script (Recommended)

```bash
# Make scripts executable
chmod +x start.sh stop.sh test-system.sh

# Start the entire system
./start.sh
```

#### Option 2: Manual Setup

**Backend:**
```bash
# Build the backend
mvn clean package -DskipTests

# Run the backend (port 8080)
java -jar target/workflow-visual-example-1.0.0.jar
```

**Frontend:**
```bash
# Navigate to frontend directory
cd workflow-frontend

# Install dependencies
npm install

# Start development server (port 5173)
npm run dev
```

### 4. Access the Application

Open your browser and navigate to: `http://localhost:5173`

## Usage Guide

### Creating a Workflow

1. Click **"New"** button to create a new workflow
2. Drag nodes from the left panel to the canvas
3. Connect nodes by dragging from output handles to input handles
4. Click on nodes to edit their properties
5. Click **"Save"** to persist the workflow

### Node Types

- **Input Node** - Entry point of the workflow, provides initial data
- **Process Node** - Performs data transformation or business logic
- **Delay Node** - Simulates time-consuming operations (1 second delay)
- **Output Node** - Final node that collects and outputs results

### Executing a Workflow

1. Select or create a workflow
2. Click **"Execute"** button
3. Monitor execution progress in the left panel
4. View execution results and status updates

## API Documentation

### Workflow Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/workflows` | Get all workflows |
| POST | `/api/workflows` | Create a new workflow |
| GET | `/api/workflows/{id}` | Get workflow details |
| PUT | `/api/workflows/{id}` | Update a workflow |
| DELETE | `/api/workflows/{id}` | Delete a workflow |

### Workflow Execution

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/workflows/{id}/execute` | Execute a workflow |
| GET | `/api/workflows/executions/{id}` | Get execution status |

## Project Structure

```
workflow-visual-example/
├── src/main/java/                 # Backend source code
│   └── com/alibaba/cloud/ai/workflow/
│       ├── controller/             # REST API controllers
│       ├── service/                # Business logic
│       ├── repository/             # Data access layer
│       ├── entity/                 # JPA entities
│       ├── model/                  # Data models
│       └── config/                 # Configuration
├── workflow-frontend/              # Frontend application
│   ├── src/
│   │   ├── App.vue                # Main component
│   │   ├── components/            # Vue components
│   │   └── utils/                 # Utility functions
│   └── package.json               # Frontend dependencies
├── pom.xml                        # Backend dependencies
└── start.sh                       # System startup script
```

## Development

### Backend Development

```bash
# Run tests
mvn test

# Run with hot reload
mvn spring-boot:run

# Build JAR
mvn clean package
```

### Frontend Development

```bash
cd workflow-frontend

# Type checking
npm run build

# Development with hot reload
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

## Troubleshooting

### Database Connection Issues

If you encounter database connection errors:

1. Verify MySQL is running:
   ```bash
   mysql -u root -proot -e "SELECT 1;"
   ```

2. Check MySQL configuration in `application.yml`

3. Ensure database user has proper permissions

### Port Conflicts

- Backend runs on port 8080
- Frontend runs on port 5173

Change ports in:
- Backend: `src/main/resources/application.yml`
- Frontend: `workflow-frontend/vite.config.ts`

### Testing the System

Use the diagnostic script:
```bash
./test-system.sh
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.

## Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- UI powered by [Vue Flow](https://vueflow.dev/)
- Component library by [Element Plus](https://element-plus.org/)

## Contact

Wilson Cyf - [GitHub](https://github.com/Wilsoncyf)

Project Link: [https://github.com/Wilsoncyf/workflow-visual-example](https://github.com/Wilsoncyf/workflow-visual-example)