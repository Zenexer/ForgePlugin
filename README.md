Installation
============

1. Add ForgePluginBukkit.jar to your plugins folder.
2. Add the mod_*.jar files to your mods folder.
3. Remove original copies of mods that have been replaced by the mod_*.jar files.  This step is important: if you encounter errors, you most likely forgot to do this, or you missed a mod.


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


Tips to Future Modders
======================

Don't repeat the mistakes of the past.  Before you start publishing mods, research these topics:


Important Programming Concepts/Tools
------------------------------------

* Polymorphism in Java
* Java best practices
* Events in Java
* Lombok
* Maven
* Multitasking
	* Process calculi
	* Asynchronous versus synchronous
	* Multithreading
* Version Control Systems (VCS)
	* git
	* subversion (SVN)
	* GitHub (public: free and most popular/most features, private: paid)
	* BitBucket (public: free, private: paid)
* Javadoc


Additional Software Project Tools
---------------------------------

* Continuous Integration (CI)
	* TeamCity
	* Jenkins
* Bug tracking
	* Assembla
* Documentation
	* Wikia
	* GitHub
* Licensing
	* Creative Commons: Make yourself a fitting license based on your preferences
	* BSD: Restrictive, most rights reserved
	* GPL: Restrictive, very copyleft and anti-abuse
	* LGPL: Similar to GPL, but less restrictive; possibly more appropriate for mods
	* Plenty more: look up "open source licenses"


Additional Programming Tips
---------------------------

* If you're copying and pasting code, you're doing it wrong.  Zero exceptions.  See: Polymorphism
* Don't use multiple HashMap objects with the same key.  Use one HashMap, and create your own class to store all of the would-be values.
* It's rare that you should be doing something every _n_ ticks.  If you're doing something _every_ tick, you're definitely on the wrong track: you're opening up your code to performance issues and major exploits.  Instead, do that something every time an event occurs, such as every time an inventory slot is changed.  Your code will be more responsive, more reliable, less exploitable, and more efficient.
* Consider measuring time in milliseconds instead of ticks.  If you want to run a custom furnace for 20 seconds, check how many milliseconds have elapsed every 40 or so ticks.  If 20_000 or more have elapsed, the time has completed.  This way, your mod is only dependent on TPS for precision, not accuracy.  By the way, if you're using a new enough version of Java, you can type a number like 20_000--with the underscore.  It's a good practice, since it's more legible.  There is a consistency issue with this millisecond-versus-tick approach, but I suspect that Minecraft will convert to real time at some point anyway.
* Don't keep calling an accessor method to obtain a value repeatedly.  Call it once, store the value in a final, method-scoped variable, and use the variable.  Example: `final Server server = plugin.getServer();`
* Never pass `this` to a field or method in a constructor.  If you're not creating a memory leak, you're at least sending a method an uninitialized object.  Using it as a qualifier is fine.
* If you are storing all instances of a particular type in a static array or Collection (List, Set, HashMap, etc.), be wary of memory leaks.  You **must** remove each instance from the static array/Collection when you are done with it; otherwise, the reference persists, and keeps instance (and anything _it_ references, and so on) stuck in memory.  If you are using, say, an EntityPlayer object as a key, this can be quite a significant leak.
* Make use of multiple packages to organize your code.
* Try to do things once and cache the results, rather than recalculating the same data over and over again.
* Don't take shortcuts, particularly with inventories/containers.  There are a lot of opportunities for exploitable bugs here.
* Try to add support for other mods.  Even if you don't personally like the other mods, many of your users likely will.
* Make everything as configurable as possible--but only check the configuration values once, and cache them in a field until a reload/restart occurs.
* Case-sensitivity in commands, which is Java's default behavior, tends to cause confusion for players.
* If you're returning a Collection from a method, you should probably make it immutable using a method such as `<T> List<T> Collections.unmodifiableList(List<? extends T>)`.


Controversial Tips in Ethics
----------------------------

Take these with a grain of salt.  They are not factual.

* It might be easier to use NetBeans than Eclipse if you're working on a team.  It will enforce stricter coding requirements by default, and will take a raw Maven/Ant project without additional project files.  This saves each member from needing to initialize their own project, because project files from an Eclipse workspace shouldn't be checked into a repository.  On the downside, MCP is designed for Eclipse, so you'll need to know your way around Maven and/or Ant to automate everything.
* Consider publishing your source code under an open source license on a site such as GitHub, so people like me can fix your code and send it back to you, instead of publishing a separate fix out of your control.  You are not the ultimate programmer, and if you don't publish your source code openly, you are giving up the opportunity to have tens/hundreds of other programmers fix your mistakes, or even add new features.  Remember, you're editing someone else's software--and in shady legal territory, despite permission--so don't get all high and mighty about us modifying yours.  Besides, you still retain any rights to the software, though I'm still unclear just what rights modders and plugin developers like me have.  I'm not a lawyer.
* Don't follow the coding practices of an existing mod(der), because all that I've seen are deplorable.  Some plugins are fine, but it varies widely.
* Most mods that I've seen have one big bright side: their usability.  No matter how awesome your mod is, if it's not easy and intuitive to use, nobody is going to bother.
* Just because you call it a "beta" or "alpha" does not mean that people won't mind when you crash their servers.


License
=======

Copyright (c) 2012, Paul Buonopane.  All rights reserved.  "Earth2Me", "Zenexer", and "ForgePlugin" are trademarks of Paul Buonopane.

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