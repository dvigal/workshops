package practicum.concurrency.problem.gui.market;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;


@Accessors(chain = true)
@Getter
@Setter
@Data
public class StockPriceModel {
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal open;
    private BigDecimal close;
    private LocalDate date;
    private String ticker;
}