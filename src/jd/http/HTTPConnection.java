//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team jdownloader@freenet.de
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jd.config.Configuration;
import jd.parser.Regex;
import jd.utils.JDUtilities;

public class HTTPConnection {

    public static final int HTTP_NOT_IMPLEMENTED = HttpURLConnection.HTTP_NOT_IMPLEMENTED;
    private HttpURLConnection connection;
    private String postData;

    private HashMap<String, List<String>> requestProperties = null;
    private int[] ranges;

    public HTTPConnection(URLConnection openConnection) {
        connection = (HttpURLConnection) openConnection;
        requestProperties = new HashMap<String, List<String>>();

        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");
        if (JDUtilities.getSubConfig("DOWNLOAD").getBooleanProperty(Configuration.USE_PROXY, false)) {
            String user = JDUtilities.getSubConfig("DOWNLOAD").getStringProperty(Configuration.PROXY_USER, "");
            String pass = JDUtilities.getSubConfig("DOWNLOAD").getStringProperty(Configuration.PROXY_PASS, "");

            connection.setRequestProperty("Proxy-Authorization", "Basic " + Encoding.Base64Encode(user + ":" + pass));

        }

        if (JDUtilities.getSubConfig("DOWNLOAD").getBooleanProperty(Configuration.USE_SOCKS, false)) {

            String user = JDUtilities.getSubConfig("DOWNLOAD").getStringProperty(Configuration.PROXY_USER_SOCKS, "");
            String pass = JDUtilities.getSubConfig("DOWNLOAD").getStringProperty(Configuration.PROXY_PASS_SOCKS, "");

            connection.setRequestProperty("Proxy-Authorization", "Basic " + Encoding.Base64Encode(user + ":" + pass));

        }

        Map<String, List<String>> tmp = connection.getRequestProperties();
        Iterator<Entry<String, List<String>>> set = tmp.entrySet().iterator();
        while (set.hasNext()) {
            Entry<String, List<String>> next = set.next();
            requestProperties.put(next.getKey(), next.getValue());
        }

    }

    public void connect() throws IOException {

        connection.connect();

    }

    public long getContentLength() {
        if (connection.getHeaderField("content-length") == null) { return -1; }
        return new Long(connection.getHeaderField("content-length"));
    }

    public String getContentType() {
        return connection.getContentType();
    }

    public String getHeaderField(String string) {
        return connection.getHeaderField(string);
    }

    public Map<String, List<String>> getHeaderFields() {
        return connection.getHeaderFields();
    }

    public HttpURLConnection getHTTPURLConnection() {
        return connection;

    }

    public InputStream getInputStream() throws IOException {
        if (connection.getResponseCode() != 404) {
            return connection.getInputStream();
        } else {
            return connection.getErrorStream();
        }
    }

    public OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    public String getPostData() {
        return postData;
    }

    public String getRequestMethod() {
        return connection.getRequestMethod();
    }

    public Map<String, List<String>> getRequestProperties() {
        return requestProperties;

    }

    public String getRequestProperty(String string) {
        return connection.getRequestProperty(string);
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public URL getURL() {
        return connection.getURL();
    }

    public void post(byte[] parameter) throws IOException {
        BufferedOutputStream wr = new BufferedOutputStream(connection.getOutputStream());
        if (parameter != null) {
            wr.write(parameter);
        }

        postData = "binary";
        wr.flush();
        wr.close();

    }

    public void post(String parameter) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        if (parameter != null) {
            wr.write(parameter);
        }

        postData = parameter;
        wr.flush();
        wr.close();

    }

    public void postGzip(String parameter) throws IOException {

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        if (parameter != null) {
            wr.write(parameter);
        }
        postData = parameter;
        wr.flush();
        wr.close();

    }

    public void setConnectTimeout(int timeout) {
        connection.setConnectTimeout(timeout);

    }

    public void setDoOutput(boolean b) {
        connection.setDoOutput(b);

    }

    public void setInstanceFollowRedirects(boolean redirect) {
        connection.setInstanceFollowRedirects(redirect);

    }

    public void setReadTimeout(int timeout) {
        connection.setReadTimeout(timeout);

    }

    public void setRequestMethod(String string) throws ProtocolException {
        connection.setRequestMethod(string);

    }

    public void setRequestProperty(String key, String value) {
        LinkedList<String> l = new LinkedList<String>();

        l.add(value);
        requestProperties.put(key, l);
        connection.setRequestProperty(key, value);

    }

    public boolean isOK() {

        try {

            if (connection.getResponseCode() > -2 && connection.getResponseCode() < 400)
                return true;
            else
                return false;
        } catch (IOException e) {
            return false;
        }
    }

    public int[] getRange() {
        String range;
        if (ranges != null) return ranges;
        if ((range = this.getHeaderField("Content-Range")) == null) return null;
        // bytes 174239-735270911/735270912
        String[] ranges = new Regex(range, ".*?(\\d+).*?-.*?(\\d+).*?/.*?(\\d+)").getRow(0);
        this.ranges = new int[] { Integer.parseInt(ranges[0]), Integer.parseInt(ranges[1]), Integer.parseInt(ranges[2]) };
        return this.ranges;
    }
}
