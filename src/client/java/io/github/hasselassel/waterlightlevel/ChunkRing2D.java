package io.github.hasselassel.waterlightlevel;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChunkRing2D implements Iterable<Chunk> {
    private final Chunk[][] data;
    private final int sizeX;
    private final int sizeZ;
    private int centerX = 0;
    private int centerZ = 0;

    public ChunkRing2D(int sizeX, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.data = new Chunk[sizeX][sizeZ];
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                this.data[x][z] = new Chunk(0, 0, 0, 0);
            }
        }
    }

    private static int wrap(int value, int size) {
        return Math.floorMod(value, size);
    }

    public Chunk getLogical(int x, int z) {
        return data[wrap(x + centerX, sizeX)][wrap(z + centerZ, sizeZ)];
    }

    public void shift(int dx, int dz) {
        centerX = wrap(centerX + dx, sizeX);
        centerZ = wrap(centerZ + dz, sizeZ);
    }

    public void shift(int dx, int dz, ChunkIntIntConsumer update) {
        int counter = 0;
        this.shift(dx, dz);

        int xUp = sizeX / 2;
        int zUp = sizeZ / 2;

        int xLow = xUp - sizeX + 1;
        int zLow = zUp - sizeZ + 1;

        int absDx = Math.min(Math.abs(dx), sizeX);
        int absDz = Math.min(Math.abs(dz), sizeZ);

        if (dx < 0) {
            for (int x = xLow; x < xLow + absDx; x++) {
                for (int z = zLow; z <= zUp; z++) {
                    update.accept(getLogical(x, z), x, z);
                    counter++;
                }
            }
        } else if (dx > 0) {
            for (int x = xUp; x > xUp - absDx; x--) {
                for (int z = zLow; z <= zUp; z++) {
                    update.accept(getLogical(x, z), x, z);
                    counter++;
                }
            }
        }

        int zXMin = xLow + (dx < 0 ? absDx : 0);
        int zXMax = xUp - (dx > 0 ? absDx : 0);

        if (dz < 0) {
            for (int z = zLow; z < zLow + absDz; z++) {
                for (int x = zXMin; x <= zXMax; x++) {
                    update.accept(getLogical(x, z), x, z);
                    counter++;
                }
            }
        } else if (dz > 0) {
            for (int z = zUp; z > zUp - absDz; z--) {
                for (int x = zXMin; x <= zXMax; x++) {
                    update.accept(getLogical(x, z), x, z);
                    counter++;
                }
            }
        }
        System.out.println("COUNTER: " + counter);
    }

    @Override
    public @NotNull Iterator<Chunk> iterator() {
        return new Iterator<>() {
            private int x = 0;
            private int z = 0;

            @Override
            public boolean hasNext() {
                return x < sizeX && z < sizeZ;
            }

            @Override
            public Chunk next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Chunk result = data[x][z];
                z++;
                if (z == sizeZ) {
                    z = 0;
                    x++;
                }
                return result;
            }
        };
    }
}

