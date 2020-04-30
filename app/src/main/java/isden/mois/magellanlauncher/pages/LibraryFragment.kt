package isden.mois.magellanlauncher.pages

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import isden.mois.magellanlauncher.BookActivity
import isden.mois.magellanlauncher.R
import isden.mois.magellanlauncher.models.BookMetadata
import isden.mois.magellanlauncher.models.KeyDownFragment
import isden.mois.magellanlauncher.utils.*
import isden.mois.magellanlauncher.utils.ListAdapter

class LibraryFragment : KeyDownFragment() {
    internal val adapter = LibraryAdapter(context)
    private var library: ListView? = null;
    var status = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.page_library, container, false)

        library = v.findViewById(R.id.libraryList) as ListView
        v.findViewById(R.id.newBooks).setOnClickListener {
            val status = "(Status = 0 OR Status IS NULL)"
            val sort = "LastAccess"

            BooksListLoaderTask(status, sort).execute()
            this.status = 0
            v.findViewById(R.id.newBooks).isEnabled = false
            v.findViewById(R.id.readBooks).isEnabled = true
        }
        v.findViewById(R.id.readBooks).setOnClickListener {
            val status = "Status = 1"
            val sort = "LastModified"

            BooksListLoaderTask(status, sort).execute()
            this.status = 1
            v.findViewById(R.id.newBooks).isEnabled = true
            v.findViewById(R.id.readBooks).isEnabled = false
        }

        adapter.setContext(context)
        library!!.adapter = adapter
        library!!.setOnItemClickListener { adapterView, view, i, l ->
            val item = adapter.getItem(i)
            val intent = Intent(context, BookActivity::class.java)

            intent.putExtra("MD5", item.md5)

            startActivity(intent)
        }

        library!!.setOnItemLongClickListener { adapterView, view, i, l ->
            val item = adapter.getItem(i)
            AlertDialog.Builder(context)
                    .setTitle("Вы действительно изменить статус?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, ii ->
                        adapter.removeItem(item)
                        changeStatus(context, item, 1 - status)
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, ii -> })
                    .show()
            true
        }

        BooksListLoaderTask("(Status = 0 OR Status IS NULL)", "LastAccess").execute()

        return v
    }

    override fun onKeyDown(keyCode: Int) {
        if (library == null) return;

        if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            library!!.setSelection(library!!.lastVisiblePosition + 1);
        } else {
            val first = library!!.firstVisiblePosition
            val last = library!!.lastVisiblePosition
            val height = last - first
            val target = if (first < height) 0 else first - height

            library!!.setSelection(target)
        }

        library!!.invalidate();
    }

    inner class BooksListLoaderTask(val status: String, val sort: String) : ListTask<BookMetadata>(activity, adapter) {
        override fun action() {
            list = getBooks(activity, status, sort)
        }
    }
}

class LibraryAdapter(c: Context?) : ListAdapter<BookMetadata, LibraryViewHolder>(c, R.layout.library_item) {
    override fun fillHolder(metadata: BookMetadata, holder: LibraryViewHolder) {
        if (metadata.title != "") {
            holder.title.text = metadata.title
            holder.author.text = metadata.author
            holder.author.visibility = View.VISIBLE
        } else {
            holder.title.text = metadata.filename
            holder.author.visibility = View.GONE
        }

        if (metadata.time.currentTime > 0) {
            holder.progress.text = metadata.progress
            holder.time.text = metadata.currentSpentTime()
            holder.progressLine.visibility = View.VISIBLE
        } else {
            holder.progressLine.visibility = View.GONE
        }
    }

    override fun getHolder(v: View): LibraryViewHolder = LibraryViewHolder(v)

    fun removeItem(item: BookMetadata) {
        list.remove(item)
//        list = list.
        notifyDataSetChanged()
    }
}

class LibraryViewHolder(v: View) : ViewHolder() {
    var title = v.findViewById(R.id.libraryTitle) as TextView
    var author = v.findViewById(R.id.libraryAuthor) as TextView
    var progressLine = v.findViewById(R.id.libraryProgressLine) as LinearLayout
    var progress = v.findViewById(R.id.libraryProgress) as TextView
    var time = v.findViewById(R.id.libraryTime) as TextView
}
