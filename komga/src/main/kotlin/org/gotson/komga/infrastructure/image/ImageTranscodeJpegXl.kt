package org.gotson.komga.infrastructure.image

class ImageTranscodeJpegXl {
  fun transcode(file: ByteArray): ByteArray {
    val djxl = ProcessBuilder("djxl", "--output_format", "jpeg", "-", "-").start()
    djxl.outputStream.write(file)
    djxl.outputStream.close()
    val jpeg = djxl.inputStream.readAllBytes()
    return jpeg
  }
}
