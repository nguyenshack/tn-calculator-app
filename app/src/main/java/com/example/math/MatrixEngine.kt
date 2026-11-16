package com.example.math

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

data class Matrix(val rows: Int, val cols: Int, val data: List<List<Double>>) {
    init {
        require(rows > 0 && cols > 0)
        require(data.size == rows && data.all { it.size == cols })
    }

    operator fun get(r: Int, c: Int): Double = data[r][c]

    operator fun plus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Matrix dimensions must match for addition" }
        return Matrix(rows, cols, List(rows) { r -> List(cols) { c -> this[r, c] + other[r, c] } })
    }

    operator fun minus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Matrix dimensions must match for subtraction" }
        return Matrix(rows, cols, List(rows) { r -> List(cols) { c -> this[r, c] - other[r, c] } })
    }

    operator fun times(scalar: Double): Matrix {
        return Matrix(rows, cols, List(rows) { r -> List(cols) { c -> this[r, c] * scalar } })
    }

    operator fun times(other: Matrix): Matrix {
        require(cols == other.rows) { "Matrix multiplication cols of A must equal rows of B" }
        return Matrix(rows, other.cols, List(rows) { r ->
            List(other.cols) { c ->
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += this[r, k] * other[k, c]
                }
                sum
            }
        })
    }

    fun transpose(): Matrix {
        return Matrix(cols, rows, List(cols) { c -> List(rows) { r -> this[r, c] } })
    }

    fun determinant(): Double {
        require(rows == cols) { "Determinant is only defined for square matrices" }
        val n = rows
        if (n == 1) return data[0][0]
        if (n == 2) return data[0][0] * data[1][1] - data[0][1] * data[1][0]

        val mat = Array(n) { r -> DoubleArray(n) { c -> this[r, c] } }
        var det = 1.0
        var sign = 1.0

        for (i in 0 until n) {
            var pivot = i
            for (j in i + 1 until n) {
                if (abs(mat[j][i]) > abs(mat[pivot][i])) {
                    pivot = j
                }
            }
            if (abs(mat[pivot][i]) < 1e-12) return 0.0
            if (pivot != i) {
                val temp = mat[i]
                mat[i] = mat[pivot]
                mat[pivot] = temp
                sign = -sign
            }
            det *= mat[i][i]
            for (j in i + 1 until n) {
                val factor = mat[j][i] / mat[i][i]
                for (k in i until n) {
                    mat[j][k] -= factor * mat[i][k]
                }
            }
        }
        return det * sign
    }

    fun inverse(): Matrix {
        require(rows == cols) { "Inverse only exists for square matrices" }
        val n = rows
        val det = determinant()
        if (abs(det) < 1e-12) throw ArithmeticException("Matrix is singular (det = 0), cannot invert")

        // Gauss-Jordan elimination
        val augmented = Array(n) { r ->
            DoubleArray(2 * n) { c ->
                if (c < n) this[r, c] else if (c - n == r) 1.0 else 0.0
            }
        }

        for (i in 0 until n) {
            var pivot = i
            for (j in i + 1 until n) {
                if (abs(augmented[j][i]) > abs(augmented[pivot][i])) pivot = j
            }
            if (abs(augmented[pivot][i]) < 1e-12) throw ArithmeticException("Matrix is singular")

            val temp = augmented[i]
            augmented[i] = augmented[pivot]
            augmented[pivot] = temp

            val div = augmented[i][i]
            for (c in 0 until 2 * n) {
                augmented[i][c] /= div
            }

            for (r in 0 until n) {
                if (r != i) {
                    val factor = augmented[r][i]
                    for (c in 0 until 2 * n) {
                        augmented[r][c] -= factor * augmented[i][c]
                    }
                }
            }
        }

        return Matrix(n, n, List(n) { r -> List(n) { c -> augmented[r][c + n] } })
    }

    fun rref(): Matrix {
        val n = rows
        val m = cols
        val mat = Array(n) { r -> DoubleArray(m) { c -> this[r, c] } }

        var lead = 0
        for (r in 0 until n) {
            if (lead >= m) break
            var i = r
            while (abs(mat[i][lead]) < 1e-12) {
                i++
                if (i == n) {
                    i = r
                    lead++
                    if (lead == m) break
                }
            }
            if (lead == m) break

            val temp = mat[i]
            mat[i] = mat[r]
            mat[r] = temp

            val lv = mat[r][lead]
            for (c in 0 until m) {
                mat[r][c] /= lv
            }
            for (row in 0 until n) {
                if (row != r) {
                    val factor = mat[row][lead]
                    for (c in 0 until m) {
                        mat[row][c] -= factor * mat[r][c]
                    }
                }
            }
            lead++
        }
        return Matrix(n, m, List(n) { r -> List(m) { c -> if (abs(mat[r][c]) < 1e-10) 0.0 else mat[r][c] } })
    }

    companion object {
        fun identity(n: Int): Matrix =
            Matrix(n, n, List(n) { r -> List(n) { c -> if (r == c) 1.0 else 0.0 } })
        
        fun zeros(r: Int, c: Int): Matrix =
            Matrix(r, c, List(r) { List(c) { 0.0 } })
    }
}

data class Vector3D(val x: Double, val y: Double, val z: Double = 0.0) {
    fun norm(): Double = sqrt(x * x + y * y + z * z)

    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3D): Vector3D = Vector3D(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    fun angleRad(other: Vector3D): Double {
        val d = dot(other)
        val denom = norm() * other.norm()
        if (denom < 1e-14) return 0.0
        val cosTheta = (d / denom).coerceIn(-1.0, 1.0)
        return acos(cosTheta)
    }

    fun angleDeg(other: Vector3D): Double = Math.toDegrees(angleRad(other))

    fun unit(): Vector3D {
        val n = norm()
        if (n < 1e-14) return Vector3D(0.0, 0.0, 0.0)
        return Vector3D(x / n, y / n, z / n)
    }
}
