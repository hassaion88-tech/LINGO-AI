
package com.lingoai.translator

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var fromButton: Button
    private lateinit var toButton: Button
    private lateinit var translateButton: Button

    private var sourceIndex = 0
    private var targetIndex = 1

    private val languages = arrayOf(
        "العربية" to "ar",
        "English" to "en",
        "French" to "fr",
        "German" to "de",
        "Spanish" to "es",
        "Turkish" to "tr",
        "Persian" to "fa",
        "Russian" to "ru",
        "Chinese" to "zh",
        "Japanese" to "ja",
        "Korean" to "ko",
        "Italian" to "it",
        "Portuguese" to "pt",
        "Hindi" to "hi"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createInterface()
    }

    private fun createInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 24)
            setBackgroundColor(Color.rgb(12, 18, 35))
        }

        val title = TextView(this).apply {
            text = "LINGO AI"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "ترجم العالم بين يديك"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        fromButton = Button(this).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            isAllCaps = false
            background = roundedBackground(
                Color.rgb(211, 47, 47)
            )
        }

        toButton = Button(this).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            isAllCaps = false
            background = roundedBackground(
                Color.rgb(22, 132, 71)
            )
        }

        updateLanguageButtons()

        fromButton.setOnClickListener {
            chooseLanguage(true)
        }

        toButton.setOnClickListener {
            chooseLanguage(false)
        }

        val languageRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        languageRow.addView(
            fromButton,
            LinearLayout.LayoutParams(0, 60, 1f).apply {
                marginEnd = 6
            }
        )

        languageRow.addView(
            toButton,
            LinearLayout.LayoutParams(0, 60, 1f).apply {
                marginStart = 6
            }
        )

        inputText = EditText(this).apply {
            hint = "اكتب النص الذي تريد ترجمته..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(18, 20, 18, 20)
            minLines = 5
            gravity = Gravity.TOP or Gravity.START
            textDirection = View.TEXT_DIRECTION_FIRST_STRONG
            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            background = roundedBackground(
                Color.rgb(29, 39, 60)
            )
        }

        translateButton = Button(this).apply {
            text = "ترجم الآن"
            textSize = 18f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = roundedBackground(
                Color.rgb(40, 100, 190)
            )
        }

        outputText = TextView(this).apply {
            text = "ستظهر الترجمة هنا..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(18, 25, 18, 25)
            background = roundedBackground(
                Color.rgb(29, 39, 60)
            )
            minLines = 4
            gravity = Gravity.TOP or Gravity.START
            textDirection = View.TEXT_DIRECTION_FIRST_STRONG
            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }

        val scrollView = ScrollView(this)

        root.addView(title)
        root.addView(subtitle)

        root.addView(space(25))
        root.addView(languageRow)

        root.addView(space(20))
        root.addView(label("النص الأصلي"))
        root.addView(inputText)

        root.addView(space(15))
        root.addView(translateButton)

        root.addView(space(20))
        root.addView(label("الترجمة"))
        root.addView(outputText)

        scrollView.addView(root)
        setContentView(scrollView)

        translateButton.setOnClickListener {
            translateText()
        }
    }

    private fun chooseLanguage(isSource: Boolean) {

        val names = languages.map { it.first }.toTypedArray()

        val selected = if (isSource) {
            sourceIndex
        } else {
            targetIndex
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (isSource) "اختر لغة النص الأصلي"
                else "اختر لغة الترجمة"
            )
            .setSingleChoiceItems(names, selected) { dialog, which ->

                if (isSource) {
                    sourceIndex = which
                } else {
                    targetIndex = which
                }

                updateLanguageButtons()
                dialog.dismiss()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun updateLanguageButtons() {

        fromButton.text =
            "🔴 من: ${languages[sourceIndex].first}"

        toButton.text =
            "🟢 إلى: ${languages[targetIndex].first}"
    }

    private fun roundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 20f
        }
    }

    private fun label(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(4, 8, 4, 8)
        }
    }

    private fun space(height: Int): TextView {
        return TextView(this).apply {
            this.height = height
        }
    }

    private fun translateText() {

        val text = inputText.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(
                this,
                "اكتب النص أولاً",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val source = languages[sourceIndex].second
        val target = languages[targetIndex].second

        if (source == target) {
            outputText.text = text
            return
        }

        translateButton.isEnabled = false
        outputText.text = "جاري الترجمة..."

        thread {
            var connection: HttpURLConnection? = null

            try {
                val encoded = URLEncoder.encode(text, "UTF-8")

                val url = URL(
                    "https://api.mymemory.translated.net/get?q=$encoded&langpair=$source|$target"
                )

                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val json = JSONObject(response)

                val translated = json
                    .getJSONObject("responseData")
                    .getString("translatedText")

                runOnUiThread {
                    outputText.text = translated
                    outputText.textDirection =
                        View.TEXT_DIRECTION_FIRST_STRONG
                    translateButton.isEnabled = true
                }

            } catch (e: Exception) {

                runOnUiThread {
                    outputText.text =
                        "تعذرت الترجمة. تحقق من الإنترنت وحاول مجدداً."
                    translateButton.isEnabled = true
                }

            } finally {
                connection?.disconnect()
            }
        }
    }
}
