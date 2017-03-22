package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;
import club.elo.dao.EloDAO;
import club.elo.pojo.EloEntry;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class Main {

    // TODO: Create object that contains methods for various DB queries
    private static final String JDB_URL = "jdbc:mysql://localhost:3306/soccerElo?rewriteBatchedStatements=true";

    public static void main(String args[]) throws Exception {
        /* Change these according to your local database connection */
        String username = "usr";
        String password = "psswrd";

        System.out.println("Connecting to database...");

        try (Connection connection = DriverManager.getConnection(JDB_URL, username, password)) {
            System.out.println("Connected!");
            Statement statement = connection.createStatement();
            DBUpdater dbUpdater = new DBUpdater(new ClubEloConverter(), new ResultSetConverter());
            EloDAO dao = new EloDAO(new ResultSetConverter());

            try (Scanner input = new Scanner(System.in)) {
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
                    // TODO: Accurate help message
                    System.out.println("[Quit]: End program");

                    command = input.nextLine().toLowerCase();
                    switch (command) {
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
                }
            }
            // TODO: Move the DB update to a command you run through the above interface.
            // TODO: Have the db update run in the background instead of having the user wait for a few hours.

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
