// RagaIdentifierGUI.java - Complete Java Application with Swing GUI
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Swara Notation Convention:
 * - Shuddha (Natural) swaras: UPPERCASE (Sa, Re, Ga, Ma, Pa, Dha, Ni)
 * - Komal (Flat) swaras: lowercase (re, ga, dha, ni)
 * - Teevra (Sharp) swara: Ma' (only Ma can be Teevra)
 * - Sa and Pa are Achal swaras (unchangeable - always capital)
 */

// ================== DATA CLASSES ==================

class Raga {
    private String name;
    private String arohana;
    private String avarohana;
    private String description;
    private String swaraTypes;

    public Raga(String name, String arohana, String avarohana, String description, String swaraTypes) {
        this.name = name;
        this.arohana = arohana;
        this.avarohana = avarohana;
        this.description = description;
        this.swaraTypes = swaraTypes;
    }

    public String getName() { return name; }
    public String getArohana() { return arohana; }
    public String getAvarohana() { return avarohana; }
    public String getDescription() { return description; }
    public String getSwaraTypes() { return swaraTypes; }
}

// ================== ALGORITHM CLASSES ==================

class SwaraAnalyzer {
    public static String analyzeSwaraType(String swara) {
        if (swara == null || swara.isEmpty()) return "UNKNOWN";
        if (swara.contains("'")) return "TEEVRA";
        if (swara.equals("Sa") || swara.equals("Pa")) return "SHUDDHA";

        char firstChar = swara.charAt(0);
        if (Character.isLowerCase(firstChar)) return "KOMAL";
        if (Character.isUpperCase(firstChar)) return "SHUDDHA";

        return "UNKNOWN";
    }

    public static Map<String, Integer> analyzeSwaraSequence(String sequence) {
        Map<String, Integer> swaraCount = new HashMap<>();
        swaraCount.put("SHUDDHA", 0);
        swaraCount.put("KOMAL", 0);
        swaraCount.put("TEEVRA", 0);

        String[] swaras = sequence.trim().split("\\s+");
        for (String swara : swaras) {
            String type = analyzeSwaraType(swara);
            swaraCount.put(type, swaraCount.getOrDefault(type, 0) + 1);
        }
        return swaraCount;
    }
}

class KMPAlgorithm {
    private int[] computeLPSArray(String pattern) {
        int M = pattern.length();
        int[] lps = new int[M];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < M) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    public double calculateEnhancedSimilarity(String input, String ragaPattern) {
        if (input == null || ragaPattern == null) return 0.0;
        input = input.trim();
        ragaPattern = ragaPattern.trim();
        if (input.isEmpty() || ragaPattern.isEmpty()) return 0.0;

        String[] inputSwaras = input.split("\\s+");
        String[] ragaSwaras = ragaPattern.split("\\s+");

        int exactMatches = 0;
        int partialMatches = 0;

        for (int i = 0; i < inputSwaras.length; i++) {
            for (int j = 0; j < ragaSwaras.length; j++) {
                if (inputSwaras[i].equals(ragaSwaras[j])) {
                    exactMatches++;
                    break;
                } else if (inputSwaras[i].equalsIgnoreCase(ragaSwaras[j])) {
                    partialMatches++;
                    break;
                }
            }
        }

        double exactScore = (exactMatches * 100.0) / inputSwaras.length;
        double partialScore = (partialMatches * 50.0) / inputSwaras.length;

        // cap at 100
        double score = Math.min(100.0, exactScore + partialScore);
        return score;
    }
}

