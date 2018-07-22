package com.lgsdiamond.amtree.Amway

class FirstBonus {

    companion object {
        class PVrate(val pv: Float, val rate: Float)

        val firstBonus = arrayOf(
                PVrate(1_000.0f, 0.21f),
                PVrate(680.0f, 0.18f),
                PVrate(400.0f, 0.15f),
                PVrate(240.0f, 0.12f),
                PVrate(120.0f, 0.09f),
                PVrate(60.0f, 0.06f),
                PVrate(20.0f, 0.03f),
                PVrate(-0.1f, 0.00f)
        )

        fun computeFirstBonus(pv: Float): Float {
            require(pv >= 0.0f)
            val bonusRate = firstBonus.first { (pv >= it.pv) }.rate
            return bonusRate * pv
        }
    }
}