import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class FlappyBirdGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Flappy Bird");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);

            GamePanel panel = new GamePanel();
            frame.add(panel);

            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int GROUND_HEIGHT = 100;
    private final float GRAVITY = 0.5f;
    private final float AIR_RESISTANCE = 0.98f;
    private final int JUMP_STRENGTH = -10;
    public static final int PIPE_WIDTH = 80;
    private final int PIPE_GAP = 200;
    private final int PIPE_SPEED = 5;
    private final int PIPE_SPAWN_INTERVAL = 1500;
    private final double COIN_SPAWN_CHANCE = 0.3; 

    private Bird bird;
    private CopyOnWriteArrayList<Pipe> pipes;
    private CopyOnWriteArrayList<Coin> coins;
    private Timer gameTimer;
    private Timer pipeTimer;
    private int score;
    private int coinsCollected;
    private boolean gameOver;
    private boolean gameStarted;
    private CopyOnWriteArrayList<Cloud> clouds;
    private int highScore = 0;
    private int flapCycle = 0;
    private CopyOnWriteArrayList<Particle> particles;
    private int cloudCounter = 0;
    private boolean showWelcomeScreen = true;
    private RoundRectangle2D playButton;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        resetGame();
        createPlayButton();
    }

    private void createPlayButton() {
        int buttonWidth = 200;
        int buttonHeight = 60;
        int buttonX = (WIDTH - buttonWidth) / 2;
        int buttonY = HEIGHT - 200;
        playButton = new RoundRectangle2D.Double(buttonX, buttonY, buttonWidth, buttonHeight, 30, 30);
    }

    private void resetGame() {
        bird = new Bird(WIDTH / 4, HEIGHT / 2);
        pipes = new CopyOnWriteArrayList<>();
        coins = new CopyOnWriteArrayList<>();
        clouds = new CopyOnWriteArrayList<>();
        particles = new CopyOnWriteArrayList<>();
        score = 0;
        coinsCollected = 0;
        gameOver = false;
        gameStarted = false;
        cloudCounter = 0;

        for (int i = 0; i < 5; i++) {
            addCloud();
        }
        if (gameTimer != null) gameTimer.stop();
        if (pipeTimer != null) pipeTimer.stop();
    }
    private void addCloud() {
        clouds.add(new Cloud(
                (int)(Math.random() * WIDTH),
                (int)(Math.random() * (HEIGHT - GROUND_HEIGHT - 100)),
                cloudCounter++
        ));
    }
    private void startGame() {
        gameStarted = true;
        showWelcomeScreen = false;

        gameTimer = new Timer(16, this);
        gameTimer.start();

        pipeTimer = new Timer(PIPE_SPAWN_INTERVAL, e -> spawnPipe());
        pipeTimer.start();
    }

    private void spawnPipe() {
        if (!gameOver) {
            int gapY = (int) (Math.random() * (HEIGHT - GROUND_HEIGHT - PIPE_GAP - 100)) + 80;
            Pipe newPipe = new Pipe(WIDTH, gapY);
            pipes.add(newPipe);
            if (Math.random() < COIN_SPAWN_CHANCE) {
                int coinX = WIDTH + PIPE_WIDTH/2;
                int coinY = gapY + PIPE_GAP/2;
                coins.add(new Coin(coinX, coinY));
            }
        }
    }

    private void createParticles(int x, int y, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            bird.update(GRAVITY, AIR_RESISTANCE);
            flapCycle = (flapCycle + 1) % 10;
            Iterator<Particle> particleIter = particles.iterator();
            while (particleIter.hasNext()) {
                Particle p = particleIter.next();
                p.update();
                if (p.isDead()) {
                    particles.remove(p);
                }
            }
            Iterator<Cloud> cloudIter = clouds.iterator();
            while (cloudIter.hasNext()) {
                Cloud cloud = cloudIter.next();
                cloud.update();
                if (cloud.isOffScreen()) {
                    clouds.remove(cloud);
                    addCloud();
                }
            }
            Iterator<Pipe> pipeIter = pipes.iterator();
            while (pipeIter.hasNext()) {
                Pipe pipe = pipeIter.next();
                pipe.update(PIPE_SPEED);

                if (pipe.getX() + PIPE_WIDTH < 0) {
                    pipes.remove(pipe);
                }

                if (!pipe.isPassed() && pipe.getX() + PIPE_WIDTH < bird.getX()) {
                    score++;
                    if (score > highScore) highScore = score;
                    pipe.setPassed(true);
                    createParticles(pipe.getX() + PIPE_WIDTH, pipe.getGapY() + PIPE_GAP / 2, 15);
                }

                if (pipe.collidesWith(bird)) {
                    gameOver = true;
                    createParticles((int)bird.getX(), (int)bird.getY(), 30);
                }
            }
            Iterator<Coin> coinIter = coins.iterator();
            while (coinIter.hasNext()) {
                Coin coin = coinIter.next();
                coin.update(PIPE_SPEED);

               
                if (coin.getX() + Coin.SIZE < 0) {
                    coins.remove(coin);
                }

              
                if (coin.collidesWith(bird)) {
                    coinsCollected++;
                    coins.remove(coin);
                    createParticles((int)coin.getX(), (int)coin.getY(), 20);
                }
            }

            if (bird.getY() <= 0 || bird.getY() + Bird.SIZE >= HEIGHT - GROUND_HEIGHT) {
                gameOver = true;
                createParticles((int)bird.getX(), (int)bird.getY(), 30);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (showWelcomeScreen) {
            drawWelcomeScreen(g2d);
            return;
        }
        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(135, 206, 250),
                0, HEIGHT, new Color(100, 149, 237));
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

      
        g2d.setColor(new Color(255, 255, 200, 150));
        g2d.fillOval(WIDTH - 120, 40, 80, 80);
        for (Cloud cloud : clouds) {
            cloud.draw(g2d);
        }
        for (Pipe pipe : pipes) {
            pipe.draw(g2d);
        }
        for (Coin coin : coins) {
            coin.draw(g2d);
        }
        GradientPaint groundGradient = new GradientPaint(0, HEIGHT - GROUND_HEIGHT,
                new Color(0, 155, 0), 0, HEIGHT, new Color(0, 100, 0));
        g2d.setPaint(groundGradient);
        g2d.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);

        g2d.setColor(new Color(139, 69, 19)); 
        g2d.fillRect(0, HEIGHT - 20, WIDTH, 20);

        g2d.setColor(new Color(100, 80, 0));
        for (int i = 0; i < WIDTH; i += 30) {
            g2d.fillRect(i, HEIGHT - 20, 15, 5);
        }
        for (Particle p : particles) {
            p.draw(g2d);
        }
        bird.draw(g2d, flapCycle);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(10, 10, 200, 85, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("Score: " + score, 20, 40);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("High: " + highScore, 20, 70);


        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(20, 80, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(20, 80, 15, 15);
        g2d.setColor(Color.WHITE);
        g2d.drawString(": " + coinsCollected, 40, 95);

        if (!gameStarted && !showWelcomeScreen) {
            drawCenteredMessage(g2d, "FLAPPY BIRD", 48, Color.YELLOW, -100);
            drawCenteredMessage(g2d, "Press SPACE to Start", 24, Color.WHITE, 0);
            drawCenteredMessage(g2d, "Press SPACE to Jump", 24, Color.WHITE, 40);
        }
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            drawCenteredMessage(g2d, "GAME OVER", 48, Color.RED, -50);
            drawCenteredMessage(g2d, "Score: " + score, 36, Color.WHITE, 20);
            drawCenteredMessage(g2d, "Coins: " + coinsCollected, 30, new Color(255, 215, 0), 60);
            drawCenteredMessage(g2d, "High Score: " + highScore, 30, Color.CYAN, 100);
            drawCenteredMessage(g2d, "Press SPACE to restart", 24, Color.WHITE, 150);
        }
    }

    private void drawWelcomeScreen(Graphics2D g2d) {

        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(100, 149, 237),
                0, HEIGHT, new Color(70, 130, 180));
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);


        for (Cloud cloud : clouds) {
            cloud.draw(g2d);
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 64));
        String title = "FLAPPY BIRD";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(title, (WIDTH - titleWidth)/2 + 4, HEIGHT/4 + 4);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(title, (WIDTH - titleWidth)/2, HEIGHT/4);
        int boxWidth = 400;
        int boxHeight = 120;
        int boxX = (WIDTH - boxWidth)/2;
        int boxY = HEIGHT/3 + 50;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(boxX + 4, boxY + 4, boxWidth, boxHeight, 20, 20);
        GradientPaint boxGradient = new GradientPaint(
                boxX, boxY, new Color(50, 100, 150, 200),
                boxX, boxY + boxHeight, new Color(30, 70, 120, 200));
        g2d.setPaint(boxGradient);
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        g2d.setColor(new Color(200, 220, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.setColor(Color.WHITE);
        String creatorsTitle = "CREATED BY";
        int creatorsTitleWidth = g2d.getFontMetrics().stringWidth(creatorsTitle);
        g2d.drawString(creatorsTitle, (WIDTH - creatorsTitleWidth)/2, boxY + 40);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(255, 215, 0));
        String names = "AYUSH, SHUBHAM, VAISHALI";
        int namesWidth = g2d.getFontMetrics().stringWidth(names);
        g2d.drawString(names, (WIDTH - namesWidth)/2, boxY + 85);
        int buttonWidth = 200;
        int buttonHeight = 60;
        int buttonX = (WIDTH - buttonWidth) / 2;
        int buttonY = HEIGHT - 200;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(buttonX + 4, buttonY + 4, buttonWidth, buttonHeight, 30, 30);
        GradientPaint buttonGradient = new GradientPaint(
                buttonX, buttonY, new Color(50, 200, 50),
                buttonX, buttonY + buttonHeight, new Color(0, 150, 0));
        g2d.setPaint(buttonGradient);
        g2d.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 30, 30);

        g2d.setColor(new Color(200, 255, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 30, 30);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.WHITE);
        String playText = "PLAY";
        int playWidth = g2d.getFontMetrics().stringWidth(playText);
        g2d.drawString(playText, buttonX + (buttonWidth - playWidth)/2, buttonY + buttonHeight/2 + 12);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);
        String instruction = "Click PLAY or press SPACE to begin";
        int instrWidth = g2d.getFontMetrics().stringWidth(instruction);
        g2d.drawString(instruction, (WIDTH - instrWidth)/2, buttonY + buttonHeight + 30);
    }
    private void drawCenteredMessage(Graphics2D g2d, String text, int fontSize, Color color, int yOffset) {
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        int y = HEIGHT / 2 + yOffset;
        g2d.drawString(text, x, y);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (showWelcomeScreen) {
                startGame();
            } else if (!gameStarted) {
                startGame();
            } else if (gameOver) {
                resetGame();
            } else {
                bird.jump(JUMP_STRENGTH);
                createParticles((int)bird.getX(), (int)(bird.getY() + Bird.SIZE/2), 10);
            }
        }
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {
        if (showWelcomeScreen && playButton.contains(e.getPoint())) {
            startGame();
        }
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
class Bird {
    public static final int SIZE = 34;
    private float x, y;
    private float velocity;
    private float rotation;
    private int wingState;
    private long lastFlapTime;
    private boolean isFlapping;

    public Bird(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocity = 0;
        this.rotation = 0;
        this.wingState = 0;
        this.lastFlapTime = 0;
        this.isFlapping = false;
    }
    public void update(float gravity, float resistance) {
        velocity += gravity;
        velocity *= resistance;
        y += velocity;
        if (isFlapping && System.currentTimeMillis() - lastFlapTime < 150) {
            wingState = (wingState + 1) % 4;
        } else {
            wingState = 0;
            isFlapping = false;
        }
        rotation = velocity * 1.2f;
        if (rotation < -25) rotation = -25;
        if (rotation > 90) rotation = 90;
    }
    public void jump(int strength) {
        velocity = strength;
        isFlapping = true;
        lastFlapTime = System.currentTimeMillis();
        wingState = 1;
    }
    public void draw(Graphics2D g2d, int flapCycle) {
        AffineTransform originalTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(rotation), x + SIZE/2, y + SIZE/2);
        GradientPaint bodyGradient = new GradientPaint(
                x, y, new Color(255, 220, 0),
                x, y + SIZE, new Color(255, 150, 0)
        );
        g2d.setPaint(bodyGradient);
        g2d.fillOval((int)x, (int)y, SIZE, SIZE);
        drawWing(g2d);
        g2d.setColor(new Color(255, 80, 0));
        Polygon beak = new Polygon();
        beak.addPoint((int)x + SIZE, (int)(y + SIZE/2 - 6));
        beak.addPoint((int)x + SIZE, (int)(y + SIZE/2 + 6));
        beak.addPoint((int)x + SIZE + 15, (int)(y + SIZE/2));
        g2d.fillPolygon(beak);
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)x + 20, (int)y + 8, 10, 10);
        g2d.setColor(Color.BLACK);
        int eyeX = velocity > 0 ? 24 : 22;
        g2d.fillOval((int)x + eyeX, (int)y + 10, 4, 4);
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)x + eyeX, (int)y + 9, 2, 2);
        g2d.setColor(new Color(255, 180, 0));
        int[] crestX = {(int)x + SIZE/2, (int)x + SIZE/2 + 5, (int)x + SIZE/2 + 10};
        int[] crestY = {(int)y - 3, (int)y - 8, (int)y - 3};
        g2d.fillPolygon(crestX, crestY, 3);
        g2d.setTransform(originalTransform);
    }
    private void drawWing(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 220));

        switch(wingState) {
            case 0:
                g2d.fillOval((int)x + 4, (int)y + 10, 20, 12);
                break;
            case 1:
                g2d.fillOval((int)x + 4, (int)y + 5, 22, 14);
                break;
            case 2:
                g2d.fillOval((int)x + 2, (int)y, 24, 16);
                break;
            case 3:
                g2d.fillOval((int)x + 4, (int)y + 5, 22, 14);
                break;
        }
    }
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, SIZE, SIZE);
    }
    public float getX() { return x; }
    public float getY() { return y; }
}

