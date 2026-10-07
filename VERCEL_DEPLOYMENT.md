# 🚀 How to Push to GitHub & Deploy to Vercel (Access Anywhere)

This guide walks you through linking your project with **Git/GitHub** and deploying it to **Vercel** so you can open the attendance portal from **any mobile phone, laptop, or tablet anywhere in the world** with a public HTTPS link!

---

## ⚡ Quick 1-Click Method (Using `push_to_github.bat`)

1. **Create an empty repository on GitHub**:
   - Go to: [https://github.com/new](https://github.com/new)
   - Repository Name: `college-attendance-management-system` (or any name you like)
   - Set to **Public**
   - **Do NOT** check "Add a README" or ".gitignore" (the project already has them)
   - Click **Create repository**
   - Copy the repository URL (e.g., `https://github.com/your-username/college-attendance-management-system.git`)

2. **Run the script**:
   - Double-click [**`push_to_github.bat`**](file:///c:/Users/Ruthvik/New%20folder%20(3)/push_to_github.bat)
   - Paste your GitHub repository URL when prompted
   - Press Enter
   - *Your code is now pushed to GitHub!*

---

## 🌐 Deploying to Vercel (Takes 30 Seconds)

1. Open [**https://vercel.com/new**](https://vercel.com/new) in your browser.
2. Sign in with your **GitHub** account.
3. Click **"Import"** next to your `college-attendance-management-system` repository.
4. Leave all settings as default:
   - Framework Preset: *Other* (detected automatically via `vercel.json`)
   - Output Directory: *web* (already configured in `vercel.json`)
5. Click **Deploy**!

Within ~15 seconds, Vercel gives you a live public URL such as:
`https://college-attendance-management-system-xxx.vercel.app`

---

## 🔔 Student Warning Letter Delivery Feature

- **Teacher Portal**:
  - In the **Defaulters List (< 75%)** tab, click:
    `📢 Send Notice to Students' Dashboards`
  - Warning notices are immediately dispatched to all defaulters.
- **Student Portal**:
  - Whenever any student with attendance < 75% logs into their dashboard (e.g. Roll `126 - Zoya Siddiqui`, Roll `128 - Bharat Subramanian`, Roll `130 - Deepa Mehta`):
  - A prominent red alert appears on their dashboard:
    `🚨 Dean's Action Notice: Official Attendance Shortage Warning Letter Issued`
  - Clicking `📄 View & Print My Warning Letter` opens their personalized official warning letter addressed to them, showing their course, deficit, and required recovery classes.
  - Students can print or save their copy as PDF directly!
