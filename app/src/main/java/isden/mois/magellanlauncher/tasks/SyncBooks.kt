package isden.mois.magellanlauncher.tasks

import android.content.Context
import android.os.AsyncTask
import isden.mois.magellanlauncher.tasks.sync.createUploadBookMetadata
import isden.mois.magellanlauncher.tasks.sync.createUploadHistory
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by isden on 24.07.17.
 */

interface SyncProgress {
    fun onStart(): Unit
    fun onProgress(progress: Int): Unit
    fun onNewStep(total: Int, title: String): Unit
    fun onEnd(): Unit
}

class Progress (val completed: Int, val total: Int, val title: String?)

interface SyncTask {
    fun execute(ctx: Context): Unit
}

class SyncBooks (
    private val ctx: Context,
    private val progress: SyncProgress
): AsyncTask<Unit, Progress, Unit>() {

    private val taskList: Queue<SyncTask> = LinkedBlockingQueue()

    override fun onPreExecute() {
        progress.onStart()
    }

    override fun doInBackground(vararg p0: Unit?): Unit {
        taskList.addAll(createUploadBookMetadata(ctx))

        if (!isCancelled) {
            publishProgress(Progress(0, taskList.size, "Загрузка книг на сервер"))
            executeAllTasks()
        }

        if (!isCancelled) {
            taskList.addAll(createUploadHistory(ctx))
        }

        if (!isCancelled) {
            publishProgress(Progress(0, taskList.size, "Отправка истории чтения"))
            executeAllTasks()
        }
    }

    private fun executeAllTasks() {
        if (isCancelled) {
            return
        }

        val total = taskList.size

        while (!taskList.isEmpty()) {
            if (isCancelled) {
                return
            }

            val task = taskList.remove()
            try {
                task.execute(ctx)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            publishProgress(Progress(total - taskList.size, total, null))
        }
    }

    override fun onProgressUpdate(vararg values: Progress?) {
        val p = values.first() ?: return

        if (p.title != null) {
            progress.onNewStep(p.total, p.title)
        } else {
            progress.onProgress(p.completed)
        }
    }

    override fun onPostExecute(result: Unit?) {
        progress.onEnd()
    }
}
