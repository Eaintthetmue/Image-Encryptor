fun main(args: Array<String>) {

    val secretKey: String =
        "662ede816988e58fb6d057d9d85605e0"

    val messages = "Hello World"

    println("Secret Key = $secretKey")
    println("String To Encrypt = $messages")
    println()

    var encryptor: AESEncryptor = AESEncryptor()



    val encryptedValue: String? =encryptor.encrypt(messages, secretKey)
    println("Encrypted Value = $encryptedValue")
    println()

    val decryptedValue: String? =encryptor.decryptWithAES(secretKey, encryptedValue)
    println("Decrypted Value = $decryptedValue")
}