class Pipe {
    private int x, gapY, gapHeight = 200;
    private boolean passed;
    private Color pipeColor;

    public Pipe(int x, int gapY) {
        this.x = x;
        this.gapY = gapY;
        this.pipeColor = new Color(0, 180, 0);
    }
    public void update(int speed) {
        x -= speed;
    }
    public boolean collidesWith(Bird bird) {
        Rectangle birdBounds = bird.getBounds();
        Rectangle top = new Rectangle(x, 0, GamePanel.PIPE_WIDTH, gapY);
        Rectangle bottom = new Rectangle(x, gapY + gapHeight,
                GamePanel.PIPE_WIDTH, GamePanel.HEIGHT - gapY - gapHeight);
        return birdBounds.intersects(top) || birdBounds.intersects(bottom);
    }
    public void draw(Graphics2D g2d) {
        GradientPaint pipeGradient = new GradientPaint(
                x, 0, pipeColor.darker(),
                x + GamePanel.PIPE_WIDTH, 0, pipeColor.brighter()
        );
        g2d.setPaint(pipeGradient);

        g2d.fillRect(x, 0, GamePanel.PIPE_WIDTH, gapY);
        g2d.fillRect(x, gapY + gapHeight, GamePanel.PIPE_WIDTH,
                GamePanel.HEIGHT - gapY - gapHeight);

        g2d.setColor(pipeColor.darker().darker());
        g2d.fillRect(x - 5, gapY - 20, GamePanel.PIPE_WIDTH + 10, 20);
        g2d.fillRect(x - 5, gapY + gapHeight, GamePanel.PIPE_WIDTH + 10, 20);

        g2d.setColor(pipeColor.brighter());
        g2d.fillRect(x - 3, gapY - 18, GamePanel.PIPE_WIDTH + 6, 16);
        g2d.fillRect(x - 3, gapY + gapHeight + 2, GamePanel.PIPE_WIDTH + 6, 16);

        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.fillRect(x + 5, 5, 10, gapY - 10);
        g2d.fillRect(x + 5, gapY + gapHeight + 5, 10,
                GamePanel.HEIGHT - gapY - gapHeight - 10);
    }
    public int getX() { return x; }
    public int getGapY() { return gapY; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}

class Cloud {
    private int x, y;
    private int speed;
    private float scale;
    private int id;

