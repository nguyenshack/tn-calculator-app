package com.example.math

import kotlin.math.abs
import kotlin.math.sqrt

object Factorizer {

    fun factorize(n: Long): String {
        require(n > 0) { "FACT chỉ áp dụng cho số nguyên dương > 0" }
        if (n == 1L) return "1"

        var remainder = n
        val factors = mutableListOf<Pair<Long, Int>>()

        var d = 2L
        while (d * d <= remainder) {
            var count = 0
            while (remainder % d == 0L) {
                count++
                remainder /= d
            }
            if (count > 0) {
                factors.add(Pair(d, count))
            }
            d = if (d == 2L) 3L else d + 2L
        }
        if (remainder > 1L) {
            factors.add(Pair(remainder, 1))
        }

        return factors.joinToString(" × ") { (prime, exp) ->
            if (exp == 1) "$prime" else "$prime^$exp"
        }
    }

    fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    fun gcdList(numbers: List<Long>): Long {
        if (numbers.isEmpty()) return 1L
        return numbers.reduce { acc, n -> gcd(acc, n) }
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a / gcd(a, b) * b)
    }

    fun lcmList(numbers: List<Long>): Long {
        if (numbers.isEmpty()) return 1L
        return numbers.reduce { acc, n -> lcm(acc, n) }
    }
}
