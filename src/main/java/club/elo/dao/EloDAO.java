package club.elo.dao;

import club.elo.converter.ResultSetConverter;
import club.elo.pojo.EloChange;
import club.elo.pojo.EloEntry;
import lombok.AllArgsConstructor;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;

/**
 * Methods for accessing the local DB.
 */
@AllArgsConstructor
public class EloDAO {

    private final ResultSetConverter rsConverter;
    // used for query number 5
    private static final String TEAMS_ON_DATE = "select * from ClubEloEntry E where '%s' >= E.startDate and '%s' <= E.endDate";
    private static final String LOW_ELO = "select min(E.elo) as lowElo from (%s) as E order by E.elo desc limit 32";
    // used for query number 7
    private static final String TEAM_WORST = "SELECT * FROM ClubEloEntry WHERE name='%s' ORDER BY elo ASC LIMIT 1";
    // used for query number 8
    private static final String TEAMS_ERA = "select * from ClubEloEntry C where C.startDate >= '%s' and C.endDate <= '%s'";
    private static final String AVG_ERA = "select T.country, T.name, avg(T.elo) as avgElo from (%s) as T group by T.country, T.name;";
    private static final String MIN_AVG = "select min(A.avgElo) as minAvg from (%s) as A order by A.avgElo desc limit 20";
    private final static String DATE_QUERY = "SELECT * FROM ClubEloEntry WHERE startDate<='%s' AND endDate>='%s'";
    private final static String CLUB_DATE_QUERY = DATE_QUERY + " AND name='%s'";
    private static final String UPSET_QUERY = "SELECT E1.name, E2.endDate, (E1.elo - E2.elo) as eloChange\n" +
            "FROM (SELECT * FROM ClubEloEntry WHERE name='%s') as E1, (SELECT * FROM ClubEloEntry WHERE name='%s') as E2\n" +
            "WHERE E1.entryId!=E2.entryId AND DATEDIFF(e1.startDate, e2.endDate) = 1\n" +
            "ORDER BY eloChange ASC\n" +
            "LIMIT 1;";

    public Set<EloEntry> getEntriesForDate(final Statement statement, final Date date) {
        try {
            ResultSet rs = statement.executeQuery(String.format(DATE_QUERY, date, date));
            return rsConverter.convertToPOJO(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failure querying local database.", e);
        }
    }

    public Set<EloEntry> getTopEntriesForDate(final Statement statement, final Date date) {
        String sqlQuery = String.format(DATE_QUERY, date, date).concat(" ORDER BY elo DESC LIMIT 32");
        try {
            ResultSet rs = statement.executeQuery(sqlQuery);
            return rsConverter.convertToPOJO(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failure querying local database.", e);
        }
    }

    public int getTeamLowestRank(final Statement statement, final String clubName) {
        Optional<EloEntry> entry = getMinEloEntry(statement, clubName);

        if (entry.isPresent()) {
            String sqlQuery = String.format("SELECT (1 + count(*)) from ClubEloEntry where startDate <= '%s' and endDate >= '%s' and elo >= %s", entry.get().getStartDate(), entry.get().getEndDate(), entry.get().getElo());
            
            try {
                ResultSet rs = statement.executeQuery(sqlQuery);
                return rsConverter.convertToRank(rs);
            } catch (Exception e) {
                throw new RuntimeException("Failure querying local database.", e);
            }

        }

        return -1;
    }

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
            throw new RuntimeException(String.format("Failure querying local database for dates %s and %s.", date, secondDate), e);
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
        final String nextYear = Integer.toString(Integer.valueOf(year) + 1);
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
            throw new RuntimeException(String.format("Failure querying local database for year %s.", year), e);
        }
    }

    public Map<String, Double> getBestTeamsYearAndMonth(final Statement statement, final String year, final String month) {
        Map<String, Double> changeMap = new HashMap<>();
        final Date startDate = Date.valueOf(year + "-" + month + "-" + "01");
        int monthVal = Integer.parseInt(month);
        monthVal = (monthVal == 12) ? 1 : monthVal + 1;
        int yearVal = Integer.parseInt(year);
        yearVal = (monthVal == 1) ? yearVal + 1 : yearVal;

        final Date endDate = Date.valueOf(yearVal + "-" + monthVal + "-" + "01");

        for (String team : getLocalTeams(statement)) {
            changeMap.put(team, changeBetween(statement, team, startDate, endDate));
        }

        return changeMap;
    }

    public Map<String, Double> getBestTeamsYear(final Statement statement, final String year) {
        Map<String, Double> changeMap = new HashMap<>();
        final Date startDate = Date.valueOf(year + "-01-01");
        final String nextYear = Integer.toString(Integer.valueOf(year) + 1);
        final Date endDate = Date.valueOf(nextYear + "-01-01");

        for (String team : getLocalTeams(statement)) {
            changeMap.put(team, changeBetween(statement, team, startDate, endDate));
        }

        return changeMap;
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
