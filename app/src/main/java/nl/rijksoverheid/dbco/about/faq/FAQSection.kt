package nl.rijksoverheid.dbco.about.faq

import androidx.annotation.Keep
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.items.HeaderItem
import nl.rijksoverheid.dbco.items.ParagraphItem

@Keep
enum class FAQItemId { DUMMY }

class FAQDetailSections{
    fun getSection(faqItemId: FAQItemId) = when (faqItemId) {
        FAQItemId.DUMMY -> {
            Section(
                listOf(
                    HeaderItem(R.string.placeholder),
                    ParagraphItem(R.string.placeholder_long)
                )
            )
        }
    }
}