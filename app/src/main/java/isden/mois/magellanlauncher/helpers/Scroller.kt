package isden.mois.magellanlauncher.helpers

import android.widget.AbsListView

public class Scroller(): AbsListView.OnScrollListener {
    private var firstVisibleItem = 0
    private var itemsPerScreen = 0

    val prevPageItem: Int get() = firstVisibleItem + itemsPerScreen
    val nextPageItem: Int get() = if (firstVisibleItem > itemsPerScreen) firstVisibleItem - itemsPerScreen else 0

    override fun onScroll(p0: AbsListView?, firstItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        firstVisibleItem = firstItem
        itemsPerScreen = visibleItemCount;
    }

    override fun onScrollStateChanged(p0: AbsListView?, scrollState: Int) {
        if (scrollState == 0) {
            p0?.setSelection(firstVisibleItem)
        }
    }
}