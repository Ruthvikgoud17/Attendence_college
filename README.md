# College Attendance Management System (Java SE & Modern Web Interface)

A complete, Object-Oriented Course-End Project built according to university attendance regulations with both a **Modern Glassmorphic Web Interface** and a **Standard Java SE Terminal Application**.

---

## 🌐 Modern Web Interface (New!)

A responsive, high-performance web dashboard featuring real-time data sync with the Java backend CSV database (`data/*.csv`):

- **👨‍🏫 Teacher Portal**:
  - **5 Faculty Teachers Pre-configured**:
    - `JAVA` — **Java Programming** (Prof. Venkat, password: `java`)
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
  - **Student Selector**: Quick selector for all 30 students (Roll `101` to `130`) or demo badges (`101` Top Performer, `126` Defaulter, etc.).
  - **Profile Hero Card**: Displays roll number, department (CSE 3rd Year / A), and an **Animated Circular Attendance Gauge**.
  - **Semester Examination Verdict Banner**: Instant clearance status (Prominent Green for $\ge$ 75% or Caution Amber/Red for Shortage).
  - **5-Subjects Attendance Cards**: Individual progress meters, faculty in-charge, and recovery target advice (*Goal Satisfied* or *Need +N classes*).
  - **Date-wise Lecture Log**: Chronological classroom attendance history with subject filters.

- **📊 College Overview & Analytics**:
  - Executive KPI statistics across students, courses, records, and overall institutional turnout rate.
  - Subject comparative performance charts.
  - Department-wide class directory.

- **Aesthetic Excellence**:
  - Bespoke glassmorphism design system with midnight gradients, subtle glows, micro-animations, and full Dark / Light mode toggle.
  - Integrated with Java SE's built-in `com.sun.net.httpserver.HttpServer` (zero external dependencies).

---

## 🚀 How to Run the Web Interface

### Option 1: One-Click Batch Script (Windows)
Double-click or run in terminal:
```cmd
run_web.bat
```
*(Compiles all Java classes, starts the server at `http://localhost:8080`, and opens your default browser).*

### Option 2: PowerShell
```powershell
.\run_web.ps1
```

### Option 3: Unified Runner
```cmd
.\run.bat
```
*Select `[1]` for the Modern Web Interface or `[2]` for the Console CLI.*

### Option 4: Standalone / Direct HTML Access
You can also directly open `web/index.html` in any browser. It includes built-in offline synchronization and localStorage persistence if the Java backend is not running.

---

## 📁 Project Structure

```
├── data/
│   ├── attendance.csv              # Persistent attendance records database
│   ├── students.csv                # 30 preloaded CSE students
│   └── subjects.csv                # 5 academic subjects with teachers & passwords
├── src/
│   └── com/attendance/
│       ├── Main.java               # Terminal CLI application
│       ├── model/                  # Object-Oriented models (Student, Subject, Record, Summary)
│       ├── service/                # Business logic & CSV persistence services
│       ├── util/                   # Console table formatters
│       └── web/
│           └── AttendanceWebServer.java  # Built-in Java SE HTTP REST server
├── web/
│   ├── assets/
│   │   └── crest.jpg               # University emblem crest
│   ├── index.html                  # Semantic, rich HTML5 application
│   ├── style.css                   # Bespoke Vanilla CSS glassmorphic design system
│   └── app.js                      # Front-end engine & API client
├── run.bat                         # Unified launcher (Web or CLI)
├── run_web.bat                     # Direct Web launcher
├── run_web.ps1                     # PowerShell Web launcher
└── README.md                       # Project documentation
```
