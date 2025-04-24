package com.amarullz.androidtv.animetvjmto

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.jsoup.Jsoup

class SourceDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sources.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE sources (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                baseUrl TEXT,
                animeListSelector TEXT,
                episodeSelector TEXT,
                streamUrlSelector TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun insertSource(
        name: String, baseUrl: String,
        animeListSelector: String, episodeSelector: String, streamUrlSelector: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("baseUrl", baseUrl)
            put("animeListSelector", animeListSelector)
            put("episodeSelector", episodeSelector)
            put("streamUrlSelector", streamUrlSelector)
        }
        db.insert("sources", null, values)
    }
}

class AddSourceActivity : AppCompatActivity() {

    private lateinit var dbHelper: SourceDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_source)

        dbHelper = SourceDatabaseHelper(this)

        val nameField = findViewById<EditText>(R.id.sourceName)
        val urlField = findViewById<EditText>(R.id.baseUrl)
        val listField = findViewById<EditText>(R.id.animeListSelector)
        val episodeField = findViewById<EditText>(R.id.episodeSelector)
        val streamField = findViewById<EditText>(R.id.streamUrlSelector)

        findViewById<Button>(R.id.saveSource).setOnClickListener {
            dbHelper.insertSource(
                nameField.text.toString(),
                urlField.text.toString(),
                listField.text.toString(),
                episodeField.text.toString(),
                streamField.text.toString()
            )
            Toast.makeText(this, "Source Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.testSource).setOnClickListener {
            Thread {
                try {
                    val results = parseAnimeFLV(urlField.text.toString())
                    runOnUiThread {
                        Toast.makeText(this, "Found ${results.size} entries", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }

    fun parseAnimeFLV(baseUrl: String): List<String> {
        val result = mutableListOf<String>()
        val doc = Jsoup.connect(baseUrl).get()
        val elements = doc.select("ul.ListAnimes a[href]")
        for (el in elements) {
            result.add(el.text())
        }
        return result
    }
}
