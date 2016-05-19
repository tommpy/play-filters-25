case class Container[+T](t: T){

	def apply(i: Int) : T = t
}


val c = Container("Hello")

c(0)
