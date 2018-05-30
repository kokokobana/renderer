package org.bitbucket.wakfuthesaurus.renderer

import scala.scalajs.js.typedarray.Float32Array

object util {
  @inline private[renderer] def setRectTexCoords(
      array: Float32Array,
      left: Float,
      right: Float,
      top: Float,
      bottom: Float
  ): Unit = {
    array(0) = left
    array(1) = bottom
    array(2) = left
    array(3) = top
    array(4) = right
    array(5) = bottom
    array(6) = right
    array(7) = bottom
    array(8) = left
    array(9) = top
    array(10) = right
    array(11) = top
  }

  // inferred from the game
  // (these are allowed hashcodes of base animation names for the given character body parts)

  val CustomHairParts: Set[Int] = Set(
    -434472426, 1006468258, -434472431, 595393602, -1936755333, -1936755365,
    305687247, -1936755358, -212768807, -1891312795, -148124709, -1799293399,
    -434472434, -434472402, 99257258, -434472430, 99257261, 258742502,
    1527482179, -1936755332, 99257260, -1936755336, -212768830, -1386775672,
    -148124708, -434472427, -1936755359, 1018041093, 65368809, -1936755364,
    -1799293398, -1173770306, -212768834, -1936755361, -616966505, -212768806,
    -1936755360, -212768829, 1494681735, -434472401, -434472433, -1936755335,
    -212768805, -212768833, -1799293397, 749411954, -1713624817, 1796303170,
    -1799293401, 1527482178, 63954806, -1799293400, 305687248, -1936755363,
    -434472428, 43285675, -434472400, -1936755331, -1936755334, -148124707,
    -434472432, 1135923850, -434472404, -1386775670, -1936755366, -212768832,
    305687246, 458580894, 1339152237, -1936755362, 76069493, -434472429,
    -1386775671, -1288184498, -45633279, -212768831, -212768804, 99257259,
    -1936755330, 99257262, -434472403
  )

  val CustomBodyParts: Set[Int] = Set(
    1006468258, -2078225375, 892321662, -1891312795, 1749427467, 73723264,
    494389220, -172789396, -1380936348, 1483327197, 1018041093, 1327105750,
    1494681735, -2136674192, 2092787, 1483327198, -156683205, -1380936345,
    63954806, 456610759, 1779636034, -1953599098, -723798377
  )

  // val HeadParts: HashSet[Int] = new HashSet(-1891312795, 1135923850)

  // val HairParts: HashSet[Int] = new HashSet(-1887439342, 75039538)

  // val ShoulderParts: HashSet[Int] = new HashSet(-1380936345, -1380936348)

  // val ChestParts: HashSet[Int] = new HashSet(1749427467, 2320544, -1953599098)

  // val LegParts: HashSet[Int] = new HashSet(-156683205, 1483327197, 1483327198, -1953599098)

  // val BackParts: HashSet[Int] = new HashSet(2092787, -2078225375)
}
