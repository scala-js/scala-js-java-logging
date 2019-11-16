import java.util.logging._

object Main {
  def main(args: Array[String]): Unit = {
    val f = new SimpleFormatter()
    val r = new LogRecord(Level.INFO, "message")
    r.setLoggerName("logger")
    assert(f.format(r).contains("message"))
    assert(f.format(r).contains("logger"))
  }
}