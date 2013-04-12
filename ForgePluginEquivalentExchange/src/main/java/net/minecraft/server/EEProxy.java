package net.minecraft.server;

import ee.EEBase;
import ee.EEMaps;
import ee.TransTabletData;
import forge.DimensionManager;
import forge.NetworkMod;
import java.util.*;


public class EEProxy
{
	public static final int MAXWORLDHEIGHT = 256;
	private static boolean initialized;
	private static MinecraftServer mc;
	private static NetworkMod ee;
	public static World theWorld;
	public static Map<String, TTGroup> ttGroups = new HashMap<>();
	public static Map<String, TTGroup> playerGroups = new HashMap<>();

	private EEProxy()
	{
	}

	public static void Init(MinecraftServer var0, NetworkMod var1)
	{
		if (!initialized)
		{
			initialized = true;
		}

		ee = var1;
		mc = var0;
		theWorld = DimensionManager.getWorld(0);
	}

	public static boolean isClient(World var0)
	{
		return var0.isStatic;
	}

	public static boolean isServer()
	{
		return true;
	}

	public static Object getTileEntity(IBlockAccess var0, int var1, int var2, int var3, Class var4)
	{
		if (var2 < 0)
		{
			return null;
		}

		TileEntity var5 = var0.getTileEntity(var1, var2, var3);
		return !var4.isInstance(var5) ? null : var5;
	}

	public static boolean addTTGroup(String groupName, int masterDimension, Integer[] otherDimensions)
	{
		if (ttGroups.containsKey(groupName))
		{
			return false;
		}

		TTGroup grp = new TTGroup(groupName, masterDimension);
		grp.addDimensions(otherDimensions);
		ttGroups.put(groupName, grp);
		return true;
	}

	public static boolean addPlayerToGroup(String player, String group)
	{
		TTGroup ttGroup = ttGroups.get(group);
		if (ttGroup == null)
		{
			return false;
		}
		TTGroup pGroup = playerGroups.get(player);
		if ((pGroup != null) && (pGroup != ttGroup))
		{
			return false;
		}
		ttGroup.addPlayer(player);
		return true;
	}

	public static void removePlayerFromGroup(String player)
	{
		TTGroup grp = playerGroups.get(player);
		if (grp != null)
		{
			grp.removePlayer(player);
		}
	}

	public static String getGroupDescription(String gName)
	{
		TTGroup group = ttGroups.get(gName);
		if (group == null)
		{
			return "No such group";
		}
		return String.format("%s : Dimensions (main %d others %s) members %s", new Object[]
			{
				group.getGroupName(), Integer.valueOf(group.getMasterDimension()), group.dimensions, group.players
			});
	}

	public static TransTabletData getTransData(EntityHuman var0)
	{
		String playerName = var0.name;
		int dimension = 0;
		if (playerGroups.containsKey(playerName))
		{
			TTGroup groupList = playerGroups.get(playerName);
			if (groupList.containsDimension(var0.dimension))
			{
				playerName = groupList.getGroupName();
				dimension = groupList.getMasterDimension();
			}
		}

		String var1 = "tablet_" + playerName;

		TransTabletData tabletData = (TransTabletData)DimensionManager.getWorld(dimension).a(TransTabletData.class, var1);

		if (tabletData == null)
		{
			tabletData = new TransTabletData(var1, var0);
			tabletData.a();
			DimensionManager.getWorld(dimension).a(var1, tabletData);
		}
		else
		{
			tabletData.setPlayer(var0);
		}

		return tabletData;
	}

	public static boolean isEntityFireImmune(Entity var0)
	{
		return var0.fireProof;
	}

	public static int getEntityHealth(EntityLiving var0)
	{
		return var0.health;
	}

	public static void dealFireDamage(Entity var0, int var1)
	{
		var0.burn(var1);
	}

	public static int getArmorRating(EntityLiving var0)
	{
		return var0.lastDamage;
	}

	public static void setArmorRating(EntityLiving var0, int var1)
	{
		var0.lastDamage = var1;
	}

