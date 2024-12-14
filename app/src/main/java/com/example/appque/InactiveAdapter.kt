import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appque.R

class InactiveAdapter(
    private val onRestoreClick: (User) -> Unit
) : ListAdapter<User, InactiveAdapter.InactiveViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InactiveViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inactive, parent, false)
        return InactiveViewHolder(view)
    }

    override fun onBindViewHolder(holder: InactiveViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, onRestoreClick)
    }

    class InactiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val courseTextView: TextView = itemView.findViewById(R.id.courseTextView)
        private val yearTextView: TextView = itemView.findViewById(R.id.yearTextView)
        private val restoreButton: Button = itemView.findViewById(R.id.restoreButton)

        @SuppressLint("SetTextI18n")
        fun bind(user: User, onRestoreClick: (User) -> Unit) {
            nameTextView.text = "Name: ${user.name}"
            emailTextView.text = "Email: ${user.email}"
            courseTextView.text = "Course: ${user.course}"
            yearTextView.text = "Year Leve: ${user.year}"

            restoreButton.setOnClickListener {
                onRestoreClick(user)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}
