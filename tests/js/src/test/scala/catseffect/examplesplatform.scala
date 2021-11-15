/*
 * Copyright 2020-2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package catseffect

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._

import scala.collection.mutable
import scala.scalajs.js

package examples {

import scala.annotation.nowarn

  object JSRunner {
    val apps = mutable.Map.empty[String, IOApp]
    @nowarn("cat=unused")
    def main(paperweight: Array[String]): Unit = {
      val args = js.Dynamic.global.process.argv.asInstanceOf[js.Array[String]].toList
      apps(args.head).main(args.tail.toArray)
    }
  }

  object FatalErrorUnsafeRun extends IOApp {
    def run(args: List[String]): IO[ExitCode] =
      for {
        _ <- (0 until 100).toList.traverse(_ => IO.never.start)
        _ <- IO(throw new OutOfMemoryError("Boom!")).start
        _ <- IO.never[Unit]
      } yield ExitCode.Success
  }

  object Finalizers extends IOApp {
    def writeToFile(string: String, file: String): IO[Unit] =
      IO(js.Dynamic.global.require("fs").writeFileSync(file, string)).void

    def run(args: List[String]): IO[ExitCode] =
      (IO(println("Started")) >> IO.never)
        .onCancel(writeToFile("canceled", args.head))
        .as(ExitCode.Success)
  }

}
