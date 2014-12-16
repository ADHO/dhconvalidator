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
	
	private ConcurrentHashMap<User, List<Paper>> internalPaperCache = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, User> internalUserCache = new ConcurrentHashMap<>();
	private volatile String confToolUrl;
	private volatile char[] restSharedPass;
	
	public ConfToolCache(String confToolUrl, char[] restSharedPass) {
		this.confToolUrl = confToolUrl;
		this.restSharedPass = restSharedPass;
	}
	
	void load() throws IOException {
		
		ConfToolClient confToolClient = new ConfToolClient(confToolUrl, restSharedPass);
		
		List<User> authors = confToolClient.getAuthors();
		List<Paper> papers = confToolClient.getPapers();
		
		Multimap<Integer, User> paperToUserMap = HashMultimap.create();
		ArrayListMultimap<User,Paper> userToPapersMap = ArrayListMultimap.create();
		for (User author : authors) {
			for (Integer paperId : author.getPaperIds()) {
				paperToUserMap.put(paperId, author);
			}
			internalUserCache.put(author.getUserId(), author);
		}
		
		
		for (Paper paper : papers) {
			for (User user : paperToUserMap.get(paper.getPaperId())) {
				userToPapersMap.put(user, paper);
			}
			paper.getAuthorsAndAffiliations();
		}
		
		for (Map.Entry<User,Collection<Paper>> entry : userToPapersMap.asMap().entrySet()) {
			internalPaperCache.put(entry.getKey(), (List<Paper>)entry.getValue());
		}
		
	}
	
	public List<Paper> getPapers(User user) {
		return internalPaperCache.get(user);
	}

	public User getDetailedUser(User loginUser) {
		User author = internalUserCache.get(loginUser.getUserId());
		if (author != null) {
			loginUser.setFirstName(author.getFirstName());
			loginUser.setLastName(author.getLastName());
		}
		return loginUser;
	}

	public Paper getPaper(User user, Integer paperId) {
		List<Paper> papers = getPapers(user);
		if (papers != null) {
			for (Paper paper : papers) {
				if (paper.getPaperId().equals(paperId)) {
					return paper;
				}
			}
		}
		return null;
	}
}
