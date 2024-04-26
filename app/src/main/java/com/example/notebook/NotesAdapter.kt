import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.databinding.ViewNotesLayoutBinding

class NotesAdapter(private val notesList: List<Pair<String, String>>) : RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

    inner class MyViewHolder(private val binding: ViewNotesLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val noteTitle = binding.titleText
        val notesShortcut = binding.notesShortcut

        fun bind(noteTitle: String, noteText: String) {
            this.noteTitle.text = noteTitle
            this.notesShortcut.text = noteText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewNotesLayoutBinding = ViewNotesLayoutBinding.inflate(inflater, parent, false)
        return MyViewHolder(viewNotesLayoutBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (noteTitle, noteText) = notesList[position]
        holder.bind(noteTitle, noteText)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

}
