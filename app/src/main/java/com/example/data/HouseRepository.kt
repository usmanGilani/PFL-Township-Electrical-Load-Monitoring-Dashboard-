package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

class HouseRepository(private val houseDao: HouseDao) {

    val allRecords: Flow<List<HouseRecord>> = houseDao.getAllRecordsFlow()

    suspend fun getRecordById(id: Int): HouseRecord? = withContext(Dispatchers.IO) {
        houseDao.getRecordById(id)
    }

    suspend fun insertRecords(records: List<HouseRecord>) = withContext(Dispatchers.IO) {
        houseDao.insertRecords(records)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        houseDao.clearAllRecords()
    }

    /**
     * Converts standard Google Sheets sharing or published URLs into a direct CSV export URL.
     */
    fun convertToCsvUrl(inputUrl: String): String {
        val trimmed = inputUrl.trim()
        
        // Handle spreadsheets/d/{id} standard links
        if (trimmed.contains("/spreadsheets/d/")) {
            val parts = trimmed.split("/spreadsheets/d/")
            if (parts.size > 1) {
                val idPart = parts[1].split("/")[0]
                return "https://docs.google.com/spreadsheets/d/$idPart/export?format=csv"
            }
        }
        
        // Handle published CSV links
        if (trimmed.contains("/pubhtml")) {
            return trimmed.replace("/pubhtml", "/pub?output=csv")
        }
        if (trimmed.contains("/pub") && !trimmed.contains("output=csv")) {
            return if (trimmed.contains("?")) "$trimmed&output=csv" else "$trimmed?output=csv"
        }
        
        return trimmed
    }

    /**
     * Downloads CSV from the given URL and parses it into HouseRecords, then stores in the Room DB.
     */
    suspend fun syncGoogleSheet(sheetUrl: String): Int = withContext(Dispatchers.IO) {
        val csvUrl = convertToCsvUrl(sheetUrl)
        Log.d("HouseRepository", "Connecting to CSV URL: $csvUrl")

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(csvUrl).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to download sheet. HTTP Status: ${response.code}")
            }

            val inputStream = response.body?.byteStream() ?: throw Exception("Empty response body")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            val records = parseCsv(reader)
            
            if (records.isNotEmpty()) {
                houseDao.clearAllRecords()
                houseDao.insertRecords(records)
                Log.d("HouseRepository", "Successfully synced ${records.size} records.")
            }
            
