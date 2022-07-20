package practicum.concurrency.problem.gui;

import practicum.concurrency.problem.gui.market.MoexQuoteDownloader;
import practicum.concurrency.problem.gui.market.QuoteDto;

import javax.swing.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class QuoteAppender {
    private static final MoexQuoteDownloader quoteDownloader = new MoexQuoteDownloader();

    private final SortedSet<QuoteDto> sortedQuotes = new TreeSet<>(Comparator.comparing(QuoteDto::getDate));
    private final Executor executor;
    private final JTextArea outputTextArea;

    public QuoteAppender(JTextArea outputTextArea, int numActiveThreads) {
        this.outputTextArea = outputTextArea;
        this.executor = Executors.newFixedThreadPool(numActiveThreads);
        startAppenderThread();
    }

    public void startForStock(String stockSymbol) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusMonths(1);
        for (var start = startDate; !start.isAfter(endDate); start = start.plusDays(1)) {
            executor.execute(new Worker(quoteDownloader, stockSymbol, sortedQuotes, start, start));
        }
    }

    public void reset() {
        sortedQuotes.clear();
    }

    private void startAppenderThread() {
        Thread textAreaAppenderThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (sortedQuotes) {
                    try {
                        sortedQuotes.wait();
                        outputTextArea.setText(formatQuotes());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        textAreaAppenderThread.start();
    }

    private String formatQuotes() {
        StringBuilder builder = new StringBuilder();
        sortedQuotes.forEach(quote -> builder.append(quote.getDate())
                .append(": ")
                .append(quote.getClose()).append("\n"));
        return builder.toString();
    }
}
