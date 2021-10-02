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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.Style;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

final record TranslatableComponentImpl(
  @NotNull List<Component> children,
  @NotNull Style style,
  @NotNull String key,
  @NotNull List<Component> args
) implements TranslatableComponent {
  TranslatableComponentImpl(final @NotNull List<Component> children, final @NotNull Style style, final @NotNull String key, final @NotNull List<Component> args) {
    this.children = List.copyOf(children);
    this.style = style;
    this.key = key;
    this.args = List.copyOf(args);
  }

  @Override
  public @NotNull TranslatableComponent key(final @NotNull String key) {
    if (Objects.equals(this.key, key)) return this;
    return new TranslatableComponentImpl(this.children, this.style, requireNonNull(key, "key"), this.args);
  }

  @Override
  public @NotNull TranslatableComponent args(final @NotNull ComponentLike@NotNull... args) {
    return new TranslatableComponentImpl(this.children, this.style, this.key, ComponentLike.asComponents(args));
  }

  @Override
  public @NotNull TranslatableComponent args(final @NotNull List<? extends ComponentLike> args) {
    return new TranslatableComponentImpl(this.children, this.style, this.key, ComponentLike.asComponents(args));
  }

  @Override
  public @NotNull TranslatableComponent children(final @NotNull List<? extends ComponentLike> children) {
    return new TranslatableComponentImpl(ComponentLike.asComponents(children, NOT_EMPTY), this.style, this.key, this.args);
  }

  @Override
  public @NotNull TranslatableComponent style(final @NotNull Style style) {
    return new TranslatableComponentImpl(this.children, style, this.key, this.args);
  }

  @Override
  public @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
    return Stream.concat(
      Stream.of(
        ExaminableProperty.of("key", this.key),
        ExaminableProperty.of("args", this.args)
      ),
      TranslatableComponent.super.examinableProperties()
    );
  }

  @Override
  public String toString() {
    return this.examine(StringExaminer.simpleEscaping());
  }

  @Override
  public @NotNull Builder toBuilder() {
    return new BuilderImpl(this);
  }

  static final class BuilderImpl extends AbstractComponentBuilder<TranslatableComponent, Builder> implements TranslatableComponent.Builder {
    private @Nullable String key;
    private List<Component> args = Collections.emptyList();

    BuilderImpl() {
    }

    BuilderImpl(final @NotNull TranslatableComponent component) {
      super(component);
      this.key = component.key();
      this.args = component.args();
    }

    @Override
    public @NotNull Builder key(final @NotNull String key) {
      this.key = key;
      return this;
    }

    @Override
    public @NotNull Builder args(final @NotNull ComponentBuilder<?, ?> arg) {
      return this.args(Collections.singletonList(arg.build()));
    }

    @Override
    @SuppressWarnings("checkstyle:GenericWhitespace")
    public @NotNull Builder args(final @NotNull ComponentBuilder<?, ?>@NotNull... args) {
      if (args.length == 0) return this.args(Collections.emptyList());
      return this.args(Stream.of(args).map(ComponentBuilder::build).collect(Collectors.toList()));
    }

    @Override
    public @NotNull Builder args(final @NotNull Component arg) {
      return this.args(Collections.singletonList(arg));
    }

    @Override
    public @NotNull Builder args(final @NotNull ComponentLike@NotNull... args) {
      if (args.length == 0) return this.args(Collections.emptyList());
      return this.args(Arrays.asList(args));
    }

    @Override
    public @NotNull Builder args(final @NotNull List<? extends ComponentLike> args) {
      this.args = ComponentLike.asComponents(args);
      return this;
    }

    @Override
    public @NotNull TranslatableComponentImpl build() {
      if (this.key == null) throw new IllegalStateException("key must be set");
      return new TranslatableComponentImpl(this.children, this.buildStyle(), this.key, this.args);
    }
  }
}
