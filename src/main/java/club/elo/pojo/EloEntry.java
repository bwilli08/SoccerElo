package club.elo.pojo;

import lombok.*;

import java.util.Date;

/**
 * Created by Brent Williams on 2/27/2017.
 */
@Value
@Builder(builderClassName = "Builder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class EloEntry {
    private final String rank;
    private final String clubName;
    private final String country;
    private final Integer levelOfPlay;
    private final Double elo;
    private final Date startDate;
    private final Date endDate;
}

// class reserved for queries that return clubname and country
public class TeamAndCountry {
    private final String clubname;
    private final String country;
}

// class reserved for queries that return clubname and elo
public class TeamAndElo {
    private final String clubname;
    private final Double elo;
}
