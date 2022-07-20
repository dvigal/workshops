package practicum.concurrency.problem.gui;

import lombok.RequiredArgsConstructor;
import practicum.concurrency.problem.gui.market.MoexQuoteDownloader;
import practicum.concurrency.problem.gui.market.QuoteDto;

import java.time.LocalDate;
import java.util.SortedSet;

@RequiredArgsConstructor
class Worker implements Runnable {
    private final MoexQuoteDownloader quoteDownloader;
    private final String symbol;
    private final SortedSet<QuoteDto> waitingSetOfQuotes;
    private final LocalDate startDate;
    private final LocalDate endDate;

    @Override
    public void run() {
        var quotes = quoteDownloader.download(symbol, startDate, endDate);
        System.out.println(quotes);
        synchronized (waitingSetOfQuotes) {
            waitingSetOfQuotes.addAll(quotes);
            waitingSetOfQuotes.notifyAll();
        }
    }
}
