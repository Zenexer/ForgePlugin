package com.earth2me.minecraft.forgeplugin;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;


public final class VaultPermissionHandler
{
	private final transient Server server;
	private transient Permission permission;

	public VaultPermissionHandler(final Server server)
	{
		this.server = server;
		
		RegisteredServiceProvider<Permission> permissionProvider = server.getServicesManager().getRegistration(Permission.class);
		if (permissionProvider == null)
		{
			permission = null;
		}
		else
		{
			permission = permissionProvider.getProvider();
		}
	}

	public boolean hasPermission(CommandSender sender, String node)
	{
		if (permission == null)
		{
			return sender.hasPermission(node);
		}
		else
		{
			return permission.has(sender, node);
		}
	}
}
