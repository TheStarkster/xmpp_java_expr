package com.example.xmpp_java.bitmapcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.xmpp_java.utils.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BitmapUtils {
	private static final String TAG = "ImageCache";
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// BEGIN_INCLUDE (calculate_sample_size)
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
	
//	private static Bitmap addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
//        //BEGIN_INCLUDE(add_bitmap_options)
//        // inBitmap only works with mutable bitmaps so force the decoder to
//        // return mutable bitmaps.
//        options.inMutable = true;
//
//        if (cache != null) {
//            // Try and find a bitmap to use for inBitmap
//            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
//
//            if (inBitmap != null) {
//                options.inBitmap = inBitmap;
//                return inBitmap;
//            }
//        }
//
//        return null;
//    }

	public static Bitmap decodeSampledBitmapFromStream(Context context, Uri uri, int reqWidth, int reqHeight) {
		InputStream inputStream;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
		} catch(FileNotFoundException e) {
			return null;
		}
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(inputStream, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
		} catch(FileNotFoundException e) {
			return null;
		}
		return BitmapFactory.decodeStream(inputStream, null, options);
	}
}