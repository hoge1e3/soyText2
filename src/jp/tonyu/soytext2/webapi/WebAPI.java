package jp.tonyu.soytext2.webapi;

import java.util.Map;


public interface WebAPI {

	public abstract void search(Map<String, String> keyMap, ItemIterator iter)
			throws Exception;

}