/* ---------- NEW ALGORITHM 1: Levenshtein on token sequences ----------
   Simple DP edit distance across tokens (swaras). Returns similarity%
   = (1 - distance / maxLen) * 100
*/
class LevenshteinAlgorithm {
    public int computeEditDistance(String[] a, String[] b) {
        int n = a.length;
        int m = b.length;
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = a[i - 1].equals(b[j - 1]) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1,   // deletion
                                             dp[i][j - 1] + 1),  // insertion
                                             dp[i - 1][j - 1] + cost); // replace
            }
        }
        return dp[n][m];
    }

    public double similarityPercentage(String input, String pattern) {
        if (input == null || pattern == null) return 0.0;
        input = input.trim();
        pattern = pattern.trim();
        if (input.isEmpty() || pattern.isEmpty()) return 0.0;

        String[] a = input.split("\\s+");
        String[] b = pattern.split("\\s+");
        int dist = computeEditDistance(a, b);
        int maxLen = Math.max(a.length, b.length);
        if (maxLen == 0) return 0.0;
        double sim = (1.0 - ((double) dist / maxLen)) * 100.0;
        if (sim < 0) sim = 0;
        return sim;
    }
}

/* ---------- NEW ALGORITHM 2: Jaccard similarity ----------
   Uses sets of swaras (unique tokens). Jaccard = |A ∩ B| / |A ∪ B| * 100
*/
class JaccardAlgorithm {
    public double similarityPercentage(String input, String pattern) {
        if (input == null || pattern == null) return 0.0;
        input = input.trim();
        pattern = pattern.trim();
        if (input.isEmpty() || pattern.isEmpty()) return 0.0;

        Set<String> a = new HashSet<>(Arrays.asList(input.split("\\s+")));
        Set<String> b = new HashSet<>(Arrays.asList(pattern.split("\\s+")));

        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        if (union.isEmpty()) return 0.0;
        double sim = (intersection.size() * 100.0) / union.size();
        return sim;
    }
}

class TrieNode {
    Map<String, TrieNode> children;
    String ragaName;
    boolean isEndOfSequence;

    public TrieNode() {
        children = new HashMap<>();
        ragaName = null;
        isEndOfSequence = false;
    }
}

class RagaTrie {
    private TrieNode root;

    public RagaTrie() {
        root = new TrieNode();
    }

    public void insert(String sequence, String ragaName) {
        TrieNode current = root;
        String[] notes = sequence.trim().split("\\s+");

        for (String note : notes) {
            current.children.putIfAbsent(note, new TrieNode());
            current = current.children.get(note);
        }

        current.isEndOfSequence = true;
        current.ragaName = ragaName;
    }

    public List<String> search(String inputSequence) {
        List<String> matches = new ArrayList<>();
        String[] notes = inputSequence.trim().split("\\s+");

        TrieNode current = root;
        for (String note : notes) {
            if (!current.children.containsKey(note)) {
                break;
            }
            current = current.children.get(note);
            if (current.isEndOfSequence) {
                matches.add(current.ragaName);
            }
        }
        return matches;
    }
}

// ================== CORE ENGINE ==================

class RagaIdentifierEngine {
    private List<Raga> ragaDatabase;
    private KMPAlgorithm kmpAlgorithm;
    private RagaTrie ragaTrie;
    private LevenshteinAlgorithm levenshteinAlgorithm;
    private JaccardAlgorithm jaccardAlgorithm;

    public RagaIdentifierEngine() {
        ragaDatabase = new ArrayList<>();
        kmpAlgorithm = new KMPAlgorithm();
        ragaTrie = new RagaTrie();
        levenshteinAlgorithm = new LevenshteinAlgorithm();
        jaccardAlgorithm = new JaccardAlgorithm();
        initializeRagaDatabase();
    }

