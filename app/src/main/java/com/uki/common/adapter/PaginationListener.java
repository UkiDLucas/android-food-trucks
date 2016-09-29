package com.uki.common.adapter;

/**
 * @author Andrii Kovalov
 */
public interface PaginationListener<T>
{
	void onNewPage(Page<T> page);
}
