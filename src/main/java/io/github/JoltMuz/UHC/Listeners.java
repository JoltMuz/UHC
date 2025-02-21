package io.github.JoltMuz.UHC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class Listeners implements Listener
{
	private boolean enabled = true;
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	private static double appleDropChance = 0.3;
	public double getAppleDropChance() {
		return appleDropChance;
	}
	public static void setAppleDropChance(double value) {
		appleDropChance = value;
	}
	
	private static boolean pvpEnabled = false;
	public static boolean isPvpEnabled() 
	{
		return pvpEnabled;
	}
	public static void setPvpEnabled(boolean enabled) {
		pvpEnabled = enabled;
	}
	
	public static ArrayList<Player> spectators = new ArrayList<>();
	public static ArrayList<Player> immune = new ArrayList<>();
	
	public Map<String, PlayerInventory> inventories = new HashMap<>();
	
	public void join(PlayerJoinEvent event) 
	{
		if (Commands.isOngoing())
		{
			Player player = event.getPlayer();
			String playerName = player.getName();
			if (inventories.containsKey(playerName))
			{
				PlayerInventory inventory = inventories.get(playerName);
				player.getInventory().setArmorContents(inventory.getArmorContents());
				player.getInventory().setContents(inventory.getContents());
				Bukkit.broadcastMessage(ChatColor.YELLOW + playerName + ChatColor.GRAY + " has reconnected.");
				Commands.getAlivePlayers().add(playerName);
			}
			else
			{
				setSpectating(player);
				player.sendMessage(ChatColor.YELLOW + "(!)" + ChatColor.GRAY + "UHC event is currently on-going and you're a spectator");
			}
		}
	}
	public void quit(PlayerQuitEvent event) 
	{
		Player player = event.getPlayer();
		String playerName = player.getName();

		if (Commands.getAlivePlayers().contains(playerName))
		{
			inventories.put(playerName, player.getInventory());
			Bukkit.broadcastMessage(ChatColor.YELLOW + playerName + ChatColor.GRAY + " has disconnected. They can rejoin.");
		}
		if (spectators.contains(player))
		{
			removeSpectating(player);
		}
	}
	
	public static void removeSpectating(Player player)
	{
		player.setHealth(20); // Restore the player's health
        player.removePotionEffect(PotionEffectType.INVISIBILITY); // Remove the invisibility potion effect
        player.setAllowFlight(false); // Disable flight
        player.setFlying(false);
        player.getInventory().clear(); // Clear the player's inventory
        player.setGameMode(GameMode.SURVIVAL); // Set the game mode back to survival

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) 
        {
            onlinePlayer.showPlayer(player); // Make the player visible to other players
            player.showPlayer(onlinePlayer);
        }
	}
	public void onPlayerChat(AsyncPlayerChatEvent event) 
	{
		Player player = event.getPlayer();
		if (spectators.contains(player))
		{
			event.setCancelled(true);
			for (Player spectator : spectators)
			{
				spectator.sendMessage(ChatColor.GRAY + "[SPEC] " + player.getName() + " " + event.getMessage());
			}
		}
		
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) 
	{
		if (!isEnabled())
		{
			return;
		}
		
	    Block block = event.getBlock();
	    Material blockType = block.getType();

	    if (blockType == Material.IRON_ORE || blockType == Material.GOLD_ORE || blockType == Material.DIAMOND_ORE) 
	    {
	        event.setCancelled(true);
	        block.setType(Material.AIR);
	        int dropAmount = 2;
	        Material ingotType = Material.IRON_INGOT;

	        if (blockType == Material.GOLD_ORE) 
	        {
	            ingotType = Material.GOLD_INGOT;
	        } else if (blockType == Material.DIAMOND_ORE) 
	        {
	            ingotType = Material.DIAMOND;
	        }
	        Player player = event.getPlayer();
	        player.getInventory().addItem(new ItemStack(ingotType, dropAmount));
	        player.giveExp(10);
	    }
	    
	    if (blockType == Material.LEAVES || blockType == Material.LEAVES_2) {
	        if (Math.random() < getAppleDropChance()) {
	            block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.APPLE));
	        }
	    }
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) 
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		Player player = (Player) event.getEntity();
		
		if (spectators.contains(player))
		{
			event.setCancelled(true);
		}
		
		if (isPvpEnabled())
		{
			return;
		}
		
	    if ( event.getCause() == DamageCause.FALL ||
	    		event.getCause() == DamageCause.FIRE ||
	    		event.getCause() == DamageCause.FIRE_TICK ||
	    		event.getCause() == DamageCause.LAVA)
	    {
	        event.setCancelled(true);
	    }
	}
	
	 @EventHandler
	 public void onEntityDamageByEntity(EntityDamageByEntityEvent event) 
	 {
		 if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player))
		 {
			 return;
		 }
		 
		 Player damagee = (Player) event.getEntity();
		 Player damager = (Player) event.getDamager();
		 
		if (spectators.contains(damager) || spectators.contains(damagee))
		{
			event.setCancelled(true);
			return;
		}
		 
		if (immune.contains(damagee))
		{
			event.setCancelled(true);
			damager.sendMessage(ChatColor.RED + "(!) " + ChatColor.GRAY + damagee.getName() + " is currently immune to hits.");
		}
		if (immune.contains(damager))
		{
			immune.remove(damager);
			damager.sendMessage(ChatColor.GRAY + "You are no longer immune.");
		}
		 
		if (!isPvpEnabled()) {
		    event.setCancelled(true);
		}
	 }
	 
	 private static String thirdPlayerStanding = "None";
	 
	 @EventHandler
	 public void Death(PlayerDeathEvent event) 
	 {
		 if (!Commands.isOngoing())
		 {
			 return;
		 }
		 
		 Player player = event.getEntity();
		 
		 if (Commands.getAlivePlayers().contains(player.getName()))
		 {
			 Commands.getAlivePlayers().remove(player.getName());
		 }
		 event.setKeepInventory(true);
		 player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES,1);
		 
		 if (player.getKiller() != null)
		 {
			 Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has been killed by " + ChatColor.YELLOW + player.getKiller().getName());
		 }
		 else
		 {
			 Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " has died.");
		 } 
		 
		 //Chest on Player's Location
		 
		 Block deathLocation = player.getLocation().getBlock();
		 deathLocation.getWorld().strikeLightningEffect(deathLocation.getLocation());
         if (deathLocation.getType() != Material.CHEST) 
         {
             deathLocation.setType(Material.CHEST);
             Location secondChestLocation = deathLocation.getLocation().clone(); 
             secondChestLocation.add(0, 0, 1); 
             secondChestLocation.getBlock().setType(Material.CHEST);
         }
         Chest chest = (Chest) deathLocation.getState();
         Inventory chestInventory = chest.getInventory();

         for (ItemStack item : player.getInventory().getContents()) 
         {
             if (item != null) {
                 chestInventory.addItem(item);
                 player.getInventory().remove(item);
             }
         }
         for (ItemStack armorPiece : player.getInventory().getArmorContents()) 
         {
    	     if (armorPiece != null) 
    	     {
    	         chestInventory.addItem(armorPiece);
    	         player.getInventory().remove(armorPiece);
    	     }
    	 }
         
         setSpectating(player);
		 		 
		 Player killer = player.getKiller();
		 if (killer != null)
		 {
			 immune.add(killer);
			 killer.sendMessage(ChatColor.GRAY + "You are immune for 30 seconds unless you hit someone.");
			 Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
				 if (immune.contains(killer))
				 {
					 immune.remove(killer);
					 killer.sendMessage(ChatColor.GRAY + "You are no longer immune.");

				 } 
	            }, 20L * 30);
		 }
		 
		 if (Commands.getAlivePlayers().size() == 2)
		 {
			 thirdPlayerStanding = player.getName();
		 }
		 if (Commands.getAlivePlayers().size() == 1)
		 {
			 StringBuilder message = new StringBuilder();
		   	 message.append(ChatColor.YELLOW + ChatColor.BOLD.toString() + "The UHC Event has Ended!\n")
		       	    .append(" ")
		       	    .append(ChatColor.GREEN + "――――――――――――――――――――――――――――――――")
		       	    .append(" ")
		       	    .append(" ")
		       	    .append(ChatColor.YELLOW + "\n #1 - " + killer.getName())
			       	.append(ChatColor.WHITE + "\n #2 - " + player.getName())
			       	.append(ChatColor.GOLD + "\n #3 - " + thirdPlayerStanding)
			       	.append(" ")
		   	 		.append("\n ");
		       	
		   	 Bukkit.broadcastMessage(message.toString());
		   	 
		 }
	 }
     private final int pageSize = 9; // Number of slots in each page
     private int currentPage = 0; // Current page of the inventory

     @EventHandler
     public void onInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();

         if (event.getItem() != null
                 && event.getItem().getItemMeta() != null
                 && event.getItem().getItemMeta().getDisplayName() != null
                 && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Teleporter")) {

             // Create an inventory with player heads for the current page
             Inventory teleportInventory = createTeleportInventory(player);

             player.openInventory(teleportInventory);
         }
         
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
    			 spectators.contains(player))
    	 {
    		 event.setCancelled(true);
    		 return;
    	 }
    	 
     }

     @EventHandler
     public void onInventoryClick(InventoryClickEvent event) {
         if (event.getInventory().getTitle().contains("Teleport to Players")) {
             event.setCancelled(true);

             ItemStack clickedItem = event.getCurrentItem();

             if (clickedItem != null && clickedItem.getType() == Material.SKULL_ITEM) {
                 Player player = Bukkit.getPlayer(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
                 if (player != null) {
                     ((Player) event.getWhoClicked()).teleport(player);
                 }
             }

             if (clickedItem != null && clickedItem.getType() == Material.ARROW) {
                 String arrowName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

                 if (arrowName.equals(ChatColor.YELLOW + "Previous Page") && currentPage > 0) {
                     currentPage--;
                 } else if (arrowName.equals(ChatColor.YELLOW + "Next Page")) {
                     // Check if there are more pages
                     if ((currentPage + 1) * pageSize < Commands.getAlivePlayers().size()) {
                         currentPage++;
                     }
                 }

                 // Update the inventory
                 event.getWhoClicked().openInventory(createTeleportInventory((Player) event.getWhoClicked()));
             }
         }
     }
     
     @EventHandler
     public void drop(PlayerDropItemEvent event)
     {
    	 if (spectators.contains(event.getPlayer()))
    	 {
    		 event.setCancelled(true);
    		 return;
    	 }
    	 if (event.getItemDrop().getCustomName().contains("teleporter"))
    	 {
    		 event.setCancelled(true);
    	 }
     }

     private Inventory createTeleportInventory(Player player) {
         List<String> alivePlayers = Commands.getAlivePlayers();
         int totalPlayers = alivePlayers.size();

         int totalPages = (totalPlayers + pageSize - 1) / pageSize;

         // Ensure currentPage is within valid bounds
         if (currentPage < 0) {
             currentPage = 0;
         } else if (currentPage >= totalPages) {
             currentPage = totalPages - 1;
         }

         int startIndex = currentPage * pageSize;
         int endIndex = Math.min(startIndex + pageSize, totalPlayers);

         Inventory teleportInventory = Bukkit.createInventory(null, pageSize + 9, "Teleport to Players (Page " + (currentPage + 1) + "/" + totalPages + ")");

         for (int i = startIndex; i < endIndex; i++) {
             String playerName = alivePlayers.get(i);
             ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
             SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
             skullMeta.setOwner(playerName);
             skullMeta.setDisplayName(playerName);
             playerHead.setItemMeta(skullMeta);

             teleportInventory.addItem(playerHead);
         }

         // Add navigation arrows
         if (currentPage > 0) {
             ItemStack prevArrow = new ItemStack(Material.ARROW);
             ItemMeta prevArrowMeta = prevArrow.getItemMeta();
             prevArrowMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
             prevArrow.setItemMeta(prevArrowMeta);
             teleportInventory.setItem(pageSize + 3, prevArrow);
         }

         if (currentPage < totalPages - 1) {
             ItemStack nextArrow = new ItemStack(Material.ARROW);
             ItemMeta nextArrowMeta = nextArrow.getItemMeta();
             nextArrowMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
             nextArrow.setItemMeta(nextArrowMeta);
             teleportInventory.setItem(pageSize + 5, nextArrow);
         }

         return teleportInventory;
     }
     
     public static void setSpectating(Player player)
     {
		 player.setHealth(20);
		 player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
		 player.setAllowFlight(true);
		 player.setFlying(true);
		 spectators.add(player);
		 player.getInventory().clear();
		 ItemStack teleporter = new ItemStack(Material.COMPASS,1);
		 ItemMeta meta = teleporter.getItemMeta();
		 meta.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Teleporter");
		 teleporter.setItemMeta(meta);
         player.getInventory().addItem(teleporter);
         player.setGameMode(GameMode.ADVENTURE);
         for(Player onlinePlayer : Bukkit.getOnlinePlayers()) 
         {
        	 onlinePlayer.hidePlayer(player);
         }
     }

}
