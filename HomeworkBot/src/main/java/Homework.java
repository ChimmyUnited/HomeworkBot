import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Period;
import java.time.ZoneId;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Homework extends ListenerAdapter {

    // Homework is stored in format CHANNELID DATE HOMEWORK TimesPinged

    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        // If I want to add homework - also check that user is moderator
        if (args[0].equalsIgnoreCase(Main.prefix + "add") && Moderator.isMod(event.getAuthor().getId()) != 0) {
            // Check syntax
            if (args.length < 4) {
                event.getChannel().sendMessage("Please follow the syntax `" + Main.prefix + "add ChannelID DATE HW`").queue();
                return;
            // Check if channel exists
            } else if (Suggest.isChannel(args[1]).equals("")) {
                event.getChannel().sendMessage(args[1] + " is not a valid channel ID. Use `" + Main.prefix + "list channels` to see all channels").queue();
                return;
            // Check if date is after current day
            } else if (!Suggest.isValidDate(args[2])) {
                event.getChannel().sendMessage(args[2] + " is not a valid date. Use the syntax **mm/dd/yyyy**").queue();
                return;
            }
            String topic = "";
            for (int i = 3; i < args.length; i++) {
                topic += args[i] + " ";
            }
            // Add homework to a file storing the homework
            try {
                File f = new File("homework.txt");
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                // Used to determine how many times it should ping - bot pings day before the homework assignment is due and day the homework assignment is due
                if (calcDays(args[2]) == 0) {
                    bw.append("\n" + args[1] + " " + args[2] + " " + topic.trim() + " 1");
                } else {
                    bw.append("\n" + args[1] + " " + args[2] + " " + topic.trim() + " 0");
                }
                bw.close();
            } catch (Exception e) {e.printStackTrace();};
            // Log that homework was added
            Logger.log("added hw", event.getAuthor(), Channel.getRoleIDfromChannel(args[1]), new String[]{"New", "HW for " + Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(args[1]).getAsMention() + " due `" + args[2] + "` about `" + topic.trim() + "`"});
            // Update the bot's messages with the homework
            Messages.updateMessages(false);
            // Notify user that homework creation was successful
            event.getChannel().sendMessage("Added hw in `" + args[1] + "` (#" + Suggest.isChannel(args[1]) + ") due `" + args[2] + "` about `" + topic.trim() + "`").queue();
        // If I want to remove homework - also check moderator
        } else if (args[0].equalsIgnoreCase(Main.prefix + "remove") && Moderator.isMod(event.getAuthor().getId()) != 0) {
            // Check syntax
            if (args.length < 2) {
                event.getChannel().sendMessage("Please follow the syntax `" + Main.prefix + "remove HW`").queue();
                return;
            }
            // Remove homework from file storing homework
            String hwNameInput = "";
            for (int i = 1; i < args.length; i++) {
                hwNameInput += args[i] + " ";
            }
            hwNameInput = hwNameInput.trim();
            String oldDate = "";
            String oldChannel = "";
            try {
                File f = new File("homework.txt");
                Scanner sc = new Scanner(f);
                String newText = "";
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.equals("")) {
                        int indexOfSecondSpace = line.indexOf(" ", line.indexOf(" ") + 1);
                        String hwNameFile = line.substring(indexOfSecondSpace + 1, line.lastIndexOf(" "));
                        if (!hwNameFile.equalsIgnoreCase(hwNameInput)) {
                            newText += line + "\n";
                        } else {
                            oldDate = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                            oldChannel = line.substring(0, line.indexOf(" "));
                        }
                    }
                }
                sc.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                bw.write(newText);
                bw.close();
            } catch (Exception e) {e.printStackTrace();};
            // Check if homework exists in the system
            if (oldDate.equals("")) {
                event.getChannel().sendMessage("There is no homework with the subject `" + hwNameInput + "`").queue();
                return;
            }
            // If reminder was sent out for the homework, revoke the reminder
            if (calcDays(oldDate) == 0 || calcDays(oldDate) == 1) {
                final String hwName = hwNameInput;
                Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.reminderID).getIterableHistory().stream().filter(msg -> msg.getContentRaw().contains(hwName)).findFirst().get().delete().queue();
            }
            // Log that homework was removed
            Logger.log("removed hw", event.getAuthor(), Channel.getRoleIDfromChannel(oldChannel), new String[]{"Removed", "HW for " + Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(oldChannel).getAsMention() + " due `" + oldDate + "` about `" + hwNameInput + "`"});
            // Update bot's messages with updated homework
            Messages.updateMessages(false);
            // Notify user that homework was removed
            event.getChannel().sendMessage("Removed homework").queue();
        // If I want to edit the date of a homework assignment
        } else if (args[0].equalsIgnoreCase(Main.prefix + "editDate") && Moderator.isMod(event.getAuthor().getId()) != 0) {
            // Check syntax
            if (args.length < 3) {
                event.getChannel().sendMessage("Please follow the syntax `" + Main.prefix + "editDate newDate HW`").queue();
                return;
            }
            // Check if date is after current date
            if (!Suggest.isValidDate(args[1])) {
                event.getChannel().sendMessage(args[1] + " is not a valid date. Use the syntax **mm/dd/yyyy**").queue();
                return;
            }
            // Add homework to file storing homework
            String hwNameInput = "";
            String oldDate = "";
            for (int i = 2; i < args.length; i++) {
                hwNameInput += args[i] + " ";
            }
            hwNameInput = hwNameInput.trim();
            boolean shouldAlert = false;
            String oldChannel = "";
            try {
                File f = new File("homework.txt");
                Scanner sc = new Scanner(f);
                String newText = "";
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.equals("")) {
                        int indexOfSecondSpace = line.indexOf(" ", line.indexOf(" ") + 1);
                        String hwNameFile = line.substring(indexOfSecondSpace + 1, line.lastIndexOf(" "));
                        if (!hwNameFile.equalsIgnoreCase(hwNameInput)) {
                            newText += line + "\n";
                        } else {
                            if (!line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1)).equalsIgnoreCase(args[1])) {
                                shouldAlert = true;
                                oldDate = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
                                oldChannel = line.substring(0, line.indexOf(" "));
                                newText += line.substring(0, line.indexOf(" ") + 1) + args[1] + " " + hwNameFile + " 0\n";
                            } else {
                                event.getChannel().sendMessage("`" + hwNameInput + "` already has a due date of " + args[1]).queue();
                                newText += line + "\n";
                            }
                        }
                    }
                }
                sc.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                bw.write(newText);
                bw.close();
            } catch (Exception e) {e.printStackTrace();};
            // Check if date changed
            if (shouldAlert) {
                Logger.log("edited date for `" + hwNameInput + "`", event.getAuthor(), Channel.getRoleIDfromChannel(oldChannel), new String[]{"Old Date", "Due `" + oldDate + "`"}, new String[]{"New Date", "Due `" + args[1] + "`"});
                event.getChannel().sendMessage("Edited `" + hwNameInput + "` to now have a due date of " + args[1]).queue();
                // Check if specialized announcement should be made regarding that date changed - ONLY OCCURS IF ALREADY PINGED
                if (calcDays(oldDate) == 0 || calcDays(oldDate) == 1) {
                    final String hwName = hwNameInput;
                    Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.reminderID).getIterableHistory().stream().filter(msg -> msg.getContentRaw().contains(hwName)).findFirst().get().delete().queue();
                }
                Messages.updateMessages(true);
            }
        }

        // If a user wants to view their homework
        if (args[0].equalsIgnoreCase(Main.prefix + "view")) {
            // Check if they are banned
            if (Banned.isBanned(event.getAuthor().getId())) {
                event.getChannel().sendMessage("You are banned from using this bot. DM `" + Main.jda.getUserById(Main.SteveID).getAsTag() + "` if you want it repealed.").queue();
                return;
            }
            // Combine homework into one message to prevent spam
            String breakApart = "";
            for (String channelID : Channel.getChannels()) {
                Member m = Main.jda.getGuildById(Main.SeminoleID).getMember(event.getAuthor());
                Role r = Main.jda.getGuildById(Main.SeminoleID).getRoleById(Channel.getRoleIDfromChannel(channelID));
                if (m.getRoles().contains(r)) {
                    // Loop through pinned messages and add content to message
                    Message msg = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).retrieveMessageById(Messages.getPinnedId(channelID)).complete();
                    if (!msg.getContentRaw().contains("Currently, there is no homework at this time.")) {
                        if ((breakApart + "\n\n**__" + r.getName() + "__**\n" + msg.getContentRaw().substring(msg.getContentRaw().indexOf(">") + 1)).length() <= 2000) {
                            breakApart += "\n\n**__" + r.getName() + "__**\n" + msg.getContentRaw().substring(msg.getContentRaw().indexOf(">") + 1);
                        } else {
                            event.getChannel().sendMessage(breakApart).queue();
                            breakApart = "";
                        }
                    }
                }
            }
            if (!breakApart.equals("")) {
                event.getChannel().sendMessage(breakApart).queue();
            }
        }
    }

    // Method that generates formatted homework
    public static String generateHomework(String channelID, boolean edited) {
        File f = new File("homework.txt");
        String output = "";
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("") && line.substring(0, line.indexOf(" ")).equals(channelID)) {
                    String outputDays = calcDays(line, channelID, edited);
                    if (!outputDays.equals("")) {
                        int indexOfSecondSpace = line.indexOf(" ", line.indexOf(" ") + 1);
                        output += "`" + line.substring(indexOfSecondSpace + 1, line.lastIndexOf(" ")) + "` **" + line.substring(line.indexOf(" ") + 1, indexOfSecondSpace) + "** *" + outputDays + "*\n\n";
                    }
                }
            }
            sc.close();
        } catch (Exception e) {e.printStackTrace();};
        if (output.equals("")) {
            if (channelID.equals(Main.generalID)) {
                return "Currently, there are no general announcements.";
            }
            return "Currently, there is no homework at this time.";
        }
        return output;
    }

    // Returns a formatted String containing the number of days between now and due date
    private static String calcDays(String line, String channelID, boolean edited) {
        String date = line.substring(line.indexOf(" ") + 1, line.indexOf(" ", line.indexOf(" ") + 1));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dtf = dtf.withZone(ZoneId.of("America/New_York"));
        try {
            LocalDate dateDue = LocalDate.parse(date, dtf);
            LocalDate now = LocalDate.now(Clock.system(ZoneId.of("America/New_York")));
            if (Period.between(now, dateDue).getDays() <= 1) {
                remind(line, Channel.getRoleIDfromChannel(channelID), Period.between(now, dateDue).getDays(), edited);
                if (Period.between(now, dateDue).getDays() == 1) {
                    return "[Tomorrow]";
                } else if (Period.between(now, dateDue).getDays() == 0) {
                    return "[Today]";
                }
                return "";
            } else {
                return "[" + Period.between(now, dateDue).getDays() + " days]";
            }
        } catch (Exception e) {e.printStackTrace();};
        return "";
    }

    // Determines an int of the number of days between now and due date
    public static int calcDays(String inputDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dtf = dtf.withZone(ZoneId.of("America/New_York"));
        try {
            LocalDate dateDue = LocalDate.parse(inputDate, dtf);
            LocalDate now = LocalDate.now(Clock.system(ZoneId.of("America/New_York")));
            return Period.between(now, dateDue).getDays();
        } catch (Exception e) {e.printStackTrace();};
        return -1;
    }

    // Method to send message in reminder channel that homework is due tomorrow/today
    public static void remind(String homework, String roleID, int numOfDays, boolean edited) {
        int indexOfSecondSpace = homework.indexOf(" ", homework.indexOf(" ") + 1);
        File f = new File("homework.txt");
        // If the homework's date is passed, remove it from system
        if (numOfDays <= -1) {
            try {
                Scanner sc = new Scanner(f);
                String newText = "";
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.equals("") && !line.equalsIgnoreCase(homework)) {
                        newText += line + "\n";
                    }
                }
                sc.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                bw.write(newText);
                bw.close();
            } catch (Exception e) {e.printStackTrace();};
        // Only ping if homework has not been pinged before
        // Determines this by storing a number alongside each homework indicidating the number of times it has been pinged, and looks to see if the number is != the appropriate number of times to ping
        } else if (Integer.parseInt(homework.substring(homework.lastIndexOf(" ") + 1)) != (2 - numOfDays)) {
            // Ping for today
            if (numOfDays == 0) {
                // Check to see if edited should be included
                if (edited) {
                    Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.reminderID).sendMessage("<@&" + roleID + "> EDIT: NEW DATE - `" + homework.substring(indexOfSecondSpace + 1, homework.lastIndexOf(" ")) + "` due **today!**").queue();
                } else {
                    Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.reminderID).sendMessage("<@&" + roleID + "> `" + homework.substring(indexOfSecondSpace + 1, homework.lastIndexOf(" ")) + "` due **today!**").queue();
                }
            // Ping for tomorrow
            } else {
                // Check to see if edited should be included
                if (edited) {
                    Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.reminderID).sendMessage("<@&" + roleID + "> EDIT: NEW DATE - `" + homework.substring(indexOfSecondSpace + 1, homework.lastIndexOf(" ")) + "` due **tomorrow!**").queue();
                } else {
                    Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.reminderID).sendMessage("<@&" + roleID + "> `" + homework.substring(indexOfSecondSpace + 1, homework.lastIndexOf(" ")) + "` due **tomorrow!**").queue();
                }
            }
            // Update with the new times pinged amount
            try {
                Scanner sc = new Scanner(f);
                String newText = "";
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.equals("")) {
                        if (!line.equalsIgnoreCase(homework)) {
                            newText += line + "\n";
                        } else {
                            newText += line.substring(0, line.lastIndexOf(" ") + 1) + (Integer.parseInt(homework.substring(homework.lastIndexOf(" ") + 1)) + 1) + "\n";
                        }
                    }
                }
                sc.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                bw.write(newText);
                bw.close();
            } catch (Exception e) {e.printStackTrace();};
        }
    }
}
