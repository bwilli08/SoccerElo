package club.elo.pojo;

import lombok.*;

import java.util.Date;

/**
 * Created by Brent Williams on 3/22/2017.
 */
@Value
@Builder(builderClassName = "Builder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class EloChange {
    private final String name;
    private final Date date;
    private final Double change;
}
