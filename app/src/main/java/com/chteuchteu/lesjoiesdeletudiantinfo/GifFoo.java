package com.chteuchteu.lesjoiesdeletudiantinfo;

public class GifFoo {
	private static GifFoo instance;

	private GifFoo() {

	}

	public static synchronized GifFoo getInstance() {
		if (instance == null)
			instance = new GifFoo();
		return instance;
	}
}
