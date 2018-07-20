package org.bitbucket.niehsaibot.simplewebserver;

import java.io.File;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleWebServer
  extends JavaPlugin
{
  protected Logger log;
  protected NanoHTTPD server;
  protected UpdateChecker updateChecker;
  
  public void onEnable()
  {
    this.log = getLogger();
    try
    {
      getCommand("sws").setExecutor(new SimpleWebServerCommandExecutor(this));
      
      File dataFolder = getDataFolder();
      if (!dataFolder.exists()) {
        dataFolder.mkdir();
      }
      File configFile = new File(getDataFolder(), "config.yml");
      if (!configFile.exists()) {
        saveDefaultConfig();
      }
      if (getConfig().getBoolean("plugin-metrics", true))
      {
        Metrics metrics = new Metrics(this);
        metrics.start();
      }
      if (getConfig().getBoolean("update-check", true))
      {
        this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/server-mods/simplewebserver/files.rss");
        if (this.updateChecker.isUpdateAvailable()) {
          this.log.info("A new version is available: " + this.updateChecker.getVersion());
        }
      }
      startServer();
    }
    catch (Exception ex)
    {
      this.log.log(Level.WARNING, "The web server could not be initialized.");
    }
  }
  
  public void onDisable()
  {
    stopServer();
  }
  
  public void startServer()
  {
    try
    {
      int port = getConfig().getInt("port", 8080);
      
      InetAddress address = InetAddress.getByName(getConfig().getString("binding-address", getServer().getIp()));
      
      File root = new File(getDataFolder(), getConfig().getString("root-folder", "./")).getCanonicalFile();
      
      this.server = new NanoHTTPD(port, root, address);
    }
    catch (Exception ex)
    {
      this.log.log(Level.WARNING, "The web server could not be started.");
    }
  }
  
  public void stopServer()
  {
    this.server.stop();
  }
}
