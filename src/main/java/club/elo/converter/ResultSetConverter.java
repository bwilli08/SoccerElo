package club.elo.converter;

import club.elo.pojo.EloEntry;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Converts a ResultSet to the entry POJO. Indexing starts at 1, and since we don't care about the 'entryId'
 * and it's set to autoincrement, we can just ignore it.
 */
public class ResultSetConverter {

    public Set<EloEntry> convertToPOJO(final ResultSet rs) {
        Set<EloEntry> entries = new HashSet<>();

        try {
            while (rs.next()) {
                int ndx = 2;
                EloEntry.Builder builder = EloEntry.builder();

                builder.rank(String.class.cast(rs.getObject(ndx++)));
                builder.clubName(String.class.cast(rs.getObject(ndx++)));
                builder.country(String.class.cast(rs.getObject(ndx++)));
                builder.levelOfPlay(Integer.class.cast(rs.getObject(ndx++)));
                builder.elo(Double.class.cast(rs.getObject(ndx++)));
                builder.startDate(Date.class.cast(rs.getObject(ndx++)));
                builder.endDate(Date.class.cast(rs.getObject(ndx)));

                entries.add(builder.build());
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure converting result set to local entry."), e);
        }

        return entries;
    }
}
