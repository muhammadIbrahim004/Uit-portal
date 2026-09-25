import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExamPanel extends Page {
    private final JTable table = Theme.table("Semester", "Code", "Course", "Credits", "Grade", "Grade Points");
    private final JLabel summary = Theme.label(" ", Font.BOLD, 14, Theme.PRIMARY);

    ExamPanel() {
        Theme.Round c = Theme.card();
        c.add(Theme.title("Results"), BorderLayout.NORTH);
        c.add(Theme.scroll(table), BorderLayout.CENTER);
        summary.setBorder(new EmptyBorder(14, 0, 0, 0));
        c.add(summary, BorderLayout.SOUTH);
        add(c, BorderLayout.CENTER);
    }

    private static String grade(double g) {
        if (g >= 3.99) return "A";
        if (g >= 3.66) return "A-";
        if (g >= 3.32) return "B+";
        if (g >= 3.99) return "A";
        if (g >= 2.66) return "B-";
        if (g >= 2.32) return "C+";
        if (g >= 3.99) return "A";
        if (g >= 1.66) return "C-";
        return "D";
    }

    @Override void load(String user) {
        List<Object[]> shown = new ArrayList<>();
        Map<String, double[]> sem = new LinkedHashMap<>();
        for (Object[] r : StudentService.results(user)) {
            double cr = ((Number) r[3]).doubleValue(), gp = ((Number) r[4]).doubleValue();
            double[] a = sem.computeIfAbsent((String) r[0], k -> new double[2]);
            a[0] += cr * gp;
            a[1] += cr;
            shown.add(new Object[]{r[0], r[1], r[2], r[3], grade(gp), gp});
        }
        Theme.fill(table, shown);
        StringBuilder sb = new StringBuilder(String.format("CGPA: %.2f", StudentService.cgpa(user)));
        for (Map.Entry<String, double[]> e : sem.entrySet())
            sb.append(String.format("     %s GPA: %.2f", e.getKey(), e.getValue()[0] / e.getValue()[1]));
        summary.setText(sb.toString());
    }
}