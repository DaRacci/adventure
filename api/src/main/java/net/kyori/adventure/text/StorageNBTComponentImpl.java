/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2021 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.Style;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final record StorageNBTComponentImpl(
  @NotNull List<Component> children,
  @NotNull Style style,
  @NotNull String nbtPath,
  boolean interpret,
  @Nullable Component separator,
  @NotNull Key storage
) implements StorageNBTComponent {
  @Override
  public @NotNull StorageNBTComponent nbtPath(final @NotNull String nbtPath) {
    if (Objects.equals(this.nbtPath, nbtPath)) return this;
    return new StorageNBTComponentImpl(this.children, this.style, nbtPath, this.interpret, this.separator, this.storage);
  }

  @Override
  public @NotNull StorageNBTComponent interpret(final boolean interpret) {
    if (this.interpret == interpret) return this;
    return new StorageNBTComponentImpl(this.children, this.style, this.nbtPath, interpret, this.separator, this.storage);
  }

  @Override
  public @NotNull StorageNBTComponent separator(final @Nullable ComponentLike separator) {
    return new StorageNBTComponentImpl(this.children, this.style, this.nbtPath, this.interpret, ComponentLike.unbox(separator), this.storage);
  }

  @Override
  public @NotNull StorageNBTComponent storage(final @NotNull Key storage) {
    if (Objects.equals(this.storage, storage)) return this;
    return new StorageNBTComponentImpl(this.children, this.style, this.nbtPath, this.interpret, this.separator, storage);
  }

  @Override
  public @NotNull StorageNBTComponent children(final @NotNull List<? extends ComponentLike> children) {
    return new StorageNBTComponentImpl(ComponentLike.asComponents(children, NOT_EMPTY), this.style, this.nbtPath, this.interpret, this.separator, this.storage);
  }

  @Override
  public @NotNull StorageNBTComponent style(final @NotNull Style style) {
    return new StorageNBTComponentImpl(this.children, style, this.nbtPath, this.interpret, this.separator, this.storage);
  }

  @Override
  public @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
    return Stream.concat(
      Stream.of(
        ExaminableProperty.of("storage", this.storage)
      ),
      StorageNBTComponent.super.examinableProperties()
    );
  }

  @Override
  public String toString() {
    return this.examine(StringExaminer.simpleEscaping());
  }

  @Override
  public StorageNBTComponent.@NotNull Builder toBuilder() {
    return new BuilderImpl(this);
  }

  static class BuilderImpl extends AbstractNBTComponentBuilder<StorageNBTComponent, Builder> implements StorageNBTComponent.Builder {
    private @Nullable Key storage;

    BuilderImpl() {
    }

    BuilderImpl(final @NotNull StorageNBTComponent component) {
      super(component);
      this.storage = component.storage();
    }

    @Override
    public StorageNBTComponent.@NotNull Builder storage(final @NotNull Key storage) {
      this.storage = storage;
      return this;
    }

    @Override
    public @NotNull StorageNBTComponent build() {
      if (this.nbtPath == null) throw new IllegalStateException("nbt path must be set");
      if (this.storage == null) throw new IllegalStateException("storage must be set");
      return new StorageNBTComponentImpl(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.separator, this.storage);
    }
  }
}
