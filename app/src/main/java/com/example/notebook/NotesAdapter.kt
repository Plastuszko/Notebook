import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.MainActivity
import com.example.notebook.databinding.ViewNotesLayoutBinding
import com.google.firebase.firestore.firestore

class NotesAdapter(private val notesList: List<Pair<String, String>>,private val onNoteClickListener: OnNoteClickListener,private val onNoteDeleteListener: OnNoteDeleteListener) : RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {
    var onDeleteListener: OnNoteDeleteListener? = null
    inner class MyViewHolder(private val binding: ViewNotesLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val noteTitle = binding.titleText
        val deleteImg= binding.deleteImg



        fun bind(noteTitle: String, noteText: String) {
            this.noteTitle.text = noteTitle
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
        holder.deleteImg.setOnClickListener{
            onDeleteListener?.onDeleteClicked(noteTitle)
        }
        holder.itemView.setOnClickListener {
            onNoteClickListener.onNoteClicked(noteTitle, noteText)
        }
        }


    override fun getItemCount(): Int {
        return notesList.size
    }
    interface OnNoteDeleteListener{
        fun onDeleteClicked(noteTitle: String)
    }
    fun setOnNoteDeleteListener(listener: MainActivity) {
        onDeleteListener = listener
    }

    interface OnNoteClickListener {
        fun onNoteClicked(noteTitle: String, noteText: String)
    }


}
