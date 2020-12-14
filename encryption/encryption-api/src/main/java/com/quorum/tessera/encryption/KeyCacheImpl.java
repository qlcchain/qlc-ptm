package com.quorum.tessera.encryption;

import java.util.HashMap;
import java.util.Map;

public class KeyCacheImpl {
	private volatile static KeyCacheImpl mapCacheObject;
	private static Map<String, KeyCache> cacheMap = new HashMap<>();

	public static KeyCacheImpl getInstance() {
		if (null == mapCacheObject) {
			synchronized (KeyCacheImpl.class) {
				if (null == mapCacheObject) {
					mapCacheObject = new KeyCacheImpl();
				}
			}
		}
		return mapCacheObject;
	}

	public synchronized static SharedKey KeyCacheSearch(String SenderKey, String ReciperKey) {
		String keyIndex = SenderKey + ReciperKey;
		KeyCache target = cacheMap.get(keyIndex);
		if (target != null) {
			target.UsedCount++;
			return target.SharedKey;
		}
		return null;
	}

	public synchronized static void KeyCacheUpdate(String SenderKey, String ReciperKey, SharedKey SharedKey) {
		String keyIndex = SenderKey + ReciperKey;
		KeyCache target = cacheMap.get(keyIndex);
		if (target != null) {
			target.SharedKey = SharedKey;
		} else {
			KeyCache newCache = new KeyCache(SenderKey, ReciperKey, SharedKey);
			cacheMap.put(keyIndex, newCache);
		}
	}
}
