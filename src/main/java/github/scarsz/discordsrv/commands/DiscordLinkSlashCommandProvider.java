/*
 * DiscordSRV - https://github.com/DiscordSRV/DiscordSRV
 *
 * Copyright (C) 2016 - 2024 Austin "Scarsz" Shapiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package github.scarsz.discordsrv.commands;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandPriority;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.objects.managers.link.LinkResult;
import github.scarsz.discordsrv.util.LangUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Collections;
import java.util.Set;

/**
 * Provides the built-in Discord slash command {@code /discord link <code>}.
 */
public class DiscordLinkSlashCommandProvider implements SlashCommandProvider {

    public static final DiscordLinkSlashCommandProvider INSTANCE = new DiscordLinkSlashCommandProvider();

    private PluginSlashCommand command;

    private DiscordLinkSlashCommandProvider() {}

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        if (command == null) {
            CommandData commandData = new CommandData("discord", "DiscordSRV commands")
                    .addSubcommands(new SubcommandData("link", "Link your Minecraft account to your Discord account")
                            .addOption(OptionType.STRING, "code", "The link code shown by /discord link in Minecraft", true));

            command = new PluginSlashCommand(DiscordSRV.getPlugin(), commandData, SlashCommandPriority.FIRST);
        }

        return Collections.singleton(command);
    }

    @SlashCommand(path = "discord/link", priority = SlashCommandPriority.FIRST, deferReply = true, deferEphemeral = true)
    public void link(SlashCommandEvent event) {
        TextChannel linkChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("link");
        if (linkChannel != null && !linkChannel.equals(event.getTextChannel())) {
            event.getHook().sendMessage(LangUtil.Message.LINK_COMMAND_WRONG_CHANNEL.toString()
                    .replace("%channel%", linkChannel.getAsMention())
            ).setEphemeral(true).queue();
            return;
        }

        AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
        if (manager == null) {
            event.getHook().sendMessage(LangUtil.Message.UNABLE_TO_LINK_ACCOUNTS_RIGHT_NOW.toString()).setEphemeral(true).queue();
            return;
        }

        OptionMapping codeOption = event.getOption("code");
        String code = codeOption != null ? codeOption.getAsString() : "";
        LinkResult result = manager.processLink(code, event.getUser().getId());

        Message linkedMessage = result.isSuccess() ? DiscordSRV.getPlugin().getAccountLinkedMessage(result) : null;
        if (linkedMessage != null) {
            if (linkChannel != null) linkChannel.sendMessage(linkedMessage).queue();
            event.getHook().sendMessage(linkedMessage).setEphemeral(true).queue();
            return;
        }

        String reply = result.getMessage();
        if (reply == null) {
            reply = LangUtil.Message.UNABLE_TO_LINK_ACCOUNTS_RIGHT_NOW.toString();
        }

        event.getHook().sendMessage(reply).setEphemeral(true).queue();
    }

}
