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
