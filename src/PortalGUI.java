import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class PortalGUI extends JFrame {
    private static final Color RED = new Color(220, 38, 38), GREEN = new Color(22, 163, 74);
    private static final Color NAV_ON = new Color(238, 245, 255);

    /** Shrinks in half-steps instead of one big jump, which keeps fine detail (text, thin rings) much sharper. */
    private static BufferedImage smoothDownscale(BufferedImage src, int targetSize) {
        BufferedImage img = src;
        int w = img.getWidth(), h = img.getHeight();
        while (w / 2 >= targetSize && h / 2 >= targetSize) {
            w /= 2;
            h /= 2;
            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(img, 0, 0, w, h, null);
            g2.dispose();
            img = tmp;
        }
        return img;
    }

    /** Loads a non-square logo (e.g. a wide "U | UIT UNIVERSITY" lockup), scaled to a fixed height, aspect preserved. */
    private static ImageIcon loadWideLogo(int targetHeight) {
        for (String name : new String[]{"UIT_University_Logo.jpg", "UIT_University_Logo.png", "logo_top.png", "logo_top.jpg", "logo_wide.png", "logo_wide.jpg"}) {
            File f = new File(name);
            System.out.println("[PortalGUI] looking for " + f.getAbsolutePath() + "  exists=" + f.exists());
            if (!f.exists()) continue;
            try {
                BufferedImage src = ImageIO.read(f);
                if (src == null) { System.out.println("[PortalGUI] " + name + " could not be decoded"); continue; }
                int targetWidth = Math.round(src.getWidth() * (targetHeight / (float) src.getHeight()));
                BufferedImage img = src;
                int w = img.getWidth(), h = img.getHeight();
                while (w / 2 >= targetWidth && h / 2 >= targetHeight) {
                    w /= 2; h /= 2;
                    BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = tmp.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.drawImage(img, 0, 0, w, h, null);
                    g2.dispose();
                    img = tmp;
                }
                BufferedImage out = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = out.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
                g2.dispose();
                System.out.println("[PortalGUI] " + name + " loaded successfully");
                return new ImageIcon(out);
            } catch (Exception e) {
                System.out.println("[PortalGUI] FAILED to decode " + name + ": " + e);
            }
        }
        return null;
    }

    private static ImageIcon loadLogo(int size) {
        int render = size * 3;   // draw bigger, then shrink smoothly -> crisper final result
        for (String name : new String[]{"logo.png", "logo.jpg"}) {
            File f = new File(name);
            System.out.println("[PortalGUI] looking for " + f.getAbsolutePath() + "  exists=" + f.exists());
            if (!f.exists()) continue;
            try {
                BufferedImage src = ImageIO.read(f);
                if (src == null) { System.out.println("[PortalGUI] " + name + " exists but could not be decoded"); continue; }
                int s = Math.min(src.getWidth(), src.getHeight());
                BufferedImage square = src.getSubimage((src.getWidth() - s) / 2, (src.getHeight() - s) / 2, s, s);
                BufferedImage big = smoothDownscale(square, render);
                BufferedImage circle = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = circle.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                // Paint an anti-aliased circle first, then composite the photo only where that circle is opaque.
                // (setClip() on a shape is hard-edged in Java2D and produces a jagged/dashed-looking border.)
                g2.setColor(Color.WHITE);
                g2.fill(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                g2.setComposite(AlphaComposite.SrcAtop);
                g2.drawImage(big, 0, 0, size, size, null);
                g2.dispose();
                System.out.println("[PortalGUI] " + name + " loaded successfully");
                return new ImageIcon(circle);
            } catch (Exception e) {
                System.out.println("[PortalGUI] FAILED to decode " + name + ": " + e);
            }
        }
        return null;
    }

    private final Register_login auth = new Register_login();
    private final Preferences prefs = Preferences.userNodeForPackage(PortalGUI.class);
    private final CardLayout rootCards = new CardLayout(), formCards = new CardLayout(), pageCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards), forms = new JPanel(formCards), pageHolder = new JPanel(pageCards);

    private final JTextField loginUser = new JTextField(), regName = new JTextField(), regUser = new JTextField();
    private final JPasswordField loginPass = new JPasswordField(), regPass = new JPasswordField(), regConfirm = new JPasswordField();
    private final JCheckBox remember = new JCheckBox("Remember me");
    private final JLabel loginMsg = new JLabel(" "), regMsg = new JLabel(" ");

    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, JButton> nav = new LinkedHashMap<>();
    private final JLabel welcome = new JLabel(), avatarText = new JLabel("", SwingConstants.CENTER);
    private final JLabel pageTitle = new JLabel(), idInfo = new JLabel();
    private String currentUser;

    public PortalGUI() {
        super("Student Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        root.add(buildAuth(), "auth");
        root.add(buildApp(), "app");
        setContentPane(root);
        String saved = prefs.get("user", "");
        if (!saved.isEmpty()) { loginUser.setText(saved); remember.setSelected(true); }
    }

    // =================================================================== LOGIN
    private JPanel buildAuth() {
        JPanel p = new JPanel(new BorderLayout());
        forms.setBackground(Color.WHITE);
        forms.setPreferredSize(new Dimension(430, 0));
        forms.add(loginForm(), "login");
        forms.add(registerForm(), "register");
        p.add(forms, BorderLayout.WEST);
        p.add(new Backdrop(), BorderLayout.CENTER);
        return p;
    }

    /** Right-hand picture. Drop a file named login_bg.jpg in the project folder to use a photo. */
    static class Backdrop extends JPanel {
        private BufferedImage img;
        Backdrop() {
            File f = new File("login_bg.jpg");
            System.out.println("[PortalGUI] looking for " + f.getAbsolutePath() + "  exists=" + f.exists());
            if (f.exists()) {
                try {
                    img = ImageIO.read(f);
                    System.out.println("[PortalGUI] login_bg.jpg decoded=" + (img != null));
                } catch (Exception e) {
                    System.out.println("[PortalGUI] FAILED to decode login_bg.jpg: " + e);
                }
            }
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();
            if (img != null) {
                double s = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
                int iw = (int) (img.getWidth() * s), ih = (int) (img.getHeight() * s);
                g2.drawImage(img, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
                return;
            }
            g2.setPaint(new GradientPaint(0, 0, new Color(30, 64, 175), w, h, new Color(14, 165, 233)));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 38));
            g2.fillOval(w - 260, -120, 420, 420);
            g2.fillOval(-140, h - 240, 380, 380);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
            g2.drawString("Learn. Track. Grow.", 70, h / 2);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.drawString("Tip: put a photo named login_bg.jpg in your project folder to show it here.", 70, h / 2 + 34);
        }
    }

    private JPanel formBase() {
        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBackground(Color.WHITE);
        f.setBorder(new EmptyBorder(30, 50, 30, 50));
        return f;
    }

    private JPanel header(String sub) {
        JPanel h = new JPanel();
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setOpaque(false);
        h.setAlignmentX(0f);
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        ImageIcon logoImg = loadLogo(140);
        Theme.Round logoDisplay = new Theme.Round(logoImg == null ? Theme.PRIMARY : Color.WHITE, 140);
        logoDisplay.setLayout(new GridBagLayout());
        logoDisplay.setPreferredSize(new Dimension(140, 140));
        logoDisplay.setMaximumSize(new Dimension(140, 140));
        logoDisplay.add(logoImg != null ? new JLabel(logoImg) : Theme.label("SP", Font.BOLD, 28, Color.WHITE));
        logoDisplay.setAlignmentX(0.5f);
        JLabel t = Theme.label("STUDENT PORTAL", Font.BOLD, 24, Theme.TEXT);
        JLabel s = Theme.label(sub, Font.PLAIN, 13, Theme.MUTED);
        t.setAlignmentX(0.5f);
        s.setAlignmentX(0.5f);
        h.add(logoDisplay);
        h.add(Box.createVerticalStrut(14));
        h.add(t);
        h.add(Box.createVerticalStrut(4));
        h.add(s);
        h.add(Box.createVerticalStrut(22));
        return h;
    }

    private JPanel field(String caption, JTextField tf) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(0f);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        JLabel l = Theme.label(caption, Font.PLAIN, 12, Theme.MUTED);
        l.setAlignmentX(0f);
        tf.setFont(Theme.FONT.deriveFont(15f));
        tf.setAlignmentX(0f);
        tf.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(209, 213, 219)), new EmptyBorder(6, 0, 6, 0)));
        p.add(l);
        p.add(tf);
        return p;
    }

    private JCheckBox showBox(JPasswordField... fs) {
        JCheckBox cb = new JCheckBox("Show password");
        cb.setFont(Theme.FONT.deriveFont(13f));
        cb.setForeground(Theme.MUTED);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        char echo = fs[0].getEchoChar();
        cb.addActionListener(e -> { for (JPasswordField f : fs) f.setEchoChar(cb.isSelected() ? (char) 0 : echo); });
        return cb;
    }

    private JPanel optionsRow(JComponent... items) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        r.setOpaque(false);
        r.setAlignmentX(0f);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        for (JComponent c : items) r.add(c);
        return r;
    }

    private JPanel buttonRow(JButton b, JLabel msg) {
        msg.setFont(Theme.FONT.deriveFont(Font.BOLD, 13f));
        msg.setAlignmentX(0f);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        r.setOpaque(false);
        r.setAlignmentX(0f);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        r.add(b);
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.setAlignmentX(0f);
        wrap.add(msg);
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(r);
        return wrap;
    }

    private JButton link(String text, Runnable r) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT);
        b.setForeground(Theme.PRIMARY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setAlignmentX(0f);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> r.run());
        return b;
    }

    private JPanel loginForm() {
        JPanel f = formBase();
        f.add(header("Enter your Registration No and Password"));
        f.add(field("Registration No", loginUser));
        f.add(Box.createVerticalStrut(10));
        f.add(field("Password", loginPass));
        remember.setFont(Theme.FONT.deriveFont(13f));
        remember.setForeground(Theme.MUTED);
        remember.setOpaque(false);
        f.add(Box.createVerticalStrut(8));
        f.add(optionsRow(remember, showBox(loginPass)));
        JButton signIn = Theme.button("Sign In", Theme.PRIMARY);
        signIn.addActionListener(e -> doLogin());
        loginUser.addActionListener(e -> loginPass.requestFocus());
        loginPass.addActionListener(e -> doLogin());
        f.add(Box.createVerticalStrut(10));
        f.add(buttonRow(signIn, loginMsg));
        f.add(Box.createVerticalStrut(10));
        f.add(link("New student? Create an account", () -> { clearMsgs(); formCards.show(forms, "register"); }));
        f.add(Box.createVerticalGlue());
        return f;
    }

    private JPanel registerForm() {
        JPanel f = formBase();
        f.add(header("Create your portal account"));
        f.add(field("Full name", regName));
        f.add(Box.createVerticalStrut(6));
        f.add(field("Registration No", regUser));
        f.add(Box.createVerticalStrut(6));
        f.add(field("Password (min 8 characters)", regPass));
        f.add(Box.createVerticalStrut(6));
        f.add(field("Confirm password", regConfirm));
        f.add(Box.createVerticalStrut(6));
        f.add(optionsRow(showBox(regPass, regConfirm)));
        JButton reg = Theme.button("Register", Theme.PRIMARY);
        reg.addActionListener(e -> doRegister());
        regConfirm.addActionListener(e -> doRegister());
        f.add(buttonRow(reg, regMsg));
        f.add(Box.createVerticalStrut(10));
        f.add(link("Already registered? Sign in", () -> { clearMsgs(); formCards.show(forms, "login"); }));
        f.add(Box.createVerticalGlue());
        return f;
    }

    private void msg(JLabel l, String text, boolean ok) {
        l.setForeground(ok ? GREEN : RED);
        l.setText(text);
    }

    private void clearMsgs() { loginMsg.setText(" "); regMsg.setText(" "); }

    private void doLogin() {
        String u = loginUser.getText().trim(), p = new String(loginPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) { msg(loginMsg, "Enter registration no and password.", false); return; }
        if (!auth.login(u, p)) { msg(loginMsg, "Invalid registration no or password.", false); return; }
        if (StudentService.profile(u) == null) StudentService.createProfile(u, u);   // account made in the old console version
        if (remember.isSelected()) prefs.put("user", u); else prefs.remove("user");
        loginPass.setText("");
        clearMsgs();
        openApp(u);
    }

    private void doRegister() {
        String name = regName.getText().trim(), u = regUser.getText().trim();
        String p = new String(regPass.getPassword());
        if (name.isEmpty()) { msg(regMsg, "Full name is required.", false); return; }
        if (!p.equals(new String(regConfirm.getPassword()))) { msg(regMsg, "Passwords do not match.", false); return; }
        String res = auth.register(u, p);
        if (!res.startsWith("Registered")) { msg(regMsg, res, false); return; }
        StudentService.createProfile(u, name);
        regName.setText(""); regUser.setText(""); regPass.setText(""); regConfirm.setText("");
        loginUser.setText(u);
        formCards.show(forms, "login");
        msg(loginMsg, "Account created. Please sign in.", true);
    }

    // =================================================================== APP SHELL
    private JPanel buildApp() {
        JPanel app = new JPanel(new BorderLayout());
        app.add(sidebar(), BorderLayout.WEST);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(12, 30, 12, 30));
        welcome.setFont(Theme.FONT.deriveFont(15f));
        welcome.setForeground(Theme.TEXT);
        Theme.Round av = new Theme.Round(new Color(204, 251, 241), 44);
        av.setLayout(new BorderLayout());
        av.setPreferredSize(new Dimension(44, 44));
        av.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatarText.setFont(Theme.FONT.deriveFont(Font.BOLD, 18f));
        avatarText.setForeground(new Color(20, 184, 166));
        avatarText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        av.add(avatarText);
        MouseAdapter openProfile = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showProfilePopup(av); }
        };
        av.addMouseListener(openProfile);
        avatarText.addMouseListener(openProfile);
        top.add(welcome, BorderLayout.CENTER);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        topRight.setOpaque(false);
        topRight.add(av);
        top.add(topRight, BorderLayout.EAST);

        JPanel sub = new JPanel(new BorderLayout());
        sub.setBackground(Color.WHITE);
        sub.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(235, 237, 243)), new EmptyBorder(14, 30, 14, 30)));
        pageTitle.setFont(Theme.FONT.deriveFont(Font.BOLD, 20f));
        pageTitle.setForeground(Theme.TEXT);
        idInfo.setFont(Theme.FONT.deriveFont(Font.BOLD, 13f));
        idInfo.setForeground(Theme.MUTED);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        left.setOpaque(false);
        left.add(pageTitle);
        left.add(idInfo);
        sub.add(left, BorderLayout.WEST);
        sub.add(Theme.label("Today:  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d yyyy")),
                Font.BOLD, 14, Theme.PRIMARY), BorderLayout.EAST);

        JPanel head = new JPanel(new BorderLayout());
        head.add(top, BorderLayout.NORTH);
        head.add(sub, BorderLayout.CENTER);

        pages.put("Dashboard", new DashboardPanel());
        pages.put("Course", new CoursesPanel());
        pages.put("Exam", new ExamPanel());
        pages.put("Fee", new FeePanel());
        pages.put("General", new GeneralPanel());
        pages.put("Registration", new RegistrationPanel());
        pages.forEach((k, v) -> pageHolder.add(v, k));

        JPanel right = new JPanel(new BorderLayout());
        right.add(head, BorderLayout.NORTH);
        right.add(pageHolder, BorderLayout.CENTER);
        app.add(right, BorderLayout.CENTER);
        return app;
    }

    private JButton navButton(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT.deriveFont(15f));
        b.setForeground(fg);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(0, 28, 0, 10));
        b.setAlignmentX(0f);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel sidebar() {
        JPanel s = new JPanel();
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBackground(Color.WHITE);
        s.setPreferredSize(new Dimension(230, 0));
        JPanel brandRow = new JPanel();
        brandRow.setLayout(new BoxLayout(brandRow, BoxLayout.Y_AXIS));
        brandRow.setOpaque(false);
        brandRow.setBorder(new EmptyBorder(20, 20, 16, 10));
        brandRow.setAlignmentX(0f);
        brandRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        ImageIcon wideLogo = loadWideLogo(34);
        if (wideLogo != null) {
            JLabel wl = new JLabel(wideLogo);
            wl.setAlignmentX(0f);
            brandRow.add(wl);
        } else {
            JLabel fallback = Theme.label("Student Portal", Font.BOLD, 18, Theme.PRIMARY);
            fallback.setAlignmentX(0f);
            brandRow.add(fallback);
        }
        s.add(brandRow);
        for (String n : new String[]{"Dashboard", "Course", "Exam", "Fee", "General", "Registration"}) {
            JButton b = navButton(n, Theme.TEXT);
            b.addActionListener(e -> showPage(n));
            nav.put(n, b);
            s.add(b);
        }
        s.add(Box.createVerticalGlue());
        JButton out = navButton("Logout", RED);
        out.addActionListener(e -> {
            currentUser = null;
            formCards.show(forms, "login");
            rootCards.show(root, "auth");
        });
        s.add(out);
        s.add(Box.createVerticalStrut(20));
        return s;
    }

    private void openApp(String user) {
        currentUser = user;
        String[] p = StudentService.profile(user);
        welcome.setText("<html><span style='color:#78808f'>Welcome,</span> <b>" + p[0] + " | " + user + "</b></html>");
        avatarText.setText(p[0].substring(0, 1).toUpperCase());
        idInfo.setText("My ID: " + user + "     My Enrollment No.: " + p[3] + "     My Section: " + p[2]);
        rootCards.show(root, "app");
        showPage("Dashboard");
    }

    private void showPage(String name) {
        pages.get(name).load(currentUser);
        pageCards.show(pageHolder, name);
        pageTitle.setText(name);
        nav.forEach((k, b) -> {
            boolean on = k.equals(name);
            b.setContentAreaFilled(on);
            b.setBackground(on ? NAV_ON : Color.WHITE);
            b.setForeground(on ? Theme.PRIMARY : Theme.TEXT);
        });
    }

    // =================================================================== PROFILE POPUP
    private void showProfilePopup(Component invoker) {
        String[] p = StudentService.profile(currentUser);
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(new Color(229, 231, 235), 1));

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 22, 14, 22));
        content.setPreferredSize(new Dimension(300, 0));

        JLabel title = Theme.label("User Profile", Font.BOLD, 18, Theme.TEXT);
        title.setAlignmentX(0f);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(0f);
        Theme.Round big = new Theme.Round(new Color(204, 251, 241), 60);
        big.setLayout(new GridBagLayout());
        big.setPreferredSize(new Dimension(60, 60));
        big.setMaximumSize(new Dimension(60, 60));
        big.add(Theme.label(p[0].substring(0, 1).toUpperCase(), Font.BOLD, 22, new Color(20, 184, 166)));
        row.add(big);
        row.add(Box.createHorizontalStrut(14));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = Theme.label(p[0], Font.BOLD, 16, Theme.TEXT);
        JLabel program = Theme.label(p[1], Font.PLAIN, 13, Theme.MUTED);
        JLabel reg = Theme.label("Reg No: " + currentUser, Font.PLAIN, 13, Theme.MUTED);
        name.setAlignmentX(0f);
        program.setAlignmentX(0f);
        reg.setAlignmentX(0f);
        info.add(name);
        info.add(program);
        info.add(reg);
        row.add(info);
        content.add(row);
        content.add(Box.createVerticalStrut(16));

        JButton signOut = new JButton("\u2192  Sign Out");
        signOut.setFont(Theme.FONT.deriveFont(Font.BOLD, 14f));
        signOut.setForeground(Theme.PRIMARY);
        signOut.setBackground(new Color(224, 238, 255));
        signOut.setOpaque(true);
        signOut.setBorderPainted(false);
        signOut.setFocusPainted(false);
        signOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signOut.setAlignmentX(0f);
        signOut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        signOut.addActionListener(e -> {
            popup.setVisible(false);
            currentUser = null;
            formCards.show(forms, "login");
            rootCards.show(root, "auth");
        });
        content.add(signOut);
        content.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(0f);
        content.add(sep);
        content.add(Box.createVerticalStrut(6));

        content.add(profileMenuItem("My Profile", "Account settings and more", "General", popup));
        content.add(profileMenuItem("My Courses", "Enrolled courses this semester", "Course", popup));
        content.add(profileMenuItem("Exam Results", "Grades and CGPA", "Exam", popup));
        content.add(profileMenuItem("Fee Details", "Balance and payment history", "Fee", popup));
        content.add(profileMenuItem("Registration", "Register or drop courses", "Registration", popup));

        popup.add(content);
        popup.show(invoker, invoker.getWidth() - 300, invoker.getHeight() + 10);
    }

    private JPanel profileMenuItem(String titleText, String subText, String page, JPopupMenu popup) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBorder(new EmptyBorder(8, 6, 8, 6));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setOpaque(false);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel t = Theme.label(titleText, Font.BOLD, 14, Theme.TEXT);
        JLabel s = Theme.label(subText, Font.PLAIN, 12, Theme.MUTED);
        t.setAlignmentX(0f);
        s.setAlignmentX(0f);
        text.add(t);
        text.add(s);
        item.add(text, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                popup.setVisible(false);
                showPage(page);
            }
            @Override public void mouseEntered(MouseEvent e) {
                item.setOpaque(true);
                item.setBackground(new Color(248, 249, 252));
                item.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setOpaque(false);
                item.repaint();
            }
        });
        return item;
    }

    public static void main(String[] args) {
        // Sharper, smoother text rendering everywhere in the app (uses the OS's own font smoothing).
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Database.init();
        SwingUtilities.invokeLater(() -> new PortalGUI().setVisible(true));
    }
}