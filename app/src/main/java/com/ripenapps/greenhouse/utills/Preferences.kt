package com.ripenapps.greenhouse.utills

import android.content.Context
import android.content.SharedPreferences
import com.ripenapps.greenhouse.fragment.sellerfragemnt.MarkAsFeature

//Const
object PreEntity {
    const val NOTIFICATION_SWITCH ="1"
    const val USER_ID ="id"
    const val BEARER_TOKEN ="token"
    const val FCM_TOKEN ="fcmtoken"
    const val TAG = "tag"
    const val USERDATA = "userdata"
    const val IS_LOGIN = "is_user_login"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val ADDRESS = "address"
    const val FROM="cart_from"
    const val STORE_DETAILS="store_details"
}


//Utility Class
object Preferences {

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PreEntity.TAG, Context.MODE_PRIVATE)
    }

    fun setStringPreference(context: Context, key: String?, `val`: String?) {
        val settings: SharedPreferences = getSharedPreferences(context)
        val editor = settings.edit()
        editor.putString(key, `val`)
        editor.apply()
    }

    fun setBooleanPreference(context: Context, key: String?, `val`: Boolean) {
        val settings: SharedPreferences = getSharedPreferences(context)
        val editor = settings.edit()
        editor.putBoolean(key, `val`)
        editor.commit()
    }

    fun setIntegerPreference(context: Context, key: String?, `val`: Int) {
        val settings: SharedPreferences = getSharedPreferences(context)
        val editor = settings.edit()
        editor.putInt(key, `val`)
        editor.commit()
    }

    fun setLongPreference(context: Context, key: String?, `val`: Long) {
        val settings: SharedPreferences = getSharedPreferences(context)
        val editor = settings.edit()
        editor.putLong(key, `val`)
        editor.commit()
    }

    fun getLongPreference(context: Context, key: String?): Long? {
        val prefs: SharedPreferences = getSharedPreferences(context)
        return prefs.getLong(key, 0)
    }

    fun getStringPreference(context: Context, key: String?): String? {
        val prefs: SharedPreferences = getSharedPreferences(context)
        return prefs.getString(key, "")
    }

    fun getStringPreference1(context: Context, key: String?): String? {
        val prefs: SharedPreferences =
            getSharedPreferences(context)
        return prefs.getString(key, "0")
    }

    fun getBooleanPreference(context: Context, key: String?): Boolean {
        val prefs: SharedPreferences =
            getSharedPreferences(context)
        return prefs.getBoolean(key, false)
    }

    fun getIntegerPreference(context: Context, key: String?): Int {
        val prefs: SharedPreferences =
            getSharedPreferences(context)
        return prefs.getInt(key, -1)
    }

    fun getAllPreference(context: Context): String {
        val settings: SharedPreferences =
            getSharedPreferences(context)
        val editor = settings.all
        var text = ""
        try {
            for ((key, value1) in editor) {
                val value = value1!!
                // do stuff
                text += "\t$key = $value\t"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return text
    }

    fun removePreference(context: Context, key: String?) {
        val settings: SharedPreferences =
            getSharedPreferences(context)
        val editor = settings.edit()
        editor.remove(key)
        editor.commit()
    }

    fun removeAllPreference(context: Context) {
        val settings: SharedPreferences = getSharedPreferences(context)
        val editor = settings.edit()
        editor.clear()
        editor.commit()
    }
}



