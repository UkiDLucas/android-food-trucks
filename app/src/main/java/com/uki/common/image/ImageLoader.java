package com.uki.common.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.uki.common.util.ConvertUtils;

/**
 * Use it only to load images from assets
 * in other cases better to use this library http://code.google.com/p/libs-for-android/wiki/ImageLoader
 * 
 * @author endryha
 *
 */
@Deprecated
public class ImageLoader
{
	private static final String TAG = "ImageCache [" + ImageLoader.class.getSimpleName() + "]";
	private static final String DEFAULT_CACHE_FOLDER = "data/cache";

	private String cacheDirPath;
	private int stubId = -1;
	private File cacheDir;
	private WeakReference<Context> ctxRef;

	private Stack<Task> queue = new Stack<Task>();
	private Worker workerThread = new Worker();

	private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>()
	{
		@Override
		public SoftReference<Bitmap> get(Object key)
		{
			SoftReference<Bitmap> result = super.get(key);
			if (result != null && result.get() == null)
			{
				result = new SoftReference<Bitmap>(getBitmap((String) key));
				put((String) key, result);
			}
			return result;
		}
	};

	public ImageLoader(Context ctx)
	{
		this(ctx, DEFAULT_CACHE_FOLDER);
	}

	public ImageLoader(Context ctx, String cacheDirPath)
	{
		this.cacheDirPath = cacheDirPath;
		this.ctxRef = new WeakReference<Context>(ctx);
		workerThread.setPriority(Thread.NORM_PRIORITY - 1);

		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), cacheDirPath);
		} else
		{
			cacheDir = ctx.getCacheDir();
		}
		if (!cacheDir.exists())
		{
			cacheDir.mkdirs();
		}
		// clearCache();
	}

	public void setStubId(int resourceId)
	{
		stubId = resourceId;
	}

	public void displayImageFromAsset(final String path, final ImageView imageView)
	{
		if (TextUtils.isEmpty(path) || imageView == null)
		{
			Log.w(TAG, "Null or empty arguments");
		} else
		{
			setStubImage(imageView);
			Bitmap bitmap = null;
			if (cache.containsKey(path))
			{
				SoftReference<Bitmap> bitmapRef = cache.get(path);
				if (bitmapRef != null)
				{
					bitmap = bitmapRef.get();
				}
			}

			if (bitmap != null)
			{
				imageView.setImageBitmap(bitmap);
			} else
			{
				DisplayImageFromAssetTask task = new DisplayImageFromAssetTask(imageView, path);
				task.execute();
			}
		}
	}

	public void displayImage(String url, ImageView imageView)
	{
		setStubImage(imageView);
		displayImageInternal(url, imageView);
	}

	private void setStubImage(ImageView imageView)
	{
		if (-1 != stubId)
		{
			imageView.setImageResource(stubId);
		}
	}

	public void displayImage(String url, ImageView imageView, int orientation, int width, int height)
	{
		imageView.setImageBitmap(getStubBitmap(orientation, width, height));
		displayImageInternal(url, imageView);
	}

	public void preloadImage(String url)
	{
		getBitmap(url);
	}

	private void displayImageInternal(String url, ImageView imageView)
	{
		if (cache.containsKey(url))
		{
			imageView.setImageBitmap(cache.get(url).get());
		} else
		{
			imageView.setTag(url);
			Task task = new Task(url, imageView);
			queue.push(task);
			queue.notifyAll();

			if (workerThread.getState() == Thread.State.NEW)
				workerThread.start();
		}
	}

	private Bitmap getStubBitmap(int orientation, int width, int height)
	{
		Context ctx = ctxRef.get();
		if (ctx != null)
		{
			Bitmap placeholder = BitmapFactory.decodeResource(ctx.getResources(), stubId);
			Matrix scale = new Matrix();

			if (orientation == 1 || orientation == 3)
			{
				Matrix rotate = new Matrix();
				rotate.setRotate(90.0f);
				placeholder = Bitmap.createBitmap(placeholder, 0, 0, placeholder.getWidth(), placeholder.getHeight(), rotate, false);
			}
			scale.setScale((float) placeholder.getWidth() / width, (float) placeholder.getHeight() / height);
			placeholder = Bitmap.createBitmap(placeholder, 0, 0, placeholder.getWidth(), placeholder.getHeight(), scale, false);
			return placeholder;
		} else
		{
			return null;
		}
	}

	public Bitmap getBitmap(String url)
	{
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);

		Bitmap b = ConvertUtils.toBitmap(f);
		if (b != null)
			return b;

		try
		{
			InputStream is = new URL(url).openStream();
			OutputStream os = new FileOutputStream(f);
			ImageLoader.copyStream(is, os, true);
			Bitmap bitmap = ConvertUtils.toBitmap(f);
			return bitmap;
		} catch (Exception e)
		{
			Log.e(TAG, "!!!" + e.getMessage(), e);
			return null;
		}
	}

	public static void copyStream(InputStream is, OutputStream os, boolean closeStreams)
	{
		final int bufferSize = 1024;
		try
		{
			byte[] bytes = new byte[bufferSize];
			for (;;)
			{
				int count = is.read(bytes, 0, bufferSize);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		} finally
		{
			if (closeStreams)
			{
				try
				{
					is.close();
				} catch (IOException e)
				{
				}
				try
				{
					os.close();
				} catch (IOException e)
				{
				}
			}
		}
	}

	private class Task
	{
		public String url;
		public ImageView imageView;

		public Task(String url, ImageView imageView)
		{
			this.url = url;
			this.imageView = imageView;
		}
	}

	private class Worker extends Thread
	{
		private Handler handler = new Handler(Looper.getMainLooper());

		public void run()
		{
			try
			{
				while (true)
				{
					// thread waits until there are any images to load in the
					// queue
					if (queue.isEmpty())
					{
						synchronized (queue)
						{
							queue.wait();
						}
					} else if (queue.size() != 0)
					{
						Task task = queue.pop();
						Log.d("###", "size: " + queue.size());
						Bitmap bmp = getBitmap(task.url);
						if (((String) task.imageView.getTag()).equals(task.url))
						{
							cache.put(task.url, new SoftReference<Bitmap>(bmp));
							DisplayBitmap bd = new DisplayBitmap(bmp, task.imageView);
							handler.post(bd);
						}
					}
					if (Thread.interrupted())
						Log.i(TAG, Worker.class.getSimpleName() + " interrupted!");
					break;
				}
			} catch (InterruptedException e)
			{
				// allow thread to exit
				Log.i(TAG, Worker.class.getSimpleName() + " interrupted!");
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class DisplayBitmap implements Runnable
	{
		private Bitmap bitmap;
		private ImageView imageView;

		public DisplayBitmap(Bitmap b, ImageView i)
		{
			bitmap = b;
			imageView = i;
		}

		public void run()
		{
			if (bitmap != null)
			{
				imageView.setImageBitmap(bitmap);
				Log.d(TAG, "Image was set for url " + imageView.getTag().toString());
			} else
			{
				imageView.setImageResource(stubId);
			}
		}
	}

	public void clearCache()
	{
		// clear memory cache
		cache.clear();

		// clear SD cache
		File[] files = cacheDir.listFiles();
		if (files != null)
		{
			for (File f : files)
			{
				f.delete();
			}
		}
	}

	public File fileForUrl(String url)
	{
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f;
	}

	public File copyFile(String newName, String url)
	{
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		File nf = new File(cacheDir, newName + ".png");
		try
		{
			nf.createNewFile();
			FileInputStream is = new FileInputStream(f);
			OutputStream os = new FileOutputStream(nf);
			ImageLoader.copyStream(is, os, true);
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return nf;
	}

	private class DisplayImageFromAssetTask extends AsyncTask<Void, Void, Bitmap>
	{
		private final ImageView imageView;
		private final String path;

		public DisplayImageFromAssetTask(ImageView imageView, String path)
		{
			super();
			this.imageView = imageView;
			this.path = path;
		}

		@Override
		protected Bitmap doInBackground(Void... params)
		{
			try
			{
				Context ctx = ctxRef.get();
				if (ctx != null)
				{
					InputStream is = ctx.getAssets().open(path);
					Bitmap bitmap = ConvertUtils.toBitmap(is);
					cache.put(path, new SoftReference<Bitmap>(bitmap));
					return bitmap;
				} else
				{
					return null;
				}
			} catch (IOException e)
			{
				Log.e(TAG, "Image asset is not found " + path);
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result)
		{
			if (result != null)
			{
				imageView.setImageBitmap(result);
			}
		}
	}

}
