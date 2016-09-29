package com.uki.common.bind;

import android.os.Binder;

public abstract class SimpleBinder<T> extends Binder
{
	public abstract T getObject();
}
