import java.io.File;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Messages extends ListenerAdapter
{
    // When the bot starts up
    public void onReady(ReadyEvent event) {
        updateMessages(false);
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // Check if update comes from updater server
        // Used for automation of updating - see https://replit.com/@SteveSajeev/HomeworkBotUpdater#main.py
        if (event.getGuild().getId().equals(Main.updateID) && event.getAuthor().getId().equals(Main.updaterID) && event.getMessage().getContentRaw().toLowerCase().startsWith(Main.prefix + "update")) {
            Messages.updateMessages(false);
        }
    }

    // Method to update messages in pinned
    public static void updateMessages(boolean edited) {
        for (String channelID : Channel.getChannels()) {
            TextChannel t = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID);
            // Checks if a pinned exists; if not, it creates one
            t.retrievePinnedMessages().queue(pinned -> {
                boolean pinnedExists = false;
                for (Message m : pinned) {
                    if (m.getAuthor().equals(Main.jda.getSelfUser())) {
                        if (!m.getId().equals(getPinnedId(channelID))) {
                            Channel.updateLine(channelID, channelID + " " + Channel.getRoleIDfromChannel(channelID) + " " + m.getId());
                        }
                        pinnedExists = true;
                    }
                }
                // Edits the pinned message with the formatted homework
                if (!pinnedExists) {
                    t.sendMessage("<@&" + Channel.getRoleIDfromChannel(channelID) + ">\n" + Homework.generateHomework(channelID, edited)).queue(message -> {
                        Channel.updateLine(channelID, channelID + " " + Channel.getRoleIDfromChannel(channelID) + " " + message.getId());
                        t.pinMessageById(message.getId()).queue();
                    });
                } else {
                    t.editMessageById(getPinnedId(channelID), "<@&" + Channel.getRoleIDfromChannel(channelID) + ">\n" + Homework.generateHomework(channelID, edited)).queue();
                }
            });
        }
    }

    // Helper method to return the message ID of the pinned message of the bot in a channel
    public static String getPinnedId(String channelID) {
        File f = new File("channels.txt");
        try {
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("") && line.substring(0, line.indexOf(" ")).equals(channelID)) {
                    sc.close();
                    return line.substring(line.lastIndexOf(" ") + 1);
                }
            }
            sc.close();
        } catch (Exception e) {e.printStackTrace();};
        return "";
    }
}
