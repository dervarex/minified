/**
 * <pre>
 * NBT(Named Binary Tag) and MCA(Minecraft Anvil Files) parsing utility.
 *
 * Note: We do not support LZ4 Compressed Chunk files, since it's just
 * a flag in the server.properties file (building an LZ4 Parser is a
 * lot of effort, and I do not want to add a library just for this)
 * </pre>
 */
package com.dervarex.minified.utils.nbt;