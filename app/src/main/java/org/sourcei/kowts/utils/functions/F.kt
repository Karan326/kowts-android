/**
 * ISC License
 *
 * Copyright 2018-2019, Saksham (DawnImpulse)
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
 * OR PERFORMANCE OF THIS SOFTWARE.
 **/
package org.sourcei.kowts.utils.functions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.setMargins
import kotlinx.android.synthetic.main.inflator_quote_empty.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sourcei.kowts.R
import org.sourcei.kowts.network.Model
import org.sourcei.kowts.utils.pojo.ObjectQuote
import org.sourcei.kowts.utils.pojo.PojoQuotes
import org.sourcei.kowts.utils.reusables.Paper
import org.sourcei.kowts.utils.reusables.QUOTES


/**
 * @info -
 *
 * @author - Saksham
 * @note Last Branch Update - master
 *
 * @note Created on 2019-08-20 by Saksham
 * @note Updates :
 * Saksham - 2019 09 06 - master - compare bitmap
 * Saksham - 2019 09 07 - master - handing of multiple quotes
 * Saksham - 2019 09 11 - master - generate quote bitmap for storage
 * Saksham - 2019 09 13 - master - generate quote alignment
 */
object F {

    private fun getBitmapFromView(view: View): Bitmap {

        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    // Generating random color
    private fun randomColor(): String {
        val chars = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
        var color = "#"
        for (i in 1..6) {
            color += chars[Math.floor(Math.random() * chars.size).toInt()]
        }
        return color
    }

    // Generate random gradient
    fun randomGradient(): List<Int> {
        val count = 2
        val angle = (0..180).random()
        val colors = mutableListOf<Int>()

        for (i in 1..count) {
            colors.add(randomColor().toColorInt())
        }


        return colors

    }

    // convert dp - px
    fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // get display height
    fun displayDimensions(context: Context): Point {
        val point = Point()
        val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = mWindowManager.defaultDisplay
        display.getSize(point) //The point now has display dimens
        return point
    }

    // verify two bitmaps
    fun compareBitmaps(b1: Bitmap?, b2: Bitmap?, callback: (Boolean) -> Unit) {

        if (b1 == null || b2 == null) {
            callback(false)
        } else
            GlobalScope.launch {
                try {
                    callback(b1.sameAs(b2)) // callback with compare
                } catch (e: Exception) {
                    //Crashlytics.logException(e)
                    e.printStackTrace()
                    callback(false)
                }
            }
    }

    // get quote
    fun getQuote(activity: AppCompatActivity, callback: (PojoQuotes?) -> Unit) {

        val quotes: MutableList<PojoQuotes>? = Paper.read(QUOTES, null)

        // quote available in prefs
        if (quotes != null) {
            callback(quotes[0])

            if (quotes.size != 1)
                Paper.write(QUOTES, quotes.subList(1, quotes.size))
            else
                Paper.delete(QUOTES)

        } else {
            // fetch quote from server
            Model(activity).getRandomQuote { e, r ->
                e?.let {
                    loge(e)
                    callback(null)
                }
                r?.let {
                    callback(it[0])
                    Paper.write(QUOTES, it.subList(1, it.size))
                }
            }
        }
    }

    // generate quote bitmap
    fun generateBitmap(context: Context, quoteObject: ObjectQuote): Bitmap {
        val layout = LayoutInflater.from(context).inflate(R.layout.inflator_quote_empty, null)
        val card = layout.card
        val quote = layout.quote
        val authorText = layout.author
        val image = layout.image
        val gradient = layout.gradient
        val authorLayout = layout.authorLayout

        // set values
        image.setImageBitmap(quoteObject.image)
        authorText.text = quoteObject.author
        quote.text = quoteObject.quote

        // set dimensions for card
        val point = displayDimensions(context)
        val margin = dpToPx(16, context)
        val x = point.x
        val y = x

        val params = FrameLayout.LayoutParams(x, y)
        card.layoutParams = params


        // new params
        val paramsNQ = RelativeLayout.LayoutParams(x, 3 * y / 4)
        val paramsNA = RelativeLayout.LayoutParams(authorLayout.layoutParams.width, authorLayout.layoutParams.height)

        // alignment quote
        quote.gravity = when (quoteObject.quoteAlign) {
            0 -> Gravity.LEFT
            1 -> Gravity.CENTER
            else -> Gravity.RIGHT
        }

        // align author
        when (quoteObject.authorAlign) {
            0 -> paramsNA.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            1 -> paramsNA.addRule(RelativeLayout.CENTER_HORIZONTAL)
            2 -> paramsNA.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        }

        // set new params
        paramsNQ.setMargins(margin)
        paramsNA.setMargins(margin, 0, margin, margin)
        paramsNA.addRule(RelativeLayout.BELOW, R.id.quote)

        quote.layoutParams = paramsNQ
        authorLayout.layoutParams = paramsNA

        // set gradients
        gradient.setGradient(quoteObject.gradient, 0, quoteObject.angle)
        authorLayout.setGradient(quoteObject.authorGradient, 16)

        // prepare for export
        layout.measure(View.MeasureSpec.makeMeasureSpec(x, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(y, View.MeasureSpec.EXACTLY))
        layout.layout(0, 0, layout.measuredWidth, layout.measuredHeight)

        return getBitmapFromView(layout)
    }
}