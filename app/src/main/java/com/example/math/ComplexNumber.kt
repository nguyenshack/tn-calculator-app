package com.example.math

import kotlin.math.*

data class ComplexNumber(val re: Double, val im: Double = 0.0) {
    operator fun plus(other: ComplexNumber) = ComplexNumber(re + other.re, im + other.im)
    operator fun minus(other: ComplexNumber) = ComplexNumber(re - other.re, im - other.im)
    operator fun times(other: ComplexNumber) = ComplexNumber(
        re * other.re - im * other.im,
        re * other.im + im * other.re
    )
    operator fun times(scalar: Double) = ComplexNumber(re * scalar, im * scalar)
    operator fun div(other: ComplexNumber): ComplexNumber {
        val denom = other.re * other.re + other.im * other.im
        if (abs(denom) < 1e-15) throw ArithmeticException("Division by zero in complex calculation")
        return ComplexNumber(
            (re * other.re + im * other.im) / denom,
            (im * other.re - re * other.im) / denom
        )
    }

    fun modulus(): Double = hypot(re, im)
    
    fun argumentRad(): Double = atan2(im, re)
    fun argumentDeg(): Double = Math.toDegrees(argumentRad())

    fun conjugate() = ComplexNumber(re, -im)

    fun pow(n: Double): ComplexNumber {
        val r = modulus()
        val theta = argumentRad()
        val newR = r.pow(n)
        val newTheta = theta * n
        return ComplexNumber(newR * cos(newTheta), newR * sin(newTheta))
    }

    fun toCartesianString(): String {
        val r = cleanDouble(re)
        val i = cleanDouble(abs(im))
        return when {
            abs(im) < 1e-10 -> r
            abs(re) < 1e-10 -> if (im < 0) "-${if (i == "1") "" else i}i" else "${if (i == "1") "" else i}i"
            im < 0 -> "$r - ${if (i == "1") "" else i}i"
            else -> "$r + ${if (i == "1") "" else i}i"
        }
    }

    fun toPolarString(isDeg: Boolean = true): String {
        val r = cleanDouble(modulus())
        val theta = if (isDeg) "${cleanDouble(argumentDeg())}°" else "${cleanDouble(argumentRad())} rad"
        return "$r ∠ $theta"
    }

    fun toTrigString(isDeg: Boolean = true): String {
        val r = cleanDouble(modulus())
        val theta = if (isDeg) "${cleanDouble(argumentDeg())}°" else "${cleanDouble(argumentRad())}"
        return "$r(cos $theta + i sin $theta)"
    }

    companion object {
        val ZERO = ComplexNumber(0.0, 0.0)
        val ONE = ComplexNumber(1.0, 0.0)
        val I = ComplexNumber(0.0, 1.0)

        fun cleanDouble(v: Double): String {
            if (abs(v - round(v)) < 1e-9) {
                return round(v).toLong().toString()
            }
            val formatted = String.format("%.6f", v).trimEnd('0').trimEnd('.')
            return if (formatted == "-0") "0" else formatted
        }
    }
}
