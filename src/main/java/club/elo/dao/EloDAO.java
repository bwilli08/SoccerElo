package club.elo.dao;

import club.elo.converter.ResultSetConverter;
import club.elo.pojo.EloChange;
import club.elo.pojo.EloEntry;
import lombok.AllArgsConstructor;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Methods for accessing the local DB.
 */
@AllArgsConstructor
public class EloDAO {

    private final ResultSetConverter rsConverter;

    private final static String DATE_QUERY = "SELECT * FROM ClubEloEntry WHERE startDate<='%s' AND endDate>='%s'";
    private final static String CLUB_DATE_QUERY = DATE_QUERY + " AND name='%s'";
    private static final String UPSET_QUERY = "SELECT E1.name, E2.endDate, (E1.elo - E2.elo) as eloChange\n" +
            "FROM (SELECT * FROM ClubEloEntry WHERE name='%s') as E1, (SELECT * FROM ClubEloEntry WHERE name='%s') as E2\n" +
            "WHERE E1.entryId!=E2.entryId AND DATEDIFF(e1.startDate, e2.endDate) = 1\n" +
            "ORDER BY eloChange ASC\n" +
            "LIMIT 1;";

    public Double changeBetween(final Statement statement, final String name, final Date date, final Date secondDate) {
        final String firstDateQuery = String.format(CLUB_DATE_QUERY + " ORDER BY endDate ASC LIMIT 1", secondDate, date, name);
        final String lastDateQuery = String.format(CLUB_DATE_QUERY + " ORDER BY endDate DESC LIMIT 1", secondDate, date, name);

        try {
            final Optional<EloEntry> first = rsConverter.convertToPOJO(statement.executeQuery(firstDateQuery)).stream().findFirst();
            final Optional<EloEntry> last = rsConverter.convertToPOJO(statement.executeQuery(lastDateQuery)).stream().findFirst();

            if (first.isPresent() && last.isPresent()) {
                return first.get().getElo() - last.get().getElo();
            }
            return Double.MAX_VALUE;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for date %s.", date), e);
        }
    }

