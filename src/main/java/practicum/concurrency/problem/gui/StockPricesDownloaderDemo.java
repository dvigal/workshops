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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final List<Thread> workers = new ArrayList<>();
    private static final List<QuoteDto> quotes = new CopyOnWriteArrayList<>();
    private static Thread textAreaAppenderThread;
    private static final Object monitor = new Object();
    private static final AtomicInteger counterWorkers = new AtomicInteger(0);
    private static final List<QuoteDto> cacheCollection = new CopyOnWriteArrayList<>();

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
                    textField.setEnabled(false);
                    buttonState = ButtonState.STARTED;
                    startDownloaderWorkers(textField.getText(), quotes);
                } else {
                    button.setText(BUTTON_TEXT_START);
                    textField.setEnabled(true);
                    buttonState = ButtonState.STOPPED;
                    textArea.setText("");
                    workers.forEach(thread -> thread.interrupt());
                    workers.clear();
                    quotes.clear();
                }
            }
        });

        textAreaAppenderThread = new Thread(() -> {
            while (true) {
                if (counterWorkers.get() != 0) {
                    synchronized (quotes) {
                        if (quotes.isEmpty() && !textArea.getText().equalsIgnoreCase(""))
                            continue;
                        synchronized (cacheCollection) {
                            cacheCollection.addAll(quotes.stream().collect(Collectors.toList()));
                            textArea.setText("");
                            cacheCollection.stream()
                                    .filter(quote -> quote.getTicker().equalsIgnoreCase(textField.getText()))
                                    .sorted(Comparator.comparing(QuoteDto::getDate))
                                    .forEach(quote -> {
                                        textArea.append(quote.getDate() + ": " + quote.getClose() + "\n");
                                    });
                            quotes.clear();
                        }
                    }
                }
            }
        });
        textAreaAppenderThread.start();

        frame.setVisible(true);
    }

    private static void startDownloaderWorkers(String symbol, List<QuoteDto> result) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusMonths(1);
        for (var start = startDate; !start.isAfter(endDate); start = start.plusDays(1)) {
            synchronized (cacheCollection) {
                LocalDate finalStart = start;
                if ( result.stream()
                        .filter(quote -> quote.getTicker().equalsIgnoreCase(symbol))
                        .anyMatch(quote -> quote.getDate().isEqual(finalStart)) )
                    continue;
                var thread = new Thread(new Worker(symbol, result, start, start));
                workers.add(thread);
                counterWorkers.incrementAndGet();
                thread.start();
            }
        }
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
            synchronized (result) {
                result.addAll(quotes);
                counterWorkers.decrementAndGet();
            }
        }
    }

}
