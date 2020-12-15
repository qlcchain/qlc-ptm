package com.quorum.tessera.encryption;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyCacheImpl {
	private volatile static KeyCacheImpl mapCacheObject;
	private static Map<String, KeyCache> cacheMap = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyCacheImpl.class);
	private static boolean cacheEnable = false;

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

	public synchronized static SharedKey KeyCacheSearch(String senderKey, String reciperKey) {
		if (cacheEnable == true) {
			String keyIndex = senderKey + reciperKey;
			KeyCache target = cacheMap.get(keyIndex);
			LOGGER.debug("KeyCacheSearch get target{}", target);
			if (target != null) {
				target.usedCount++;
				return target.sharedKey;
			}
		}
		return null;
	}

	public synchronized static void KeyCacheUpdate(String senderKey, String reciperKey, SharedKey sharedKey) {
		if (cacheEnable == true) {
			if (senderKey.length() == 0 || reciperKey.length() == 0) {
				return;
			}
			if (sharedKey == null || sharedKey.encodeToBase64().length() == 0) {
				return;
			}
			LOGGER.info("KeyCacheUpdate update senderKey{},reciperKey{},sharedKey{}", senderKey, reciperKey, sharedKey);
			String keyIndex = senderKey + reciperKey;
			KeyCache target = cacheMap.get(keyIndex);
			if (target != null) {
				target.sharedKey = sharedKey;
			} else {
				KeyCache newCache = new KeyCache(senderKey, reciperKey, sharedKey);
				cacheMap.put(keyIndex, newCache);
			}
		}
	}
	
	public static void cacheEnableSet(boolean enable) {
		cacheEnable = enable;
	}
}