    public Set<EloChange> getBiggestUpset(final Statement statement, final String clubName) {
        try {
            ResultSet rs = statement.executeQuery(String.format(UPSET_QUERY, clubName, clubName));
            return rsConverter.convertToEloChanges(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failure querying local database.", e);
        }
    }

    public Set<EloEntry> getBestAllTime(final Statement statement, final Integer limit) {
        String sqlQuery = String.format("SELECT * FROM ClubEloEntry ORDER BY elo DESC LIMIT %d", limit);
        try {
            ResultSet rs = statement.executeQuery(sqlQuery);
            return rsConverter.convertToPOJO(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failure querying local database.", e);
        }
    }

    public Set<EloEntry> getBestForDate(final Statement statement, final Date date, final Optional<Integer> limit) {
        String sqlQuery = String.format(DATE_QUERY + " ORDER BY elo DESC", date, date);
        if (limit.isPresent()) {
            sqlQuery = sqlQuery.concat(String.format(" LIMIT %d", limit.get()));
        }
        try {
            ResultSet rs = statement.executeQuery(sqlQuery);
            return rsConverter.convertToPOJO(rs);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for date %s.", date), e);
        }
    }

    public Optional<EloEntry> getMaxEloEntry(final Statement statement, final String clubName) {
        try {
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM ClubEloEntry WHERE name='%s' ORDER BY elo DESC LIMIT 1", clubName));
            return rsConverter.convertToPOJO(rs).stream().findFirst();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for %s.", clubName), e);
        }
    }

    public Optional<EloEntry> getMinEloEntry(final Statement statement, final String clubName) {
        try {
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM ClubEloEntry WHERE name='%s' ORDER BY elo ASC LIMIT 1", clubName));
            return rsConverter.convertToPOJO(rs).stream().findFirst();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for %s.", clubName), e);
        }
    }

    public List<String> getLocalTeams(final Statement statement) {
        try {
            ResultSet rs = statement.executeQuery("SELECT DISTINCT name FROM ClubEloEntry ORDER BY name ASC");
            return rsConverter.convertToTeamNames(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failure querying local database for local teams.", e);
        }
    }

    public Double yearDifference(final Statement statement, final String name, String year) {
        final String startDate = year + "-01-01";
        final String nextYear = Integer.toString(Integer.toInt(year) + 1);
        final String endDate = nextYear + "-01-01";

        final String firstDateQuery = String.format(CLUB_DATE_QUERY + " ORDER BY endDate ASC LIMIT 1", endDate, startDate, name);
        final String lastDateQuery = String.format(CLUB_DATE_QUERY + " ORDER BY endDate DESC LIMIT 1", endDate, startDate, name);

        try {
            final Optional<EloEntry> first = rsConverter.convertToPOJO(statement.executeQuery(firstDateQuery)).stream().findFirst();
            final Optional<EloEntry> last = rsConverter.convertToPOJO(statement.executeQuery(lastDateQuery)).stream().findFirst();

            if (first.isPresent() && last.isPresent()) {
                return first.get().getElo() - last.get().getElo();
            }
            return Double.MAX_VALUE;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for date %s.", date), e);
        }
    }

    //INCOMPLETE
    public List<String> getBestTeamsYearAndMonth(final Statement statement, final String year, final String month) {
        final String startDate = year + "-" + month + "-" + "01";
        int monthVal = Integer.parseInt(month);
        monthVal = (monthVal == 12) ? monthVal = 0 : monthVal = monthVal + 1;

        final String endDate = year + "-" + Integer.toString(month) + "-" + "01";
        
        //TODO 
        //String sqlQuery = String.format(CLUB_DATE_QUERY)
    }

    //INCOMPLETE
    public List<String> getBestTeamsYear(final Statement statement, final String year) {
        final String startDate = year + "-01-01";
        final String nextYear = Integer.toString(Integer.toInt(year) + 1);
        final String endDate = nextYear + "-01-01";

        //TODO
    }

    public Set<EloEntry> getClubEntries(final Statement statement, final String clubName, final Optional<Integer> limit) {
        String sqlQuery = String.format("SELECT * FROM ClubEloEntry WHERE name='%s' ORDER BY startDate DESC", clubName);
        if (limit.isPresent()) {
            sqlQuery = sqlQuery.concat(String.format(" LIMIT %d", limit.get()));
        }
        try {
            ResultSet rs = statement.executeQuery(sqlQuery);
            return rsConverter.convertToPOJO(rs);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for %s.", clubName), e);
        }
    }

    public void addToLocalDatabase(final Statement statement, final EloEntry entry) {
        try {
            statement.addBatch(String.format("INSERT INTO ClubEloEntry (rank, name, country, level, elo, startDate," +
                            "endDate) VALUES ('%s', '%s', '%s', %s, %s, '%s', '%s')", entry.getRank(), entry.getClubName(),
                    entry.getCountry(), entry.getLevelOfPlay(), entry.getElo(), entry.getStartDate(),
                    entry.getEndDate()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure adding %s entry to database.", entry), e);
        }
    }

    public void removeFromLocalDatabase(final Statement statement, final EloEntry entry) {
        try {
            System.out.println("Removing " + entry);
            statement.addBatch(String.format("DELETE FROM ClubEloEntry WHERE name='%s' AND startDate='%s' AND " +
                            "endDate='%s'", entry.getClubName(), entry.getStartDate(), entry.getEndDate()));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure adding %s entry to database.", entry), e);
        }
    }

    public void executeBatch(final Statement statement) {
        try {
            statement.executeLargeBatch();
        } catch (Exception e) {
            throw new RuntimeException("Batch execution failed.", e);
        }
    }
}
