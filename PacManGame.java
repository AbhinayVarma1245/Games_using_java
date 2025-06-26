import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class PacManGame extends JPanel implements KeyListener, ActionListener {

    private final int tileSize = 24;
    private final int rows = 15, cols = 20;
    private final int[][] map = new int[rows][cols]; // 0=empty, 1=wall, 2=dot, 3=power pellet
    private int pacX = 1, pacY = 1;
    private int lives = 3;
    private boolean poweredUp = false;
    private int powerTimer = 0;
    private int levelDelay = 300;

    private int[][] ghosts;
    private Timer gameTimer;

    public PacManGame(String difficulty) {
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        initMap();
        setDifficulty(difficulty);

        gameTimer = new Timer(levelDelay, this);
        gameTimer.start();
    }

    private void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "Easy" -> {
                ghosts = new int[][]{{10, 10}};
                levelDelay = 300;
            }
            case "Medium" -> {
                ghosts = new int[][]{{10, 10}, {1, 13}};
                levelDelay = 200;
            }
            case "Hard" -> {
                ghosts = new int[][]{{10, 10}, {1, 13}, {18, 1}};
                levelDelay = 120;
            }
        }
    }

    private void initMap() {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                map[y][x] = 2;

        for (int x = 0; x < cols; x++) {
            map[0][x] = 1;
            map[rows - 1][x] = 1;
        }
        for (int y = 0; y < rows; y++) {
            map[y][0] = 1;
            map[y][cols - 1] = 1;
        }

        map[5][5] = map[5][6] = map[5][7] = 1;
        map[6][7] = map[7][7] = 1;

        map[10][10] = 3;
        map[1][18] = 3;

        map[pacY][pacX] = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++) {
                switch (map[y][x]) {
                    case 1 -> {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    }
                    case 2 -> {
                        g.setColor(Color.WHITE);
                        g.fillOval(x * tileSize + tileSize / 3, y * tileSize + tileSize / 3, tileSize / 3, tileSize / 3);
                    }
                    case 3 -> {
                        g.setColor(Color.PINK);
                        g.fillOval(x * tileSize + 6, y * tileSize + 6, 12, 12);
                    }
                }
            }

        // Pac-Man
        g.setColor(Color.YELLOW);
        g.fillOval(pacX * tileSize, pacY * tileSize, tileSize, tileSize);

        // Ghosts
        for (int[] ghost : ghosts) {
            g.setColor(poweredUp ? Color.LIGHT_GRAY : Color.RED);
            g.fillOval(ghost[0] * tileSize, ghost[1] * tileSize, tileSize, tileSize);
        }

        // HUD
        g.setColor(Color.WHITE);
        g.drawString("Lives: " + lives, 10, 20);
        if (poweredUp) g.drawString("Power-Up Active!", 10, 40);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int newX = pacX, newY = pacY;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> newX--;
            case KeyEvent.VK_RIGHT -> newX++;
            case KeyEvent.VK_UP -> newY--;
            case KeyEvent.VK_DOWN -> newY++;
        }

        if (isValidMove(newX, newY)) {
            pacX = newX;
            pacY = newY;
            if (map[pacY][pacX] == 2 || map[pacY][pacX] == 3) {
                if (map[pacY][pacX] == 3) {
                    poweredUp = true;
                    powerTimer = 20;
                }
                map[pacY][pacX] = 0;
            }
        }

        repaint();
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows && map[y][x] != 1;
    }

    private void moveGhosts() {
        Random rand = new Random();
        for (int[] ghost : ghosts) {
            int[] dx = {0, 0, -1, 1};
            int[] dy = {-1, 1, 0, 0};
            int dir = rand.nextInt(4);
            int newX = ghost[0] + dx[dir];
            int newY = ghost[1] + dy[dir];

            if (isValidMove(newX, newY)) {
                ghost[0] = newX;
                ghost[1] = newY;
            }

            if (ghost[0] == pacX && ghost[1] == pacY) {
                if (poweredUp) {
                    ghost[0] = 10; ghost[1] = 10;
                } else {
                    lives--;
                    pacX = 1; pacY = 1;
                    if (lives <= 0) {
                        gameTimer.stop();
                        JOptionPane.showMessageDialog(this, "Game Over!");
                        System.exit(0);
                    }
                }
            }
        }
    }

    private boolean checkWin() {
        for (int[] row : map)
            for (int tile : row)
                if (tile == 2 || tile == 3)
                    return false;
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveGhosts();
        if (poweredUp) {
            powerTimer--;
            if (powerTimer <= 0) poweredUp = false;
        }

        if (checkWin()) {
            gameTimer.stop();
            JOptionPane.showMessageDialog(this, "You Win!");
            System.exit(0);
        }

        repaint();
    }

    public static void main(String[] args) {
        String[] options = {"Easy", "Medium", "Hard"};
        String difficulty = (String) JOptionPane.showInputDialog(
                null,
                "Select Difficulty:",
                "Difficulty",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                "Easy"
        );

        if (difficulty == null) System.exit(0); // User cancelled

        JFrame frame = new JFrame("Pac-Man - " + difficulty);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new PacManGame(difficulty));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
