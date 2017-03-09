package club.elo.converter;

import club.elo.pojo.EloEntry;

import java.sql.Date;

/**
 * Converts a ClubElo request to the local POJO.
 */
public class ClubEloConverter {

    private static final Integer NUM_ATTRIBUTES = 7;
    private static final String DELIMITER = ",";

    public EloEntry convertToPOJO(final String entry) {
        final String[] attributes = entry.split(DELIMITER, NUM_ATTRIBUTES);
        if (attributes.length != NUM_ATTRIBUTES) {
            throw new RuntimeException(String.format("Malformed ClubElo data. %s", entry));
        }

        final EloEntry.Builder builder = EloEntry.builder();

        builder.rank(attributes[0]);
        builder.clubName(attributes[1].replace(" ", ""));
        builder.country(attributes[2].replace(" ", ""));
        builder.levelOfPlay(Integer.valueOf(attributes[3]));
        builder.elo(Double.valueOf(attributes[4]));
        builder.startDate(Date.valueOf(attributes[5]));
        builder.endDate(Date.valueOf(attributes[6]));

        return builder.build();
    }
}