    private void initializeRagaDatabase() {
        ragaDatabase.add(new Raga("Yaman", "Sa Re Ga Ma' Pa Dha Ni Sa",
                                   "Sa Ni Dha Pa Ma' Ga Re Sa",
                                   "Evening raga, uses all seven notes with Teevra Ma",
                                   "All Shuddha except Teevra Ma"));
        ragaDatabase.add(new Raga("Bhairav", "Sa re Ga Ma Pa dha Ni Sa",
                                   "Sa Ni dha Pa Ma Ga re Sa",
                                   "Morning raga, King of morning ragas",
                                   "Komal Re and Komal Dha"));
        ragaDatabase.add(new Raga("Bhairavi", "Sa re ga Ma Pa dha ni Sa",
                                   "Sa ni dha Pa Ma ga re Sa",
                                   "Late morning raga, uses all five Komal swaras",
                                   "Komal Re, Ga, Dha, Ni"));
        ragaDatabase.add(new Raga("Bilawal", "Sa Re Ga Ma Pa Dha Ni Sa",
                                   "Sa Ni Dha Pa Ma Ga Re Sa",
                                   "Late night raga, all Shuddha swaras",
                                   "All Shuddha (Natural major scale)"));
        ragaDatabase.add(new Raga("Kafi", "Sa Re ga Ma Pa Dha ni Sa",
                                   "Sa ni Dha Pa Ma ga Re Sa",
                                   "Night raga, with Komal Ga and Komal Ni",
                                   "Komal Ga and Komal Ni"));
        ragaDatabase.add(new Raga("Kalyan", "Sa Re Ga Ma' Pa Dha Ni Sa",
                                   "Sa Ni Dha Pa Ma' Ga Re Sa",
                                   "Evening raga, same as Yaman",
                                   "All Shuddha with Teevra Ma"));
        ragaDatabase.add(new Raga("Khamaj", "Sa Re Ga Ma Pa Dha ni Sa",
                                   "Sa ni Dha Pa Ma Ga Re Sa",
                                   "Late night raga, with Komal Ni",
                                   "Komal Ni"));
        ragaDatabase.add(new Raga("Marwa", "Ni re Ga Ma' Dha Ni Sa",
                                   "Sa Ni Dha Ma' Ga re Sa",
                                   "Sunset raga, omits Pa, uses Teevra Ma and Komal Re",
                                   "Komal Re and Teevra Ma"));
        ragaDatabase.add(new Raga("Purvi", "Sa re Ga Ma' Dha Ni Sa",
                                   "Sa Ni Dha Pa Ma' Ga re Sa",
                                   "Evening raga, with Komal Re and Teevra Ma",
                                   "Komal Re and Teevra Ma"));
        ragaDatabase.add(new Raga("Asavari", "Sa Re ga Ma Pa dha ni Sa",
                                   "Sa ni dha Pa Ma ga Re Sa",
                                   "Morning to afternoon raga",
                                   "Komal Ga, Dha, Ni"));
        ragaDatabase.add(new Raga("Bhupali", "Sa Re Ga Pa Dha Sa",
                                   "Sa Dha Pa Ga Re Sa",
                                   "Evening raga, pentatonic scale with all Shuddha",
                                   "All Shuddha (omits Ma and Ni)"));
        ragaDatabase.add(new Raga("Desh", "Sa Re Ma Pa Ni Sa",
                                   "Sa ni Dha Pa Ma Ga Re Sa",
                                   "Late night raga, patriotic raga",
                                   "Komal Ni in Avarohana"));
        ragaDatabase.add(new Raga("Todi", "Sa re ga Ma' Dha Ni Sa",
                                   "Sa Ni Dha Ma' ga re Sa",
                                   "Late morning raga, highly serious and devotional",
                                   "Komal Re, Komal Ga, Teevra Ma"));
        ragaDatabase.add(new Raga("Malkauns", "Sa ga Ma dha ni Sa",
                                   "Sa ni dha Ma ga Sa",
                                   "Midnight raga, deeply meditative, pentatonic",
                                   "Komal Ga, Komal Dha, Komal Ni (omits Re and Pa)"));
        ragaDatabase.add(new Raga("Durga", "Sa Re Ma Pa Dha Sa",
                                   "Sa Dha Pa Ma Re Sa",
                                   "Evening raga, peaceful and devotional, pentatonic",
                                   "Omits Ga and Ni (all Shuddha swaras)"));

        ragaDatabase.add(new Raga("Hamsadhwani", "Sa Re Ga Pa Ni Sa",
                                   "Sa Ni Pa Ga Re Sa",
                                   "Auspicious raga, often used in invocation",
                                   "Omits Ma and Dha, all Shuddha"));

        ragaDatabase.add(new Raga("Miyan Malhar", "Sa Re Ma Pa Ni Sa",
                                   "Sa Ni Pa Ma Re Sa",
                                   "Rain raga, evokes the beauty of monsoon",
                                   "All Shuddha with strong Ma–Pa usage"));

        ragaDatabase.add(new Raga("Jog", "Sa ga Ma Pa ni Sa",
                                   "Sa ni Pa Ma ga Re Sa",
                                   "Night raga, romantic and expressive",
                                   "Mix of Shuddha and Komal notes (Komal Ga, Ni)"));

        ragaDatabase.add(new Raga("Charukeshi", "Sa Re Ga Ma Pa dha Ni Sa",
                                   "Sa Ni dha Pa Ma Ga Re Sa",
                                   "Evening raga, emotional and devotional",
                                   "Komal Dha, rest Shuddha"));

        ragaDatabase.add(new Raga("Hansadhwani", "Sa Re Ga Pa Ni Sa",
                                   "Sa Ni Pa Ga Re Sa",
                                   "Morning raga, used for Mangala (auspicious) performances",
                                   "Omission of Ma and Dha"));

        ragaDatabase.add(new Raga("Kedar", "Sa Ma Pa, Ma Pa Dha Pa Ma Ga Re Sa",
                                   "Sa Ni Dha Pa Ma Pa Ga Ma Re Sa",
                                   "Night raga, devotional and calm",
                                   "Frequent Ma usage, all Shuddha"));

        ragaDatabase.add(new Raga("Shree", "Sa re Ga Ma' Pa dha Ni Sa",
                                   "Sa Ni dha Pa Ma' Ga re Sa",
                                   "Sunset raga, serious and peaceful",
                                   "Komal Re, Komal Dha, Teevra Ma"));

        ragaDatabase.add(new Raga("Bageshree", "Sa Ga Ma Dha Ni Sa",
                                   "Sa Ni Dha Ma Ga Re Sa",
                                   "Late night raga, evokes longing and devotion",
                                   "Komal Ga, Komal Ni"));

        ragaDatabase.add(new Raga("Madhuwanti", "Sa Re Ga Ma' Pa Dha Ni Sa",
                                   "Sa Ni Dha Pa Ma' Ga Re Sa",
                                   "Afternoon raga, romantic and light",
                                   "Teevra Ma"));

        for (Raga raga : ragaDatabase) {
            if (raga.getArohana() != null && !raga.getArohana().trim().isEmpty())
                ragaTrie.insert(raga.getArohana(), raga.getName() + " (Arohana)");
            if (raga.getAvarohana() != null && !raga.getAvarohana().trim().isEmpty())
                ragaTrie.insert(raga.getAvarohana(), raga.getName() + " (Avarohana)");
        }
    }

