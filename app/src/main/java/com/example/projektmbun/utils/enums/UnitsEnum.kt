package com.example.projektmbun.utils.enums

/**
 * Enum of all possible units.
 * @param toBaseMultiplier the multiplier to convert to the base unit.
 */
enum class UnitsEnum(val toBaseMultiplier: Double) {
    STUECK(1.0),
    GRAMM(1.0),
    KILOGRAMM(1000.0),
    LITER(1000.0),
    MILLILITER(1.0),
    TEELOEFEL(5.0),
    ESSLOEFEL(15.0),
    PRISE(0.2),
    TASSE(250.0),
    GLAS(200.0),
    PACKUNG(1.0),
    NACH_GESCHMACK(1.0),
    DOSE(1.0),
    BECHER(1.0),
    UNITLESS(1.0);

    /**
     * Converts a quantity to the base unit.
     * @param quantity the quantity in the current unit.
     * @return the quantity in the base unit.
     */
    fun convertToBase(quantity: Double): Double {
        return quantity * toBaseMultiplier
    }

    /**
     * Converts a quantity from the base unit to a larger unit, if applicable.
     * @param quantity the quantity in the base unit.
     * @return a pair of the quantity in the larger unit and the larger unit enum.
     */
    fun convertToLarger(quantity: Double): Pair<Double, UnitsEnum> {
        return when (this) {
            GRAMM -> if (quantity >= KILOGRAMM.toBaseMultiplier) {
                quantity / KILOGRAMM.toBaseMultiplier to KILOGRAMM
            } else {
                quantity to GRAMM
            }
            MILLILITER -> if (quantity >= LITER.toBaseMultiplier) {
                quantity / LITER.toBaseMultiplier to LITER
            } else {
                quantity to MILLILITER
            }
            else -> quantity to this
        }
    }
}
