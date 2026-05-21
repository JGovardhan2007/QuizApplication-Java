import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Question {
    private String questionText;
    private String[] options;
    private int correctAnswerIndex;

    public Question(String questionText, String[] options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionText() { return questionText; }
    public String[] getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
}

class Leaderboard {
    private Map<String, Integer> scores;

    public Leaderboard() {
        scores = new HashMap<>();
        scores.put("Alice", 20);
        scores.put("Bob", 10);
    }

    public void addScore(String playerName, int score) {
        scores.put(playerName, score);
    }

    public String getLeaderboardText() {
        StringBuilder sb = new StringBuilder("--- LEADERBOARD ---\n");
        List<Map.Entry<String, Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int rank = 1;
        for (Map.Entry<String, Integer> entry : list) {
            sb.append(rank++).append(". ").append(entry.getKey())
              .append(" : ").append(entry.getValue()).append(" pts\n");
        }
        return sb.toString();
    }
}

public class QuizApplication extends JFrame {
    private List<Question> questionBank;
    private Leaderboard leaderboard;
    private int currentQuestionIndex = 0;
    private int score = 0;
    
    private Thread timerThread;
    private int timeLeft;
    private boolean isTimerRunning;

    private JLabel lblQuestion = new JLabel("Welcome to the Ultimate Quiz!", SwingConstants.CENTER);
    private JButton[] btnOptions = new JButton[4];
    private JLabel lblTimer = new JLabel("Time: --", SwingConstants.CENTER);
    private JTextArea txtLeaderboard = new JTextArea();
    private JButton btnStart = new JButton("Start Quiz");

    public QuizApplication() {
        setTitle("Quiz Competition Application");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        leaderboard = new Leaderboard();
        questionBank = new ArrayList<>();
        loadQuestions();
        setupUI();
    }

    private void loadQuestions() {
        questionBank.add(new Question("Which programming language is structurally platform-independent?", 
                new String[]{"C", "C++", "Java", "Assembly"}, 2));
        questionBank.add(new Question("Which Java Collection guarantees unique elements only?", 
                new String[]{"ArrayList", "HashSet", "Vector", "LinkedList"}, 1));
        questionBank.add(new Question("Which method is called to explicitly begin thread execution?", 
                new String[]{"run()", "execute()", "start()", "init()"}, 2));
    }

    private void setupUI() {
        JPanel panelTop = new JPanel(new GridLayout(2, 1));
        lblQuestion.setFont(new Font("Arial", Font.BOLD, 16));
        lblTimer.setFont(new Font("Arial", Font.ITALIC, 14));
        lblTimer.setForeground(Color.RED);
        panelTop.add(lblQuestion);
        panelTop.add(lblTimer);
        add(panelTop, BorderLayout.NORTH);

        JPanel panelCenter = new JPanel(new GridLayout(4, 1, 5, 5));
        for (int i = 0; i < 4; i++) {
            btnOptions[i] = new JButton("Option " + (i + 1));
            btnOptions[i].setFont(new Font("Arial", Font.PLAIN, 14));
            btnOptions[i].setEnabled(false);
            final int selectedIdx = i;
            btnOptions[i].addActionListener(e -> checkAnswer(selectedIdx));
            panelCenter.add(btnOptions[i]);
        }
        add(panelCenter, BorderLayout.CENTER);

        JPanel panelRight = new JPanel(new BorderLayout());
        panelRight.setBorder(BorderFactory.createTitledBorder("Live Leaderboard"));
        txtLeaderboard.setEditable(false);
        txtLeaderboard.setFont(new Font("Monospaced", Font.PLAIN, 13));
        updateLeaderboardDisplay();
        panelRight.add(new JScrollPane(txtLeaderboard), BorderLayout.CENTER);
        panelRight.setPreferredSize(new Dimension(200, 0));
        add(panelRight, BorderLayout.EAST);

        btnStart.setFont(new Font("Arial", Font.BOLD, 14));
        btnStart.addActionListener(e -> {
            btnStart.setEnabled(false);
            startQuiz();
        });
        add(btnStart, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < questionBank.size()) {
            Question q = questionBank.get(currentQuestionIndex);
            lblQuestion.setText(q.getQuestionText());
            String[] options = q.getOptions();
            for (int i = 0; i < 4; i++) {
                btnOptions[i].setText(options[i]);
                btnOptions[i].setEnabled(true);
            }
            startTimerThread();
        } else {
            endQuiz();
        }
    }

    private void checkAnswer(int selectedIndex) {
        stopTimerThread();
        Question currentQuestion = questionBank.get(currentQuestionIndex);
        if (selectedIndex == currentQuestion.getCorrectAnswerIndex()) {
            score += 10;
        }
        currentQuestionIndex++;
        showNextQuestion();
    }

    private void startTimerThread() {
        stopTimerThread();
        timeLeft = 10;
        isTimerRunning = true;

        timerThread = new Thread(() -> {
            while (timeLeft >= 0 && isTimerRunning) {
                SwingUtilities.invokeLater(() -> lblTimer.setText("Time Left: " + timeLeft + "s"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                timeLeft--;
            }
            if (isTimerRunning) {
                currentQuestionIndex++;
                showNextQuestion();
            }
        });
        timerThread.start();
    }

    private void stopTimerThread() {
        isTimerRunning = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    private void updateLeaderboardDisplay() {
        txtLeaderboard.setText(leaderboard.getLeaderboardText());
    }

    private void endQuiz() {
        stopTimerThread();
        lblQuestion.setText("Quiz Finished!");
        lblTimer.setText("Game Over");
        for (JButton btn : btnOptions) {
            btn.setEnabled(false);
        }

        String name = JOptionPane.showInputDialog(this, "Quiz Over! Your score: " + score + "\nEnter your Name:");
        if (name != null && !name.trim().isEmpty()) {
            leaderboard.addScore(name.trim(), score);
            updateLeaderboardDisplay();
        }
        btnStart.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApplication::new);
    }
}
