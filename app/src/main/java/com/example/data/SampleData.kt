package com.example.data

import kotlin.random.Random

object SampleData {

    private val residentFirstNames = listOf(
        "Muhammad", "Ahmed", "Ali", "Zainab", "Sarah", "Ayesha", "Hamza", "Fatima",
        "John", "David", "Robert", "Michael", "William", "James", "Mary", "Patricia",
        "Omar", "Osman", "Yusuf", "Sana", "Amara", "Bilal", "Kashif", "Tariq",
        "Usman", "Faisal", "Kamran", "Asif", "Noman", "Farhan", "Raza", "Hassan"
    )

    private val residentLastNames = listOf(
        "Khan", "Ali", "Ahmed", "Malik", "Butt", "Shah", "Sheikh", "Gill",
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson",
        "Chaudhry", "Riaz", "Siddiqui", "Iqbal", "Hashmi", "Qureshi", "Zafar", "Bukhari",
        "Dar", "Mughal", "Latif", "Abbasi", "Ghaffar", "Rasheed", "Farooq", "Baig"
    )

    private val feeders = listOf("Grid Feeder A", "Grid Feeder B", "Grid Feeder C", "Grid Feeder D")

    fun generate600Houses(): List<HouseRecord> {
        val list = mutableListOf<HouseRecord>()
        val random = Random(42) // Constant seed for deterministic visual preview

        // Generate 600 houses across 5 blocks: A, B, C, D, E (120 houses each)
        val blocks = listOf("A", "B", "C", "D", "E")
        
        for (b in blocks) {
            for (num in 1..120) {
                val houseNo = "$b-$num"
                val firstName = residentFirstNames[random.nextInt(residentFirstNames.size)]
                val lastName = residentLastNames[random.nextInt(residentLastNames.size)]
                val residentName = "$firstName $lastName"
                val feeder = feeders[random.nextInt(feeders.size)]

                // Determine house style based on block or number to make analytics interesting!
                // Luxury houses in block A, normal in B/C, minimalist/energy-saving in D/E.
                val isLuxury = b == "A" && num <= 30
                val isMedium = b == "B" || b == "C" || (b == "A" && num > 30)
                
                // 1. AC & Major Loads
                val acs = when {
                    isLuxury -> random.nextInt(4, 7) // 4 to 6 ACs
                    isMedium -> random.nextInt(1, 4) // 1 to 3 ACs
                    else -> random.nextInt(0, 2)     // 0 or 1 AC
                }
                val safetyBreakers = acs + random.nextInt(1, 3)

                // 2. Fans
                val ceilingFans = when {
                    isLuxury -> random.nextInt(8, 15)
                    isMedium -> random.nextInt(5, 9)
                    else -> random.nextInt(3, 6)
                }
                val exhaust10Plastic = random.nextInt(1, 3)
                val exhaust10Metal = if (isLuxury) random.nextInt(0, 2) else 0
                val exhaust12Plastic = if (isMedium) random.nextInt(0, 2) else 0
                val exhaust12Metal = if (isLuxury) random.nextInt(1, 3) else 0
                val bracketPlastic = random.nextInt(0, 3)
                val bracketMetal18 = if (isLuxury) random.nextInt(1, 4) else 0
                val falseCeilingExhaust = if (isLuxury) random.nextInt(1, 3) else 0
                val kitchenBlower = random.nextInt(1, 2)
                val falseCeilingFanPlastic = if (isLuxury) random.nextInt(2, 6) else 0

                // 3. Lighting
                val singleFluorescent = if (isLuxury) 0 else random.nextInt(0, 4)
                val doubleFluorescent = if (isLuxury) 0 else random.nextInt(0, 3)
                val bulbHolder = random.nextInt(1, 5)
                val ledSingle = random.nextInt(2, 10)
                val ledDouble = random.nextInt(1, 6)
                val ledWeatherProof = random.nextInt(1, 4)
                
                val ledDownlight5w = when {
                    isLuxury -> random.nextInt(15, 40)
                    isMedium -> random.nextInt(5, 20)
                    else -> random.nextInt(0, 8)
                }
                val ledDownlight13w = when {
                    isLuxury -> random.nextInt(10, 25)
                    isMedium -> random.nextInt(4, 12)
                    else -> random.nextInt(0, 5)
                }
                val ledDownlight21w = if (isLuxury) random.nextInt(4, 15) else 0
                val ledDownlight24w = if (isLuxury) random.nextInt(2, 10) else 0
                val ledVanity10w = random.nextInt(1, 4)
                
                // Tango lights (Accent / Spotlights / Landscape)
                val ledTango10w = if (isLuxury) random.nextInt(2, 8) else 0
                val ledTango20w = if (isLuxury) random.nextInt(1, 5) else 0
                val ledTango30w = if (isLuxury) random.nextInt(0, 4) else 0
                val ledTango50w = if (isLuxury) random.nextInt(0, 3) else 0
                val ledTango70w = if (isLuxury) random.nextInt(0, 2) else 0
                val ledTango200w = if (isLuxury && random.nextFloat() > 0.8f) 1 else 0
                val fancyLight10w = random.nextInt(0, 5)
                
                // Hi-bay lights (mostly zero for standard residential, but maybe a few high ceilings have them)
                val ledHibay150w = if (isLuxury && random.nextFloat() > 0.95f) 1 else 0
                val ledHibay200w = if (isLuxury && random.nextFloat() > 0.98f) 1 else 0
                val ledHibay2200w = 0 // extremely high wattage, usually zero
                
                val ledFalseCeilingPanel = when {
                    isLuxury -> random.nextInt(8, 20)
                    isMedium -> random.nextInt(2, 8)
                    else -> 0
                }

                // 4. Sockets
                val socket5A = when {
                    isLuxury -> random.nextInt(15, 30)
                    isMedium -> random.nextInt(8, 16)
                    else -> random.nextInt(4, 10)
                }
                val socket15A = when {
                    isLuxury -> random.nextInt(4, 10)
                    isMedium -> random.nextInt(2, 5)
                    else -> random.nextInt(1, 3)
                }
                val socket20A = when {
                    isLuxury -> random.nextInt(1, 4)
                    isMedium -> random.nextInt(0, 2)
                    else -> 0
                }

                val record = HouseRecord(
                    residentName = residentName,
                    houseNo = houseNo,
                    acQuantity = acs,
                    safetyBreakerQuantity = safetyBreakers,
                    singleFluorescentQuantity = singleFluorescent,
                    doubleFluorescentQuantity = doubleFluorescent,
                    bulbHolderQuantity = bulbHolder,
                    ceilingFanQuantity = ceilingFans,
                    exhaustFan10PlasticQuantity = exhaust10Plastic,
                    exhaustFan10MetalQuantity = exhaust10Metal,
                    exhaustFan12PlasticQuantity = exhaust12Plastic,
                    exhaustFan12MetalQuantity = exhaust12Metal,
                    bracketFanPlasticQuantity = bracketPlastic,
                    bracketFanMetal18Quantity = bracketMetal18,
                    falseCeilingExhaustFanQuantity = falseCeilingExhaust,
                    kitchenHoodBlowerQuantity = kitchenBlower,
                    falseCeilingFanPlasticQuantity = falseCeilingFanPlastic,
                    ledSingleQuantity = ledSingle,
                    ledDoubleQuantity = ledDouble,
                    ledWeatherProofQuantity = ledWeatherProof,
                    ledDownlight5WQuantity = ledDownlight5w,
                    ledDownlight13WQuantity = ledDownlight13w,
                    ledDownlight21WQuantity = ledDownlight21w,
                    ledDownlight24WQuantity = ledDownlight24w,
                    ledVanity10WQuantity = ledVanity10w,
                    ledTango10WQuantity = ledTango10w,
                    ledTango20WQuantity = ledTango20w,
                    ledTango30WQuantity = ledTango30w,
                    ledTango50WQuantity = ledTango50w,
                    ledTango70WQuantity = ledTango70w,
                    ledTango200WQuantity = ledTango200w,
                    fancyLight10WQuantity = fancyLight10w,
                    ledHiBay150WQuantity = ledHibay150w,
                    ledHiBay200WQuantity = ledHibay200w,
                    ledHiBay2200WQuantity = ledHibay2200w,
                    ledFalseCeilingPanelQuantity = ledFalseCeilingPanel,
                    socket5AQuantity = socket5A,
                    socket15AQuantity = socket15A,
                    socket20AQuantity = socket20A,
                    gridFeeder = feeder
                )
                list.add(record)
            }
        }
        return list
    }
}
