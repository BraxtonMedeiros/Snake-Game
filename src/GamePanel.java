import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.Arrays;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH*SCREEN_HEIGHT)/UNIT_SIZE;
    static final int DELAY = 75;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    int highScore = 0;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    GamePanel (){
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        startGame();
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(Integer.toString(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startGame() {

        loadHighScore();
        newApple();
        running = true;
        timer = new Timer(DELAY,this);
        timer.start();

    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        draw(g);

        // Display the high score
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 20));
        g.drawString("High Score: " + highScore, 10, 20);

    }

    public void draw(Graphics g) {

        if(running) {
            /*
            for(int i=0;i<SCREEN_HEIGHT/UNIT_SIZE;i++) {

                g.setColor(Color.white);
                g.drawLine(i*UNIT_SIZE, 0, i*UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0,i*UNIT_SIZE, SCREEN_WIDTH,i*UNIT_SIZE);

            }
            */

            g.setColor(Color.green);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if(i == 0) {
                    g.setColor(Color.blue);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
                else {
                    g.setColor(new Color(4, 158, 247));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            g.setColor(Color.red);
            g.setFont( new Font("Ink Free",Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: "+applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: "+applesEaten))/2, g.getFont().getSize());


        }
        else {
            gameOver(g);
        }

    }

    public void newApple() {
        boolean validApplePosition = false;

        while (!validApplePosition) {
            appleX = random.nextInt( (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt( (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

            validApplePosition = true;

            // Check if the apple's position overlaps with any part of the snake's body
            for (int i = 0; i < bodyParts; i++) {
                if (appleX == x[i] && appleY == y[i]) {
                    validApplePosition = false;
                    break;
                }
            }
        }
    }

    public void move() {

        for (int i = bodyParts;i>0;i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        switch (direction) {
            case 'U' -> y[0] = y[0] - UNIT_SIZE;
            case 'D' -> y[0] = y[0] + UNIT_SIZE;
            case 'L' -> x[0] = x[0] - UNIT_SIZE;
            case 'R' -> x[0] = x[0] + UNIT_SIZE;
        }

    }

    public void checkApple() {

        if((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;

            if (applesEaten > highScore) {
                highScore = applesEaten;
                saveHighScore();
            }

            newApple();
        }

    }

    public void checkCollisions() {
        //Checks if head collides with body
        for(int i = bodyParts;i>0;i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }
        //Check if head touches left border
        if(x[0] < 0) {
            running = false;
        }
        //Check if head touches right border
        if(x[0] > SCREEN_WIDTH) {
            running = false;
        }
        //Check if head touches top border
        if(y[0] < SCREEN_HEIGHT) {
            running = false;
        }
        //Check if head touches bottom border
        if(y[0] > 600) {
            running = false;
        }

        if(!running) {
            timer.stop();
        }

    }

    public void gameOver(Graphics g) {

        // Display the high score
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 20));
        g.drawString("High Score: " + highScore, 10, 20);

        //Display Score
        g.setColor(Color.red);
        g.setFont( new Font("Ink Free",Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: "+applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: "+applesEaten))/2, g.getFont().getSize());

        //Game over text
        g.setColor(Color.red);
        g.setFont( new Font("Ink Free",Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over"))/2, SCREEN_HEIGHT/2);

        // Ask the player if they want to play again
        if (askToPlayAgain()) {
            // Reset the game
            resetGame();
        } else {
            // Exit the game or perform any other action you want
            System.exit(0);
        }

    }

    public boolean askToPlayAgain() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        return choice == JOptionPane.YES_OPTION;
    }

    public void resetGame() {
        // Reset all game variables to their initial state
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        running = true;
        newApple();
        Arrays.fill(x, 0);
        Arrays.fill(y, 0);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(running) {
            move();
            checkApple();
            checkCollisions();
        }

        if (!running) {
            timer.stop();
            gameOver(getGraphics()); // Display the game over screen
        }
        repaint();

    }

    public class MyKeyAdapter extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') {
                        direction = 'L';
                    }
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') {
                        direction = 'R';
                    }
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') {
                        direction = 'U';
                    }
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') {
                        direction = 'D';
                    }
                }
            }

        }
    }

}
