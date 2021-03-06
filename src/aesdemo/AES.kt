package aesdemo

import aesdemo.internal.Word
import java.util.*
import kotlin.experimental.xor

class AES {
    companion object {
        private const val BLOCK_LENGHT_BYTE = 16
        private const val Nb = 4 // Number of columns comprising the State

        // Set of constants for AES-128
        private const val Nr = 12   // Number of rounds //12
        private const val Nk = 6    // Number of 32-bit words comprising the Cipher Key //6
        private const val NUMBER_OF_WORDS = Nb * (Nr + 1)
    }

    // S-boxes from https://csrc.nist.gov/csrc/media/publications/fips/197/final/documents/fips-197.pdf
    private val sBox = intArrayOf(
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    )

    private val invSbox = intArrayOf(
            0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
            0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
            0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
            0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
            0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
            0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
            0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
            0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
            0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
            0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
            0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
            0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
            0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
            0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
            0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
            0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
    )

    private val rCon = arrayOf(
            Word(0x00, 0x00, 0x00, 0x00),
            Word(0x01, 0x00, 0x00, 0x00),
            Word(0x02, 0x00, 0x00, 0x00),
            Word(0x04, 0x00, 0x00, 0x00),
            Word(0x08, 0x00, 0x00, 0x00),
            Word(0x10, 0x00, 0x00, 0x00),
            Word(0x20, 0x00, 0x00, 0x00),
            Word(0x40, 0x00, 0x00, 0x00),
            Word(0x80.toByte(), 0x00, 0x00, 0x00),
            Word(0x1b, 0x00, 0x00, 0x00),
            Word(0x36, 0x00, 0x00, 0x00)
    )

    private lateinit var expandedKey: Array<Word>

    fun encrypt(plainText: ByteArray, key: ByteArray): ByteArray {
        expandKey(key)
        var cipher = plainText xor getRoundKey(0)
        println("R[1]_Start = ${cipher.asHexString()}")

        for (i in 1..11) {
            cipher = subBytes(cipher)
            println("R[$i]_S-Box = ${cipher.asHexString()}")

            cipher = shiftRows(cipher)
            println("R[$i]_S-Row = ${cipher.asHexString()}")

            cipher = mixColumns(cipher)
            println("R[$i]_M-Col = ${cipher.asHexString()}")

            println()

            cipher = cipher xor getRoundKey(i)
            println("R[${i+1}]_Start = ${cipher.asHexString()}")
        }

        cipher = subBytes(cipher)
        println("R[12]_S-Box = ${cipher.asHexString()}")

        cipher = shiftRows(cipher)
        println("R[12]_S-Row = ${cipher.asHexString()}")

        cipher = cipher xor getRoundKey(12)
        println("R[13]_Start = ${cipher.asHexString()}")
        println()

        return cipher
    }

    fun decrypt(cipher: ByteArray, key: ByteArray): ByteArray {
        expandKey(key)
        var plainText = cipher xor getRoundKey(12)


        for (i in 11 downTo 1) {
            plainText = invShiftRows(plainText)
            plainText = invSubBytes(plainText)

            plainText = plainText xor getRoundKey(i)

            plainText = invMixColumns(plainText)
        }

        plainText = invShiftRows(plainText)
        plainText = invSubBytes(plainText)

        plainText = plainText xor getRoundKey(0)

        return plainText
    }

    private fun getRoundKey(round: Int) = Arrays.copyOfRange(expandedKey, Nb * round, Nb * (round + 1)).getBytes()

    //region Key expansion
    private fun rotWord(word: Word): Word {
        return Word(
                word.w1,
                word.w2,
                word.w3,
                word.w0
        )
    }

    private fun subWord(word: Word): Word {
        val subWordArray = word.toByteArray().map {
            val i = it.toInt()

            val row = (i shr 4) and 0x000f
            val col = i and 0x000f

            sBox[row * 16 + col].toByte()
        }.toByteArray()


        return Word.fromByteArray(subWordArray)
    }

    private fun expandKey(key: ByteArray) {
        val w = Array(NUMBER_OF_WORDS) { Word.empty() }
        var temp: Word

        //find w0 => w3
        for (i in 0 until Nk) {
            val baseIndex = i * 4

            w[i] = Word(
                    key[baseIndex],
                    key[baseIndex + 1],
                    key[baseIndex + 2],
                    key[baseIndex + 3]
            )

         //   println("w$i = ${w[i].toString()}")
        }

        for (i in Nk until NUMBER_OF_WORDS) {
            temp = w[i - 1]

            if (i % Nk == 0) {
                temp = subWord(rotWord(temp)) xor rCon[i / Nk]
            } else if (Nk > 6 && i % Nk == 4) {
                temp = subWord(temp)

            }

            w[i] = w[i - Nk] xor temp
        }

        expandedKey = w
    }
    //endregion

