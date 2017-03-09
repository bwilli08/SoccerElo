package club.elo.request;

import club.elo.converter.ClubEloConverter;

/**
 * Club-specific requests.
 */
public class ClubRequest extends ClubEloRequest<String> {

    public ClubRequest(final ClubEloConverter clubEloConverter) {
        super(clubEloConverter);
    }

    public String getUrlString(String clubName) {
        return HTTP_HEADER + clubName.replace(" ", "");
    }
}
