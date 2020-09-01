package com.innerproduct.ee.apps

import cats.effect._
import java.io.RandomAccessFile

class FileBufferReader private (in: RandomAccessFile) { // <1>
  def readBuffer(offset: Long): IO[(Array[Byte], Int)] = // <2>
    IO {
      in.seek(offset)

      val buf = new Array[Byte](FileBufferReader.bufferSize)
      val len = in.read(buf)

      (buf, len)
    }

  private def close: IO[Unit] = IO(in.close()) // <3>
}

object FileBufferReader {
  val bufferSize = 4096

  def makeResource(
      fileName: String,
      closeErrorHandler: Throwable => Unit
  ): Resource[IO, FileBufferReader] = // <4>
    Resource.make {
      IO(new FileBufferReader(new RandomAccessFile(fileName, "r")))
    } { res =>
      res.close.handleErrorWith(e => IO(closeErrorHandler(e)))
    }
}
