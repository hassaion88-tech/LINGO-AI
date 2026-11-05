
package com.lingoai.translator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.text.InputType
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var fromLanguage: Spinner
    private lateinit var toLanguage: Spinner

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
        }

        val subtitle = TextView(this).apply {
            text = "ترجم العالم بين يديك"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        fromLanguage = Spinner(this)
        toLanguage = Spinner(this)

        val languageNames = languages.map { it.first }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languageNames
        )

        fromLanguage.adapter = adapter
        toLanguage.adapter = adapter

        fromLanguage.setSelection(0)
        toLanguage.setSelection(1)

        inputText = EditText(this).apply {
            hint = "اكتب النص الذي تريد ترجمته..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setPadding(18, 20, 18, 20)
            minLines = 5
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundColor(Color.rgb(29, 39, 60))
        }

        val translateButton = Button(this).apply {
            text = "ترجم الآن"
            textSize = 18f
        }

        outputText = TextView(this).apply {
            text = "ستظهر الترجمة هنا..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(18, 25, 18, 25)
            setBackgroundColor(Color.rgb(29, 39, 60))
            minLines = 4
        }

        val scrollView = ScrollView(this)

        root.addView(title)
        root.addView(subtitle)

        root.addView(space(25))
        root.addView(label("لغة النص الأصلية"))
        root.addView(fromLanguage)

        root.addView(space(15))
        root.addView(label("لغة الترجمة"))
        root.addView(toLanguage)

        root.addView(space(20))
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

        val source = languages[fromLanguage.selectedItemPosition].second
        val target = languages[toLanguage.selectedItemPosition].second

        outputText.text = "جاري الترجمة..."

        thread {
            try {
                val encoded = URLEncoder.encode(text, "UTF-8")

                val url = URL(
                    "https://api.mymemory.translated.net/get?q=$encoded&langpair=$source|$target"
                )

                val connection = url.openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val response = connection.getInputStream()
                    .bufferedReader()
                    .use { it.readText() }

                val json = JSONObject(response)
                val translated = json
                    .getJSONObject("responseData")
                    .getString("translatedText")

                runOnUiThread {
                    outputText.text = translated
                }

                connection.getInputStream().close()
                connection.disconnect()

            } catch (e: Exception) {
                runOnUiThread {
                    outputText.text =
                        "تعذرت الترجمة. تحقق من الإنترنت وحاول مجدداً."
                }
            }
        }
    }
}