    public Cloud(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.speed = (int)(Math.random() * 2) + 1;
        this.scale = (float)(Math.random() * 0.5 + 0.8);
        this.id = id;
    }
    public void update() {
        x -= speed;
    }
    public boolean isOffScreen() {
        return x < -200;
    }
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 220));
        int w = (int)(80 * scale);
        int h = (int)(40 * scale);

        switch(id % 3) {
            case 0:
                g2d.fillOval(x, y, w, h);
                g2d.fillOval(x + w/3, y - h/3, w, h);
                g2d.fillOval(x + w/2, y + h/4, w, h);
                break;
            case 1:
                g2d.fillOval(x, y, w+10, h);
                g2d.fillOval(x + 20, y - 10, w-10, h+10);
                g2d.fillOval(x + 40, y+5, w-20, h-5);
                break;
            case 2:
                g2d.fillOval(x, y+5, w-10, h-5);
                g2d.fillOval(x+15, y-5, w, h);
                g2d.fillOval(x+35, y+8, w-15, h-8);
                break;
        }
    }
}

class Particle {
    private float x, y;
    private float vx, vy;
    private float size;
    private Color color;
    private int life;

    public Particle(float x, float y) {
        this.x = x;
        this.y = y;
        this.vx = (float)(Math.random() * 6 - 3);
        this.vy = (float)(Math.random() * 6 - 3);
        this.size = (float)(Math.random() * 8 + 2);
        this.color = new Color(
                (int)(Math.random() * 156 + 100),
                (int)(Math.random() * 156 + 100),
                (int)(Math.random() * 156 + 100),
                200
        );
        this.life = (int)(Math.random() * 30 + 20);
    }
    public void update() {
        x += vx;
        y += vy;
        vy += 0.1;
        life--;
        size *= 0.95;
    }
    public void draw(Graphics2D g2d) {
        float alpha = life / 50.0f;
        if (alpha < 0) alpha = 0;
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)));
        g2d.fillOval((int)x, (int)y, (int)size, (int)size);
    }
    public boolean isDead() {
        return life <= 0;
    }
}