    /**
     * New combined identification:
     * For each raga we compute:
     *  - kmpScore (0-100)
     *  - levenshteinScore (0-100)
     *  - jaccardScore (0-100)
     * Then combined = 0.5*kmp + 0.3*lev + 0.2*jaccard
     *
     * We return a map: ragaName -> Double[]{combined, kmp, lev, jaccard}
     */
    public Map<String, Double[]> identifyRagaCombined(String inputSequence) {
        Map<String, Double[]> results = new HashMap<>();
        if (inputSequence == null || inputSequence.trim().isEmpty()) return results;

        for (Raga raga : ragaDatabase) {
            double aroKMP = kmpAlgorithm.calculateEnhancedSimilarity(inputSequence, raga.getArohana());
            double avaKMP = kmpAlgorithm.calculateEnhancedSimilarity(inputSequence, raga.getAvarohana());
            double kmpScore = Math.max(aroKMP, avaKMP);

            double aroLev = levenshteinAlgorithm.similarityPercentage(inputSequence, raga.getArohana());
            double avaLev = levenshteinAlgorithm.similarityPercentage(inputSequence, raga.getAvarohana());
            double levScore = Math.max(aroLev, avaLev);

            double aroJac = jaccardAlgorithm.similarityPercentage(inputSequence, raga.getArohana());
            double avaJac = jaccardAlgorithm.similarityPercentage(inputSequence, raga.getAvarohana());
            double jacScore = Math.max(aroJac, avaJac);

            // weights: KMP 0.5, Levenshtein 0.3, Jaccard 0.2
            double combined = (0.5 * kmpScore) + (0.3 * levScore) + (0.2 * jacScore);

            if (combined > 0) {
                results.put(raga.getName(), new Double[] { combined, kmpScore, levScore, jacScore });
            }
        }
        return results;
    }

