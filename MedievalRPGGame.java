import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import java.util.List;
import java.util.ArrayList;

public class MedievalRPGGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Medieval RPG Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new GamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class GamePanel extends JPanel implements KeyListener {
    private final int tileSize = 48;
    private final int rows = 12, cols = 16;
    private final int[][] map = new int[rows][cols];
    private int playerX = 1, playerY = 1;
    private boolean inCombat = false;
    private int playerHealth = 100;
    private int enemyHealth;
    private List<String> inventory = new ArrayList<>();
    private boolean questAccepted = false;
    private int goblinsDefeated = 0;
    private boolean npcNearby = false;

    public GamePanel() {
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        initMap();
        playMusic();
        loadGame();
    }

    private void initMap() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                map[y][x] = 0;
            }
        }
        map[3][4] = 1; // tree
        map[2][2] = 1;
        map[5][5] = 2; // goblin
        map[8][10] = 2;
        map[1][14] = 3; // village
        map[10][1] = 4; // chest
        map[6][2] = 5; // NPC
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                switch (map[y][x]) {
                    case 0 -> g.setColor(new Color(34, 139, 34));
                    case 1 -> g.setColor(new Color(0, 100, 0));
                    case 2 -> g.setColor(Color.RED);
                    case 3 -> g.setColor(Color.ORANGE);
                    case 4 -> g.setColor(Color.YELLOW);
                    case 5 -> g.setColor(Color.CYAN);
                }
                g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        g.setColor(Color.BLUE);
        g.fillOval(playerX * tileSize + 8, playerY * tileSize + 8, 32, 32);

        g.setColor(Color.WHITE);
        g.drawString("HP: " + playerHealth + " | Inventory: " + inventory, 10, 15);
        if (questAccepted) g.drawString("Quest: Defeat 2 Goblins (" + goblinsDefeated + "/2)", 10, 30);

        if (inCombat) {
            g.setColor(Color.WHITE);
            g.fillRect(50, 300, 500, 80);
            g.setColor(Color.BLACK);
            g.drawString("Fighting Goblin! Press SPACE to attack.", 60, 330);
            g.drawString("Your HP: " + playerHealth + " | Goblin HP: " + enemyHealth, 60, 360);
        }

        if (npcNearby) {
            g.setColor(Color.WHITE);
            g.fillRect(50, 250, 500, 50);
            g.setColor(Color.BLACK);
            g.drawString("Press E to talk to the Villager.", 60, 280);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_S) {
            saveGame();
            JOptionPane.showMessageDialog(this, "Game Saved.");
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_L) {
            loadGame();
            repaint();
            JOptionPane.showMessageDialog(this, "Game Loaded.");
            return;
        }

        if (npcNearby && e.getKeyCode() == KeyEvent.VK_E) {
            if (!questAccepted) {
                JOptionPane.showMessageDialog(this, "Villager: Brave one, defeat 2 goblins to earn a reward!");
                questAccepted = true;
            } else if (goblinsDefeated >= 2) {
                JOptionPane.showMessageDialog(this, "Villager: You did it! Here's your reward: Gold Coin!");
                inventory.add("Gold Coin");
                questAccepted = false;
                goblinsDefeated = 0;
            } else {
                JOptionPane.showMessageDialog(this, "Villager: You're still on your quest. Keep going!");
            }
            repaint();
            return;
        }

        if (inCombat && e.getKeyCode() == KeyEvent.VK_SPACE) {
            int playerHit = (int)(Math.random() * 15 + 5);
            int enemyHit = (int)(Math.random() * 10 + 3);
            enemyHealth -= playerHit;
            playerHealth -= enemyHit;
            if (enemyHealth <= 0) {
                inCombat = false;
                map[playerY][playerX] = 0;
                inventory.add("Goblin Tooth");
                goblinsDefeated++;
            }
            if (playerHealth <= 0) {
                JOptionPane.showMessageDialog(this, "You died! Game Over.");
                System.exit(0);
            }
            repaint();
            return;
        }

        npcNearby = false;
        int newX = playerX, newY = playerY;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> newY--;
            case KeyEvent.VK_DOWN -> newY++;
            case KeyEvent.VK_LEFT -> newX--;
            case KeyEvent.VK_RIGHT -> newX++;
        }

        if (newX >= 0 && newX < cols && newY >= 0 && newY < rows && map[newY][newX] != 1) {
            playerX = newX;
            playerY = newY;
        }

        switch (map[playerY][playerX]) {
            case 2 -> {
                inCombat = true;
                enemyHealth = 40;
            }
            case 3 -> {
                JOptionPane.showMessageDialog(this, "You visit a village and rest. Health restored.");
                playerHealth = 100;
            }
            case 4 -> {
                JOptionPane.showMessageDialog(this, "You found a chest with a Healing Potion!");
                inventory.add("Healing Potion");
                map[playerY][playerX] = 0;
            }
            case 5 -> npcNearby = true;
        }
        repaint();
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    private void playMusic() {
        try (InputStream is = getClass().getResourceAsStream("/music.wav")) {
            if (is == null) throw new IOException("music.wav not found in JAR");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Music error: " + e.getMessage());
        }
    }

    private void saveGame() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            out.writeInt(playerX);
            out.writeInt(playerY);
            out.writeInt(playerHealth);
            out.writeObject(inventory);
            out.writeBoolean(questAccepted);
            out.writeInt(goblinsDefeated);
        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    private void loadGame() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
            playerX = in.readInt();
            playerY = in.readInt();
            playerHealth = in.readInt();
            inventory = (List<String>) in.readObject();
            questAccepted = in.readBoolean();
            goblinsDefeated = in.readInt();
        } catch (Exception e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }
}
