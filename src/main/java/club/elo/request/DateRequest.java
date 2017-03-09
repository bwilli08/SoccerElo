package club.elo.request;

import club.elo.converter.ClubEloConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date-specific requests.
 */
public class DateRequest extends ClubEloRequest<Date> {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    public DateRequest(final ClubEloConverter clubEloConverter) {
        super(clubEloConverter);
    }

    public String getUrlString(Date date) {
        return HTTP_HEADER + FORMATTER.format(date);
    }
}
