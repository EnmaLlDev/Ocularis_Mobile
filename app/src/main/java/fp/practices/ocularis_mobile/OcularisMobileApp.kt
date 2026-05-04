package fp.practices.ocularis_mobile

import android.app.Application
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class OcularisMobileApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
    }
}

