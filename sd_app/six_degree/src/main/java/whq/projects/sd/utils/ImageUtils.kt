package whq.projects.sd.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import whq.projects.sd.R
import whq.projects.entities.MOBILEMD5_NAME
import whq.projects.utilities.*
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt


/**
 * 创建二维码位图
 *
 * @param content 字符串内容(支持中文)
 * @param width 位图宽度(单位:px)
 * @param height 位图高度(单位:px)
 * @return
 */
object ImageUtils {
    private fun getColor(first: Char, second: Char, third: Char): Int {
        val c1 = first.toString().toPinyinCaptial().first()
        val c2 = second.toString().toPinyinCaptial().first()
        val c3 = third.toString().toPinyinCaptial().first()
        val r = getAlphaColor(c1)
        val g = getAlphaColor(c2)
        val b = getAlphaColor(c3)
        return Color.rgb(r, g, b)
    }

    private fun getAlphaColor(c: Char): Int {
        val cons = 'Z'.toInt() - 'A'.toInt()
        val diff = abs(c.toUpperCase().toInt() - 'A'.toInt())
        val percent = abs(diff.toFloat() / cons)
        return (200 * percent).toInt() % 200
    }

    fun getTextBitmap(text: String, charSize: Int = 120, color: Int = R.color.md_blue_700): Bitmap? {
        val margin = charSize / 20
        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.createFromAsset(AppCommon.instance.assets, "fonts/zhuan_ti.ttf")
            textAlign = Paint.Align.LEFT
            textSize = charSize.toFloat() * 1.1f
            setColor(AppCommon.instance.resources.getColor(color))
        }
        val bitmap = Bitmap.createBitmap((charSize + 2 * margin) * text.length, charSize + 2 * margin, Bitmap.Config.ARGB_8888).also {
            Canvas(it).run {
                text.forEachIndexed { index, c ->
                    drawText(c.toString(), ((charSize + 2 * margin) * index + margin).toFloat(), (charSize + margin).toFloat(), paint)
                }
            }
        }
        return bitmap
    }

    fun getNamesBitmap(names: List<String>, sizeMax: Int = 240): Bitmap? {
        val nameList = names.map { it.removeNotChinese() }
        val margin = sizeMax.toFloat() / 40
        val namesList = namesValid(nameList)
        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.createFromAsset(AppCommon.instance.assets, "fonts/zhuan_ti.ttf")
            textAlign = Paint.Align.LEFT
        }
        val paintLine = Paint().apply {
            isAntiAlias = true
            color = AppCommon.instance.resources.getColor(R.color.md_pink_700)
            strokeWidth = 1F
        }
        val fontSizePercent = 1.1f
        fun getPaint(name: String, fontSize: Float): Paint {
            val (first, second, third) = ("${MOBILEMD5_NAME.NULL}$name").takeLast(3).toCharArray()
            return paint.apply {
                color = getColor(first, second, third)
                textSize = fontSize
            }
        }


        val bitmap = when (namesList.size) {
            0, 1 -> {
                val heightCanvas = sizeMax / 2
                val fontSize = (sizeMax / 2 - 2 * margin) * fontSizePercent
                Bitmap.createBitmap(sizeMax, heightCanvas, Bitmap.Config.ARGB_8888).also {
                    Canvas(it).run {
                        val name = namesList.firstOrNull() ?: AppCommon.instance.appNameChinese()
                        drawText(name.takeLast(2), margin, (heightCanvas - margin), getPaint(name, fontSize))
                    }
                }
            }
            2 -> {
                val name0 = namesList[0].takeLast(2)
                val name1 = namesList[1].takeLast(2)
                val heightCanvas = sizeMax
                val fontSize = (sizeMax / 2 - 2 * margin) * fontSizePercent
                Bitmap.createBitmap(sizeMax, heightCanvas, Bitmap.Config.ARGB_8888).also {
                    Canvas(it).run {
                        drawText(name0, margin, (heightCanvas / 2 - margin), getPaint(name0, fontSize))
                        drawLine(margin, (heightCanvas / 2).toFloat(), sizeMax - margin, (heightCanvas / 2).toFloat(), paintLine)
                        drawText(name1, margin, (heightCanvas - margin), getPaint(name1, fontSize))
                    }
                }
            }
            3 -> {
                val name0 = namesList[0].takeLast(2)
                val name1 = namesList[1].takeLast(2)
                val name2 = namesList[2].takeLast(2)
                val heightCanvas = sizeMax / 2
                val fontSize = (sizeMax / 4 - 2 * margin) * fontSizePercent
                Bitmap.createBitmap(sizeMax, heightCanvas, Bitmap.Config.ARGB_8888).also {
                    Canvas(it).run {
                        drawText(name0, (sizeMax / 4 + margin), heightCanvas / 2 - margin, getPaint(name0, fontSize))
                        drawLine(margin, (heightCanvas / 2).toFloat(), sizeMax - margin, (heightCanvas / 2).toFloat(), paintLine)
                        drawText(name1, margin, (heightCanvas - margin), getPaint(name1, fontSize))
                        drawText(name2, sizeMax / 2 + margin, (heightCanvas - margin), getPaint(name2, fontSize))
                    }
                }
            }
            4 -> {
                val name0 = namesList[0].takeLast(2)
                val name1 = namesList[1].takeLast(2)
                val name2 = namesList[2].takeLast(2)
                val name3 = namesList[3].takeLast(2)
                val heightCanvas = sizeMax / 2
                val fontSize = (sizeMax / 4 - 2 * margin) * fontSizePercent
                Bitmap.createBitmap(sizeMax, heightCanvas, Bitmap.Config.ARGB_8888).also {
                    Canvas(it).run {
                        drawText(name0, margin, heightCanvas / 2 - margin, getPaint(name0, fontSize))
                        drawText(name1, (sizeMax / 2 + margin), heightCanvas / 2 - margin, getPaint(name1, fontSize))
                        drawLine(margin, (heightCanvas / 2).toFloat(), sizeMax - margin, (heightCanvas / 2).toFloat(), paintLine)
                        drawText(name2, margin, (heightCanvas - margin), getPaint(name2, fontSize))
                        drawText(name3, sizeMax / 2 + margin, (heightCanvas - margin), getPaint(name3, fontSize))
                        drawLine((sizeMax / 2).toFloat(), margin, (sizeMax / 2).toFloat(), heightCanvas - margin, paintLine)
                    }
                }
            }
            else -> TODO()
        }
        return bitmap
    }

    fun namesValid(nameList: List<String>): List<String> {
        return nameList
            .map { it.replace(Regex("[^a-zA-Z\\u4e00-\\u9fa5]+"), "") }
            .filter { it.length >= 2 }
            .sortedWith(comparatorChinese)
            .take(4)
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, roundPx: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, w, h)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    @SuppressLint("ObsoleteSdkInt")
    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @JvmStatic
    fun main(args: Array<String>) {

    }

    fun merge(toMerge: List<Bitmap>, size: Int): Bitmap {
        val bitmaps = toMerge.take(9)
        val avgCount: Int = ceil(sqrt(bitmaps.size.toDouble())).toInt()
        val avgW: Int = size / avgCount
        val avgH: Int = size / avgCount
        val result: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        repeat(bitmaps.size) {
            val top: Int = (it / avgCount) * avgH
            val left = (it % avgCount) * avgW
            canvas.drawBitmap(resize(bitmaps[it], avgW.toFloat() / bitmaps[it].width), left.toFloat(), top.toFloat(), Paint())
        }
        return result
    }

    fun size(bitmap: Bitmap) = bitmap.width * bitmap.height * 4 / 1024
    fun resize(bitmap: Bitmap, times: Float): Bitmap {
        val matrix = Matrix()
        matrix.postScale(times, times)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}