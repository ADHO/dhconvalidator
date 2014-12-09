package org.adho.dhconvalidator.conftool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Paper {
	
	private static final String AUTHOR_PATTERN = "([^;]+)|((.*?\\((\\d+)\\);)+(.*?\\((\\d+)\\)))";
	private static final String FIND_AUTHOR_PATTERN = "((.*?)\\((\\d+)\\));?";
	private static final String FIND_ORGANIZATION_PATTERN = "\\s*((\\d+):)?([^;]+);?";
	private static final String AUTHOR_AFFILIATION_CONC = ", ";

	private Integer paperId;
	private String title;
	private String authors;
	private String organisations;
	private String keywords;
	
	public Paper(Integer paperId, String title, String authors,
			String organisations, String keywords) {
		super();
		this.paperId = paperId;
		this.title = title;
		this.authors = authors;
		this.organisations = organisations;
		this.keywords = keywords;
	}

	public String getTitle() {
		return title;
	}
	
	public Integer getPaperId() {
		return paperId;
	}
	
	@Override
	public String toString() {
		return "#"+paperId + "["+title+"]";
	}
	
	public List<String> getAuthorsAndAffiliations() throws IOException {
		Matcher fullAuthorMatcher = Pattern.compile(AUTHOR_PATTERN).matcher(authors);
		if (fullAuthorMatcher.matches()) {
			if (fullAuthorMatcher.group(1) != null) {
				return Collections.singletonList(
						fullAuthorMatcher.group(1).trim() 
						+ AUTHOR_AFFILIATION_CONC 
						+ organisations.trim());
			}
			else {
				List<String> result = new ArrayList<>();
				
				Matcher findOrganizationsMatcher = 
					Pattern.compile(FIND_ORGANIZATION_PATTERN).matcher(organisations);
				Map<Integer, String> organizationsByIndex = new HashMap<>();
				while (findOrganizationsMatcher.find()) {
					organizationsByIndex.put(
						Integer.valueOf(findOrganizationsMatcher.group(2).trim()), 
						findOrganizationsMatcher.group(3).trim());
				}

				Matcher findAuthorMatcher = 
						Pattern.compile(FIND_AUTHOR_PATTERN).matcher(authors);
				while (findAuthorMatcher.find()) {
					int idx = Integer.valueOf(findAuthorMatcher.group(3).trim());
					String author = findAuthorMatcher.group(2).trim();
					result.add(
						author + AUTHOR_AFFILIATION_CONC 
						+ organizationsByIndex.get(idx));
				}
				
				if (result.isEmpty()) {
					throw new IOException(
							"Unknown author/affiliation pattern: " 
									+ authors
									+ "/"
									+ organisations);
				}
				
				return result;
			}
		}
		
		throw new IOException(
			"Unknown author/affiliation pattern: " 
					+ authors
					+ "/"
					+ organisations);
	}
}
