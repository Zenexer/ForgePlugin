package com.earth2me.minecraft.forgeplugin;

import com.earth2me.minecraft.forgeplugin.commands.ChunkCleanupCommand;
import com.earth2me.minecraft.forgeplugin.commands.ForgePluginCommand;
import com.earth2me.minecraft.forgeplugin.commands.IsPersistentCommand;
import com.earth2me.minecraft.forgeplugin.events.*;
import com.earth2me.minecraft.forgeplugin.patch.IPatch;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class ForgePlugin extends JavaPlugin
{
	@Getter
	private transient ItemMonitor itemMonitor;
	@Getter
	private transient ChunkManager chunkManager;
	@Getter
	private transient EventLogger eventLogger;
	@Getter
	private transient EntityOverflow entityOverflow;
	@Getter
	private transient VaultPermissionHandler vault;
	private transient Set<IEventHandler> eventHandlers;
	private transient Set<IPatch> patches;
	private transient Set<IComponent> components;

	@Override
	public void onEnable()
	{
		eventHandlers = Sets.newHashSet(new IEventHandler[]
		{
			eventLogger = new EventLogger(this),
			itemMonitor = new ItemMonitor(this),
			entityOverflow = new EntityOverflow(this),
			chunkManager = new ChunkManager(this),
		});

		patches = Sets.newHashSet(new IPatch[]
		{
		});

		components = new HashSet<>();
		components.addAll(eventHandlers);
		components.addAll(patches);

		reload();

		final PluginManager pluginManager = getServer().getPluginManager();
		for (final IEventHandler eventHandler : eventHandlers)
		{
			try
			{
				pluginManager.registerEvents(eventHandler, this);
			}
			catch (final Throwable ex)
			{
				getLogger().log(Level.SEVERE, "Unable to register an event handler as a listener with Bukkit.", ex);
			}
		}

		getCommand("forgeplugin").setExecutor(new ForgePluginCommand(this));
		getCommand("chunkcleanup").setExecutor(new ChunkCleanupCommand(this));
		getCommand("ispersistent").setExecutor(new IsPersistentCommand(this));

		super.onEnable();

		for (final IComponent component : components)
		{
			try
			{
				component.onEnabled();
			}
			catch (final Throwable ex)
			{
				getLogger().log(Level.SEVERE, "Unable to cleanly enable a component.", ex);
			}
		}
	}

	public void setConfigOptions(final Configuration config)
	{
		if (config == null)
		{
			return;
		}

		config.options().pathSeparator('/');
	}

	public FileConfiguration loadConfig(final String yamlFileName)
	{
		if (yamlFileName == null)
		{
			return null;
		}

		final File configFile = new File(getDataFolder(), yamlFileName);
		final URL defaultConfigFile = ForgePlugin.class.getResource("/" + yamlFileName);
		YamlConfiguration config = null;
		YamlConfiguration defaultConfig;

		try (final InputStream in = defaultConfigFile.openStream())
		{
			if (in == null)
			{
				defaultConfig = null;
				getLogger().log(Level.WARNING, String.format("Default configuration file does not exist in plugin jar: %s", yamlFileName));
			}
			else
			{
				defaultConfig = YamlConfiguration.loadConfiguration(in);
			}
		}
		catch (Throwable ex)
		{
			defaultConfig = null;
			getLogger().log(Level.WARNING, String.format("Could not load default configuration file from jar: %s", yamlFileName), ex);
		}

		if (defaultConfig != null && !configFile.exists())
		{
			getLogger().log(Level.INFO, String.format("Saving default configuration: %s (%s)", yamlFileName, configFile));

			config = defaultConfig;

			try (final InputStream in = defaultConfigFile.openStream();
				final FileOutputStream out = new FileOutputStream(configFile);
				final ReadableByteChannel readChannel = Channels.newChannel(in);
				final WritableByteChannel writeChannel = Channels.newChannel(out))
			{
				final ByteBuffer buffer = ByteBuffer.allocateDirect(10240);

				while (readChannel.isOpen() && writeChannel.isOpen() && readChannel.read(buffer) >= 0)
				{
					buffer.flip();

					while (writeChannel.isOpen() && buffer.hasRemaining())
					{
						writeChannel.write(buffer);
					}

					buffer.compact();
				}
			}
			catch (Throwable ex)
			{
				getLogger().log(Level.WARNING, String.format("Unable to save default configuration: %s (%s)", yamlFileName, configFile), ex);
			}
		}

		if (!configFile.exists() && defaultConfig == null)
		{
			getLogger().log(Level.WARNING, String.format("Default configuration was unable to load, and custom configuration file does not exist: %s (%s)", yamlFileName, configFile));
			return null;
		}

		if (config == null)
		{
			config = YamlConfiguration.loadConfiguration(configFile);
			if (config == null)
			{
				getLogger().log(Level.SEVERE, String.format("Unspecified error encountered while attempting to load configuration file: %s (%s)", yamlFileName, configFile));
				return null;
			}
		}
		else if (defaultConfig != null)
		{
			config.setDefaults(defaultConfig);
		}

		setConfigOptions(config);
		return config;
	}

	public boolean reload()
	{
		boolean success = true;

		try
		{
			vault = new VaultPermissionHandler(getServer());
		}
		catch (final Throwable ex)
		{
			getLogger().log(Level.WARNING, "Vault not found.  You should probably get that.");
		}

		try
		{
			if (!new File(getDataFolder(), "config.yml").exists())
			{
				saveDefaultConfig();
			}

			reloadConfig();
			setConfigOptions(getConfig());
		}
		catch (final Throwable ex)
		{
			getLogger().log(Level.SEVERE, "Unable to reload configuration.", ex);
			success = false;
		}

		for (final IComponent component : components)
		{
			try
			{
				component.onReload();
			}
			catch (final Throwable ex)
			{
				getLogger().log(Level.SEVERE, "Unable to reload a component.", ex);
				success = false;
			}
		}

		return success;
	}

	public boolean checkPermission(final CommandSender sender, final Command command, final String subNode)
	{
		if (sender.isOp() || sender instanceof ConsoleCommandSender)
		{
			return true;
		}

		final String node = "forgeplugin." + subNode;
		if (vault == null)
		{
			if (sender.hasPermission(node))
			{
				return true;
			}
		}
		else if (vault.hasPermission(sender, node))
		{
			return true;
		}

		sender.sendMessage(String.format("%sYou require the %s permission node to perform that command.", ChatColor.RED, node));
		return false;
	}
}
