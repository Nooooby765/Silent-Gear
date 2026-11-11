/*
 * SilentLib - WorldGeneratorSL
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.lib.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public abstract class WorldGeneratorSL implements IWorldGenerator {

  public final boolean allowRetrogen;
  public final String retrogenKey;
  public final int retrogenVersion;

  protected boolean printDebugInfo = false;

  public WorldGeneratorSL(boolean allowRetrogren, String retrogenKey) {

    this(allowRetrogren, retrogenKey, 1);
  }

  public WorldGeneratorSL(boolean allowRetrogren, String retrogenKey, int retrogenVersion) {

    this.allowRetrogen = allowRetrogren;
    this.retrogenKey = retrogenKey;
    this.retrogenVersion = retrogenVersion > 0 ? retrogenVersion : 1;

    if (allowRetrogren)
      MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public void generate(Random random, int chunkX, int chunkZ, World world,
      IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

    final int dim = world.provider.getDimension();
    final int posX = chunkX * 16;
    final int posZ = chunkZ * 16;

    if (!generateForDimension(dim, world, random, posX, posZ)) {
      switch (dim) {
        case 0:
          generateSurface(world, random, posX, posZ);
          break;
        case -1:
          generateNether(world, random, posX, posZ);
          break;
        case 1:
          generateEnd(world, random, posX, posZ);
          break;
        default:
          generateSurface(world, random, posX, posZ);
      }
    }
  }

  protected void generateSurface(World world, Random random, int posX, int posZ) {

  }

  protected void generateNether(World world, Random random, int posX, int posZ) {

  }

  protected void generateEnd(World world, Random random, int posX, int posZ) {

  }

  protected boolean generateForDimension(final int dim, World world, Random random, int posX,
      int posZ) {

    return false;
  }
}
