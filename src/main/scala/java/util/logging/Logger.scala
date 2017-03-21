package java.util.logging

import scala.annotation.tailrec
import scala.collection.mutable

object Logger {

  final val GLOBAL_LOGGER_NAME = "global"
  private final val ROOT_LOGGER_NAME = ""

  // Not implemented, deprecated on JDK 1.8
  //val global: Logger

  private val defaultLogLevel: Level = Level.INFO

  private val loggers: mutable.Map[String, Logger] = mutable.Map.empty

  /** Find parent logger */
  @tailrec
  private def findParentLoggerOf(name: String): Logger = {
    name match {
      case null => // Anonymous logger
        rootLogger
      case ROOT_LOGGER_NAME =>
        null
      case _ =>
        val parentNodes = name.split("\\.").toList.dropRight(1)
        val parentName = name.substring(0, name.lastIndexOf('.').max(0))
        loggers.get(parentName) match {
          case Some(l) => l
          case None    => findParentLoggerOf(parentName)
        }
    }
  }

  private def newLogger(name: String): Logger = {
    val logger = new Logger(name, null)
    logger.setLevel(null)
    logger.setUseParentHandlers(true)
    logger.setParent(findParentLoggerOf(name))
    if (name != null) {
      updateChildLoggerParent(logger)
    }
    logger
  }

  private def updateChildLoggerParent(newParent: Logger): Unit ={
    val prefix = s"${newParent.getName}."
    for ((name, childLogger) <- loggers if name.startsWith(prefix)) {
      val currentParent = childLogger.getParent
      // For example, when a new parent is a.b:
      // - if child a.b.c.d (parent = null) => needs to be a.b.c.d (parent = a.b)
      // - if child a.b.c.e (parent = a.b.c) => no update is required.
      if (currentParent == null || !currentParent.getName.startsWith(prefix)) {
        childLogger.setParent(newParent)
      }
    }
  }

  // Root is not visible to the outside but gives defaults
  private[this] val rootLogger: Logger = {
    val root = getLogger(ROOT_LOGGER_NAME)
    root.setLevel(defaultLogLevel)
    root.setUseParentHandlers(false)
    root
  }

  private[this] val globalLogger: Logger = getLogger(GLOBAL_LOGGER_NAME)

  def getGlobal(): Logger = globalLogger

  def getLogger(name: String): Logger = {
    if (name == null)
      throw new NullPointerException("Logger name cannot be null")

    loggers.getOrElseUpdate(name, newLogger(name))
  }

  // Not implemented, no resource bundle in scala.js
  //def getLogger(name: String, resourceBundle: String): Logger

  def getAnonymousLogger(): Logger = {
    // No references to anonymous loggers are kept
    newLogger(null)
  }

  // Not implemented, no resource bundle in scala.js
  //def getAnonymousLogger(resourceBundle: String):Logger
}

class Logger(name: String, resourceBundle: String) {

  private[this] var level: Level = null
  private[this] var useParentsHandlers: Boolean = false
  private[this] var parent: Logger = null
  private[this] var filter: Filter = null
  private[this] var handlers: Array[Handler] = Array.empty

  // Find the effective level
  private def levelR: Level = {
    @tailrec
    def go(logger: Logger): Level = {
      if (logger.getLevel != null) logger.getLevel
      else if (logger.getParent == null) null
      else go(logger.getParent)
    }

    go(this)
  }

  // Not implemented, no resource bundle
  //def getResourceBundle():ResourceBundle = ???

  def getResourceBundleName(): String = resourceBundle

  def setFilter(filter: Filter): Unit = this.filter = filter

  def getFilter(): Filter = filter

  def log(record: LogRecord): Unit = {
    if (isLoggable(record.getLevel)) {
      if (useParentsHandlers) {
        val parent = getParent()
        if (parent != null)
          parent.log(record)
      }
      handlers.foreach(_.publish(record))
    }
  }

  def log(level: Level, msg: String): Unit = {
    val r = new LogRecord(level, msg)
    r.setLoggerName(name)
    log(r)
  }

  def log(level: Level, msg: String, param: AnyRef): Unit = {
    val r = new LogRecord(level, msg)
    r.setParameters(Array(param))
    r.setLoggerName(name)
    log(r)
  }

  def log(level: Level, msg: String, params: Array[AnyRef]): Unit = {
    val r = new LogRecord(level, msg)
    r.setParameters(params)
    r.setLoggerName(name)
    log(r)
  }

  def log(level: Level, msg: String, thrown: Throwable): Unit = {
    val r = new LogRecord(level, msg)
    r.setThrown(thrown)
    r.setLoggerName(name)
    log(r)
  }

  def logp(level: Level, sourceClass: String, sourceMethod: String, msg: String): Unit = {
    val r = new LogRecord(level, msg)
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    log(r)
  }

