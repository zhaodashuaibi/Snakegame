import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    // 游戏区域大小
    static final int SCREEN_WIDTH = 1920;
    static final int SCREEN_HEIGHT = 1080;
    // 游戏单元格大小
    static final int UNIT_SIZE = 80;
    // 游戏单元格数量
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    // 游戏速度（毫秒）
    static final int DELAY = 150;
    // 蛇身体的x和y坐标
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    // 蛇的初始长度
    int bodyParts = 6;
    // 吃到的食物数量
    int applesEaten = 0;
    // 食物x坐标
    int appleX;
    // 食物y坐标
    int appleY;
    // 移动方向 'U'=向上, 'D'=向下, 'L'=向左, 'R'=向右
    char direction = 'R';
    // 游戏是否正在运行
    boolean live = false;
    // 定时器
    Timer timer;
    // 随机数生成器
    Random random;
    
    // 图片资源
    private BufferedImage headImage;
    private BufferedImage bodyImage;
    private BufferedImage appleImage;

    public GamePanel() {
        random = new Random();
        // 设置面板大小
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        loadImages();
        startGame();
    }
    
    private void loadImages() {
        try {
            headImage = ImageIO.read(new File("resources/head.jpg"));
            int bodyIndex = random.nextInt(3) + 1; // 1, 2, 3
            bodyImage = ImageIO.read(new File("resources/body" + bodyIndex + ".jpg"));
            appleImage = ImageIO.read(new File("resources/apple.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("无法加载图像文件，将使用默认颜色替代");
        }
    }

    public void startGame() {
        // 初始化蛇的位置 - 修改这里
        // 让蛇头从一个网格对齐的位置开始，例如第3列，第3行
        x[0] = 2 * UNIT_SIZE; // 如果 UNIT_SIZE = 80, x[0] = 160
        y[0] = 2 * UNIT_SIZE; // 如果 UNIT_SIZE = 80, y[0] = 160

        // 根据蛇头位置初始化身体其他部分
        for (int i = 1; i < bodyParts; i++) {
            x[i] = x[0] - i * UNIT_SIZE; // 初始向右，身体在左边
            y[i] = y[0];
        }
        // 确保初始方向是 'R' 时，身体在头部的左侧
        // 如果初始方向是其他，则需要调整身体的相对位置
        // (您的代码中 direction 默认为 'R', bodyParts=6，所以 x[0]=160, x[1]=80, x[2]=0, ...)

        // 生成第一个食物
        newApple();
        // 启动游戏
        live = true;
        // 启动定时器
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (live) {
            /*
            // 绘制网格线（可选）
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            } */
            // 绘制食物
            if (appleImage != null) {
                g.drawImage(appleImage, appleX, appleY, null);
            } else {
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
            }

            // 绘制蛇
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    // 蛇头 - 根据移动方向旋转图像
                    if (headImage != null) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        // 使用图片的实际宽高来计算中心点
                        double imageActualWidth = headImage.getWidth();  // 获取图片实际宽度 (80)
                        double imageActualHeight = headImage.getHeight(); // 获取图片实际高度 (106)

                        // 旋转中心应基于图片左上角 (x[i], y[i]) 和其自身尺寸
                        double centerX = x[i] + imageActualWidth / 2.0;
                        double centerY = y[i] + imageActualHeight / 2.0;

                        double rotationAngle = 0.0; // 默认右方向，不旋转
                        switch (direction) {
                            case 'U':
                                rotationAngle = -Math.PI/2; // -90度
                                break;
                            case 'D':
                                rotationAngle = Math.PI/2; // 90度
                                break;
                            case 'L':
                                rotationAngle = Math.PI; // 180度
                                break;
                            case 'R':
                                rotationAngle = 0.0; // 0度
                                break;
                        }

                        // 应用旋转变换
                        AffineTransform transform = new AffineTransform();
                        transform.rotate(rotationAngle, centerX, centerY);
                        g2d.setTransform(transform);
                        
                        // 绘制图片，图片的左上角在 (x[i], y[i])
                        g2d.drawImage(headImage, x[i], y[i], null);
                        g2d.dispose();
                    } else {
                        // 如果图像无法加载，使用默认颜色
                        g.setColor(Color.green);
                        g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                    }
                } else {
                    // 蛇身
                    if (bodyImage != null) {
                        g.drawImage(bodyImage, x[i], y[i], null);
                    } else {
                        // 如果图像无法加载，使用默认颜色
                        g.setColor(new Color(45, 180, 0));
                        g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                    }
                }
            }
            
            // 绘制分数
            g.setColor(Color.white);
            g.setFont(new Font("微软雅黑", Font.BOLD, 30));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("SCORE: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("SCORE: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            // 游戏结束画面
            gameOver(g);
        }
    }

    public void newApple() {
        // 生成随机位置的食物
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        // 移动蛇身
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // 根据方向移动蛇头
        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        // 检测是否吃到食物
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            // 播放音效
            try {
                javax.sound.sampled.AudioInputStream audioInputStream = javax.sound.sampled.AudioSystem.getAudioInputStream(new java.io.File("resources/eat.wav"));
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                System.out.println("无法播放音效: " + e.getMessage());
            }
            loadImages();
        }
    }

    public void checkCollisions() {
        // 检测是否撞到自己（只检查蛇头与蛇身的碰撞）
        for (int i = 1; i < bodyParts; i++) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                live = false;
                break;
            }
        }
        
        // 检测是否撞到边界
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            live = false;
        }

        // 如果游戏停止，停止定时器
        if (!live) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // 绘制分数
        g.setColor(Color.white);
        g.setFont(new Font("微软雅黑", Font.BOLD, 30));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("分数: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("分数: " + applesEaten)) / 2, g.getFont().getSize());
        
        // 绘制"游戏结束"
        g.setColor(Color.red);
        g.setFont(new Font("微软雅黑", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("GAME OVER", (SCREEN_WIDTH - metrics2.stringWidth("GAME OVER")) / 2, SCREEN_HEIGHT / 2);
        // 播放游戏结束音效
        try {
            javax.sound.sampled.AudioInputStream audioInputStream = javax.sound.sampled.AudioSystem.getAudioInputStream(new java.io.File("resources/gameover.wav"));
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("无法播放游戏结束音效: " + e.getMessage());
     }
    }

    public void restartGame() {
        // 重置游戏状态
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R'; // 确保初始方向

        // 重置蛇的位置（与startGame方法一致） - 修改这里
        x[0] = 2 * UNIT_SIZE; 
        y[0] = 2 * UNIT_SIZE; 

        for (int i = 1; i < bodyParts; i++) {
            x[i] = x[0] - i * UNIT_SIZE;
            y[i] = y[0];
        }
        
        // 开始新游戏
        newApple();
        live = true;
        timer.start(); // 确保定时器重新启动
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (live) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    // 键盘控制类
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_R:
                        restartGame();
                    break;
                case KeyEvent.VK_B:
                    bodyParts++;
                    break;
            }
        }
    }
} 