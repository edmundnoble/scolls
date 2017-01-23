package scabs

import scabs.seq.{Sequence, TASequence}
import simulacrum.typeclass

object Util {

  type Const[A] = {type l[B] = A}
  type BiConst[A] = {type l[B, C] = A}
  type Id[A] = A
  type ~>[F[_], G[_]] = NatTrans[F, G]
  type ~~>[F[_, _], G[_, _]] = BinatTrans[F, G]
  type <~>[F[_], G[_]] = (F ~> G, G ~> F)
  type Kleisli[F[_], A, B] = A => F[B]
  type KleisliC[F[_]] = {type l[A, B] = A => F[B]}
  type Cokleisli[F[_], A, B] = F[A] => B
  type CokleisliC[F[_]] = {type l[A, B] = F[A] => B}
  type Algebra[F[_], A] = A => F[A]
  type Coalgebra[F[_], A] = F[A] => A

  def FunctionToKleisli[F[_]](pure: Id ~> F): Function1 ~~> KleisliC[F]#l = new (Function1 ~~> KleisliC[F]#l) {
    override def apply[A, B](fa: (A) => B): (A) => F[B] = fa.andThen(pure(_))
  }

  trait Lub1[F[_], G[_]] {
    def ev[A]: F[A] <:< G[A]
  }

  trait NatTrans[F[_], G[_]] {
    def apply[A](fa: F[A]): G[A]
  }

  trait BinatTrans[F[_, _], G[_, _]] {
    def apply[A, B](fa: F[A, B]): G[A, B]
  }

  @typeclass trait Monoid[A] {
    def mempty: A

    def mappend(fst: A, snd: A): A
  }

  @typeclass trait Functor[F[_]] {
    def fmap[A, B](fa: F[A])(f: A => B): F[B]

    def tailRecF[A, B](fa: F[A])(f: A => A Either B): F[B]
  }

  @typeclass trait Applicative[F[_]] {
    def pure[A](a: A): F[A]

    def ap[A, B](fa: F[A])(f: F[A => B]): F[B]

    def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
      ap(fa)(fmap(fb)((b: B) => (a: A) => f(a, b)))

    def fmap[A, B](fa: F[A])(f: A => B): F[B] =
      ap(fa)(pure(f))

    def tuple2[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
      map2(fa, fb)((a, b) => (a, b))

    def traverse[S[_] : Traverse, G[_], A, B](fa: S[A])(f: A => G[B]): G[S[B]]

    def sequence[S[_] : Traverse, G[_], A](fa: S[G[A]]): G[S[A]]
  }

  @typeclass trait Traverse[F[_]] {
    def pure[A](a: A): F[A]

    def ap[A, B](fa: F[A])(f: F[A => B]): F[B]

    def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
      ap(fa)(fmap(fb)((b: B) => (a: A) => f(a, b)))

    def fmap[A, B](fa: F[A])(f: A => B): F[B] =
      ap(fa)(pure(f))

    def traverse[S[_] : Traverse, G[_], A, B](fa: S[A])(f: A => G[B]): G[S[B]]

    def sequence[S[_] : Traverse, G[_], A](fa: S[G[A]]): G[S[A]]
  }

  @typeclass trait Monad[F[_]] {
    def pure[A](a: A): F[A]

    def bind[A, B](fa: F[A])(f: A => F[B]): F[B]

    def fmap[A, B](fa: F[A])(f: A => B): F[B]

    def join[A](ffa: F[F[A]]): F[A]

    def tailRecM[A, B](a: A)(f: A => F[A Either B]): F[B]
  }

  trait Semigroupoid[F[_, _]] {
    def compose[A, B, C](ab: F[A, B], bc: F[B, C]): F[A, C]
  }

  trait Category[F[_, _]] {
    def compose[A, B, C](ab: F[A, B], bc: F[B, C]): F[A, C]

    def id[A]: F[A, A]
  }

  trait Prearrow[F[_, _]] {
    def compose[A, B, C](ab: F[A, B], bc: F[B, C]): F[A, C]

    def id[A]: F[A, A]

    def arr[A, B](f: A => B): F[A, B]
  }

  trait CategoryTailrec[F[_, _]] {
    def compose[A, B, C](ab: F[A, B], bc: F[B, C]): F[A, C]

    def id[A]: F[A, A]

    def tailRecP[S[_] : Sequence, A, B](seq: TASequence[S, F, A, B]): F[A, B]
  }

  trait Arrow[F[_, _]] {
    def compose[A, B, C](ab: F[A, B], bc: F[B, C]): F[A, C]

    def id[A]: F[A, A]

    def arr[A, B](f: A => B): F[A, B]

    def zip[A, B, C, D](fst: F[A, B], snd: F[C, D]): F[(A, C), (B, D)]
  }

  trait ArrowApply[F[_, _]] {
    def compose[A, B, C](ab: F[A, B], bc: F[B, C]): F[A, C]

    def id[A]: F[A, A]

    def arr[A, B](f: A => B): F[A, B]

    def zip[A, B, C, D](fst: F[A, B], snd: F[C, D]): F[(A, C), (B, D)]

    def app[A, B]: F[F[A, B], F[Unit, A => B]]
  }

  trait HFunctor[F[_[_], _]] {
    def transform[G[_], H[_], A](trans: G ~> H)(fga: F[G, A]): F[H, A]
  }

  trait HMonad[F[_[_], _]] {
    def pures[G[_], A](ga: G[A]): F[G, A]

    def flasten[G[_], A](fgfga: F[G, F[G, A]]): F[G, A]
  }

}