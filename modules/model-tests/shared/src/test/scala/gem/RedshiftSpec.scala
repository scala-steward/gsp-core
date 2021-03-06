// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import cats.tests.CatsSuite
import cats.kernel.laws.discipline._

import coulomb._
import coulomb.si._
import coulomb.siprefix._
import java.math.MathContext
import gem.arb._

final class RedshiftSpec extends CatsSuite {
  import ArbRedshift._

  // Laws
  checkAll("Redshift", EqTests[Redshift].eqv)
  checkAll("RedshiftOrder", OrderTests[Redshift].order)

  test("toRadialVelocity") {
    assert(Redshift.Zero.toRadialVelocity === RadialVelocity(0.withUnit[Meter %/ Second]))
    assert(
      // Example from http://spiff.rit.edu/classes/phys240/lectures/expand/expand.html
      // We need to specify the Math context to properly compare
      Redshift(BigDecimal.decimal(5.82, MathContext.DECIMAL64)).toRadialVelocity === RadialVelocity(
        BigDecimal
          .decimal(287172.9120288430, MathContext.DECIMAL64)
          .withUnit[(Kilo %* Meter) %/ Second]
      )
    )
  }

  test("toApparentRadialVelocity") {
    assert(
      Redshift.Zero.toApparentRadialVelocity === ApparentRadialVelocity(0.withUnit[Meter %/ Second])
    )
    assert(Redshift(1).toApparentRadialVelocity === ApparentRadialVelocity(RadialVelocity.C))
    assert(
      // In apparent radial velocity we can go faster than C
      Redshift(
        BigDecimal.decimal(5.82, MathContext.DECIMAL64)
      ).toApparentRadialVelocity === ApparentRadialVelocity(
        BigDecimal
          .decimal(1744792.10556, MathContext.DECIMAL64)
          .withUnit[(Kilo %* Meter) %/ Second]
      )
    )
  }
}
