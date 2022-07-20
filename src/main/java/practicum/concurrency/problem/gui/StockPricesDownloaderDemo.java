package practicum.concurrency.problem.gui;

import javax.swing.*;

public class StockPricesDownloaderDemo extends JFrame {
    private static final int DEFAULT_NUM_ACTIVE_THREADS = 4;
    private static final String DEFAULT_STOCK_SYMBOL = "MOEX";
    private static final String BUTTON_TEXT_STOP = "Stop";
    private static final String BUTTON_TEXT_START = "Start";

    enum ButtonState {
        STOPPED,
        STARTED
    }

    private ButtonState buttonState = ButtonState.STOPPED;

    private StockPricesDownloaderDemo()  {
        var frame = new JFrame("Stock price loader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setResizable(false);

        var panel = new JPanel();
        frame.add(panel);

        var button = new JButton(BUTTON_TEXT_START);
        panel.add(button);

        var textField = new JTextField(16);
        textField.setText(DEFAULT_STOCK_SYMBOL);
        panel.add(textField);

        var inputLabel = new JLabel("Enter stock symbol");
        panel.add(inputLabel);

        var outputTextArea = new JTextArea(22, 35);
        outputTextArea.setEditable(false);
        var scroll = new JScrollPane(outputTextArea);
        panel.add(scroll);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final QuoteAppender quoteAppender = new QuoteAppender(outputTextArea, DEFAULT_NUM_ACTIVE_THREADS);

        button.addActionListener(e -> {
            if (buttonState == ButtonState.STOPPED) {
                button.setText(BUTTON_TEXT_STOP);
                buttonState = ButtonState.STARTED;
                quoteAppender.startForStock(textField.getText());
            } else {
                button.setText(BUTTON_TEXT_START);
                buttonState = ButtonState.STOPPED;
                outputTextArea.setText("");
                quoteAppender.reset();
            }
        });

        frame.setVisible(true);
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(StockPricesDownloaderDemo::new);
    }
}
