package com.example.data

enum class ApplianceCategory(val displayName: String) {
    MAJOR_LOADS("AC & Major Loads"),
    FANS("Fans"),
    LIGHTING("Lighting"),
    SOCKETS("Sockets"),
    OTHERS("Others")
}

enum class ApplianceType(
    val id: String,
    val displayName: String,
    val ratedWattage: Int,
    val category: ApplianceCategory,
    val csvHeaderName: String
) {
    AC(
        "ac",
        "AC(s) Installed",
        1500,
        ApplianceCategory.MAJOR_LOADS,
        "No. of AC(s) Installed"
    ),
    SAFETY_BREAKER(
        "safety_breaker",
        "Safety Breaker(s) Installed",
        0,
        ApplianceCategory.MAJOR_LOADS,
        "No. of Safety Breaker(s) Installed"
    ),
    SINGLE_FLUORESCENT(
        "single_fluorescent",
        "Single Fixture Fluorescent",
        40,
        ApplianceCategory.LIGHTING,
        "No. of Single Fixture Fluorescent"
    ),
    DOUBLE_FLUORESCENT(
        "double_fluorescent",
        "Double Fixture Fluorescent",
        80,
        ApplianceCategory.LIGHTING,
        "No. of Double Fixture Fluorescent"
    ),
    BULB_HOLDER(
        "bulb_holder",
        "Bulb Holder Screw Type",
        10,
        ApplianceCategory.LIGHTING,
        "No. of Bulb Holder Screw Type"
    ),
    CEILING_FAN(
        "ceiling_fan",
        "Ceiling Fan",
        70,
        ApplianceCategory.FANS,
        "No. of Ceiling Fan"
    ),
    EXHAUST_FAN_10_PLASTIC(
        "exhaust_10_plastic",
        "Exhaust Fan Plastic Body - 10 inch",
        40,
        ApplianceCategory.FANS,
        "No. of Exhaust Fan Plastic Body - 10 inch"
    ),
    EXHAUST_FAN_10_METAL(
        "exhaust_10_metal",
        "Exhaust Fan Metal Body - 10 inch",
        50,
        ApplianceCategory.FANS,
        "No. of Exhaust Fan Metal Body - 10 inch"
    ),
    EXHAUST_FAN_12_PLASTIC(
        "exhaust_12_plastic",
        "Exhaust Fan Plastic Body - 12 inch",
        60,
        ApplianceCategory.FANS,
        "No. of Exhaust Fan Plastic Body - 12 inch"
    ),
    EXHAUST_FAN_12_METAL(
        "exhaust_12_metal",
        "Exhaust Fan Metal Body - 12 inch",
        70,
        ApplianceCategory.FANS,
        "No. of Exhaust Fan Metal Body - 12 inch"
    ),
    BRACKET_FAN_PLASTIC(
        "bracket_plastic",
        "Bracket Fan Plastic Body",
        60,
        ApplianceCategory.FANS,
        "No. of Bracket Fan Plastic Body"
    ),
    BRACKET_FAN_METAL(
        "bracket_metal_18",
        "Bracket Fan Metal Body - 18inch",
        80,
        ApplianceCategory.FANS,
        "No. of Bracket Fan Metal Body - 18inch"
    ),
    FALSE_CEILING_EXHAUST(
        "false_ceiling_exhaust",
        "False Ceiling Exhaust Fan",
        60,
        ApplianceCategory.FANS,
        "No. of False Ceiling Exhaust Fan"
    ),
    KITCHEN_HOOD_BLOWER(
        "kitchen_hood_blower",
        "Kitchen Hood Blower Fan",
        200,
        ApplianceCategory.FANS,
        "No. of Kitchen Hood Blower Fan"
    ),
    FALSE_CEILING_FAN_PLASTIC(
        "false_ceiling_fan_plastic",
        "False Ceiling Fan Plastic Body",
        70,
        ApplianceCategory.FANS,
        "No. of False Ceiling Fan Plastic Body"
    ),
    LED_SINGLE(
        "led_single",
        "LED Single Fixture",
        10,
        ApplianceCategory.LIGHTING,
        "No. of LED Single Fixture"
    ),
    LED_DOUBLE(
        "led_double",
        "LED Double Fixture",
        20,
        ApplianceCategory.LIGHTING,
        "No. of LED Double Fixture"
    ),
    LED_WEATHER_PROOF(
        "led_weather_proof",
        "LED Weather Proof Light with Cover",
        20,
        ApplianceCategory.LIGHTING,
        "No. of LED Weather Proof Light with Cover"
    ),
    LED_DOWNLIGHT_5W(
        "led_downlight_5w",
        "LED Downlight - 5W",
        5,
        ApplianceCategory.LIGHTING,
        "No. of LED Downlight - 5W"
    ),
    LED_DOWNLIGHT_13W(
        "led_downlight_13w",
        "LED Downlight - 13W",
        13,
        ApplianceCategory.LIGHTING,
        "No. of LED Downlight - 13W"
    ),
    LED_DOWNLIGHT_21W(
        "led_downlight_21w",
        "LED Downlight - 21W",
        21,
        ApplianceCategory.LIGHTING,
        "No. of LED Downlight - 21W"
    ),
    LED_DOWNLIGHT_24W(
        "led_downlight_24w",
        "LED Downlight - 24W",
        24,
        ApplianceCategory.LIGHTING,
        "No. of LED Downlight - 24W"
    ),
    LED_VANITY_10W(
        "led_vanity_10w",
        "LED Vanity Light - 10W",
        10,
        ApplianceCategory.LIGHTING,
        "No. of LED Vanity Light - 10W"
    ),
    LED_TANGO_10W(
        "led_tango_10w",
        "LED Tango Light - 10W",
        10,
        ApplianceCategory.LIGHTING,
        "No. of LED Tango Light - 10W"
    ),
    LED_TANGO_20W(
        "led_tango_20w",
        "LED Tango Light - 20W",
        20,
        ApplianceCategory.LIGHTING,
        "No. of LED Tango Light - 20W"
    ),
    LED_TANGO_30W(
        "led_tango_30w",
        "LED Tango Light - 30W",
        30,
        ApplianceCategory.LIGHTING,
        "No. of LED Tango Light - 30W"
    ),
    LED_TANGO_50W(
        "led_tango_50w",
        "LED Tango Light - 50W",
        50,
        ApplianceCategory.LIGHTING,
        "No. of LED Tango Light - 50W"
    ),
    LED_TANGO_70W(
        "led_tango_70w",
        "LED Tango Light - 70W",
        70,
        ApplianceCategory.LIGHTING,
        "No. of LED Tango Light - 70W"
    ),
    LED_TANGO_200W(
        "led_tango_200w",
        "LED Tango Light - 200W",
        200,
        ApplianceCategory.LIGHTING,
        "No. of LED Tango Light - 200W"
    ),
    FANCY_LIGHT_10W(
        "fancy_light_10w",
        "Fancy Light - 10W",
        10,
        ApplianceCategory.LIGHTING,
        "No. of Fancy Light - 10W"
    ),
    LED_HI_BAY_150W(
        "led_hibay_150w",
        "LED Hi-Bay Light - 150W",
        150,
        ApplianceCategory.LIGHTING,
        "No. of LED Hi-Bay Light - 150W"
    ),
    LED_HI_BAY_200W(
        "led_hibay_200w",
        "LED Hi-Bay Light - 200W",
        200,
        ApplianceCategory.LIGHTING,
        "No. of LED Hi-Bay Light - 200W"
    ),
    LED_HI_BAY_2200W(
        "led_hibay_2200w",
        "LED Hi-Bay Light - 2200W",
        2200,
        ApplianceCategory.LIGHTING,
        "No. of LED Hi-Bay Light - 2200W"
    ),
    LED_PANEL_LIGHTS(
        "led_panel_lights",
        "LED False Ceiling Panel Lights",
        40,
        ApplianceCategory.LIGHTING,
        "No. of LED False Ceiling Panel Lights"
    ),
    SOCKET_5A(
        "socket_5a",
        "5A Sockets",
        100,
        ApplianceCategory.SOCKETS,
        "No. of 5A sockets"
    ),
    SOCKET_15A(
        "socket_15a",
        "15A Sockets",
        1500,
        ApplianceCategory.SOCKETS,
        "No. of 15A sockets"
    ),
    SOCKET_20A(
        "socket_20a",
        "20A Sockets",
        3000,
        ApplianceCategory.SOCKETS,
        "No. of 20A sockets"
    );

    companion object {
        fun fromId(id: String): ApplianceType? = values().find { it.id == id }
        fun fromHeaderName(header: String): ApplianceType? = values().find { 
            it.csvHeaderName.equals(header.trim(), ignoreCase = true) 
        }
    }
}
