import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;
import java.time.Clock;
import java.time.LocalDateTime;

public class Logger extends ListenerAdapter {
    // Method that takes fields and creates an embed based on the fields provided
    public static void log(String event, User u, String roleAffected, String[]... fields) {
        // Create an embed to log events
        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setColor(Color.decode("#9B59B6"));
        logEmbed.setAuthor(u.getAsTag(), null, u.getEffectiveAvatarUrl());
        logEmbed.setDescription(Main.jda.getGuildById(Main.SeminoleID).getMember(u).getAsMention() + " " + event + ", affecting " + Main.jda.getGuildById(Main.SeminoleID).getRoleById(roleAffected).getAsMention());
        for (String[] field : fields) {
            logEmbed.addField("**" + field[0] + "**", field[1], false);
        }
        logEmbed.setFooter("Made by " + Main.jda.getUserById(Main.SteveID).getAsTag(), Main.jda.getUserById(Main.SteveID).getEffectiveAvatarUrl());
        logEmbed.setTimestamp(LocalDateTime.now(Clock.systemUTC()));
        Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(Main.logID).sendMessageEmbeds(logEmbed.build()).queue();
        logEmbed.clear();
    }
}
