# scalajs-java-logging

[![Build Status](https://travis-ci.org/scala-js/scala-js-java-logging.svg?branch=master)](https://travis-ci.org/scala-js/scala-js-java-logging)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.15.svg)](https://www.scala-js.org/)

`scalajs-java-logging` is a BSD-licensed reimplementation of the `java.logging` API for Scala.js. 
It enables this API in Scala.js projects.

## Usage

Simply add the following line to your sbt settings:

```scala
libraryDependencies += "org.scala-js" %%% "scalajs-java-logging" % "0.1.1"
```

If you have a `crossProject`, the setting must be used only in the JS part:

```scala
lazy val myCross = crossProject.
  ...
  jsSettings.(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-logging" % "0.1.1"
  )
```

## Work in Progress / linking errors

This library is a work in progress.
There are still many classes and methods that have not been implemented yet.
If you use any of those, you will get linking errors.

Feel free to [contribute](./CONTRIBUTING.md) to extend the set of supported
classes and methods!

## License

`scalajs-java-logging` is distributed under the
[BSD 3-Clause license](./LICENSE.txt).

## Contributing

Follow the [contributing guide](./CONTRIBUTING.md).
