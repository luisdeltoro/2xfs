[![Build Status](https://travis-ci.com/luisdeltoro/2xfs.svg?branch=master)](https://travis-ci.com/luisdeltoro/2xfs)
[![codecov](https://codecov.io/gh/luisdeltoro/2xfs/branch/master/graph/badge.svg)](https://codecov.io/gh/luisdeltoro/2xfs)

# Fluent Streaming (Framework) For Sequences (2xfs)

A streaming IO micro library for Kotlin Sequences, that allows to fluently build streams from and/or to files

2xfs is inspired by the API of the Scala library [fs2](https://fs2.io) 

2xfs allows to work with files in a streaming manner making use of Kotlin sequences. The streaming nature of the framework allows to work with potentially big files, without the need to load the whole contents in memory.

# Getting started

Here an example of how to read a file containing quantities in kilos from disk, covert them to pounds and write back to another file on disk, all in a streaming manner.

```kotlin
import org.fsfs.core.*
import java.text.DecimalFormat

io.linesFromResource("/tmp/quantities-in-metric-system.txt")
    .filter { s -> !s.trim().isEmpty() && !s.startsWith("#") }
    .map { DecimalFormat("#.#").format(it.toDouble() * 2.20462) }
    .log("test1")
    .intersperse("\n")
    .through(text.utf8Encode())
    .toFile("/tmp/quantities-in-imperial-system.txt") // terminal operation (consumes the sequence)
```
