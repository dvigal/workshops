package practicum.concurrency.problem.gui;

import lombok.RequiredArgsConstructor;
import practicum.concurrency.problem.gui.market.MoexQuoteDownloader;
import practicum.concurrency.problem.gui.market.QuoteDto;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

public class StockPricesDownloaderDemo {
    private static final String BUTTON_TEXT_STOP = "Stop";
    private static final String BUTTON_TEXT_START = "Start";

    enum ButtonState {
        STOPPED,
        STARTED
    }

    private static ButtonState buttonState = ButtonState.STOPPED;
    private static final MoexQuoteDownloader quoteDownloader = new MoexQuoteDownloader();
    private static final List<QuoteDto> quotes = new CopyOnWriteArrayList<>();
    private static Thread textAreaAppenderThread;

    private static final Phaser phaser = new Phaser(1);
    private static final ConcurrentLinkedQueue<QuoteDto> queue = new ConcurrentLinkedQueue<>();
    private static final Executor executor = Executors.newFixedThreadPool(4);

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
                    startDownloaderWorkers(textField.getText(), quotes);
                } else {
                    button.setText(BUTTON_TEXT_START);
                    buttonState = ButtonState.STOPPED;
                    textArea.setText("");
                    quotes.clear();
                }
            }
        });

        textAreaAppenderThread = new Thread(() -> {
            while (true) {
                phaser.awaitAdvance(phaser.getPhase());
                while (!queue.isEmpty()) {
                    quotes.add(queue.poll());
                }
                quotes.sort(Comparator.comparing(QuoteDto::getDate));
                quotes.forEach(quoteDto -> textArea.append(quoteDto.getDate() + ": " + quoteDto.getClose() + "\n"));
            }
        });

        textAreaAppenderThread.start();

        frame.setVisible(true);
    }

    private static void startDownloaderWorkers(String symbol, List<QuoteDto> result) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusMonths(1);
        for (var start = startDate; !start.isAfter(endDate); start = start.plusDays(1)) {
            phaser.register();
            executor.execute(new Worker(symbol, result, start, start));
        }
        phaser.arrive();
    }

    @RequiredArgsConstructor
    private static class Worker implements Runnable {
        private final String symbol;
        private final List<QuoteDto> result;
        private final LocalDate startDate;
        private final LocalDate endDate;

        @Override
        public void run() {
            var quotes = quoteDownloader.download(symbol, startDate, endDate);
            System.out.println(quotes);
            quotes.forEach(queue::offer);
            phaser.arriveAndDeregister();
        }
    }

}