  def logp(level: Level, sourceClass: String, sourceMethod: String,
      msg: String, param: AnyRef): Unit = {
    val r = new LogRecord(level, msg)
    r.setParameters(Array(param))
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    log(r)
  }

  def logp(level: Level, sourceClass: String, sourceMethod: String,
      msg: String, params: Array[AnyRef]): Unit = {
    val r = new LogRecord(level, msg)
    r.setParameters(params)
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    log(r)
  }

  def logp(level: Level, sourceClass: String, sourceMethod: String,
      msg: String, thrown: Throwable): Unit = {
    val r = new LogRecord(level, msg)
    r.setThrown(thrown)
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    log(r)
  }

  // Not implemented, no resource bundle
  //def logrb(level: Level, sourceClass: String, sourceMethod: String,
  //    bundleName: String, msg: String): Unit = ???

  // Not implemented, no resource bundle
  //def logrb(level: Level, sourceClass: String, sourceMethod: String,
  //    bundleName: String, msg: String, param: AnyRef): Unit = ???

  // Not implemented, no resource bundle
  //def logrb(level: Level, sourceClass: String, sourceMethod: String,
  //    bundleName: String, msg: String, params: Array[AnyRef]): Unit = ???

  // Not implemented, no resource bundle
  //def logrb(level: Level, sourceClass: String, sourceMethod: String,
  //    bundleName: String, msg: String, thrown: Throwable): Unit = ???

  def entering(sourceClass: String, sourceMethod: String): Unit = {
    val r = new LogRecord(Level.FINER, "ENTRY")
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    log(r)
  }

  def entering(sourceClass: String, sourceMethod: String, param: AnyRef): Unit = {
    val r = new LogRecord(Level.FINER, "ENTRY {0}")
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setParameters(Array(param))
    r.setLoggerName(name)
    log(r)
  }

  private def paramsString(i: Int): String =
    (0 until i).map(i => s"{$i}").mkString(" ")

  def entering(sourceClass: String, sourceMethod: String, params: Array[AnyRef]): Unit = {
    val r = new LogRecord(Level.FINER, s"ENTRY ${paramsString(params.length)}")
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    r.setParameters(params)
    log(r)
  }

  def exiting(sourceClass: String, sourceMethod: String): Unit = {
    val r = new LogRecord(Level.FINER, "RETURN")
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setLoggerName(name)
    log(r)
  }

  def exiting(sourceClass: String, sourceMethod: String, result: AnyRef): Unit = {
    val r = new LogRecord(Level.FINER, "RETURN {0}")
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setParameters(Array(result))
    r.setLoggerName(name)
    log(r)
  }

  def throwing(sourceClass: String, sourceMethod: String, thrown: Throwable): Unit = {
    val r = new LogRecord(Level.FINER, "THROW")
    r.setSourceClassName(sourceClass)
    r.setSourceMethodName(sourceMethod)
    r.setThrown(thrown)
    r.setLoggerName(name)
    log(r)
  }

  def severe(msg: String): Unit = {
    val r = new LogRecord(Level.SEVERE, msg)
    r.setLoggerName(name)
    log(r)
  }

  def warning(msg: String): Unit = {
    val r = new LogRecord(Level.WARNING, msg)
    r.setLoggerName(name)
    log(r)
  }

  def info(msg: String): Unit = {
    val r = new LogRecord(Level.INFO, msg)
    r.setLoggerName(name)
    log(r)
  }

  def config(msg: String): Unit = {
    val r = new LogRecord(Level.CONFIG, msg)
    r.setLoggerName(name)
    log(r)
  }

  def fine(msg: String): Unit = {
    val r = new LogRecord(Level.FINE, msg)
    r.setLoggerName(name)
    log(r)
  }

  def finer(msg: String): Unit = {
    val r = new LogRecord(Level.FINER, msg)
    r.setLoggerName(name)
    log(r)
  }

  def finest(msg: String): Unit = {
    val r = new LogRecord(Level.FINEST, msg)
    r.setLoggerName(name)
    log(r)
  }

  def setLevel(newLevel: Level): Unit = this.level = newLevel

  def getLevel(): Level = level

  def getName(): String = name

  def isLoggable(level: Level): Boolean = {
    val effectiveLevel = levelR
    effectiveLevel == null || effectiveLevel.intValue() <= level.intValue()
  }

  def addHandler(handler: Handler): Unit = handlers = handlers :+ handler

  def removeHandler(handler: Handler): Unit =
    handlers = handlers.filterNot(_ == handler)

  def getHandlers(): Array[Handler] = handlers

  def setUseParentHandlers(useParentHandlers: Boolean): Unit =
    this.useParentsHandlers = useParentHandlers

  def getUseParentHandlers(): Boolean = useParentsHandlers

  def getParent(): Logger = parent

  def setParent(parent: Logger): Unit = this.parent = parent
}
