/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.paper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adho.dhconvalidator.conversion.SubmissionLanguage;
import org.adho.dhconvalidator.user.User;

/**
 * A paper as delivered by ConfTool.
 * 
 * @author marco.petris@web.de
 *
 */
public class Paper {

	private Integer paperId;
	private String title;
	private List<User> authors;
	private List<String> keywords;
	private String contributionType;
	private List<String> topics;
	private SubmissionLanguage submissionLanguage;
	
	public Paper(Integer paperId, String title, List<User> authors, String keywords, String topics,
			String contributionType) {
		super();
		this.paperId = paperId;
		this.title = title;
		this.authors = authors;
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
	 * @return a list of authors
	 */
	public List<User> getAuthorsAndAffiliations() {
		return authors;
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


	public SubmissionLanguage getSubmissionLanguage() {
		return submissionLanguage;
	}


	public void setSubmissionLanguage(SubmissionLanguage submissionLanguage) {
		this.submissionLanguage = submissionLanguage;
	}

	
}
