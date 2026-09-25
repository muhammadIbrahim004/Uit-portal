import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RegistrationPanel extends Page {
    private final JTable avail = Theme.table("Code", "Course", "Credits", "Teacher", "Timings");
    private final JTable mine = Theme.table("Code", "Course", "Credits", "Teacher", "Timings");
    private final JLabel msg = Theme.label(" ", Font.BOLD, 13, Theme.MUTED);
    private String user;

    RegistrationPanel() {
        JPanel col = new JPanel(new GridLayout(2, 1, 0, 18));
        col.setOpaque(false);
        col.add(section("Available courses", avail, "Register selected", Theme.PRIMARY, true));
        col.add(section("Registered - " + StudentService.CURRENT, mine, "Drop selected", new Color(239, 68, 68), false));
        msg.setBorder(new EmptyBorder(10, 0, 0, 0));
        add(col, BorderLayout.CENTER);
        add(msg, BorderLayout.SOUTH);
    }

    private JComponent section(String title, JTable t, String text, Color color, boolean register) {
        Theme.Round c = Theme.card();
        c.add(Theme.title(title), BorderLayout.NORTH);
        c.add(Theme.scroll(t), BorderLayout.CENTER);
        JButton b = Theme.button(text, color);
        b.addActionListener(e -> act(t, register));
        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        s.setOpaque(false);
        s.setBorder(new EmptyBorder(12, 0, 0, 0));
        s.add(b);
        c.add(s, BorderLayout.SOUTH);
        return c;
    }

    private void act(JTable t, boolean register) {
        int row = t.getSelectedRow();
        if (row < 0) { show("Select a course first.", false); return; }
        String code = (String) t.getValueAt(row, 0);
        String res = register ? StudentService.register(user, code) : StudentService.drop(user, code);
        load(user);
        show(res, res.startsWith("Registered") || res.startsWith("Dropped"));
    }

    private void show(String text, boolean ok) {
        msg.setForeground(ok ? new Color(22, 163, 74) : new Color(220, 38, 38));
        msg.setText(text);
    }

    @Override void load(String u) {
        user = u;
        Theme.fill(avail, StudentService.available(u));
        Theme.fill(mine, StudentService.registered(u));
    }
}