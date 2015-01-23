/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.util.Pair;

/**
 * A paper as delivered by ConfTool.
 * 
 * @author marco.petris@web.de
 *
 */
public class Paper {
	private static final String AUTHOR_PATTERN = 
			"([^;]+)|" // single author 1 
			+ "(" // multiple authors 2
				+ "([^()]+?(\\((\\d+)\\))?;)+" // author list 3-5
				+ "([^()]+?(\\((\\d+)\\))?)" // last author 6-8
			+ ")"; 
	
	private static final String FIND_AUTHOR_PATTERN = "(([^();]+)(\\((\\d+)\\))?);?"; //1 name, 3 index 4 index number

	private static final String FIND_ORGANIZATION_PATTERN = 
			"(\\s*+[^0-9].+)|" //single organization 1
			+ "(\\s*(\\d+):([^;]+);?)"; //indexed list 3 index 4 name

	private Integer paperId;
	private String title;
	private String authors;
	private String organisations;
	private List<String> keywords;
	private String contributionType;
	private List<String> topics;
	
	public Paper(Integer paperId, String title, String authors,
			String organisations, String keywords, String topics,
			String contributionType) {
		super();
		this.paperId = paperId;
		this.title = title;
		this.authors = authors;
		this.organisations = organisations;
		this.keywords = makeCollection(keywords);
		this.contributionType = contributionType;
		this.topics = makeCollection(topics);
	}
	

	/**
	 * @return the title of the paper
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return the ConfTool paperID.
	 */
	public Integer getPaperId() {
		return paperId;
	}
	
	@Override
	public String toString() {
		return "#"+paperId + "["+title+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @return a list of authors and their affiliation as pairs.
	 * @throws IOException in case of any failure
	 */
	public List<Pair<String, String>> getAuthorsAndAffiliations() throws IOException {
		Matcher fullAuthorMatcher = Pattern.compile(AUTHOR_PATTERN).matcher(authors);
		if (fullAuthorMatcher.matches()) { // do we have a valid auther and affiliation statement?
			if (fullAuthorMatcher.group(1) != null) { // a single author
				return Collections.singletonList(
						new Pair<>(
							fullAuthorMatcher.group(1).trim(),
							organisations.trim()));
			}
			else { // a list of authors and organizations
				List<Pair<String,String>> result = new ArrayList<>();
				
				// create mapping index->organization
				Matcher findOrganizationsMatcher = 
					Pattern.compile(FIND_ORGANIZATION_PATTERN).matcher(organisations);
				Map<Integer, String> organizationsByIndex = new HashMap<>();
				
				while (findOrganizationsMatcher.find()) {
					if (findOrganizationsMatcher.group(1) != null) {
						organizationsByIndex.put(0, findOrganizationsMatcher.group(1));
					}
					else {
						organizationsByIndex.put(
							Integer.valueOf(findOrganizationsMatcher.group(3).trim()), 
							findOrganizationsMatcher.group(4).trim());
					}
				}
				
				// find authors and assign their organization
				Matcher findAuthorMatcher = 
						Pattern.compile(FIND_AUTHOR_PATTERN).matcher(authors);
				while (findAuthorMatcher.find()) {
					int idx = 0;
					if (findAuthorMatcher.group(3) != null) {
						idx = Integer.valueOf(findAuthorMatcher.group(4).trim());
					}
					String author = findAuthorMatcher.group(2).trim();
					result.add(
						new Pair<>(author, organizationsByIndex.get(idx)));
				}
				
				if (result.isEmpty()) {
					throw new IOException(
						Messages.getString(
							"Paper.unknownAuthorAffiliationPattern", //$NON-NLS-1$
							authors, organisations));  
				}
				
				return result;
			}
		}
		
		throw new IOException(
			Messages.getString(
				"Paper.unknownAuthorAffiliationPattern", //$NON-NLS-1$
				authors, organisations));
	}
	
	/**
	 * @return the type of the submission
	 */
	public String getContributionType() {
		return contributionType;
	}

	private List<String> makeCollection(String items) {
		if ((items == null) || items.trim().isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<String> result = new ArrayList<>();
		for (String item : items.split(",")) { //$NON-NLS-1$
			result.add(item.trim());
		}
		return result;
	}

	/**
	 * @return ConfTool keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}
	
	/**
	 * @return ConfTool topics
	 */
	public List<String> getTopics() {
		return topics;
	}

}