	public static FoodMetaData getFoodStats(EntityHuman var0)
	{
		return var0.foodData;
	}

	public static WorldData getWorldInfo(World var0)
	{
		return var0.worldData;
	}

	public static int getMaxStackSize(Item var0)
	{
		return var0.maxStackSize;
	}

	public static int blockDamageDropped(Block var0, int var1)
	{
		return var0.getDropData(var1);
	}

	public static void dropBlockAsItemStack(Block var0, EntityLiving var1, int var2, int var3, int var4, ItemStack var5)
	{
		var0.a(var1.world, var2, var3, var4, var5);
	}

	public static void setPlayerFireImmunity(EntityHuman var0, boolean var1)
	{
		var0.fireProof = var1;
	}

	public static void setEMC(ItemStack var0, int var1)
	{
		EEMaps.addEMC(var0.id, var0.getData(), var1);
	}

	public static void setEMC(int var0, int var1, int var2)
	{
		EEMaps.addEMC(var0, var1, var2);
	}

	public static void setEMC(int var0, int var1)
	{
		setEMC(var0, 0, var1);
	}

	public static int getEMC(ItemStack var0)
	{
		return EEMaps.getEMC(var0);
	}

	public static int getEMC(int var0, int var1)
	{
		ItemStack var2 = new ItemStack(var0, 1, var1);
		return EEMaps.getEMC(var2);
	}

	public static int getEMC(int var0)
	{
		ItemStack var1 = new ItemStack(var0, 1, 0);
		return EEMaps.getEMC(var1);
	}

	public static boolean isFuel(ItemStack var0)
	{
		return isFuel(var0.id, var0.getData());
	}

	public static boolean isFuel(int var0)
	{
		return isFuel(var0, 0);
	}

	public static boolean isFuel(int var0, int var1)
	{
		return EEMaps.isFuel(var0, var1);
	}

	public static void addFuel(ItemStack var0)
	{
		addFuel(var0.id, var0.getData());
	}

	public static void addFuel(int var0)
	{
		addFuel(var0, 0);
	}

	public static void addFuel(int var0, int var1)
	{
		EEMaps.addFuelItem(var0, var1);
	}

	public static void handleControl(NetworkManager var0, int var1)
	{
		NetServerHandler var2 = (NetServerHandler)var0.getNetHandler();
		EntityPlayer var3 = var2.getPlayerEntity();

		switch (var1)
		{
		case 0:
			EEBase.doAlternate(var3.world, var3);
			break;
		case 1:
			EEBase.doCharge(var3.world, var3);
			break;
		case 2:
			EEBase.doToggle(var3.world, var3);
			break;
		case 3:
			EEBase.doRelease(var3.world, var3);
		case 4:
		default:
			break;
		case 5:
			EEBase.doJumpTick(var3.world, var3);
			break;
		case 6:
			EEBase.doSneakTick(var3.world, var3);
		}
	}

	public static void handleTEPacket(int var0, int var1, int var2, byte var3, String var4)
	{
	}

	public static void handlePedestalPacket(int var0, int var1, int var2, int var3, boolean var4)
	{
	}


	public static class TTGroup
	{
		private String groupName;
		private int masterDimension;
		private Set<Integer> dimensions;
		private Set<String> players;

		public TTGroup(String name, int masterDimension)
		{
			this.masterDimension = masterDimension;
			groupName = name;
			dimensions = new HashSet<>();
			players = new HashSet<>();
			dimensions.add(Integer.valueOf(masterDimension));
		}

		public boolean containsDimension(int dim)
		{
			return dimensions.contains(Integer.valueOf(dim));
		}

		public String getGroupName()
		{
			return groupName;
		}

		public int getMasterDimension()
		{
			return masterDimension;
		}

		public void addPlayer(String player)
		{
			players.add(player);
			EEProxy.playerGroups.put(player, this);
		}

		public void removePlayer(String player)
		{
			players.remove(player);
			EEProxy.playerGroups.remove(player);
		}

		public void addDimensions(Integer[] dim)
		{
			dimensions.addAll(Arrays.asList(dim));
		}
	}
}