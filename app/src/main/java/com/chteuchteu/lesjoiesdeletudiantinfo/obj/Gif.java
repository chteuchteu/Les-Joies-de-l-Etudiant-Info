package com.chteuchteu.lesjoiesdeletudiantinfo.obj;

public class Gif {
	private String name = "";
	private String articleUrl = "";
	private String gifUrl = "";
	private String date = "";
	private int state = 0;
	
	public static int ST_UNKNOWN = 0;
	public static int ST_EMPTY = 1;
	public static int ST_DOWNLOADING = 2;
	public static int ST_COMPLETE = 3;
	
	public Gif() { }
	
	public boolean isValid() {
		return !name.equals("") && !gifUrl.equals("");
	}
	
	public boolean equals(Gif g) {
		if (!this.name.equals(g.name))
			return false;
		if (!this.articleUrl.equals("") && !g.articleUrl.equals("") && !this.articleUrl.equals(g.articleUrl))
			return false;
		if (!this.gifUrl.equals("") && !g.gifUrl.equals("") && !this.gifUrl.equals(g.gifUrl))
			return false;
		if (!this.date.equals("") && !g.date.equals("") && !this.date.equals(g.date))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getArticleUrl() {
		return articleUrl;
	}
	public void setArticleUrl(String articleUrl) {
		this.articleUrl = articleUrl;
	}

	public String getGifUrl() {
		return gifUrl;
	}
	public void setGifUrl(String gifUrl) {
		this.gifUrl = gifUrl;
	}

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
}
