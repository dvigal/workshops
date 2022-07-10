package practicum.concurrency.problem.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import lombok.RequiredArgsConstructor;
import practicum.concurrency.problem.gui.market.MoexQuoteDownloader;
import practicum.concurrency.problem.gui.market.QuoteDto;

public class StockPricesDownloaderDemo {
    private static final String BUTTON_TEXT_STOP = "Stop";
    private static final String BUTTON_TEXT_START = "Start";

    enum ButtonState {
        STOPPED,
        STARTED
    }

    private static ButtonState buttonState = ButtonState.STOPPED;
    private static final MoexQuoteDownloader quoteDownloader = new MoexQuoteDownloader();
    private static final Executor executor = Executors.newFixedThreadPool(4);
    private static final ConcurrentLinkedQueue<QuoteDto> queue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean isRunning = new AtomicBoolean();

    public static void main(String... args) {
        var frame = new JFrame("Stock price loader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setResizable(false);

        var panel = new JPanel();
        frame.add(panel);

        var button = new JButton(BUTTON_TEXT_START);
        panel.add(button);

        var textField = new JTextField(16);
        panel.add(textField);

        var inputLabel = new JLabel("Enter stock symbol");
        panel.add(inputLabel);

        var textArea = new JTextArea(10, 35);
        textArea.setEditable(false);
        panel.add(textArea);

        var scroll = new JScrollPane(textArea);
        panel.add(scroll);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buttonState == ButtonState.STOPPED) {
                    button.setText(BUTTON_TEXT_STOP);
                    buttonState = ButtonState.STARTED;
                    startDownloaderWorkers(textField.getText());
                    isRunning.set(true);
                } else {
                    button.setText(BUTTON_TEXT_START);
                    buttonState = ButtonState.STOPPED;
                    isRunning.set(false);
                }
            }
        });

        new TextAreaAppender(textArea).start();

        frame.setVisible(true);
    }

    private static class TextAreaAppender extends Thread {
        private final Set<QuoteDto> quotes = new TreeSet<>(Comparator.comparing(QuoteDto::getDate));
        private final JTextArea textArea;

        private TextAreaAppender(JTextArea textArea) {
            super("TextAreaAppender");
            setDaemon(true);
            this.textArea = textArea;
        }

        @Override
        public void run() {
            for (;;) {
                final int sizeBefore = quotes.size();
                if (isRunning.get()) {
                    poolQueue();
                } else {
                    quotes.clear();
                }
                print(sizeBefore);
                sleep();
            }
        }

        private void sleep() {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void poolQueue() {
            while (queue.peek() != null) {
                quotes.add(queue.poll());
            }
        }

        private void print(int sizeBefore) {
            if (sizeBefore != quotes.size()) {
                textArea.setText("");
                quotes.forEach(quote -> textArea.append(quote.getDate() + ": " + quote.getClose() + "\n"));
            }
        }
    }

    private static void startDownloaderWorkers(String symbol) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusMonths(1);
        for (var start = startDate; !start.isAfter(endDate); start = start.plusDays(1)) {
            var worker = new Worker(symbol, start, start);
            executor.execute(worker);
        }
    }

    @RequiredArgsConstructor
    private static class Worker implements Runnable {
        private final String symbol;
        private final LocalDate startDate;
        private final LocalDate endDate;

        @Override
        public void run() {
            var quotes = quoteDownloader.download(symbol, startDate, endDate);
            System.out.println(quotes);
            quotes.forEach(queue::offer);
        }
    }

}
