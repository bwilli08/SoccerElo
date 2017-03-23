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

    private static final String CLUB_QUERY =
            "SELECT *\n" +
            "FROM ClubEloEntry\n" +
            "WHERE name='%s'";
    private static final String ALL_TIME_QUERY =
            "SELECT name, MAX(elo) as maxElo\n" +
            "FROM ClubEloEntry\n" +
            "GROUP BY name\n" +
            "ORDER BY maxElo DESC\n" +
            "LIMIT %s";
    private static final String FIRST_ELO_IN_RANGE =
            "SELECT elo\n" +
            "FROM ClubEloEntry\n" +
            "WHERE name='%s' AND startDate<='%s' AND endDate>='%s'\n" +
            "ORDER BY endDate ASC\n" +
            "LIMIT 1";
    private static final String LAST_ELO_IN_RANGE =
            "SELECT elo\n" +
            "FROM ClubEloEntry\n" +
            "WHERE name='%s' AND startDate<='%s' AND endDate>='%s'\n" +
            "ORDER BY endDate DESC\n" +
            "LIMIT 1";
    private final static String DATE_QUERY =
            "SELECT *\n" +
            "FROM ClubEloEntry\n" +
            "WHERE startDate<='%s' AND endDate>='%s'";
    private static final String UPSET_QUERY =
            "SELECT E1.name, E2.endDate, (E1.elo - E2.elo) as eloChange\n" +
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
        final String firstEloQuery = String.format(FIRST_ELO_IN_RANGE, name, secondDate, date);
        final String lastEloQuery = String.format(LAST_ELO_IN_RANGE, name, secondDate, date);
        final String changeQuery = String.format("SELECT ((%s) - (%s))", firstEloQuery, lastEloQuery);

        try {
            ResultSet rs = statement.executeQuery(changeQuery);
            return rs.next() ? rs.getDouble(1) : Double.MAX_VALUE;
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

    public Map<String, Double> getBestAllTime(final Statement statement, final Integer limit) {
        String sqlQuery = String.format(ALL_TIME_QUERY, limit);
        try {
            ResultSet rs = statement.executeQuery(sqlQuery);
            return rsConverter.convertToEloMap(rs);
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
            ResultSet rs = statement.executeQuery(String.format(CLUB_QUERY + " ORDER BY elo DESC LIMIT 1", clubName));
            return rsConverter.convertToPOJO(rs).stream().findFirst();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for %s.", clubName), e);
        }
    }

    public Optional<EloEntry> getMinEloEntry(final Statement statement, final String clubName) {
        try {
            ResultSet rs = statement.executeQuery(String.format(CLUB_QUERY + " ORDER BY elo ASC LIMIT 1", clubName));
            return rsConverter.convertToPOJO(rs).stream().findFirst();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failure querying local database for %s.", clubName), e);
        }
    }

    public List<String> getLocalTeams(final Statement statement) {
        try {
            ResultSet rs = statement.executeQuery("SELECT DISTINCT name FROM ClubEloEntry GROUP BY name ORDER BY name ASC");
            return rsConverter.convertToTeamNames(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failure querying local database for local teams.", e);
        }
    }

    public Double yearDifference(final Statement statement, final String name, String year) {
        final Date startDate = Date.valueOf(year + "-01-01");
        final Date endDate = plusYear(startDate);

        return changeBetween(statement, name, startDate, endDate);
    }

    public Map<String, Double> getBestTeamsYearAndMonth(final Statement statement, final String year, final String month) {
        Map<String, Double> changeMap = new HashMap<>();
        final Date startDate = Date.valueOf(year + "-" + month + "-" + "01");
        final Date endDate = plusMonth(startDate);

        for (String team : getLocalTeams(statement)) {
            changeMap.put(team, changeBetween(statement, team, startDate, endDate));
        }

        return changeMap;
    }

    public Map<String, Double> getBestTeamsYear(final Statement statement, final String year) {
        Map<String, Double> changeMap = new HashMap<>();
        final Date startDate = Date.valueOf(year + "-01-01");
        final Date endDate = plusYear(startDate);

        for (String team : getLocalTeams(statement)) {
            changeMap.put(team, changeBetween(statement, team, startDate, endDate));
        }

        return changeMap;
    }

    public Set<EloEntry> getClubEntries(final Statement statement, final String clubName, final Optional<Integer> limit) {
        String sqlQuery = String.format(CLUB_QUERY + " ORDER BY startDate DESC", clubName);
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

    private Date plusYear(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 1);
        return new Date(calendar.getTime().getTime());
    }

    private Date plusMonth(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        return new Date(calendar.getTime().getTime());
    }
}
