package com.example.math

data class ChemicalElement(
    val atomicNumber: Int,
    val symbol: String,
    val nameVi: String,
    val nameEn: String,
    val atomicMass: Double,
    val group: Int,
    val period: Int,
    val category: String,
    val electronConfig: String
)

data class PhysicalConstant(
    val symbol: String,
    val nameVi: String,
    val nameEn: String,
    val value: Double,
    val unit: String,
    val expressionStr: String
)

object PeriodicTableData {
    val constants = listOf(
        PhysicalConstant("c", "Tốc độ ánh sáng trong chân không", "Speed of light in vacuum", 299792458.0, "m/s", "299792458"),
        PhysicalConstant("h", "Hằng số Planck", "Planck constant", 6.62607015e-34, "J·s", "6.62607015×10^-34"),
        PhysicalConstant("ℏ", "Hằng số Dirac (h/2π)", "Reduced Planck constant", 1.054571817e-34, "J·s", "1.054571817×10^-34"),
        PhysicalConstant("G", "Hằng số hấp dẫn", "Newtonian constant of gravitation", 6.67430e-11, "m³/(kg·s²)", "6.67430×10^-11"),
        PhysicalConstant("e", "Điện tích nguyên tố", "Elementary charge", 1.602176634e-19, "C", "1.602176634×10^-19"),
        PhysicalConstant("m_e", "Khối lượng nghỉ của electron", "Electron mass", 9.1093837015e-31, "kg", "9.1093837015×10^-31"),
        PhysicalConstant("m_p", "Khối lượng nghỉ của proton", "Proton mass", 1.67262192369e-27, "kg", "1.67262192369×10^-27"),
        PhysicalConstant("m_n", "Khối lượng nghỉ của neutron", "Neutron mass", 1.67492749804e-27, "kg", "1.67492749804×10^-27"),
        PhysicalConstant("u", "Đơn vị khối lượng nguyên tử (amu)", "Atomic mass unit", 1.66053906660e-27, "kg", "1.66053906660×10^-27"),
        PhysicalConstant("N_A", "Hằng số Avogadro", "Avogadro constant", 6.02214076e23, "mol⁻¹", "6.02214076×10^23"),
        PhysicalConstant("R", "Hằng số khí lý tưởng", "Molar gas constant", 8.314462618, "J/(mol·K)", "8.314462618"),
        PhysicalConstant("k_B", "Hằng số Boltzmann", "Boltzmann constant", 1.380649e-23, "J/K", "1.380649×10^-23"),
        PhysicalConstant("F", "Hằng số Faraday", "Faraday constant", 96485.33212, "C/mol", "96485.33212"),
        PhysicalConstant("ε_0", "Độ điện thẩm chân không", "Vacuum electric permittivity", 8.8541878128e-12, "F/m", "8.8541878128×10^-12"),
        PhysicalConstant("μ_0", "Độ từ thẩm chân không", "Vacuum magnetic permeability", 1.25663706212e-6, "N/A²", "1.25663706212×10^-6"),
        PhysicalConstant("g", "Gia tốc trọng trường chuẩn", "Standard acceleration of gravity", 9.80665, "m/s²", "9.80665"),
        PhysicalConstant("σ", "Hằng số Stefan-Boltzmann", "Stefan-Boltzmann constant", 5.670374419e-8, "W/(m²·K⁴)", "5.670374419×10^-8"),
        PhysicalConstant("R_∞", "Hằng số Rydberg", "Rydberg constant", 10973731.568160, "m⁻¹", "10973731.568160"),
        PhysicalConstant("a_0", "Bán kính Bohr", "Bohr radius", 5.29177210903e-11, "m", "5.29177210903×10^-11"),
        PhysicalConstant("atm", "Áp suất khí quyển chuẩn", "Standard atmosphere", 101325.0, "Pa", "101325")
    )

