package com.kira.healthcare.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPrefrenceUtil(context: Context) {

    val TAG = SharedPrefrenceUtil::class.java.getSimpleName()

    // private lateinit var pref:SharedPreference
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("Kira-SP", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    val perSharedPreferences: SharedPreferences =
        context.getSharedPreferences("Kira-SP111", Context.MODE_PRIVATE)
    val perEditor: SharedPreferences.Editor = perSharedPreferences.edit()

    companion object {
        private var instance: SharedPrefrenceUtil? = null

        fun getInstance(ctx: Context): SharedPrefrenceUtil {
            if (instance == null) {
                instance = SharedPrefrenceUtil(ctx)
            }
            return instance!!
        }
    }

    var notificationStatus: Int
        get() = sharedPreferences["notificationStatus", 0]!!
        set(value) = sharedPreferences.set("notificationStatus", value)

    var payment: Int
        get() = sharedPreferences["payment", 0]!!
        set(value) = sharedPreferences.set("payment", value)
    var chooseType: Int
        get() = sharedPreferences["chooseType", 0]!!
        set(value) = sharedPreferences.set("chooseType", value)
    var inAppNotification: Int
        get() = sharedPreferences["inAppNotification", 0]!!
        set(value) = sharedPreferences.set("inAppNotification", value)

    var serviceType: String
        get() = sharedPreferences["serviceType", ""]!!
        set(value) = sharedPreferences.set("serviceType", value)

        var totalAmount: String
        get() = sharedPreferences["totalAmount", ""]!!
        set(value) = sharedPreferences.set("totalAmount", value)


    var noOfItem: String
        get() = sharedPreferences["noOfItem", ""]!!
        set(value) = sharedPreferences.set("noOfItem", value)



    var cartId: String
        get() = sharedPreferences["cartId", String()]!!
        set(value) = sharedPreferences.set("cartId", value)


    var firebaseToken: String
        get() = sharedPreferences["firebaseToken", ""]!!
        set(value) = sharedPreferences.set("firebaseToken", value)
    var first_name: String
        get() = sharedPreferences["first_name", ""]!!
        set(value) = sharedPreferences.set("first_name", value)
    var last_name: String
        get() = sharedPreferences["last_name", ""]!!
        set(value) = sharedPreferences.set("last_name", value)

    var profilePic: String
        get() = sharedPreferences["profilePic", ""]!!
        set(value) = sharedPreferences.set("profilePic", value)

    var profileUpdated: String
        get() = sharedPreferences["profileUpdated", ""]!!
        set(value) = sharedPreferences.set("profileUpdated", value)

    var age: String
        get() = sharedPreferences["age", ""]!!
        set(value) = sharedPreferences.set("age", value)

    var user_id: String
        get() = sharedPreferences["user_id", ""]!!
        set(value) = sharedPreferences.set("user_id", value)

    var searchedStore: String
        get() = sharedPreferences["searchedStore", ""]!!
        set(value) = sharedPreferences.set("searchedStore", value)

    var email: String
        get() = sharedPreferences["email", ""]!!
        set(value) = sharedPreferences.set("email", value)
    var address: String
        get() = sharedPreferences["address", ""]!!
        set(value) = sharedPreferences.set("address", value)

    var mobile: String
        get() = sharedPreferences["mobile", ""]!!
        set(value) = sharedPreferences.set("mobile", value)

    var country_code: String
        get() = sharedPreferences["country_code", ""]!!
        set(value) = sharedPreferences.set("country_code", value)

    var country: String
        get() = sharedPreferences["country", ""]!!
        set(value) = sharedPreferences.set("country", value)
    var local: String
        get() = sharedPreferences["language", ""]!!
        set(value) = sharedPreferences.set("language", value)

    var location: String
        get() = sharedPreferences["location", ""]!!
        set(value) = sharedPreferences.set("location", value)

    var isLocal: Boolean
        get() = perSharedPreferences["isLocal", false]!!
        set(value) = perSharedPreferences.set("isLocal", value)
    var isWT: Boolean
        get() = perSharedPreferences["isWT", false]!!
        set(value) = perSharedPreferences.set("isWT", value)

    var image: String
        get() = sharedPreferences["image", ""]!!
        set(value) = sharedPreferences.set("image", value)

    var isLogin: Boolean
        get() = sharedPreferences["isLogin", false]!!
        set(value) = sharedPreferences.set("isLogin", value)
    var isGuestLogin: Boolean
        get() = sharedPreferences["isGuestLogin", false]!!
        set(value) = sharedPreferences.set("isGuestLogin", value)
    var isProfileCreated: Boolean
        get() = sharedPreferences["isProfileCreated", false]!!
        set(value) = sharedPreferences.set("isProfileCreated", value)

    var isRegistered: Boolean
        get() = sharedPreferences["isRegistered", false]!!
        set(value) = sharedPreferences.set("isRegistered", value)


        var isSocialLogin: Boolean
        get() = sharedPreferences["isSocialLogin", false]!!
        set(value) = sharedPreferences.set("isSocialLogin", value)

   var socialId: String
        get() = sharedPreferences["socialId"]?:""
        set(value) = sharedPreferences.set("socialId", value)



    var isOnline: Boolean
        get() = sharedPreferences["isOnline", true]!!
        set(value) = sharedPreferences.set("isOnline", value)


    var access_token: String
        get() = sharedPreferences["access_token", ""]!!
        set(value) = sharedPreferences.set("access_token", value)


    var latitude: String
        get() = sharedPreferences["lat", ""]!!
        set(value) = sharedPreferences.set("lat", value)
    var device_token: String
        get() = sharedPreferences["device_token", ""]!!
        set(value) = sharedPreferences.set("device_token", value)


    var longitude: String
        get() = sharedPreferences["long", ""]!!
        set(value) = sharedPreferences.set("long", value)

    var s_latitude: String
        get() = sharedPreferences["s_latitude", ""]!!
        set(value) = sharedPreferences.set("s_latitude", value)

    var filter_loc: String
        get() = sharedPreferences["filter_loc", ""]!!
        set(value) = sharedPreferences.set("filter_loc", value)
    var comming_status: String
        get() = sharedPreferences["filter_loc", ""]!!
        set(value) = sharedPreferences.set("filter_loc", value)

    var s_longitude: String
        get() = sharedPreferences["s_longitude", ""]!!
        set(value) = sharedPreferences.set("s_longitude", value)


    var isbusy: Int
        get() = sharedPreferences["isbusy", 0]!!
        set(value) = sharedPreferences.set("isbusy", value)

    var dislike: Boolean
        get() = sharedPreferences["isleftClick", false]!!
        set(value) = sharedPreferences.set("isleftClick", value)

    var like: Boolean
        get() = sharedPreferences["isRightClick", false]!!
        set(value) = sharedPreferences.set("isRightClick", value)

    var permissionGranted: Boolean
        get() = sharedPreferences["permissionGranted", false]!!
        set(value) = sharedPreferences.set("permissionGranted", value)
    var permissionDenied: Boolean
        get() = sharedPreferences["permissionDenied", false]!!
        set(value) = sharedPreferences.set("permissionDenied", value)

    //Filter data
    var isFilter: Boolean
        get() = sharedPreferences["isFilter", false]!!
        set(value) = sharedPreferences.set("isFilter", value)

    var gender: String
        get() = sharedPreferences["gender", ""]!!
        set(value) = sharedPreferences.set("gender", value)

    var mygender: String
        get() = sharedPreferences["mygender", ""]!!
        set(value) = sharedPreferences.set("mygender", value)

    var height: String
        get() = sharedPreferences["height", ""]!!
        set(value) = sharedPreferences.set("height", value)

    var height_unit: String
        get() = sharedPreferences["height_unit", ""]!!
        set(value) = sharedPreferences.set("height_unit", value)

    var weight: String
        get() = sharedPreferences["weight", ""]!!
        set(value) = sharedPreferences.set("weight", value)

    var weight_unit: String
        get() = sharedPreferences["weight_unit", ""]!!
        set(value) = sharedPreferences.set("weight_unit", value)

    var salary: String
        get() = sharedPreferences["salary", ""]!!
        set(value) = sharedPreferences.set("salary", value)

    var looking_for: String
        get() = sharedPreferences["looking_for", ""]!!
        set(value) = sharedPreferences.set("looking_for", value)

    var max_distance: String
        get() = sharedPreferences["max_distance", ""]!!
        set(value) = sharedPreferences.set("max_distance", value)

    var age_range: String
        get() = sharedPreferences["age_range", ""]!!
        set(value) = sharedPreferences.set("age_range", value)

    var current_job: String
        get() = sharedPreferences["current_job", ""]!!
        set(value) = sharedPreferences.set("current_job", value)
    var profession: String
        get() = sharedPreferences["profession", ""]!!
        set(value) = sharedPreferences.set("profession", value)

    var distance_unit: String
        get() = sharedPreferences["distance_unit", ""]!!
        set(value) = sharedPreferences.set("distance_unit", value)

    var isSuper: String
        get() = sharedPreferences["isSuper", "0"]!!
        set(value) = sharedPreferences.set("isSuper", value)

    var sexual_orie: String
        get() = sharedPreferences["sexual_orie", ""]!!
        set(value) = sharedPreferences.set("sexual_orie", value)


    var latitudeFilter: String
        get() = sharedPreferences["latitudeFilter", ""]!!
        set(value) = sharedPreferences.set("latitudeFilter", value)

    var longitudeFilter: String
        get() = sharedPreferences["longitudeFilter", ""]!!
        set(value) = sharedPreferences.set("longitudeFilter", value)

    var isInfo: Boolean
        get() = sharedPreferences["isInfo", false]!!
        set(value) = sharedPreferences.set("isInfo", value)

    var isNearBy: Boolean
        get() = sharedPreferences["isNearBy", false]!!
        set(value) = sharedPreferences.set("isNearBy", value)

    var viewProfileOnSentLike: Boolean
        get() = sharedPreferences["viewProfileOnSentLike", false]!!
        set(value) = sharedPreferences.set("viewProfileOnSentLike", value)

    var viewProfileOnMyconnection: Boolean
        get() = sharedPreferences["viewProfileOnMyconnection", false]!!
        set(value) = sharedPreferences.set("viewProfileOnMyconnection", value)


    /*fun saveUserBeanData(userBean: ResponseUser) {
        user_id = userBean.userdetails.id.toString()
        latitude = userBean.userdetails.latitude!!
        longitude = userBean.userdetails.longitude!!
    }*/


    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit({ it.putString(key, value) })
            is Int -> edit({ it.putInt(key, value) })
            is Boolean -> edit({ it.putBoolean(key, value) })
            is Float -> edit({ it.putFloat(key, value) })
            is Long -> edit({ it.putLong(key, value) })
            else -> Log.e(TAG, "Setting shared pref failed for key: $key and value: $value ")
        }
    }

    fun deletePreference() {
        editor.clear()
        editor.apply()
        editor.commit()
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T? = null
    ): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented") as Throwable
        }
    }
}
