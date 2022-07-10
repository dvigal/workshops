package practicum.concurrency.problem.gui.market;

import lombok.Builder;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Utils {

    public interface Page {

        Data getData();

        Cursor getCursor();

    }

    public interface Data {

        List<String> getColumns();

        List<List<Object>> getData();

    }

    public interface Cursor {

        List<String> getColumns();

        List<List<Integer>> getData();

    }

    public PageCursor getPageCursor(Page page) {
        if (page.getData() == null || page.getData().getData() == null || page.getData().getData().isEmpty()) {
            return new PageCursor();
        }
        if (page.getCursor() == null || page.getCursor().getColumns() == null || page.getCursor().getColumns().isEmpty()) {
            return new PageCursor();
        }
        if (page.getCursor().getData() == null || page.getCursor().getData().isEmpty()) {
            return new PageCursor();
        }
        var pageCursor = new PageCursor();
        zip(page.getCursor().getColumns(), page.getCursor().getData().get(0)).forEach(e -> {
            if ("INDEX".equalsIgnoreCase(e._1)) {
                pageCursor.setIndex(e._2);
            }
            if ("TOTAL".equalsIgnoreCase(e._1)) {
                pageCursor.setTotal(e._2);
            }
            if ("PAGESIZE".equalsIgnoreCase(e._1)) {
                pageCursor.setPageSize(e._2);
            }
        });
        return pageCursor;
    }

    public <A, B> List<Pair<A, B>> zip(List<A> fstList, List<B> sndList) {
        var result = new ArrayList<Pair<A, B>>();
        for (int i = 0; i < fstList.size() && i < sndList.size(); i++) {
            result.add(Pair.<A, B>builder()._1(fstList.get(i))._2(sndList.get(i)).build());
        }
        return result;
    }

    @Builder
    public static class Pair<A, B> {
        public final A _1;
        public final B _2;
    }

    @lombok.Data
    @Accessors(chain = true)
    public static class PageCursor {
        private int total = 0;
        private int pageSize = 0;
        private int index = 0;
    }

}
