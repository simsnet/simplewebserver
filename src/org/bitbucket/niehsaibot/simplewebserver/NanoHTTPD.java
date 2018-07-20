package org.bitbucket.niehsaibot.simplewebserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

public class NanoHTTPD
{
  private InetAddress bindAddress;
  public static final String HTTP_OK = "200 OK";
  public static final String HTTP_PARTIALCONTENT = "206 Partial Content";
  public static final String HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable";
  public static final String HTTP_REDIRECT = "301 Moved Permanently";
  public static final String HTTP_NOTMODIFIED = "304 Not Modified";
  public static final String HTTP_FORBIDDEN = "403 Forbidden";
  public static final String HTTP_NOTFOUND = "404 Not Found";
  public static final String HTTP_BADREQUEST = "400 Bad Request";
  public static final String HTTP_INTERNALERROR = "500 Internal Server Error";
  public static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";
  public static final String MIME_PLAINTEXT = "text/plain";
  public static final String MIME_HTML = "text/html";
  public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
  public static final String MIME_XML = "text/xml";
  private int myTcpPort;
  private final ServerSocket myServerSocket;
  private Thread myThread;
  private File myRootDir;
  
  public Response serve(String uri, String method, Properties header, Properties parms, Properties files)
  {
    return serveFile(uri, header, this.myRootDir, true);
  }
  
  public class Response
  {
    public String status;
    public String mimeType;
    public InputStream data;
    
    public Response()
    {
      this.status = "200 OK";
    }
    
    public Response(String status, String mimeType, InputStream data)
    {
      this.status = status;
      this.mimeType = mimeType;
      this.data = data;
    }
    
    public Response(String status, String mimeType, String txt)
    {
      this.status = status;
      this.mimeType = mimeType;
      try
      {
        this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException uee)
      {
        uee.printStackTrace();
      }
    }
    
    public void addHeader(String name, String value)
    {
      this.header.put(name, value);
    }
    
    public Properties header = new Properties();
  }
  
