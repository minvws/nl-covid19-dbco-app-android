package nl.rijksoverheid.dbco.items.ui

import androidx.annotation.DimenRes
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSpaceBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class VerticalSpaceItem(
    @DimenRes private val height: Int
) : BaseBindableItem<ItemSpaceBinding>() {

    override fun getLayout() = R.layout.item_space

    override fun bind(viewBinding: ItemSpaceBinding, position: Int) {
        viewBinding.space.layoutParams.height = viewBinding.space.resources.getDimensionPixelSize(height)
    }

    override fun isSameAs(other: Item<*>): Boolean = other is VerticalSpaceItem && other.height == height

    override fun hasSameContentAs(other: Item<*>) = other is VerticalSpaceItem && other.height == height
}