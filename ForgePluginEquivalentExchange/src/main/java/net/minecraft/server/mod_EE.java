package net.minecraft.server;

import ee.*;
import ee.core.AlchemyBagPickupHandler;
import ee.core.ConnectionHandler;
import ee.core.PickupHandler;
import ee.network.PacketHandler;
import forge.DimensionManager;
import forge.MinecraftForge;
import forge.NetworkMod;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class mod_EE extends NetworkMod
{
	public static final String MOD_NAME = "Equivalent Exchange 2";
	public static final String CHANNEL_NAME = "EE2";
	public static final String SOUND_RESOURCE_LOCATION = "/ee/sound/";
	public static final String SOUND_PREFIX = "ee.sound.";
	private static int tickCounter = 0;
	private static mod_EE instance;
	private int blackListTimer;
	private int rmArmorClearSlot;
	public static int pedestalModelID;
	public static int chestModelID;
	public static HashMap playerShockwave = new HashMap();

	@Override
	public void load()
	{
		instance = this;
		MinecraftForge.versionDetect("Equivalent Exchange 2", 3, 3, 7);
		ModLoader.setInGameHook(this, true, true);
		MinecraftForge.registerConnectionHandler(new PacketHandler());
		MinecraftForge.setGuiHandler(this, new EEGuiHandler());
		EEBase.init(this);
		MinecraftForge.registerEntity(EntityPhilosopherEssence.class, this, 143, 300, 2, true);
		MinecraftForge.registerEntity(EntityWaterEssence.class, this, 144, 300, 2, true);
		MinecraftForge.registerEntity(EntityLavaEssence.class, this, 145, 300, 2, true);
		MinecraftForge.registerEntity(EntityWindEssence.class, this, 146, 300, 2, true);
		MinecraftForge.registerEntity(EntityPyrokinesis.class, this, 147, 300, 2, true);
		MinecraftForge.registerEntity(EntityGrimArrow.class, this, 148, 300, 2, true);
		MinecraftForge.registerEntity(EntityLootBall.class, this, 149, 300, 2, true);
		MinecraftForge.registerEntity(EntityNovaPrimed.class, this, 150, 300, 2, true);
		MinecraftForge.registerEntity(EntityHyperkinesis.class, this, 151, 300, 2, true);

		EEItem.init();
		EEBlock.init();

		EEMaps.InitAlchemicalValues();
		EEMaps.InitFlyingItems();
		EEMaps.InitFuelItems();
		EEMaps.InitFireImmunities();
		EEMaps.InitDurationEffectItems();
		EEMaps.InitEERecipes();
		EEMaps.InitRepairRecipes();
		EEMaps.InitChestItems();
		EEMaps.InitChargeditems();
		EEMaps.InitWoodAndLeafBlocks();
		EEMaps.InitPedestalItems();
		EEMaps.InitModItems();
		EEMaps.InitOreBlocks();
		EEMaps.InitBlacklist();
		EEMaps.InitMetaData();

		pedestalModelID = ModLoader.getUniqueBlockModelID(this, true);
		chestModelID = ModLoader.getUniqueBlockModelID(this, true);

		blackListTimer = 100;

		MinecraftForge.registerPickupHandler(new PickupHandler());
		MinecraftForge.registerPickupHandler(new AlchemyBagPickupHandler());
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());
	}

	@Override
	public String getVersion()
	{
		return String.format("%d.%d.%d.%d", 1, 4, 6, 5);
	}

	@Override
	public boolean onTickInGame(MinecraftServer var1)
	{
		EEProxy.Init(var1, this);
		World[] var2 = DimensionManager.getWorlds();
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; var4++)
		{
			final World var5 = var2[var4];

			@SuppressWarnings("unchecked")
			final List<EntityHuman> players = var5.players;

			onTickInGame(var1, players, var5);
		}

		return true;
	}

	public boolean onTickInGame(MinecraftServer var1, List<EntityHuman> players, World var3)
	{
		if (!EEBase.externalModsInitialized)
		{
			for (final BaseMod mod : ModLoader.getLoadedMods())
			{
				final String name = mod.toString();

				if (name.contains("mod_IC2"))
				{
					EEAddonIC2.initialize();
				}
				else if (name.contains("mod_RedPowerCore"))
				{
					EEAddonRP2.initBase();
				}
				else if (name.contains("mod_RedPowerWorld"))
				{
					EEAddonRP2.initBase();
					EEAddonRP2.initWorld();
				}
				else if (name.contains("mod_BuildCraftEnergy"))
				{
					EEAddonBC.initialize();
				}
				else if (name.contains("mod_Forestry"))
				{
					EEAddonForestry.initialize();
				}
			}

			EEBase.externalModsInitialized = true;
		}

		EEBase.validatePedestalCoords(var3);

		if (tickCounter % 10 == 0)
		{
			//doTransGridUpdates(var2);
			//doWatchCheck(players, var3);

			tickCounter = 0;
		}

		doFlightCheck(players, var3);

		for (final EntityHuman player : players)
		{
			//doGemPowers(player);
			doEquipCheck(player, var3);
			doFireImmuneCheck(player);
		}

		tickCounter++;
		return true;
	}

	private void doTransGridUpdates(List<EntityHuman> var1)
	{
		for (final EntityHuman var3 : var1)
		{
			if (EEBase.getTransGridOpen(var3).booleanValue())
			{
				EEProxy.getTransData(var3).onUpdate(var3.world, var3);
			}
		}
	}

	private void doGemPowers(EntityHuman var1)
	{
		EEBase.updatePlayerToggleCooldown(var1);

		for (int var2 = 0; var2 <= 3; var2++)
		{
			if (var1.inventory.armor[var2] != null)
			{
				ItemStack var3 = var1.inventory.armor[var2];

				if ((var2 == 2) && ((var3.getItem() instanceof ItemRedArmorPlus)) && (var1.hasEffect(MobEffectList.POISON)))
				{
					var1.d(var1.getEffect(MobEffectList.POISON));
				}

				if ((var2 == 1) && ((var3.getItem() instanceof ItemRedArmorPlus)))
				{
					List var4 = var1.world.a(EntityLiving.class, AxisAlignedBB.b(var1.locX - 5.0D, var1.locY - 5.0D, var1.locZ - 5.0D, var1.locX + 5.0D, var1.locY + 5.0D, var1.locZ + 5.0D));

					for (int var5 = 0; var5 < var4.size(); var5++)
					{
						Entity var6 = (Entity)var4.get(var5);

						if ((!(var6 instanceof EntityHuman)) && ((var6.motX > 0.0D) || (var6.motZ > 0.0D)))
						{
							var6.motX *= 0.1000000014901161D;
							var6.motZ *= 0.1000000014901161D;
						}
					}
				}

				if ((var2 == 0) && ((var3.getItem() instanceof ItemRedArmorPlus)))
				{
					if ((!var1.isSprinting()) && (EEBase.getPlayerArmorMovement(var1)))
					{
						var1.setSprinting(true);
					}

					if (EEBase.getPlayerArmorMovement(var1))
					{
						var1.am = ((float)(var1.am + 0.04000000000000001D));

						if (var1.am > 0.3F)
						{
							var1.am = 0.3F;
						}

						if (var1.motY > 1.0D)
						{
							var1.motY = 1.0D;
						}

						if ((var1.motY < 0.0D) && (!var1.isSneaking()))
						{
							var1.motY *= 0.88D;
						}
					}

					if (var1.fallDistance > 0.0F)
					{
						var1.fallDistance = 0.0F;
					}
				}

				if ((var2 == 3) && ((var3.getItem() instanceof ItemRedArmorPlus)) && (var1.getAirTicks() < 20))
				{
					var1.setAirTicks(20);
				}
			}
		}
	}

	private void doShockwave(EntityHuman var1)
	{
		List var2 = var1.world.a(EntityLiving.class, AxisAlignedBB.b(var1.locX - 7.0D, var1.locY - 7.0D, var1.locZ - 7.0D, var1.locX + 7.0D, var1.locY + 7.0D, var1.locZ + 7.0D));

		for (int var3 = 0; var3 < var2.size(); var3++)
		{
			Entity var4 = (Entity)var2.get(var3);

			if (!(var4 instanceof EntityHuman))
			{
				var4.motX += 0.2D / (var4.locX - var1.locX);
				var4.motY += 0.05999999865889549D;
				var4.motZ += 0.2D / (var4.locZ - var1.locZ);
			}
		}

		List var7 = var1.world.a(EntityArrow.class, AxisAlignedBB.b((float)var1.locX - 5.0F, var1.locY - 5.0D, (float)var1.locZ - 5.0F, (float)var1.locX + 5.0F, var1.locY + 5.0D, (float)var1.locZ + 5.0F));

		for (int var8 = 0; var8 < var7.size(); var8++)
		{
			Entity var5 = (Entity)var7.get(var8);
			var5.motX += 0.2D / (var5.locX - var1.locX);
			var5.motY += 0.05999999865889549D;
			var5.motZ += 0.2D / (var5.locZ - var1.locZ);
		}

		List var10 = var1.world.a(EntityFireball.class, AxisAlignedBB.b((float)var1.locX - 5.0F, var1.locY - 5.0D, (float)var1.locZ - 5.0F, (float)var1.locX + 5.0F, var1.locY + 5.0D, (float)var1.locZ + 5.0F));

		for (int var9 = 0; var9 < var10.size(); var9++)
		{
			Entity var6 = (Entity)var10.get(var9);
			var6.motX += 0.2D / (var6.locX - var1.locX);
			var6.motY += 0.05999999865889549D;
			var6.motZ += 0.2D / (var6.locZ - var1.locZ);
		}
	}

	private void doWatchCheck(List<EntityHuman> var1, World var2)
	{
		int var3 = 0;

		for (final EntityHuman var5 : var1)
		{
			for (final ItemStack var9 : EEBase.quickBar(var5))
			{
				final Item item = var9.getItem();
				if (var9 != null && item instanceof ItemWatchOfTime && (var9.getData() & 0x1) == 1 && EEBase.getPlayerEffect(item, var5) > 0)
				{
					var3 += ((ItemEECharged)var9.getItem()).chargeLevel(var9) + 1;
					EEBase.playerWatchMagnitude.put(var5, ((ItemEECharged)var9.getItem()).chargeLevel(var9) + 1);
				}
			}
		}

		EEBase.playerWoftFactor = var3;
	}

	private void doFlightCheck(List var1, World var2)
	{
		Iterator var3 = var1.iterator();

		while (var3.hasNext())
		{
			EntityHuman var4 = (EntityHuman)var3.next();

			if (((EntityPlayer)var4).itemInWorldManager.isCreative())
			{
				return;
			}

			boolean var5 = false;
			boolean var6 = false;
			boolean var7 = false;
			boolean var8 = false;
			ItemStack[] var9 = EEBase.quickBar(var4);
			int var10 = var9.length;

			for (int var11 = 0; var11 < var10; var11++)
			{
				ItemStack var12 = var9[var11];

				if ((var12 != null) && (EEMaps.isFlyingItem(var12.id)))
				{
					if (var12.getItem() == EEItem.arcaneRing)
					{
						var5 = true;
						var4.abilities.canFly = true;
					}
					else if ((var12.getItem() == EEItem.evertide) && (EEBase.isPlayerInWater(var4)))
					{
						var7 = true;
						var4.abilities.isFlying = true;
					}
					else if ((var12.getItem() == EEItem.volcanite) && (EEBase.isPlayerInLava(var4)))
					{
						var6 = true;
						var4.abilities.isFlying = true;
					}
					else if (var12.getItem() == EEItem.swiftWolfRing)
					{
						var8 = true;
						var4.abilities.canFly = true;
					}
				}
			}

			if ((var8) && ((var7) || (var6) || (var5)))
			{
				var8 = false;
				disableSWRG(var4);
			}
			else if ((var8) && (!var6) && (!var7) && (!var5))
			{
				if (var4.abilities.isFlying)
				{
					forceEnableSWRG(var4);
				}
				else
				{
					disableSWRG(var4);
				}
			}

			if ((!var8) && (!var6) && (!var7) && (!var5))
			{
				var4.abilities.canFly = false;
				var4.abilities.isFlying = false;
				var4.updateAbilities();
			}
			else
			{
				var4.abilities.canFly = true;
			}
		}
	}

	private void forceEnableSWRG(EntityHuman var1)
	{
		ItemStack[] var2 = EEBase.quickBar(var1);
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; var4++)
		{
			ItemStack var5 = var2[var4];

			if ((var5 != null) && (var5.getItem() == EEItem.swiftWolfRing))
			{
				if (!((ItemEECharged)var5.getItem()).isActivated(var5))
				{
					((ItemEECharged)var5.getItem()).doToggle(var5, var1.world, var1);
					return;
				}

				return;
			}
		}
	}

	private void disableSWRG(EntityHuman var1)
	{
		ItemStack[] var2 = EEBase.quickBar(var1);
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; var4++)
		{
			ItemStack var5 = var2[var4];

			if ((var5 != null) && (var5.getItem() == EEItem.swiftWolfRing))
			{
				if (var1.abilities.isFlying)
				{
					if (((ItemEECharged)var5.getItem()).isActivated(var5))
					{
						if (EEBase.getPlayerEffect(var5.getItem(), var1) <= 0)
						{
							((ItemEECharged)var5.getItem()).ConsumeReagent(var5, var1, false);
						}

						if (EEBase.getPlayerEffect(var5.getItem(), var1) <= 0)
						{
							((ItemEECharged)var5.getItem()).doToggle(var5, var1.world, var1);
						}
					}
				}
				else if (((ItemEECharged)var5.getItem()).isActivated(var5))
				{
					((ItemEECharged)var5.getItem()).doToggle(var5, var1.world, var1);
				}
			}
		}
	}

	private void doEquipCheck(EntityHuman var1, World var2)
	{
		ItemStack[] var3 = EEBase.quickBar(var1);
		int var4 = var3.length;

		for (int var5 = 0; var5 < var4; var5++)
		{
			ItemStack var6 = var3[var5];
			if (var6 == null)
			{
				continue;
			}

			final Item item = var6.getItem();

			if (item != null && item instanceof ItemEECharged)
			{
				final ItemEECharged chargedItem = (ItemEECharged)item;

				if (var6 == var1.U())
				{
					chargedItem.doHeld(var6, var2, var1);
				}

				chargedItem.doPassive(var6, var2, var1);

				if (((var6.getData() & 0x1) == 1) && (EEMaps.hasDurationEffect(var6.getItem())))
				{
					if ((var6.getItem() instanceof ItemWatchOfTime))
					{
						if (EEBase.getPlayerEffect(var6.getItem(), var1) > 0)
						{
							EEBase.updatePlayerEffect(var6.getItem(), EEBase.getPlayerEffect(var6.getItem(), var1) - (chargedItem.chargeLevel(var6) + 1) * (chargedItem.chargeLevel(var6) + 1), var1);
						}
					}
					else if (EEBase.getPlayerEffect(var6.getItem(), var1) > 0)
					{
						EEBase.updatePlayerEffect(var6.getItem(), EEBase.getPlayerEffect(var6.getItem(), var1) - 1, var1);
					}

					if (EEBase.getPlayerEffect(var6.getItem(), var1) <= 0)
					{
						chargedItem.ConsumeReagent(var6, var1, false);
					}

					if (EEBase.getPlayerEffect(var6.getItem(), var1) <= 0)
					{
						chargedItem.doToggle(var6, var2, var1);
					}
					else
					{
						chargedItem.doActive(var6, var2, var1);
					}
				}

				if (((var6.getData() & 0x2) == 2) && ((var6.getItem() instanceof ItemSwiftWolfRing)))
				{
					if (EEBase.getPlayerEffect(var6.getItem(), var1) > 0)
					{
						EEBase.updatePlayerEffect(var6.getItem(), EEBase.getPlayerEffect(var6.getItem(), var1) - 2, var1);
					}

					if (EEBase.getPlayerEffect(var6.getItem(), var1) <= 0)
					{
						chargedItem.ConsumeReagent(var6, var1, false);
					}

					if (EEBase.getPlayerEffect(var6.getItem(), var1) <= 0)
					{
						chargedItem.doToggle(var6, var2, var1);
					}
					else
					{
						chargedItem.doActive(var6, var2, var1);
					}
				}
			}
		}
	}

	private void doFireImmuneCheck(EntityHuman var1)
	{
		boolean var2 = false;

		for (int var3 = 0; var3 < 9; var3++)
		{
			if ((var1.inventory.items[var3] != null) && (EEMaps.isFireImmuneItem(var1.inventory.items[var3].id)))
			{
				var2 = true;
			}
		}

		for (int var3 = 0; var3 < 4; var3++)
		{
			if ((var1.inventory.armor[var3] != null) && (EEMaps.isFireImmuneArmor(var1.inventory.armor[var3].id)))
			{
				var2 = true;
			}
		}

		var1.fireProof = var2;
	}

	@Override
	public boolean clientSideRequired()
	{
		return true;
	}

	@Override
	public boolean serverSideRequired()
	{
		return false;
	}

	public static BaseMod getInstance()
	{
		return instance;
	}
}