  public NanoHTTPD(int port, File wwwroot, InetAddress bindAddress)
    throws IOException
  {
    this.bindAddress = bindAddress;
    this.myRootDir = wwwroot;
    this.myServerSocket = new ServerSocket(port, 0, bindAddress);
    
    this.myThread = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          for (;;)
          {
            new NanoHTTPD.HTTPSession(NanoHTTPD.this, NanoHTTPD.this.myServerSocket.accept());
          }
        }
        catch (IOException localIOException) {}
      }
    });
    this.myThread.setDaemon(true);
    this.myThread.start();
  }
  
  public NanoHTTPD(int port, File wwwroot)
    throws IOException
  {
    this.myTcpPort = port;
    this.myRootDir = wwwroot;
    this.myServerSocket = new ServerSocket(this.myTcpPort);
    this.myThread = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          for (;;)
          {
            new NanoHTTPD.HTTPSession(NanoHTTPD.this, NanoHTTPD.this.myServerSocket.accept());
          }
        }
        catch (IOException localIOException) {}
      }
    });
    this.myThread.setDaemon(true);
    this.myThread.start();
  }
  
  public void stop()
  {
    try
    {
      this.myServerSocket.close();
      this.myThread.join();
    }
    catch (IOException localIOException) {}catch (InterruptedException localInterruptedException) {}
  }
  
  public static void main(String[] args)
  {
    myOut.println("NanoHTTPD 1.25 (C) 2001,2005-2011 Jarno Elonen and (C) 2010 Konstantinos Togias\n(Command line options: [-p port] [-d root-dir] [--licence])\n");
    
    int port = 80;
    File wwwroot = new File(".").getAbsoluteFile();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-p"))
      {
        port = Integer.parseInt(args[(i + 1)]);
      }
      else if (args[i].equalsIgnoreCase("-d"))
      {
        wwwroot = new File(args[(i + 1)]).getAbsoluteFile();
      }
      else if (args[i].toLowerCase().endsWith("licence"))
      {
        myOut.println("Copyright (C) 2001,2005-2011 by Jarno Elonen <elonen@iki.fi>\nand Copyright (C) 2010 by Konstantinos Togias <info@ktogias.gr>\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n");
        break;
      }
    }
    try
    {
      new NanoHTTPD(port, wwwroot);
    }
    catch (IOException ioe)
    {
      myErr.println("Couldn't start server:\n" + ioe);
      System.exit(-1);
    }
    myOut.println("Now serving files in port " + port + " from \"" + 
      wwwroot + "\"");
    myOut.println("Hit Enter to stop.\n");
    try
    {
      System.in.read();
    }
    catch (Throwable localThrowable) {}
  }
  
  private class HTTPSession
    implements Runnable
  {
    private Socket mySocket;
    
    public HTTPSession(Socket s)
    {
      this.mySocket = s;
      Thread t = new Thread(this);
      t.setDaemon(true);
      t.start();
    }
    
    public void run()
    {
      try
      {
        InputStream is = this.mySocket.getInputStream();
        if (is == null) {
          return;
        }
        int bufsize = 8192;
        byte[] buf = new byte['?'];
        int splitbyte = 0;
        int rlen = 0;
        
        int read = is.read(buf, 0, 8192);
        while (read > 0)
        {
          rlen += read;
          splitbyte = findHeaderEnd(buf, rlen);
          if (splitbyte > 0) {
            break;
          }
          read = is.read(buf, rlen, 8192 - rlen);
        }
        ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, 
          rlen);
        BufferedReader hin = new BufferedReader(new InputStreamReader(
          hbis));
        Properties pre = new Properties();
        Properties parms = new Properties();
        Properties header = new Properties();
        Properties files = new Properties();
        
        decodeHeader(hin, pre, parms, header);
        String method = pre.getProperty("method");
        String uri = pre.getProperty("uri");
        
        long size = Long.MAX_VALUE;
        String contentLength = header.getProperty("content-length");
        if (contentLength != null) {
          try
          {
            size = Integer.parseInt(contentLength);
          }
          catch (NumberFormatException localNumberFormatException) {}
        }
        ByteArrayOutputStream f = new ByteArrayOutputStream();
        if (splitbyte < rlen) {
          f.write(buf, splitbyte, rlen - splitbyte);
        }
        if (splitbyte < rlen) {
          size -= rlen - splitbyte + 1;
        } else if ((splitbyte == 0) || (size == Long.MAX_VALUE)) {
          size = 0L;
        }
        buf = new byte['?'];
        while ((rlen >= 0) && (size > 0L))
        {
          rlen = is.read(buf, 0, 512);
          size -= rlen;
          if (rlen > 0) {
            f.write(buf, 0, rlen);
          }
        }
        byte[] fbuf = f.toByteArray();
        
        ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
        BufferedReader in = new BufferedReader(new InputStreamReader(
          bin));
        if (method.equalsIgnoreCase("POST"))
        {
          String contentType = "";
          String contentTypeHeader = header
            .getProperty("content-type");
          StringTokenizer st = new StringTokenizer(contentTypeHeader, 
            "; ");
          if (st.hasMoreTokens()) {
            contentType = st.nextToken();
          }
          if (contentType.equalsIgnoreCase("multipart/form-data"))
          {
            if (!st.hasMoreTokens()) {
              sendError(
                "400 Bad Request", 
                "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
            }
            String boundaryExp = st.nextToken();
            st = new StringTokenizer(boundaryExp, "=");
            if (st.countTokens() != 2) {
              sendError(
                "400 Bad Request", 
                "BAD REQUEST: Content type is multipart/form-data but boundary syntax error. Usage: GET /example/file.html");
            }
            st.nextToken();
            String boundary = st.nextToken();
            
            decodeMultipartData(boundary, fbuf, in, parms, files);
          }
          else
          {
            String postLine = "";
            char[] pbuf = new char['?'];
            int read = in.read(pbuf);
            while ((read >= 0) && (!postLine.endsWith("\r\n")))
            {
              postLine = postLine + String.valueOf(pbuf, 0, read);
              read = in.read(pbuf);
            }
            postLine = postLine.trim();
            decodeParms(postLine, parms);
          }
        }
        if (method.equalsIgnoreCase("PUT")) {
          files.put("content", saveTmpFile(fbuf, 0, f.size()));
        }
        NanoHTTPD.Response r = NanoHTTPD.this.serve(uri, method, header, parms, files);
        if (r == null) {
          sendError("500 Internal Server Error", 
            "SERVER INTERNAL ERROR: Serve() returned a null response.");
        } else {
          sendResponse(r.status, r.mimeType, r.header, r.data);
        }
        in.close();
        is.close();
      }
      catch (IOException ioe)
      {
        try
        {
          sendError(
            "500 Internal Server Error", 
            "SERVER INTERNAL ERROR: IOException: " + 
            ioe.getMessage());
        }
        catch (Throwable localThrowable) {}
      }
      catch (InterruptedException localInterruptedException) {}
    }
    
    private void decodeHeader(BufferedReader in, Properties pre, Properties parms, Properties header)
      throws InterruptedException
    {
      try
      {
        String inLine = in.readLine();
        if (inLine == null) {
          return;
        }
        StringTokenizer st = new StringTokenizer(inLine);
        if (!st.hasMoreTokens()) {
          sendError("400 Bad Request", 
            "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
        }
        String method = st.nextToken();
        pre.put("method", method);
        if (!st.hasMoreTokens()) {
          sendError("400 Bad Request", 
            "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
        }
        String uri = st.nextToken();
        
        int qmi = uri.indexOf('?');
        if (qmi >= 0)
        {
          decodeParms(uri.substring(qmi + 1), parms);
          uri = decodePercent(uri.substring(0, qmi));
        }
        else
        {
          uri = decodePercent(uri);
        }
        if (st.hasMoreTokens())
        {
          String line = in.readLine();
          while ((line != null) && (line.trim().length() > 0))
          {
            int p = line.indexOf(':');
            if (p >= 0) {
              header.put(line.substring(0, p).trim()
                .toLowerCase(), line.substring(p + 1)
                .trim());
            }
            line = in.readLine();
          }
        }
        pre.put("uri", uri);
      }
      catch (IOException ioe)
      {
        sendError(
          "500 Internal Server Error", 
          "SERVER INTERNAL ERROR: IOException: " + 
          ioe.getMessage());
      }
    }
    
    private void decodeMultipartData(String boundary, byte[] fbuf, BufferedReader in, Properties parms, Properties files)
      throws InterruptedException
    {
      try
      {
        int[] bpositions = getBoundaryPositions(fbuf, 
          boundary.getBytes());
        int boundarycount = 1;
        String mpline = in.readLine();
        while (mpline != null)
        {
          if (mpline.indexOf(boundary) == -1) {
            sendError(
              "400 Bad Request", 
              "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
          }
          boundarycount++;
          Properties item = new Properties();
          mpline = in.readLine();
          while ((mpline != null) && (mpline.trim().length() > 0))
          {
            int p = mpline.indexOf(':');
            if (p != -1) {
              item.put(mpline.substring(0, p).trim()
                .toLowerCase(), mpline.substring(p + 1)
                .trim());
            }
            mpline = in.readLine();
          }
          if (mpline != null)
          {
            String contentDisposition = item
              .getProperty("content-disposition");
            if (contentDisposition == null) {
              sendError(
                "400 Bad Request", 
                "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
            }
            StringTokenizer st = new StringTokenizer(
              contentDisposition, "; ");
            Properties disposition = new Properties();
            while (st.hasMoreTokens())
            {
              String token = st.nextToken();
              int p = token.indexOf('=');
              if (p != -1) {
                disposition.put(token.substring(0, p).trim()
                  .toLowerCase(), token.substring(p + 1)
                  .trim());
              }
            }
            String pname = disposition.getProperty("name");
            pname = pname.substring(1, pname.length() - 1);
            
            String value = "";
            if (item.getProperty("content-type") == null)
            {
              do
              {
                mpline = in.readLine();
                if (mpline != null)
                {
                  int d = mpline.indexOf(boundary);
                  if (d == -1) {
                    value = value + mpline;
                  } else {
                    value = value + mpline.substring(0, d - 2);
                  }
                }
                if (mpline == null) {
                  break;
                }
              } while (mpline.indexOf(boundary) == -1);
            }
            else
            {
              if (boundarycount > bpositions.length) {
                sendError("500 Internal Server Error", 
                  "Error processing request");
              }
              int offset = stripMultipartHeaders(fbuf, 
                bpositions[(boundarycount - 2)]);
              String path = saveTmpFile(fbuf, offset, 
                bpositions[(boundarycount - 1)] - offset - 4);
              files.put(pname, path);
              value = disposition.getProperty("filename");
              value = value.substring(1, value.length() - 1);
              do
              {
                mpline = in.readLine();
              } while ((mpline != null) && 
                (mpline.indexOf(boundary) == -1));
            }
            parms.put(pname, value);
          }
        }
      }
      catch (IOException ioe)
      {
        sendError(
          "500 Internal Server Error", 
          "SERVER INTERNAL ERROR: IOException: " + 
          ioe.getMessage());
      }
    }
    
    private int findHeaderEnd(byte[] buf, int rlen)
    {
      int splitbyte = 0;
      while (splitbyte + 3 < rlen)
      {
        if ((buf[splitbyte] == 13) && (buf[(splitbyte + 1)] == 10) && 
          (buf[(splitbyte + 2)] == 13) && 
          (buf[(splitbyte + 3)] == 10)) {
          return splitbyte + 4;
        }
        splitbyte++;
      }
      return 0;
    }
    
    public int[] getBoundaryPositions(byte[] b, byte[] boundary)
    {
      int matchcount = 0;
      int matchbyte = -1;
      Vector matchbytes = new Vector();
      for (int i = 0; i < b.length; i++) {
        if (b[i] == boundary[matchcount])
        {
          if (matchcount == 0) {
            matchbyte = i;
          }
          matchcount++;
          if (matchcount == boundary.length)
          {
            matchbytes.addElement(new Integer(matchbyte));
            matchcount = 0;
            matchbyte = -1;
          }
        }
        else
        {
          i -= matchcount;
          matchcount = 0;
          matchbyte = -1;
        }
      }
      int[] ret = new int[matchbytes.size()];
      for (int i = 0; i < ret.length; i++) {
        ret[i] = ((Integer)matchbytes.elementAt(i)).intValue();
      }
      return ret;
    }
    
    private String saveTmpFile(byte[] b, int offset, int len)
    {
      String path = "";
      if (len > 0)
      {
        String tmpdir = System.getProperty("java.io.tmpdir");
        try
        {
          File temp = File.createTempFile("NanoHTTPD", "", new File(
            tmpdir));
          OutputStream fstream = new FileOutputStream(temp);
          fstream.write(b, offset, len);
          fstream.close();
          path = temp.getAbsolutePath();
        }
        catch (Exception e)
        {
          NanoHTTPD.myErr.println("Error: " + e.getMessage());
        }
      }
      return path;
    }
    
    private int stripMultipartHeaders(byte[] b, int offset)
    {
      int i = 0;
      for (i = offset; i < b.length; i++) {
        if ((b[i] == 13) && (b[(++i)] == 10) && (b[(++i)] == 13) && 
          (b[(++i)] == 10)) {
          break;
        }
      }
      return i + 1;
    }
    
    private String decodePercent(String str)
      throws InterruptedException
    {
      try
      {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
          char c = str.charAt(i);
          switch (c)
          {
          case '+': 
            sb.append(' ');
            break;
          case '%': 
            sb.append((char)Integer.parseInt(
              str.substring(i + 1, i + 3), 16));
            i += 2;
            break;
          default: 
            sb.append(c);
          }
        }
        return sb.toString();
      }
      catch (Exception e)
      {
        sendError("400 Bad Request", "BAD REQUEST: Bad percent-encoding.");
      }
      return null;
    }
    
    private void decodeParms(String parms, Properties p)
      throws InterruptedException
    {
      if (parms == null) {
        return;
      }
      StringTokenizer st = new StringTokenizer(parms, "&");
      while (st.hasMoreTokens())
      {
        String e = st.nextToken();
        int sep = e.indexOf('=');
        if (sep >= 0) {
          p.put(decodePercent(e.substring(0, sep)).trim(), 
            decodePercent(e.substring(sep + 1)));
        }
      }
    }
    
    private void sendError(String status, String msg)
      throws InterruptedException
    {
      sendResponse(status, "text/plain", null, 
        new ByteArrayInputStream(msg.getBytes()));
      throw new InterruptedException();
    }
    
    private void sendResponse(String status, String mime, Properties header, InputStream data)
    {
      try
      {
        if (status == null) {
          throw new Error("sendResponse(): Status can't be null.");
        }
        OutputStream out = this.mySocket.getOutputStream();
        PrintWriter pw = new PrintWriter(out);
        pw.print("HTTP/1.0 " + status + " \r\n");
        if (mime != null) {
          pw.print("Content-Type: " + mime + "\r\n");
        }
        if ((header == null) || (header.getProperty("Date") == null)) {
          pw.print("Date: " + NanoHTTPD.gmtFrmt.format(new Date()) + "\r\n");
        }
        if (header != null)
        {
          Enumeration e = header.keys();
          while (e.hasMoreElements())
          {
            String key = (String)e.nextElement();
            String value = header.getProperty(key);
            pw.print(key + ": " + value + "\r\n");
          }
        }
        pw.print("\r\n");
        pw.flush();
        if (data != null)
        {
          int pending = data.available();
          
          byte[] buff = new byte[NanoHTTPD.theBufferSize];
          while (pending > 0)
          {
            int read = data.read(buff, 0, 
              pending > NanoHTTPD.theBufferSize ? NanoHTTPD.theBufferSize : 
              pending);
            if (read <= 0) {
              break;
            }
            out.write(buff, 0, read);
            pending -= read;
          }
        }
        out.flush();
        out.close();
        if (data != null) {
          data.close();
        }
      }
      catch (IOException ioe)
      {
        try
        {
          this.mySocket.close();
        }
        catch (Throwable localThrowable) {}
      }
    }
  }
  
  private String encodeUri(String uri)
  {
    String newUri = "";
    StringTokenizer st = new StringTokenizer(uri, "/ ", true);
    while (st.hasMoreTokens())
    {
      String tok = st.nextToken();
      if (tok.equals("/")) {
        newUri = newUri + "/";
      } else if (tok.equals(" ")) {
        newUri = newUri + "%20";
      } else {
        newUri = newUri + URLEncoder.encode(tok);
      }
    }
    return newUri;
  }
  
  public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing)
  {
    Response res = null;
    if (!homeDir.isDirectory()) {
      res = new Response("500 Internal Server Error", "text/plain", 
        "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
    }
    if (res == null)
    {
      uri = uri.trim().replace(File.separatorChar, '/');
      if (uri.indexOf('?') >= 0) {
        uri = uri.substring(0, uri.indexOf('?'));
      }
      if ((uri.startsWith("..")) || (uri.endsWith("..")) || 
        (uri.indexOf("../") >= 0)) {
        res = new Response("403 Forbidden", "text/plain", 
          "FORBIDDEN: Won't serve ../ for security reasons.");
      }
    }
    File f = new File(homeDir, uri);
    if ((res == null) && (!f.exists())) {
      res = new Response("404 Not Found", "text/plain", 
        "Error 404, file not found.");
    }
    if ((res == null) && (f.isDirectory()))
    {
      if (!uri.endsWith("/"))
      {
        uri = uri + "/";
        res = new Response("301 Moved Permanently", "text/html", 
          "<html><body>Redirected: <a href=\"" + uri + "\">" + 
          uri + "</a></body></html>");
        res.addHeader("Location", uri);
      }
      if (res == null) {
        if (new File(f, "index.html").exists())
        {
          f = new File(homeDir, uri + "/index.html");
        }
        else if (new File(f, "index.htm").exists())
        {
          f = new File(homeDir, uri + "/index.htm");
        }
        else if ((allowDirectoryListing) && (f.canRead()))
        {
          String[] files = f.list();
          String msg = "<html><body><h1>Directory " + uri + 
            "</h1><br/>";
          if (uri.length() > 1)
          {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if ((slash >= 0) && (slash < u.length())) {
              msg = 
              
                msg + "<b><a href=\"" + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
            }
          }
          if (files != null) {
            for (int i = 0; i < files.length; i++)
            {
              File curFile = new File(f, files[i]);
              boolean dir = curFile.isDirectory();
              if (dir)
              {
                msg = msg + "<b>"; int 
                  tmp558_556 = i; String[] tmp558_554 = files;tmp558_554[tmp558_556] = (tmp558_554[tmp558_556] + "/");
              }
              msg = 
                msg + "<a href=\"" + encodeUri(new StringBuilder(String.valueOf(uri)).append(files[i]).toString()) + "\">" + files[i] + "</a>";
              if (curFile.isFile())
              {
                long len = curFile.length();
                msg = msg + " &nbsp;<font size=2>(";
                if (len < 1024L) {
                  msg = msg + len + " bytes";
                } else if (len < 1048576L) {
                  msg = 
                    msg + len / 1024L + "." + len % 1024L / 10L % 100L + " KB";
                } else {
                  msg = 
                    msg + len / 1048576L + "." + len % 1048576L / 10L % 100L + " MB";
                }
                msg = msg + ")</font>";
              }
              msg = msg + "<br/>";
              if (dir) {
                msg = msg + "</b>";
              }
            }
          }
          msg = msg + "</body></html>";
          res = new Response("200 OK", "text/html", msg);
        }
        else
        {
          res = new Response("403 Forbidden", "text/plain", 
            "FORBIDDEN: No directory listing.");
        }
      }
    }
    try
    {
      if (res == null)
      {
        String mime = null;
        int dot = f.getCanonicalPath().lastIndexOf('.');
        if (dot >= 0) {
          mime = (String)theMimeTypes.get(f.getCanonicalPath()
            .substring(dot + 1).toLowerCase());
        }
        if (mime == null) {
          mime = "application/octet-stream";
        }
        String etag = Integer.toHexString(
          (f.getAbsolutePath() + f.lastModified() + f.length()).hashCode());
        
        long startFrom = 0L;
        long endAt = -1L;
        String range = header.getProperty("range");
        if ((range != null) && 
          (range.startsWith("bytes=")))
        {
          range = range.substring("bytes=".length());
          int minus = range.indexOf('-');
          try
          {
            if (minus > 0)
            {
              startFrom = Long.parseLong(range.substring(0, 
                minus));
              endAt = Long.parseLong(range
                .substring(minus + 1));
            }
          }
          catch (NumberFormatException localNumberFormatException) {}
        }
        long fileLen = f.length();
        if ((range != null) && (startFrom >= 0L))
        {
          if (startFrom >= fileLen)
          {
            res = new Response("416 Requested Range Not Satisfiable", 
              "text/plain", "");
            res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
            res.addHeader("ETag", etag);
          }
          else
          {
            if (endAt < 0L) {
              endAt = fileLen - 1L;
            }
            long newLen = endAt - startFrom + 1L;
            if (newLen < 0L) {
              newLen = 0L;
            }
            final long dataLen = newLen;
            FileInputStream fis = new FileInputStream(f)
            {
              public int available()
                throws IOException
              {
                return (int)dataLen;
              }
            };
            fis.skip(startFrom);
            
            res = new Response("206 Partial Content", mime, fis);
            res.addHeader("Content-Length", dataLen);
            res.addHeader("Content-Range", "bytes " + startFrom + 
              "-" + endAt + "/" + fileLen);
            res.addHeader("ETag", etag);
          }
        }
        else if (etag.equals(header.getProperty("if-none-match")))
        {
          res = new Response("304 Not Modified", mime, "");
        }
        else
        {
          res = new Response("200 OK", mime, 
            new FileInputStream(f));
          res.addHeader("Content-Length", fileLen);
          res.addHeader("ETag", etag);
        }
      }
    }
    catch (IOException ioe)
    {
      res = new Response("403 Forbidden", "text/plain", 
        "FORBIDDEN: Reading file failed.");
    }
    res.addHeader("Accept-Ranges", "bytes");
    
    return res;
  }
  
  private static Hashtable theMimeTypes = new Hashtable();
  private static int theBufferSize;
  protected static PrintStream myOut;
  protected static PrintStream myErr;
  private static SimpleDateFormat gmtFrmt;
  private static final String LICENCE = "Copyright (C) 2001,2005-2011 by Jarno Elonen <elonen@iki.fi>\nand Copyright (C) 2010 by Konstantinos Togias <info@ktogias.gr>\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
  
  static
  {
    StringTokenizer st = new StringTokenizer("css\t\ttext/css htm\t\ttext/html html\t\ttext/html xml\t\ttext/xml txt\t\ttext/plain asc\t\ttext/plain gif\t\timage/gif jpg\t\timage/jpeg jpeg\t\timage/jpeg png\t\timage/png mp3\t\taudio/mpeg m3u\t\taudio/mpeg-url mp4\t\tvideo/mp4 ogv\t\tvideo/ogg flv\t\tvideo/x-flv mov\t\tvideo/quicktime swf\t\tapplication/x-shockwave-flash js\t\t\tapplication/javascript pdf\t\tapplication/pdf doc\t\tapplication/msword ogg\t\tapplication/x-ogg zip\t\tapplication/octet-stream exe\t\tapplication/octet-stream class\t\tapplication/octet-stream ");
    while (st.hasMoreTokens()) {
      theMimeTypes.put(st.nextToken(), st.nextToken());
    }
    theBufferSize = 16384;
    
    myOut = System.out;
    myErr = System.err;
    
    gmtFrmt = new SimpleDateFormat(
      "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
}
