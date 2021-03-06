/**
 * ISC License
 *
 * Copyright 2018-2019, Saksham (DawnImpulse)
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE
 * OR PERFORMANCE OF THIS SOFTWARE.
 **/
package org.sourcei.kowts.network

import org.sourcei.kowts.utils.pojo.ListQuotes
import org.sourcei.kowts.utils.pojo.PojoQuotes
import org.sourcei.kowts.utils.handler.ErrorHandler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @info -
 *
 * @author - Saksham
 * @note Last Branch Update - master
 *
 * @note Created on 2019-08-20 by Saksham
 * @note Updates :
 *  Saksham - 2019 09 07 - master - using list of PojoQuotes
 */
object Repo {
    val client = RetroApiClient.getClient().create(Source::class.java)

    /**
     * get random quote
     *
     * @param callback
     */
    fun getRandomQuote(callback: (Any?, List<PojoQuotes>?) -> Unit) {

        val call = client.randomQuote()

        call.enqueue(object : Callback<ListQuotes> {

            // response
            override fun onResponse(call: Call<ListQuotes>, response: Response<ListQuotes>) {
                if (response.isSuccessful) {
                    callback(null, response.body()!!.details)
                } else
                    callback(ErrorHandler.parseError(response), null)
            }

            // on failure
            override fun onFailure(call: Call<ListQuotes>, t: Throwable) {
                callback(t.toString(), null)
            }
        })
    }
}