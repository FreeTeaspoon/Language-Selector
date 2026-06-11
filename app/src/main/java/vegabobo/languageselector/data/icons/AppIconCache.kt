package vegabobo.languageselector.data.icons

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import me.zhanghai.android.appiconloader.AppIconLoader
import vegabobo.languageselector.BuildConfig

fun ApplicationInfo.withCurrentUserUid(): ApplicationInfo {
    val currentUserId = Process.myUid() / 100000
    val appId = uid % 100000
    val targetUid = currentUserId * 100000 + appId
    if (uid == targetUid) return this
    return ApplicationInfo(this).apply { uid = targetUid }
}

object AppIconCache {
    private const val PERF_TAG = "LanguageSelectorPerf"
    private val maxMemoryKb = Runtime.getRuntime().maxMemory() / 1024
    private val cacheSizeKb = (maxMemoryKb / 8).toInt()
    private val loadSemaphore = Semaphore(4)

    private val lruCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount / 1024
        }
    }

    fun getFromCache(applicationInfo: ApplicationInfo, sizePx: Int): Bitmap? {
        val key = buildKey(applicationInfo, sizePx)
        synchronized(lruCache) { return lruCache.get(key) }
    }

    suspend fun loadIcon(
        context: Context,
        applicationInfo: ApplicationInfo,
        sizePx: Int
    ): Bitmap? {
        val key = buildKey(applicationInfo, sizePx)
        synchronized(lruCache) {
            lruCache.get(key)?.let { return it }
        }

        return loadSemaphore.withPermit {
            synchronized(lruCache) {
                lruCache.get(key)?.let { return@withPermit it }
            }

            withContext(Dispatchers.IO) {
                val start = SystemClock.elapsedRealtime()
                runCatching {
                    val loader = AppIconLoader(sizePx, false, context)
                    val bitmap = loader.loadIcon(applicationInfo.withCurrentUserUid())
                    val drawableReadyBitmap = try {
                        bitmap.copy(Bitmap.Config.HARDWARE, false)?.also {
                            bitmap.recycle()
                        } ?: bitmap.also { it.prepareToDraw() }
                    } catch (_: Exception) {
                        bitmap.also { it.prepareToDraw() }
                    }
                    synchronized(lruCache) {
                        lruCache.put(key, drawableReadyBitmap)
                    }
                    drawableReadyBitmap
                }.onSuccess {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            PERF_TAG,
                            "icon load: ${applicationInfo.packageName} ${sizePx}px in ${SystemClock.elapsedRealtime() - start}ms"
                        )
                    }
                }.onFailure {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            PERF_TAG,
                            "icon load failed: ${applicationInfo.packageName} ${sizePx}px in ${SystemClock.elapsedRealtime() - start}ms",
                            it
                        )
                    }
                }.getOrNull()
            }
        }
    }

    private fun buildKey(applicationInfo: ApplicationInfo, sizePx: Int): String =
        "${applicationInfo.packageName}:${applicationInfo.uid}:${applicationInfo.sourceDir}:$sizePx"
}
