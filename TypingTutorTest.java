import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TypingTutorTest {

    private TypingTutor tutor;
    private JTextArea textArea;

    @Before
    public void setUp() throws Exception {
        // 建立被測物件
        tutor = new TypingTutor();

        // 用反射取得 private 的 textArea 欄位
        Field textAreaField = TypingTutor.class.getDeclaredField("textArea");
        textAreaField.setAccessible(true);
        textArea = (JTextArea) textAreaField.get(tutor);
    }

    @After
    public void tearDown() {
        // 關掉視窗，避免測試跑完還留著
        tutor.dispose();
    }

    // 小工具：建立 KeyEvent
    private KeyEvent createKeyEvent(int id, int keyCode, char keyChar) {
        return new KeyEvent(
                textArea,          // 事件來源 component（這裡用 textArea 即可）
                id,                // KEY_PRESSED / KEY_RELEASED
                System.currentTimeMillis(),
                0,                 // modifiers
                keyCode,
                keyChar
        );
    }

    @Test
    public void testInsertCharacterOnKeyReleased() {
        textArea.setText("");
        textArea.setCaretPosition(0);

        // 模擬輸入 'a'
        KeyEvent e = createKeyEvent(
                KeyEvent.KEY_RELEASED,
                KeyEvent.VK_A,
                'a'
        );
        tutor.keyReleased(e);

        assertEquals("a", textArea.getText());
    }

    @Test
    public void testBackspaceRemovesPreviousCharacter() {
        textArea.setText("ABC");
        textArea.setCaretPosition(3); // 游標在最後

        // 模擬 Backspace
        KeyEvent e = createKeyEvent(
                KeyEvent.KEY_RELEASED,
                KeyEvent.VK_BACK_SPACE,
                '\b'
        );
        tutor.keyReleased(e);

        assertEquals("AB", textArea.getText());
        assertEquals(2, textArea.getCaretPosition());
    }

    @Test
    public void testArrowRightMovesCaretRight() {
        textArea.setText("AB");
        textArea.setCaretPosition(0);

        // 模擬方向鍵右
        KeyEvent e = createKeyEvent(
                KeyEvent.KEY_RELEASED,
                KeyEvent.VK_RIGHT,
                KeyEvent.CHAR_UNDEFINED
        );
        tutor.keyReleased(e);

        assertEquals(1, textArea.getCaretPosition());
    }

    @Test
    public void testArrowLeftDoesNotMoveBeforeStart() {
        textArea.setText("AB");
        textArea.setCaretPosition(0);

        // 模擬方向鍵左
        KeyEvent e = createKeyEvent(
                KeyEvent.KEY_RELEASED,
                KeyEvent.VK_LEFT,
                KeyEvent.CHAR_UNDEFINED
        );
        tutor.keyReleased(e);

        assertEquals(0, textArea.getCaretPosition());
    }

    @Test
    public void testBackspaceVirtualKeyIsHighlightedOnKeyPressed() throws Exception {
        // 用反射取得 keysPanel
        Field keysPanelField = TypingTutor.class.getDeclaredField("keysPanel");
        keysPanelField.setAccessible(true);
        JPanel keysPanel = (JPanel) keysPanelField.get(tutor);

        // 模擬按下 Backspace
        KeyEvent e = createKeyEvent(
                KeyEvent.KEY_PRESSED,
                KeyEvent.VK_BACK_SPACE,
                '\b'
        );
        tutor.keyPressed(e);

        // 按下時應該有一顆按鍵變成「按下顏色」
        Color pressedColor = new Color(245, 246, 141);
        JButton highlightedButton = null;

        for (Component c : keysPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (pressedColor.equals(b.getBackground())) {
                    highlightedButton = b;
                    break;
                }
            }
        }

        assertNotNull("Backspace 對應的虛擬按鍵應該被反白", highlightedButton);
        assertEquals("Backspace", highlightedButton.getText());
    }
}
