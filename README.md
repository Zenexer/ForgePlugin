Introduction
============

ForgePlugin offers enhancements and fixes for a variety of Tekkit Classic components.  It is comprised of one plugin and several mod patches:

* **ForgePluginBukkit** is the core plugin, and runs via Bukkit.  It provides a chunk management system, item/block banning, and entity overflow protection.  This generally allows servers to stay running for a lot longer without crashing.
* **ForgePluginEquivalentExchange** is a patch for EquivalentExchange.  It fixes some serious memory leaks and prevents most tools from working in regions protected by the likes of WorldGuard, LWC, etc.  It also fixes the infamous RM Furnace dupe glitch, and provides a rewrite for Alchemy Tablets.
* **ForgePluginRailcraft** is a patch for Railcraft that fixes a serious bug with tank carts that allows players to both dupe items and crash servers.
* **ForgePluginRedpower** is a patch for Redpower that provides dramatic performance enhancements for Pneumatic Tubes and similar mechanisms.  Players will no longer lag, crash, or disconnect near large factories, server performance will increase, and network usage will drop.
* **ForgePluginTubeStuff** is a patch for TubeStuff to fix a dupe glitch with Automatic Crafting Tables, MkII by correcting their behavior when they break.


Installation
============

1. Download and extract the ForgePlugin `zip` file.
2. Add the `jar` files in the newly extracted `plugins` folder to your server's `plugins` folder.
3. Add the `jar` files in the newly extracted `mods` folder to your server's `mods` folder.
4. Delete the old mods from your server's `mods` folder that have been replaced:
	* Equivalent Exchange
	* Railcraft
	* Redpower
	* TubeStuff
5. Restart your server.
6. Edit the `yml` files in your server's `plugins/ForgePlugin` folder as appropriate.  Missing entries will be reset to the default value.
7. Run `/fp reload` to load the new configuration files.


Commands
========

* **/forgeplugin reload** or **/fp r**: Reloads ForgePlugin's configuration files without restarting the server.
* **/forgeplugin version** or **/fp v**: Displays version information about ForgePlugin.
* **/chunkcleanup** or **/cc**: Passively unload all chunks that aren't near online players, because Tekkit isn't good at doing this automatically.  Persistent chunks will remain loaded.  This runs automatically every five minutes by default.
* **/ispersistent** or **/isp**: Determines whether the current chunk is part of a persistent region.  Also displays chunk coordinates for current position.


Permissions
===========

forgePlugin integrates with Vault, and will fall back to SuperPerms if Vault is not present.  ForgePlugin is primarily tested with GroupManager, but should work with most Vault-compatible permission systems.

We do not support PermissionsEx (PEx) integration, and discourage its use due to instability, particularly with mods.

* **forgeplugin.forgeplugin**: Required to use the `/forgeplugin` command and all sub-commands.
* **forgeplugin.forgeplugin.reload**: Required to use the `/forgeplugin reload` sub-command.  Only useful if combined with `forgeplugin.forgeplugin`.
* **forgeplugin.forgeplugin.version**: Required to use the `/forgeplugin version` sub-command.  Only useful if combined with `forgeplugin.forgeplugin`.
* **forgeplugin.chunkcleanup**: Required to use the `/chunkcleanup` command.
* **forgeplugin.ispersistent**: Required to use the `/ispersistent` command.


Bukkit Plugin Features
======================

* Persistently load chunks or ranges of chunks (a "chunk loader")
* Banned items/blocks; various categories:
	* all: will be removed under any circumstances
	* use: prevents right-click
	* craft: normal crafting/smelting won't work (might still work via alternative crafting methods, like ACT MkII)
	* build: block can't be placed, and existing blocks will be removed upon interaction
* Entity overflow: removes extraneous entities--such as item drops--in a chunk when there are too many
* Prevent items from dropping too quickly (disabled by default): used to counter tank cart-like glitches that spew items.  All known glitches are fixed as of writing, so unnecessary, and often has false-positives.  Can be used to prevent drop parties.
* Chunk manager: replaces the server's chunk loading/unloading mechanism, since the default doesn't work very well; helps keep memory usage low, and fixes server-side chunk loading glitches.  Unfortunately, it can't fix the client-side bugs.
* Support for permissions via Vault--not required, but highly recommended


Entity Overflow
---------------

Entity overflow removes extraneous entities, particularly item drops, when there are too many in a single chunk.

Entity overflow prioritizes entities based on type.  If too many entities exist after clearing the current level, it moves onto the next level and clears again.  Levels:
	1. Item drops and experience orgs
	2. Vehicles
	3. Projectiles, primed TNT, falling blocks, lighting
	4. Everything except humans and NPCs
	5. Everything except humans

