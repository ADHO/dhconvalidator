package org.adho.dhconvalidator.conftool;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ConfToolCache {
	
	private ConcurrentHashMap<User, List<Paper>> internalCache = new ConcurrentHashMap<>();
	private ConfToolClient confToolClient;
	
	
	public ConfToolCache(String confToolUrl, char[] restSharedPass) {
		this.confToolClient = new ConfToolClient(confToolUrl, restSharedPass);
	}
	
	public void load() throws IOException {
		List<User> authors = confToolClient.getAuthors();
		Multimap<Integer, User> paperToUserMap = HashMultimap.create();
		ArrayListMultimap<User,Paper> userToPapersMap = ArrayListMultimap.create();
		
		for (User author : authors) {
			for (Integer paperId : author.getPaperIds()) {
				paperToUserMap.put(paperId, author);
			}
		}
		
		List<Paper> papers = confToolClient.getPapers();
		
		for (Paper paper : papers) {
			for (User user : paperToUserMap.get(paper.getPaperId())) {
				userToPapersMap.put(user, paper);
			}
		}
		
		for (Map.Entry<User,Collection<Paper>> entry : userToPapersMap.asMap().entrySet()) {
			internalCache.put(entry.getKey(), (List<Paper>)entry.getValue());
		}
	}
	
}
