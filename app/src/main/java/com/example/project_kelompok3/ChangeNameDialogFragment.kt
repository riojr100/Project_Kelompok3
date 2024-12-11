import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.project_kelompok3.R

class ChangeNameDialogFragment(private val onNameChanged: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_change_name, null)
        val nameInput = view.findViewById<EditText>(R.id.nameInput)

        builder.setView(view)
            .setTitle("Change Account Name")
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString()
                if (newName.isNotEmpty()) {
                    onNameChanged(newName)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }
}
