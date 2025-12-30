import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

public class TypingTutor extends JFrame implements KeyListener {

    // 鍵盤 layout
    private static final String[] KEYS = {
            "~", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "Backspace",
            "Tab", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\",
            "Caps", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "↑", "Enter",
            "Shift", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "?", "←", "↓", "→"};

    // 鍵盤排版
    private static final int KEYBOARD_ROWS = 4;
    private static final int KEYBOARD_COLS = 15;

    // 按鍵大小
    private static final Dimension KEY_SIZE = new Dimension(60, 60);

    // 顏色常數
    private static final Color BACKGROUND_COLOR = new Color(249, 241, 241);
    private static final Color KEY_NORMAL_COLOR = new Color(196, 191, 223);
    private static final Color KEY_PRESSED_COLOR = new Color(245, 246, 141);

    // 文字區大小
    private static final int TEXTAREA_ROWS = 5;
    private static final int TEXTAREA_COLS = 30;

    // 視窗大小
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 300;

    private JPanel keysPanel;
    private JTextArea textArea;

    // key label → 按鈕
    private Map<String, JButton> labelToButton = new HashMap<>();
    // keyCode → 特殊鍵 label
    private Map<Integer, String> specialKeyLabelByCode = new HashMap<>();
    // 方向鍵行為：用 enum 取代裸 int（避免 primitive obsession）
    private Map<ArrowKey, Runnable> arrowKeyActions = new HashMap<>();

    // 初始化
    public TypingTutor() {
        super("Touch Typing");

        initKeyboardPanel();
        initTextArea();
        initWindow();
        initTabBehavior();
    }

    // 建立虛擬鍵盤與映射
    private void initKeyboardPanel() {
        keysPanel = new JPanel(new GridLayout(KEYBOARD_ROWS, KEYBOARD_COLS, 1, 1));
        keysPanel.setBackground(BACKGROUND_COLOR);

        for (String key : KEYS) {
            JButton button = new JButton(key);
            button.setPreferredSize(KEY_SIZE);
            button.setBorder(new LineBorder(Color.WHITE, 1));
            button.setBackground(KEY_NORMAL_COLOR);
            button.addKeyListener(this);

            // 記錄 label → JButton
            labelToButton.put(key, button);
            keysPanel.add(button);
        }

        // 建立特殊 keyCode → label 映射
        specialKeyLabelByCode.put(KeyEvent.VK_BACK_SPACE, "Backspace");
        specialKeyLabelByCode.put(KeyEvent.VK_TAB, "Tab");
        specialKeyLabelByCode.put(KeyEvent.VK_CAPS_LOCK, "Caps");
        specialKeyLabelByCode.put(KeyEvent.VK_ENTER, "Enter");
        specialKeyLabelByCode.put(KeyEvent.VK_SHIFT, "Shift");
        specialKeyLabelByCode.put(KeyEvent.VK_UP, "↑");
        specialKeyLabelByCode.put(KeyEvent.VK_DOWN, "↓");
        specialKeyLabelByCode.put(KeyEvent.VK_LEFT, "←");
        specialKeyLabelByCode.put(KeyEvent.VK_RIGHT, "→");

        // 建立方向鍵 key → 對應動作（取代原本的 switch）
        arrowKeyActions.put(ArrowKey.UP,    this::moveCaretUp);
        arrowKeyActions.put(ArrowKey.DOWN,  this::moveCaretDown);
        arrowKeyActions.put(ArrowKey.LEFT,  this::moveCaretLeft);
        arrowKeyActions.put(ArrowKey.RIGHT, this::moveCaretRight);
    }

    // 建立文字區域
    private void initTextArea() {
        textArea = new JTextArea(TEXTAREA_ROWS, TEXTAREA_COLS);
        textArea.setBackground(BACKGROUND_COLOR);
        textArea.setFont(new Font("SERIF", Font.PLAIN, 20));
    }

