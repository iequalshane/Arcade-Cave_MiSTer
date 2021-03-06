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

package cave.types

import axon.Util
import cave.Config
import chisel3._

/** Represents a tile descriptor. */
class Tile extends Bundle {
  /** Priority */
  val priority = UInt(Config.PRIO_WIDTH.W)
  /** Color code */
  val colorCode = UInt(Config.COLOR_CODE_WIDTH.W)
  /** Tile code */
  val code = UInt(24.W)
}

object Tile {
  /**
   * Decodes a tile from the given data.
   *
   * {{{
   * word   bits                  description
   * -----+-fedc-ba98-7654-3210-+----------------
   *    0 | xx-- ---- ---- ---  | priority
   *      | --xx xxxx ---- ---- | color
   *      | ---- ---- xxxx xxxx | code hi
   *    1 | xxxx xxxx xxxx xxxx | code lo
   * }}}
   *
   * @param data The tile data.
   */
  def decode(data: Bits): Tile = {
    val words = Util.decode(data, 2, 16)
    val tile = Wire(new Tile)
    tile.priority := words(0)(15, 14)
    tile.colorCode := words(0)(13, 8)
    tile.code := words(0)(7, 0) ## words(1)(15, 0)
    tile
  }
}

