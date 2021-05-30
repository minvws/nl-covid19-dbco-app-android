package nl.rijksoverheid.dbco.ui

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.BottomSheetPickerDialogBinding
import nl.rijksoverheid.dbco.databinding.BottomSheetPickerDialogItemBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

/**
 * Bottom sheet implementation used for picking between provided strings
 * also adds an "extra" option which returns an empty string
 */
class BottomSheetDialogPicker(
    context: Context,
    items: Set<String>,
    clickListener: (String) -> Unit
) {

    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)

    init {
        val binding = BottomSheetPickerDialogBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        val itemsAdapter = GroupAdapter<GroupieViewHolder>()
        binding.recyclerView.adapter = itemsAdapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerView.context,
                VERTICAL
            )
        )
        val section = Section().apply {
            addAll(items.map {
                BottomSheetDialogItem(itemText = it) { result ->
                    hide()
                    clickListener(result)
                }
            })
            add(BottomSheetDialogItem(itemText = context.getString(R.string.other)) {
                hide()
                clickListener("")
            })
            add(BottomSheetDialogItem(itemText = context.getString(R.string.cancel), bold = true) {
                hide()
            })
        }
        itemsAdapter.add(section)
    }

    fun show() = bottomSheetDialog.show()

    private fun hide() = bottomSheetDialog.hide()

    private class BottomSheetDialogItem(
        private val bold: Boolean = false,
        private val itemText: String,
        private val clickListener: (String) -> Unit
    ) : BaseBindableItem<BottomSheetPickerDialogItemBinding>() {

        override fun bind(viewBinding: BottomSheetPickerDialogItemBinding, position: Int) {
            with(viewBinding.itemText) {
                typeface = if (bold) {
                    Typeface.DEFAULT_BOLD;
                } else {
                    Typeface.DEFAULT;
                }
                text = itemText
                setOnClickListener { clickListener(itemText) }
            }
        }

        override fun getLayout(): Int = R.layout.bottom_sheet_picker_dialog_item
    }
}