package practicum.concurrency.problem.gui;

import lombok.RequiredArgsConstructor;
import practicum.concurrency.problem.gui.market.MoexQuoteDownloader;
import practicum.concurrency.problem.gui.market.QuoteDto;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StockPricesDownloaderDemo {
    private static final String BUTTON_TEXT_STOP = "Stop";
    private static final String BUTTON_TEXT_START = "Start";

    enum ButtonState {
        STOPPED,
        STARTED
    }

    private static ButtonState buttonState = ButtonState.STOPPED;
    private static final MoexQuoteDownloader quoteDownloader = new MoexQuoteDownloader();
    private static final SortedSet<QuoteDto> quotes = new TreeSet<>(Comparator.comparing(QuoteDto::getDate));
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

        var textArea = new JTextArea(22, 35);
        textArea.setEditable(false);
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

        Thread textAreaAppenderThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (quotes) {
                    try {
                        quotes.wait();
                        textArea.setText("");
                        quotes.forEach(quote -> textArea.append(quote.getDate() + ": " + quote.getClose() + "\n"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        textAreaAppenderThread.start();

        frame.setVisible(true);
    }

    private static void startDownloaderWorkers(String symbol, SortedSet<QuoteDto> result) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusMonths(1);
        for (var start = startDate; !start.isAfter(endDate); start = start.plusDays(1)) {
            executor.execute(new Worker(symbol, result, start, start));
        }
    }

    @RequiredArgsConstructor
    private static class Worker implements Runnable {
        private final String symbol;
        private final SortedSet<QuoteDto> result;
        private final LocalDate startDate;
        private final LocalDate endDate;

        @Override
        public void run() {
            var quotes = quoteDownloader.download(symbol, startDate, endDate);
            System.out.println(quotes);
            synchronized (result) {
                result.addAll(quotes);
                result.notifyAll();
            }
        }
    }

}