            return@withContext records.size
        }
    }

    /**
     * Parses CSV lines dynamically.
     */
    private fun parseCsv(reader: BufferedReader): List<HouseRecord> {
        val records = mutableListOf<HouseRecord>()
        
        val firstLine = reader.readLine() ?: return emptyList()
        val headers = parseCsvLine(firstLine)
        
        // Identify crucial column indices based on header names
        var residentNameIndex = -1
        var houseNoIndex = -1
        var gridFeederIndex = -1
        
        // Map of index -> ApplianceType for easy dynamic parsing
        val applianceIndices = mutableMapOf<Int, ApplianceType>()
        
        headers.forEachIndexed { index, header ->
            val normalized = header.trim().lowercase()
            when {
                normalized.contains("resident name") || normalized.equals("resident", ignoreCase = true) -> {
                    residentNameIndex = index
                }
                normalized.contains("house no") || normalized.contains("building name") || normalized.equals("house", ignoreCase = true) -> {
                    houseNoIndex = index
                }
                normalized.contains("grid feeder") || normalized.equals("feeder", ignoreCase = true) -> {
                    gridFeederIndex = index
                }
                else -> {
                    val applianceType = ApplianceType.fromHeaderName(header)
                    if (applianceType != null) {
                        applianceIndices[index] = applianceType
                    }
                }
            }
        }

        // Apply defaults if columns are missing or if standard indexing applies
        if (residentNameIndex == -1) residentNameIndex = 0
        if (houseNoIndex == -1) houseNoIndex = 1

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val row = parseCsvLine(line!!)
            if (row.isEmpty()) continue
            
            val residentName = row.getOrNull(residentNameIndex) ?: "Unknown Resident"
            val houseNo = row.getOrNull(houseNoIndex) ?: "Unknown House"
            val gridFeeder = if (gridFeederIndex != -1 && gridFeederIndex < row.size) {
                row[gridFeederIndex]
            } else {
                "Grid Feeder A"
            }

            // Parse individual quantities
            val quantities = mutableMapOf<ApplianceType, Int>()
            ApplianceType.values().forEach { quantities[it] = 0 }

            applianceIndices.forEach { (index, applianceType) ->
                if (index < row.size) {
                    val cellValue = row[index].trim()
                    val qty = cellValue.toIntOrNull() ?: 0
                    quantities[applianceType] = qty
                }
            }

            val record = HouseRecord(
                residentName = residentName,
                houseNo = houseNo,
                acQuantity = quantities[ApplianceType.AC] ?: 0,
                safetyBreakerQuantity = quantities[ApplianceType.SAFETY_BREAKER] ?: 0,
                singleFluorescentQuantity = quantities[ApplianceType.SINGLE_FLUORESCENT] ?: 0,
                doubleFluorescentQuantity = quantities[ApplianceType.DOUBLE_FLUORESCENT] ?: 0,
                bulbHolderQuantity = quantities[ApplianceType.BULB_HOLDER] ?: 0,
                ceilingFanQuantity = quantities[ApplianceType.CEILING_FAN] ?: 0,
                exhaustFan10PlasticQuantity = quantities[ApplianceType.EXHAUST_FAN_10_PLASTIC] ?: 0,
                exhaustFan10MetalQuantity = quantities[ApplianceType.EXHAUST_FAN_10_METAL] ?: 0,
                exhaustFan12PlasticQuantity = quantities[ApplianceType.EXHAUST_FAN_12_PLASTIC] ?: 0,
                exhaustFan12MetalQuantity = quantities[ApplianceType.EXHAUST_FAN_12_METAL] ?: 0,
                bracketFanPlasticQuantity = quantities[ApplianceType.BRACKET_FAN_PLASTIC] ?: 0,
                bracketFanMetal18Quantity = quantities[ApplianceType.BRACKET_FAN_METAL] ?: 0,
                falseCeilingExhaustFanQuantity = quantities[ApplianceType.FALSE_CEILING_EXHAUST] ?: 0,
                kitchenHoodBlowerQuantity = quantities[ApplianceType.KITCHEN_HOOD_BLOWER] ?: 0,
                falseCeilingFanPlasticQuantity = quantities[ApplianceType.FALSE_CEILING_FAN_PLASTIC] ?: 0,
                ledSingleQuantity = quantities[ApplianceType.LED_SINGLE] ?: 0,
                ledDoubleQuantity = quantities[ApplianceType.LED_DOUBLE] ?: 0,
                ledWeatherProofQuantity = quantities[ApplianceType.LED_WEATHER_PROOF] ?: 0,
                ledDownlight5WQuantity = quantities[ApplianceType.LED_DOWNLIGHT_5W] ?: 0,
                ledDownlight13WQuantity = quantities[ApplianceType.LED_DOWNLIGHT_13W] ?: 0,
                ledDownlight21WQuantity = quantities[ApplianceType.LED_DOWNLIGHT_21W] ?: 0,
                ledDownlight24WQuantity = quantities[ApplianceType.LED_DOWNLIGHT_24W] ?: 0,
                ledVanity10WQuantity = quantities[ApplianceType.LED_VANITY_10W] ?: 0,
                ledTango10WQuantity = quantities[ApplianceType.LED_TANGO_10W] ?: 0,
                ledTango20WQuantity = quantities[ApplianceType.LED_TANGO_20W] ?: 0,
                ledTango30WQuantity = quantities[ApplianceType.LED_TANGO_30W] ?: 0,
                ledTango50WQuantity = quantities[ApplianceType.LED_TANGO_50W] ?: 0,
                ledTango70WQuantity = quantities[ApplianceType.LED_TANGO_70W] ?: 0,
                ledTango200WQuantity = quantities[ApplianceType.LED_TANGO_200W] ?: 0,
                fancyLight10WQuantity = quantities[ApplianceType.FANCY_LIGHT_10W] ?: 0,
                ledHiBay150WQuantity = quantities[ApplianceType.LED_HI_BAY_150W] ?: 0,
                ledHiBay200WQuantity = quantities[ApplianceType.LED_HI_BAY_200W] ?: 0,
                ledHiBay2200WQuantity = quantities[ApplianceType.LED_HI_BAY_2200W] ?: 0,
                ledFalseCeilingPanelQuantity = quantities[ApplianceType.LED_PANEL_LIGHTS] ?: 0,
                socket5AQuantity = quantities[ApplianceType.SOCKET_5A] ?: 0,
                socket15AQuantity = quantities[ApplianceType.SOCKET_15A] ?: 0,
                socket20AQuantity = quantities[ApplianceType.SOCKET_20A] ?: 0,
                gridFeeder = gridFeeder
            )
            records.add(record)
        }
        return records
    }

    /**
     * Splits CSV lines properly by commas while respecting double quotes.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var currentToken = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(currentToken.toString().trim().removeSurrounding("\""))
                currentToken = StringBuilder()
            } else {
                currentToken.append(c)
            }
            i++
        }
        result.add(currentToken.toString().trim().removeSurrounding("\""))
        return result
    }
}
