/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.about.faq

import androidx.annotation.Keep
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem

@Keep
enum class FAQItemId { DUMMY }

class FAQDetailSections {
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