Servers with a lot of players can easily break this system if all of the players are teleported into the same area.  To prevent problems, the scanner first checks how many players are in the chunk.  If this number is greater than half of the entity threshold, the threshold will temporarily be increased to the number of players in the chunk _plus_ the configured threshold.


Mod Patches
===========


Equivalent Exchange
-------------------

Fixes:

* Black Hole Band
	* Dupe bug
	* Picking up items too quickly
* RM Furnace
	* Dupe bug
	* Improper input/output data for tubes/pipes
* Transmutation Tablet
	* Completely reprogrammed
	* Items are pre-sorted based on EMC, not sorted every 10 ticks, and then for every player with a tablet open: much faster.
	* Sorting is reliable: it will always be the same, even if items are of the same EMC value.
	* Tablets update instantly, not every 10 ticks
	* Tablets update only when necessary, not every 10 ticks
	* Dupe bug (fixed by instant updates)
	* Lots of minor bugs (residual EMC, etc.)
* RM Tools, DM Tools, Philosopher's Stone
	* Added Bukkit block events for special abilities: Bukkit plugins like LWC and Worldguard can cancel these events, so EE tools will no longer bypass region protection, locks, etc.
	* Unified cost system for releasing charges/using abilities; some abilities were "freebies", presumably by accident
	* Too many orbs (loot balls) dropping; couldn't be picked up
	* Miscellaneous logic reworking and additional polymorphism, including:
		* DM axe wasn't classified as a DM tool, and RM axe wasn't classified as an RM tool.
		* Unified common processes into new ItemEESuperTool class
* Removed processing of Watch of Flowing Time: wasted an unnecessary amount of processing power per tick, and blocked anyway by most servers
* Fixed major memory leaks: this is probably why tekkit needed to be restarted so frequently, in addition to poor chunk management.  Memory leaks occurred every time a player...
	* ...disconnected: HashMap fields in EEBase; prevented garbage collection of a lot, including EntityPlayer objects, and pretty much anything involving the player
	* ...opened a Transmutation Tablet: all TransTabletData instances persistently referenced in a static List field, never removed; prevented garbage collection of TransTabletData objects
	* ...opened an Alchemy Bag: all AlchemyBagData instances persistently referenced in a static List field, never removed; prevented garbage collection of AlchemyBagData objects
	* ...opened a Mercurial Eye: all MercurialEyeData instances persistently referenced in a static List field, never removed; prevented garbage collection of MercurialEyeData objects


To Do:

* Add Bukkit block events to explosives, rings, etc.
* Fix pedestal (low priority).


Railcraft
---------

Fixes:

* Tank Cart: dupe bug
* Tank Cart: spewing items bug, used for crashing both players and servers

To Do:

* Investigate further for more bugs: I'm not a Railcraft expert.


Redpower
--------

Fixes:

* Pneumatic Tubes, Redstone Tubes, and Magtubes
	* Prevent disconnect.overflow
	* Factories can be much larger and more complex without slowing down the server and players
	* Hundreds of stacks can be transported though a single tube block simultaneously without any trouble
	* Animations suffer slightly: only one stack will be seen per tube block

To Do:

* Prevent disconnect.overflow (and presumably excessive network I/O) caused by pneumatic tubes and related.
* Trigger proper Bukkit events for right-clicking timers and such.
* Investigate client-side chunk rendering issues in the presence of most Redpower machines, such as filters. (Investigated; fix will require a lot of work, so low priority)


TubeStuff
---------

Fixes:

* ACT (Automatic Crafting Table) MkII should only drop items when broken by a player, not when removed via WorldEdit, a Block Breaker, etc.
	* Dupe glitch in the case of Block Breaker

To Do:

* Other containers (including those in other mods) sometimes use code similar to that which was causing the problem with ACT MkII: investigate.


BuildCraft
----------

To Do:

* Reprogram pipes.  Until then, use Redpower's pneumatic tubes and derivatives.


License
=======

Copyright (c) 2012, Paul Buonopane. All rights reserved.
"Earth2Me", "Zenexer", and "ForgePlugin" are trademarks of Paul Buonopane.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Portions of code are derived from other sources.  Though heavily modified, and
in many cases, entirely rewritten, the original forms belong to their
respective owners.  Often, the licensing for this original code remains
unclear. Please be respectful of the wishes of the original mod authors,
regardless of legal obligations. They put a lot of effort into the mods that
form Tekkit.