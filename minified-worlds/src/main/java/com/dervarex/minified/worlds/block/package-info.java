/**
 * Block states as they are stored in a chunk's block palette
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.block.BlockState}: a block and its properties, like
 *     {@code minecraft:oak_log} with {@code axis=x}</li>
 *     <li>{@link com.dervarex.minified.worlds.block.BlockStateFormat}: how a palette entry looks in NBT, changed in DataVersion 5006</li>
 * </ul>
 * {@link com.dervarex.minified.worlds.block.BlockState#fromNbt(com.dervarex.minified.utils.nbt.tag.NbtTag)} can read
 * both formats. For writing, get the format for the world's DataVersion with
 * {@link com.dervarex.minified.worlds.block.BlockStateFormat#forDataVersion(int)} and give the state to
 * {@link com.dervarex.minified.worlds.block.BlockStateFormat#toNbt(BlockState) toNbt}.
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.block;

import org.apiguardian.api.API;
