package isden.mois.magellanlauncher.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import isden.mois.magellanlauncher.R
import isden.mois.magellanlauncher.models.BookMetadata
import isden.mois.magellanlauncher.utils.getBooks
import kotlinx.android.synthetic.main.activity_main.*
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

        adapter.c = context
        library.adapter = adapter

        BooksListLoaderTask("(Status = 0 OR Status IS NULL)", "LastAccess").execute()

        return v
    }

    inner class BooksListLoaderTask(val status: String, val sort: String) : AsyncTask<Void, Void, Void>() {
        private var list: List<BookMetadata> = ArrayList()

        override fun onPreExecute() {
            activity.findViewById(R.id.activityIndicator).visibility = View.VISIBLE
            activity.findViewById(R.id.container).visibility = View.GONE
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            list = getBooks(context, status, sort)

            return null
        }

        override fun onPostExecute(result: Void?) {
            adapter.setList(list)
            activity.findViewById(R.id.activityIndicator).visibility = View.GONE
            activity.findViewById(R.id.container).visibility = View.VISIBLE
        }
    }
}

class LibraryAdapter(var c: Context?) : BaseAdapter() {
    private var list: List<BookMetadata> = ArrayList()

    fun setList(books: List<BookMetadata>) {
        list = books
        notifyDataSetChanged()
    }

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var v: View? = convertView
        val inflater = LayoutInflater.from(c)

        if (v == null) {
            v = inflater.inflate(R.layout.library_item, null)
            if (v == null) return null

            v.tag = ViewHolder(
                    v.findViewById(R.id.libraryTitle) as TextView,
                    v.findViewById(R.id.libraryAuthor) as TextView,
                    v.findViewById(R.id.libraryProgressLine) as LinearLayout,
                    v.findViewById(R.id.libraryProgress) as TextView,
                    v.findViewById(R.id.libraryTime) as TextView,
                    v.findViewById(R.id.libraryImage) as ImageView
            )
        }

        val holder: ViewHolder = v.tag as ViewHolder

        val metadata = getItem(i)

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

        return v
    }

    override fun getItem(i: Int): BookMetadata = list[i]
    override fun getItemId(i: Int): Long = i.toLong()
    override fun getCount(): Int = list.size

    inner class ViewHolder(
            var title: TextView,
            var author: TextView,
            var progressLine: LinearLayout,
            var progress: TextView,
            var time: TextView,
            var image: ImageView
    ) {
    }
}
