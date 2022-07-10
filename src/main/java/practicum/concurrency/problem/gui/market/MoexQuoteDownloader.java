package practicum.concurrency.problem.gui.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MoexQuoteDownloader {
    private final Set<String> KNOWN_COLUMNS = new HashSet<>(
            Arrays.asList(
                    "BOARDID",
                    "TRADEDATE",
                    "SHORTNAME",
                    "SECID",
                    "NUMTRADES",
                    "VALUE",
                    "OPEN",
                    "LOW",
                    "HIGH",
                    "LEGALCLOSEPRICE",
                    "WAPRICE",
                    "CLOSE",
                    "VOLUME",
                    "MARKETPRICE2",
                    "MARKETPRICE3",
                    "ADMITTEDQUOTE",
                    "MP2VALTRD",
                    "MARKETPRICE3TRADESVALUE",
                    "ADMITTEDVALUE",
                    "WAVAL",
                    "TRADINGSESSION"
            )
    );
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<QuoteDto> download(String ticker, LocalDate start, LocalDate end) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 2000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var quotes = new ArrayList<QuoteDto>();
        var index = 0;
        var total = 0;
        var pageSize = 0;
        while (true) {
            var page = loadPage(ticker, start, end, index);

            var pageCursor = Utils.getPageCursor(page);

            if (pageCursor.getTotal() == 0) {
                break;
            }
            if (index == 0) {
                index = pageCursor.getIndex();
                total = pageCursor.getTotal();
                pageSize = pageCursor.getPageSize();
            }
            quotes.addAll(page.getData().data
                    .stream()
                    .map(row -> Utils.zip(page.getData().columns, row))
                    .map(this::toQuote)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                            .filter(quote -> "TQBR".equalsIgnoreCase(quote.getBoardId()))
                    .collect(Collectors.toList())
            );
            if (index + pageSize > total) {
                break;
            }
            index = index + pageSize;
        }
        return quotes;
    }

    private boolean isNull(Object value) {
        return value == null || "null".equalsIgnoreCase(value.toString());
    }

    private BigDecimal toBigDecimalOrNull(Object value) {
        return isNull(value) ? null : new BigDecimal(value.toString());
    }

    private Optional<QuoteDto> toQuote(List<Utils.Pair<String, Object>> data) {
        var quote = new QuoteDto();
        for (final Utils.Pair<String, Object> pair : data) {
            var column = pair._1;
            var value = pair._2;
            if (!KNOWN_COLUMNS.contains(column.toUpperCase().trim().strip())) {
                throw new IllegalStateException("UNKNOWN MOEX COLUMN '" + column + "'");
            }
            if ("BOARDID".equalsIgnoreCase(column)) {
                quote.setBoardId(value.toString());
            }
            if ("OPEN".equalsIgnoreCase(column)) {
                quote.setOpen(toBigDecimalOrNull(value));
            }
            if ("CLOSE".equalsIgnoreCase(column)) {
                quote.setClose(toBigDecimalOrNull(value));
            }
            if ("LOW".equalsIgnoreCase(column)) {
                quote.setLow(toBigDecimalOrNull(value));
            }
            if ("HIGH".equalsIgnoreCase(column)) {
                quote.setHigh(toBigDecimalOrNull(value));
            }
            if ("VOLUME".equalsIgnoreCase(column)) {
                quote.setVolume(toBigDecimalOrNull(value));
            }
            if ("TRADEDATE".equalsIgnoreCase(column)) {
                quote.setDate(LocalDate.parse(value.toString()));
            }
        }

        return Optional.of(quote);
    }

    @SneakyThrows
    private Page loadPage(String symbol, LocalDate start, LocalDate end, int index) {
        var url = String.format(
                "https://iss.moex.com/iss/history/engines/stock/markets/shares/sessions/%s/securities/%s.json?from=%s&till=%s&limit=%s&start=%s&sort_order=asc",
                3,
                symbol,
                start.toString(),
                end.toString(),
                100,
                index
        );
        System.out.println(url);
        var builder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET();
        var request = builder.build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        return objectMapper.readValue(response, Page.class);
    }

    @Getter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Page implements Utils.Page {
        @JsonProperty("history")
        private History data;
        @JsonProperty("history.cursor")
        private HistoryCursor cursor;
    }

    @Getter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class History implements Utils.Data {
        @JsonProperty("columns")
        public List<String> columns;
        @JsonProperty("data")
        public List<List<Object>> data;
    }

    @Getter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryCursor implements Utils.Cursor {
        @JsonProperty("columns")
        public List<String> columns;
        @JsonProperty("data")
        public List<List<Integer>> data;
    }

    public static void main(String ...args) {
        var loader = new MoexQuoteDownloader();
        loader.download("MOEX", LocalDate.parse("2022-07-07"), LocalDate.now()).forEach(System.out::println);
    }
}
