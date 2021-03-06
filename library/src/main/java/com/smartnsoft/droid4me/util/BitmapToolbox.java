// The MIT License (MIT)
//
// Copyright (c) 2017 Smart&Soft
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.smartnsoft.droid4me.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.widget.ImageView.ScaleType;

/**
 * A toolbox of {@link Bitmap} functions, that handle bitmap transformations.
 *
 * @author Édouard Mercier
 * @since 2010.02.27
 */
public final class BitmapToolbox
{

  /**
   * @return the bitmap itself, but turned into a gray-scale
   */
  // Taken from http://stackoverflow.com/questions/1793338/drawable-grayscale
  public static Bitmap turnToGrayScale(Bitmap bitmap)
  {
    final Canvas canvas = new Canvas(bitmap);
    final Paint paint = new Paint();
    final ColorMatrix colorMatrix = new ColorMatrix();
    colorMatrix.setSaturation(0);
    final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
    paint.setColorFilter(colorFilter);
    canvas.drawBitmap(bitmap, 0, 0, paint);
    return bitmap;
  }

  /**
   * Flips the provided bitmap horizontally or vertically.
   * <p>
   * <p>
   * Warning, the original bitmap is recycled and cannot be used after this call!
   * </p>
   *
   * @param bitmap     the bitmap to flip
   * @param horizontal indicates the flip direction
   * @return the transformed bitmap
   */
  public static Bitmap flipBitmap(Bitmap bitmap, boolean horizontal)
  {
    final Matrix matrix = new Matrix();
    matrix.preScale(horizontal == false ? 1 : -1, horizontal == false ? -1 : 1);
    final Bitmap flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    // We free the original bitmap image
    bitmap.recycle();
    return flippedBitmap;
  }

  /**
   * Merges a little bitmap to a larger one.
   * <p>
   * <p>
   * Warning, the original bitmaps are recycled and cannot be used after this call!
   * </p>
   *
   * @param bigBitmap    this object is {@link Bitmap#recycle() recycled} once the method has returned
   * @param littleBitmap this object is {@link Bitmap#recycle() recycled} once the method has returned
   * @param left         the left of the top-left corner position on the big bitmap where to place the little bitmap
   * @param left         the top of the top-left corner position on the big bitmap where to place the little bitmap
   * @return a bitmap which is a merge of the two bitmaps
   */
  public static Bitmap mergeBitmaps(Bitmap bigBitmap, Bitmap littleBitmap, int left, int top)
  {
    final int width = bigBitmap.getWidth();
    final int height = bigBitmap.getHeight();
    final Matrix matrix = new Matrix();
    matrix.preScale(1, -1);
    final Bitmap bitmap = Bitmap.createBitmap(bigBitmap, 0, 0, width, height, matrix, false);
    final Canvas canvas = new Canvas(bitmap);
    canvas.drawBitmap(bigBitmap, 0, 0, null);
    canvas.drawBitmap(littleBitmap, left, top, null);
    bigBitmap.recycle();
    littleBitmap.recycle();
    return bitmap;
  }

  /**
   * Computes a new bitmap with a reflect at the bottom.
   * <p>
   * <p>
   * Warning, the original bitmap is recycled and cannot be used after this call!
   * </p>
   *
   * @param bitmap             the bitmap used for the reflection
   * @param reflectionRatio    the percentage of the input bitmap height (between 0 and 1) which indicates the height of the reflect
   * @param reflectionGap      the number of pixels that should separate the bitmap and its reflect
   * @param startGradientColor the color (with an alpha properly set), which is used to start the reflection gradient
   * @param endGradientColor   the color (with an alpha properly set), which is used to end the reflection gradient; a typical value is <code>0x00ffffff</code>
   * @return a new bitmap, which contains the reflect
   */
  public static Bitmap computeReflectedBitmap(Bitmap bitmap, float reflectionRatio, int reflectionGap,
      int startGradientColor, int endGradientColor)
  {
    final int width = bitmap.getWidth();
    final int height = bitmap.getHeight();

    // This will not scale but will flip on the Y axis
    final Matrix matrix = new Matrix();
    matrix.preScale(1, -1);

    // Create a Bitmap with the flip matrix applied to it.
    // We only want the bottom half of the image
    final Bitmap reflectionBitmap = Bitmap.createBitmap(bitmap, 0, height - (int) (height * reflectionRatio), width, (int) (height * reflectionRatio), matrix, false);

    // Create a new bitmap with same width but taller to fit reflection
    final Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height + (int) (height * reflectionRatio), Config.ARGB_8888);

