import javax.swing.*;
import java.awt.*;

/** Base class for every screen inside the portal. */
public abstract class Page extends JPanel {
    Page() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
    }
    abstract void load(String user);
}