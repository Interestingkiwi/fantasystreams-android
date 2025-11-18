package com.example.fantasystreams.data.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class WebViewCookieJar : CookieJar {
    private val cookieManager = CookieManager.getInstance()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesString = cookieManager.getCookie(url.toString())
        if (cookiesString != null && cookiesString.isNotEmpty()) {
            return cookiesString.split(";").mapNotNull { cookieString ->
                Cookie.parse(url, cookieString.trim())
            }
        }
        return emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { cookie ->
            cookieManager.setCookie(urlString, cookie.toString())
        }
        cookieManager.flush()
    }
}