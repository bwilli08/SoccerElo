package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;
import club.elo.dao.EloDAO;
import club.elo.pojo.EloEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.sql.Date;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;

/**
 * Created by Brent Williams on 3/22/2017.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CLI {

    private final DBUpdater dbUpdater;
    private final EloDAO dao;

    public CLI(final ClubEloConverter clubEloConverter, final ResultSetConverter resultSetConverter) {
        this(new DBUpdater(clubEloConverter, resultSetConverter), new EloDAO(resultSetConverter));
    }

    public void handle(final Scanner input, final Statement statement) {
        String status = "continue";
        String command, team;
        Integer limit;
        Double change;
        Date date, secondDate;
        Set<EloEntry> entries;
        Optional<EloEntry> entry;

        // Ask user if he/she wants to update database
        System.out.println("Would you like to update your local database to contain current statistics?");
        System.out.println("Type [yes] or [no]");
        command = input.nextLine().toLowerCase();

        if (command.equals("yes")) {
            System.out.println("Updating local database (this could be a while! [Probably hours...])");
            dbUpdater.update(statement);
        }

        // TODO: Command line interface for user interaction and queries to the DB.
        while (status.equals("continue")) {
            System.out.println("Enter your transaction (type help for command info):");

            command = input.next().toLowerCase();
            switch (command) {
                case "help":
                    printCommands();
                    break;
                case "teams":
                    List<String> teams = dao.getLocalTeams(statement);
                    for (int x = 0; x < teams.size(); x++) {
                        if (x % 10 == 0)
                            System.out.println();
                        System.out.print(String.format("%s ", teams.get(x)));
                    }
                    break;
                case "currentelo":
                    team = input.next();
                    entry = dao.getClubEntries(statement, team, Optional.of(1)).stream().findFirst();
                    if (entry.isPresent()) {
                        System.out.println(String.format("Current Elo of %s: %s", team, entry.get().getElo()));
                    } else {
                        System.out.println(String.format("Team %s not found in database.", team));
                    }
                    break;
                case "maxelo":
                    team = input.next();
                    entry = dao.getMaxEloEntry(statement, team);
                    if (entry.isPresent()) {
                        System.out.println(String.format("Max Elo of %s: %s from %s to %s", team, entry.get().getElo(), entry.get().getStartDate(), entry.get().getEndDate()));
                    } else {
                        System.out.println(String.format("Team %s not found in database.", team));
                    }
                    break;
                case "minelo":
                    team = input.next();
                    entry = dao.getMinEloEntry(statement, team);
                    if (entry.isPresent()) {
                        System.out.println(String.format("Min Elo of %s: %s from %s to %s", team, entry.get().getElo(), entry.get().getStartDate(), entry.get().getEndDate()));
                    } else {
                        System.out.println(String.format("Team %s not found in database.", team));
                    }
                    break;
                case "best":
                    // Needs to be in YYYY-MM-DD format
                    try {
                        date = Date.valueOf(input.next());
                        entry = dao.getBestForDate(statement, date, Optional.of(1)).stream().findFirst();
                        if (entry.isPresent()) {
                            System.out.println(String.format("Max Elo on %s: %s with an elo of %s", date, entry.get().getClubName(), entry.get().getElo()));
                        } else {
                            throw new IllegalArgumentException("Error for date " + date);
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "alltime":
                    limit = input.nextInt();
                    entries = dao.getBestAllTime(statement, limit);
                    entries.stream()
                            .sorted((e1, e2) -> e2.getElo().compareTo(e1.getElo()))
                            .forEach(e -> System.out.println(e));
                    break;
                case "change":
                    team = input.next();
                    try {
                        date = Date.valueOf(input.next());
                        secondDate = Date.valueOf(input.next());
                        change = dao.changeBetween(statement, team, date, secondDate);

                        if (!change.equals(Double.MAX_VALUE)) {
                            System.out.println(String.format("%s changed %s from %s to %s.", team, change, date.toString(), secondDate.toString()));
                        } else {
                            System.out.println("Something bad happened.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "quit":
                    status = "quit";
                    break;
                default:
                    System.out.println(String.format("[%s] is an unknown command", command));
                    break;
            }
            input.nextLine();
            System.out.println();
        }
    }

    private void printCommands() {
        System.out.println("help");
        System.out.println(" -- List this message.");
        System.out.println("teams");
        System.out.println(" -- get names of every team in the database");
        System.out.println("currentelo [TeamName]");
        System.out.println(" -- obtain current elo for team [Team Name]");
        System.out.println("maxelo [TeamName]");
        System.out.println(" -- obtain the highest ELO in history for team [Team Name]");
        System.out.println("minelo [TeamName]");
        System.out.println(" -- obtain the lowest ELO in history for team [Team Name]");
        System.out.println("best [Date]");
        System.out.println(" -- obtain the Team with the highest elo for specified date [date]");
        System.out.println("alltime [NumTeams]");
        System.out.println(" -- obtain a list of the N best teams of all time");
        System.out.println("change [TeamName] [Date1] [Date2]");
        System.out.println(" -- return the net elo change for [TeamName] from [Date1] to [Date2]");
        System.out.println("\nAll Dates must be in SQL date format (YYYY-MM-DD)");
    }

    private Date plusMonth(final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        return new Date(calendar.getTime().getTime());
    }
}
