package io.github.JoltMuz.UHC;

import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin
{
	private static Main instance;
	
	@Override
    public void onEnable() 
	{
		instance = this;
		
		this.getCommand("uhc").setExecutor(new Commands());
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		
	}
	
	public static Main getInstance()
	{
		return instance;
	}

}
