import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Channel extends ListenerAdapter
{
    // Information in channels.txt is stored in format ChannelID, RoleID, PinnedID

    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        // Looks for channel-related commands
        if (args[0].equalsIgnoreCase(Main.prefix + "channel") && Moderator.isMod(event.getAuthor().getId()) == 2) {
            // Checks the syntax
            if (args.length < 3) {
                event.getChannel().sendMessage("Please use the correct syntax!").queue();
                return;
            }
            // If I want to add a channel
            if (args[1].equalsIgnoreCase("add")) {
                // Check if the channel already exists
                if (!getChannels().contains(args[2])) {
                    // Check syntax
                    if (args.length < 4) {
                        event.getChannel().sendMessage("Please use the correct syntax!").queue();
                        return;
                    // Check if role provided is valid
                    } else if (Main.jda.getGuildById(Main.SeminoleID).getRoleById(args[3]) == null) {
                        event.getChannel().sendMessage("`" + args[3] + "` is not a role!").queue();
                        return;
                    }
                    else {
                        // Syntax is correct, add channel to file storing channels + role ids + pinned ids
                        File f = new File("channels.txt");
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                            bw.append("\n" + args[2] + " " + args[3]);
                            bw.close();
                        } catch (Exception e) {e.printStackTrace();};
                        // Update the bot's messages
                        Messages.updateMessages(false);
                        // Log it as an event
                        Logger.log("added channel", event.getAuthor(), args[3], new String[]{"New", Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(args[2]).getAsMention()});
                        // Notify of successful channel creation
                        event.getChannel().sendMessage("Added `" + args[2] + "` (#" + Suggest.isChannel(args[2]) + ") with roleID `" + args[3] + "` (" + Main.jda.getGuildById(Main.SeminoleID).getRoleById(args[3]).getName() + ")").queue();
                    }
                } else {
                    // Alert that the channel already exists
                    event.getChannel().sendMessage("`" + args[2] + "` (#" + Suggest.isChannel(args[2]) + ") is already in the system!").queue();
                    return;
                }
            // If I want to remove a channel
            } else if (args[1].equalsIgnoreCase("remove")) {
                // Check if the channel exists
                if (getChannels().contains(args[2])) {
                    String removeName = Suggest.isChannel(args[2]);
                    String channelMention = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(args[2]).getAsMention();
                    String removeRole = Channel.getRoleIDfromChannel(args[2]);
                    // need to update hw and unpin
                    Message hw = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(args[2]).retrieveMessageById(Messages.getPinnedId(args[2])).complete();
                    hw.unpin().queue();
                    MessageHistory mh = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(args[2]).getHistoryAfter(hw, 1).complete();
                    mh.getRetrievedHistory().get(0).delete().queue();
                    Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(args[2]).deleteMessageById(Messages.getPinnedId(args[2])).queue();
                    // update hw
                    File f2 = new File("homework.txt");
                    try {
                        Scanner sc = new Scanner(f2);
                        String newText = "";
                        while (sc.hasNextLine()) {
                            String line = sc.nextLine();
                            if (!line.equals("") && !line.substring(0, line.indexOf(" ")).equals(args[2])) {
                                newText += line + "\n";
                            }
                        }
                        sc.close();
                        BufferedWriter bw = new BufferedWriter(new FileWriter(f2, false));
                        bw.write(newText);
                        bw.close();
                    } catch (Exception e) {e.printStackTrace();};
                    // update channels
                    File f = new File("channels.txt");
                    try {
                        Scanner sc = new Scanner(f);
                        String newText = "";
                        while (sc.hasNextLine()) {
                            String line = sc.nextLine();
                            if (!line.equals("") && !line.substring(0, line.indexOf(" ")).equals(args[2])) {
                                newText += line + "\n";
                            }
                        }
                        sc.close();
                        BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                        bw.write(newText);
                        bw.close();
                    } catch (Exception e) {e.printStackTrace();};
                    Logger.log("removed channel", event.getAuthor(), removeRole, new String[]{"Removed", channelMention});
                    event.getChannel().sendMessage("Removed `" + args[2] + "` (#" + removeName + ")").queue();
                } else {
                    // Alert that the channel is not in the system
                    event.getChannel().sendMessage("`" + args[2] + "` is not currently in the system!").queue();
                    return;
                }
            }
        }
    }

    // Method that returns an ArrayList of all channels in the system
    public static ArrayList<String> getChannels() {
        File f = new File("channels.txt");
        ArrayList<String> output = new ArrayList<>();
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("")) {
                    output.add(line.substring(0, line.indexOf(" ")));
                }
            }
            sc.close();
        } catch (Exception e) {e.printStackTrace();};
        return output;
    }

    // Method that gets the roleID from channel ID
    public static String getRoleIDfromChannel(String channelID) {
        File f = new File("channels.txt");
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("") && line.substring(0, line.indexOf(" ")).equals(channelID)) {
                    sc.close();
                    if (line.lastIndexOf(" ") == line.indexOf(" ")) {
                        return line.substring(line.indexOf(" ") + 1);
                    } else {
                        return line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" "));
                    }
                }
            }
            sc.close();
        } catch (Exception e) {e.printStackTrace();};
        return null;
    }

    // Method that updates specified line
    public static void updateLine(String channelIdentifier, String newText) {
        File f = new File("channels.txt");
        try {
            Scanner sc = new Scanner(f);
            String newFile = "";
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("")) {
                    if (line.substring(0, line.indexOf(" ")).equals(channelIdentifier)) {
                        newFile += newText + "\n";
                    } else {
                        newFile += line + "\n";
                    }
                }
            }
            sc.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            bw.write(newFile);
            bw.close();
        } catch (Exception e) {e.printStackTrace();};
    }
}
