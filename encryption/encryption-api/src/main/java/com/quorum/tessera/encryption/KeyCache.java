package com.quorum.tessera.encryption;

import java.util.Objects;

public class KeyCache {
	public String SenderKey;
	public String ReciperKey;
	public SharedKey SharedKey;
	public int UsedCount;

	public KeyCache(String SenderKey, String ReciperKey, SharedKey SharedKey) {
		this.SenderKey = SenderKey;
		this.ReciperKey = ReciperKey;
		this.UsedCount = 0;
		this.SharedKey = SharedKey;
	}
}
