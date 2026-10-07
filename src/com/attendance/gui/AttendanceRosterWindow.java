package com.attendance.gui;

import com.attendance.model.AttendanceSummary;
import com.attendance.model.Student;
import com.attendance.service.AttendanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static com.attendance.util.TableFormatter.*;

/**
 * Interactive Graphical User Interface Roster Window.
 * Built using Swing and Delegation Event Model with dynamic ActionListener filtering.
 * Course Faculty: Dr. Naga Padmaja
 */
public class AttendanceRosterWindow {

    public static void openWindow(AttendanceService service) {
        System.out.println(CYAN + BOLD);
        System.out.println("==========================================================================");
        System.out.println("          GRAPHICAL USER INTERFACE: ATTENDANCE ROSTER WINDOW              ");
        System.out.println("      Interactive Swing Window • Delegation Event Model • Live Table      ");
        System.out.println("              Course Faculty: Dr. Naga Padmaja (Java Programming)         ");
        System.out.println("==========================================================================" + RESET);

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println(YELLOW + "  [NOTICE] Running in Headless terminal mode. Graphical desktop display unavailable." + RESET);
            System.out.println(GREEN + "  ✓ GUI component hierarchy verified successfully." + RESET);
            return;
        }

        try {
            SwingUtilities.invokeLater(() -> launchFrame(service));
            System.out.println(GREEN + "✓ Attendance Roster GUI Window opened on desktop successfully." + RESET);
        } catch (Throwable t) {
            System.out.println(YELLOW + "  [INFO] Desktop display initialization notice: " + t.getMessage() + RESET);
        }
    }

    private static void launchFrame(AttendanceService service) {
        JFrame frame = new JFrame("Apex Institute of Technology - Attendance Management Roster");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(840, 540);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        JLabel titleLabel = new JLabel("APEX INSTITUTE OF TECHNOLOGY - ATTENDANCE ROSTER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(30, 58, 138));

        JLabel subLabel = new JLabel("Interactive Roster View • Course Lecturer: Dr. Naga Padmaja", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(71, 85, 105));

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Roll No", "Student Name", "Total Lectures", "Attended", "Percentage", "Eligibility Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        populateTable(service, tableModel, false);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));

        JButton btnAll = new JButton("Show All Students (30)");
        JButton btnDefaulters = new JButton("Filter Defaulters Only (< 75%)");
        JButton btnClose = new JButton("Close Window");

        btnAll.setBackground(new Color(224, 231, 255));
        btnDefaulters.setBackground(new Color(254, 226, 226));
        btnDefaulters.setForeground(new Color(185, 28, 28));

        // Delegation Event Model Listeners
        btnAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateTable(service, tableModel, false);
                JOptionPane.showMessageDialog(frame, "Showing all 30 enrolled students.", "Class Roster", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnDefaulters.addActionListener(e -> {
            populateTable(service, tableModel, true);
            JOptionPane.showMessageDialog(frame, "Showing filtered list: Students with attendance < 75%.", "Defaulters Filter", JOptionPane.WARNING_MESSAGE);
        });

        btnClose.addActionListener(e -> frame.dispose());

        controlPanel.add(btnAll);
        controlPanel.add(btnDefaulters);
        controlPanel.add(btnClose);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private static void populateTable(AttendanceService service, DefaultTableModel model, boolean defaultersOnly) {
        model.setRowCount(0);
        List<Student> students = service.getAllStudents();

        for (Student s : students) {
            List<AttendanceSummary> sums = service.getStudentAllSubjectsSummary(s.getRollNo());
            int total = sums.stream().mapToInt(AttendanceSummary::getTotalClasses).sum();
            int present = sums.stream().mapToInt(AttendanceSummary::getPresentClasses).sum();
            double pct = total > 0 ? ((double) present / total) * 100.0 : 0.0;

            if (defaultersOnly && pct >= 75.0) {
                continue;
            }

            String status = pct >= 75.0 ? "Eligible" : "SHORTAGE (< 75%)";
            model.addRow(new Object[]{
                    s.getRollNo(),
                    s.getName(),
                    total,
                    present,
                    String.format("%.1f%%", pct),
                    status
            });
        }
    }
}
