
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

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation

class MainActivity : Activity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var fromButton: Button
    private lateinit var toButton: Button
    private lateinit var translateButton: Button

    private var sourceIndex = 0
    private var targetIndex = 1

    private val modelManager by lazy {
        RemoteModelManager.getInstance()
    }

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
            background = roundedBackground(Color.rgb(211, 47, 47))
        }

        toButton = Button(this).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            isAllCaps = false
            background = roundedBackground(Color.rgb(22, 132, 71))
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
            background = roundedBackground(Color.rgb(29, 39, 60))
        }

        translateButton = Button(this).apply {
            text = "ترجم الآن"
            textSize = 18f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = roundedBackground(Color.rgb(40, 100, 190))
        }

        val manageButton = Button(this).apply {
            text = "إدارة اللغات وتنزيلها"
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = roundedBackground(Color.rgb(75, 85, 110))
        }

        outputText = TextView(this).apply {
            text = "ستظهر الترجمة هنا..."
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(18, 25, 18, 25)
            background = roundedBackground(Color.rgb(29, 39, 60))
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

        root.addView(space(10))
        root.addView(manageButton)

        root.addView(space(20))
        root.addView(label("الترجمة"))
        root.addView(outputText)

        scrollView.addView(root)
        setContentView(scrollView)

        translateButton.setOnClickListener {
            translateText()
        }

        manageButton.setOnClickListener {
            showLanguageManager()
        }
    }

    private fun chooseLanguage(isSource: Boolean) {

        val names = languages.map { it.first }.toTypedArray()

        val selected = if (isSource) sourceIndex else targetIndex

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
        fromButton.text = "🔴 من: ${languages[sourceIndex].first}"
        toButton.text = "🟢 إلى: ${languages[targetIndex].first}"
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

    private fun mlLanguage(index: Int): String? {
        return TranslateLanguage.fromLanguageTag(
            languages[index].second
        )
    }

    private fun getModel(language: String): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(language).build()
    }

    // إدارة حزم اللغات
    private fun showLanguageManager() {

        val names = languages.map { it.first }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("إدارة حزم اللغات")
            .setItems(names) { _, which ->
                manageOneLanguage(which)
            }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    private fun manageOneLanguage(index: Int) {

        val language = mlLanguage(index)

        if (language == null) {
            Toast.makeText(
                this,
                "هذه اللغة غير مدعومة من محرك الترجمة",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val model = getModel(language)

        modelManager.isModelDownloaded(model)
            .addOnSuccessListener { downloaded ->

                if (downloaded) {
                    AlertDialog.Builder(this)
                        .setTitle(languages[index].first)
                        .setMessage("الحزمة موجودة على جهازك.")
                        .setPositiveButton("حذف الحزمة") { _, _ ->
                            deleteLanguageModel(model, index)
                        }
                        .setNegativeButton("إغلاق", null)
                        .show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(languages[index].first)
                        .setMessage(
                            "هذه اللغة غير منزّلة.\n" +
                            "حجم النموذج يقارب 30 ميغابايت.\n" +
                            "التنزيل يحتاج إنترنت."
                        )
                        .setPositiveButton("تنزيل") { _, _ ->
                            downloadLanguageModel(model, index)
                        }
                        .setNegativeButton("إلغاء", null)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "تعذر فحص حالة الحزمة",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun downloadLanguageModel(
        model: TranslateRemoteModel,
        index: Int
    ) {

        val progress = ProgressBar(this)

        val dialog = AlertDialog.Builder(this)
            .setTitle("تنزيل ${languages[index].first}")
            .setMessage("جارٍ تنزيل حزمة اللغة...")
            .setView(progress)
            .setCancelable(false)
            .create()

        dialog.show()

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        modelManager.download(model, conditions)
            .addOnSuccessListener {
                dialog.dismiss()

                AlertDialog.Builder(this)
                    .setTitle("اكتمل التنزيل")
                    .setMessage(
                        "تم تنزيل حزمة ${languages[index].first} بنجاح."
                    )
                    .setPositiveButton("حسناً", null)
                    .show()
            }
            .addOnFailureListener { error ->
                dialog.dismiss()

                AlertDialog.Builder(this)
                    .setTitle("فشل التنزيل")
                    .setMessage(
                        "لم نتمكن من تنزيل الحزمة.\n" +
                        "تحقق من اتصال الإنترنت والمساحة المتوفرة.\n\n" +
                        "التفاصيل: ${error.localizedMessage ?: "خطأ غير معروف"}"
                    )
                    .setPositiveButton("حسناً", null)
                    .show()
            }
    }

    private fun deleteLanguageModel(
        model: TranslateRemoteModel,
        index: Int
    ) {

        modelManager.deleteDownloadedModel(model)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "تم حذف حزمة ${languages[index].first}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "تعذر حذف الحزمة",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // فحص وجود نموذج اللغة
    private fun checkModel(
        language: String,
        callback: (Boolean) -> Unit
    ) {
        modelManager.isModelDownloaded(getModel(language))
            .addOnSuccessListener { downloaded ->
                callback(downloaded)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // الترجمة المحلية
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

        val source = mlLanguage(sourceIndex)
        val target = mlLanguage(targetIndex)

        if (source == null || target == null) {
            outputText.text = "إحدى اللغتين غير مدعومة."
            return
        }

        if (source == target) {
            outputText.text = text
            return
        }

        translateButton.isEnabled = false
        outputText.text = "جارٍ فحص حزم اللغات..."

        checkModel(source) { sourceReady ->

            if (!sourceReady) {
                translateButton.isEnabled = true
                outputText.text =
                    "حمّل حزمة لغة ${languages[sourceIndex].first} أولاً من إدارة اللغات."
                return@checkModel
            }

            checkModel(target) { targetReady ->

                if (!targetReady) {
                    translateButton.isEnabled = true
                    outputText.text =
                        "حمّل حزمة لغة ${languages[targetIndex].first} أولاً من إدارة اللغات."
                    return@checkModel
                }

                runLocalTranslation(text, source, target)
            }
        }
    }

    private fun runLocalTranslation(
        text: String,
        source: String,
        target: String
    ) {

        outputText.text = "جارٍ الترجمة محلياً..."

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()

        val translator = Translation.getClient(options)

        translator.translate(text)
            .addOnSuccessListener { translated ->

                outputText.text = translated
                outputText.textDirection =
                    View.TEXT_DIRECTION_FIRST_STRONG

                translateButton.isEnabled = true
                translator.close()
            }
            .addOnFailureListener { error ->

                outputText.text =
                    "تعذرت الترجمة. تأكد من تنزيل حزم اللغتين."

                translateButton.isEnabled = true
                translator.close()
            }
    }
}
