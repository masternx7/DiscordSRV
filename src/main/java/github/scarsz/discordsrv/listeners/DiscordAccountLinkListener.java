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

package github.scarsz.discordsrv.listeners;

import github.scarsz.discordsrv.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.events.DiscordPrivateMessageReceivedEvent;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.objects.managers.link.LinkResult;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DiscordAccountLinkListener extends ListenerAdapter {

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        // don't process messages sent by the bot
        if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) return;

        DiscordSRV.api.callEvent(new DiscordPrivateMessageReceivedEvent(event));

        // don't link accounts if config option is disabled
        if (!DiscordSRV.config().getBoolean("account-linking.use-pm")) return;

        LinkResult result = DiscordSRV.getPlugin().getAccountLinkManager().processLink(event.getMessage().getContentRaw(), event.getAuthor().getId());
        Message linkedMessage = result.isSuccess() ? DiscordSRV.getPlugin().getAccountLinkedMessage(result) : null;
        if (linkedMessage != null) {
            event.getMessage().reply(linkedMessage).queue();
        } else if (result.getMessage() != null) {
            event.getMessage().reply(result.getMessage()).queue();
        }
    }

    private final ErrorHandler ignoreFailedToDeleteMessage = new ErrorHandler()
            .ignore(ErrorResponse.UNKNOWN_MESSAGE)
            .ignore(ErrorResponse.MISSING_ACCESS);

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // don't process messages sent by bots
        if (event.getAuthor().isBot()) return;

        // if message is not in the link channel, don't process it
        TextChannel linkChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("link");
        if (!event.getChannel().equals(linkChannel)) return;

        Message receivedMessage = event.getMessage();
        AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
        if (manager == null) return;

        if (manager.getUuid(event.getAuthor().getId()) != null) {
            receivedMessage.delete().queue(null, ignoreFailedToDeleteMessage);
            return;
        }

        LinkResult result = manager.processLink(receivedMessage.getContentRaw(), event.getAuthor().getId());
        Message linkedMessage = result.isSuccess() ? DiscordSRV.getPlugin().getAccountLinkedMessage(result) : null;
        if (result.getMessage() != null || linkedMessage != null) {
            int deleteSeconds = DiscordSRV.config().getIntElse("account-linking.message-delete-seconds", 0);
            RestAction<Message> repliedMessage = linkedMessage != null
                    ? receivedMessage.reply(linkedMessage).delay(deleteSeconds, TimeUnit.SECONDS)
                    : receivedMessage.reply(result.getMessage()).delay(deleteSeconds, TimeUnit.SECONDS);

            repliedMessage.queue(replyMessage -> {
                // delete the message after a delay if the config option is set
                if (deleteSeconds > 0) {
                    replyMessage.delete().queue(null, ignoreFailedToDeleteMessage);
                    receivedMessage.delete().queue(null, ignoreFailedToDeleteMessage.handle(ErrorResponse.MISSING_PERMISSIONS,
                            e -> DiscordSRV.debug(Debug.ACCOUNT_LINKING, "Failed to delete " + receivedMessage.getAuthor() + "'s message in the link channel because of missing permissions.")));
                }
            });

        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();

        if (DiscordSRV.getPlugin().getAccountLinkManager() == null) {
            DiscordSRV.debug(Debug.ACCOUNT_LINKING, "AccountLinkManager is null, not processing join event");
            return;
        }

        // add linked role and nickname back to people when they rejoin the server
        UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getUser().getId());
        if (uuid != null) {
            Role roleToAdd = DiscordUtil.resolveRole(DiscordSRV.config().getString("account-linking.role-name-to-add-user-to"));
            if (roleToAdd == null || roleToAdd.getGuild().equals(member.getGuild())) {
                if (roleToAdd != null) DiscordUtil.addRoleToMember(member, roleToAdd);
                else DiscordSRV.debug(Debug.GROUP_SYNC, "Couldn't add user to null role");
            } else {
                DiscordSRV.debug(Debug.GROUP_SYNC, "Not adding role to member upon guild join due to the guild being different! (" + roleToAdd.getGuild() + " / " + member.getGuild() + ")");
            }

            if (DiscordSRV.config().getBoolean("nickname-synchronization.enabled")) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                DiscordSRV.getPlugin().getNicknameUpdater().setNickname(member, player);
            }
        }
    }

}
