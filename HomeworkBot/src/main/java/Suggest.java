import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Suggest extends ListenerAdapter {
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        // If the user wants to suggest homework to be added to the bot
        if (args[0].equalsIgnoreCase(Main.prefix + "suggest")) {
            // Check syntax
            if (args.length < 4) {
                event.getChannel().sendMessage("Please follow the syntax `" + Main.prefix + "suggest ChannelID DATE HW`").queue();
                return;
            // Check if channel is valid
            } else if (isChannel(args[1]).equals("")) {
                event.getChannel().sendMessage(args[1] + " is not a valid channel ID. Use `" + Main.prefix + "list channels` to see all channels").queue();
                return;
            // Check if due date is after current date
            } else if (!isValidDate(args[2])) {
                event.getChannel().sendMessage(args[2] + " is not a valid date.").queue();
                return;
            }
            // Send suggestion to all of the moderators
            File f = new File("moderators.txt");
            String topic = "";
            for (int i = 3; i < args.length; i++) {
                topic += args[i] + " ";
            }
            final String hwName = topic;
            try {
                Scanner sc = new Scanner(f);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    Main.jda.openPrivateChannelById(line.substring(1)).queue(channel -> {
                        channel.sendMessage("<@" + line.substring(1) + "> ** " + event.getAuthor().getAsTag() + "** suggested to add hw in `" + args[1] + " (#" + isChannel(args[1]) + ")` due `" + args[2] + "` about `" + hwName.trim() + "`").queue();
                    });
                }
                sc.close();
            } catch (FileNotFoundException e1) {}
            // Notify user of successful suggestion
            event.getChannel().sendMessage("Suggested to add hw in `" + args[1] + " (#" + isChannel(args[1]) + ")` due `" + args[2] + "` about `" + hwName.trim() + "`").queue();
        }

        // If the user wants to apply for moderator
        if (args[0].equalsIgnoreCase(Main.prefix + "apply")) {
            // Send an application request to each of the staff moderators
            File f = new File("moderators.txt");
            try {
                Scanner sc = new Scanner(f);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.startsWith("S")) {
                        Main.jda.openPrivateChannelById(line.substring(1)).queue(channel -> {
                            channel.sendMessage("<@" + line.substring(1) + "> **" + event.getAuthor().getAsTag() + "** applied for moderator").queue();
                        });
                    }
                }
                sc.close();
            } catch (FileNotFoundException e) {e.printStackTrace();}
        }
    }

    // Helper method to determine if the channel exists in the bot
    public static String isChannel(String channelID) {
        File f = new File("channels.txt");
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("") && channelID.equalsIgnoreCase(line.substring(0, line.indexOf(" ")))) {
                    sc.close();
                    return Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).getName();
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {}
        return "";
    }

    // Helper method to determine if the due date is after the current date
    public static boolean isValidDate(String input) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dateFormat = dateFormat.withZone(ZoneId.of("America/New_York"));
        try {
            LocalDate convert = LocalDate.parse(input, dateFormat);
            LocalDate nowDate = LocalDate.now(Clock.system(ZoneId.of("America/New_York")));
            if (convert.isAfter(nowDate.minusDays(1))) {
                return true;
            } else {
                return false;
            }
        } catch (DateTimeParseException pe) {
            return false;
        }
    }
    
}