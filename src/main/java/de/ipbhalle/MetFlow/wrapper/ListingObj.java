package de.ipbhalle.MetFlow.wrapper;

import java.util.List;

public class ListingObj {
	
	private List<String> listing;
	private List<String> images;
	
	public ListingObj() {
		// TODO Auto-generated constructor stub
	}

	public ListingObj(List<String> list, List<String> img) {
		this.listing = list;
		this.images = img;
	}
	
	public void setListing(List<String> listing) {
		this.listing = listing;
	}

	public List<String> getListing() {
		return listing;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public List<String> getImages() {
		return images;
	}
}
