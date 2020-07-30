package org.citopt.connde.util;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel.PageMetadata;

/**
 * Utility class for {@link Pageable}, {@link Page}.
 * 
 * @author Jakob Benz
 */
public class Pages {
	
	// Not to be instantiated
	private Pages() {}
	
	/**
	 * A {@link Pageable} of size {@link Integer#MAX_VALUE}.
	 */
	public static final Pageable ALL = PageRequest.of(0, Integer.MAX_VALUE);


	/**
	 * Computes a page of elements based on a given list of elements
	 * and a pageable.
	 * 
	 * @param <T> the data type of the elements.
	 * @param elements all elements.
	 * @param pageable the {@link Pageable}.
	 * @return a {@link List} holding the elements according to the page specification.
	 */
	public static final <T> List<T> page(List<T> elements, Pageable pageable) {
		int start = (int) pageable.getOffset();
		int end = (start + pageable.getPageSize()) > elements.size() ? elements.size() : (start + pageable.getPageSize());
    	return elements.subList(start, end);
	}

//	/**
//	 * Computes a page of elements based on a given list of elements
//	 * and a pageable.
//	 * 
//	 * @param <T> the data type of the elements.
//	 * @param elements all elements.
//	 * @param pageable the {@link Pageable}.
//	 * @return a {@link Page} holding the elements according to the page specification.
//	 */
//	public static final <T> Page<T> page(List<T> elements, Pageable pageable) {
//		int start = (int) pageable.getOffset();
//		int end = (start + pageable.getPageSize()) > elements.size() ? elements.size() : (start + pageable.getPageSize());
//    	return new PageImpl<T>(elements.subList(start, end), pageable, elements.size());
//	}
	
	/**
	 * Creates page meta data based on a pageable and the total number of elements.
	 * 
	 * @param pageable the {@link Pageable}.
	 * @param total the total number of elements.
	 * @return the created {@link PageMetadata}.
	 */
	public static final PageMetadata metaDataOf(Pageable pageable, int total) {
		return new PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), total);
	}
	
	/**
	 * Creates page meta data based on a pageable and the total number of elements
	 * and total number of pages.
	 * 
	 * @param pageable the {@link Pageable}.
	 * @param total the total number of elements.
	 * @param totalPages the total number of pages.
	 * @return the created {@link PageMetadata}.
	 */
	public static final PageMetadata metaDataOf(Pageable pageable, int total, int totalPages) {
		return new PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), total, totalPages);
	}

}
