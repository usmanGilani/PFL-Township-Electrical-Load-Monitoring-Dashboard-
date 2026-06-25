package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFetchGoogleSheetHeaders() {
    val sheetUrl = "https://docs.google.com/spreadsheets/d/1kYndPjWpIlPpEEyCXp_ZuKnA_RoX84u_IEyjlIie7QY/export?format=csv"
    val client = okhttp3.OkHttpClient()
    val request = okhttp3.Request.Builder().url(sheetUrl).build()
    client.newCall(request).execute().use { response ->
        assertTrue("HTTP Request failed: ${response.code}", response.isSuccessful)
        val body = response.body?.byteStream()
        assertNotNull("Response body is null", body)
        val reader = java.io.BufferedReader(java.io.InputStreamReader(body!!))
        val firstLine = reader.readLine()
        assertNotNull("First line (headers) is null", firstLine)
        System.out.println("SHEET_HEADERS: $firstLine")
        for (i in 1..3) {
            System.out.println("SHEET_ROW_$i: ${reader.readLine()}")
        }
    }
  }
}
