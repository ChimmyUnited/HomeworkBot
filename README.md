# HomeworkBot
A Java Discord Bot that reminds you of HW and other general announcements.
## Setup
In the [.env](https://github.com/ChimmyUnited/HomeworkBot/blob/main/.env) file, you will find
```shell
TOKEN=
PREFIX=
SEMINOLE_ID=
STEVE_ID=
REMINDER_ID=
UPDATE_ID=
UPDATER_ID=
LOG_ID=
GENERAL_ID=
```
- Token is the token of your discord bot
- Prefix is the prefix you want the bot to respond to
- SeminoleID is the id of the discord server
- SteveID is the id of the creator of the bot
- ReminderID is the id of the channel you want to send reminders for homework in
- UpdateID is the id of the server which you perform automatic updating (See [Automatic Updater](https://replit.com/@SteveSajeev/HomeworkBotUpdater))
- UpdaterID is the id of the bot which performs the automatic updating (See [Automatic Updater](https://replit.com/@SteveSajeev/HomeworkBotUpdater))
- LogID is the id of the logging channel
- GeneralID is the id of the announcements channel you want to send general announcements in
## Text Files
| Text File | Content |
| --------- | ------- |
| [banned.txt](https://github.com/ChimmyUnited/HomeworkBot/blob/main/banned.txt) | Contains the ids of banned users. Each line stores a UserID. |
| [channels.txt](https://github.com/ChimmyUnited/HomeworkBot/blob/main/channels.txt) | Contains the ids of the channels with their roles and the pinned bot message. Each line stores a ChannelID, RoleID, and PinnedMessageID. |
| [homework.txt](https://github.com/ChimmyUnited/HomeworkBot/blob/main/homework.txt) | Contains the homework with its due date. Each line stores the ChannelID, DueDate, HomeworkName, and TimesPinged. |
| [moderators.txt](https://github.com/ChimmyUnited/HomeworkBot/blob/main/moderators.txt) | Contains the ids of the moderators of the bot. Each line stores a prefix indicating type of mod (S means Staff, D means Default) and the UserID. |
## Contributing
If you want to help contribute to this project, please make a pull request and DM **Hackerman#2546**