    // Create a new Canvas with the bitmap that's big enough for the image, plus the gap, plus the reflection
    final Canvas canvas = new Canvas(bitmapWithReflection);
    // Draw in the original image
    canvas.drawBitmap(bitmap, 0, 0, null);
    // Draw in the gap
    final Paint firstPaint = new Paint();
    canvas.drawRect(0, height, width, height + reflectionGap, firstPaint);
    // Draw in the reflection
    canvas.drawBitmap(reflectionBitmap, 0, height + reflectionGap, null);

    // Create a shader that is a linear gradient that covers the reflection
    final Paint paint = new Paint();
    final LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, startGradientColor, endGradientColor, TileMode.CLAMP);
    // Set the paint to use this shader (linear gradient)
    paint.setShader(shader);
    // Set the Transfer mode to be porter duff and destination in
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    // Draw a rectangle using the paint with our linear gradient
    canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

    // We free the original bitmap image
    bitmap.recycle();

    return bitmapWithReflection;
  }

  /**
   * Resizes a bitmap so that it is filled with transparency in order to match the expected dimension. No scaling is performed.
   * <p>
   * <p>
   * Warning, the original bitmap is recycled and cannot be used after this call; furthermore, it is supposed that the provided image is smaller in
   * both dimension that the provided dimensions!
   * </p>
   *
   * @param bitmap the bitmap to enlarge
   * @param width  the new width of the image, which much be greater or equal than the provided bitmap width
   * @param height the new height of the image, which much be greater or equal than the provided bitmap height
   * @return the resized image
   */
  public static Bitmap enlarge(Bitmap bitmap, int width, int height)
  {
    // Create a new bitmap with the final size
    final Bitmap resizedBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    // Create a new Canvas with the bitmap that's big enough for the image, plus the gap, plus the reflection
    final Canvas canvas = new Canvas(resizedBitmap);
    // Draw in the original image
    canvas.drawBitmap(bitmap, ((float) width - (float) bitmap.getWidth()) / 2f, ((float) height - (float) bitmap.getHeight()) / 2f, null);
    // We free the original bitmap image
    bitmap.recycle();
    return resizedBitmap;
  }

  /**
   * Generates a translucent bitmap with the provided dimensions, and draws inside (centered) the provided bitmap, while respecting its original
   * ratio.
   *
   * @param bitmap    the bitmap to draw
   * @param scaleType if set to {@code ScaleType#FIT_CENTER} and the provided bitmap both dimensions are smaller than the target frame, then the provided
   *                  bitmap is scaled so as to fill as much as possible the frame
   * @param width     the target frame width
   * @param height    the target frame height
   * @param config    the newly created bitmap configuration
   * @param recycle   if set to {@code true}, the provided bitmap is {@link Bitmap#recycle() recycled}
   * @return a bitmap with the provided dimensions
   */
  public static Bitmap scale(Bitmap bitmap, ScaleType scaleType, int width, int height, Bitmap.Config config,
      boolean recycle)
  {
    // Create a new bitmap with the final size
    final Bitmap resizedBitmap = Bitmap.createBitmap(width, height, config);
    // Create a new Canvas with the bitmap that's big enough for the image, plus the gap, plus the reflection
    final Canvas canvas = new Canvas(resizedBitmap);

    // We compute the ratio to apply
    final float widthRatio = (float) width / (float) bitmap.getWidth();
    final float heightRatio = (float) height / (float) bitmap.getHeight();
    final float ratio;
    if (widthRatio < 1f && heightRatio < 1f)
    {
      // The bitmap is larger and higher than the frame
      ratio = Math.min(widthRatio, heightRatio);
    }
    else if (widthRatio < 1f)
    {
      // The bitmap is larger but not higher than the frame
      ratio = widthRatio;
    }
    else if (heightRatio < 1f)
    {
      // The bitmap is higher but not larger than the frame
      ratio = heightRatio;
    }
    else
    {
      if (scaleType == ScaleType.FIT_CENTER)
      {
        ratio = Math.min(widthRatio, heightRatio);
      }
      else
      {
        ratio = 1f;
      }
    }
    final int newBitmapWidth = (int) ((float) bitmap.getWidth() * ratio);
    final int newBitmapHeight = (int) ((float) bitmap.getHeight() * ratio);

    // Draw in the original image in the target frame
    final int horizontalPadding = (width - newBitmapWidth) / 2;
    final int verticalPadding = (height - newBitmapHeight) / 2;
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    canvas.drawBitmap(bitmap, null, new Rect(horizontalPadding, verticalPadding, horizontalPadding + newBitmapWidth, verticalPadding + newBitmapHeight), paint);
    // We free the original bitmap image
    if (recycle == true)
    {
      bitmap.recycle();
    }
    return resizedBitmap;
  }

}
