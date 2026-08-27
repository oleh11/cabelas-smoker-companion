package com.cabelas.smokercompanion.data.model

data class CookPreset(
    val id: String,
    val name: String,
    val description: String,
    val recommendedGrillTempF: Int,
    val recommendedSmokeLevel: Int,
    val targetInternalTempF: Int,
    val estimatedDurationHours: Double,
    val woodPairing: String,
    val tips: List<String>
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            CookPreset(
                id = "brisket",
                name = "Texas Style Brisket",
                description = "Low and slow full packer brisket with deep smoke ring and bark.",
                recommendedGrillTempF = 225,
                recommendedSmokeLevel = 8,
                targetInternalTempF = 203,
                estimatedDurationHours = 12.0,
                woodPairing = "Oak & Hickory",
                tips = listOf(
                    "Wrap in butcher paper when the stall hits at 165°F.",
                    "Rest for at least 2 hours in a cooler before slicing."
                )
            ),
            CookPreset(
                id = "pulled_pork",
                name = "Smoked Pork Shoulder (Boston Butt)",
                description = "Juicy, shreddable pulled pork with rich bark.",
                recommendedGrillTempF = 250,
                recommendedSmokeLevel = 7,
                targetInternalTempF = 203,
                estimatedDurationHours = 9.5,
                woodPairing = "Apple & Hickory",
                tips = listOf(
                    "Spritz with apple cider vinegar every 45 mins after hour 3.",
                    "Pull at 203°F when bone slides out effortlessly."
                )
            ),
            CookPreset(
                id = "ribs_321",
                name = "3-2-1 Smoked Baby Back Ribs",
                description = "Tender, fall-off-the-bone ribs using the classic 3-2-1 technique.",
                recommendedGrillTempF = 225,
                recommendedSmokeLevel = 6,
                targetInternalTempF = 195,
                estimatedDurationHours = 6.0,
                woodPairing = "Cherry & Pecan",
                tips = listOf(
                    "3 hours unwrapped with heavy smoke.",
                    "2 hours tightly wrapped in foil with butter and honey.",
                    "1 hour unwrapped glazed with BBQ sauce."
                )
            ),
            CookPreset(
                id = "salmon",
                name = "Sweet & Smoky Atlantic Salmon",
                description = "Rich, buttery hot-smoked salmon with maple glaze.",
                recommendedGrillTempF = 180,
                recommendedSmokeLevel = 9,
                targetInternalTempF = 145,
                estimatedDurationHours = 2.0,
                woodPairing = "Alder & Maple",
                tips = listOf(
                    "Dry brine for 4 hours to form a shiny pellicle before smoking.",
                    "Keep temps low (180°F) to prevent white albumin albumen extrusion."
                )
            ),
            CookPreset(
                id = "spatchcock_turkey",
                name = "Herb-Smoked Whole Turkey",
                description = "Crispy skin and juicy meat with uniform cooking.",
                recommendedGrillTempF = 275,
                recommendedSmokeLevel = 5,
                targetInternalTempF = 165,
                estimatedDurationHours = 4.0,
                woodPairing = "Pecan & Apple",
                tips = listOf(
                    "Spatchcock (remove backbone) for even cooking.",
                    "Probe the deepest part of the breast and thigh."
                )
            ),
            CookPreset(
                id = "reverse_sear_steak",
                name = "Reverse-Sear Cowboy Ribeye",
                description = "Edge-to-edge medium rare with deep woodsmoke flavor.",
                recommendedGrillTempF = 225,
                recommendedSmokeLevel = 10,
                targetInternalTempF = 130,
                estimatedDurationHours = 1.0,
                woodPairing = "Mesquite & Oak",
                tips = listOf(
                    "Smoke at 225°F until internal reaches 115°F.",
                    "Finish on high sear / cast iron until 130°F."
                )
            )
        )
    }
}
