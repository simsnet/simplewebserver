package org.bitbucket.niehsaibot.simplewebserver;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SimpleWebServerCommandExecutor
  implements CommandExecutor
{
  private SimpleWebServer plugin;
  
  public SimpleWebServerCommandExecutor(SimpleWebServer plugin)
  {
    this.plugin = plugin;
  }
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    if ((!command.getName().equalsIgnoreCase("sws")) || (args.length == 0)) {
      return false;
    }
    Player player = (sender instanceof Player) ? (Player)sender : null;
    if ((args.length == 1) && (args[0].equalsIgnoreCase("reload")))
    {
      if ((player != null) && (!player.hasPermission("sws.reload")))
      {
        player.sendMessage(ChatColor.RED + "You don't have permissions to execute this command.");
      }
      else
      {
        reloadConfig();
        if (player != null) {
          player.sendMessage(ChatColor.RED + "The configuration has been reloaded.");
        }
      }
      return true;
    }
    return false;
  }
  
  private void reloadConfig()
  {
    this.plugin.stopServer();
    this.plugin.reloadConfig();
    this.plugin.startServer();
    
    this.plugin.getLogger().info("The configuration has been reloaded.");
  }
}
