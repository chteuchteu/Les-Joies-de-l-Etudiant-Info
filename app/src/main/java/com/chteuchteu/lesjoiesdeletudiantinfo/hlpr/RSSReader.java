package com.chteuchteu.lesjoiesdeletudiantinfo.hlpr;


import android.util.Log;

import com.chteuchteu.lesjoiesdeletudiantinfo.obj.Gif;
import com.chteuchteu.lesjoiesdeletudiantinfo.ui.Activity_Main.parseFeed;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
public class RSSReader {
	public static List<Gif> parse(String feedurl, parseFeed thread) {
		List<Gif> l = new ArrayList<Gif>();
		try {
			if (thread != null)
				thread.manualPublishProgress(10);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			URL url = new URL(feedurl);
			Document doc = builder.parse(url.openStream());

			if (thread != null)
				thread.manualPublishProgress(50);
			
			NodeList nodes = doc.getElementsByTagName("item");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				if (!readNode(element, "title").equals("")) {
					Gif g = new Gif();
					g.nom = readNode(element, "title");
					g.urlArticle = readNode(element, "link");
					if (g.urlArticle.contains("?utm_source"))
						g.urlArticle = g.urlArticle.substring(0, g.urlArticle.lastIndexOf('/'));
					g.date = gMTDateToFrench3(readNode(element, "pubDate"));
					
					String content = readNode(element, "content:encoded");
					if (content.contains("<![CDATA["))
						content = content.substring("<![CDATA[".length(), content.length() - "]]>".length());
					org.jsoup.nodes.Document c = Jsoup.parse(content);
					Elements pngs = c.select("img[src$=.gif]");
					g.urlGif = pngs.get(0).attr("src");
					l.add(g);
					
					int percentage = i * 100 / nodes.getLength() / 2 + 50;
					if (thread != null)
						thread.manualPublishProgress(percentage);
				}
			}
			if (thread != null)
				thread.manualPublishProgress(100);
		} catch (SAXException ex) {
			Log.e("", ex.toString());
		} catch (IOException ex) {
			Log.e("", ex.toString());
		} catch (ParserConfigurationException ex) {
			Log.e("", ex.toString());
		}
		return l;
	}

	public static String readNode(Node _node, String _path) {
		
		String[] paths = _path.split("\\|");
		Node node = null;
		
		if (paths.length > 0) {
			node = _node;

			for (String path : paths)
				node = getChildByName(node, path.trim());
		}

		return node != null ? node.getTextContent() : "";
	}

	public static Node getChildByName(Node _node, String _name) {
		if (_node == null)
			return null;

		NodeList listChild = _node.getChildNodes();
		
		if (listChild != null) {
			for (int i = 0; i < listChild.getLength(); i++) {
				Node child = listChild.item(i);
				if (child != null) {
					if ((child.getNodeName() != null && (_name.equals(child.getNodeName()))) || (child.getLocalName() != null && (_name.equals(child.getLocalName())))) {
						return child;
					}
				}
			}
		}
		return null;
	}
	
	public static String gMTDateToFrench3(String gmtDate) {
		try {
			SimpleDateFormat dfGMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			dfGMT.parse(gmtDate);
			SimpleDateFormat dfFrench = new SimpleDateFormat("d/MM", Locale.FRANCE);
			return dfFrench.format(dfGMT.getCalendar().getTime());
		} catch (ParseException ex) {
			Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "";
	}
}
