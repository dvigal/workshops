package practicum.concurrency.problem.gui.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class QuoteDto {
    private Long tickerId;
    @JsonFormat
    private LocalDate date;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal adjClose;
    private BigDecimal volume;
    private String boardId;
}