    //region Encryption
    private fun subBytes(input: ByteArray): ByteArray {
        return input.map {
            val i = it.toInt()

            val row = (i shr 4) and 0x000f
            val col = i and 0x000f

            sBox[row * 16 + col].toByte()
        }.toByteArray()
    }

    private fun shiftRows(input: ByteArray): ByteArray {
        var shiftedRows = ByteArray(BLOCK_LENGHT_BYTE)
        var tempShiftedRows = ByteArray(0)
        val temp = ByteArray(Nb)

        for (i in 0 until Nb) {
            for (j in 0 until Nb) {
                temp[j] = input[Nb * j + i]
            }

            val dst = ByteArray(Nb)

            System.arraycopy(temp, i, dst, 0, Nb - i)
            System.arraycopy(temp, 0, dst, Nb - i, i)

            tempShiftedRows += dst
        }

        for (i in 0 until Nb) {
            for (j in 0 until Nb) {
                shiftedRows[Nb * i + j] = tempShiftedRows[Nb * j + i]
            }
        }

        return shiftedRows
    }

    private fun mixColumns(input: ByteArray): ByteArray {
        val out = ByteArray(BLOCK_LENGHT_BYTE)

        for (i in 0 until Nb) {
            val baseIndex = i * Nb

            out[baseIndex] =
                    (input[baseIndex] gmul 0x02) xor
                    (input[baseIndex + 1] gmul 0x03) xor
                    input[baseIndex + 2] xor
                    input[baseIndex + 3]

            out[baseIndex + 1] =
                    input[baseIndex] xor
                    (input[baseIndex + 1] gmul 0x02) xor
                    (input[baseIndex + 2] gmul 0x03) xor
                    input[baseIndex + 3]


            out[baseIndex + 2] =
                    input[baseIndex] xor
                    input[baseIndex + 1] xor
                    (input[baseIndex + 2] gmul 0x02) xor
                    (input[baseIndex + 3] gmul 0x03)

            out[baseIndex + 3] =
                    (input[baseIndex] gmul 0x03) xor
                    input[baseIndex + 1] xor
                    input[baseIndex + 2] xor
                    (input[baseIndex + 3] gmul 0x02)
        }

        return out
    }
    //endregion Ecnryption

    //region Decryption
    private fun invSubBytes(input: ByteArray): ByteArray {
        return input.map {
            val i = it.toInt()

            val row = (i shr 4) and 0x000f
            val col = i and 0x000f

            invSbox[row * 16 + col].toByte()
        }.toByteArray()
    }

    private fun invShiftRows(input: ByteArray): ByteArray {
        var shiftedRows = ByteArray(BLOCK_LENGHT_BYTE)
        var tempShiftedRows = ByteArray(0)
        val temp = ByteArray(Nb)

        for (i in 0 until Nb) {
            for (j in 0 until Nb) {
                temp[j] = input[Nb * j + i]
            }

            val dst = ByteArray(Nb)

            System.arraycopy(temp, 0, dst, i, Nb - i)
            System.arraycopy(temp, Nb - i, dst, 0, i)

            tempShiftedRows += dst
        }

        for (i in 0 until Nb) {
            for (j in 0 until Nb) {
                shiftedRows[Nb * i + j] = tempShiftedRows[Nb * j + i]
            }
        }

        return shiftedRows
    }

    private fun invMixColumns(input: ByteArray): ByteArray {
        val out = ByteArray(BLOCK_LENGHT_BYTE)

        for (i in 0 until Nb) {
            val baseIndex = i * Nb

            out[baseIndex] =
                    (input[baseIndex] gmul 0x0e) xor
                    (input[baseIndex + 1] gmul 0x0b) xor
                    (input[baseIndex + 2] gmul 0x0d) xor
                    (input[baseIndex + 3] gmul 0x09)

            out[baseIndex + 1] =
                    (input[baseIndex] gmul 0x09) xor
                    (input[baseIndex + 1] gmul 0x0e) xor
                    (input[baseIndex + 2] gmul 0x0b) xor
                    (input[baseIndex + 3] gmul 0x0d)


            out[baseIndex + 2] =
                    (input[baseIndex] gmul 0x0d) xor
                    (input[baseIndex + 1] gmul 0x09) xor
                    (input[baseIndex + 2] gmul 0x0e) xor
                    (input[baseIndex + 3] gmul 0x0b)

            out[baseIndex + 3] =
                    (input[baseIndex] gmul 0x0b) xor
                    (input[baseIndex + 1] gmul 0x0d) xor
                    (input[baseIndex + 2] gmul 0x09) xor
                    (input[baseIndex + 3] gmul 0x0e)
        }

        return out
    }
    //endregion
}