class Coin {
    public static final int SIZE = 20;
    private int x, y;
    private float rotation;
    private float bounce;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
        this.rotation = 0;
        this.bounce = 0;
    }
    public void update(int speed) {
        x -= speed;
        rotation += 3;
        bounce = (float)Math.sin(System.currentTimeMillis() / 200.0) * 5;
    }
    public void draw(Graphics2D g2d) {
        AffineTransform original = g2d.getTransform();
        g2d.translate(x, y + bounce);
        g2d.rotate(Math.toRadians(rotation));
        GradientPaint goldGradient = new GradientPaint(
                -10, -10, new Color(255, 215, 0),
                10, 10, new Color(218, 165, 32)
        );
        g2d.setPaint(goldGradient);
        g2d.fillOval(-10, -10, SIZE, SIZE);
        g2d.setColor(new Color(139, 119, 0));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(-10, -10, SIZE, SIZE);
        g2d.setColor(new Color(100, 80, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("$", -5, 5);
        g2d.setTransform(original);
    }
    public boolean collidesWith(Bird bird) {
        Rectangle birdRect = bird.getBounds();
        Rectangle coinRect = new Rectangle(x - SIZE/2, y - SIZE/2, SIZE, SIZE);
        return birdRect.intersects(coinRect);
    }
    public int getX() { return x; }
    public int getY() { return y; }
}