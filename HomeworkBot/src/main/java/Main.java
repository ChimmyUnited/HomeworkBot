import javax.security.auth.login.LoginException;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {
    public static JDA jda;
    // Class constants
    static Dotenv dotenv = Dotenv.load();
    public static final String token = dotenv.get("TOKEN");
    public static final String prefix = dotenv.get("PREFIX");
    public static final String SeminoleID = dotenv.get("SEMINOLE_ID");
    public static final String SteveID = dotenv.get("STEVE_ID");
    public static final String reminderID = dotenv.get("REMINDER_ID");
    public static final String updateID = dotenv.get("UPDATE_ID");
    public static final String updaterID = dotenv.get("UPDATER_ID");
    public static final String logID = dotenv.get("LOG_ID");
    public static final String generalID = dotenv.get("GENERAL_ID");
    public static void main(String[] args) throws LoginException, InterruptedException {
        // Create bot
        jda = JDABuilder.createDefault(token).setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES).enableCache(CacheFlag.CLIENT_STATUS).build();
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.watching("new assignments. Use " + prefix + "help"));
        // Add listeners
        // Listener for updating pinneds and for updating homework deadlines
        jda.addEventListener(new Messages());
        // Listener for any commands that are moderator-related
        jda.addEventListener(new Moderator());
        // Listener for if a user wants to suggest homework or want to apply for moderator
        jda.addEventListener(new Suggest());
        // Listener for commands related to the channels
        jda.addEventListener(new Channel());
        // Listener for manipulating homework and other homework-related commands
        jda.addEventListener(new Homework());
        // Listener for slash commands
        jda.addEventListener(new Interactions());
        // Listener for banning users from using the bot
        jda.addEventListener(new Banned());
        // Listener to log events such as adding/deleting of homework
        jda.addEventListener(new Logger());
        // Listener to ensure files are up to date in case of user account deletion, channel deletion, or role deletion
        jda.addEventListener(new Update());
    }
}
