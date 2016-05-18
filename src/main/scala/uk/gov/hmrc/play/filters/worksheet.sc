import play.api.libs.iteratee._

val enumerator = Enumerator(0 to 10)

val res = enumerator.run(Iteratee.foreach(_.toString))

val itree = Iteratee.foreach[Int](_.toString)

val step = itree.unflatten.map(_.it)
