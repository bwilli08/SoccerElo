package club.elo;

import club.elo.converter.ClubEloConverter;
import club.elo.converter.ResultSetConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    // TODO: Create object that contains methods for various DB queries
    private static final boolean UPDATE = true;
    private static final String JDB_URL = "jdbc:mysql://localhost:3306/soccerElo?rewriteBatchedStatements=true";

    public static void main(String args[]) throws Exception {
        /* Change these according to your local database connection */
        String username = "usr";
        String password = "psswrd";
        String status = "continue";
        String command;
        Scanner input = new Scanner(System.in);

        System.out.println("Connecting to database...");

        try (Connection connection = DriverManager.getConnection(JDB_URL, username, password)) {
            System.out.println("Connected!");
            Statement statement = connection.createStatement();
            DBUpdater dbUpdater = new DBUpdater(new ClubEloConverter(), new ResultSetConverter());
            
            // Ask user if he/she wants to update database
            System.out.println("Would you like to update your local database to contain current statistics?");
            System.out.println("Type [yes] or [no]");
            command = input.nextLine().toLowerCase();

            if (command.equals("yes")) {
                System.out.println("Updating local database (this could be a while!)");
                dbUpdater.update(statement);
            }

            // TODO: Command line interface for user interaction and queries to the DB.
            while (status.equals("continue")) {
                System.out.print("Choose the type of transaction");
                System.out.println(" (Transaction type provided in brackets):");
                System.out.println("[Global]: Obtain info about many teams");
                System.out.println("[Specific]: Obtain info about one specific team");
                System.out.println("[Quit]: End program");

                command = input.nextLine().toLowerCase();
                switch(command) {
                    case "global":
                        break;
                    case "specific":
                        break;
                    case "quit":
                        status = "quit";
                        break;
                }
            }
            // TODO: Move the DB update to a command you run through the above interface.
            // TODO: Have the db update run in the background instead of having the user wait for a few hours.

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
