package scalaz

import Scalaz._

sealed trait LazyOption[+A] {
  self =>
  def fold[B](ifSome: A => B = identity[A] _, ifNone: => B = LazyOption.none[B]): B

  def isDefined: Boolean

  def isEmpty: Boolean = !isDefined

  def getOrElse[B >: A](default: => B): B = fold[B](ifNone = default)

  def get: A = getOrElse(throw new NoSuchElementException())

  def orNull[A1 >: A](implicit ev: Null <:< A1): A1 = this getOrElse null

  def map[B](f: A => B): LazyOption[B] = fold(a => LazyOption.some(f(a)))

  def flatMap[B](f: A => LazyOption[B]): LazyOption[B] = fold(f)

  def filter(f: (A => Boolean)): LazyOption[A] = new LazyOption[A] {
    lazy val isDefined = self.fold(f, false)
    def fold[B](ifSome: (A) => B, ifNone: => B) = if (isDefined) self.fold(ifSome, ifNone) else ifNone
  }

  def foreach[U](f: A => U) {
    fold(f)
  }

  def withFilter(p: A => Boolean): WithFilter = new WithFilter(p)

  def exists(p: A => Boolean): Boolean = fold(p, false)

  def orElse[B >: A](alternative: => LazyOption[B]): LazyOption[B] =
    if (isEmpty) alternative else this

  def toRight[X](left: => X) = if (isEmpty) Left(left) else Right(get)

  def toLeft[X](right: => X) = if (isEmpty) Right(right) else Left(get)

  def fst[AA >: A]: FirstLazyOption[AA] = this

  def lst[AA >: A]: LastLazyOption[AA] = this

  class WithFilter(p: A => Boolean) {
    def map[B](f: A => B): LazyOption[B] = self filter p map f

    def flatMap[B](f: A => LazyOption[B]): LazyOption[B] = self filter p flatMap f

    def foreach[U](f: A => U) {
      self filter p foreach f
    }

    def withFilter(q: A => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
  }
}

object LazyOption {
  def some[A](a: => A): LazyOption[A] = new Some(a)

  def none[A]: LazyOption[A] = None

  private class Some[+A](a: => A) extends LazyOption[A] {
    lazy val aa = a

    def fold[B](ifSome: (A) => B, ifNone: => B) = ifSome(aa)

    def isDefined = true
  }

  private object None extends LazyOption[Nothing] {
    def fold[B](ifSome: (Nothing) => B, ifNone: => B) = ifNone

    def isDefined = false
  }

}

