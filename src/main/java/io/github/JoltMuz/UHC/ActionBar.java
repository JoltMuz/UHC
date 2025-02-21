package io.github.JoltMuz.UHC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class ActionBar {

	public static void sendToPlayer(Player player, String message)
    {
          
       PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
       chat.getBytes().write(0, (byte)2);
       chat.getChatComponents().write(0, WrappedChatComponent.fromText(message));
       ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat);

   }
		
	public static void sendToAll(String message) 
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			sendToPlayer(player, message);
		}
	}

}
