package com.uki.common.bind;

public class StrongBinder<T> extends SimpleBinder<T>
{
	private T target;

	public StrongBinder(T object)
	{
		target = object;
	}

	public T getObject()
	{
		return target;
	}
}
