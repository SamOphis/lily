package gg.samantha.lily.commands.list.reminder;

import gg.samantha.lily.commands.AbstractCommandBase;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.regex.Pattern;

public class SetReminderCommand extends AbstractCommandBase {
    private static final Pattern RELATIVE_PATTERN = Pattern.compile("(:?to)? (.+)? in (\\d+)([smhdwy])");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
            .withZone(ZoneId.from(ZoneOffset.UTC));
    private final ReminderUtility reminderUtility;

    public SetReminderCommand(@NotNull ReminderUtility reminderUtility) {
        this.reminderUtility = reminderUtility;
    }

    @NotNull
    @Override
    public List<String> prefixes() {
        return List.of("remind me", "can you remind me", "i want to be reminded");
    }

    @Override
    public void execute(@NotNull Message message, @NotNull String trimmedContent) {
        final var channel = message.getChannel();
        final var author = message.getAuthor();

        final var matcher = RELATIVE_PATTERN.matcher(trimmedContent);

        if (!matcher.find()) {
            message.reply("Whoops. Expected format: <reminder> in <number><s|m|h|d|w|y>").queue();
            return;
        }

        final var reminder = matcher.group(1);
        final var amount = Long.parseLong(matcher.group(2));
        final var offset = switch (matcher.group(3)) {
            case "s" -> amount;
            case "m" -> amount * 60;
            case "h" -> amount * 60 * 60;
            case "d" -> amount * 60 * 60 * 24;
            case "w" -> amount * 60 * 60 * 24 * 7;
            case "y" -> amount * 60 * 60 * 24 * 365;
            default -> throw new IllegalArgumentException("invalid time");
        };

        final var reminderTimestamp = Instant.now().plusSeconds(offset);
        reminderUtility.addReminderEntry(message, author, System.currentTimeMillis() / 1000 + offset, reminder);
        reminderUtility.scheduleReminder(channel, reminder, message.getId(), offset);
        message.replyFormat("Awesome. I'll remind you about this at **%s** UTC.", TIME_FORMATTER.format(reminderTimestamp))
                .mentionRepliedUser(false)
                .queue();
    }
}
