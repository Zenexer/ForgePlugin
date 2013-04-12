package ee;

import cpw.mods.fml.server.FMLBukkitHandler;
import forge.ICraftingHandler;
import forge.MinecraftForge;
import java.io.File;
import java.util.*;
import net.minecraft.server.*;


public class EEBase
{
	public final static HashMap<EntityHuman, Boolean> playerSwordMode = new HashMap<>();
	public final static HashMap<EntityHuman, Integer> playerWatchCycle = new HashMap<>();
	public final static HashMap<EntityHuman, Integer> playerBuildMode = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerInWater = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerInLava = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerHammerMode = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerArmorOffensiveToggle = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerArmorMovementToggle = new HashMap<>();
	public final static HashMap<EntityHuman, Integer> playerToggleCooldown = new HashMap<>();
	public final static HashMap<EntityHuman, Integer> playerToolMode = new HashMap<>();
	public final static HashMap<EntityHuman, Integer> playerWatchMagnitude = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerLeftClick = new HashMap<>();
	public final static HashMap<EntityHuman, Boolean> playerTransGridOpen = new HashMap<>();
	public final static HashMap<EntityHuman, HashMap<Item, Boolean>> playerItemCharging = new HashMap<>();
	public final static HashMap<EntityHuman, HashMap<Item, Integer>> playerEffectDurations = new HashMap<>();
	public final static HashMap<EntityHuman, ?> playerKnowledge = new HashMap<>();
	private final static Set<Map<EntityHuman, ?>> playerMaps;
	private static BaseMod instance;
	private static EEBase eeBaseInstance;
	public static boolean initialized = false;
	public static EEProps props;
	public static int playerWoftFactor = 1;
	private static boolean leftClickWasDown;
	private static boolean extraKeyWasDown;
	private static boolean releaseKeyWasDown;
	private static boolean chargeKeyWasDown;
	private static boolean toggleKeyWasDown;
	public static boolean externalModsInitialized;
	public static int alchChestFront = 0;
	public static int alchChestSide = 1;
	public static int alchChestTop = 2;
	public static int condenserFront = 3;
	public static int condenserSide = 4;
	public static int condenserTop = 5;
	public static int relayFront = 6;
	public static int relaySide = 7;
	public static int relayTop = 8;
	public static int collectorFront = 9;
	public static int collectorSide = 10;
	public static int collectorTop = 11;
	public static int dmFurnaceFront = 12;
	public static int dmBlockSide = 13;
	public static int rmFurnaceFront = 14;
	public static int rmBlockSide = 15;
	public static int iTorchSide = 16;
	public static int novaCatalystSide = 17;
	public static int novaCataclysmSide = 18;
	public static int novaCatalystTop = 19;
	public static int novaCatalystBottom = 20;
	public static int collector2Top = 21;
	public static int collector3Top = 22;
	public static int relay2Top = 23;
	public static int relay3Top = 24;
	public static int transTabletSide = 25;
	public static int transTabletBottom = 26;
	public static int transTabletTop = 27;
	public static int portalDeviceSide = 28;
	public static int portalDeviceBottom = 29;
	public static int portalDeviceTop = 30;
	public static HashMap<Integer, Integer[]> pedestalCoords = new HashMap<>();
	private static int machineFactor;

	static
	{
		final Set<Map<EntityHuman, ?>> playerMapsBuilder = new HashSet<>();
		playerMapsBuilder.add(playerSwordMode);
		playerMapsBuilder.add(playerWatchCycle);
		playerMapsBuilder.add(playerBuildMode);
		playerMapsBuilder.add(playerInWater);
		playerMapsBuilder.add(playerInLava);
		playerMapsBuilder.add(playerHammerMode);
		playerMapsBuilder.add(playerArmorOffensiveToggle);
		playerMapsBuilder.add(playerArmorMovementToggle);
		playerMapsBuilder.add(playerToggleCooldown);
		playerMapsBuilder.add(playerToolMode);
		playerMapsBuilder.add(playerWatchMagnitude);
		playerMapsBuilder.add(playerLeftClick);
		playerMapsBuilder.add(playerTransGridOpen);
		playerMapsBuilder.add(playerItemCharging);
		playerMapsBuilder.add(playerEffectDurations);
		playerMapsBuilder.add(playerKnowledge);

		playerMaps = Collections.unmodifiableSet(playerMapsBuilder);
	}

	public static void init(BaseMod var0)
	{
		if (!initialized)
		{
			initialized = true;
			instance = var0;
			props = new EEProps(new File("mod_EE.props").getPath());
			props = EEMaps.InitProps(props);
			props.func_26596_save();
			machineFactor = props.getInt("machineFactor");
			setupCraftHook();
		}
	}

	public static void cleanup()
	{
		final List<String> players = Arrays.asList(FMLBukkitHandler.instance().getServer().getPlayers());

		for (final Map<EntityHuman, ?> map : playerMaps)
		{
			final Set<EntityHuman> keySet = map.keySet();
			final Iterator<EntityHuman> iterator = keySet.iterator();
			while (iterator.hasNext())
			{
				if (!players.contains(iterator.next().name))
				{
					iterator.remove();
				}
			}
		}
	}

	public int AddFuel(int var1, int var2)
	{
		if (var1 == EEItem.alchemicalCoal.id)
		{
			if (var2 == 0)
			{
				return 6400;
			}
		}
		else if (var1 == EEItem.mobiusFuel.id)
		{
			return 25600;
		}

		return 0;
	}

