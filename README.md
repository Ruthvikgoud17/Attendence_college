# College Attendance Management System (Java SE & Modern Web Interface)

A complete, Object-Oriented Course-End Project built according to university attendance regulations with both a **Modern Glassmorphic Web Interface** and a **Standard Java SE Terminal Application**.

---

## 🌐 Modern Web Interface (New!)

A responsive, high-performance web dashboard featuring real-time data sync with the Java backend CSV database (`data/*.csv`):

- **👨‍🏫 Teacher Portal**:
  - **5 Faculty Teachers Pre-configured**:
    - `JAVA` — **Java Programming** (Dr. Naga Padmaja, password: `java`)
    - `PYTHON` — **Python Programming** (Dr. Rajesh, password: `python`)
    - `MATHS` — **Engineering Mathematics** (Dr. Sharma, password: `maths`)
    - `DSA` — **Data Structures & Algo** (Prof. Priya, password: `dsa`)
    - `WEB` — **Web Technologies** (Prof. Arun, password: `web`)
  - **Post Attendance (3 Marking Modes)**:
    - *Method 1*: Mark **ABSENT** Roll Numbers (comma-separated or click chips; remaining marked Present).
    - *Method 2*: Mark **PRESENT** Roll Numbers (comma-separated or click chips; remaining marked Absent).
    - *Method 3*: Interactive **Roster Cards Grid** with 1-click `P / A` toggle, bulk buttons (*All Present*, *All Absent*, *Invert*), and instant live attendance turnout calculator.
  - **Class Attendance Register**: Complete tabular register for all 30 students with search, filters (All, Eligible $\ge$ 75%, Shortage < 75%), CSV Export, and Print.
  - **Defaulters Analysis (< 75%)**: Automatic detection with shortage deficit percentage, **consecutive classes needed** using the statutory formula $\lceil(75 \times \text{Held} - 100 \times \text{Attended}) / 25\rceil$, and one-click printable formal Dean's Warning Notice.
  - **Daily Attendance Register**: Turnout rate inspector for any date.

- **🧑‍🎓 Student Portal (View Only)**:
  - **Password Protected**: Secure student login requiring Roll Number (101 to 130) and password formatted as `<RollNumber>@123` (e.g., `101@123`, `102@123`).
  - **Student Selector**: Quick selector for all 30 students (Roll `101` to `130`) or demo badges (`101` Top Performer, `126` Defaulter, etc.) with automatic password filling for fast access.
  - **Profile Hero Card**: Displays roll number, department (CSE 3rd Year / A), and an **Animated Circular Attendance Gauge**.
  - **Semester Examination Verdict Banner**: Instant clearance status (Prominent Green for $\ge$ 75% or Caution Amber/Red for Shortage).
  - **5-Subjects Attendance Cards**: Individual progress meters, faculty in-charge (including **Dr. Naga Padmaja** for Java), and recovery target advice (*Goal Satisfied* or *Need +N classes*).
  - **Date-wise Lecture Log**: Chronological classroom attendance history with subject filters.

- **📊 College Overview & Analytics**:
  - Executive KPI statistics across students, courses, records, and overall institutional turnout rate.
  - Subject comparative performance charts.
  - Department-wide class directory.

- **Aesthetic Excellence**:
  - Bespoke glassmorphism design system with midnight gradients, subtle glows, micro-animations, and full Dark / Light mode toggle.
  - Integrated with Java SE's built-in `com.sun.net.httpserver.HttpServer` (zero external dependencies).

---

## 🚀 How to Run the Applications

### 1. Terminal Console Application
To launch the interactive Java SE terminal console application:
```cmd
run_terminal.bat
```
*(Or in PowerShell: `.\run_terminal.bat`)*

**Features in Terminal:**
* **Teacher Portal**: Faculty login for Dr. Naga Padmaja & subject teachers with attendance posting and registers.
* **Student Portal**: Secure roll number login (`101` to `130`) with `<roll>@123` password and 5-subject attendance statements.
* **Multi-Threaded Classroom Audit**: Parallel worker threads for concurrent classroom attendance auditing and validation.
* **Analytics & Defaulters Stream Report**: High-performance Java Streams processing to filter students with attendance below 75%.
* **Interactive GUI Roster Window**: Swing desktop interface with Delegation Event Model and live table filters.
* **Relational Database & SQL Simulator**: Multi-tier persistence simulation with PreparedStatement parameter binding and ResultSet processing.

---

### 2. Modern Web Interface
To launch the full glassmorphic web portal:
```cmd
run_web.bat
```
*(Or in PowerShell: `.\run_web.bat`)*

*Compiles all classes, frees port 8080 if needed, launches the Java HTTP Server, and opens [http://localhost:8080](http://localhost:8080) automatically.*

---

## 📁 Clean Project Structure

```
├── data/
│   ├── attendance.csv                  # Persistent attendance records database
│   ├── students.csv                    # 30 preloaded CSE students
│   └── subjects.csv                    # 5 academic subjects (Dr. Naga Padmaja for Java)
├── src/
│   └── com/attendance/
│       ├── Main.java                   # Terminal application entry menu
│       ├── concurrent/                 # Multi-threaded classroom attendance auditing
│       │   └── ConcurrentAttendanceAuditor.java
│       ├── dao/                        # Database & SQL persistence simulation
│       │   └── DatabaseSimulator.java
│       ├── exception/                  # Custom business & validation exceptions
│       │   ├── AttendanceValidationException.java
│       │   ├── AuthenticationException.java
│       │   └── InvalidRollNumberException.java
│       ├── gui/                        # Swing desktop interface & event handling
│       │   └── AttendanceRosterWindow.java
│       ├── model/                      # Domain models (Student, Subject, AttendanceRecord, AttendanceSummary)
│       ├── service/                    # Business logic & analytics engines
│       │   ├── AttendanceAnalyticsEngine.java
│       │   ├── AttendanceService.java
│       │   └── DataPersistenceService.java
│       ├── util/                       # Console table formatters
│       └── web/                        # Built-in Java SE HTTP REST server
│           └── AttendanceWebServer.java
├── web/
│   ├── assets/
│   │   └── crest.jpg                   # University emblem crest
│   ├── index.html                      # Semantic HTML5 portal (Student & Teacher)
│   ├── style.css                       # Modern CSS design system
│   └── app.js                          # Client-side engine with student password auth
├── run_terminal.bat                    # One-click launcher for Terminal Application
├── run_web.bat                         # One-click launcher for Web Interface
└── README.md                           # Documentation & user guide
```
