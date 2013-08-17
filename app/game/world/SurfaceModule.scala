package game.world

import game.EventModule

/**
 * A surface is an object with length, slope, and position. A surface
 * can be either a Wall or a Floor.
 *
 * A Wall always has a vertical (undefined) slope, while a Surface
 * always has a Defined slope.
 */
trait SurfaceModule {
  this: RoomModule with EventModule ⇒

  class UndefinedSlopeException extends Exception

  trait Slope {
    def x: Int
    def y: Int
  }

  abstract class Defined( val x: Int, val y: Int ) extends Slope {
    lazy val m = y.toDouble / x.toDouble
    lazy val b = y - ( m * x )
  }

  case class Slant( _x: Int, _y: Int ) extends Defined( _x, _y )

  case object Flat extends Defined( 1, 0 )

  case object Undefined extends Slope {
    val x = 0
    def y = throw new UndefinedSlopeException
  }

  /**
   * A Surface is essentially just a line with a length, position (x,y), and a slope.
   * Surfaces are owned by Room objects, and can supply Adjusts to modify certain Events.
   */
  trait Surface extends AdjustSupplier {
    val xpos: Int
    val ypos: Int
    val length: Int
    val slope: Slope
  }

  trait Floor extends Surface {
    val slope: Defined
    val stopDown: Adjust = {
      case e @ Moved( ar, xpos, ypos, xdir, ydir ) ⇒ e
    }

    def standingOn( x: Int, y: Int ) = {
      slope.m
      false
    }

    adjusts = adjusts :+ stopDown
  }

  case class Wall( val xpos: Int,
                   val ypos: Int,
                   val length: Int ) extends Surface {
    val slope = Undefined
    val ytop = ypos + ( length / 2 )
    val ybottom = ypos - ( length / 2 )
    def inBounds( ypos: Int ) = ypos <= ytop && ypos >= ybottom

    val stopLeft: Adjust = {
      case Moved( ar, xpos, ypos, xdir, ydir ) if xpos == this.xpos + 1 && xdir < 0 && inBounds( ypos ) ⇒
        Moved( ar, xpos, ypos, 0, ydir )
    }
    val stopRight: Adjust = {
      case Moved( ar, xpos, ypos, xdir, ydir ) if xpos == this.xpos - 1 && xdir > 0 && inBounds( ypos ) ⇒
        Moved( ar, xpos, ypos, 0, ydir )
    }
    adjusts = adjusts ::: List( stopLeft, stopRight )
  }

  case class SingleSided( val xpos: Int,
                          val ypos: Int,
                          val length: Int,
                          val slope: Defined ) extends Floor

  case class DoubleSided( val xpos: Int,
                          val ypos: Int,
                          val length: Int,
                          val slope: Defined ) extends Floor

}