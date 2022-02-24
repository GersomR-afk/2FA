package com.lielamar.auth.velocity.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.lielamar.auth.shared.handlers.AuthHandler;
import com.lielamar.auth.shared.handlers.PluginMessagingHandler;
import com.lielamar.auth.velocity.TwoFactorAuthentication;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import java.io.*;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class OnPluginMessage extends PluginMessagingHandler {

    private final TwoFactorAuthentication main;

    public OnPluginMessage(TwoFactorAuthentication main) {
        this.main = main;
    }

    @Subscribe
    public void onQueryReceive(PluginMessageEvent event) {
        // If the Channel name is not the 2FA's Channel name we want to return
        if(event.getIdentifier() != main.getOUTGOING()) return;

        ByteArrayDataInput msg = ByteStreams.newDataInput(event.getData());
        String subChannel = msg.readUTF();

        // If the SubChannel name is the 2FA's SubChannel name
        if(subChannel.equals(super.subChannelName)) {
            UUID messageUUID = UUID.fromString(msg.readUTF());
            UUID playerUUID = UUID.fromString(msg.readUTF());

            Optional<Player> optionalPlayer = main.getProxy().getPlayer(playerUUID);
            if(optionalPlayer.isPresent()) {
                Player player = optionalPlayer.get();

                short bodyLength = msg.readShort();
                byte[] msgBody = new byte[bodyLength];
                msg.readFully(msgBody);

                DataInputStream msgBodyData = new DataInputStream(new ByteArrayInputStream(msgBody));
                try {
                    MessageAction action = MessageAction.valueOf(msgBodyData.readUTF());

                    if(action == MessageAction.GET_STATE) {
                        AuthHandler.AuthState defaultState = AuthHandler.AuthState.valueOf(msgBodyData.readUTF());

                        if(main.getAuthHandler().getAuthState(player.getUniqueId()) == null)
                            main.getAuthHandler().changeState(player.getUniqueId(), defaultState);
                    } else if(action == MessageAction.SET_STATE) {
                        AuthHandler.AuthState state = AuthHandler.AuthState.valueOf(msgBodyData.readUTF());

                        main.getAuthHandler().changeState(player.getUniqueId(), state);
                    }

                    sendResponse(messageUUID, player, action);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends back a response from BungeeCord to spigot
     *
     * @param messageUUID   UUID of the message
     * @param player        Player to send the response of
     * @param action        The action of the message (isAuthenticated/setAuthenticated)
     */
    public void sendResponse(UUID messageUUID, Player player, MessageAction action) {
        AuthHandler.AuthState state = main.getAuthHandler().getAuthState(player.getUniqueId());

        ByteArrayDataOutput response = ByteStreams.newDataOutput();
        response.writeUTF(super.subChannelName);
        response.writeUTF(messageUUID.toString());
        response.writeUTF(player.getUniqueId().toString());

        ByteArrayOutputStream msgBody = new ByteArrayOutputStream();
        DataOutputStream msgBodyData = new DataOutputStream(msgBody);
        try {
            msgBodyData.writeUTF(action.name());
            msgBodyData.writeUTF(state != null ? state.name() : "null");
        } catch(IOException exception) {
            exception.printStackTrace();
        }

        response.writeShort(msgBody.toByteArray().length);
        response.write(msgBody.toByteArray());

        Optional<ServerConnection> optionalServer = player.getCurrentServer();
        optionalServer.ifPresent(serverConnection -> serverConnection.sendPluginMessage(main.getOUTGOING(), response.toByteArray()));
    }
}