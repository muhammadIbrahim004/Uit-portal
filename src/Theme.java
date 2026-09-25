import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** Shared colors and small UI factories. */
public class Theme {
    static final Color PRIMARY = new Color(33, 150, 243);
    static final Color BG = new Color(243, 244, 250);
    static final Color TEXT = new Color(31, 41, 55);
    static final Color MUTED = new Color(120, 128, 145);
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);

    static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(FONT.deriveFont(style, size));
        l.setForeground(color);
        return l;
    }

    static JLabel title(String text) {
        JLabel l = label(text, Font.BOLD, 20, TEXT);
        l.setBorder(new EmptyBorder(0, 0, 14, 0));
        return l;
    }

    static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT.deriveFont(Font.BOLD, 14f));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Panel with rounded corners. */
    static class Round extends JPanel {
        private final Color fill;
        private final int arc;
        Round(Color fill, int arc) { this.fill = fill; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static Round card() {
        Round r = new Round(Color.WHITE, 16);
        r.setLayout(new BorderLayout());
        r.setBorder(new EmptyBorder(22, 24, 22, 24));
        return r;
    }

    static JTable table(String... cols) {
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setFont(FONT);
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(235, 237, 243));
        t.setSelectionBackground(new Color(224, 238, 255));
        t.setSelectionForeground(TEXT);
        JTableHeader h = t.getTableHeader();
        h.setFont(FONT.deriveFont(Font.BOLD, 13f));
        h.setBackground(new Color(248, 249, 252));
        h.setForeground(MUTED);
        h.setReorderingAllowed(false);
        return t;
    }

    static void fill(JTable t, List<Object[]> rows) {
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        m.setRowCount(0);
        for (Object[] r : rows) m.addRow(r);
    }

    static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }
}