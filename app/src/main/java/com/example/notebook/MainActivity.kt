package com.example.notebook

import NotesAdapter
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.R
import com.example.notebook.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NotesAdapter.OnNoteDeleteListener {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var notesAdapter: NotesAdapter
    private var user_email: String = ""
    private var Map_of_titles: MutableMap<String, String> = mutableMapOf()
    private var Map_of_notes: MutableMap<String, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        if (intent.hasExtra("USER")) {
            user_email = intent.getStringExtra("USER")!!
            Toast.makeText(this, "Hi, $user_email", Toast.LENGTH_SHORT).show()
        }

        val updateNotesDialog = Dialog(this).apply {
            setContentView(R.layout.update_notes)
        }

        mainBinding.addNewNoteBtn.setOnClickListener {
            updateNotesDialog.show()
        }

        val saveNotesBtn = updateNotesDialog.findViewById<Button>(R.id.save_notes_btn)

        saveNotesBtn.setOnClickListener {
            val ednoteTitle = updateNotesDialog.findViewById<TextView>(R.id.ednoteTitle)
            val ednote = updateNotesDialog.findViewById<TextView>(R.id.ednote)

            val noteTitle = ednoteTitle.text.toString()
            val noteText = ednote.text.toString()

            // Sprawdzanie, czy notatka o takim tytule już istnieje
            if (Map_of_titles.containsKey(noteTitle)) {
                // Notatka o takim tytule już istnieje, wyświetl odpowiednie powiadomienie
                Toast.makeText(this, "Notatka o takim tytule już istnieje, wybierz inny tytuł", Toast.LENGTH_SHORT).show()
            } else {
                // Notatka o takim tytule nie istnieje, dodaj nową notatkę do bazy danych
                val noteData = hashMapOf(
                    "who" to user_email,
                    "noteTitle" to noteTitle,
                    "note" to noteText
                )

                db.collection("Notes")
                    .add(noteData)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        Toast.makeText(this, "Notatka została zapisana", Toast.LENGTH_SHORT).show()
                        // Aktualizacja listy notatek po dodaniu nowej notatki
                        fetchData(user_email)
                        // Zamknięcie dialogu po zapisaniu notatki
                        updateNotesDialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                        Toast.makeText(this, "Błąd podczas zapisywania notatki", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        val recyclerView: RecyclerView = findViewById(R.id.notes_list)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Pobranie danych przy pierwszym uruchomieniu
        fetchData(user_email)
    }

    override fun onDeleteClicked(noteTitle: String) {
        db.collection("Notes")
            .whereEqualTo("noteTitle", noteTitle)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Usunięto notatkę pomyślnie
                            // Aktualizacja listy notatek po usunięciu
                            fetchData(user_email)
                        }
                        .addOnFailureListener { e ->
                            // Błąd podczas usuwania notatki
                            Log.e(TAG, "Error deleting note", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Błąd podczas pobierania notatek
                Log.e(TAG, "Error getting notes", exception)
            }
    }

    private fun fetchData(userEmail: String) {
        Map_of_titles.clear()
        Map_of_notes.clear()
        db.collection("Notes")
            .whereEqualTo("who", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val noteTitle = document.getString("noteTitle")
                    val noteText = document.getString("note")
                    if (noteTitle != null && noteText != null) {
                        Map_of_titles[noteTitle] = noteTitle
                        Map_of_notes[noteTitle] = noteText
                    }
                }
                // Przekazujemy listę notatek do adaptera
                val notesList = Map_of_titles.toList()
                notesAdapter = NotesAdapter(notesList)
                notesAdapter.setOnNoteDeleteListener(this)
                mainBinding.notesList.adapter = notesAdapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