    /**
     * Return top N matches as a list of Map.Entry<String, Double[]>
     */
    public List<Map.Entry<String, Double[]>> getTopMatches(String inputSequence, int topN) {
        Map<String, Double[]> results = identifyRagaCombined(inputSequence);
        List<Map.Entry<String, Double[]>> sortedResults = new ArrayList<>(results.entrySet());
        sortedResults.sort((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]));
        return sortedResults.subList(0, Math.min(topN, sortedResults.size()));
    }

    public Raga getRagaDetails(String ragaName) {
        for (Raga raga : ragaDatabase) {
            if (raga.getName().equalsIgnoreCase(ragaName)) {
                return raga;
            }
        }
        return null;
    }
}

// ================== GUI COMPONENTS ==================

class RagaIdentifierGUI extends JFrame {
    private RagaIdentifierEngine engine;
    private JTextField inputField;
    private JPanel resultsPanel;
    private JScrollPane resultsScrollPane;

    private final Color PRIMARY_COLOR = new Color(102, 126, 234);
    private final Color SECONDARY_COLOR = new Color(118, 75, 162);
    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = new Color(51, 51, 51);
    private final Color ACCENT_COLOR = new Color(76, 175, 80);

    public RagaIdentifierGUI() {
        engine = new RagaIdentifierEngine();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🎵 Raga Identifier - Hindustani Classical Music");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createInputPanel(), BorderLayout.CENTER);
        mainPanel.add(createInfoPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("🎵 Raga Identifier");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Hindustani Classical Music Pattern Recognition");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    private JPanel createInputPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout(10, 10));
        containerPanel.setBackground(BACKGROUND_COLOR);

        JPanel inputCard = new JPanel();
        inputCard.setLayout(new BorderLayout(10, 10));
        inputCard.setBackground(CARD_COLOR);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel inputLabel = new JLabel("Enter Swara Sequence:");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        inputLabel.setForeground(TEXT_COLOR);

        inputField = new JTextField();
        inputField.setFont(new Font("Consolas", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton identifyButton = createStyledButton("Identify Raga", PRIMARY_COLOR);
        identifyButton.addActionListener(e -> identifyRaga());

        JButton clearButton = createStyledButton("Clear", new Color(150, 150, 150));
        clearButton.addActionListener(e -> clearResults());

        buttonPanel.add(identifyButton);
        buttonPanel.add(clearButton);

        JPanel examplesPanel = createExamplesPanel();

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(CARD_COLOR);
        topPanel.add(inputLabel, BorderLayout.NORTH);
        topPanel.add(inputField, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        inputCard.add(topPanel, BorderLayout.NORTH);
        inputCard.add(examplesPanel, BorderLayout.CENTER);

        containerPanel.add(inputCard, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BACKGROUND_COLOR);

        resultsScrollPane = new JScrollPane(resultsPanel);
        resultsScrollPane.setBorder(null);
        resultsScrollPane.setBackground(BACKGROUND_COLOR);
        resultsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        containerPanel.add(resultsScrollPane, BorderLayout.CENTER);

        return containerPanel;
    }

    private JPanel createExamplesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel label = new JLabel("Quick Examples:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label);

        String[][] examples = {
            {"Yaman", "Sa Re Ga Ma' Pa Dha Ni Sa"},
            {"Bhairav", "Sa re Ga Ma Pa dha Ni Sa"},
            {"Bhairavi", "Sa re ga Ma Pa dha ni Sa"},
            {"Todi", "Sa re ga Ma' Dha Ni Sa"},
            {"Malkauns", "Sa ga Ma dha ni Sa"}
        };

        for (String[] example : examples) {
            JButton btn = new JButton(example[0]);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setBackground(new Color(240, 240, 240));
            btn.setBorder(new EmptyBorder(5, 12, 5, 12));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                inputField.setText(example[1]);
                identifyRaga();
            });
            panel.add(btn);
        }

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        panel.add(createInfoCard("📝 Shuddha", "UPPERCASE\n(Sa, Re, Ga, Ma, Pa, Dha, Ni)", PRIMARY_COLOR));
        panel.add(createInfoCard("♭ Komal", "lowercase\n(re, ga, dha, ni)", SECONDARY_COLOR));
        panel.add(createInfoCard("♯ Teevra", "Apostrophe\n(Ma')", ACCENT_COLOR));

        return panel;
    }

    private JPanel createInfoCard(String title, String content, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel contentLabel = new JLabel("<html><center>" + content.replace("\n", "<br>") + "</center></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        contentLabel.setForeground(TEXT_COLOR);
        contentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(contentLabel);

        return card;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void identifyRaga() {
        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a swara sequence!",
                "Input Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        resultsPanel.removeAll();

        List<Map.Entry<String, Double[]>> matches = engine.getTopMatches(input, 5);

        if (matches.isEmpty()) {
            JLabel noResults = new JLabel("No matching ragas found. Try a different sequence.");
            noResults.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noResults.setForeground(new Color(150, 150, 150));
            noResults.setBorder(new EmptyBorder(20, 20, 20, 20));
            resultsPanel.add(noResults);
        } else {
            for (Map.Entry<String, Double[]> match : matches) {
                String ragaName = match.getKey();
                Double[] scores = match.getValue(); // [combined, kmp, lev, jac]
                Raga raga = engine.getRagaDetails(ragaName);
                resultsPanel.add(createResultCard(raga, scores));
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createResultCard(Raga raga, Double[] scores) {
        double combined = scores[0];
        double kmp = scores[1];
        double lev = scores[2];
        double jac = scores[3];

        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 10));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 3, true),
                new LineBorder(CARD_COLOR, 2)
            ),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JLabel nameLabel = new JLabel(raga.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(PRIMARY_COLOR);

        JLabel similarityLabel = new JLabel(String.format("%.1f%% Match", combined));
        similarityLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        similarityLabel.setForeground(Color.WHITE);
        similarityLabel.setOpaque(true);
        similarityLabel.setBackground(ACCENT_COLOR);
        similarityLabel.setBorder(new EmptyBorder(5, 15, 5, 15));

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(similarityLabel, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_COLOR);

        JLabel aroLabel = new JLabel("Arohana: " + raga.getArohana());
        aroLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        aroLabel.setForeground(new Color(0, 100, 200));

        JLabel avaLabel = new JLabel("Avarohana: " + raga.getAvarohana());
        avaLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        avaLabel.setForeground(new Color(200, 0, 100));

        JLabel swaraLabel = new JLabel("Swara Types: " + raga.getSwaraTypes());
        swaraLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        swaraLabel.setForeground(new Color(100, 100, 100));

        JLabel descLabel = new JLabel(raga.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_COLOR);

        // New: algorithm breakdown
        String breakdownText = String.format("<html><small>KMP: %.1f%% &nbsp; | &nbsp; EditDist: %.1f%% &nbsp; | &nbsp; Jaccard: %.1f%%</small></html>",
                                            kmp, lev, jac);
        JLabel breakdownLabel = new JLabel(breakdownText);
        breakdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        breakdownLabel.setForeground(new Color(80, 80, 80));

        contentPanel.add(aroLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(avaLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(swaraLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(breakdownLabel);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private void clearResults() {
        inputField.setText("");
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new RagaIdentifierGUI());
    }
}
