package aesdemo

fun main(args: Array<String>) {

    val aes = AES()
    /*val plainText = byteArrayOf(
            0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte(), 0x99.toByte(), 0xaa.toByte(), 0xbb.toByte(), 0xcc.toByte(), 0xdd.toByte(), 0xee.toByte(), 0xff.toByte()

    )

    //need to change 192 bits
    val key = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14,0x15, 0x16, 0x17
    )*/

    val plainText = "OneNineNineSevenef"
    val plainTextByteArray = plainText.toByteArray()


    println("Message Hex Origin >> ${plainTextByteArray.asHexString()}")

    val key ="InformationTechnologyHTU"
    val keyByteArray = key.toByteArray()
    println("Key Hex >> ${keyByteArray.asHexString()}")
    println()

    /*val plainText = byteArrayOf(
            0x4f, 0x6e, 0x65, 0x4e, 0x69, 0x6e, 0x65, 0x4e, 0x69, 0x6e, 0x65, 0x53, 0x65, 0x76, 0x65, 0x6e
    )*/

    //need to change 192 bits
    /*val key = byteArrayOf(
            0x49, 0x6e, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x54, 0x65, 0x63, 0x68, 0x6e, 0x6f, 0x6c, 0x6e, 0x6f, 0x79, 0x48, 0x54, 0x55
    )*/

   // println("Plain text: ${plainText.asHexString()}")
   // println("Key: ${key.asHexString()}\n")

   // val requireLength = 16 - plainText.size%16

    val requireLength = 16 - (plainTextByteArray.size%16)
    val newArray = ByteArray(requireLength)
    val destination = ByteArray(plainTextByteArray.size+requireLength)
    System.arraycopy(plainTextByteArray, 0, destination, 0, plainTextByteArray.size)

    if (requireLength != 16){
        for (i in 0..(requireLength-1))
            newArray[i] = 0x00
    }

    System.arraycopy(newArray, 0, destination, plainTextByteArray.size, newArray.size)

    println("Message Hex Casted >> ${destination.asHexString()}")

    val loop = destination.size/16

    val cipher = ByteArray(destination.size)
    val input = ByteArray(16)
    for (i in 0..(loop-1)){
        System.arraycopy(destination, i*16, input, 0, 16)
         val cipherByte = aes.encrypt(input, keyByteArray)
        System.arraycopy(cipherByte, 0, cipher, i*16, 16)
    }

 //   val cipher = aes.encrypt(plainTextByteArray, keyByteArray)

    val decryptByte = ByteArray(cipher.size)
    val decryptInput = ByteArray(16)
    for (i in 0..(loop - 1)){
           System.arraycopy(cipher, i*16, decryptInput, 0, 16)
            val decryptedByte = aes.decrypt(decryptInput, keyByteArray)
            System.arraycopy(decryptedByte, 0, decryptByte, i*16, 16)
    }

    println("Cipher: ${cipher.asHexString()}")
    println()
    println("Decrypted text: ${decryptByte.asHexString()}")
}