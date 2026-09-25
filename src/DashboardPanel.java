import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DashboardPanel extends Page {
    private final JLabel balance = new JLabel(), cgpa = new JLabel(), count = new JLabel(), attendance = new JLabel();
    private final JPanel list = new JPanel();

    DashboardPanel() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.gridx = 0; g.weightx = 0.4; g.insets = new Insets(0, 0, 0, 15);
        body.add(statsCard(), g);
        g.gridx = 1; g.weightx = 0.6; g.insets = new Insets(0, 15, 0, 0);
        body.add(coursesCard(), g);
        add(body, BorderLayout.CENTER);
    }

    private JComponent statsCard() {
        Theme.Round c = new Theme.Round(new Color(238, 240, 250), 16);
        c.setLayout(new BorderLayout());
        Theme.Round head = new Theme.Round(new Color(244, 78, 98), 16);
        head.setLayout(new BorderLayout());
        head.setBorder(new EmptyBorder(22, 26, 22, 26));
        head.add(Theme.label("My Stats", Font.BOLD, 20, Color.WHITE));

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(16, 16, 16, 16));
        grid.add(tile("Current<br>Balance", balance, new Color(255, 243, 214), new Color(245, 158, 11)));
        grid.add(tile("CGPA", cgpa, new Color(224, 238, 255), new Color(59, 130, 246)));
        grid.add(tile("Registered<br>Courses", count, new Color(255, 226, 232), new Color(244, 63, 94)));
        grid.add(tile("Average<br>Attendance", attendance, new Color(204, 247, 242), new Color(20, 184, 166)));
        c.add(head, BorderLayout.NORTH);
        c.add(grid, BorderLayout.CENTER);
        return c;
    }

    private JComponent tile(String title, JLabel value, Color bg, Color fg) {
        Theme.Round t = new Theme.Round(bg, 18);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel l = Theme.label("<html>" + title + "</html>", Font.BOLD, 17, fg);
        value.setFont(Theme.FONT.deriveFont(Font.BOLD, 20f));
        value.setForeground(fg);
        l.setAlignmentX(0f);
        value.setAlignmentX(0f);
        t.add(Box.createVerticalGlue());
        t.add(l);
        t.add(Box.createVerticalStrut(4));
        t.add(value);
        return t;
    }

    private JComponent coursesCard() {
        Theme.Round c = Theme.card();
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel t = Theme.label("My Courses", Font.BOLD, 20, Theme.TEXT);
        JLabel s = Theme.label(StudentService.CURRENT, Font.PLAIN, 13, Theme.MUTED);
        t.setAlignmentX(0f);
        s.setAlignmentX(0f);
        top.add(t);
        top.add(s);
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        c.add(top, BorderLayout.NORTH);
        c.add(Theme.scroll(list), BorderLayout.CENTER);
        return c;
    }

    private JPanel row(Object[] r) {
        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(9, 0, 9, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        p.add(Theme.label("<html><b>" + r[1] + "</b><br><span style='color:#78808f'>Timings: " + r[5] + "</span></html>",
                Font.PLAIN, 14, Theme.TEXT), BorderLayout.CENTER);
        p.add(Theme.label(r[3] + "   " + r[4], Font.PLAIN, 13, Theme.MUTED), BorderLayout.EAST);
        return p;
    }

    @Override void load(String user) {
        balance.setText(String.format("(%,d)", StudentService.balance(user)));
        cgpa.setText(String.format("(%.2f)", StudentService.cgpa(user)));
        count.setText("(" + StudentService.registeredCount(user) + ")");
        attendance.setText(String.format("(%.2f)", StudentService.avgAttendance(user)));
        list.removeAll();
        for (Object[] r : StudentService.currentCourses(user)) list.add(row(r));
        list.revalidate();
        list.repaint();
    }
}