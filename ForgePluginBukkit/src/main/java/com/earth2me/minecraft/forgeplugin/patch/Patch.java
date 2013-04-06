package com.earth2me.minecraft.forgeplugin.patch;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import cpw.mods.fml.common.modloader.BaseMod;
import java.util.logging.Level;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;


public abstract class Patch implements IPatch
{
	protected final transient ForgePlugin plugin;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private boolean enabled;
	private boolean attemptedFindMod;
	private Class<? extends BaseMod> mod;
	
	public Patch(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public abstract String getModID();
	
	public String getModClassName()
	{
		return "mod_" + getModID();
	}
	
	public String getFullModClassName()
	{
		return "net.minecraft." + getModClassName();
	}
	
	public Class<? extends BaseMod> getMod()
	{
		if (attemptedFindMod)
		{
			return mod;
		}
		
		try
		{
			return mod = findMod();
		}
		finally
		{
			attemptedFindMod = true;
		}
	}
	
	private Class<? extends BaseMod> findMod()
	{
		try
		{
			final Class<?> type = Class.forName(getFullModClassName());
			final Class<?>[] interfaces = type.getInterfaces();
			
			for (Class<?> inter : interfaces)
			{
				if (BaseMod.class.equals(inter))
				{
					return inter.asSubclass(BaseMod.class);
				}
			}
			
			return null;
		}
		catch (ClassNotFoundException ex)
		{
			plugin.getLogger().log(Level.WARNING, String.format("Mod %s is not present.  Not loading Bukkit-side patch."));
			return null;
		}
	}

	@Override
	public void onEnabled()
	{
		updateModEnabled();
	}

	@Override
	public void onReload()
	{
		updateModEnabled();
	}
	
	protected abstract void onModEnabled();
	
	protected abstract void onModDisabled();
	
	protected final void updateModEnabled()
	{
		final boolean oldEnabled = isEnabled();
		final boolean newEnabled = getMod() != null;
		
		if (oldEnabled != newEnabled)
		{
			setEnabled(newEnabled);
			
			if (newEnabled)
			{
				onModEnabled();
			}
			else
			{
				onModDisabled();
			}
		}
	}
}