	public static boolean isCurrentItem(Item var0, EntityHuman var1)
	{
		return var1.inventory.getItemInHand() != null;
	}

	public static boolean isOnQuickBar(Item var0, EntityHuman var1)
	{
		for (int var2 = 0; var2 < 9; var2++)
		{
			if ((var1.inventory.getItem(var2) != null) && (var1.inventory.getItem(var2).getItem() == var0))
			{
				return true;
			}
		}

		return false;
	}

	public static ItemStack[] quickBar(EntityHuman var0)
	{
		ItemStack[] var1 = new ItemStack[9];
		System.arraycopy(var0.inventory.items, 0, var1, 0, var1.length);
		return var1;
	}

	public static boolean EntityHasItemStack(ItemStack var0, IInventory var1)
	{
		boolean var2 = var0.getData() == -1;
		ItemStack[] var3 = new ItemStack[40];

		for (int var4 = 0; var4 < var1.getSize(); var4++)
		{
			if ((var1.getItem(var4) != null) && ((var1.getItem(var4).doMaterialsMatch(var0)) || ((var1.getItem(var4).getItem() == var0.getItem()) && (var2))))
			{
				if (var1.getItem(var4).count >= var0.count)
				{
					return true;
				}

				var3[var4] = var1.getItem(var4);
			}
		}

		int var4 = 0;

		for (int var5 = 0; var5 < var1.getSize(); var5++)
		{
			if ((var3[var5] != null) && ((var3[var5].doMaterialsMatch(var0)) || ((var3[var5].getItem() == var0.getItem()) && (var2))))
			{
				var4 += var3[var5].count;

				if (var4 >= var0.count)
				{
					return true;
				}
			}
		}

		return false;
	}

	public static boolean HasItemStack(ItemStack var0, EntityHuman var1)
	{
		boolean var2 = var0.getData() == -1;
		ItemStack[] var3 = new ItemStack[40];
		PlayerInventory var4 = var1.inventory;

		for (int var5 = 0; var5 < var4.items.length + var4.armor.length; var5++)
		{
			if ((var4.getItem(var5) != null) && ((var4.getItem(var5).doMaterialsMatch(var0)) || ((var4.getItem(var5).getItem() == var0.getItem()) && (var2))))
			{
				if (var4.getItem(var5).count >= var0.count)
				{
					return true;
				}

				var3[var5] = var4.getItem(var5);
			}
		}

		int var5 = 0;

		for (int var6 = 0; var6 < var4.items.length + var4.armor.length; var6++)
		{
			if ((var3[var6] != null) && ((var3[var6].doMaterialsMatch(var0)) || ((var3[var6].getItem() == var0.getItem()) && (var2))))
			{
				var5 += var3[var6].count;

				if (var5 >= var0.count)
				{
					return true;
				}
			}
		}

		return false;
	}

	public static int getKleinEnergyForDisplay(ItemStack var0)
	{
		return (var0.getItem() instanceof ItemKleinStar) ? ((ItemKleinStar)var0.getItem()).getKleinPoints(var0) : var0 == null ? 0 : 0;
	}

	public static int getDisplayEnergy(ItemStack var0)
	{
		if (var0 == null)
		{
			return 0;
		}
		if (((var0.getItem() instanceof ItemEECharged)) && ((var0.getItem() instanceof ItemTransTablet)))
		{
			ItemEECharged var1 = (ItemEECharged)var0.getItem();
			return var1.getInteger(var0, "displayEnergy");
		}

		return 0;
	}

	public static void setDisplayEnergy(ItemStack var0, int var1)
	{
		if (var0 != null)
		{
			if (((var0.getItem() instanceof ItemEECharged)) && ((var0.getItem() instanceof ItemTransTablet)))
			{
				ItemEECharged var2 = (ItemEECharged)var0.getItem();
				var2.setInteger(var0, "displayEnergy", var1);
			}
		}
	}

	public static int getLatentEnergy(ItemStack var0)
	{
		if (var0 == null)
		{
			return 0;
		}
		if (((var0.getItem() instanceof ItemEECharged)) && ((var0.getItem() instanceof ItemTransTablet)))
		{
			ItemEECharged var1 = (ItemEECharged)var0.getItem();
			return var1.getInteger(var0, "latentEnergy");
		}

		return 0;
	}

	public static void setLatentEnergy(ItemStack var0, int var1)
	{
		if (var0 != null)
		{
			if (((var0.getItem() instanceof ItemEECharged)) && ((var0.getItem() instanceof ItemTransTablet)))
			{
				ItemEECharged var2 = (ItemEECharged)var0.getItem();
				var2.setInteger(var0, "latentEnergy", var1);
			}
		}
	}

	public static boolean canIncreaseKleinStarPoints(ItemStack var0, World var1)
	{
		if (EEProxy.isClient(var1))
		{
			return false;
		}

		byte var2 = 1;
		return var0 != null;
	}

	public static boolean isKleinStar(int var0)
	{
		return (var0 == EEItem.kleinStar1.id) || (var0 == EEItem.kleinStar2.id) || (var0 == EEItem.kleinStar3.id) || (var0 == EEItem.kleinStar4.id) || (var0 == EEItem.kleinStar5.id) || (var0 == EEItem.kleinStar6.id);
	}

	public static int getKleinLevel(int var0)
	{
		return var0 == EEItem.kleinStar6.id ? 6 : var0 == EEItem.kleinStar5.id ? 5 : var0 == EEItem.kleinStar4.id ? 4 : var0 == EEItem.kleinStar3.id ? 3 : var0 == EEItem.kleinStar2.id ? 2 : var0 == EEItem.kleinStar1.id ? 1 : 0;
	}

