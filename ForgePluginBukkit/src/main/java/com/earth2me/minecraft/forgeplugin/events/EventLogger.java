package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;


public final class EventLogger implements IEventHandler
{
	private final transient ForgePlugin plugin;

	public EventLogger(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void onEnabled()
	{
	}

	@Override
	public void onReload()
	{
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent e)
	{
		logCommand("Player", e.getPlayer().getName(), e.getMessage());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onServerCommand(final ServerCommandEvent e)
	{
		logCommand("Local", e.getSender().getName(), e.getCommand());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onRemoteServerCommand(final RemoteServerCommandEvent e)
	{
		logCommand("Remote", e.getSender().getName(), e.getCommand());
	}
	
	private void logCommand(final String type, final String sender, final String command)
	{
		plugin.getLogger().log(Level.INFO, String.format("[%s:%s] %s", type, sender, command));
	}
}
