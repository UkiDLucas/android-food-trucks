package com.uki.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class ClipboardUtils
{
	private static final String TAG = ClipboardUtils.class.getSimpleName();

	public static void copyToClipboard(Context context, String text)
	{
		Object o = context.getSystemService(Context.CLIPBOARD_SERVICE);

		Method[] allMethods = o.getClass().getDeclaredMethods();

		for (Method m : allMethods)
		{
			if (!m.getName().equals("setText"))
			{
				continue;
			}
			try
			{
				m.invoke(o, new Object[] { text });
			} catch (IllegalArgumentException e)
			{
				Log.e(TAG, e.getMessage(), e);
			} catch (IllegalAccessException e)
			{
				Log.e(TAG, e.getMessage(), e);
			} catch (InvocationTargetException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
			break;
		}
	}
}
