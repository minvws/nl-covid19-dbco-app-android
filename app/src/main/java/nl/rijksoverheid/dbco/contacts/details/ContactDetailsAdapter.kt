package nl.rijksoverheid.dbco.contacts.details

import com.jay.widget.StickyHeaders
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader

class ContactDetailsAdapter : GroupAdapter<GroupieViewHolder>(), StickyHeaders {

    override fun isStickyHeader(position: Int): Boolean {
        val item = getItem(position)
        return item is QuestionnaireSectionHeader
    }
}