	public static boolean addKleinStarPoints(ItemStack var0, int var1, World var2)
	{
		if (EEProxy.isClient(var2))
		{
			return false;
		}
		if (var0 == null)
		{
			return false;
		}
		if (!isKleinStar(var0.id))
		{
			return false;
		}

		ItemKleinStar var3 = (ItemKleinStar)var0.getItem();

		if (var3.getKleinPoints(var0) <= var3.getMaxPoints(var0) - var1)
		{
			var3.setKleinPoints(var0, var3.getKleinPoints(var0) + var1);
			var3.onUpdate(var0);
			return true;
		}

		return false;
	}

	public static boolean addKleinStarPoints(ItemStack var0, int var1)
	{
		if (var0 == null)
		{
			return false;
		}
		if (!isKleinStar(var0.id))
		{
			return false;
		}

		ItemKleinStar var2 = (ItemKleinStar)var0.getItem();

		if (var2.getKleinPoints(var0) <= var2.getMaxPoints(var0) - var1)
		{
			var2.setKleinPoints(var0, var2.getKleinPoints(var0) + var1);
			var2.onUpdate(var0);
			return true;
		}

		return false;
	}

	public static boolean takeKleinStarPoints(ItemStack var0, int var1, World var2)
	{
		if (EEProxy.isClient(var2))
		{
			return false;
		}
		if (var0 == null)
		{
			return false;
		}
		if (!isKleinStar(var0.id))
		{
			return false;
		}

		ItemKleinStar var3 = (ItemKleinStar)var0.getItem();

		if (var3.getKleinPoints(var0) >= var1)
		{
			var3.setKleinPoints(var0, var3.getKleinPoints(var0) - var1);
			var3.onUpdate(var0);
			return true;
		}

		return false;
	}

