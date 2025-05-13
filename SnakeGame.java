import javax.swing.*;

public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("军营爱贪吃");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}