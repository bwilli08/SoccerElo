package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;
import club.elo.dao.EloDAO;
import club.elo.pojo.EloEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.sql.Date;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

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
        Date date;
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
            System.out.println("Choose the type of transaction");
            System.out.println("--type [help] to view list of commands");
            System.out.println("[Quit]: End program");

            command = input.nextLine().toLowerCase();
            switch (command) {
                case "help":
                    printCommands();
                case "teams":
                    List<String> teams = dao.getLocalTeams(statement);
                    for (int x = 0; x < teams.size(); x++) {
                        if (x % 10 == 0)
                            System.out.println();
                        System.out.print(String.format("%s ", teams.get(x)));
                    }
                    break;
                case "currentelo":
                    team = input.nextLine();
                    entry = dao.getLocalClubEntry(statement, team, Optional.of(1)).stream().findFirst();
                    if (entry.isPresent()) {
                        System.out.println(String.format("Current Elo of %s: %s", team, entry.get().getElo()));
                    } else {
                        System.out.println(String.format("Team %s not found in database.", team));
                    }
                    break;
                case "maxelo":
                    team = input.nextLine();
                    entry = dao.getMaxEloEntry(statement, team).stream().findFirst();
                    if (entry.isPresent()) {
                        System.out.println(String.format("Max Elo of %s: %s from %s to %s", team, entry.get().getElo(), entry.get().getStartDate(), entry.get().getEndDate()));
                    } else {
                        System.out.println(String.format("Team %s not found in database.", team));
                    }
                    break;
                case "best":
                    // Needs to be in YYYY-MM-DD format
                    try {
                        date = Date.valueOf(input.nextLine());
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
                    limit = Integer.valueOf(input.nextLine());
                    entries = dao.getBestAllTime(statement, limit);
                    entries.stream()
                            .sorted((e1, e2) -> e2.getElo().compareTo(e1.getElo()))
                            .forEach(e -> System.out.println(e));
                    break;
                case "quit":
                    status = "quit";
                    break;
                default:
                    System.out.println(String.format("[%s] is an unknown command", command));
                    break;
            }
            System.out.println();
        }
    }

    private void printCommands() {
        System.out.println("teams");
        System.out.println(" -- get names of every team in Europe");
        System.out.println("currentelo");
        System.out.println(" -- obtain current elo for team [Team Name]");
        System.out.println("maxelo");
        System.out.println(" -- obtain the highest ELO in history for team [Team Name]");
        System.out.println("best");
        System.out.println(" -- obtain the Team with the highest elo for specified date [date]");
        System.out.println("alltime");
        System.out.println(" -- obtain a list of the N best teams of all time");
    }
}
