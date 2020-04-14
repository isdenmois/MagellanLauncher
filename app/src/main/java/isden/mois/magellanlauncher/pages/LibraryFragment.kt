package isden.mois.magellanlauncher.pages

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import isden.mois.magellanlauncher.R
import isden.mois.magellanlauncher.models.BookMetadata
import isden.mois.magellanlauncher.utils.*
import isden.mois.magellanlauncher.utils.ListAdapter
import kotlinx.android.synthetic.main.page_library.*

class LibraryFragment : Fragment() {
    internal val adapter = LibraryAdapter(context)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.page_library, container, false)

        val library = v.findViewById(R.id.libraryList) as ListView
        v.findViewById(R.id.libraryToggle).setOnClickListener {
            val status = if (libraryToggle.isChecked) "Status = 1" else "(Status = 0 OR Status IS NULL)"
            val sort = if (libraryToggle.isChecked) "LastModified" else "LastAccess"

            BooksListLoaderTask(status, sort).execute()
        }
        v.findViewById(R.id.libraryUp).setOnClickListener {
            val first = library.firstVisiblePosition
            val last = library.lastVisiblePosition
            val height = last - first
            val target = if (first < height) 0 else first - height

            library.setSelection(target)
        }
        v.findViewById(R.id.libraryDown).setOnClickListener {
            library.setSelection(library.lastVisiblePosition)
        }

        adapter.setContext(context)
        library.adapter = adapter

        BooksListLoaderTask("(Status = 0 OR Status IS NULL)", "LastAccess").execute()

        return v
    }

    inner class BooksListLoaderTask(val status: String, val sort: String) : ListTask<BookMetadata>(activity, adapter) {
        override fun action() {
            list = getBooks(activity, status, sort)
        }
    }
}

class LibraryAdapter(c: Context?) : ListAdapter<BookMetadata, LibraryViewHolder>(c, R.layout.library_item) {
    override fun fillHolder(metadata: BookMetadata, holder: LibraryViewHolder) {
        val bmp = metadata.getThumbnail()
        if (bmp != null) {
            holder.image.setImageBitmap(bmp)
        }

        if (metadata.title != "") {
            holder.title.text = metadata.title
            holder.author.text = metadata.author
            holder.author.visibility = View.VISIBLE
        } else {
            holder.title.text = metadata.filename
            holder.author.visibility = View.INVISIBLE
        }

        if (metadata.time.currentTime > 0) {
            holder.progress.text = metadata.progress
            holder.time.text = metadata.currentSpentTime()
            holder.progressLine.visibility = View.VISIBLE
        } else {
            holder.progressLine.visibility = View.INVISIBLE
        }
    }

    override fun getHolder(v: View): LibraryViewHolder = LibraryViewHolder(v)
}

class LibraryViewHolder(v: View) : ViewHolder() {
    var title = v.findViewById(R.id.libraryTitle) as TextView
    var author = v.findViewById(R.id.libraryAuthor) as TextView
    var progressLine = v.findViewById(R.id.libraryProgressLine) as LinearLayout
    var progress = v.findViewById(R.id.libraryProgress) as TextView
    var time = v.findViewById(R.id.libraryTime) as TextView
    var image = v.findViewById(R.id.libraryImage) as ImageView
}
