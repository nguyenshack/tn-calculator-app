package com.example.math

data class UnitCategory(
    val id: String,
    val name: String,
    val units: List<ConversionUnit>
)

data class ConversionUnit(
    val name: String,
    val symbol: String,
    val toBaseFactor: Double, // value * toBaseFactor = base value
    val isTemperature: Boolean = false
)

object UnitConverter {
    val categories = listOf(
        UnitCategory(
            id = "length",
            name = "Độ dài",
            units = listOf(
                ConversionUnit("Mét (Base)", "m", 1.0),
                ConversionUnit("Kilômét", "km", 1000.0),
                ConversionUnit("Xentimét", "cm", 0.01),
                ConversionUnit("Milimét", "mm", 0.001),
                ConversionUnit("Micrômét", "µm", 1e-6),
                ConversionUnit("Nanômét", "nm", 1e-9),
                ConversionUnit("Inch", "in", 0.0254),
                ConversionUnit("Foot", "ft", 0.3048),
                ConversionUnit("Yard", "yd", 0.9144),
                ConversionUnit("Dặm (Mile)", "mi", 1609.344),
                ConversionUnit("Hải lý (Nautical Mile)", "nmi", 1852.0)
            )
        ),
        UnitCategory(
            id = "area",
            name = "Diện tích",
            units = listOf(
                ConversionUnit("Mét vuông (Base)", "m²", 1.0),
                ConversionUnit("Kilômét vuông", "km²", 1e6),
                ConversionUnit("Hecta", "ha", 10000.0),
                ConversionUnit("Xentimét vuông", "cm²", 1e-4),
                ConversionUnit("Foot vuông", "ft²", 0.092903),
                ConversionUnit("Inch vuông", "in²", 0.00064516),
                ConversionUnit("Acre (Mẫu Anh)", "ac", 4046.8564)
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Thể tích",
            units = listOf(
                ConversionUnit("Lít (Base)", "L", 1.0),
                ConversionUnit("Mililít", "mL", 0.001),
                ConversionUnit("Mét khối", "m³", 1000.0),
                ConversionUnit("Xentimét khối (cc)", "cm³", 0.001),
                ConversionUnit("Gallon (Mỹ)", "gal", 3.78541),
                ConversionUnit("Ounce chất lỏng (fl oz)", "fl oz", 0.0295735),
                ConversionUnit("Foot khối", "ft³", 28.3168),
                ConversionUnit("Inch khối", "in³", 0.016387)
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Khối lượng",
            units = listOf(
                ConversionUnit("Kilôgam (Base)", "kg", 1.0),
                ConversionUnit("Gam", "g", 0.001),
                ConversionUnit("Miligam", "mg", 1e-6),
                ConversionUnit("Tấn", "t", 1000.0),
                ConversionUnit("Pound", "lb", 0.45359237),
                ConversionUnit("Ounce", "oz", 0.0283495)
            )
        ),
        UnitCategory(
            id = "time",
            name = "Thời gian",
            units = listOf(
                ConversionUnit("Giây (Base)", "s", 1.0),
                ConversionUnit("Phút", "min", 60.0),
                ConversionUnit("Giờ", "h", 3600.0),
                ConversionUnit("Ngày", "ngày", 86400.0),
                ConversionUnit("Tuần", "tuần", 604800.0),
                ConversionUnit("Tháng (30 ngày)", "tháng", 2592000.0),
                ConversionUnit("Năm (365 ngày)", "năm", 31536000.0)
            )
        ),
        UnitCategory(
            id = "storage",
            name = "Lưu trữ máy tính",
            units = listOf(
                ConversionUnit("Byte (Base)", "B", 1.0),
                ConversionUnit("Bit", "b", 0.125),
                ConversionUnit("Kilobyte (KB)", "KB", 1024.0),
                ConversionUnit("Megabyte (MB)", "MB", 1024.0 * 1024.0),
                ConversionUnit("Gigabyte (GB)", "GB", 1024.0 * 1024.0 * 1024.0),
                ConversionUnit("Terabyte (TB)", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Nhiệt độ",
            units = listOf(
                ConversionUnit("Độ C", "°C", 1.0, isTemperature = true),
                ConversionUnit("Độ F", "°F", 1.0, isTemperature = true),
                ConversionUnit("Kelvin", "K", 1.0, isTemperature = true)
            )
        ),
        UnitCategory(
            id = "velocity",
            name = "Vận tốc",
            units = listOf(
                ConversionUnit("Mét/giây (Base)", "m/s", 1.0),
                ConversionUnit("Kilômét/giờ", "km/h", 1.0 / 3.6),
                ConversionUnit("Dặm/giờ (mph)", "mph", 0.44704),
                ConversionUnit("Hải lý/giờ (knot)", "kn", 0.514444)
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Áp suất",
            units = listOf(
                ConversionUnit("Pascal (Base)", "Pa", 1.0),
                ConversionUnit("Kilopascal", "kPa", 1000.0),
                ConversionUnit("Bar", "bar", 100000.0),
                ConversionUnit("Atmosphere chuẩn", "atm", 101325.0),
                ConversionUnit("Milimét thủy ngân", "mmHg", 133.322),
                ConversionUnit("Pound trên inch vuông", "psi", 6894.76)
            )
        ),
        UnitCategory(
            id = "energy",
            name = "Năng lượng",
            units = listOf(
                ConversionUnit("Joule (Base)", "J", 1.0),
                ConversionUnit("Kilojoule", "kJ", 1000.0),
                ConversionUnit("Calorie", "cal", 4.184),
                ConversionUnit("Kilocalorie", "kcal", 4184.0),
                ConversionUnit("Kilowatt-giờ", "kWh", 3.6e6),
                ConversionUnit("Electron-volt", "eV", 1.602176634e-19),
                ConversionUnit("BTU", "BTU", 1055.06)
            )
        )
    )

    fun convert(value: Double, fromUnit: ConversionUnit, toUnit: ConversionUnit): Double {
        if (fromUnit == toUnit) return value
        if (fromUnit.isTemperature) {
            // Convert to Celsius first
            val celsius = when (fromUnit.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            // From Celsius to target
            return when (toUnit.symbol) {
                "°C" -> celsius
                "°F" -> celsius * 9.0 / 5.0 + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }
        val baseVal = value * fromUnit.toBaseFactor
        return baseVal / toUnit.toBaseFactor
    }
}
