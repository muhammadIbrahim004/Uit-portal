import javax.swing.*;
import java.awt.*;

public class CoursesPanel extends Page {
    private final JTable table = Theme.table("Code", "Course", "Credits", "Teacher", "Room", "Timings", "Attendance %");

    CoursesPanel() {
        Theme.Round c = Theme.card();
        c.add(Theme.title("My Courses - " + StudentService.CURRENT), BorderLayout.NORTH);
        c.add(Theme.scroll(table), BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    @Override void load(String user) {
        Theme.fill(table, StudentService.currentCourses(user));
    }
}