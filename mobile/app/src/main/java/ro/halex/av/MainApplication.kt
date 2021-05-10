package ro.halex.av

import android.app.Application
import io.ktor.client.*
import ro.halex.av.backend.createHttpClient

class MainApplication : Application()
{
    lateinit var httpClient: HttpClient

    override fun onCreate()
    {
        super.onCreate()
        httpClient = createHttpClient()
    }
}