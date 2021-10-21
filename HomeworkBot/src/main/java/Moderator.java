import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

public class Moderator extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        
        // Check if bot
        if (event.getAuthor().isBot()) {
            return;
        }

        // Get message
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        // Uses DMs or slash commands to prevent clogging up of server channels
        if (args[0].toLowerCase().startsWith(Main.prefix) && !event.getGuild().getId().equals(Main.updateID)) {
            event.getChannel().sendMessage("Please use my commands in DMs or use slash commands (`/view`)!").queue();
            // Send user a help embed
            sendHelp(event.getAuthor().getId());
        }
    }

    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
    
        // Check if bot
        if (event.getAuthor().isBot()) {
            return;
        }

        // Get message
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        // If a user has moderator status, then send them a help embed if they desire
        if (event.getMessage().getContentRaw().toLowerCase().startsWith(Main.prefix + "mod help") && isMod(event.getAuthor().getId()) != 0) {
            Main.jda.openPrivateChannelById(event.getAuthor().getId()).queue(channel -> {
                EmbedBuilder modHelp = new EmbedBuilder();
                modHelp.setColor(Color.decode("#9B59B6"));
                modHelp.setTitle("Homework Bot");
                modHelp.setThumbnail(Main.jda.getSelfUser().getAvatarUrl());
                // Default mod commands
                modHelp.addField("Add HW: ", "`" + Main.prefix + "add ChannelID Date HW`", true);
                modHelp.addField("Remove HW: ", "`" + Main.prefix + "remove HW`", true);
                modHelp.addField("Edit Date: ", "`" + Main.prefix + "editDate newDate HW`", false);
                // Staff mod commands
                if (isMod(event.getAuthor().getId()) == 2) {
                    modHelp.addField("Add Channel: ", "`" + Main.prefix + "channel add ChannelID RoleID`", false);
                    modHelp.addField("Remove Channel: ", "`" + Main.prefix + "channel remove ChannelID`", false);
                    modHelp.addField("Deny/Remove Moderator: ", "`" + Main.prefix + "mod remove UserID`", false);
                    modHelp.addField("Add Moderator: ", "`" + Main.prefix + "mod add UserID`", false);
                }
                // Owner commands
                if (event.getAuthor().getId().equals(Main.SteveID)) {
                    modHelp.addField("Get Files: ", "`" + Main.prefix + "debug`", true);
                    modHelp.addField("Ban/Unban User", "`" + Main.prefix + "(un)ban UserID`", true);
                    modHelp.addField("List Banned Users", "`" + Main.prefix + "list banned`", true);
                }
                modHelp.setFooter("Made by " + Main.jda.getUserById(Main.SteveID).getAsTag(), Main.jda.getUserById(Main.SteveID).getEffectiveAvatarUrl());
                channel.sendMessageEmbeds(modHelp.build()).queue();
                modHelp.clear();
            });
        }

        // If a staff mod wants to recruit/remove a default moderator
        if (args[0].equalsIgnoreCase(Main.prefix + "mod") && isMod(event.getAuthor().getId()) == 2) {
            // Check syntax
            if (args.length < 3) {
                event.getChannel().sendMessage("Please follow the correct syntax!").queue();
                return;
            }
            // If the user wants to add a moderator
            if (args[1].equalsIgnoreCase("add")) {
                // Alert the added user that they now have moderator perms
                Main.jda.openPrivateChannelById(args[2]).queue(channel -> {
                    channel.sendMessage("<@" + args[2] + "> You were added as a mod by `" + event.getAuthor().getAsTag() + "` Use `" + Main.prefix + "mod help` to see what you can do with moderator.").queue(); 
                });
                // Add user to file storing moderators
                try {
                    File f = new File("moderators.txt");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                    bw.append("\nD" + args[2]);
                    bw.close();
                } catch (Exception e1) {}
                // send to other mods
                File f = new File("moderators.txt");
                try {
                    Scanner sc = new Scanner(f);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        // Ensure that account is not deleted
                        if (line.startsWith("S") && !line.substring(1).equals(event.getAuthor().getId())) {
                            if (Main.jda.getUserById(line.substring(1)).equals(null)) {
                                sc.close();
                                Update.updateMods(line.substring(1));
                                return;
                            }
                            Main.jda.openPrivateChannelById(line.substring(1)).queue(channel -> {
                                channel.sendMessage(event.getAuthor().getAsTag() + " added " + args[2] + " as a mod").queue();
                            });
                        }
                    }
                    sc.close();
                } catch (FileNotFoundException e1) {}
                // Notify user that moderator addition was successful
                event.getChannel().sendMessage("Added " + Main.jda.getUserById(args[2]).getAsTag() + " as a mod").queue();

            // If the user wants to remove a moderator
            } else if (args[1].equalsIgnoreCase("remove")) {
                // Alert the removed user that they now do not have perms
                Main.jda.openPrivateChannelById(args[2]).queue(channel -> {
                    channel.sendMessage("<@" + args[2] + "> Your mod was removed/denied by `" + event.getAuthor().getAsTag() + "`").queue(); 
                });
                // Remove user from file storing moderators
                try {
                    File f = new File("moderators.txt");
                    Scanner sc = new Scanner(f);
                    String lines = "";
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (!line.equalsIgnoreCase("") && !line.substring(1).equals(args[2])) {
                            // Ensure that the account is not deleted
                            if (Main.jda.getUserById(line.substring(1)).equals(null)) {
                                sc.close();
                                Update.updateMods(line.substring(1));
                                return;
                            }
                            lines += line + "\n";
                        }
                    }
                    sc.close();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
                    bw.write(lines);
                    bw.close();
                } catch (Exception e1) {};
                // send to other mods
                File f = new File("moderators.txt");
                try {
                Scanner sc = new Scanner(f);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.startsWith("S") && !line.substring(1).equals(event.getAuthor().getId())) {
                        // Ensure any of the moderators' accounts are not deleted
                        if (Main.jda.getUserById(line.substring(1)).equals(null)) {
                            sc.close();
                            Update.updateMods(line.substring(1));
                            return;
                        }
                        Main.jda.openPrivateChannelById(line.substring(1)).queue(channel -> {
                            channel.sendMessage(event.getAuthor().getAsTag() + " denied/removed " + Main.jda.getUserById(args[2]).getAsTag() + "'s mod.").queue();
                        });
                    }
                }
                sc.close();
                // Notify user of successful removal of moderator
                event.getChannel().sendMessage("Removed/Denied " + Main.jda.getUserById(args[2]).getAsTag() + "'s mod.").queue();
                } catch (FileNotFoundException e1) {}
            }
        }

        // If the user wants a help embed showing the moderator commands
        if (args[0].equalsIgnoreCase(Main.prefix + "help")) {
            sendHelp(event.getAuthor().getId());
        }

        // Lists mods, channels, and banned users
        if (args[0].toLowerCase().startsWith(Main.prefix + "list")) {
            // Check syntax
            if (args.length < 2) {
                event.getChannel().sendMessage("Please follow the syntax `" + Main.prefix + "list TYPE`").queue();
                return;
            }
            // If user wants a list of mods
            if (args[1].equalsIgnoreCase("mods")) {
                String output = "";
                File f = new File("moderators.txt");
                try {
                    Scanner sc = new Scanner(f);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (!line.equalsIgnoreCase("")) {
                            // Ensure that all of the mods' accounts are not deleted
                            User u = Main.jda.getUserById(line.substring(1));
                            if (u.equals(null)) {
                                sc.close();
                                Update.updateMods(line.substring(1));
                                return;
                            }
                            output += u.getAsTag() + "\n";
                        }
                    }
                    sc.close();
                } catch (Exception e) {e.printStackTrace();};
                // Send output
                event.getChannel().sendMessage("```" + output + "```").queue();
            // If the user wants to see the channels
            } else if (args[1].equalsIgnoreCase("channels")) {
                String output = "";
                File f = new File("channels.txt");
                try {
                    Scanner sc = new Scanner(f);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (!line.equalsIgnoreCase("")) {
                            TextChannel t = Main.jda.getGuildById(Main.SeminoleID).getTextChannelById(line.substring(0, line.indexOf(" ")));
                            // Join the channel name with the channel ID
                            output += "#" + t.getName() + " " + t.getId() + "\n";
                        }
                    }
                    sc.close();
                } catch (Exception e) {e.printStackTrace();};
                // Send output
                event.getChannel().sendMessage("```" + output + "```").queue();
            }
            // If the user wants to see banned users - only accessible to owner
            else if (args[1].equalsIgnoreCase("banned") && event.getAuthor().getId().equals(Main.SteveID)) {
                String output = "";
                File f = new File("banned.txt");
                try {
                    Scanner sc = new Scanner(f);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (!line.equalsIgnoreCase("")) {
                            output += Main.jda.getUserById(line).getAsTag() + "\n";
                        }
                    }
                    sc.close();
                } catch (Exception e) {e.printStackTrace();};
                // Send output
                event.getChannel().sendMessage("```" + output + "```").queue();
            }
        }

        // For debugging purposes - only accessible by owner
        if (args[0].toLowerCase().startsWith(Main.prefix + "debug") && event.getAuthor().getId().equals(Main.SteveID)) {
            File folder = new File(".");
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    try {
                        String output = "";
                        Scanner sc = new Scanner(file);
                        while (sc.hasNextLine()) {
                            String line = sc.nextLine();
                            if (!line.equals("")) {
                                output += line + "\n";
                            }
                        }
                        sc.close();
                        event.getChannel().sendMessage("```\n" + file.getName() + "\n\n" + output + "```").queue();
                    } catch (Exception e) {e.printStackTrace();};
                }
            }
        }
    }

    // Helper method that sends a general help embed (not moderator!)
    public void sendHelp(String userID) {
        Main.jda.openPrivateChannelById(userID).queue(channel -> {
            EmbedBuilder helpEmbed = new EmbedBuilder();
            helpEmbed.setColor(Color.decode("#9B59B6"));
            helpEmbed.setTitle("Homework Bot");
            helpEmbed.setThumbnail(Main.jda.getSelfUser().getAvatarUrl());
            helpEmbed.addField("Suggest HW: ", "`" + Main.prefix + "suggest ChannelID DATE HW`", false);
            helpEmbed.addField("Apply for mod: ", "`" + Main.prefix + "apply`", true);
            helpEmbed.addField("Personalized HW:", "`" + Main.prefix + "view`", true);
            helpEmbed.addField("List Channels:", "`" + Main.prefix + "list channels`", false);
            helpEmbed.addField("List Moderators:", "`" + Main.prefix + "list mods`", true);
            if (isMod(userID) != 0) {
                helpEmbed.addField("Moderator Help:", "`" + Main.prefix + "mod help`", true);
            }
            helpEmbed.setFooter("Made by " + Main.jda.getUserById(Main.SteveID).getAsTag(), Main.jda.getUserById(Main.SteveID).getEffectiveAvatarUrl());
            channel.sendMessageEmbeds(helpEmbed.build()).queue();
            helpEmbed.clear();
        });
    }
    

    // 0 = not mod, 1 = default mod, 2 = staff mod
    public static int isMod(String userID) {
        File f = new File("moderators.txt");
        Scanner sc;
        try {
            sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.substring(1).equals(userID)) {
                    sc.close();
                    if (line.startsWith("S")) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            }
            sc.close();
        } catch (Exception e) {e.printStackTrace();};
        return 0;
    }
}
