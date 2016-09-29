package com.uki.common.bind;

import java.lang.ref.WeakReference;

public class WeakBinder<T> extends SimpleBinder<T>
{
	private WeakReference<T> objectHolder;

	public WeakBinder(T object)
	{
		objectHolder = new WeakReference<T>(object);
	}

	public T getObject()
	{
		return objectHolder.get();
	}
}
