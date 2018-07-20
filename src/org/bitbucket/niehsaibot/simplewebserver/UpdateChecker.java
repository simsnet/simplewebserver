package org.bitbucket.niehsaibot.simplewebserver;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateChecker
{
  private SimpleWebServer plugin;
  private URL rssFeed;
  private String version;
  private String link;
  
  public UpdateChecker(SimpleWebServer plugin, String url)
  {
    this.plugin = plugin;
    try
    {
      this.rssFeed = new URL(url);
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }
  }
  
  public String getVersion()
  {
    return this.version;
  }
  
  public boolean isUpdateAvailable()
  {
    try
    {
      InputStream inputStream = this.rssFeed.openConnection().getInputStream();
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
      
      Node latestFile = document.getElementsByTagName("item").item(0);
      NodeList children = latestFile.getChildNodes();
      
      this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
      this.link = children.item(3).getTextContent();
      
      Double currentVersion = Double.valueOf(Double.parseDouble(this.plugin.getDescription().getVersion()));
      Double latestVersion = Double.valueOf(Double.parseDouble(this.version));
      if (currentVersion.doubleValue() < latestVersion.doubleValue()) {
        return true;
      }
    }
    catch (Exception e)
    {
      this.plugin.log.info("Checking for updates has failed.");
    }
    return false;
  }
}
