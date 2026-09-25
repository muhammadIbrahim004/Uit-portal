import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FeePanel extends Page {
    private final JTable table = Theme.table("Semester", "Amount", "Status");
    private final JLabel due = Theme.label(" ", Font.BOLD, 15, Theme.TEXT);
    private final JButton pay = Theme.button("Pay outstanding", new Color(34, 197, 94));
    private String user;

    FeePanel() {
        Theme.Round c = Theme.card();
        c.add(Theme.title("Fee Details"), BorderLayout.NORTH);
        c.add(Theme.scroll(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(14, 0, 0, 0));
        bottom.add(due, BorderLayout.WEST);
        bottom.add(pay, BorderLayout.EAST);
        c.add(bottom, BorderLayout.SOUTH);
        add(c, BorderLayout.CENTER);

        pay.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Pay Rs. " + String.format("%,d", StudentService.balance(user)) + " now?",
                    "Confirm payment", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                StudentService.payFees(user);
                load(user);
            }
        });
    }

    @Override void load(String u) {
        user = u;
        List<Object[]> rows = new ArrayList<>();
        for (Object[] r : StudentService.fees(u))
            rows.add(new Object[]{r[1], String.format("Rs. %,d", ((Number) r[2]).intValue()),
                    ((Number) r[3]).intValue() == 1 ? "Paid" : "Due"});
        Theme.fill(table, rows);
        int b = StudentService.balance(u);
        due.setText(String.format("Outstanding balance: Rs. %,d", b));
        pay.setEnabled(b > 0);
    }
}