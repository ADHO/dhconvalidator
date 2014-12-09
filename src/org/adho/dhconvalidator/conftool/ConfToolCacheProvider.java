package org.adho.dhconvalidator.conftool;

public enum ConfToolCacheProvider {
	INSTANCE;
	private volatile ConfToolCache confToolCache;
	void setConfToolCache(ConfToolCache confToolCache) {
		this.confToolCache = confToolCache;
	}
	public ConfToolCache getConfToolCache() {
		return confToolCache;
	}
}
