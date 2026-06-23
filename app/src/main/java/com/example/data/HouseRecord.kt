package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "house_records")
data class HouseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val residentName: String,
    val houseNo: String,
    val acQuantity: Int = 0,
    val safetyBreakerQuantity: Int = 0,
    val singleFluorescentQuantity: Int = 0,
    val doubleFluorescentQuantity: Int = 0,
    val bulbHolderQuantity: Int = 0,
    val ceilingFanQuantity: Int = 0,
    val exhaustFan10PlasticQuantity: Int = 0,
    val exhaustFan10MetalQuantity: Int = 0,
    val exhaustFan12PlasticQuantity: Int = 0,
    val exhaustFan12MetalQuantity: Int = 0,
    val bracketFanPlasticQuantity: Int = 0,
    val bracketFanMetal18Quantity: Int = 0,
    val falseCeilingExhaustFanQuantity: Int = 0,
    val kitchenHoodBlowerQuantity: Int = 0,
    val falseCeilingFanPlasticQuantity: Int = 0,
    val ledSingleQuantity: Int = 0,
    val ledDoubleQuantity: Int = 0,
    val ledWeatherProofQuantity: Int = 0,
    val ledDownlight5WQuantity: Int = 0,
    val ledDownlight13WQuantity: Int = 0,
    val ledDownlight21WQuantity: Int = 0,
    val ledDownlight24WQuantity: Int = 0,
    val ledVanity10WQuantity: Int = 0,
    val ledTango10WQuantity: Int = 0,
    val ledTango20WQuantity: Int = 0,
    val ledTango30WQuantity: Int = 0,
    val ledTango50WQuantity: Int = 0,
    val ledTango70WQuantity: Int = 0,
    val ledTango200WQuantity: Int = 0,
    val fancyLight10WQuantity: Int = 0,
    val ledHiBay150WQuantity: Int = 0,
    val ledHiBay200WQuantity: Int = 0,
    val ledHiBay2200WQuantity: Int = 0,
    val ledFalseCeilingPanelQuantity: Int = 0,
    val socket5AQuantity: Int = 0,
    val socket15AQuantity: Int = 0,
    val socket20AQuantity: Int = 0,
    val gridFeeder: String = ""
) {
    fun getQuantity(type: ApplianceType): Int {
        return when (type) {
            ApplianceType.AC -> acQuantity
            ApplianceType.SAFETY_BREAKER -> safetyBreakerQuantity
            ApplianceType.SINGLE_FLUORESCENT -> singleFluorescentQuantity
            ApplianceType.DOUBLE_FLUORESCENT -> doubleFluorescentQuantity
            ApplianceType.BULB_HOLDER -> bulbHolderQuantity
            ApplianceType.CEILING_FAN -> ceilingFanQuantity
            ApplianceType.EXHAUST_FAN_10_PLASTIC -> exhaustFan10PlasticQuantity
            ApplianceType.EXHAUST_FAN_10_METAL -> exhaustFan10MetalQuantity
            ApplianceType.EXHAUST_FAN_12_PLASTIC -> exhaustFan12PlasticQuantity
            ApplianceType.EXHAUST_FAN_12_METAL -> exhaustFan12MetalQuantity
            ApplianceType.BRACKET_FAN_PLASTIC -> bracketFanPlasticQuantity
            ApplianceType.BRACKET_FAN_METAL -> bracketFanMetal18Quantity
            ApplianceType.FALSE_CEILING_EXHAUST -> falseCeilingExhaustFanQuantity
            ApplianceType.KITCHEN_HOOD_BLOWER -> kitchenHoodBlowerQuantity
            ApplianceType.FALSE_CEILING_FAN_PLASTIC -> falseCeilingFanPlasticQuantity
            ApplianceType.LED_SINGLE -> ledSingleQuantity
            ApplianceType.LED_DOUBLE -> ledDoubleQuantity
            ApplianceType.LED_WEATHER_PROOF -> ledWeatherProofQuantity
            ApplianceType.LED_DOWNLIGHT_5W -> ledDownlight5WQuantity
            ApplianceType.LED_DOWNLIGHT_13W -> ledDownlight13WQuantity
            ApplianceType.LED_DOWNLIGHT_21W -> ledDownlight21WQuantity
            ApplianceType.LED_DOWNLIGHT_24W -> ledDownlight24WQuantity
            ApplianceType.LED_VANITY_10W -> ledVanity10WQuantity
            ApplianceType.LED_TANGO_10W -> ledTango10WQuantity
            ApplianceType.LED_TANGO_20W -> ledTango20WQuantity
            ApplianceType.LED_TANGO_30W -> ledTango30WQuantity
            ApplianceType.LED_TANGO_50W -> ledTango50WQuantity
            ApplianceType.LED_TANGO_70W -> ledTango70WQuantity
            ApplianceType.LED_TANGO_200W -> ledTango200WQuantity
            ApplianceType.FANCY_LIGHT_10W -> fancyLight10WQuantity
            ApplianceType.LED_HI_BAY_150W -> ledHiBay150WQuantity
            ApplianceType.LED_HI_BAY_200W -> ledHiBay200WQuantity
            ApplianceType.LED_HI_BAY_2200W -> ledHiBay2200WQuantity
            ApplianceType.LED_PANEL_LIGHTS -> ledFalseCeilingPanelQuantity
            ApplianceType.SOCKET_5A -> socket5AQuantity
            ApplianceType.SOCKET_15A -> socket15AQuantity
            ApplianceType.SOCKET_20A -> socket20AQuantity
        }
    }

    /**
     * Total load contribution for a single appliance type in Watts.
     */
    fun getLoadContribution(type: ApplianceType): Int {
        return getQuantity(type) * type.ratedWattage
    }

    /**
     * Total Connected Load in Watts
     */
    fun calculateTotalLoad(): Int {
        return ApplianceType.values().sumOf { getLoadContribution(it) }
    }

    /**
     * Total Connected Load in kilowatts (kW)
     */
    fun calculateTotalLoadKw(): Double {
        return calculateTotalLoad() / 1000.0
    }

    /**
     * Calculated load per category in Watts
     */
    fun calculateCategoryLoad(category: ApplianceCategory): Int {
        return ApplianceType.values()
            .filter { it.category == category }
            .sumOf { getLoadContribution(it) }
    }

    /**
     * Extracts block designation from house number.
     * E.g., "A-101" -> "Block A", "Block-B-12" -> "Block B", "Villa-22" -> "Villa"
     */
    fun getBlock(): String {
        if (houseNo.isBlank()) return "Unknown"
        
        // Match standard prefix like A-123 or Block B or B-50
        val trimmed = houseNo.trim()
        val parts = trimmed.split('-', ' ')
        if (parts.isNotEmpty()) {
            val first = parts[0]
            if (first.length == 1 && first[0].isLetter()) {
                return "Block ${first.uppercase()}"
            }
            if (first.equals("Block", ignoreCase = true) && parts.size > 1) {
                return "Block ${parts[1].uppercase()}"
            }
            // If it starts with a letter followed by numbers directly (e.g. A102)
            if (trimmed[0].isLetter() && (trimmed.length > 1 && trimmed[1].isDigit())) {
                return "Block ${trimmed[0].uppercase()}"
            }
        }
        
        // Fallback: search for first letter
        val firstLetter = trimmed.firstOrNull { it.isLetter() }
        return if (firstLetter != null) {
            "Block ${firstLetter.uppercase()}"
        } else {
            "General"
        }
    }
}