    val elements = listOf(
        ChemicalElement(1, "H", "Hiđrô", "Hydrogen", 1.008, 1, 1, "Phi kim phản ứng", "1s¹"),
        ChemicalElement(2, "He", "Heli", "Helium", 4.0026, 18, 1, "Khí hiếm", "1s²"),
        ChemicalElement(3, "Li", "Liti", "Lithium", 6.94, 1, 2, "Kim loại kiềm", "[He] 2s¹"),
        ChemicalElement(4, "Be", "Beri", "Beryllium", 9.0122, 2, 2, "Kim loại kiềm thổ", "[He] 2s²"),
        ChemicalElement(5, "B", "Bo", "Boron", 10.81, 13, 2, "Á kim", "[He] 2s² 2p¹"),
        ChemicalElement(6, "C", "Cacbon", "Carbon", 12.011, 14, 2, "Phi kim phản ứng", "[He] 2s² 2p²"),
        ChemicalElement(7, "N", "Nitơ", "Nitrogen", 14.007, 15, 2, "Phi kim phản ứng", "[He] 2s² 2p³"),
        ChemicalElement(8, "O", "Oxy", "Oxygen", 15.999, 16, 2, "Phi kim phản ứng", "[He] 2s² 2p⁴"),
        ChemicalElement(9, "F", "Flo", "Fluorine", 18.998, 17, 2, "Phi kim phản ứng (Halogen)", "[He] 2s² 2p⁵"),
        ChemicalElement(10, "Ne", "Neon", "Neon", 20.180, 18, 2, "Khí hiếm", "[He] 2s² 2p⁶"),
        ChemicalElement(11, "Na", "Natri", "Sodium", 22.990, 1, 3, "Kim loại kiềm", "[Ne] 3s¹"),
        ChemicalElement(12, "Mg", "Magie", "Magnesium", 24.305, 2, 3, "Kim loại kiềm thổ", "[Ne] 3s²"),
        ChemicalElement(13, "Al", "Nhôm", "Aluminium", 26.982, 13, 3, "Kim loại sau chuyển tiếp", "[Ne] 3s² 3p¹"),
        ChemicalElement(14, "Si", "Silic", "Silicon", 28.085, 14, 3, "Á kim", "[Ne] 3s² 3p²"),
        ChemicalElement(15, "P", "Photpho", "Phosphorus", 30.974, 15, 3, "Phi kim phản ứng", "[Ne] 3s² 3p³"),
        ChemicalElement(16, "S", "Lưu huỳnh", "Sulfur", 32.06, 16, 3, "Phi kim phản ứng", "[Ne] 3s² 3p⁴"),
        ChemicalElement(17, "Cl", "Clo", "Chlorine", 35.45, 17, 3, "Phi kim phản ứng (Halogen)", "[Ne] 3s² 3p⁵"),
        ChemicalElement(18, "Ar", "Agon", "Argon", 39.948, 18, 3, "Khí hiếm", "[Ne] 3s² 3p⁶"),
        ChemicalElement(19, "K", "Kali", "Potassium", 39.098, 1, 4, "Kim loại kiềm", "[Ar] 4s¹"),
        ChemicalElement(20, "Ca", "Canxi", "Calcium", 40.078, 2, 4, "Kim loại kiềm thổ", "[Ar] 4s²"),
        ChemicalElement(21, "Sc", "Scandi", "Scandium", 44.956, 3, 4, "Kim loại chuyển tiếp", "[Ar] 3d¹ 4s²"),
        ChemicalElement(22, "Ti", "Titan", "Titanium", 47.867, 4, 4, "Kim loại chuyển tiếp", "[Ar] 3d² 4s²"),
        ChemicalElement(23, "V", "Vanadi", "Vanadium", 50.942, 5, 4, "Kim loại chuyển tiếp", "[Ar] 3d³ 4s²"),
        ChemicalElement(24, "Cr", "Crom", "Chromium", 51.996, 6, 4, "Kim loại chuyển tiếp", "[Ar] 3d⁵ 4s¹"),
        ChemicalElement(25, "Mn", "Mangan", "Manganese", 54.938, 7, 4, "Kim loại chuyển tiếp", "[Ar] 3d⁵ 4s²"),
        ChemicalElement(26, "Fe", "Sắt", "Iron", 55.845, 8, 4, "Kim loại chuyển tiếp", "[Ar] 3d⁶ 4s²"),
        ChemicalElement(27, "Co", "Coban", "Cobalt", 58.933, 9, 4, "Kim loại chuyển tiếp", "[Ar] 3d⁷ 4s²"),
        ChemicalElement(28, "Ni", "Niken", "Nickel", 58.693, 10, 4, "Kim loại chuyển tiếp", "[Ar] 3d⁸ 4s²"),
        ChemicalElement(29, "Cu", "Đồng", "Copper", 63.546, 11, 4, "Kim loại chuyển tiếp", "[Ar] 3d¹⁰ 4s¹"),
        ChemicalElement(30, "Zn", "Kẽm", "Zinc", 65.38, 12, 4, "Kim loại sau chuyển tiếp", "[Ar] 3d¹⁰ 4s²"),
        ChemicalElement(31, "Ga", "Gali", "Gallium", 69.723, 13, 4, "Kim loại sau chuyển tiếp", "[Ar] 3d¹⁰ 4s² 4p¹"),
        ChemicalElement(32, "Ge", "Gecmani", "Germanium", 72.630, 14, 4, "Á kim", "[Ar] 3d¹⁰ 4s² 4p²"),
        ChemicalElement(33, "As", "Asen", "Arsenic", 74.922, 15, 4, "Á kim", "[Ar] 3d¹⁰ 4s² 4p³"),
        ChemicalElement(34, "Se", "Selen", "Selenium", 78.971, 16, 4, "Phi kim phản ứng", "[Ar] 3d¹⁰ 4s² 4p⁴"),
        ChemicalElement(35, "Br", "Brom", "Bromine", 79.904, 17, 4, "Phi kim phản ứng (Halogen)", "[Ar] 3d¹⁰ 4s² 4p⁵"),
        ChemicalElement(36, "Kr", "Kripton", "Krypton", 83.798, 18, 4, "Khí hiếm", "[Ar] 3d¹⁰ 4s² 4p⁶"),
        ChemicalElement(37, "Rb", "Rubidi", "Rubidium", 85.468, 1, 5, "Kim loại kiềm", "[Kr] 5s¹"),
        ChemicalElement(38, "Sr", "Stronti", "Strontium", 87.62, 2, 5, "Kim loại kiềm thổ", "[Kr] 5s²"),
        ChemicalElement(47, "Ag", "Bạc", "Silver", 107.87, 11, 5, "Kim loại chuyển tiếp", "[Kr] 4d¹⁰ 5s¹"),
        ChemicalElement(50, "Sn", "Thiếc", "Tin", 118.71, 14, 5, "Kim loại sau chuyển tiếp", "[Kr] 4d¹⁰ 5s² 5p²"),
        ChemicalElement(53, "I", "Iốt", "Iodine", 126.90, 17, 5, "Phi kim phản ứng (Halogen)", "[Kr] 4d¹⁰ 5s² 5p⁵"),
        ChemicalElement(54, "Xe", "Xenon", "Xenon", 131.29, 18, 5, "Khí hiếm", "[Kr] 4d¹⁰ 5s² 5p⁶"),
        ChemicalElement(56, "Ba", "Bari", "Barium", 137.33, 2, 6, "Kim loại kiềm thổ", "[Xe] 6s²"),
        ChemicalElement(74, "W", "Vonfram", "Tungsten", 183.84, 6, 6, "Kim loại chuyển tiếp", "[Xe] 4f¹⁴ 5d⁴ 6s²"),
        ChemicalElement(78, "Pt", "Bạch kim", "Platinum", 195.08, 10, 6, "Kim loại chuyển tiếp", "[Xe] 4f¹⁴ 5d⁹ 6s¹"),
        ChemicalElement(79, "Au", "Vàng", "Gold", 196.97, 11, 6, "Kim loại chuyển tiếp", "[Xe] 4f¹⁴ 5d¹⁰ 6s¹"),
        ChemicalElement(80, "Hg", "Thủy ngân", "Mercury", 200.59, 12, 6, "Kim loại sau chuyển tiếp", "[Xe] 4f¹⁴ 5d¹⁰ 6s²"),
        ChemicalElement(82, "Pb", "Chì", "Lead", 207.2, 14, 6, "Kim loại sau chuyển tiếp", "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p²"),
        ChemicalElement(92, "U", "Urani", "Uranium", 238.03, 3, 7, "Actinit", "[Rn] 5f³ 6d¹ 7s²")
    )
}
