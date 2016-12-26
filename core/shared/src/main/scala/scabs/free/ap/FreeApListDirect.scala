package scabs.free.ap

import scabs.Util.{Applicative, ~>}

trait FreeApListDirect[F[_], A] {
  type U

  def ua: U => A

  def apList: ApList[F, U]
}

object FreeApListDirect {
  def lift[F[_], A](fa: F[A]): FreeApListDirect[F, A] = new FreeApListDirect[F, A] {
    override type U = (A, Unit)
    override def ua: U => A = {
      case (a, _) => a
    }
    override def apList: ApList[F, (A, Unit)] = Cons(fa, Nil[F]())
  }

  def retract[F[_], A](fa: FreeApListDirect[F, A])(implicit F: Applicative[F]): F[A] = {
    F.fmap(fa.apList.reduce)(fa.ua)
  }

  def foldMap[F[_], G[_], A](fa: FreeApListDirect[F, A], trans: F ~> G)(implicit G: Applicative[G]): G[A] = {
    G.fmap(fa.apList.reduceMap(trans))(fa.ua)
  }

}
