package io.github.JoltMuz.UHC;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor
{
	World world;
	
	private int minX = -2000;
	private int maxX = 2000;
	private int minZ = -2000;
    private int maxZ = 2000;
	public int getMinX() {
		return minX;
	}
	public void setMinX(int minX) {
		this.minX = minX;
	}
	public int getMaxX() {
		return maxX;
	}
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}
	public int getMinZ() {
		return minZ;
	}
	public void setMinZ(int minZ) {
		this.minZ = minZ;
	}
	public int getMaxZ() {
		return maxZ;
	}
	public void setMaxZ(int maxZ) {
		this.maxZ = maxZ;
	}
	
	private int pvpTime = 600;
	public int getPvpTime() {
		return pvpTime;
	}
	public void setPvpTime(int minX) {
		this.pvpTime = minX;
	}
	
	private int borderTime = 1200;
	public int getBorderTime() {
		return borderTime;
	}
	public void setBorderTime(int minX) {
		this.borderTime = minX;
	}
	
	private static double initialBorderSize = 2000;
	private static double finalBorderSize = 100;
	private static long borderShrinkTime = 600;
	public static double getInitialBorderSize() {
		return initialBorderSize;
	}
	public void setInitialBorderSize(double size) {
		initialBorderSize = size;
	}
	public static double getFinalBorderSize() {
		return finalBorderSize;
	}
	public void setFinalBorderSize(double size) {
		finalBorderSize = size;
	}
	public static long getBorderShrinkTime() {
		return borderShrinkTime;
	}
	public void setBorderShrinkTime(long time) {
		borderShrinkTime = time;
	}

	private static int deathmatchTime = 2400;
	public static int getDeathmatchTime() {
		return deathmatchTime;
	}
	public static void setDeathmatchTime(int deathmatchTime) {
		Commands.deathmatchTime = deathmatchTime;
	}

    
    private BukkitRunnable task;
    
    private static boolean ongoing;
    public static boolean isOngoing()
    {
    	return ongoing;
    }
    
	private static ArrayList<String> alivePlayers = new ArrayList<>();
	public static ArrayList<String> getAlivePlayers() 
	{
		return alivePlayers;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if (!sender.isOp())
		{
			sender.sendMessage(message_notOP);
			return true;
		}
		if (sender instanceof Player)
		{
			world = ((Player) sender).getWorld();
		}
		if (args.length > 0 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")))
		{
			sendCommandHelp(sender);
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("start"))
		{
			sender.sendMessage(message_starting);
			conductInitialIteration();
			sender.sendMessage(message_teleported);
			startTask();
			sender.sendMessage(message_timerStarted);
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("cancel"))
		{
			cancel();
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("forcestop"))
		{
			task.cancel();
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("settings"))
		{
			settingsCommand(sender, args);
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("disqualify")) 
		{
			disqualifyCommand(sender, args);
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("revive")) 
		{
			reviveCommand(sender, args);
		}
		else
		{
			sendCommandHelp(sender);
		}
		return true;
	}
	private void cancel()
	{
		task.cancel();
		for (Player player : Listeners.spectators)
		{
			Listeners.removeSpectating(player);
		}
		Listeners.spectators.clear();
		alivePlayers.clear();
		for (Player player: Bukkit.getOnlinePlayers())
		{
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
		}
	}
	
	private void conductInitialIteration() {
	        
	    for (Player player : Bukkit.getOnlinePlayers()) 
	    {
	    	
	    	if (player.isOp())
	    	{
	    		continue;
	    	}
	    	teleportedPlayers += 1;
	    	alivePlayers.add(player.getName());
	    	
	    	GiveStarterItems(player);
	    	if (world == null)
	    	{
	    		world = player.getWorld();
	    	}
	    		        
	        // Find a suitable random location that is not in water and teleport
	        Location randomLocation = findSuitableLocation(world, minX, maxX, minZ, maxZ);
	        
	        if (randomLocation != null) 
	        {
	            player.teleport(randomLocation, TeleportCause.PLUGIN);
	        }
	    }
	}

	private Location findSuitableLocation(World world, int minX, int maxX, int minZ, int maxZ) {
	    int maxAttempts = 100; // Limit the number of attempts to find a suitable location
	    
	    for (int attempt = 0; attempt < maxAttempts; attempt++) {
	        int randomX = minX + (int) (Math.random() * (maxX - minX + 1));
	        int randomZ = minZ + (int) (Math.random() * (maxZ - minZ + 1));
	        int coordinateY = world.getHighestBlockYAt(randomX, randomZ) + 1;
	        Location randomLocation = new Location(world, randomX, coordinateY, randomZ);
	        
	        if (!randomLocation.getBlock().isLiquid()) {
	            return randomLocation; // Found a suitable location
	        }
	    }
	    
	    return null; // Couldn't find a suitable location after maxAttempts
	}
	
	private void GiveStarterItems(Player player)
	{
		ItemStack pickaxe = new ItemStack(Material.STONE_PICKAXE);
		pickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
		player.getInventory().addItem(pickaxe);

		ItemStack axe = new ItemStack(Material.STONE_AXE);
		axe.addEnchantment(Enchantment.DIG_SPEED, 1);
		player.getInventory().addItem(axe);

		ItemStack shovel = new ItemStack(Material.STONE_SPADE);
		shovel.addEnchantment(Enchantment.DIG_SPEED, 1);
		player.getInventory().addItem(shovel);

		ItemStack leather = new ItemStack(Material.LEATHER);
		player.getInventory().addItem(leather);

		ItemStack sugarcanes = new ItemStack(Material.SUGAR_CANE,3);
		player.getInventory().addItem(sugarcanes);
	}
	
	private int timeToPvp;
	private int timeToBorder;
	private int timeToDeathmatch;
	
	public void startTask() {
	    if (task != null) {
	        task.cancel(); // Cancel the previous task if running
	    }
	    ongoing = true;
	    
	    timeToPvp = getPvpTime();
        timeToBorder = getBorderTime();
        timeToDeathmatch = getDeathmatchTime();
	    
	    WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(getInitialBorderSize());
	    
	    task = new BukkitRunnable() {
	        @Override
	        public void run() {
	            timeToPvp--;
	            timeToBorder--;
	            timeToDeathmatch--;

	            if (timeToPvp > 0 && timeToBorder > 0)
	            {
	            	ActionBar.sendToAll(ChatColor.YELLOW + "PvP: " + formatSeconds(timeToPvp) + ChatColor.GOLD + " Border: " + formatSeconds(timeToBorder));     
	            }
	            else if (timeToBorder > 0 && timeToDeathmatch > 0)
	            {
	            	ActionBar.sendToAll(ChatColor.GOLD + " Border: " + formatSeconds(timeToBorder) + ChatColor.RED + " Deathmatch: " + formatSeconds(timeToDeathmatch));     
	            }
	            else if (timeToDeathmatch > 0)
	            {
	            	ActionBar.sendToAll(ChatColor.GOLD + " Border Size: " + (int) border.getSize()/2 + ChatColor.RED + " Deathmatch: " + formatSeconds(timeToDeathmatch));  
	            }
	            Location spawnLocation = world.getSpawnLocation();
	            if (timeToPvp == 3)
	            {
	                Bukkit.broadcastMessage(message_pvpEnabledBroadcast3);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000f, 1);
	            }
	            if (timeToPvp == 2)
	            {
	                Bukkit.broadcastMessage(message_pvpEnabledBroadcast2);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000f, 1);
	            }
	            if (timeToPvp == 1)
	            {
	                Bukkit.broadcastMessage(message_pvpEnabledBroadcast1);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000f, 1);
	            }
	            if (timeToPvp == 0) {
	                Listeners.setPvpEnabled(true);
	                Bukkit.broadcastMessage(message_pvpEnabledBroadcast);
	                world.playSound(spawnLocation, Sound.ENDERDRAGON_GROWL, 4000f, 1);
	            }
	            if (timeToBorder == 0) {
	                Bukkit.broadcastMessage(message_borderBroadcast);
	                border.setSize(getFinalBorderSize(), getBorderShrinkTime());
	            }
	            if (timeToDeathmatch == 10) {
	                Bukkit.broadcastMessage(message_deathmatchWarningBroadcast);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000, 1);
	            }
	            if (timeToDeathmatch == 3) {
	                Bukkit.broadcastMessage(message_deathmatchWarningBroadcast3);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000, 1);
	            }
	            if (timeToDeathmatch == 2) {
	                Bukkit.broadcastMessage(message_deathmatchWarningBroadcast2);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000, 1);
	            }
	            if (timeToDeathmatch == 1) {
	                Bukkit.broadcastMessage(message_deathmatchWarningBroadcast1);
	                world.playSound(spawnLocation, Sound.SUCCESSFUL_HIT, 4000, 1);
	            }
	            if (timeToDeathmatch == 0) {
	                Bukkit.broadcastMessage(message_deathmatchBroadcast);
	                world.playSound(spawnLocation, Sound.ANVIL_LAND, 4000f, 1);
	                for (String playerName: alivePlayers)
	                {
	                	Bukkit.getPlayer(playerName).teleport(spawnLocation);
	                }
	            }
	            if (getAlivePlayers().size() == 1)
	   		 	{
	            	task.cancel();
	   		 	}
	            
	        }
	    };

	    task.runTaskTimer(Main.getInstance(), 0L, 20L);
	}

	private String formatSeconds(int time) {
	    int minutes = time / 60;
	    int seconds = time % 60;

	    return String.format("%02d:%02d", minutes, seconds);
	}
	
	private static String message_notOP = ChatColor.RED + "(!)" + ChatColor.GRAY + "You must be an operator to use this.";
	private static String message_starting = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Starting...";
	private static int teleportedPlayers = 0;
	private static String message_teleported = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Spawned " + teleportedPlayers + " player(s) to random locations.";
	private static String message_timerStarted = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Started the Timer";
	private static String message_appleDropChanged = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Apple Drop Chance changed to: " + ChatColor.YELLOW;
	private static String message_integerInvalid = ChatColor.RED + "(!) " + ChatColor.GRAY + "Unexpected Integer Specified: " + ChatColor.YELLOW;
	private static String message_minXChanged = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Minimum X changed to: " + ChatColor.YELLOW;
	private static String message_maxXChanged = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Maximum X changed to: " + ChatColor.YELLOW;
	private static String message_minZChanged = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Minimum Z changed to: " + ChatColor.YELLOW;
	private static String message_maxZChanged = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Maximum Z changed to: " + ChatColor.YELLOW;
	private static String message_playerInvalid = ChatColor.RED + "(!) " + ChatColor.GRAY + "Unknown Player Specified: " + ChatColor.YELLOW;
	private static String message_playerNotAlive= ChatColor.RED + "(!) " + ChatColor.GRAY + "Player is not alive: " + ChatColor.YELLOW;
	private static String message_disqualified = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Player Disqualified: " + ChatColor.YELLOW;
	private static String message_disqualified2 = ChatColor.GRAY + "Note: You should /kill <player> for natural death.";
	private static String message_disqualifyUsage = ChatColor.RED + "(!)" + ChatColor.GRAY + "Usage: /uhc disqualify <player>";
	private static String message_reviveUsage = ChatColor.RED + "(!)" + ChatColor.GRAY + "Usage: /uhc revive <player>";
	private static String message_playerAlreadyAlive= ChatColor.RED + "(!) " + ChatColor.GRAY + "Player is already alive: " + ChatColor.YELLOW;
	private static String message_revived = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Player Revived: " + ChatColor.YELLOW;
	
	private static String message_pvpEnabled = ChatColor.GRAY + "PvP " + ChatColor.GREEN + "Enabled";
	private static String message_pvpDisabled = ChatColor.GRAY + "PvP " + ChatColor.RED + "Disabled";
	private static String message_pvpCommandUsage = ChatColor.RED + "(!)" + ChatColor.GRAY + "Usage: /uhc settings pvp [Enable|Disable]";
	private static String message_pvpTime = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "PvP will enable at: " + ChatColor.YELLOW;
	
	private static String message_pvpEnabledBroadcast3 = ChatColor.YELLOW + ChatColor.BOLD.toString() + "PvP"+ ChatColor.GRAY+ " will be enabled in 3";
	private static String message_pvpEnabledBroadcast2 = ChatColor.YELLOW + ChatColor.BOLD.toString() + "PvP"+ ChatColor.GRAY+ " will be enabled in 2";
	private static String message_pvpEnabledBroadcast1 = ChatColor.YELLOW + ChatColor.BOLD.toString() + "PvP"+ ChatColor.GRAY+ " will be enabled in 1";
	private static String message_pvpEnabledBroadcast = ChatColor.YELLOW + ChatColor.BOLD.toString() + "PvP has been enabled!";
	private static String message_borderTime = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Border will start shrinking at: " + ChatColor.YELLOW;
	private static String message_borderBroadcast = ChatColor.GOLD + ChatColor.BOLD.toString() + "Border has started to shrink!";
	private static String message_borderInitial = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Initial Border Size: " + ChatColor.YELLOW;
	private static String message_borderFinal = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Final Border Size: " + ChatColor.YELLOW;
	private static String message_borderShrinkTime = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Border Shrink time: " + ChatColor.YELLOW;
	private static String message_deathmatchTime = ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Deathmatch will occur at: " + ChatColor.YELLOW;
	private static String message_deathmatchWarningBroadcast = ChatColor.RED + ChatColor.BOLD.toString() + "Deathmatch"+ ChatColor.GRAY+ " will occur in 10 seconds.";
	private static String message_deathmatchWarningBroadcast3 = ChatColor.RED + ChatColor.BOLD.toString() + "Deathmatch"+ ChatColor.GRAY+ " will occur in 3 seconds.";
	private static String message_deathmatchWarningBroadcast2 = ChatColor.RED + ChatColor.BOLD.toString() + "Deathmatch"+ ChatColor.GRAY+ " will occur in 2 seconds.";
	private static String message_deathmatchWarningBroadcast1 = ChatColor.RED + ChatColor.BOLD.toString() + "Deathmatch"+ ChatColor.GRAY+ " will occur in 1 seconds.";
	private static String message_deathmatchBroadcast = ChatColor.RED + ChatColor.BOLD.toString() + "Deathmatch has started!";

	
	private void sendCommandHelp(CommandSender sender)
	{
		StringBuilder message = new StringBuilder();
	   	 message.append(ChatColor.GOLD + "   │  " + ChatColor.YELLOW + "" + "UHC" + ChatColor.WHITE + " " + "Host Commands \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc start ‣ " + ChatColor.WHITE + "Start the UHC event\n")
		   	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc cancel ‣ " + ChatColor.WHITE + "Cancel and reset data.\n")
		   	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc forceStop ‣ " + ChatColor.WHITE + "Stop the event there.\n")
		   	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc disqualify <player> ‣ " + ChatColor.WHITE + "Remove from participants.\n")
		   	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc revive <player> ‣ " + ChatColor.WHITE + "Remove from participants.\n")
		   	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings ‣ " + ChatColor.WHITE + "UHC Settings.\n")
		   	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc teleporter ‣ " + ChatColor.WHITE + "Gives you the teleporter\n")
	       	    .append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc ? ‣ " + ChatColor.WHITE + "Shows this message.\n")
	       	    .append(" ");
	       	
	   	 sender.sendMessage(message.toString());
	}
	
	private void sendSettingsCommandHelp(CommandSender sender)
	{
		StringBuilder message = new StringBuilder();
	   	 message.append(ChatColor.GOLD + "   │  " + ChatColor.YELLOW + "" + "UHC" + ChatColor.WHITE + " " + "Settings \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings applechance <decimal> ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Apple Drop Chance\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings minX <blocks> ‣ \n" )
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Minimum X when teleporting\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings maxX <blocks> ‣ \n" )
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Maximum X when teleporting\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings minZ <blocks> ‣ \n" )
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Minimum Z when teleporting\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings maxZ <blocks> ‣ \n" )
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Maximum Z when teleporting\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings pvp [Enable|Disable] ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Toggle if enable/disable not specified.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings pvptime <seconds> ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "When the PvP should enable.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings bordertime <seconds> ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "When the border should shrink.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings initialBorder <blocks> ‣ \n" )
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ " + ChatColor.WHITE + "Size of border initially.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings finalBorder <blocks> ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Size of final border.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings borderShrink <seconds> ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "Time taken to reach the final border size.\n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "/uhc settings deathmatchTime <seconds> ‣ \n")
			   	.append(ChatColor.GOLD + "   │  " + ChatColor.GRAY + "⤷ "  + ChatColor.WHITE + "When deathmatch should occur.\n")
			   	.append(" ");
	       	
	   	 sender.sendMessage(message.toString());
	}
	
	private void settingsCommand(CommandSender sender, String[] args)
	{

		if (args.length == 3 && args[1].equalsIgnoreCase("applechance"))
		{
			try
    		{
    			double value = Double.parseDouble(args[2]);	
    			Listeners.setAppleDropChance(value);
    			sender.sendMessage(message_appleDropChanged + value);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("minx"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setMinX(blocks);
    			sender.sendMessage(message_minXChanged + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("maxx"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setMaxX(blocks);
    			sender.sendMessage(message_maxXChanged + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("minz"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setMinZ(blocks);
    			sender.sendMessage(message_minZChanged + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("maxz"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setMaxZ(blocks);
    			sender.sendMessage(message_maxZChanged + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 2 && args[1].equalsIgnoreCase("pvp"))
		{
			if (Listeners.isPvpEnabled())
			{
				Listeners.setPvpEnabled(false);
				sender.sendMessage(message_pvpDisabled);
			}
			else
			{
				Listeners.setPvpEnabled(true);
				sender.sendMessage(message_pvpEnabled);
			}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("pvp"))
		{
			if (args[2].equalsIgnoreCase("enable"))
			{
				Listeners.setPvpEnabled(true);
				sender.sendMessage(message_pvpEnabled);
			}
			else if (args[2].equalsIgnoreCase("disable"))
			{
				Listeners.setPvpEnabled(false);
				sender.sendMessage(message_pvpDisabled);
			}
			else
			{
				sender.sendMessage(message_pvpCommandUsage);
			}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("pvptime"))
		{
			try
    		{
    			int seconds = Integer.parseInt(args[2]);	
    			setPvpTime(seconds);
    			sender.sendMessage(message_pvpTime + seconds);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("bordertime"))
		{
			try
    		{
    			int seconds = Integer.parseInt(args[2]);	
    			setBorderTime(seconds);
    			sender.sendMessage(message_borderTime + seconds);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("initialBorder"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setInitialBorderSize(blocks);
    			sender.sendMessage(message_borderInitial + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("finalBorder"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setFinalBorderSize(blocks);
    			sender.sendMessage(message_borderFinal + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("borderShrink"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setBorderShrinkTime(blocks);
    			sender.sendMessage(message_borderShrinkTime + blocks);
    			sender.sendMessage(getMessageBorderShrinkSpeed());
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else if (args.length == 3 && args[1].equalsIgnoreCase("deathmatchTime"))
		{
			try
    		{
    			int blocks = Integer.parseInt(args[2]);	
    			setDeathmatchTime(blocks);
    			sender.sendMessage(message_deathmatchTime + blocks);
    		}
    		catch (NumberFormatException e)
    		{
    			sender.sendMessage(message_integerInvalid + args[2]);
    		}
		}
		else
		{
			sendSettingsCommandHelp(sender);
		}
	}
	
	private void disqualifyCommand(CommandSender sender, String[] args)
	{
		if (args.length < 2)
		{
			sender.sendMessage(message_disqualifyUsage);
			return;
		}
		Player player = Bukkit.getPlayer(args[1]);
		if (player == null)
		{
			sender.sendMessage(message_playerInvalid);
			return;
		}
		if (!alivePlayers.contains(player.getName()))
		{
			sender.sendMessage(message_playerNotAlive);
			return;
		}
		alivePlayers.remove(player.getName());
		Listeners.spectators.add(player);
		Listeners.setSpectating(player);
		sender.sendMessage(message_disqualified);
		sender.sendMessage(message_disqualified2);
	}
	private void reviveCommand(CommandSender sender, String[] args)
	{
		if (args.length < 2)
		{
			sender.sendMessage(message_reviveUsage);
			return;
		}
		Player player = Bukkit.getPlayer(args[1]);
		if (player == null)
		{
			sender.sendMessage(message_playerInvalid);
			return;
		}
		if (alivePlayers.contains(player.getName()))
		{
			sender.sendMessage(message_playerAlreadyAlive);
			return;
		}
		alivePlayers.add(player.getName());
		Listeners.spectators.remove(player);
		Listeners.removeSpectating(player);
		sender.sendMessage(message_revived);

	}
	
	public static double getBorderSpeed() {
	    double currentBorderSize = getInitialBorderSize();
	    double finalSize = getFinalBorderSize();
	    long remainingTime = getBorderShrinkTime();

	    if (remainingTime > 0) {
	        return (currentBorderSize - finalSize) / remainingTime;
	    } else {
	        return 0; // Handle the case where the remaining time is zero or negative.
	    }
	}
	
	private static String getMessageBorderShrinkSpeed() {
	    return ChatColor.GREEN + "✔ " + ChatColor.GRAY + "Border will decrease at " + ChatColor.YELLOW + getBorderSpeed() + " blocks per second.";
	}

}
