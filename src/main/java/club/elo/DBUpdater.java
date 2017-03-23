package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;
import club.elo.dao.EloDAO;
import club.elo.pojo.EloEntry;
import club.elo.request.ClubRequest;
import club.elo.request.DateRequest;

import java.sql.Statement;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * Updates the local database to reflect the clubelo database.
 *
 * WARNING: This takes a very, very long time and should only be run once a day AT MOST.
 */
public class DBUpdater {

    private final EloDAO eloDAO;
    private final ClubRequest clubRequest;
    private final DateRequest dateRequest;

    public DBUpdater(final ClubEloConverter clubEloConverter, final ResultSetConverter resultSetConverter) {
        this.eloDAO = new EloDAO(resultSetConverter);
        this.clubRequest = new ClubRequest(clubEloConverter);
        this.dateRequest = new DateRequest(clubEloConverter);
    }

    public void update(final Statement statement) {
        dateRequest.get(Date.from(Instant.now())).stream().forEach(clubEntry -> {
            final String clubName = clubEntry.getClubName();

            Set<EloEntry> localEntries = eloDAO.getClubEntries(statement, clubName, Optional.empty());
            Set<EloEntry> clubEloEntries = clubRequest.get(clubName);

            clubEloEntries.stream()
                    .filter(e -> !localEntries.contains(e))
                    .forEach(e -> eloDAO.addToLocalDatabase(statement, e));
            eloDAO.executeBatch(statement);

            localEntries.stream()
                    .filter(e -> !clubEloEntries.contains(e))
                    .forEach(e -> eloDAO.removeFromLocalDatabase(statement, e));
            eloDAO.executeBatch(statement);
        });
    }
}