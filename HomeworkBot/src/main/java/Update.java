import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Update extends ListenerAdapter {
    // Makes sure that the channels and roles are up to date
    public void onReady(ReadyEvent event) {
        for (String channelID : Channel.getChannels()) {
            if (Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).equals(null)) {
                updateChannels(channelID);
            }
        }
        for (String channelID : Channel.getChannels()) {
            if (Main.jda.getGuildById(Main.SeminoleID).getRoleById(Channel.getRoleIDfromChannel(channelID)).equals(null)) {
                updateChannels(channelID);
            }
        }
    }   
    // If a channel gets deleted which is also in the bot, remove it
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        for (String channelID : Channel.getChannels()) {
            if (channelID.equals(event.getChannel().getId())) {
                updateChannels(channelID);
            }
        }
    } 
    // If a role gets deleted which is also in the bot, remove it
    public void onRoleDelete(RoleDeleteEvent event) {
        for (String channelID : Channel.getChannels()) {
            // Remove it from file storing roles
            if (Channel.getRoleIDfromChannel(channelID).equals(event.getRole().getId())) {
                // need to update hw and unpin
                Message hw = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).retrieveMessageById(Messages.getPinnedId(channelID)).complete();
                hw.unpin().queue();
                MessageHistory mh = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).getHistoryAfter(hw, 1).complete();
                mh.getRetrievedHistory().get(0).delete().queue();
                Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).deleteMessageById(Messages.getPinnedId(channelID)).queue();
                updateChannels(channelID);
            }
        }
    }

    // Helper method that updates moderator file if a moderator's account gets deleted
    public static void updateMods(String userID) {
        try {
            File f = new File("moderators.txt");
            Scanner sc = new Scanner(f);
            String lines = "";
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equalsIgnoreCase("") && !line.substring(1).equals(userID)) {
                    lines += line + "\n";
                }
            }
            sc.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            bw.write(lines);
            bw.close();
        } catch (Exception e1) {};
    }
    public static void updateChannels(String channelID) {
        // update hw
        File f2 = new File("homework.txt");
        try {
            Scanner sc = new Scanner(f2);
            String newText = "";
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equals("") && !line.substring(0, line.indexOf(" ")).equals(channelID)) {
                    newText += line + "\n";
                }
            }
            sc.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f2, false));
            bw.write(newText);
            bw.close();
        } catch (Exception e) {e.printStackTrace();};
        // update file which stores channels
        try {
            File f = new File("channels.txt");
            Scanner sc = new Scanner(f);
            String lines = "";
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.equalsIgnoreCase("") && !line.substring(0, line.indexOf(" ")).equals(channelID)) {
                    lines += line + "\n";
                }
            }
            sc.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            bw.write(lines);
            bw.close();
        } catch (Exception e1) {};
    }
}
