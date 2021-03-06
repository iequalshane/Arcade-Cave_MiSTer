/*
 *   __   __     __  __     __         __
 *  /\ "-.\ \   /\ \/\ \   /\ \       /\ \
 *  \ \ \-.  \  \ \ \_\ \  \ \ \____  \ \ \____
 *   \ \_\\"\_\  \ \_____\  \ \_____\  \ \_____\
 *    \/_/ \/_/   \/_____/   \/_____/   \/_____/
 *   ______     ______       __     ______     ______     ______
 *  /\  __ \   /\  == \     /\ \   /\  ___\   /\  ___\   /\__  _\
 *  \ \ \/\ \  \ \  __<    _\_\ \  \ \  __\   \ \ \____  \/_/\ \/
 *   \ \_____\  \ \_____\ /\_____\  \ \_____\  \ \_____\    \ \_\
 *    \/_____/   \/_____/ \/_____/   \/_____/   \/_____/     \/_/
 *
 * https://joshbassett.info
 * https://twitter.com/nullobject
 * https://github.com/nullobject
 *
 * Copyright (c) 2021 Josh Bassett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cave

import chisel3._
import chiseltest._
import org.scalatest._

trait MemSysTestHelpers {
  protected def waitForDownloadReady(dut: MemSys) =
    while (dut.io.download.waitReq.peek().litToBoolean) { dut.clock.step() }
}

class MemSysTest extends FlatSpec with ChiselScalatestTester with Matchers with MemSysTestHelpers {
  it should "write download data to memory" in {
    test(new MemSys) { dut =>
      waitForDownloadReady(dut)
      dut.io.download.cs.poke(true.B)

      // Download & fill
      dut.io.download.wr.poke(true.B)
      dut.io.download.addr.poke(0.U)
      dut.io.download.dout.poke(0x1234.U)
      dut.clock.step()
      dut.io.download.wr.poke(false.B)
      dut.clock.step(2)
      dut.io.ddr.rd.expect(true.B)
      dut.io.ddr.burstLength.expect(1.U)
      dut.io.sdram.rd.expect(true.B)
      dut.io.sdram.burstLength.expect(4.U)

      // DDR valid
      dut.io.ddr.valid.poke(true.B)
      dut.clock.step(2)
      dut.io.ddr.valid.poke(false.B)

      // SDRAM valid
      dut.io.sdram.valid.poke(true.B)
      dut.clock.step(5)
      dut.io.sdram.valid.poke(false.B)

      // Burst done
      dut.io.ddr.burstDone.poke(true.B)
      dut.io.sdram.burstDone.poke(true.B)
      dut.clock.step()
      dut.io.ddr.burstDone.poke(false.B)
      dut.io.sdram.burstDone.poke(false.B)

      // Download & evict
      dut.io.download.wr.poke(true.B)
      dut.io.download.addr.poke(8.U)
      dut.io.download.dout.poke(0x5678.U)
      dut.clock.step()
      dut.io.download.wr.poke(false.B)
      dut.clock.step(2)
      dut.io.ddr.wr.expect(true.B)
      dut.io.ddr.burstLength.expect(1.U)
      dut.io.sdram.wr.expect(true.B)
      dut.io.sdram.burstLength.expect(4.U)
    }
  }
}
