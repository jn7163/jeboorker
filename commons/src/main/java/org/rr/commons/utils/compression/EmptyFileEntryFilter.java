package org.rr.commons.utils.compression;


public class EmptyFileEntryFilter implements FileEntryFilter {
	
	@Override
	public boolean accept(String entry, byte[] rawEntry) {
		return true;
	}

}
