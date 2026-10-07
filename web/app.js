/**
 * Apex Institute of Technology - College Attendance Management System
 * Front-end Application Engine (Role-Based Login, Clean Navigation & Robust Execution)
 */

(function () {
  'use strict';

  // API Configuration
  const API_BASE = window.location.origin.includes('localhost') || window.location.origin.includes('127.0.0.1')
    ? window.location.origin
    : 'http://localhost:8080';

  let isBackendOnline = false;

  // 30 Standard Preloaded Students (CSE 3rd Year / Section A)
  const PRELOADED_STUDENTS = [
    { rollNo: "101", name: "Aarav Sharma", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "102", name: "Bhavna Patel", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "103", name: "Chirag Reddy", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "104", name: "Divya Nair", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "105", name: "Eshwar Rao", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "106", name: "Farhan Khan", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "107", name: "Gayatri Joshi", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "108", name: "Harish Verma", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "109", name: "Ishita Sen", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "110", name: "Jatin Gupta", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "111", name: "Kavya Iyer", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "112", name: "Lokesh Yadav", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "113", name: "Meera Das", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "114", name: "Nikhil Kumar", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "115", name: "Olivia Roy", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "116", name: "Pranav Malhotra", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "117", name: "Qadir Ali", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "118", name: "Ritu Menon", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "119", name: "Sanjay Pillai", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "120", name: "Tanvi Deshmukh", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "121", name: "Uday Chauhan", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "122", name: "Vaishnavi Bhat", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "123", name: "Wasim Akram", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "124", name: "Xenia D'Souza", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "125", name: "Yash Kulkarni", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "126", name: "Zoya Siddiqui", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "127", name: "Ananya Pandey", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "128", name: "Bharat Subramanian", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "129", name: "Chetan Bhagat", department: "CSE", yearSection: "3RD YEAR / A" },
    { rollNo: "130", name: "Deepa Mehta", department: "CSE", yearSection: "3RD YEAR / A" }
  ];

  // 5 Subject Teachers & Passwords
  const PRELOADED_SUBJECTS = [
    { subjectCode: "JAVA", subjectName: "Java Programming", facultyName: "Prof. Venkat", password: "java" },
    { subjectCode: "PYTHON", subjectName: "Python Programming", facultyName: "Dr. Rajesh", password: "python" },
    { subjectCode: "MATHS", subjectName: "Engineering Mathematics", facultyName: "Dr. Sharma", password: "maths" },
    { subjectCode: "DSA", subjectName: "Data Structures & Algo", facultyName: "Prof. Priya", password: "dsa" },
    { subjectCode: "WEB", subjectName: "Web Technologies", facultyName: "Prof. Arun", password: "web" }
  ];

  let localStudents = [...PRELOADED_STUDENTS];
  let localSubjects = [...PRELOADED_SUBJECTS];
  let localAttendanceRecords = [];

  // Active Session State
  let activeTeacherSubject = PRELOADED_SUBJECTS[0]; // JAVA
  let activeStudent = null;
  let rosterStatusMap = {}; // rollNo -> "Present" | "Absent"

  // =========================================================================
  // DOM Cache
  // =========================================================================

  const Views = {
    selection: document.getElementById('viewPortalSelection'),
    teacherLogin: document.getElementById('viewTeacherLogin'),
    studentLogin: document.getElementById('viewStudentLogin'),
    teacherDashboard: document.getElementById('viewTeacherDashboard'),
    studentDashboard: document.getElementById('viewStudentDashboard')
  };

  const Header = {
    serverPill: document.getElementById('serverPill'),
    serverStatusText: document.getElementById('serverStatusText'),
    userSessionBar: document.getElementById('userSessionBar'),
    sessionUserBadge: document.getElementById('sessionUserBadge'),
    btnLogout: document.getElementById('btnLogout')
  };

  // =========================================================================
  // Helper Functions
  // =========================================================================

  function getTodayString() {
    return new Date().toISOString().split('T')[0];
  }

  function getYesterdayString() {
    const d = new Date();
    d.setDate(d.getDate() - 1);
    return d.toISOString().split('T')[0];
  }

  function getInitials(name) {
    if (!name) return 'EX';
    const clean = name.replace(/^(Prof\.|Dr\.|Mr\.|Ms\.)\s*/, '').trim();
    const parts = clean.split(/\s+/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return clean.substring(0, 2).toUpperCase();
  }

  function computeClassesNeeded(held, attended) {
    if (held === 0) return 0;
    const pct = (attended / held) * 100.0;
    if (pct >= 75.0) return 0;
    const needed = Math.ceil(((75.0 * held) - (100.0 * attended)) / 25.0);
    return Math.max(0, needed);
  }

  // View Switching Router
  function showView(viewElement) {
    Object.values(Views).forEach(v => {
      if (v) v.classList.remove('active');
    });
    if (viewElement) {
      viewElement.classList.add('active');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  // =========================================================================
  // Offline Data Seeder (Fallback)
  // =========================================================================

  function seedOfflineDataIfEmpty() {
    const cached = localStorage.getItem('apex_attendance_records');
    if (cached) {
      try {
        localAttendanceRecords = JSON.parse(cached);
        return;
      } catch (e) {}
    }

    localAttendanceRecords = [];
    const dates = [
      "2026-09-22", "2026-09-23", "2026-09-24", "2026-09-25", "2026-09-26",
      "2026-09-29", "2026-09-30", "2026-10-01", "2026-10-02", "2026-10-05"
    ];

    dates.forEach(date => {
      PRELOADED_SUBJECTS.forEach(sub => {
        PRELOADED_STUDENTS.forEach(stu => {
          const rollNum = parseInt(stu.rollNo, 10);
          let isPresent = true;

          if (rollNum >= 126) {
            isPresent = (rollNum + date.charCodeAt(9)) % 3 === 0;
          } else if (rollNum >= 121) {
            isPresent = (rollNum + date.charCodeAt(9)) % 4 !== 0;
          } else if (rollNum <= 103) {
            isPresent = true;
          } else {
            isPresent = (rollNum + date.charCodeAt(9)) % 7 !== 0;
          }

          localAttendanceRecords.push({
            rollNo: stu.rollNo,
            subjectCode: sub.subjectCode,
            date: date,
            status: isPresent ? "Present" : "Absent",
            isPresent: isPresent
          });
        });
      });
    });

    localStorage.setItem('apex_attendance_records', JSON.stringify(localAttendanceRecords));
  }

  // =========================================================================
  // Backend Connection Check
  // =========================================================================

  async function checkBackendStatus() {
    try {
      const res = await fetch(`${API_BASE}/api/status`, { cache: 'no-store' });
      if (res.ok) {
        isBackendOnline = true;
        Header.serverPill.style.background = 'rgba(255, 255, 255, 0.15)';
        Header.serverStatusText.textContent = 'Server Live';
        Header.serverPill.querySelector('.status-indicator-dot').style.background = '#4ade80';
        return true;
      }
    } catch (e) {}
    isBackendOnline = false;
    Header.serverPill.style.background = 'rgba(255, 255, 255, 0.15)';
    Header.serverStatusText.textContent = 'Standalone Mode';
    Header.serverPill.querySelector('.status-indicator-dot').style.background = '#fbbf24';
    return false;
  }

  // =========================================================================
  // 1. Role Selection & Logout Navigation
  // =========================================================================

  function initPortalNavigation() {
    // Landing Cards Click
    document.getElementById('btnGoTeacherLogin').addEventListener('click', () => {
      document.getElementById('teacherPasswordInput').value = '';
      document.getElementById('teacherLoginError').style.display = 'none';
      showView(Views.teacherLogin);
      document.getElementById('teacherPasswordInput').focus();
    });

    document.getElementById('btnGoStudentLogin').addEventListener('click', () => {
      document.getElementById('studentRollInput').value = '';
      document.getElementById('studentLoginError').style.display = 'none';
      showView(Views.studentLogin);
      document.getElementById('studentRollInput').focus();
    });

    // Back Buttons
    document.getElementById('btnBackFromTeacherLogin').addEventListener('click', () => {
      showView(Views.selection);
    });

    document.getElementById('btnBackFromStudentLogin').addEventListener('click', () => {
      showView(Views.selection);
    });

    // Logout Button
    Header.btnLogout.addEventListener('click', handleLogout);
  }

  function handleLogout() {
    activeTeacherSubject = null;
    activeStudent = null;
    Header.userSessionBar.style.display = 'none';
    showView(Views.selection);
  }

  // =========================================================================
  // 2. Teacher Login Controller
  // =========================================================================

  function initTeacherLogin() {
    const form = document.getElementById('teacherLoginForm');
    const select = document.getElementById('teacherSubjectSelect');
    const passInput = document.getElementById('teacherPasswordInput');
    const eyeBtn = document.getElementById('btnToggleTeacherPass');
    const errBox = document.getElementById('teacherLoginError');

    // Eye toggle
    eyeBtn.addEventListener('click', () => {
      passInput.type = passInput.type === 'password' ? 'text' : 'password';
    });

    // Quick password helper chips
    document.querySelectorAll('.chip-pass').forEach(chip => {
      chip.addEventListener('click', () => {
        const sub = chip.getAttribute('data-sub');
        const pass = chip.getAttribute('data-pass');
        select.value = sub;
        passInput.value = pass;
        passInput.focus();
      });
    });

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      errBox.style.display = 'none';
      const password = passInput.value.trim().toLowerCase();
      const subjectCode = select.value;

      if (!password) {
        errBox.textContent = 'Please enter faculty password.';
        errBox.style.display = 'block';
        return;
      }

      let matchedSubject = null;

      if (isBackendOnline) {
        try {
          const res = await fetch(`${API_BASE}/api/teacher/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ password, subjectCode })
          });
          const json = await res.json();
          if (json.success && json.subject) {
            matchedSubject = json.subject;
          } else {
            errBox.textContent = json.message || 'Invalid password! Standard passwords are: java, python, maths, dsa, web.';
            errBox.style.display = 'block';
            return;
          }
        } catch (err) {
          console.warn('Backend login fallback to local check', err);
        }
      }

      if (!matchedSubject) {
        // Local fallback verification
        const targetSub = localSubjects.find(s => s.subjectCode === subjectCode);
        if (targetSub && (password === targetSub.password || password === targetSub.subjectCode.toLowerCase() || password === 'teach123')) {
          matchedSubject = targetSub;
        } else {
          // Check if password matches any other subject directly
          const anySub = localSubjects.find(s => s.password === password || s.subjectCode.toLowerCase() === password);
          if (anySub) {
            matchedSubject = anySub;
          } else if (password === 'teach123') {
            matchedSubject = targetSub || localSubjects[0];
          }
        }
      }

      if (matchedSubject) {
        openTeacherDashboard(matchedSubject);
      } else {
        errBox.textContent = `Invalid password! Enter ${select.value.toLowerCase()} or teach123.`;
        errBox.style.display = 'block';
      }
    });
  }

  function openTeacherDashboard(subject) {
    activeTeacherSubject = subject;

    // Update Header Session Bar
    Header.sessionUserBadge.textContent = `Faculty: ${subject.facultyName} (${subject.subjectCode})`;
    Header.userSessionBar.style.display = 'flex';

    // Update Dashboard Banner
    document.getElementById('tDashAvatar').textContent = getInitials(subject.facultyName);
    document.getElementById('tDashSubCode').textContent = subject.subjectCode;
    document.getElementById('tDashFacultyName').textContent = subject.facultyName;
    document.getElementById('tDashSubName').textContent = `Course: ${subject.subjectName}`;
    document.getElementById('postCourseNameText').textContent = subject.subjectName;
    document.getElementById('registerCourseSubtitle').textContent = `3rd Year B.Tech CSE • ${subject.subjectName}`;
    document.getElementById('tDashSubjectSwitcher').value = subject.subjectCode;

    // Reset post form
    document.getElementById('postDateInput').value = getTodayString();
    document.getElementById('inspectDateInput').value = getTodayString();
    document.getElementById('commaRollInput').value = '';
    document.getElementById('postFeedbackMsg').style.display = 'none';

    // Reset Roster state to All Present
    localStudents.forEach(s => {
      rosterStatusMap[s.rollNo] = "Present";
    });

    renderRosterGrid();
    updateLiveTurnoutMeter();

    // Show View
    showView(Views.teacherDashboard);

    // Switch to first tab (Post Attendance)
    switchTeacherTab('tTabPost');

    // Update Defaulters count badge
    refreshDefaultersCountBadge();
  }

  // =========================================================================
  // 3. Teacher Dashboard Controllers (Clean & Properly Functioning)
  // =========================================================================

  function initTeacherDashboard() {
    // Subject Switcher dropdown
    document.getElementById('tDashSubjectSwitcher').addEventListener('change', (e) => {
      const sub = localSubjects.find(s => s.subjectCode === e.target.value);
      if (sub) {
        openTeacherDashboard(sub);
      }
    });

    // Sub-Tabs
    document.querySelectorAll('.dash-tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const targetTab = btn.getAttribute('data-tab');
        switchTeacherTab(targetTab);
      });
    });

    // Date Shortcuts
    document.getElementById('btnPostToday').addEventListener('click', () => {
      document.getElementById('postDateInput').value = getTodayString();
    });

    document.getElementById('btnPostYesterday').addEventListener('click', () => {
      document.getElementById('postDateInput').value = getYesterdayString();
    });

    // Entry Mode Radios
    document.querySelectorAll('input[name="postMode"]').forEach(radio => {
      radio.addEventListener('change', () => {
        const isAbsentMode = radio.value === 'absent';
        document.getElementById('commaRollLabel').textContent = isAbsentMode
          ? 'Quick Entry: Enter Comma-Separated ABSENT Roll Numbers:'
          : 'Quick Entry: Enter Comma-Separated PRESENT Roll Numbers:';
        document.getElementById('commaRollInput').placeholder = isAbsentMode
          ? 'e.g. 103, 108, 126, 129'
          : 'e.g. 101, 102, 104, 105';
      });
    });

    // Apply Comma Roll Numbers
    document.getElementById('btnApplyCommaRolls').addEventListener('click', applyCommaRollNumbers);
    document.getElementById('commaRollInput').addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        applyCommaRollNumbers();
      }
    });

    // Roster Bulk Actions
    document.getElementById('btnAllPresent').addEventListener('click', () => {
      localStudents.forEach(s => rosterStatusMap[s.rollNo] = "Present");
      renderRosterGrid();
      updateLiveTurnoutMeter();
    });

    document.getElementById('btnAllAbsent').addEventListener('click', () => {
      localStudents.forEach(s => rosterStatusMap[s.rollNo] = "Absent");
      renderRosterGrid();
      updateLiveTurnoutMeter();
    });

    document.getElementById('btnInvert').addEventListener('click', () => {
      localStudents.forEach(s => {
        rosterStatusMap[s.rollNo] = (rosterStatusMap[s.rollNo] === "Present") ? "Absent" : "Present";
      });
      renderRosterGrid();
      updateLiveTurnoutMeter();
    });

    // Submit Attendance to Database
    document.getElementById('btnSaveAttendance').addEventListener('click', saveAttendanceSession);

    // Register Filters & Search
    document.getElementById('searchRegisterInput').addEventListener('input', filterRegisterRows);
    document.querySelectorAll('#registerFilterGroup .filter-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        document.querySelectorAll('#registerFilterGroup .filter-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        filterRegisterRows();
      });
    });

    // Export CSV & Print
    document.getElementById('btnDownloadRegisterCsv').addEventListener('click', downloadRegisterCsv);
    document.getElementById('btnPrintRegisterReport').addEventListener('click', () => window.print());

    // Daily Register Load
    document.getElementById('btnLoadDailyRegister').addEventListener('click', loadDailyRegisterData);

    // Defaulters Warning Notice
    document.getElementById('btnPrintDefaultersNotice').addEventListener('click', openNoticeModal);
    document.getElementById('btnCloseNotice').addEventListener('click', () => document.getElementById('modalNotice').style.display = 'none');
    document.getElementById('btnDismissNotice').addEventListener('click', () => document.getElementById('modalNotice').style.display = 'none');
    document.getElementById('btnPrintNoticeModal').addEventListener('click', () => window.print());
  }

  function switchTeacherTab(tabId) {
    document.querySelectorAll('.dash-tab-btn').forEach(b => {
      b.classList.toggle('active', b.getAttribute('data-tab') === tabId);
    });
    document.querySelectorAll('.tab-pane').forEach(p => {
      p.classList.toggle('active', p.id === tabId);
    });

    if (tabId === 'tTabRegister') loadClassRegisterData();
    if (tabId === 'tTabDefaulters') loadDefaultersData();
    if (tabId === 'tTabDaily') loadDailyRegisterData();
  }

  function applyCommaRollNumbers() {
    const raw = document.getElementById('commaRollInput').value.trim();
    const rolls = raw.split(',').map(r => r.trim()).filter(Boolean);
    const mode = document.querySelector('input[name="postMode"]:checked').value;

    if (mode === 'absent') {
      const set = new Set(rolls);
      localStudents.forEach(s => {
        rosterStatusMap[s.rollNo] = set.has(s.rollNo) ? "Absent" : "Present";
      });
    } else {
      const set = new Set(rolls);
      localStudents.forEach(s => {
        rosterStatusMap[s.rollNo] = set.has(s.rollNo) ? "Present" : "Absent";
      });
    }

    renderRosterGrid();
    updateLiveTurnoutMeter();
  }

  function renderRosterGrid() {
    const container = document.getElementById('studentsRosterGrid');
    container.innerHTML = '';

    localStudents.forEach(s => {
      const status = rosterStatusMap[s.rollNo] || "Present";
      const isPresent = status === "Present";

      const chip = document.createElement('div');
      chip.className = `student-roster-chip ${isPresent ? 'is-present' : 'is-absent'}`;
      chip.setAttribute('data-roll', s.rollNo);

      chip.innerHTML = `
        <div class="chip-info">
          <span class="chip-roll">Roll ${s.rollNo}</span>
          <span class="chip-name" title="${s.name}">${s.name}</span>
        </div>
        <span class="chip-status-tag">${isPresent ? 'PRESENT' : 'ABSENT'}</span>
      `;

      chip.addEventListener('click', () => {
        toggleStudentStatus(s.rollNo);
      });

      container.appendChild(chip);
    });
  }

  function toggleStudentStatus(rollNo) {
    const current = rosterStatusMap[rollNo] || "Present";
    const next = current === "Present" ? "Absent" : "Present";
    rosterStatusMap[rollNo] = next;

    const chip = document.querySelector(`.student-roster-chip[data-roll="${rollNo}"]`);
    if (chip) {
      const isP = next === "Present";
      chip.className = `student-roster-chip ${isP ? 'is-present' : 'is-absent'}`;
      chip.querySelector('.chip-status-tag').textContent = isP ? 'PRESENT' : 'ABSENT';
    }

    updateLiveTurnoutMeter();
  }

  function updateLiveTurnoutMeter() {
    const total = localStudents.length;
    let p = 0, a = 0;

    localStudents.forEach(s => {
      if (rosterStatusMap[s.rollNo] === "Present") p++;
      else a++;
    });

    const pct = total > 0 ? ((p / total) * 100.0).toFixed(1) : "0.0";

    document.getElementById('cntTotal').textContent = total;
    document.getElementById('cntPresent').textContent = p;
    document.getElementById('cntAbsent').textContent = a;
    document.getElementById('cntPct').textContent = `${pct}%`;
  }

  async function saveAttendanceSession() {
    const date = document.getElementById('postDateInput').value;
    const feedback = document.getElementById('postFeedbackMsg');
    const btn = document.getElementById('btnSaveAttendance');

    if (!date) {
      alert('Please select a valid lecture date.');
      return;
    }

    const records = localStudents.map(s => ({
      rollNo: s.rollNo,
      status: rosterStatusMap[s.rollNo] || "Present"
    }));

    const payload = {
      subjectCode: activeTeacherSubject.subjectCode,
      date: date,
      records: records
    };

    btn.disabled = true;
    btn.innerHTML = '<span>Saving...</span>';

    let savedOk = false;

    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/mark`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          savedOk = true;
        }
      } catch (e) {
        console.warn('Backend mark error, saving locally:', e);
      }
    }

    // Always sync locally
    records.forEach(r => {
      localAttendanceRecords = localAttendanceRecords.filter(
        ar => !(ar.rollNo === r.rollNo && ar.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase() && ar.date === date)
      );
      localAttendanceRecords.push({
        rollNo: r.rollNo,
        subjectCode: activeTeacherSubject.subjectCode,
        date: date,
        status: r.status,
        isPresent: r.status === "Present"
      });
    });
    localStorage.setItem('apex_attendance_records', JSON.stringify(localAttendanceRecords));

    btn.disabled = false;
    btn.innerHTML = `
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
      <span>Save Attendance to Database</span>
    `;

    // Show clean success alert
    let pCount = records.filter(r => r.status === "Present").length;
    let aCount = records.length - pCount;
    feedback.className = 'feedback-msg success';
    feedback.innerHTML = `✓ Attendance successfully recorded for <strong>${activeTeacherSubject.subjectName}</strong> on <strong>${date}</strong>! (Present: ${pCount}, Absent: ${aCount})`;
    feedback.style.display = 'block';

    refreshDefaultersCountBadge();

    setTimeout(() => {
      feedback.style.display = 'none';
    }, 5000);
  }

  // =========================================================================
  // 4. Class Register & Defaulters Table Loaders
  // =========================================================================

  async function loadClassRegisterData() {
    const tbody = document.getElementById('tbodyClassRegister');
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:20px;">Loading student register...</td></tr>';

    let list = [];

    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/subject-percentages?subjectCode=${activeTeacherSubject.subjectCode}`);
        if (res.ok) {
          const json = await res.json();
          list = json.students;
        }
      } catch (e) {}
    }

    if (!list || list.length === 0) {
      // Local fallback calculation
      const uniqueHeldDates = new Set(localAttendanceRecords
        .filter(r => r.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase())
        .map(r => r.date));
      const totalHeld = uniqueHeldDates.size;

      list = localStudents.map(student => {
        const subRecs = localAttendanceRecords.filter(
          r => r.rollNo === student.rollNo && r.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase()
        );
        const attended = subRecs.filter(r => r.isPresent).length;
        const absent = totalHeld - attended;
        const pct = totalHeld > 0 ? ((attended / totalHeld) * 100.0) : 0;
        return {
          student: student,
          summary: {
            totalClasses: totalHeld,
            presentClasses: attended,
            absentClasses: absent,
            percentage: pct,
            isEligible: pct >= 75.0
          }
        };
      });
    }

    tbody.innerHTML = '';
    let totalEligible = 0;
    let totalShortage = 0;

    list.forEach(item => {
      const { student, summary } = item;
      const isEligible = summary.isEligible;
      if (isEligible) totalEligible++;
      else totalShortage++;

      const tr = document.createElement('tr');
      tr.setAttribute('data-roll', student.rollNo);
      tr.setAttribute('data-name', student.name);
      tr.setAttribute('data-eligible', isEligible ? 'true' : 'false');

      tr.innerHTML = `
        <td><strong>${student.rollNo}</strong></td>
        <td><strong>${student.name}</strong></td>
        <td>${student.department} - ${student.yearSection}</td>
        <td>${summary.totalClasses}</td>
        <td class="text-green font-bold">${summary.presentClasses}</td>
        <td class="text-red font-bold">${summary.absentClasses}</td>
        <td>
          <div style="display:flex; align-items:center; gap:8px;">
            <div style="flex:1; height:6px; background:#e2e8f0; border-radius:3px; overflow:hidden;">
              <div style="height:100%; width:${summary.percentage.toFixed(1)}%; background:${isEligible ? '#16a34a' : '#dc2626'};"></div>
            </div>
            <strong class="${isEligible ? 'text-green' : 'text-red'}">${summary.percentage.toFixed(1)}%</strong>
          </div>
        </td>
        <td>
          <span class="badge-status ${isEligible ? 'badge-eligible' : 'badge-shortage'}">
            ${isEligible ? 'Eligible' : 'Shortage'}
          </span>
        </td>
      `;

      tbody.appendChild(tr);
    });

    document.getElementById('tabEligibleFilter').textContent = `Eligible (${totalEligible})`;
    document.getElementById('tabShortageFilter').textContent = `Shortage (${totalShortage})`;

    document.getElementById('registerFooterSummary').innerHTML = `
      <span>Total Enrolled: <strong>${list.length}</strong></span>
      <span>Eligible (&ge;75%): <strong class="text-green">${totalEligible}</strong></span>
      <span>Shortage (&lt;75%): <strong class="text-red">${totalShortage}</strong></span>
      <span>Faculty: <strong>${activeTeacherSubject.facultyName}</strong></span>
    `;
  }

  function filterRegisterRows() {
    const q = document.getElementById('searchRegisterInput').value.toLowerCase().trim();
    const activeFilter = document.querySelector('#registerFilterGroup .filter-tab.active').getAttribute('data-filter');

    document.querySelectorAll('#tbodyClassRegister tr').forEach(row => {
      const roll = (row.getAttribute('data-roll') || '').toLowerCase();
      const name = (row.getAttribute('data-name') || '').toLowerCase();
      const isEligible = row.getAttribute('data-eligible') === 'true';

      const matchSearch = roll.includes(q) || name.includes(q);
      let matchFilter = true;

      if (activeFilter === 'eligible') matchFilter = isEligible;
      else if (activeFilter === 'shortage') matchFilter = !isEligible;

      row.style.display = (matchSearch && matchFilter) ? '' : 'none';
    });
  }

  function downloadRegisterCsv() {
    const rows = [
      ["Roll No", "Student Name", "Class", "Held", "Attended", "Absent", "Attendance %", "Status"]
    ];

    document.querySelectorAll('#tbodyClassRegister tr').forEach(tr => {
      if (tr.style.display !== 'none') {
        const tds = tr.querySelectorAll('td');
        if (tds.length >= 8) {
          rows.push([
            tds[0].innerText.trim(),
            tds[1].innerText.trim(),
            tds[2].innerText.trim(),
            tds[3].innerText.trim(),
            tds[4].innerText.trim(),
            tds[5].innerText.trim(),
            tds[6].innerText.trim(),
            tds[7].innerText.trim()
          ]);
        }
      }
    });

    const csvContent = "data:text/csv;charset=utf-8," + rows.map(r => r.join(",")).join("\n");
    const link = document.createElement("a");
    link.setAttribute("href", encodeURI(csvContent));
    link.setAttribute("download", `register_${activeTeacherSubject.subjectCode}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  }

  async function refreshDefaultersCountBadge() {
    let count = 0;
    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/defaulters?subjectCode=${activeTeacherSubject.subjectCode}`);
        if (res.ok) {
          const json = await res.json();
          count = json.defaulterCount;
        }
      } catch (e) {}
    } else {
      const uniqueHeld = new Set(localAttendanceRecords
        .filter(r => r.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase())
        .map(r => r.date)).size;

      localStudents.forEach(stu => {
        const subRecs = localAttendanceRecords.filter(r => r.rollNo === stu.rollNo && r.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase());
        const attended = subRecs.filter(r => r.isPresent).length;
        if (uniqueHeld > 0 && ((attended / uniqueHeld) * 100.0) < 75.0) count++;
      });
    }

    document.getElementById('tDefaultersCountBadge').textContent = count;
    document.getElementById('defaulterCountPill').textContent = `${count} Defaulters`;
  }

  async function loadDefaultersData() {
    const tbody = document.getElementById('tbodyDefaulters');
    tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:20px;">Analyzing defaulters...</td></tr>';

    let defaulters = [];

    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/defaulters?subjectCode=${activeTeacherSubject.subjectCode}&threshold=75`);
        if (res.ok) {
          const json = await res.json();
          defaulters = json.defaulters;
        }
      } catch (e) {}
    }

    if (!defaulters || defaulters.length === 0) {
      // Local fallback
      const uniqueHeld = new Set(localAttendanceRecords
        .filter(r => r.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase())
        .map(r => r.date)).size;

      localStudents.forEach(stu => {
        const subRecs = localAttendanceRecords.filter(r => r.rollNo === stu.rollNo && r.subjectCode.toUpperCase() === activeTeacherSubject.subjectCode.toUpperCase());
        const attended = subRecs.filter(r => r.isPresent).length;
        const absent = uniqueHeld - attended;
        const pct = uniqueHeld > 0 ? ((attended / uniqueHeld) * 100.0) : 0;

        if (uniqueHeld > 0 && pct < 75.0) {
          defaulters.push({
            student: stu,
            summary: {
              totalClasses: uniqueHeld,
              presentClasses: attended,
              absentClasses: absent,
              percentage: pct
            },
            deficit: (75.0 - pct).toFixed(1),
            classesNeeded: computeClassesNeeded(uniqueHeld, attended)
          });
        }
      });
    }

    document.getElementById('defaulterCountPill').textContent = `${defaulters.length} Defaulters`;
    document.getElementById('tDefaultersCountBadge').textContent = defaulters.length;

    if (defaulters.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="9" style="text-align:center; padding:32px; color:#16a34a; font-weight:700;">
            ✓ Great! No defaulters in ${activeTeacherSubject.subjectName}. All students have >= 75% attendance.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = '';
    defaulters.forEach(item => {
      const { student, summary, deficit, classesNeeded } = item;
      const isCritical = summary.percentage < 65.0;

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${student.rollNo}</strong></td>
        <td><strong>${student.name}</strong></td>
        <td>${summary.totalClasses}</td>
        <td class="text-green font-bold">${summary.presentClasses}</td>
        <td class="text-red font-bold">${summary.absentClasses}</td>
        <td><strong class="text-red">${summary.percentage.toFixed(1)}%</strong></td>
        <td><span class="badge-status badge-shortage">-${deficit}%</span></td>
        <td><strong style="color:#b91c1c;">+${classesNeeded} consecutive classes</strong></td>
        <td>
          <span class="badge-status ${isCritical ? 'badge-shortage' : 'badge-eligible'}">
            ${isCritical ? 'Critical (<65%)' : 'Borderline'}
          </span>
        </td>
      `;
      tbody.appendChild(tr);
    });
  }

  function openNoticeModal() {
    const today = new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
    const rows = Array.from(document.querySelectorAll('#tbodyDefaulters tr')).map(tr => {
      const tds = tr.querySelectorAll('td');
      if (tds.length < 8) return '';
      return `
        <tr>
          <td style="padding:6px; border:1px solid #cbd5e1; text-align:center;">${tds[0].innerText.trim()}</td>
          <td style="padding:6px; border:1px solid #cbd5e1;">${tds[1].innerText.trim()}</td>
          <td style="padding:6px; border:1px solid #cbd5e1; text-align:center; color:#dc2626; font-weight:bold;">${tds[5].innerText.trim()}</td>
          <td style="padding:6px; border:1px solid #cbd5e1; text-align:center; font-weight:bold;">${tds[7].innerText.trim()}</td>
        </tr>
      `;
    }).join('');

    document.getElementById('noticeContentArea').innerHTML = `
      <div style="text-align:center; border-bottom:2px solid #0f172a; padding-bottom:12px; margin-bottom:16px;">
        <h2 style="font-size:1.3rem; color:#1e3a8a; margin-bottom:2px;">APEX INSTITUTE OF TECHNOLOGY</h2>
        <p style="font-size:0.85rem; color:#475569;">Department of Computer Science & Engineering • Academic Session 2026-27</p>
        <p style="font-size:0.8rem; color:#64748b;">Notice Ref: AIT/CSE/ATTN/2026 | Date: ${today}</p>
      </div>

      <div style="font-family:serif; line-height:1.6; font-size:0.9rem;">
        <h3 style="text-align:center; text-decoration:underline; margin-bottom:12px;">
          STATUTORY ATTENDANCE SHORTAGE WARNING NOTICE (&lt; 75%)
        </h3>
        <p><strong>Course:</strong> ${activeTeacherSubject.subjectName} (${activeTeacherSubject.subjectCode})</p>
        <p><strong>Faculty In-Charge:</strong> ${activeTeacherSubject.facultyName}</p>
        <p style="margin-top:10px;">
          As per university regulations, candidates must maintain a minimum attendance of <strong>75%</strong> to be eligible for Semester End Examinations. The following candidates currently have an attendance shortage:
        </p>

        <table style="width:100%; border-collapse:collapse; margin:16px 0; font-size:0.85rem;">
          <thead>
            <tr style="background:#f1f5f9;">
              <th style="padding:6px; border:1px solid #cbd5e1;">Roll No</th>
              <th style="padding:6px; border:1px solid #cbd5e1;">Student Name</th>
              <th style="padding:6px; border:1px solid #cbd5e1;">Current Attendance</th>
              <th style="padding:6px; border:1px solid #cbd5e1;">Consecutive Classes Required</th>
            </tr>
          </thead>
          <tbody>
            ${rows}
          </tbody>
        </table>

        <p style="margin-top:12px; font-size:0.85rem; color:#475569;">
          Candidates are warned to attend all remaining lectures without fail. Shortage condonation is subject to Dean's review.
        </p>

        <div style="display:flex; justify-content:space-between; margin-top:40px;">
          <div>
            __________________________<br>
            <strong>${activeTeacherSubject.facultyName}</strong><br>
            Subject Teacher
          </div>
          <div style="text-align:right;">
            __________________________<br>
            <strong>Head of Department (CSE)</strong><br>
            Apex Institute of Technology
          </div>
        </div>
      </div>
    `;

    document.getElementById('modalNotice').style.display = 'flex';
  }

  async function loadDailyRegisterData() {
    const date = document.getElementById('inspectDateInput').value;
    const tbody = document.getElementById('tbodyDailyRegister');
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:16px;">Loading daily log...</td></tr>';

    let data = null;

    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/daily-register?subjectCode=${activeTeacherSubject.subjectCode}&date=${date}`);
        if (res.ok) data = await res.json();
      } catch (e) {}
    }

    if (!data) {
      const records = localAttendanceRecords.filter(r => r.subjectCode === activeTeacherSubject.subjectCode && r.date === date);
      const p = records.filter(r => r.isPresent).length;
      data = {
        date: date,
        total: records.length,
        presentCount: p,
        absentCount: records.length - p,
        turnoutRate: records.length > 0 ? ((p / records.length) * 100.0).toFixed(1) : "0.0",
        records: records.map(r => ({
          rollNo: r.rollNo,
          name: (localStudents.find(s => s.rollNo === r.rollNo) || {}).name || "Student",
          subjectCode: r.subjectCode,
          date: r.date,
          status: r.status,
          isPresent: r.isPresent
        }))
      };
    }

    document.getElementById('dLogDate').textContent = data.date;
    document.getElementById('dLogRate').textContent = `${data.turnoutRate}%`;
    document.getElementById('dLogPresent').textContent = data.presentCount;
    document.getElementById('dLogAbsent').textContent = data.absentCount;

    if (data.records.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:24px; color:var(--text-muted);">No attendance recorded for ${activeTeacherSubject.subjectCode} on ${data.date}.</td></tr>`;
      return;
    }

    tbody.innerHTML = '';
    data.records.forEach(r => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${r.rollNo}</strong></td>
        <td><strong>${r.name}</strong></td>
        <td><span class="tag-subject">${r.subjectCode}</span></td>
        <td>${r.date}</td>
        <td>
          <span class="badge-status ${r.isPresent ? 'badge-eligible' : 'badge-shortage'}">
            ${r.status}
          </span>
        </td>
      `;
      tbody.appendChild(tr);
    });
  }

  // =========================================================================
  // 5. Student Login & Dashboard Controllers (Roll Number Specific)
  // =========================================================================

  function initStudentLogin() {
    const rollInput = document.getElementById('studentRollInput');
    const select = document.getElementById('studentQuickSelect');
    const form = document.getElementById('studentLoginForm');
    const errBox = document.getElementById('studentLoginError');

    // Populate quick student dropdown
    select.innerHTML = '<option value="">-- Choose student from list --</option>';
    localStudents.forEach(s => {
      const opt = document.createElement('option');
      opt.value = s.rollNo;
      opt.textContent = `[${s.rollNo}] ${s.name}`;
      select.appendChild(opt);
    });

    select.addEventListener('change', () => {
      if (select.value) {
        rollInput.value = select.value;
      }
    });

    // Quick sample pills
    document.querySelectorAll('.sample-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        const roll = pill.getAttribute('data-roll');
        rollInput.value = roll;
        select.value = roll;
        form.dispatchEvent(new Event('submit'));
      });
    });

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      errBox.style.display = 'none';
      const roll = rollInput.value.trim().toUpperCase();

      const matchedStudent = localStudents.find(s => s.rollNo.toUpperCase() === roll);

      if (!matchedStudent) {
        errBox.textContent = `Invalid Roll Number! Enter a roll number between 101 and 130.`;
        errBox.style.display = 'block';
        return;
      }

      openStudentDashboard(matchedStudent);
    });

    // Lecture log filter
    document.getElementById('sDashHistorySubSelect').addEventListener('change', (e) => {
      if (activeStudent) {
        loadStudentHistoryLog(activeStudent.rollNo, e.target.value);
      }
    });
  }

  async function openStudentDashboard(student) {
    activeStudent = student;

    // Update Header Session Bar
    Header.sessionUserBadge.textContent = `Student: ${student.name} (#${student.rollNo})`;
    Header.userSessionBar.style.display = 'flex';

    // Update Profile Card
    document.getElementById('sDashAvatar').textContent = getInitials(student.name);
    document.getElementById('sDashRollTag').textContent = `Roll #${student.rollNo}`;
    document.getElementById('sDashName').textContent = student.name;
    document.getElementById('sDashClass').textContent = `${student.department} • ${student.yearSection}`;

    // Load 5-subjects summary
    let summaryData = null;

    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/summary?rollNo=${student.rollNo}`);
        if (res.ok) {
          summaryData = await res.json();
        }
      } catch (e) {}
    }

    if (!summaryData) {
      // Local fallback calculation
      let totalHeldAll = 0;
      let totalAttendedAll = 0;
      let totalAbsentAll = 0;

      const summaries = localSubjects.map(sub => {
        const subRecs = localAttendanceRecords.filter(
          r => r.rollNo === student.rollNo && r.subjectCode.toUpperCase() === sub.subjectCode.toUpperCase()
        );
        const uniqueHeld = new Set(localAttendanceRecords
          .filter(r => r.subjectCode.toUpperCase() === sub.subjectCode.toUpperCase())
          .map(r => r.date)).size;

        const attended = subRecs.filter(r => r.isPresent).length;
        const absent = uniqueHeld - attended;
        const pct = uniqueHeld > 0 ? ((attended / uniqueHeld) * 100.0) : 0;
        const isEligible = pct >= 75.0;

        totalHeldAll += uniqueHeld;
        totalAttendedAll += attended;
        totalAbsentAll += absent;

        return {
          subjectCode: sub.subjectCode,
          subjectName: sub.subjectName,
          facultyName: sub.facultyName,
          totalClasses: uniqueHeld,
          presentClasses: attended,
          absentClasses: absent,
          percentage: pct,
          isEligible: isEligible,
          classesNeededFor75: computeClassesNeeded(uniqueHeld, attended)
        };
      });

      const aggPct = totalHeldAll > 0 ? ((totalAttendedAll / totalHeldAll) * 100.0) : 0;

      summaryData = {
        student: student,
        summaries: summaries,
        aggregate: {
          totalHeld: totalHeldAll,
          totalAttended: totalAttendedAll,
          totalAbsent: totalAbsentAll,
          percentage: aggPct.toFixed(2),
          isEligible: aggPct >= 75.0
        }
      };
    }

    // Render Aggregate & Verdict
    const aggPctNum = parseFloat(summaryData.aggregate.percentage);
    document.getElementById('sDashAggregateVal').textContent = `${aggPctNum.toFixed(1)}%`;
    document.getElementById('sDashHeldAttendedRatio').textContent = `${summaryData.aggregate.totalAttended} / ${summaryData.aggregate.totalHeld} Lectures Attended`;

    const isEligible = summaryData.aggregate.isEligible;
    const verdictBox = document.getElementById('sDashVerdictBox');

    if (isEligible) {
      verdictBox.className = 'verdict-card verdict-pass';
      document.getElementById('sDashVerdictIcon').textContent = '🎓';
      document.getElementById('sDashVerdictTitle').textContent = 'ELIGIBLE TO APPEAR IN SEMESTER END EXAMINATIONS';
      document.getElementById('sDashVerdictDesc').textContent = 'Your cumulative attendance across all 5 curriculum subjects satisfies the mandatory 75% university requirement. Hall ticket clearance confirmed.';
    } else {
      verdictBox.className = 'verdict-card verdict-shortage';
      document.getElementById('sDashVerdictIcon').textContent = '⚠️';
      document.getElementById('sDashVerdictTitle').textContent = 'ATTENDANCE SHORTAGE CAUTION (< 75%)';
      document.getElementById('sDashVerdictDesc').textContent = 'Your cumulative attendance across curriculum subjects is below the mandatory 75% threshold. Semester hall ticket issuance is subject to institutional condonation approval.';
    }

    // Render 5 Subject Cards
    const cardsGrid = document.getElementById('sDashSubjectCardsGrid');
    cardsGrid.innerHTML = '';

    summaryData.summaries.forEach(s => {
      const isSubEligible = s.isEligible;
      const card = document.createElement('div');
      card.className = 'subject-item-card';

      card.innerHTML = `
        <div>
          <div class="sic-top">
            <span class="sic-code">${s.subjectCode}</span>
            <span class="badge-status ${isSubEligible ? 'badge-eligible' : 'badge-shortage'}">
              ${isSubEligible ? 'Eligible' : 'Shortage'}
            </span>
          </div>
          <h4 class="sic-title">${s.subjectName}</h4>
          <p class="sic-faculty">Faculty: ${s.facultyName || (localSubjects.find(sub=>sub.subjectCode===s.subjectCode)||{}).facultyName || 'Faculty'}</p>

          <div class="sic-pct-row">
            <span class="sic-pct ${isSubEligible ? 'text-green' : 'text-red'}">${parseFloat(s.percentage).toFixed(1)}%</span>
            <span style="font-size:0.75rem; color:var(--text-muted);">${s.presentClasses} / ${s.totalClasses} Attended</span>
          </div>

          <div class="sic-progress-wrap">
            <div class="sic-progress-bar" style="width:${parseFloat(s.percentage).toFixed(1)}%; background:${isSubEligible ? '#16a34a' : '#dc2626'};"></div>
          </div>
        </div>

        <div class="sic-bottom">
          <strong class="${isSubEligible ? 'text-green' : 'text-red'}">
            ${isSubEligible ? '✓ Goal Satisfied' : `⚠️ Need +${s.classesNeededFor75} classes for 75%`}
          </strong>
        </div>
      `;

      cardsGrid.appendChild(card);
    });

    // Render Statement Table
    const tbodyStatement = document.getElementById('tbodyStudentStatement');
    tbodyStatement.innerHTML = '';

    summaryData.summaries.forEach(s => {
      const isSubEligible = s.isEligible;
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${s.subjectCode}</strong></td>
        <td><strong>${s.subjectName}</strong></td>
        <td>${s.totalClasses}</td>
        <td class="text-green font-bold">${s.presentClasses}</td>
        <td class="text-red font-bold">${s.absentClasses}</td>
        <td><strong class="${isSubEligible ? 'text-green' : 'text-red'}">${parseFloat(s.percentage).toFixed(1)}%</strong></td>
        <td>
          <span class="badge-status ${isSubEligible ? 'badge-eligible' : 'badge-shortage'}">
            ${isSubEligible ? 'Eligible' : 'Shortage'}
          </span>
        </td>
        <td>
          <span class="${isSubEligible ? 'text-green' : 'text-red'} font-bold">
            ${isSubEligible ? 'Goal Met' : `+${s.classesNeededFor75} classes`}
          </span>
        </td>
      `;
      tbodyStatement.appendChild(tr);
    });

    // Statement Total Row
    const totalTr = document.createElement('tr');
    totalTr.style.background = '#f1f5f9';
    totalTr.style.fontWeight = '700';
    totalTr.innerHTML = `
      <td colspan="2"><strong>5-SUBJECTS OVERALL TOTAL</strong></td>
      <td><strong>${summaryData.aggregate.totalHeld}</strong></td>
      <td class="text-green"><strong>${summaryData.aggregate.totalAttended}</strong></td>
      <td class="text-red"><strong>${summaryData.aggregate.totalAbsent}</strong></td>
      <td><strong class="${isEligible ? 'text-green' : 'text-red'}">${summaryData.aggregate.percentage}%</strong></td>
      <td colspan="2">
        <span class="badge-status ${isEligible ? 'badge-eligible' : 'badge-shortage'}">
          ${isEligible ? 'EXAM ELIGIBLE' : 'SHORTAGE'}
        </span>
      </td>
    `;
    tbodyStatement.appendChild(totalTr);

    // Load Lecture Log
    loadStudentHistoryLog(student.rollNo, document.getElementById('sDashHistorySubSelect').value);

    // Switch view to Student Dashboard
    showView(Views.studentDashboard);
  }

  async function loadStudentHistoryLog(rollNo, subjectCode = 'ALL') {
    const tbody = document.getElementById('tbodyStudentHistory');
    tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; padding:12px;">Loading history...</td></tr>';

    let records = [];

    if (isBackendOnline) {
      try {
        const res = await fetch(`${API_BASE}/api/attendance/history?rollNo=${rollNo}&subjectCode=${subjectCode}`);
        if (res.ok) records = await res.json();
      } catch (e) {}
    }

    if (!records || records.length === 0) {
      records = localAttendanceRecords
        .filter(r => r.rollNo === rollNo && (subjectCode === 'ALL' || r.subjectCode.toUpperCase() === subjectCode.toUpperCase()))
        .sort((a, b) => b.date.localeCompare(a.date));
    }

    if (records.length === 0) {
      tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; padding:16px; color:var(--text-muted);">No records found for selected subject.</td></tr>';
      return;
    }

    tbody.innerHTML = '';
    records.forEach(r => {
      const isP = r.isPresent;
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${r.date}</td>
        <td><span class="tag-subject">${r.subjectCode}</span></td>
        <td>
          <span class="badge-status ${isP ? 'badge-eligible' : 'badge-shortage'}">
            ${isP ? 'Present' : 'Absent'}
          </span>
        </td>
      `;
      tbody.appendChild(tr);
    });
  }

  // =========================================================================
  // Bootstrap Application
  // =========================================================================

  async function init() {
    seedOfflineDataIfEmpty();
    initPortalNavigation();
    initTeacherLogin();
    initStudentLogin();
    initTeacherDashboard();

    await checkBackendStatus();
    setInterval(checkBackendStatus, 15000);
  }

  document.addEventListener('DOMContentLoaded', init);

})();
