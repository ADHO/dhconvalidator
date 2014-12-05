package org.adho.dhconvalidator.conftool;

public class Paper {

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
}
