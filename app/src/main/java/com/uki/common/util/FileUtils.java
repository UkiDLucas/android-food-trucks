package com.uki.common.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * @author Andrii Kovalov
 */
public class FileUtils
{
    public static String getMimeType(File file)
    {
        String mimeType = "*/*";

        String extension = StringUtils.getFileExtension(file);
        if (!TextUtils.isEmpty(extension))
        {
            String detectedMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (!TextUtils.isEmpty(detectedMimeType))
            {
                mimeType = detectedMimeType;
            }
        }
        return mimeType;
    }

    public static void openFile(Context ctx, File file)
    {
        String mimeType = FileUtils.getMimeType(file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType);

        Intent intentChooser = Intent.createChooser(intent, "Open file");
        intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ctx.startActivity(intentChooser);
    }
}