	public static boolean consumeKleinStarPoint(EntityHuman var0, int var1)
	{
		if (var0 == null)
		{
			return false;
		}
		if (EEProxy.isClient(var0.world))
		{
			return false;
		}

		PlayerInventory var2 = var0.inventory;

		for (int var3 = 0; var3 < var2.items.length; var3++)
		{
			if (var2.getItem(var3) != null)
			{
				ItemStack var4 = var2.getItem(var3);

				if ((isKleinStar(var4.id)) && (takeKleinStarPoints(var4, var1, var0.world)))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static boolean Consume(ItemStack var0, EntityHuman var1, boolean var2)
	{
		if (var1 == null)
		{
			return false;
		}
		if (EEProxy.isClient(var1.world))
		{
			return false;
		}

		int var3 = var0.count;
		int var4 = 0;
		boolean var5 = false;

		if (var0.getData() == -1)
		{
			var5 = true;
		}

		ItemStack[] var6 = var1.inventory.items;

		for (int var7 = 0; var7 < var6.length; var7++)
		{
			if (var6[var7] != null)
			{
				if (var3 <= var4)
				{
					break;
				}

				if ((var6[var7].doMaterialsMatch(var0)) || ((var5) && (var6[var7].id == var0.id)))
				{
					var4 += var6[var7].count;
				}
			}
		}

		if (var4 < var3)
		{
			return false;
		}

		var4 = 0;

		for (int var7 = 0; var7 < var6.length; var7++)
		{
			if ((var6[var7] != null) && ((var6[var7].doMaterialsMatch(var0)) || ((var5) && (var6[var7].id == var0.id))))
			{
				for (int var8 = var6[var7].count; var8 > 0; var8--)
				{
					var6[var7].count -= 1;

					if (var6[var7].count == 0)
					{
						var6[var7] = null;
					}

					var4++;

					if (var4 >= var3)
					{
						return true;
					}
				}
			}
		}

		if (var2)
		{
			var1.a("You don't have enough fuel/klein power to do that.");
		}

		return false;
	}

	public static double direction(EntityHuman var0)
	{
		return var0.pitch <= -55.0F ? 1.0D : (var0.pitch > -55.0F) && (var0.pitch < 55.0F) ? (MathHelper.floor(var0.yaw * 4.0F / 360.0F + 0.5D) & 0x3) + 2 : 0.0D;
	}

	public static double heading(EntityHuman var0)
	{
		return (MathHelper.floor(var0.yaw * 4.0F / 360.0F + 0.5D) & 0x3) + 2;
	}

	public static double playerX(EntityHuman var0)
	{
		return MathHelper.floor(var0.locX);
	}

	public static double playerY(EntityHuman var0)
	{
		return MathHelper.floor(var0.locY);
	}

	public static double playerZ(EntityHuman var0)
	{
		return MathHelper.floor(var0.locZ);
	}

	public static void doLeftClick(World var0, EntityHuman var1)
	{
		if (var1.U() != null)
		{
			if ((var1.U().getItem() instanceof ItemEECharged))
			{
				((ItemEECharged)var1.U().getItem()).doLeftClick(var1.U(), var0, var1);
			}
		}
	}

	public static void doAlternate(World var0, EntityHuman var1)
	{
		ItemStack var2 = var1.U();

		if (var2 == null)
		{
			armorCheck(var1);
		}
		else if ((var1.U().getItem() instanceof ItemEECharged))
		{
			((ItemEECharged)var1.U().getItem()).doAlternate(var1.U(), var0, var1);
		}
		else
		{
			armorCheck(var1);
		}
	}

	private static void armorCheck(EntityHuman var0)
	{
		if ((hasRedArmor(var0)) && (getPlayerArmorOffensive(var0)))
		{
			Combustion var1 = new Combustion(var0.world, var0, var0.locX, var0.locY, var0.locZ, 4.0F);
			var1.doExplosionA();
			var1.doExplosionB(true);
		}
	}

	private static boolean hasRedArmor(EntityHuman var0)
	{
		return (var0.inventory.armor[2] != null) && ((var0.inventory.armor[2].getItem() instanceof ItemRedArmorPlus));
	}

	public static void doToggle(World var0, EntityHuman var1)
	{
		ItemStack var2 = var1.U();

		if (var2 == null)
		{
			if ((hasMovementArmor(var1)) && (getPlayerToggleCooldown(var1) <= 0))
			{
				updatePlayerArmorMovement(var1, true);
				setPlayerToggleCooldown(var1, 20);
			}
		}
		else if ((var1.U().getItem() instanceof ItemEECharged))
		{
			((ItemEECharged)var1.U().getItem()).doToggle(var1.U(), var0, var1);
		}
		else if ((hasMovementArmor(var1)) && (getPlayerToggleCooldown(var1) <= 0))
		{
			updatePlayerArmorMovement(var1, true);
			setPlayerToggleCooldown(var1, 20);
		}
	}

	public static void doJumpTick(World var0, EntityPlayer var1)
	{
		bootsCheck(var1);
	}

	private static void bootsCheck(EntityHuman var0)
	{
		if ((hasRedBoots(var0)) && (getPlayerArmorMovement(var0)))
		{
			var0.motY += 0.1D;
		}
	}

	private static boolean hasRedBoots(EntityHuman var0)
	{
		return (var0.inventory.armor[0] != null) && ((var0.inventory.armor[0].getItem() instanceof ItemRedArmorPlus));
	}

	public static void doSneakTick(World var0, EntityPlayer var1)
	{
		greavesCheck(var1);
	}

	private static void greavesCheck(EntityHuman var0)
	{
		if ((hasRedGreaves(var0)) && (getPlayerArmorOffensive(var0)))
		{
			var0.motY -= 0.97D;
			doShockwave(var0);
		}
	}

	private static void doShockwave(EntityHuman var0)
	{
		List var1 = var0.world.a(EntityLiving.class, AxisAlignedBB.b(var0.locX - 7.0D, var0.locY - 7.0D, var0.locZ - 7.0D, var0.locX + 7.0D, var0.locY + 7.0D, var0.locZ + 7.0D));

		for (int var2 = 0; var2 < var1.size(); var2++)
		{
			Entity var3 = (Entity)var1.get(var2);

			if (!(var3 instanceof EntityHuman))
			{
				var3.motX += 0.2D / (var3.locX - var0.locX);
				var3.motY += 0.05999999865889549D;
				var3.motZ += 0.2D / (var3.locZ - var0.locZ);
			}
		}

		List var6 = var0.world.a(EntityArrow.class, AxisAlignedBB.b((float)var0.locX - 5.0F, var0.locY - 5.0D, (float)var0.locZ - 5.0F, (float)var0.locX + 5.0F, var0.locY + 5.0D, (float)var0.locZ + 5.0F));

		for (int var7 = 0; var7 < var6.size(); var7++)
		{
			Entity var4 = (Entity)var6.get(var7);
			var4.motX += 0.2D / (var4.locX - var0.locX);
			var4.motY += 0.05999999865889549D;
			var4.motZ += 0.2D / (var4.locZ - var0.locZ);
		}

		List var8 = var0.world.a(EntityFireball.class, AxisAlignedBB.b((float)var0.locX - 5.0F, var0.locY - 5.0D, (float)var0.locZ - 5.0F, (float)var0.locX + 5.0F, var0.locY + 5.0D, (float)var0.locZ + 5.0F));

		for (int var9 = 0; var9 < var8.size(); var9++)
		{
			Entity var5 = (Entity)var8.get(var9);
			var5.motX += 0.2D / (var5.locX - var0.locX);
			var5.motY += 0.05999999865889549D;
			var5.motZ += 0.2D / (var5.locZ - var0.locZ);
		}
	}

	private static boolean hasRedGreaves(EntityHuman var0)
	{
		return (var0.inventory.armor[1] != null) && ((var0.inventory.armor[1].getItem() instanceof ItemRedArmorPlus));
	}

	public static void doRelease(World var0, EntityHuman var1)
	{
		ItemStack var2 = var1.U();

		if (var2 == null)
		{
			helmetCheck(var1);
		}
		else if ((var2.getItem() instanceof ItemEECharged))
		{
			((ItemEECharged)var1.U().getItem()).doRelease(var1.U(), var0, var1);
		}
		else
		{
			helmetCheck(var1);
		}
	}

	private static void helmetCheck(EntityHuman var0)
	{
		if ((hasRedHelmet(var0)) && (getPlayerArmorOffensive(var0)))
		{
			float var1 = 1.0F;
			float var2 = var0.lastPitch + (var0.pitch - var0.lastPitch) * var1;
			float var3 = var0.lastYaw + (var0.yaw - var0.lastYaw) * var1;
			double var4 = var0.lastX + (var0.locX - var0.lastX) * var1;
			double var6 = var0.lastY + (var0.locY - var0.lastY) * var1 + 1.62D - var0.height;
			double var8 = var0.lastZ + (var0.locZ - var0.lastZ) * var1;
			Vec3D var10 = Vec3D.create(var4, var6, var8);
			float var11 = MathHelper.cos(-var3 * 0.01745329F - 3.141593F);
			float var12 = MathHelper.sin(-var3 * 0.01745329F - 3.141593F);
			float var13 = -MathHelper.cos(-var2 * 0.01745329F);
			float var14 = MathHelper.sin(-var2 * 0.01745329F);
			float var15 = var12 * var13;
			float var17 = var11 * var13;
			double var18 = 150.0D;
			Vec3D var20 = var10.add(var15 * var18, var14 * var18, var17 * var18);
			MovingObjectPosition var21 = var0.world.rayTrace(var10, var20, true);

			if (var21 == null)
			{
				return;
			}

			if (var21.type == EnumMovingObjectType.TILE)
			{
				int var22 = var21.b;
				int var23 = var21.c;
				int var24 = var21.d;
				var0.world.strikeLightning(new EntityWeatherLighting(var0.world, var22, var23, var24));
			}
		}
	}

	private static boolean hasRedHelmet(EntityHuman var0)
	{
		return (var0.inventory.armor[3] != null) && ((var0.inventory.armor[3].getItem() instanceof ItemRedArmorPlus));
	}

	public static void doCharge(World var0, EntityHuman var1)
	{
		ItemStack var2 = var1.U();

		if (var2 == null)
		{
			if ((hasOffensiveArmor(var1)) && (getPlayerToggleCooldown(var1) <= 0))
			{
				updatePlayerArmorOffensive(var1, true);
				setPlayerToggleCooldown(var1, 20);
			}
		}
		else if ((var2.getItem() instanceof ItemEECharged))
		{
			ItemEECharged var3 = (ItemEECharged)var2.getItem();

			if (!var1.isSneaking())
			{
				if ((var3.getMaxCharge() > 0) && (var3.chargeLevel(var2) < var3.getMaxCharge()) && (var3.chargeGoal(var2) < var3.getMaxCharge()))
				{
					var3.setShort(var2, "chargeGoal", var3.chargeGoal(var2) + 1);
				}
			}
			else
			{
				var3.doUncharge(var2, var0, var1);
			}
		}
		else if ((hasOffensiveArmor(var1)) && (getPlayerToggleCooldown(var1) <= 0))
		{
			updatePlayerArmorOffensive(var1, true);
			setPlayerToggleCooldown(var1, 20);
		}
	}

	private static boolean hasOffensiveArmor(EntityHuman var0)
	{
		return ((var0.inventory.armor[2] != null) && ((var0.inventory.armor[2].getItem() instanceof ItemRedArmorPlus))) || ((var0.inventory.armor[1] != null) && ((var0.inventory.armor[1].getItem() instanceof ItemRedArmorPlus))) || ((var0.inventory.armor[3] != null) && ((var0.inventory.armor[3].getItem() instanceof ItemRedArmorPlus)));
	}

	private static boolean hasMovementArmor(EntityHuman var0)
	{
		return (var0.inventory.armor[0] != null) && ((var0.inventory.armor[0].getItem() instanceof ItemRedArmorPlus));
	}

	static boolean isPlayerCharging(EntityHuman var0, Item var1)
	{
		return playerItemCharging.get(var0) == null ? false : playerItemCharging.get(var0).get(var1) == null ? false : playerItemCharging.get(var0).get(var1);
	}

	public static void updatePlayerEffect(Item var0, int var1, EntityHuman var2)
	{
		HashMap<Item, Integer> effectDuration = playerEffectDurations.get(var2);
		if (effectDuration == null)
		{
			playerEffectDurations.put(var2, effectDuration = new HashMap<>());
		}

		effectDuration.put(var0, Integer.valueOf(var1));
	}

	public static int getPlayerEffect(Item var0, EntityHuman var1)
	{
		return playerEffectDurations.get(var1) == null ? 0 : playerEffectDurations.get(var1).get(var0) == null ? 0 : playerEffectDurations.get(var1).get(var0);
	}

	public static int getPlayerToggleCooldown(EntityHuman var0)
	{
		if (playerToggleCooldown.get(var0) == null)
		{
			playerToggleCooldown.put(var0, Integer.valueOf(0));
		}

		return playerToggleCooldown.get(var0).intValue();
	}

	public static void setPlayerToggleCooldown(EntityHuman var0, int var1)
	{
		if (playerToggleCooldown.get(var0) == null)
		{
			playerToggleCooldown.put(var0, Integer.valueOf(0));
		}
		else
		{
			playerToggleCooldown.put(var0, Integer.valueOf(var1));
		}
	}

	public static void updatePlayerToggleCooldown(EntityHuman var0)
	{
		if (playerToggleCooldown.get(var0) == null)
		{
			playerToggleCooldown.put(var0, Integer.valueOf(0));
		}
		else
		{
			playerToggleCooldown.put(var0, Integer.valueOf(playerToggleCooldown.get(var0).intValue() - 1));
		}
	}

	public static int getBuildMode(EntityHuman var0)
	{
		if (playerBuildMode.get(var0) == null)
		{
			playerBuildMode.put(var0, Integer.valueOf(0));
		}

		return playerBuildMode.get(var0).intValue();
	}

	public static void updateBuildMode(EntityHuman var0)
	{
		if (playerBuildMode.get(var0) == null)
		{
			playerBuildMode.put(var0, Integer.valueOf(0));
		}
		else if (playerBuildMode.get(var0).intValue() == 3)
		{
			playerBuildMode.put(var0, Integer.valueOf(0));
		}
		else
		{
			playerBuildMode.put(var0, Integer.valueOf(playerBuildMode.get(var0).intValue() + 1));
		}

		if (playerBuildMode.get(var0).intValue() == 0)
		{
			var0.a("Mercurial Extension mode.");
		}
		else if (playerBuildMode.get(var0).intValue() == 1)
		{
			var0.a("Mercurial Creation mode.");
		}
		else if (playerBuildMode.get(var0).intValue() == 2)
		{
			var0.a("Mercurial Transmute mode.");
		}
		else if (playerBuildMode.get(var0).intValue() == 3)
		{
			var0.a("Mercurial Pillar mode. [Careful!]");
		}
	}

	public static boolean getPlayerArmorOffensive(EntityHuman var0)
	{
		if (playerArmorOffensiveToggle.get(var0) == null)
		{
			playerArmorOffensiveToggle.put(var0, Boolean.valueOf(false));
		}

		return playerArmorOffensiveToggle.get(var0).booleanValue();
	}

	public static void updatePlayerArmorOffensive(EntityHuman var0, boolean var1)
	{
		if (playerArmorOffensiveToggle.get(var0) == null)
		{
			playerArmorOffensiveToggle.put(var0, Boolean.valueOf(false));
		}
		else
		{
			playerArmorOffensiveToggle.put(var0, Boolean.valueOf(!playerArmorOffensiveToggle.get(var0).booleanValue()));
		}

		if (playerArmorOffensiveToggle.get(var0).booleanValue())
		{
			if (var1)
			{
				var0.a("Armor offensive powers on.");
			}
		}
		else if (var1)
		{
			var0.a("Armor offensive powers off.");
		}
	}

	public static boolean getPlayerArmorMovement(EntityHuman var0)
	{
		if (playerArmorMovementToggle.get(var0) == null)
		{
			playerArmorMovementToggle.put(var0, Boolean.valueOf(false));
		}

		return playerArmorMovementToggle.get(var0).booleanValue();
	}

	public static void updatePlayerArmorMovement(EntityHuman var0, boolean var1)
	{
		if (playerArmorMovementToggle.get(var0) == null)
		{
			playerArmorMovementToggle.put(var0, Boolean.valueOf(false));
		}
		else
		{
			playerArmorMovementToggle.put(var0, Boolean.valueOf(!playerArmorMovementToggle.get(var0).booleanValue()));
		}

		if (playerArmorMovementToggle.get(var0).booleanValue())
		{
			if (var1)
			{
				var0.a("Armor movement powers on.");
			}
		}
		else if (var1)
		{
			var0.a("Armor movement powers off.");
		}
	}

	public static boolean getHammerMode(EntityHuman var0)
	{
		if (playerHammerMode.get(var0) == null)
		{
			playerHammerMode.put(var0, Boolean.valueOf(false));
			return false;
		}

		return playerHammerMode.get(var0).booleanValue();
	}

	public static void updateHammerMode(EntityHuman var0, boolean var1)
	{
		if (playerHammerMode.get(var0) == null)
		{
			playerHammerMode.put(var0, Boolean.valueOf(false));
		}
		else
		{
			playerHammerMode.put(var0, Boolean.valueOf(!playerHammerMode.get(var0).booleanValue()));
		}

		if (playerHammerMode.get(var0).booleanValue())
		{
			if (var1)
			{
				var0.a("Hammer mega-impact mode.");
			}
		}
		else if (var1)
		{
			var0.a("Hammer normal-impact mode.");
		}
	}

	public static boolean getSwordMode(EntityHuman var0)
	{
		if (playerSwordMode.get(var0) == null)
		{
			playerSwordMode.put(var0, Boolean.valueOf(false));
			return false;
		}

		return playerSwordMode.get(var0).booleanValue();
	}

	public static void updateSwordMode(EntityHuman var0)
	{
		if (playerSwordMode.get(var0) == null)
		{
			playerSwordMode.put(var0, Boolean.valueOf(false));
		}
		else
		{
			playerSwordMode.put(var0, Boolean.valueOf(!playerSwordMode.get(var0).booleanValue()));
		}

		if (playerSwordMode.get(var0).booleanValue())
		{
			var0.a("Sword AoE will harm peaceful/aggressive.");
		}
		else
		{
			var0.a("Sword AoE will harm aggressive only.");
		}
	}

	public static int getWatchCycle(EntityHuman var0)
	{
		if (playerWatchCycle.get(var0) == null)
		{
			playerWatchCycle.put(var0, Integer.valueOf(0));
			return 0;
		}

		return playerWatchCycle.get(var0).intValue();
	}

	public static void updateWatchCycle(EntityHuman var0)
	{
		if (playerWatchCycle.get(var0) == null)
		{
			playerWatchCycle.put(var0, Integer.valueOf(0));
		}
		else if (playerWatchCycle.get(var0).intValue() == 2)
		{
			playerWatchCycle.put(var0, Integer.valueOf(0));
		}
		else
		{
			playerWatchCycle.put(var0, Integer.valueOf(playerWatchCycle.get(var0).intValue() + 1));
		}

		if (playerWatchCycle.get(var0).intValue() == 0)
		{
			var0.a("Sun-scroll is off.");
		}

		if (playerWatchCycle.get(var0).intValue() == 1)
		{
			var0.a("Sun-scrolling forward.");
		}

		if (playerWatchCycle.get(var0).intValue() == 2)
		{
			var0.a("Sun-scrolling backwards.");
		}
	}

	public static int getToolMode(EntityHuman var0)
	{
		if (playerToolMode.get(var0) == null)
		{
			playerToolMode.put(var0, Integer.valueOf(0));
			return 0;
		}

		return playerToolMode.get(var0).intValue();
	}

	public static void updateToolMode(EntityHuman var0)
	{
		if (playerToolMode.get(var0) == null)
		{
			playerToolMode.put(var0, Integer.valueOf(0));
		}
		else if (playerToolMode.get(var0).intValue() == 3)
		{
			playerToolMode.put(var0, Integer.valueOf(0));
		}
		else
		{
			playerToolMode.put(var0, Integer.valueOf(playerToolMode.get(var0).intValue() + 1));
		}

		if (playerToolMode.get(var0).intValue() == 0)
		{
			var0.a("Tool set to normal.");
		}

		if (playerToolMode.get(var0).intValue() == 1)
		{
			var0.a("Tool set to tall-shot.");
		}

		if (playerToolMode.get(var0).intValue() == 2)
		{
			var0.a("Tool set to wide-shot.");
		}

		if (playerToolMode.get(var0).intValue() == 3)
		{
			var0.a("Tool set to long-shot.");
		}
	}

	public static boolean isPlayerInLava(EntityHuman var0)
	{
		if (playerInLava.get(var0) == null)
		{
			playerInLava.put(var0, Boolean.valueOf(false));
			return false;
		}

		return playerInLava.get(var0).booleanValue();
	}

	public static void updatePlayerInLava(EntityHuman var0, boolean var1)
	{
		playerInLava.put(var0, Boolean.valueOf(var1));
	}

	public static boolean isPlayerInWater(EntityHuman var0)
	{
		if (playerInWater.get(var0) == null)
		{
			playerInWater.put(var0, Boolean.valueOf(false));
			return false;
		}

		return playerInWater.get(var0).booleanValue();
	}

	public static void updatePlayerInWater(EntityHuman var0, boolean var1)
	{
		playerInWater.put(var0, Boolean.valueOf(var1));
	}

	private static void setupCraftHook()
	{
		ICraftingHandler var0 = new ICraftingHandler()
		{
			@Override
			public void onTakenFromCrafting(EntityHuman var1, ItemStack var2, IInventory var3)
			{
				int var4 = 0;

				if ((var2 != null) && (EEMergeLib.mergeOnCraft.contains(Integer.valueOf(var2.id))))
				{
					for (int var5 = 0; var5 < var3.getSize(); var5++)
					{
						ItemStack var6 = var3.getItem(var5);

						if ((var6 != null) && ((var6.getItem() instanceof ItemKleinStar)) && (((ItemKleinStar)var6.getItem()).getKleinPoints(var6) > 0))
						{
							var4 += ((ItemKleinStar)var6.getItem()).getKleinPoints(var6);
						}
					}

					((ItemKleinStar)var2.getItem()).setKleinPoints(var2, var4);
				}
				else if ((var2 != null) && (EEMergeLib.destroyOnCraft.contains(Integer.valueOf(var2.id))) && (var2.id == EEItem.arcaneRing.id))
				{
					for (int var5 = 0; var5 < var3.getSize(); var5++)
					{
						var3.setItem(var5, null);
					}
				}
			}
		};
		MinecraftForge.registerCraftingHandler(var0);
	}

	public static EEBase getInstance()
	{
		return eeBaseInstance;
	}

	public static float getPedestalFactor(World var0)
	{
		float var1 = 1.0F;
		validatePedestalCoords(var0);

		for (int var2 = 0; var2 < pedestalCoords.size(); var2++)
		{
			if (pedestalCoords.get(Integer.valueOf(var2)) != null)
			{
				var1 = (float)(var1 * 0.9D);
			}
		}

		return var1 < 0.1F ? 0.1F : var1;
	}

	public static void addPedestalCoords(TilePedestal var0)
	{
		Integer[] var2 = { var0.x, var0.y, var0.z };

		for (int var1 = 0; pedestalCoords.get(var1) != null; var1++)
		{
			pedestalCoords.put(var1, var2);
		}

		validatePedestalCoords(var0.world);
	}

	public static void validatePedestalCoords(World var0)
	{
		for (int var1 = 0; var1 < pedestalCoords.size(); var1++)
		{
			if (pedestalCoords.get(Integer.valueOf(var1)) != null)
			{
				Integer[] var2 = pedestalCoords.get(Integer.valueOf(var1));

				if (EEProxy.getTileEntity(var0, var2[0].intValue(), var2[1].intValue(), var2[2].intValue(), TilePedestal.class) == null)
				{
					removePedestalCoord(var1);
				}
				else if (!((TilePedestal)EEProxy.getTileEntity(var0, var2[0].intValue(), var2[1].intValue(), var2[2].intValue(), TilePedestal.class)).isActivated())
				{
					removePedestalCoord(var1);
				}
				else
				{
					for (int var3 = 0; var3 < pedestalCoords.size(); var3++)
					{
						if ((var1 != var3) && (pedestalCoords.get(Integer.valueOf(var3)) != null))
						{
							Integer[] var4 = pedestalCoords.get(Integer.valueOf(var3));

							if (coordsEqual(var2, var4))
							{
								removePedestalCoord(var3);
							}
						}
					}
				}
			}
		}
	}

	private static boolean coordsEqual(Integer[] var0, Integer[] var1)
	{
		return (var0[0].equals(var1[0])) && (var0[1].equals(var1[1])) && (var0[2].equals(var1[2]));
	}

	private static void removePedestalCoord(int var0)
	{
		pedestalCoords.remove(Integer.valueOf(var0));
	}

	public static float getPlayerWatchFactor()
	{
		float var0 = 1.0F;

		for (int var1 = 0; var1 < playerWoftFactor; var1++)
		{
			var0 = (float)(var0 * 0.9D);
		}

		return var0;
	}

	public static void ConsumeReagentForDuration(ItemStack var0, EntityHuman var1, boolean var2)
	{
		if (!EEProxy.isClient(var1.world))
		{
			if (consumeKleinStarPoint(var1, 32))
			{
				updatePlayerEffect(var0.getItem(), 64, var1);
			}
			else if (Consume(new ItemStack(EEItem.aeternalisFuel), var1, var2))
			{
				updatePlayerEffect(var0.getItem(), 16384, var1);
			}
			else if (Consume(new ItemStack(EEItem.mobiusFuel), var1, var2))
			{
				updatePlayerEffect(var0.getItem(), 4096, var1);
			}
			else if (Consume(new ItemStack(Block.GLOWSTONE), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 3072, var1);
			}
			else if (Consume(new ItemStack(EEItem.alchemicalCoal), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 1024, var1);
			}
			else if (Consume(new ItemStack(Item.GLOWSTONE_DUST), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 768, var1);
			}
			else if (Consume(new ItemStack(Item.SULPHUR), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 384, var1);
			}
			else if (Consume(new ItemStack(Item.COAL, 1, 0), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 256, var1);
			}
			else if (Consume(new ItemStack(Item.REDSTONE), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 128, var1);
			}
			else if (Consume(new ItemStack(Item.COAL, 1, 1), var1, false))
			{
				updatePlayerEffect(var0.getItem(), 64, var1);
			}
		}
	}

	public static void ConsumeReagent(ItemStack var0, EntityHuman var1, boolean var2)
	{
		if (consumeKleinStarPoint(var1, 32))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 4);
		}
		else if (Consume(new ItemStack(EEItem.aeternalisFuel, 1), var1, false))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 1024);
		}
		else if (Consume(new ItemStack(EEItem.mobiusFuel, 1), var1, false))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 256);
		}
		else if (Consume(new ItemStack(Block.GLOWSTONE, 1), var1, false))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 192);
		}
		else if (Consume(new ItemStack(EEItem.alchemicalCoal, 1), var1, false))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 64);
		}
		else if (Consume(new ItemStack(Item.GLOWSTONE_DUST, 1), var1, false))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 48);
		}
		else if (Consume(new ItemStack(Item.SULPHUR, 1), var1, false))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 24);
		}
		else if (Consume(new ItemStack(Item.COAL, 1, 0), var1, var2))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 16);
		}
		else if (Consume(new ItemStack(Item.REDSTONE, 1), var1, var2))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 8);
		}
		else if (Consume(new ItemStack(Item.COAL, 1, 1), var1, var2))
		{
			((ItemEECharged)var0.getItem()).setShort(var0, "fuelRemaining", ((ItemEECharged)var0.getItem()).getShort(var0, "fuelRemaining") + 4);
		}
	}

	public static boolean isLeftClickDown(EntityHuman var0, MinecraftServer var1)
	{
		if (playerLeftClick.get(var0) == null)
		{
			resetLeftClick(var0);
		}

		return playerLeftClick.get(var0).booleanValue();
	}

	public static void resetLeftClick(EntityHuman var0)
	{
		playerLeftClick.put(var0, Boolean.valueOf(false));
	}

	public static void watchTransGrid(EntityHuman var0)
	{
		playerTransGridOpen.put(var0, Boolean.valueOf(true));
	}

	public static void closeTransGrid(EntityHuman var0)
	{
		playerTransGridOpen.put(var0, Boolean.valueOf(false));
	}

	public static Boolean getTransGridOpen(EntityHuman var0)
	{
		if (playerTransGridOpen.get(var0) == null)
		{
			playerTransGridOpen.put(var0, Boolean.valueOf(false));
		}

		return playerTransGridOpen.get(var0);
	}

	public static int getMachineFactor()
	{
		return machineFactor > 16 ? 16 : machineFactor < 1 ? 1 : machineFactor;
	}

	public static boolean isNeutralEntity(Entity var0)
	{
		return ((var0 instanceof EntitySheep)) || ((var0 instanceof EntityCow)) || ((var0 instanceof EntityPig)) || ((var0 instanceof EntityChicken)) || ((var0 instanceof EntityMushroomCow)) || ((var0 instanceof EntityVillager)) || ((var0 instanceof EntityOcelot)) || ((var0 instanceof EntityWolf)) || ((var0 instanceof EntitySnowman)) || ((var0 instanceof EntityIronGolem));
	}

	public static boolean isHostileEntity(Entity var0)
	{
		return ((var0 instanceof EntityCreeper)) || ((var0 instanceof EntityZombie)) || ((var0 instanceof EntitySkeleton)) || ((var0 instanceof EntitySpider)) || ((var0 instanceof EntityCaveSpider)) || ((var0 instanceof EntityEnderman)) || ((var0 instanceof EntitySilverfish)) || ((var0 instanceof EntitySlime)) || ((var0 instanceof EntityGhast)) || ((var0 instanceof EntityMagmaCube)) || ((var0 instanceof EntityPigZombie)) || ((var0 instanceof EntityBlaze));
	}
}