    // 視窗與版面配置
    private void initWindow() {
        add(keysPanel, BorderLayout.SOUTH);
        add(textArea, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 自訂 TAB 行為
    private void initTabBehavior() {
        // 禁用默認 TAB 的焦點移動行為
        keysPanel.setFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                Collections.emptySet());
        keysPanel.setFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                Collections.emptySet());

        String tabKey = "tabKey";
        InputMap inputMap =
                keysPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), tabKey);
        ActionMap actionMap = keysPanel.getActionMap();
        actionMap.put(tabKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 不做任何事，避免 TAB 亂跳焦點
            }
        });
    }

    // ==============================
    // caret / 文字編輯相關邏輯
    // ==============================

    // caret 左移
    private void moveCaretLeft() {
        int caretPosition = textArea.getCaretPosition();
        if (caretPosition > 0) {
            textArea.setCaretPosition(caretPosition - 1);
        }
    }

    // caret 右移
    private void moveCaretRight() {
        int caretPosition = textArea.getCaretPosition();
        if (caretPosition < textArea.getText().length()) {
            textArea.setCaretPosition(caretPosition + 1);
        }
    }

    // caret 上移一行（盡量維持同一欄）
    private void moveCaretUp() {
        try {
            int caretPosition = textArea.getCaretPosition();
            int line = textArea.getLineOfOffset(caretPosition);
            int col = caretPosition - textArea.getLineStartOffset(line);

            if (line > 0) {
                int prevStart = textArea.getLineStartOffset(line - 1);
                int prevEnd = textArea.getLineEndOffset(line - 1);
                int newPos = Math.min(prevStart + col, prevEnd - 1);
                textArea.setCaretPosition(newPos);
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    // caret 下移一行（盡量維持同一欄）
    private void moveCaretDown() {
        try {
            int caretPosition = textArea.getCaretPosition();
            int line = textArea.getLineOfOffset(caretPosition);
            int col = caretPosition - textArea.getLineStartOffset(line);

            if (line < textArea.getLineCount() - 1) {
                int nextStart = textArea.getLineStartOffset(line + 1);
                int nextEnd = textArea.getLineEndOffset(line + 1);
                int newPos = Math.min(nextStart + col, nextEnd - 1);
                textArea.setCaretPosition(newPos);
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    // 處理 Backspace：刪除前一個字元
    private void handleBackspace() {
        int caretPosition = textArea.getCaretPosition();
        if (caretPosition > 0 && caretPosition <= textArea.getDocument().getLength()) {
            try {
                textArea.getDocument().remove(caretPosition - 1, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 插入一般按鍵對應的字元
    private void insertTypedChar(char key) {
        textArea.insert(Character.toString(key), textArea.getCaretPosition());
    }

    // ==============================
    // KeyListener 介面實作
    // ==============================

    // 按下按鍵
    @Override
    public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        JButton button = getButton(Character.toUpperCase(key), e);
        if (button != null) {
            button.setBackground(KEY_PRESSED_COLOR);
        }
    }

    // 鬆開按鍵
    @Override
    public void keyReleased(KeyEvent e) {
        char key = e.getKeyChar();
        JButton button = getButton(Character.toUpperCase(key), e);

        // 還原虛擬鍵盤顏色
        if (button != null) {
            button.setBackground(KEY_NORMAL_COLOR);
        }

        int keyCode = e.getKeyCode();

        // Caps Lock、Shift：只改顏色，不改文字
        if (keyCode == KeyEvent.VK_CAPS_LOCK || keyCode == KeyEvent.VK_SHIFT) {
            return;
        }

        // Backspace：刪除前一個字元
        if (keyCode == KeyEvent.VK_BACK_SPACE) {
            handleBackspace();
            return;
        }

        // 方向鍵：用 enum + Map 查表取代 switch
        ArrowKey arrowKey = ArrowKey.fromKeyCode(keyCode);
        Runnable action = arrowKeyActions.get(arrowKey);
        if (action != null) {
            action.run();
            return;
        }

        // 其他一般按鍵：插入對應字元
        insertTypedChar(key);
    }

    // 輸入字符（交給 keyReleased / JTextArea 控制，不在這裡做事）
    @Override
    public void keyTyped(KeyEvent e) {
        // 不用做任何事
    }

    // ==============================
    // 查找對應按鍵（重構後版本）
    // ==============================
    private JButton getButton(char key, KeyEvent e) {
        // 1. 先處理特殊按鍵（Backspace、Tab、Caps、Enter、Shift、方向鍵）
        String specialLabel = specialKeyLabelByCode.get(e.getKeyCode());
        if (specialLabel != null) {
            return labelToButton.get(specialLabel);
        }

        // 2. 一般按鍵：使用按鍵文字，而不是 component index
        for (JButton button : labelToButton.values()) {
            String text = button.getText();

            // 跳過特殊鍵（已經在上面處理過）
            if (text.equals("Backspace") || text.equals("Tab") || text.equals("Caps")) {
                continue;
            }

            if (text.charAt(0) == key) {
                return button;
            }
        }
        return null;
    }

    // 方向鍵 enum（取代原本裸露的 int keyCode）
    private enum ArrowKey {
        UP(KeyEvent.VK_UP),
        DOWN(KeyEvent.VK_DOWN),
        LEFT(KeyEvent.VK_LEFT),
        RIGHT(KeyEvent.VK_RIGHT);

        private final int keyCode;

        ArrowKey(int keyCode) {
            this.keyCode = keyCode;
        }

        static ArrowKey fromKeyCode(int keyCode) {
            for (ArrowKey k : values()) {
                if (k.keyCode == keyCode) {
                    return k;
                }
            }
            return null;
        }
    }

    public static void main(String[] args) {
        new TypingTutor();
    }
}
