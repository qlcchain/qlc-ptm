package com.quorum.tessera.encryption;

import java.util.Objects;

public class KeyCache {
	public String senderKey;
	public String reciperKey;
	public SharedKey sharedKey;
	public int usedCount;

	public KeyCache(String senderKey, String reciperKey, SharedKey sharedKey) {
		this.senderKey = senderKey;
		this.reciperKey = reciperKey;
		this.usedCount = 0;
		this.sharedKey = sharedKey;
	}
}
