package club.elo.converter;

import club.elo.pojo.EloChange;
import club.elo.pojo.EloEntry;

import java.sql.ResultSet;
import java.util.*;

/**
 * Converts a ResultSet to the entry POJO. Indexing starts at 1, and since we don't care about the 'entryId'
 * and it's set to autoincrement, we can just ignore it.
 */
public class ResultSetConverter {

    public List<String> convertToTeamNames(final ResultSet rs) {
        List<String> teams = new ArrayList<>();

        try {
            while (rs.next()) {
                teams.add(rs.getString(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure converting result set to team names."), e);
        }

        return teams;
    }

    public int convertToRank(final ResultSet rs) {
        int rank = -1;

        try {
            while (rs.next()) {
                rank = rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure converting result set to local entry."), e);
        }

        return rank;
    }

    public Set<EloChange> convertToEloChanges(final ResultSet rs) {
        Set<EloChange> changes = new HashSet<>();

        try {
            while (rs.next()) {
                int ndx = 1;
                EloChange.Builder builder = EloChange.builder();
                builder.name(rs.getString(ndx++));
                builder.date(rs.getDate(ndx++));
                builder.change(rs.getDouble(ndx));
                changes.add(builder.build());
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure converting result set to elo changes."), e);
        }

        return changes;
    }

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
