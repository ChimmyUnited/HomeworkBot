import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class Banned extends ListenerAdapter {
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        // If I want to ban a user
        if (event.getMessage().getContentRaw().toLowerCase().startsWith(Main.prefix + "ban") && event.getAuthor().getId().equals(Main.SteveID)) {
            String[] args = event.getMessage().getContentRaw().split("\\s+");

            // Checks for correct syntax
            if (args.length < 2) {
                event.getChannel().sendMessage("Please use the correct syntax, `" + Main.prefix + "ban UserID`").queue();
                return;
            }

            // Checks if user is already banned
            if (isBanned(args[1])) {
                event.getChannel().sendMessage("`" + Main.jda.getUserById(args[1]).getAsTag() + "` is already banned!").queue();
                return;
            }

            // Adds user to a text file that stores all banned users
            File f = new File("banned.txt");
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                bw.append("\n" + args[1]);
                bw.close();
            } catch (Exception e) {e.printStackTrace();};

            // Alerts me that I have successfully banned the user
            event.getChannel().sendMessage("Banned " + Main.jda.getUserById(args[1]).getAsTag() + " from using this bot").queue();
        }

        // If I want to unban a user
        if (event.getMessage().getContentRaw().toLowerCase().startsWith(Main.prefix + "unban") && event.getAuthor().getId().equals(Main.SteveID)) {
            String[] args = event.getMessage().getContentRaw().split("\\s+");

            // Check the syntax
            if (args.length < 2) {
                event.getChannel().sendMessage("Please use the correct syntax, `" + Main.prefix + "unban UserID`").queue();
                return;
            }

            // Check if user is already unbanned
            if (!isBanned(args[1])) {
                event.getChannel().sendMessage("`" + Main.jda.getUserById(args[1]).getAsTag() + "` is not banned!").queue();
                return;
            }

            // Removes user from text file storing banned members
            try {
                File f = new File("banned.txt");
                Scanner sc = new Scanner(f);
                String lines = "";
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.equalsIgnoreCase("") && !line.equals(args[1])) {
                        lines += line + "\n";
                    }
                }
                sc.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                bw.write(lines);
                bw.close();
            } catch (Exception e1) {};

            // Notifies me that I have successfully unbanned them
            event.getChannel().sendMessage("Unbanned " + Main.jda.getUserById(args[1]).getAsTag() + " from using this bot").queue();
        }
    }

    // Helper method to check if a user is banned
    public static boolean isBanned(String userID) {
        File f = new File("banned.txt");
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                if (sc.nextLine().equals(userID)) {
                    sc.close();
                    return true;
                }
            }
            sc.close();
        } catch (Exception e) {e.printStackTrace();};
        return false;
    }
}
