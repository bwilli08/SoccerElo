package club.elo.request;

import club.elo.converter.ClubEloConverter;
import club.elo.pojo.EloEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface for requests to clubelo.com
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public abstract class ClubEloRequest<T> {

    @NonNull
    private final ClubEloConverter clubEloConverter;

    protected static final String HTTP_HEADER = "http://api.clubelo.com/";

    public abstract String getUrlString(T t);

    public Set<EloEntry> get(T t) {
        Set<EloEntry> entries = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getUrlString(t)).openStream()))) {
            String output;
            reader.readLine(); // We want to ignore the initial formatting explanation
            while ((output = reader.readLine()) != null) {
                if (!output.isEmpty()) {
                    entries.add(clubEloConverter.convertToPOJO(output));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }

        return entries;
    }
}
