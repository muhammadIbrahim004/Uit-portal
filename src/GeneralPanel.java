import javax.swing.*;
import java.awt.*;

public class GeneralPanel extends Page {
    private final JLabel[] vals = new JLabel[5];
    private final JPasswordField oldPw = new JPasswordField(), newPw = new JPasswordField(), confirm = new JPasswordField();
    private final JLabel msg = Theme.label(" ", Font.BOLD, 13, Theme.MUTED);
    private String user;

    GeneralPanel() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);

        Theme.Round info = Theme.card();
        info.add(Theme.title("My Profile"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 16));
        grid.setOpaque(false);
        String[] names = {"Name", "Registration No", "Enrollment No", "Program", "Section"};
        for (int i = 0; i < names.length; i++) {
            vals[i] = Theme.label(" ", Font.BOLD, 14, Theme.TEXT);
            grid.add(Theme.label(names[i], Font.PLAIN, 14, Theme.MUTED));
            grid.add(vals[i]);
        }
        JPanel w1 = new JPanel(new BorderLayout());
        w1.setOpaque(false);
        w1.add(grid, BorderLayout.NORTH);
        info.add(w1, BorderLayout.CENTER);

        Theme.Round pw = Theme.card();
        pw.add(Theme.title("Change Password"), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setOpaque(false);
        form.add(Theme.label("Current password", Font.PLAIN, 13, Theme.MUTED));
        form.add(oldPw);
        form.add(Theme.label("New password (min 8 characters)", Font.PLAIN, 13, Theme.MUTED));
        form.add(newPw);
        form.add(Theme.label("Confirm new password", Font.PLAIN, 13, Theme.MUTED));
        form.add(confirm);
        JButton save = Theme.button("Update password", Theme.PRIMARY);
        save.addActionListener(e -> save());
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        btn.setOpaque(false);
        btn.add(save);
        form.add(btn);
        form.add(msg);
        JPanel w2 = new JPanel(new BorderLayout());
        w2.setOpaque(false);
        w2.add(form, BorderLayout.NORTH);
        pw.add(w2, BorderLayout.CENTER);

        row.add(info);
        row.add(pw);
        add(row, BorderLayout.CENTER);
    }

    private void save() {
        String n = new String(newPw.getPassword());
        if (!n.equals(new String(confirm.getPassword()))) {
            msg.setForeground(new Color(220, 38, 38));
            msg.setText("New passwords do not match.");
            return;
        }
        String res = StudentService.changePassword(user, new String(oldPw.getPassword()), n);
        boolean ok = res.startsWith("Password updated");
        msg.setForeground(ok ? new Color(22, 163, 74) : new Color(220, 38, 38));
        msg.setText(res);
        if (ok) { oldPw.setText(""); newPw.setText(""); confirm.setText(""); }
    }

    @Override void load(String u) {
        user = u;
        String[] p = StudentService.profile(u);
        vals[0].setText(p[0]);
        vals[1].setText(u);
        vals[2].setText(p[3]);
        vals[3].setText(p[1]);
        vals[4].setText(p[2]);
        msg.setText(" ");
    }
}