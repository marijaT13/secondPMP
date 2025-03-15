package com.example.secondpmp

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var searchInput: SearchView
    private lateinit var tagInput: EditText
    private lateinit var saveButton: Button
    private lateinit var buttonClear: Button
    private lateinit var resultTextView: TextView // ✅ Додадено за приказ на резултатот

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Наоѓање на UI елементи
        searchInput = findViewById(R.id.searchInput)
        tagInput = findViewById(R.id.tagInput)
        saveButton = findViewById(R.id.saveButton)
        buttonClear = findViewById(R.id.buttonClear)
        resultTextView = findViewById(R.id.resultTextView) // ✅ Додадено

        copyAssetFileToInternalStorage("dictionary.txt")

        // Обработка на пребарување
        searchInput.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.trim()?.let { searchAndDisplayTranslation(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Копче за зачувување
        saveButton.setOnClickListener {
            val newWords = tagInput.text.toString().trim()
            if (newWords.isNotEmpty() && newWords.contains(",")) {
                val parts = newWords.split(",").map { it.trim() }
                if (parts.size == 2) {
                    addNewWord(parts[0], parts[1])
                    tagInput.setText("")
                    Toast.makeText(this, "Додаден е нов превод!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Копче за чистење
        buttonClear.setOnClickListener {
            resultTextView.text = ""
            Toast.makeText(this, "Листата е исчистена!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyAssetFileToInternalStorage(filename: String) {
        val file = File(filesDir, filename)
        if (!file.exists()) {
            try {
                assets.open(filename).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (inputStream.read(buffer).also { length = it } > 0) {
                            outputStream.write(buffer, 0, length)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun searchAndDisplayTranslation(query: String) {
        val file = File(filesDir, "dictionary.txt")
        var translationFound = false
        val resultBuilder = StringBuilder()

        try {
            BufferedReader(FileReader(file)).use { reader ->
                reader.forEachLine { line ->
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size == 2) {
                        val (word, translation) = parts
                        when {
                            word.equals(query, ignoreCase = true) -> {
                                resultBuilder.append("$word -> $translation\n")
                                translationFound = true
                            }
                            translation.equals(query, ignoreCase = true) -> {
                                resultBuilder.append("$query -> $word\n")
                                translationFound = true
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (!translationFound) {
            resultBuilder.append("Нема таков превод")
        }

        resultTextView.text = resultBuilder.toString()

        searchInput.clearFocus()
        searchInput.setQuery("", false)
        searchInput.isIconified = true
    }

    private fun addNewWord(word1: String, word2: String) {
        val file = File(filesDir, "dictionary.txt")

        try {
            BufferedWriter(FileWriter(file, true)).use { writer ->
                writer.write("$word1,$word2")
                writer.newLine()
                Log.d("Dictionary", "Зборот е поврзан со преводот: $word1,$word2")
            }
        } catch (e: IOException) {
            Log.e("Dictionary", "Зборот не може да биде додаден: ${e.message}")
        }
    }
}

