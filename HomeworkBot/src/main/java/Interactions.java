import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Interactions extends ListenerAdapter {

    // Create view slash command
    public void onReady(ReadyEvent event) {
        Main.jda.upsertCommand("view", "display personalized homework with its due dates to the user").queue();
    }

    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("view")) {
            // Set reply as ephemeral to not clog up server channels and because responses are personalized
            event.deferReply(true).queue();
            InteractionHook hook = event.getHook();
            hook.setEphemeral(true);
            // Check if banned
            if (Banned.isBanned(event.getUser().getId())) {
                hook.sendMessage("You are banned from using this bot. DM `" + Main.jda.getUserById(Main.SteveID).getAsTag() + "` if you want it repealed.").queue();
                return;
            }
            String output = "";
            for (String channelID : Channel.getChannels()) {
                Member m = Main.jda.getGuildById(Main.SeminoleID).getMember(event.getUser());
                Role r = Main.jda.getGuildById(Main.SeminoleID).getRoleById(Channel.getRoleIDfromChannel(channelID));
                if (m.getRoles().contains(r)) {
                    Message msg = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(channelID).retrieveMessageById(Messages.getPinnedId(channelID)).complete();
                    if (!msg.getContentRaw().contains("Currently, there is no homework at this time.")) {
                        output += "\n\n**__" + r.getName() + "__**\n" + msg.getContentRaw().substring(msg.getContentRaw().indexOf(">") + 1);
                    }
                }
            }
            if (output.equals("")) {
                hook.sendMessage("Currently, there is no homework at this time.").queue();
            } else {
                hook.sendMessage(output).queue();
            }
        }
    }
    
}
