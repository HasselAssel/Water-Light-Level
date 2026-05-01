package io.github.hasselassel.waterlightlevel;

@FunctionalInterface
public interface IntIntToChunkConsumer {
    Chunk accept(int b, int c);
}
