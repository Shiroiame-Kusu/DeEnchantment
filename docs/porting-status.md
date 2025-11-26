# Porting Status

## Gameplay Controllers

| Legacy Listener | Purpose | Config Toggle | Java Status |
| --- | --- | --- | --- |
| AnvilListener | Handles cursed behavior when combining/repairing in anvils | `config.anvil` | ✅ Ported (AnvilController) |
| ChestLootTableListener | Applies curse translation to generated loot | `config.chestLoot` | ✅ Ported (ChestLootController) |
| EnchantListener | Converts enchanting table results into curses | `config.enchant` | ✅ Ported (EnchantController) |
| EntitySpawnListener | Applies curses to naturally spawned mobs and their gear | `config.spawn` | ✅ Ported (EntitySpawnController) |
| MerchantListener | Injects curses into villager trade results | `config.trade` | ✅ Ported (MerchantController) |
| PlayerFishListener | Adds curses to fishing loot | `config.fishing` | ✅ Ported (PlayerFishController) |
| EntityDropItemListener | Converts mob reward drops to curses | `config.reward` | ✅ Ported (RewardDropController) |
| GrindstoneListener | Cleans lore when players disenchant items | `config.grindstone` | ✅ Ported (GrindstoneController) |

## Curse Behavior Listeners

| Curse | Legacy Purpose | Status |
| --- | --- | --- |
| Protection | Increase incoming generic damage | ✅ Ported (ProtectionCurseHandler) |
| Fire_Protection | Chance to set wearer on fire when damaged | ✅ Ported (FireProtectionCurseHandler) |
| Blast_Protection | Chance to detonate explosions on hit | ✅ Ported (BlastProtectionCurseHandler) |
| Projectile_Protection | Attracts nearby projectiles toward cursed targets | ✅ Ported (ProjectileProtectionCurseHandler) |
| Feather_Falling | Amplify fall damage and apply slowness | ✅ Ported (FeatherFallingCurseHandler) |
| Respiration | Consume air faster while underwater | ✅ Ported (RespirationCurseHandler) |
| Aqua_Affinity | Underwater block breaking can fail | ✅ Ported (AquaAffinityCurseHandler) |
| Depth_Strider | Applies slowness while submerged | ✅ Ported (DepthStriderCurseHandler) |
| Frost_Walker | Converts nearby lava into temporary obsidian | ✅ Ported (FrostWalkerCurseHandler) |
| Thorns | Damages sprinting wearers randomly | ✅ Ported (ThornsCurseHandler) |
| Binding_Curse | Binds equipment to the first owner | ✅ Ported (BindingCurseHandler) |
| Sharpness | Reduces damage dealt by the attacker | ✅ Ported (SharpnessCurseHandler) |
| Smite | Reduces damage against undead mobs | ✅ Ported (SmiteCurseHandler) |
| Bane_Of_Arthropods | Reduces damage against arthropods | ✅ Ported (BaneOfArthropodsCurseHandler) |
| Knockback | Throws the attacker backward when striking | ✅ Ported (KnockbackCurseHandler) |
| Fire_Aspect | Chance to ignite the attacker on hit | ✅ Ported (FireAspectCurseHandler) |
| Flame | Greatly accelerates fired arrows uncontrollably | ✅ Ported (FlameCurseHandler) |
| Looting | Kills may yield no loot drops | ✅ Ported (LootingCurseHandler) |
| Sweeping | Sweeping attacks pull nearby mobs inward | ✅ Ported (SweepingCurseHandler) |
| Efficiency | Cancels block breaking with a level-scaled chance | ✅ Ported (EfficiencyCurseHandler) |
| Silk_Touch | Broken blocks fail to drop items | ✅ Ported (SilkTouchCurseHandler) |
| Unbreaking | Durability consumption is increased | ✅ Ported (UnbreakingCurseHandler) |
| Fortune | Block drops have a chance to vanish | ✅ Ported (FortuneCurseHandler) |
| Power | Fired arrows lose damage and speed | ✅ Ported (PowerCurseHandler) |
| Punch | Pulls struck targets toward the attacker | ✅ Ported (PunchCurseHandler) |
| Infinity | Consumes one arrow but fires multiple shots | ✅ Ported (InfinityCurseHandler) |
| Luck_Of_The_Sea | Caught items can slip off the hook | ✅ Ported (LuckOfTheSeaCurseHandler) |
| Lure | Extends the time before fish bite | ✅ Ported (LureCurseHandler) |
| Loyalty | Tridents may betray their owner and fly to others | ✅ Ported (LoyaltyCurseHandler) |
| Impaling | Increases damage to non-aquatic mobs | ✅ Ported (ImpalingCurseHandler) |
| Riptide | Rapid movement when the wielder is burning | ✅ Ported (RiptideCurseHandler) |
| Channeling | Calls punitive lightning during storms | ✅ Ported (ChannelingCurseHandler) |
| Multishot | Fires consecutive volleys from crossbows | ✅ Ported (MultishotCurseHandler) |
| Quick_Charge | Crossbow reload speed is slowed | ✅ Ported (QuickChargeCurseHandler) |
| Piercing | Projectiles rebound after hitting mobs | ✅ Ported (PiercingCurseHandler) |
| Mending | Consumes durability to grant extra XP | ✅ Ported (MendingCurseHandler) |
| Vanishing_Curse | Prevents cursed items from being dropped | ✅ Ported (VanishingCurseHandler) |
| Soul_Speed | Lets wearers sprint unnaturally fast on land | ✅ Ported (SoulSpeedCurseHandler) |
| Swift_Sneak | Slows walking speed but boosts resistance | ✅ Ported (SwiftSneakCurseHandler) |

## Command Suite

| Legacy Command | Purpose | Java Status |
| --- | --- | --- |
| /deenchantment add | Inject curses into a specified item stack | ✅ Ported (AddSubcommand) |
| /deenchantment give | Give players cursed items directly | ✅ Ported (GiveSubcommand) |
| /deenchantment migrate | Convert legacy data/configs to the new format | ✅ Ported (MigrateSubcommand) |
| /deenchantment pur | Purify player equipment | ✅ Ported (PurificationSubcommand) |
| /deenchantment random | Roll random curses onto held items | ✅ Ported (RandomSubcommand) |
| /deenchantment reload | Reload configuration files | ✅ Ported (ReloadSubcommand) |
| /deenchantment update | Toggle the update checker | ✅ Ported (UpdateSubcommand) |

## External Plugin Hooks

| Legacy Hook | Integration Purpose | Status |
| --- | --- | --- |
| EcoEnchantHook | Synchronize curse data with EcoEnchants | ✅ Ported (EcoEnchantsHook) |
| SlimeFun4Hook | Apply curses to SlimeFun-generated gear | ✅ Ported (SlimefunHook) |

### Remaining Curse Behavior Listeners

| Curse | Legacy Purpose | Status |
| --- | --- | --- |
| _All curse behavior listeners have been ported!_ |  |  |
