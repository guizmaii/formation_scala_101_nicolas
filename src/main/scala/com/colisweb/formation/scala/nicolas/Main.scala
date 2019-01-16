package com.colisweb.formation.scala.nicolas

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, blocking}

class Toto {
  val a = 1
}

object Main extends App {

  // evaluation strategies

  val i: Int = 1
  lazy val j: Int = 1
  def f: Int = 1

  def f(i: Int) = 1
  def lazyf(i: => Int) = 1

  f(1 / 0) // crash
  lazyf(1 / 0) // crash pas

  // immutable vs mutable

  val a = "a" // pointeur immutable
  var b = "b" // pointeur mutable => Tu n'as pas le droit d'utiliser les `var`.

  b = "c"
  //a = "c" do not work

  var iml: List[String] = List.empty
  val imll = iml :+ "string"

  val ml
    : mutable.ListBuffer[String] = mutable.ListBuffer.empty // optimization only

  // scope & implicits

  object Scope0 {
    def ff()(implicit string: String) = string
    implicit val aa = "c"

    {
      ff()
    }
  }

  object Scope1 {
    import Scope0.aa

    def g(indice: Int)(implicit aa: String) = aa

    g(1)
  }

  // currify
  def cur0(a: Int, b: Int): Int = a + b
  def cur1(a: Int)(b: Int): Int = a + b

  val res0 = cur0(1, 2)
  val res1 = cur1(1)(2)

  val add1: Int => Int = cur1(1) _
  val res11 = add1(2)

  def solve(data: Any)(indice: Int): Any = ???

  val withDataSolve0: Int => Any = solve("data") _
  def withDataSolve1(indice: Int): Any = solve("data")(indice)

  val result0 = withDataSolve0(0)
  val result2 = withDataSolve0(2)
  val result3 = withDataSolve0(3)
  val result4 = withDataSolve1(4)
  val result5 = withDataSolve1(5)

  def strategy(data: Int)(f: (Int, Int) => Int): Any = ???

  strategy(1)((a, b) => a + b)
  strategy(1)(_ + _) // sucre syntaxique

  def ffff(a: Int, b: Int): Int = a + b

  strategy(1)(ffff)

  List(1, 2, 3).map(a => a.toString)
  List(1, 2, 3).map(_.toString) // sucre syntaxique

  // Implicits
  trait Encoder[A] {
    def toJson(a: A): String
  }

  object Encoder {
    implicit val a: Encoder[Klass] = ???
    implicit val b: Encoder[Person] = ???
  }

  case class Person(name: String, age: Int)

  object DB {
    def getPerson(index: Int): Person = ???
  }

  object Implicits {
    object ImplicitVal {
      implicit val encoder: Encoder[Person] =
        new Encoder[Person] {
          override def toJson(a: Person): String = ???
        }

      //implicit def encoder[A]: Encoder[A] = new Encoder[A] {
      //  override def toJson(a: A): String = ???
      //}

      def get[A](index: Int)(implicit encoder: Encoder[A]): /*Json*/ String =
        ??? //encoder.toJson(DB.getPerson(i))

      implicit val ec = ExecutionContext.global

      def future: Future[String] = Future("String")

      def blockingWork = ???

      Future("string")
      Future { "String" }

      Future {
        blocking {
          blockingWork
        }
      }
    }

    /**
      * NE JAMAIS UTILISER d'`implicit def`
      */
    object ImplicitDef {
      implicit def conversion(double: Double): Int = double.toInt

      val i: Int = 12.1
    }

    object ImplicitClass {
      class MyType {
        def f: Int = ???
        def g: Int = ???
      }

      (new MyType).f
      (new MyType).g

      implicit class StringOps(string: String) {
        def taille: Int = string.length
      }

      "my string".taille

      // Java
      object StringUtils {
        def taille(s: String) = s.length
      }

      StringUtils.taille("my string")

      // Pour remplacer les implicit def
      implicit class DoubleOps(d: Double) {
        def toEntier: Int = d.toInt
      }

      12.1.toEntier
    }

  }

}

// Class & companion objects

class Klass(a: String, b: String) {

  import Klass._

  val c = a + b + constante

}

object Klass {
  def apply(a: String, b: String): Klass = new Klass(a, b)
  def apply(a: String): Klass = new Klass(a, "toot")

  val constante = 12

  import Main.Encoder

  implicit lazy val encoder: Encoder[Klass] = ???
}

object Context extends App {
  import Main.Encoder

  Klass.constante

  val k = Klass("a", "b")
  val kk = Klass("a")

  println(k)

  def f()(implicit c: Encoder[Klass]) = ???

  k.c
}

// case class
final case class Person(name: String, age: Int)
final case class LivingPerson(name: String, age: Int, friend: Person) {
  val c = 1

  def f(i: Int) = ???
}

object Examples extends App {

  val jules = Person("Jules", 30)

  println(jules)

}

// sealed
sealed trait Fruit
final case class Pomme(name: String) extends Fruit
final case object Cerise extends Fruit
final case object Poire extends Fruit

sealed trait Error
final case object PasFaim extends Error
final case object DejaMortDeFaim extends Error
final case object AllergiqueAuxCerises extends Error

object Examples2 {

  // type Fruit = Pomme | Cerise | Poire

  def mange(f: Fruit): Either[Error, Unit] =
    if (f == Cerise) Left(AllergiqueAuxCerises) else Right(())

  val grany = Pomme("grany")
  val canadian = Pomme("canadian")

  mange(grany) match {
    case Left(_: AllergiqueAuxCerises.type) => println("Je suis allergique aux cerises")
    case Left(error)                        => println(error)
    case Right(_)                           => mange(canadian)
  }

  mange(grany).flatMap(_ => mange(canadian))

  case object DivideByZeroError

  def lyingDivide(a: Int, b: Int): Int = a / b

  def optionDivide(a: Int, b: Int): Option[Int] = if (b == 0) None else Some(a / b)
  def eitherDivice(a: Int, b: Int): Either[DivideByZeroError.type, Int] =
    if (b == 0) Left(DivideByZeroError) else Right(a / b)

  optionDivide(1, 0) match {
    case Some(value)   => println(s"Got a value: $value")
    case None          =>  println(s"No value")
  }

  optionDivide(1, 0).map(value => println(s"Got a value: $value"))

  List(1, 2, 3) match {
    case Nil => println("List vide")
    case x :: Nil => println("List à 1 element")
    case x :: y :: Nil => println("List à 2 element")
    case x :: y :: _ :: Nil => println("List à 3 element")
    case x :: xs => println("List à au moins 1 element")
  }

  List(1, 2, 3).map(_ * 2)

  implicit val ec = ExecutionContext.global

  Future("future value")
    .map(_ + "toto")
    .map(result2 => (result2, "tata"))
    .map { case (a, b) => a + b }


  Future("get api key")
    .flatMap(apiKey => Future("get Person with API key"))
    .recover {
      case ArithmeticException => println("toto")
      case NullPointerException => println("NPE")
      case error => println(s"Exception non géré: $error")
    }

  Future("get api key")
    .flatMap(apiKey => Future("get Person with API key"))
    .recoverWith {
      case ArithmeticException => Future.successful(println("toto"))
      case NullPointerException => Future.successful(println("NPE"))
      case error => println(s"Exception non géré: $error"); Future.failed(error)
    }


  // foldLeft

  var res = 0
  val l = List(1, 2, 4)
  while (l.nonEmpty) {
    res = res + l.head
  }

  List(1, 2, 4).foldLeft(0)((acc, elem) => acc + elem)

}
